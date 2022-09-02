package com.gpn.pipeline

class CheckmarxSast extends SastFunction implements Saster {
    String sast_project_name
    String sast_proto
    String sast_base_url
    String sast_port
    String sast_generate_pdf_report
    String sast_password
    String sast_preset
    String sast_filter_pattern
    String sast_project_policy_enforce
    String sast_hide_debug
    String sast_cac
    String sast_incremental
    String sast_group_id
    String sast_vulnerability_threshold_enabled
    String sast_high_threshold
    String sast_medium_threshold
    String sast_low_threshold
    String sast_srv_cred_id
    String sast_report_email_list

     // Cretate zip archive with all interesting data and send it to SAST server for scan proccess
    void doSastScan() {
        script.step([
            $class: 'CxScanBuilder', 
            comment: '', 
            configAsCode: sast_cac, 
            credentialsId: '', 
            excludeFolders: '', 
            exclusionsSetting: sast_filter_pattern, 
            failBuildOnNewResults: false, 
            failBuildOnNewSeverity: 'HIGH',
            filterPattern: sast_filter_pattern, 
            fullScanCycle: 10, 
            generatePdfReport: sast_generate_pdf_report, 
            groupId: sast_group_id, 
            password: sast_password, 
            preset: sast_preset, 
            enableProjectPolicyEnforcement: sast_project_policy_enforce,
            projectName: "${sast_project_name}", 
            sastEnabled: true, 
            serverUrl: "${sast_proto}://${sast_base_url}:${sast_port}", 
            sourceEncoding: '1', 
            hideDebugLogs: sast_hide_debug, 
            incremental: sast_incremental,
            username: '', 
            waitForResultsEnabled: true, 
            vulnerabilityThresholdEnabled: sast_vulnerability_threshold_enabled, 
                highThreshold: sast_high_threshold, 
                mediumThreshold: sast_medium_threshold, 
                lowThreshold: sast_low_threshold,
            vulnerabilityThresholdResult: 'FAILURE'
        ])
    }

    // Generate SAST reports
    void genSastReport() {
        // Default image name for SAST report generation
        // Source of image build process: https://alm-itsk.gazprom-neft.local:8080/TFS/GPN/DSO_SERVICE/_git/scripts?path=%2Fjobs%2Fsast&version=GBmaster
        def sast_image_name = "sast-gen-report"
        // Default image tag                              
        def sast_image_tag = "latest"
        // Name of directory where sast reports will be saved                                         
        def reports_dir = "sast-reports"
        // Setup export reports path based on current workspace                                     
        def sast_reports_output_dir = "${script.env.WORKSPACE}/${reports_dir}"


        // Setup docker registry/cred vars based on current running environment
        def sast_image_registry = ""
        def sast_image_registry_cred_id = ""
        switch(script.env.BUILD_ENVIRONMENT) {
            case "dev":
                sast_image_registry = script.env.REPO_DEV_DOCKER_REGISTRY
                sast_image_registry_cred_id = script.env.REPO_DEV_DOCKER_REGISTRY_CRED_ID
                break
            case "trust":
                sast_image_registry = script.env.REPO_TRUST_DOCKER_REGISTRY
                sast_image_registry_cred_id = script.env.REPO_TRUST_DOCKER_REGISTRY_CRED_ID
                break
            default:
                script.echo("Error: You must define 'BUILD_ENVIRONMENT' variable strictly as 'trust or 'dev'")
                script.currentBuild.result = "ABORTED"
                script.error("Aborting the build.")
                break
        }

        script.dir(reports_dir) {
            // Spawn docker container at current execution agent
            script.withCredentials([script.usernamePassword(
                credentialsId: sast_srv_cred_id,
                usernameVariable: 'USERNAME',
                passwordVariable: 'PASSWORD'
            )]) {
                script.docker.withRegistry("https://${sast_image_registry}", "${sast_image_registry_cred_id}") {
                        script.docker.image("${sast_image_name}:${sast_image_tag}").inside("-e REQUESTS_CA_BUNDLE=/etc/pki/tls/cert.pem") {
                            // We don't care about passing credentials through docker container environment variables here (in more secure way) 
                            // because they will be exposed in Jenkins console log as docker '-e' parameters anyway
                            script.sh """
                            python3 /app/app.py -u $script.USERNAME -p '$script.PASSWORD' -n $sast_project_name -d "$sast_reports_output_dir"
                            """
                        }
                }
            }
        }

        // Create tar.gz archive and attach it to build as an artifact
        String artifact_file_name = createReportsArtifcat(reports_dir)

        // Send email with reports as attachment if any email address was provided
        sendReportsByEmail(sast_report_email_list, artifact_file_name)
    }
}