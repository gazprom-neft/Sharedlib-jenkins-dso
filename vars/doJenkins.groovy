import groovy.transform.Field

// Define defaults
@Field service_job_path = "DSO_SERVICE/jenkins-openshift"
@Field service_job_branch = "master"
@Field deploy_environment = "trust"
@Field ocp_url_target = "${env.JEN_OCP_DEFAULT_URL}"
@Field ocp_cred_id = "${env.JEN_OCP_DEFAULT_CRED_ID}"
@Field ocp_namespace = "${env.JEN_OCP_DEFAULT_NAMESPACE}"
@Field jenkins_name = "jenkins"
@Field jenkins_service_type = "ephemeral"
@Field jenkins_instance_num = "1"
@Field jenkins_volume_capacity = "1Gi"
@Field mail_list = ""

def call(String func, Map parameters = [:]) {
    // Rewrite defaults if specific parameters was defined
    if(parameters.service_job_branch != null) {
        service_job_branch = parameters.service_job_branch
    }
    if(parameters.deploy_environment != null) {
        deploy_environment = parameters.deploy_environment
    }
    if(parameters.ocp_url_target != null) {
        ocp_url_target = parameters.ocp_url_target
    }
    if(parameters.ocp_cred_id != null) {
        ocp_cred_id = parameters.ocp_cred_id
    }
    if(parameters.ocp_namespace != null) {
        ocp_namespace = parameters.ocp_namespace
    }
    if(parameters.jenkins_name != null) {
        jenkins_name = parameters.jenkins_name
    }
    if(parameters.jenkins_service_type != null) {
        jenkins_service_type = parameters.jenkins_service_type
    }
    if(parameters.jenkins_instance_num != null) {
        jenkins_instance_num = parameters.jenkins_instance_num
    }
    if(parameters.jenkins_volume_capacity != null) {
        jenkins_volume_capacity = parameters.jenkins_volume_capacity
    }
    if(parameters.mail_list != null) {
        mail_list = parameters.mail_list
    }

    switch(func) {
        case "deployEphemeral":
            // Check if deploy environemnt has right type
            if (deploy_environment != "dev" && deploy_environment != "trust") {
                echo("Error: unknown deploy environment: '${deploy_environment}'. Please define either 'dev' or 'trust'.")
                currentBuild.result = "ABORTED"
                error("Aborting the build.")
            }

            build job: "$service_job_path/deploy-ephemeral", parameters: [
                    [$class: 'StringParameterValue', name: 'BRANCH', value: "$service_job_branch"], \
                    [$class: 'StringParameterValue', name: 'BUILD_ENVIRONMENT', value: "$deploy_environment"], \
                    [$class: 'StringParameterValue', name: 'OCP_URL_TARGET', value: "$ocp_url_target"], \
                    [$class: 'StringParameterValue', name: 'OCP_CRED_ID', value: "$ocp_cred_id"], \
                    [$class: 'StringParameterValue', name: 'JENKINS_NAME', value: "$jenkins_name"], \
                    [$class: 'StringParameterValue', name: 'MAIL_LIST', value: "$mail_list"], \
                    [$class: 'StringParameterValue', name: 'OCP_NAMESPACE', value: "$ocp_namespace"]]
            break
        case "deployPersistent":
            // Check if deploy environemnt has right type
            if (deploy_environment != "dev" && deploy_environment != "trust") {
                echo("Error: unknown deploy environment: '${deploy_environment}'. Please define either 'dev' or 'trust'.")
                currentBuild.result = "ABORTED"
                error("Aborting the build.")
            }
            
            build job: "$service_job_path/deploy-persistent", parameters: [
                    [$class: 'StringParameterValue', name: 'BRANCH', value: "$service_job_branch"], \
                    [$class: 'StringParameterValue', name: 'BUILD_ENVIRONMENT', value: "$deploy_environment"], \
                    [$class: 'StringParameterValue', name: 'OCP_URL_TARGET', value: "$ocp_url_target"], \
                    [$class: 'StringParameterValue', name: 'OCP_CRED_ID', value: "$ocp_cred_id"], \
                    [$class: 'StringParameterValue', name: 'JENKINS_NAME', value: "$jenkins_name"], \
                    [$class: 'StringParameterValue', name: 'MAIL_LIST', value: "$mail_list"], \
                    [$class: 'StringParameterValue', name: 'JENKINS_VOLUME_CAPACITY', value: "$jenkins_volume_capacity"], \
                    [$class: 'StringParameterValue', name: 'OCP_NAMESPACE', value: "$ocp_namespace"]]
            break
        case "destroy":
            // Check if service type has right type
            if (jenkins_service_type != "ephemeral" && jenkins_service_type != "persistent") {
                echo("Error: unknown service type: '${jenkins_service_type}'. Please define either 'ephemeral' or 'persistent'.")
                currentBuild.result = "ABORTED"
                error("Aborting the build.")
            }
            // Check if deploy environemnt has right type
            if (deploy_environment != "dev" && deploy_environment != "trust") {
                echo("Error: unknown deploy environment: '${deploy_environment}'. Please define either 'dev' or 'trust'.")
                currentBuild.result = "ABORTED"
                error("Aborting the build.")
            }

            build job: "$service_job_path/destroy", parameters: [
                    [$class: 'StringParameterValue', name: 'BRANCH', value: "$service_job_branch"], \
                    [$class: 'StringParameterValue', name: 'BUILD_ENVIRONMENT', value: "$deploy_environment"], \
                    [$class: 'StringParameterValue', name: 'OCP_URL_TARGET', value: "$ocp_url_target"], \
                    [$class: 'StringParameterValue', name: 'OCP_CRED_ID', value: "$ocp_cred_id"], \
                    [$class: 'StringParameterValue', name: 'JENKINS_SERVICE_NAME', value: "$jenkins_name"], \
                    [$class: 'StringParameterValue', name: 'JENKINS_SERVICE_TYPE', value: "$jenkins_service_type"], \
                    [$class: 'StringParameterValue', name: 'JENKINS_INSTANCE_NUM', value: "$jenkins_instance_num"], \
                    [$class: 'StringParameterValue', name: 'OCP_NAMESPACE', value: "$ocp_namespace"]]
            break
    }
}