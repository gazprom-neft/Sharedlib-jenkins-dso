import com.gpn.pipeline.OcFunction
import com.gpn.pipeline.ExtendDescription

/**
* Executes ocp functions
* @param func String 
* @param parameters Map with specific for place parameters. For now registryCred is for Jenkins credId and registry or urlTarget for path of place
*/
def call(String func, Map parameters = [:]) {
    OcFunction ocFunc = new OcFunction(this)
    ExtendDescription extDesc = new ExtendDescription(this)
    def ocpCredId = parameters.ocpCredId ?: ""
    def ocpUrlTarget = parameters.ocpUrlTarget ?: ""
    def ocpNamespace = parameters.ocpNamespace ?: ""
    def vaultUrl = parameters.vaultUrl ?: ""
    def secretPrefixPath = parameters.secretPrefixPath ?: ""
    def secretPath = parameters.secretPath ?: ""
    def quayRegistry = parameters.quayRegistry ?: ""
    def fileTemplateName = parameters.fileTemplateName ?: ""
    def moonLicPods = parameters.moonLicPods ?: ""
    def moonReplicas = parameters.moonReplicas ?: ""
    def moonLicCredId = parameters.moonLicCredId ?: ""
    def entityUid = parameters.entityUid ?: ""
    def entityGid = parameters.entityGid ?: ""
    def postfixSA = parameters.postfixSA ?: ""
    def registryCredId = parameters.registryCredId ?: ""
    def registryList = parameters.registryList ?: ""
    def registryPort = parameters.registryPort ?: ""
    def registry = parameters.registry ?: ""
    def ocpAppName = parameters.ocpAppName ?: ""
    def gitCommitShort = parameters.gitCommitShort ?: ""
    def templateName = parameters.templateName ?: ""
    def templateParams = parameters.templateParams ?: ""
    def ocpLabel = parameters.ocpLabel ?: ""
    def quotaName = parameters.quotaName ?: ""
    def registry_path
    def image_name
    def image_tag

    if (parameters.registry_path) {
        registry_path = parameters.registry_path.toLowerCase()
    } else {
        registry_path = ocpNamespace
    }

    if (parameters.image_name) {
        image_name = parameters.image_name
    } else {
        image_name = ocpAppName
    }

    if (parameters.image_tag) {
        image_tag = parameters.image_tag
    } else {
        image_tag = gitCommitShort
    }

    switch(func) {
        case "login":
            println "=====ocp login====="
            ocFunc.ocLogin(ocpCredId, ocpUrlTarget, ocpNamespace)
            break
        case "loginVault":
            println "=====ocp login with vault====="
            ocFunc.ocLoginVault(ocpCredId, ocpUrlTarget, ocpNamespace, vaultUrl, secretPrefixPath, secretPath)
            break
        case "installMoonTemplate":
            println "=== ocp install Moon template ==="
            ocFunc.ocInstallSpecTemplate(quayRegistry, fileTemplateName, ocpNamespace, moonLicPods, moonReplicas, moonLicCredId, entityUid, entityGid, postfixSA)
            break
        case "createSecret":
            println "=====ocp create secrets for registry====="
            ocFunc.ocCreateSecrets(registryCredId, registryList, registryPort, ocpNamespace)
            break
        case "tag":
            println "=====ocp tag new image as latest====="
            ocFunc.ocTag(registry, registry_path, image_name, image_tag)
            // add string to extend description
            extDesc.addString('IMAGE_NAME', image_name)
            extDesc.addString('IMAGE_TAG', image_tag)
            break
        case "checkIS":
            println "=====ocp check imagestream====="
            if (ocFunc.ocDescribeIS(image_name, ocpNamespace)) {
                return false;
            }
            else {
                currentBuild.result = "FAILURE"
                throw new Exception("Throw to stop pipeline")
            }
            break
        // Template functions
        case "createTemplate":
            println "=====ocp create app====="
            ocFunc.ocCreateApp(ocpNamespace, templateName, templateParams)
            break
        case "deployTemplate":
            println "=====ocp deploy/change templates====="
            ocFunc.ocDeployTemplate(ocpNamespace)
            break
        case "applyApp":
            println "=====ocp apply app====="
            ocFunc.ocApplyApp(ocpNamespace, templateName, templateParams)
            break
        
        case "status":
            println "====ocp get status===="
            ocFunc.ocGetDcStatus(ocpAppName)
            break

        case "deleteAll":
            println "=====ocp delete all====="
            ocFunc.ocDeleteAll(ocpNamespace, ocpLabel, ocpAppName)
            break
        case "destroyApp":
            println "=====ocp destroy all====="
            ocFunc.ocDeleteAll(ocpNamespace, ocpLabel, ocpAppName)
            ocFunc.ocDestroyApp(ocpNamespace, ocpLabel, ocpAppName, quotaName)
    }
}