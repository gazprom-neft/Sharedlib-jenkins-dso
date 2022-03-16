package com.gpn.pipeline

class DockerFunction {
    
    private Script script

    DockerFunction(Script script) {
        this.script = script
    }
    /**
        Login into docker registry
        @param registryCredId ID of cred for login to docker registry
        @param registry URL of desired docker-registry service
    */
    public void dockerLogin(String registryCredId, String registry) {
        script.withCredentials([script.usernamePassword(
            credentialsId: registryCredId, 
            usernameVariable: 'USERNAME', 
            passwordVariable: 'PASSWORD')]) {
            script.sh """
                docker login $registry -u $script.USERNAME -p $script.PASSWORD
            """
        }
    }
    /**
        Docker build
        @param registry URL of desired docker-registry service
        @param ocpNamespace project OCP namespace
        @param ocpAppName OCP name used for application
        @param gitCommitShort short hash id of used git commit
        @param dockerfileName Dockerfile name
        @param additionalArgs extra variable, should consist of command flag (for ex., -build-arg) and value
    */
    public void dockerBuild(String registry, String ocpNamespace, String ocpAppName, String gitCommitShort, String dockerfileName, String additionalArgs) {
        script.sh """
            docker build -t $registry/$ocpNamespace/$ocpAppName:$gitCommitShort -f $dockerfileName $additionalArgs .
        """
    }
    /**
        Docker push
        @param registry URL of desired docker-registry service
        @param ocpNamespace project OCP namespace
        @param ocpAppName OCP name used for application
        @param gitCommitShort short hash id of used git commit
    */
    public void dockerPush(String registry, String ocpNamespace, String ocpAppName, String gitCommitShort) {
        script.sh """
            docker push $registry/$ocpNamespace/$ocpAppName:$gitCommitShort
        """
    }
    /**
        Typical docker build pipeline: login, build, push
        @param registryCredId ID of cred for login to docker registry
        @param registry URL of desired docker-registry service
        @param ocpNamespace project OCP namespace
        @param ocpAppName OCP name used for application
        @param gitCommitShort short hash id of used git commit
        @param dockerfileName Dockerfile name
        @param additionalArgs extra variable, should consist of command flag (for ex., -build-arg) and value
    */
    public void dockerFull(String registryCredId, String registry, String ocpNamespace, String ocpAppName, String gitCommitShort, String dockerfileName, String additionalArgs) {
        script.withCredentials([script.usernamePassword(
            credentialsId: registryCredId, 
            usernameVariable: 'USERNAME', 
            passwordVariable: 'PASSWORD')]) {

            script.sh """
                docker login $registry -u $script.USERNAME -p $script.PASSWORD
                docker build -t $registry/$ocpNamespace/$ocpAppName:$gitCommitShort -f $dockerfileName $additionalArgs .
                docker push $registry/$ocpNamespace/$ocpAppName:$gitCommitShort
            """
            }
    }
}