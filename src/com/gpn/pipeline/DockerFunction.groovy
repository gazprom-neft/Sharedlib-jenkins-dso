package com.gpn.pipeline

class DockerFunction {
    
    private Script script

    DockerFunction(Script script) {
        this.script = script
    }
    private String retryScript(scriptblock, max = 5, SleepSeconds = 3) {
        def res = ""

        for (def i=1; i<=max;) {
            i++
            try {
                res += script.sh(script: scriptblock, returnStdout: true) as String
                return res
            } catch(e) {
                script.sleep(SleepSeconds)
                continue
            }
        }

        script.error("Невозможно выполнить скрипт. Превышено максимальное количечество попыток. " + res)
    }
    private String parsedAditionalArgs (aditionalArgs) {
        def args = ""
        def result = ""
        def t = ""
        if ((aditionalArgs instanceof String  || aditionalArgs instanceof GString) && aditionalArgs.length() > 0){
            List list = aditionalArgs.split(',')
            List tmp = []
            for (v in list) {
                //args = "$args" + "--build-arg" + ' ' + i + ' '
                t = v.replaceAll("--build-arg", "").trim()
                for (vv in t.split(' ')) {
                    if (vv != '') {
                        tmp << vv
                    }
                }
            }
            for (v in tmp) {
                result += "--build-arg $v "
            }
            return result
        }
        else if (aditionalArgs instanceof List && aditionalArgs.size() > 0){
            def list = aditionalArgs
            for(i in list){
                args = "$args" + "--build-arg" + ' ' + i.replace("--build-arg", '') + ' '
                
            }
            return args
        }
        else{ 
            return args
        }
    }   
    /**
        Login into docker registry
        @param registryCredId ID of cred for login to docker registry
        @param registry URL of desired docker-registry service
        @param registry_path project_id any ocp_namespace
    */
    public void dockerLogin(String registryCredId, String registry) {
        script.withCredentials([script.usernamePassword(
                                credentialsId: registryCredId, 
                                usernameVariable: 'USERNAME', 
                                passwordVariable: 'PASSWORD')]) {
            retryScript("docker --config '.project_docker_config' login '$registry' -u '$script.USERNAME' -p '$script.PASSWORD'")
        }
    }
    /**
        Docker build
        @param registry URL of desired docker-registry service
        @param registry_path project_id any ocp_namespace
        @param image_name name used for application
        @param image_tag docker image tag
        @param dockerfileName Dockerfile name
        @param additionalArgs extra variable, should consist of command flag (for ex., -build-arg) and value
    */
    public void dockerBuild(String registry, String registry_path, String image_name, String image_tag, String dockerfileName, additionalArgs) {
        def strArgs = parsedAditionalArgs (additionalArgs)
        script.sh """
            docker --config '.project_docker_config' build -t '$registry/$registry_path/$image_name:$image_tag' -f $dockerfileName $strArgs .
        """
    }
    /**
        Docker push
        @param registry URL of desired docker-registry service
        @param registry_path project_id any ocp_namespace
        @param image_name name used for application
        @param image_tag docker image tag
    */
    public void dockerPush(String registry, String registry_path, String image_name, String image_tag) {
        script.sh """
            docker --config '.project_docker_config' push '$registry/$registry_path/$image_name:$image_tag'
        """
    }
    /**
        Docker logout
        for exclude cash use
        @param registry URL of desired docker-registry service
        @param registry_path project_id any ocp_namespace
    */
    public void dockerLogout(String registry, String registry_path) {
        script.sh """
            docker --config '.project_docker_config' logout '$registry'
        """
    }
    /**
        Typical docker build pipeline: login, build, push
        @param registryCredId ID of cred for login to docker registry
        @param registry URL of desired docker-registry service
        @param registry_path project_id any ocp_namespace
        @param image_name name used for application
        @param image_tag docker image tag
        @param dockerfileName Dockerfile name
        @param additionalArgs extra variable, should consist of command flag (for ex., -build-arg) and value
    */
    public void dockerFull(String registryCredId, String registry, String registry_path, String image_name, String image_tag, String dockerfileName, additionalArgs) {
        script.withCredentials([script.usernamePassword(
                                credentialsId: registryCredId, 
                                usernameVariable: 'USERNAME', 
                                passwordVariable: 'PASSWORD')]) {

                retryScript("docker --config '.project_docker_config' login '$registry' -u '$script.USERNAME' -p '$script.PASSWORD'")
                def strArgs = parsedAditionalArgs (additionalArgs)
                script.sh """\
                             docker --config '.project_docker_config' build -t '$registry/$registry_path/$image_name:$image_tag' -f $dockerfileName $strArgs .
                             docker --config '.project_docker_config' push '$registry/$registry_path/$image_name:$image_tag'
                             docker --config '.project_docker_config' logout '$registry'
                """.trim()
            }
    }
}