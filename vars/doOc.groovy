import com.gpn.pipeline.OcFunction

/**
* Executes ocp functions
* @param func String 
* @param parameters Map with specific for place parameters. For now registryCred is for Jenkins credId and registry or urlTarget for path of place
*/
def call(String func, Map parameters = [:]) {
    OcFunction ocFunc = new OcFunction(this)

    switch(func) {
        case "login":
            println "=====ocp login====="
            ocFunc.ocLogin(parameters.ocpCredId, 
                           parameters.ocpUrlTarget, 
                           parameters.ocpNamespace)
            break
        case "installMoonTemplate":
            println "=== ocp install Moon template ==="
            ocFunc.ocInstallSpecTemplate(parameters.quayRegistry,
                                        parameters.fileTemplateName, 
                                        parameters.ocpNamespace, 
                                        parameters.moonLicPods,
                                        parameters.moonReplicas,
                                        parameters.moonLicCredId,
                                        parameters.entityUid,
                                        parameters.entityGid,
                                        parameters.postfixSA)
            break
        case "createSecret":
            println "=====ocp create secrets for registry====="
            ocFunc.ocCreateSecrets(parameters.registryCredId, 
                                parameters.registryList, 
                                parameters.registryPort, 
                                parameters.ocpNamespace)
            break
        case "tag":
            println "=====ocp tag new image as latest====="
            ocFunc.ocTag(parameters.registry,
                        parameters.ocpNamespace,
                        parameters.ocpAppName,
                        parameters.gitCommitShort)
            break
        case "checkIS":
            println "=====ocp check imagestream====="
            if (ocFunc.ocDescribeIS(parameters.ocpAppName, parameters.ocpNamespace)) {
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
            ocFunc.ocCreateApp(parameters.ocpNamespace, 
                               parameters.templateName, 
                               parameters.templateParams)
            break
        case "deployTemplate":
            println "=====ocp deploy/change templates====="
            ocFunc.ocDeployTemplate(parameters.ocpNamespace)
            break
        case "applyApp":
            println "=====ocp apply app====="
            ocFunc.ocApplyApp(parameters.ocpNamespace, 
                              parameters.templateName, 
                              parameters.templateParams)
            break
        
        case "status":
            println "====ocp get status===="
            ocFunc.ocGetDcStatus(parameters.ocpAppName)
            break

        case "deleteAll":
            println "=====ocp delete all====="
            ocFunc.ocDeleteAll(parameters.ocpNamespace, 
                               parameters.ocpLabel, 
                               parameters.ocpAppName)
            break
        case "destroyApp":
            println "=====ocp destroy all====="
            ocFunc.ocDeleteAll(parameters.ocpNamespace, 
                               parameters.ocpLabel, 
                               parameters.ocpAppName)
            ocFunc.ocDestroyApp(parameters.ocpNamespace, 
                               parameters.ocpLabel, 
                               parameters.ocpAppName,
                               parameters.quotaName)
    }
}