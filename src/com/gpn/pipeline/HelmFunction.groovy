package com.gpn.pipeline

class HelmFunction {
    
    private Script script
    String dockerRegistry
    String dockerRegistryCredId
    String helmRegistry
    String helmRegistryCredId
    String helmImage
    String helmTag

    HelmFunction(Script script, String dockerRegistry, String dockerRegistryCredId, String helmImage, String helmTag, String helmRegistry, String helmRegistryCredId) {
        this.script = script
        this.dockerRegistry = dockerRegistry
        this.dockerRegistryCredId = dockerRegistryCredId
        this.helmRegistry = helmRegistry
        this.helmRegistryCredId = helmRegistryCredId
        this.helmImage = helmImage
        this.helmTag = helmTag
    }

    void install(String kubeconfig, String helmPath, String helmValues, String helmChart, String appName, String namespace, String helmCommands, Boolean fromRepo) {
        checkParams(appName, namespace, helmChart)
        script.docker.withRegistry("https://${this.dockerRegistry}", "${this.dockerRegistryCredId}") {
            script.docker.image("${this.helmImage}:${this.helmTag}").inside("-e KUBECONFIG=${script.env.WORKSPACE}/${kubeconfig}") {
                if (fromRepo) {
                    helmRemoteInstall(helmChart, appName, namespace, helmCommands)
                } else {
                    helmLocalInstall(helmPath, helmValues, helmChart, appName, namespace, helmCommands)
                }
            }
        }
    }

    void publish(String kubeconfig, String helmPath, String helmValues, String helmChart, String appName, String namespace, String helmCommands) {
        checkParams(appName, namespace, helmChart)
        script.docker.withRegistry("https://${this.dockerRegistry}", "${this.dockerRegistryCredId}") {
            script.docker.image("${this.helmImage}:${this.helmTag}").inside("-e KUBECONFIG=${script.env.WORKSPACE}/${kubeconfig}") {
                helmPublish(helmGetChartYaml(helmPath, helmChart), helmPath, helmChart)
            }
        }
    }

    void uninstall(String kubeconfig, String appName, String namespace, String helmCommands) {
        checkParams(appName, namespace, "void")
        script.docker.withRegistry("https://${this.dockerRegistry}", "${this.dockerRegistryCredId}") {
            script.docker.image("${this.helmImage}:${this.helmTag}").inside("-e KUBECONFIG=${script.env.WORKSPACE}/${kubeconfig}") {
                helmUninstall(appName, namespace, helmCommands)
            }
        }
    }

    void rollback(String kubeconfig, String appName, String appRev, String namespace, String helmCommands) {
        checkParams(appName, namespace, "void")
        script.docker.withRegistry("https://${this.dockerRegistry}", "${this.dockerRegistryCredId}") {
            script.docker.image("${this.helmImage}:${this.helmTag}").inside("-e KUBECONFIG=${script.env.WORKSPACE}/${kubeconfig}") {
                helmRollback(appName, appRev, namespace, helmCommands)
            }
        }
    }

    void abortBuild(String error) {
        script.echo(error)
        script.currentBuild.result = "ABORTED"
        script.error("Aborting the build.")
    }

    private void checkParams(String appName, String namespace, String helmChart) {
        checkConstructorParams()
        if(!appName) {
            abortBuild("Error: you must set 'appName' parameter")
        } else if(!namespace) {
            abortBuild("Error: you must set 'namespace' parameter")
        } else if(!helmChart) {
            abortBuild("Error: you must set 'helmChart' parameter")
        }
    }

    private void checkConstructorParams() {
        if(!this.dockerRegistry) {
            abortBuild("Error: you must set 'dockerRegistry' parameter")
        } else if(!this.dockerRegistryCredId) {
            abortBuild("Error: you must set 'dockerRegistryCredId' parameter")
        } else if(!this.helmRegistry) {
            abortBuild("Error: you must set 'helmRegistry' parameter")
        } else if(!this.helmRegistryCredId) {
            abortBuild("Error: you must set 'helmRegistryCredId' parameter")
        }
    }

    private void helmLocalInstall(String helmPath, String helmValues, String helmChart, String appName, String namespace, String helmCommands) {
        script.sh """ 
            helm upgrade --install ${appName} \
            ${script.env.WORKSPACE}${helmPath}${helmChart} \
            --namespace ${namespace} \
            -f ${script.env.WORKSPACE}${helmPath}${helmChart}/${helmValues} \
            ${helmCommands}
        """
    }

    private void helmRemoteInstall(String helmChart, String appName, String namespace, String helmCommands) {
        script.sh """
            set +x
            export HELM_CONFIG_HOME=/tmp
            export HELM_CACHE_HOME=/tmp
            helm repo add ${helmChart}-repo ${this.helmRegistry}
            helm repo update
            set -x
            helm upgrade --install ${appName} \
            ${helmChart}-repo/${helmChart} \
            --namespace ${namespace} \
            ${helmCommands}
        """
    }

    private def helmGetChartYaml(String helmPath, String helmChart) {
        def chartYaml = script.sh(script: "set +x && helm show chart ${script.env.WORKSPACE}${helmPath}${helmChart} && set -x", returnStdout: true)
        return script.readYaml(text: chartYaml)
    }

    private void helmPublish(chartYaml, String helmPath, String helmChart) {
        script.withCredentials([script.usernamePassword(credentialsId: "${this.helmRegistryCredId}", usernameVariable: 'USERNAME', passwordVariable: 'PASSWORD')]) {
            def execScript = """
                set +x
                helm package ${script.env.WORKSPACE}${helmPath}${helmChart}
                curl -fsSL -u ${script.USERNAME}:${script.PASSWORD} ${this.helmRegistry} --upload-file ${script.env.WORKSPACE}/${chartYaml.name}-${chartYaml.version}.tgz
                set -x
            """
            def status = script.sh(script: execScript, returnStatus: true)
            if (status != 0) {
                abortBuild("Error: upload to remote registry failed")
            } else {
                script.echo("Upload successful")
            }
        }
    }

    private void helmUninstall(String appName, String namespace, String helmCommands) {
        script.sh """ 
            helm uninstall ${appName} --namespace ${namespace} ${helmCommands}
        """
    }

    private void helmRollback(String appName, String appRev, String namespace, String helmCommands) {
        script.sh """ 
            set +x
            helm rollback ${appName} ${appRev} --namespace ${namespace} ${helmCommands}
            set -x
            helm history ${appName} --namespace ${namespace}
        """
    }
}