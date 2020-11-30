package com.gpn.pipeline

class LoginUtils {
    
    private Script script

    LoginUtils(Script script) {
        this.script = script
    }
    
    public void ocpLogin(String ocpCredId, String ocpUrlTarget) {
        script.withCredentials([script.string(
            credentialsId: ocpCredId,
            variable: 'TOKEN'
        )]) {
            script.sh """
                oc login $ocpUrlTarget --token $script.TOKEN
            """
        }
    }

    public void dockerLogin(String registryCredId, String registry) {
        script.withCredentials([script.usernamePassword(
            credentialsId: registryCredId, 
            usernameVariable: 'USERNAME', 
            passwordVariable: 'PASSWORD'
        )]) {
            script.sh """
                docker login $registry -u $script.USERNAME -p $script.PASSWORD
            """
        }
    }
}