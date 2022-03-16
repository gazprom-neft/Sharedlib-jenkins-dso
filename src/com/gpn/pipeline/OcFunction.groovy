package com.gpn.pipeline

class OcFunction {

    private Script script

    OcFunction(Script script) {
        this.script = script
    }
    /**
        Login to ocp cluster
        @param ocpCredId ID of cred for login into OKD/OCP cluster
        @param ocpUrlTarget Path of desired cluster
        @param ocpNamespace OCP Namespace
    */
    public void ocLogin(String ocpCredId, String ocpUrlTarget, String ocpNamespace) {
        script.withCredentials([script.string(
            credentialsId: ocpCredId,
            variable: 'TOKEN'
        )]) {
            script.sh """
                oc login $ocpUrlTarget --token $script.TOKEN --namespace $ocpNamespace
            """
        }
    }
    /**
        Login to ocp cluster with Vault
        @param ocpCredId ID of cred for login into OKD/OCP cluster
        @param ocpUrlTarget Path of desired cluster
        @param ocpNamespace OCP Namespace
        @param vaultURL URL to Hashicorp Vault
        @param secretPrefixPath Path to project area on Vault
        @param secretPath Path to secret in vault project area
    */
    public void ocLoginVault(String ocpCredId, String ocpUrlTarget, String ocpNamespace, String vaultUrl, String secretPrefixPath, String secretPath) {
        def vaultSecret = [[path: secretPath, engineVersion: 2, secretValues: [[envVar: 'TOKEN', vaultKey: 'token']]]]
        def vaultConf = [vaultUrl: vaultUrl, vaultCredentialId: ocpCredId, prefixPath: secretPrefixPath, engineVersion: 2]
        script.withVault([
            configuration: vaultConf,
            vaultSecrets: vaultSecret
        ]) {
            script.sh """
                oc login $ocpUrlTarget --token $script.TOKEN --namespace $ocpNamespace
            """
        }
    }
    /**
        Get UID or GID
        @param entity string
        @param ocpNamespace OCP Namespace
    */
    public String ocGetGUID(String entity, String ocpNamespace) {
        def namespace = ocpNamespace.toLowerCase()
        def t = script.sh(script: "oc describe project $namespace | grep -Po '$entity=\\d+' | grep -Po '\\d+'", returnStdout: true)
        return t.trim()
    }
    /**
        Install Moon (or like this) template
        @param ocpNamespace OCP Namespace
        @param quayRegistry link for Quay Registry
        @param fileTemplateName name of template file
        @param moonLicPods
        @param moonReplicas
        @param moonLicCredId
        @param entityUid param for grep to find UID entry (may be default)
        @param entityGid param for grep to find GID entry (may be default)
        @param postfixSA last part of service account name, should be stage name in lower case, like 'dev' (may be default)
    */
    public void ocInstallSpecTemplate(String quayRegistry, String fileTemplateName, String ocpNamespace, String moonLicPods, String moonReplicas, String moonLicCredId, String entityUid="", String entityGid="", String postfixSA="") {
        if (entityUid == null || entityUid == "") {
            entityUid = "uid-range"
        }
        if (entityGid == null || entityGid == "") {
            entityGid = "supplemental-groups"
        }

        // required unification from CaaS side with service naming
        def serviceAccount = ocpNamespace.toLowerCase() + "-sa-" + postfixSA
        def moonPods = moonLicPods.toInteger() + moonReplicas.toInteger()

        def ocpUid = ocGetGUID(entityUid, ocpNamespace)
        def ocpGid = ocGetGUID(entityGid, ocpNamespace)

        script.withCredentials([script.string(
            credentialsId: moonLicCredId,
            variable: 'KEY'
        )]) {
            script.sh """
                oc process -n $ocpNamespace \
                    -f $fileTemplateName \
                    -p NAMESPACE=$ocpNamespace \
                    -p BROWSER_UID=${ocpUid} \
                    -p BROWSER_GID=${ocpGid} \
                    -p MOON_REGISTRY_NAME=$quayRegistry \
                    -p SERVICE_ACCOUNT=${serviceAccount} \
                    -p LIC_KEY=$script.KEY \
                    -p MOON_PODS=${moonPods} \
                    -p MOON_REPLICAS=$moonReplicas \
                    -o yaml | oc apply -f - -n $ocpNamespace
            """
        }
    }
    /**
        Create OCP secrets for registry
        @param registryCredId ID of cred for login into Docker Registry
        @param registryList List of registries
        @param registryPort Port of registry
        @param ocpNamespace OCP Namespace
    */
    public void ocCreateSecrets(String registryCredId, String registryList, String registryPort, String ocpNamespace) {
        script.withCredentials([script.usernamePassword(
            credentialsId: registryCredId,
            usernameVariable: 'USERNAME',
            passwordVariable: 'PASSWORD'
        )]) {
            script.sh """
                set +e
                for i in $registryList
                do
                oc create secret docker-registry \${i} -n '$ocpNamespace' --docker-server=\${i}'$registryPort' --docker-username='$script.USERNAME' --docker-password='$script.PASSWORD'
                oc secrets link --for=pull default \${i} -n '$ocpNamespace'
                done
                set -e
            """
        }
    }
    /**
        Tag new image as latest
        @param registry URL of desired cluster
        @param ocpNamespace OCP Namespace
        @param ocpAppName name of deployed application
        @param gitCommitShort short hash of commit
    */
    public void ocTag(String registry, String ocpNamespace, String ocpAppName, String gitCommitShort) {
        script.sh """
            oc tag $registry/$ocpNamespace/$ocpAppName:$gitCommitShort $ocpAppName:latest
        """
    }
    /**
        Get status of image stream
        @param ocpAppName name of deployed application
        @param ocpNamespace OCP Namespace
    */
    public String ocDescribeIS(String ocpAppName, String ocpNamespace) {
        return script.sh(script: "oc describe is/$ocpAppName -n $ocpNamespace | grep NotFound", returnStatus: true)
    }
    /**
        Show the status of deployment config rollout
        @param ocpAppName name of deployed application
    */
    public void ocGetDcStatus(String ocpAppName) {
        script.sh("""
            oc rollout status dc/$ocpAppName | grep "successfully rolled out"
        """)
    }
    /**
        Deploy or change templates from specific directory
        @param ocpNamespace OCP Namespace
    */
    public void ocDeployTemplate(String ocpNamespace) {
        script.sh """
            for i in `find ci/okd/ -iname '*template*.yaml'`
            do
            oc apply -n $ocpNamespace -f \${i}
            done
        """
    }
    /**
        Deploy or change template by name
        @param ocpNamespace OCP Namespace
        @param templateName
    */
    public void ocApplyTemplate(String ocpNamespace, String templateName) {
        script.sh """
            oc apply -n $ocpNamespace -f $templateName
        """
    }
    // TODO: Check if templateParams is empty
    /**
        Apply application
        @param ocpNamespace OCP Namespace
        @param templateName name of template
        @param templateParams additional parameters
    */
    public void ocApplyApp(String ocpNamespace, String templateName, String templateParams) {
        script.sh """
            set +e
            oc process -n $ocpNamespace $templateName $templateParams | oc apply -f -
            set -e
        """
    }
    /**
        Create application
        @param ocpNamespace OCP Namespace
        @param templateName name of template
        @param templateParams additional parameters
    */
    public void ocCreateApp(String ocpNamespace, String templateName, String templateParams) {
        script.sh """
            set +e
            oc process -n $ocpNamespace $templateName $templateParams | oc create -f -
            set -e
        """
    }
    /**
        Delete all
        @param ocpNamespace OCP Namespace
        @param ocpLabel OCP Label
        @param ocpAppName name of deployed application
    */
    public void ocDeleteAll(String ocpNamespace, String ocpAppName, String ocpLabel="") {
        if (ocpLabel == null || ocpLabel == "") {
            ocpLabel = "app"
        }
        script.sh """
            oc delete all --selector $ocpLabel=$ocpAppName -n $ocpNamespace
        """
    }
    /**
        Destroy all
        @param ocpNamespace OCP Namespace
        @param ocpLabel OCP Label
        @param ocpAppName name of application
        @param ocpAppQuotaName quota name of application
    */
    public void ocDestroyApp(String ocpNamespace, String ocpAppName, String ocpLabel="", String quotaName="") {
        if (quotaName == null || quotaName == "") {
            quotaName = "max-moon-sessions"
        }
        if (ocpLabel == null || ocpLabel == "") {
            ocpLabel = "app"
        }
        script.sh """
            oc delete configmap --selector $ocpLabel=$ocpAppName -n $ocpNamespace || :
            oc delete secret --selector $ocpLabel=$ocpAppName -n $ocpNamespace || :
            oc delete quota $quotaName -n $ocpNamespace || :
        """
    }  
}