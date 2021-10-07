package com.gpn.pipeline

class LoginUtils {
    
    private Script script

    LoginUtils(Script script) {
        this.script = script
    }
    /**
        Login into ocp cluster
        @param ocpCredId ID of cred for login into OKD/OCP cluster
        @param ocpUrlTarget Path of desired cluster
        @param ocpNamespace OCP Namespace
    */
    public void ocpLogin(String ocpCredId, String ocpUrlTarget, String ocpNamespace) {
        script.withCredentials([script.string(
            credentialsId: ocpCredId,
            variable: 'TOKEN'
        )]) {
            script.sh """
                oc login $ocpUrlTarget --token $script.TOKEN --namespace $ocpNamespace
            """
        }
    }
}