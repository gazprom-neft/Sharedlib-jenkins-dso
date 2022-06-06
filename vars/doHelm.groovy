import com.gpn.pipeline.HelmFunction

def call(String func, Map parameters = [:]) {
    def helmPath = parameters.helm_path ?: "/ci/helm/"
    def kubeconfig = parameters.kubeconfig ?: ".kube.cfg"
    def helmImage = parameters.helm_image ?: "dso/helm"
    def helmTag = parameters.helm_tag ?: "latest"
    def helmValues = parameters.helm_values ?: "values.yaml"
    def helmChart = parameters.helm_chart ?: ""
    def helmCommands = parameters.helm_commands ?: ""
    def helmRegistry = parameters.helm_registry ?: ""
    def helmRegistryCredId = parameters.helm_registry_cred_id ?: ""
    def fromRepo = parameters.from_repo ?: false
    def appRev = parameters.app_rev ?: ""
    def appName = parameters.app_name ?: params.OCP_APP_NAME
    withFolderProperties {namespace = parameters.namespace ?: env.OCP_NAMESPACE}

    if (helmRegistry == "") {
        withFolderProperties {(helmRegistry, helmRegistryCredId) = getHelmRegistry(env.DEPLOY_ENVIRONMENT)} 
    }
    withFolderProperties {(dockerRegistry, dockerRegistryCredId) = getRegistry(env.DEPLOY_ENVIRONMENT)} 
    HelmFunction helm = new HelmFunction(this, dockerRegistry, dockerRegistryCredId, helmImage, helmTag, helmRegistry, helmRegistryCredId)

    switch(func) {
        case "install":
            helm.install(kubeconfig, helmPath, helmValues, helmChart, appName, namespace, helmCommands, fromRepo)
            break
        case "publish":
            helm.publish(kubeconfig, helmPath, helmValues, helmChart, appName, namespace, helmCommands)
            break
        case "rollback":
            helm.rollback(kubeconfig, appName, appRev, namespace, helmCommands)
            break
        case "uninstall":
            helm.uninstall(kubeconfig, appName, namespace, helmCommands)
            break
        default:
            helm.abortBuild("Error: unknown 'doHelm' function - '${func}'.")
    }
}

void getRegistry(String deployEnv) {
    switch(deployEnv) {
        case 'dev':
            return [env.REGISTRY_DEV, env.REGISTRY_DEV_CRED_ID]
            break
        case 'trust':
            return [env.REGISTRY_TRUST, env.REGISTRY_TRUST_CRED_ID]
            break
        default:
            abortBuild("Error: you must set 'DEPLOY_ENVIRONMENT' strictrly to 'dev' or 'trust', but it is '${deployEnv}'")
    }
}

void getHelmRegistry(String deployEnv) {
    switch(deployEnv) {
        case 'dev':
            return [env.REPO_DEV_HELM, env.REPO_DEV_HELM_CRED_ID]
            break
        case 'trust':
            return [env.REPO_TRUST_HELM, env.REPO_TRUST_HELM_CRED_ID]
            break
        default:
            abortBuild("Error: you must set 'DEPLOY_ENVIRONMENT' strictrly to 'dev' or 'trust', but it is '${deployEnv}'")
    }
}

void abortBuild(String error) {
    echo(error)
    currentBuild.result = "ABORTED"
    error("Aborting the build.")
}