package com.gpn.pipeline

class SastFunction {
    
    private Script script

    SastFunction(Script script) {
        this.script = script
    }

    // Shows jekins user input and returns selected items
    public def showDynamicInput(message, dynamic_parameters) {
        def userInput = script.input(
            id: 'userInput', message: message, 
            parameters: dynamic_parameters
        )

        return userInput
    }

    // Generate correct git url for checkout requests
    public def getGitFullPath(repo_name, user_name, project_id, git_ssh_proto, git_ssh_port, git_base_url, git_collection_path, git_repo_url_prefix){
        def git_full_path_without_repo_name = "${git_ssh_proto}://${user_name}@${git_base_url}:${git_ssh_port}${git_collection_path}${project_id}${git_repo_url_prefix}"
        def git_full_path_with_repo_name = git_full_path_without_repo_name + repo_name

        return git_full_path_with_repo_name
    }

    // Search across ADS collections for a requested project string and returns project 'id, name' if found some one
    public def getGitProjects(user_name, git_api_proto, git_api_port, git_base_url, git_collection_path, git_project_name, git_project_req_elements, git_api_url_prefix, git_api_cred_id, is_debug) {
        def project_id = script.sh(script: """curl -su :${git_api_cred_id} '${git_api_proto}://${user_name}@${git_base_url}:${git_api_port}${git_collection_path}${git_api_url_prefix}projects?\$top=${git_project_req_elements}' | \
        jq -r '.value[] | \
        select(.name|test("^${git_project_name}.*?(?=\\\\\$|\$)"; "i")) | \
        .id,.name'""", returnStdout: true).trim()

        // Generate error if nothing found
        if (project_id == "" || project_id == null) {
            script.echo("Error: zero projects found with id ${git_project_name}. Please refine your querry.")
            script.currentBuild.result = "ABORTED"
            script.error("Aborting the build.")
        }

        // Generate error if found more than one project with 'id, name'
        def project_id_name = project_id.split('\n')
        def p_id, p_name
        if (project_id_name.size() > 2) {
            script.echo("Error: found more than one project with id ${git_project_name}. Please refine your querry.")
            script.currentBuild.result = "ABORTED"
            script.error("Aborting the build.")
        } else if (project_id_name.size() == 2) {
            p_id = project_id_name[0]
            p_name = project_id_name[1]
        }

        if (is_debug) {
            script.echo(project_id)
        }

        return [p_id, p_name]
    }

    // Returns all project repos
    public def getGitProjectRepos(user_name, project_id, git_api_proto, git_api_port, git_base_url, git_collection_path, git_api_url_prefix, git_api_cred_id, is_debug) {
        def repos = script.sh(script: """curl -su :${git_api_cred_id} '${git_api_proto}://${user_name}@${git_base_url}:${git_api_port}${git_collection_path}${project_id}${git_api_url_prefix}git/repositories?api-version=5.0' | \
        jq -r '.value[] | .name'""", returnStdout: true).trim()

        if (is_debug) {
            script.echo(repos)
        }

        return repos
    }

    // Check user input returned data for correct format
    public def checkUserInput(userInput, user_input_repo_name, is_debug) {
        if (!(userInput instanceof java.util.HashMap)){ // Workaround userInput behaviour, when it's return string instead of map when only one choice in the collection
            if (is_debug) {
                script.echo(userInput.getClass());
            }
            return [(user_input_repo_name): userInput]
        }
        else {
            return userInput
        }
    }

    // Checkout branches/tags for selected repos
    public def gitCheckoutRepos(userInput, dont_scan_string, git_cred_id, git_project_id, git_ssh_proto, git_ssh_port, git_base_url, git_collection_path, git_repo_url_prefix, USER, func_name, is_set_build_name) {
        def selected_repos = []
        for (def repo_name in userInput.keySet()){
            if (userInput[repo_name] != dont_scan_string){
                selected_repos.add(repo_name)
                script.dir(repo_name.replaceAll("\\s", "_")) {
                    script.checkout scm: [$class: 'GitSCM', userRemoteConfigs: [[url: getGitFullPath(repo_name, USER, git_project_id, git_ssh_proto, git_ssh_port, git_base_url, git_collection_path, git_repo_url_prefix),
                    credentialsId: git_cred_id ]], branches: [[name: userInput[repo_name]]]], poll: false
                }
            }
        }
        if(is_set_build_name) {
            setBuildName(func_name, selected_repos.unique().join(', ')) // Set build name with info about checkouted repos
        }
    }

    // Generate dynamic set of parameters (jenkins choice parameter) for an user input
    public def dynamicParametersGenerate(projects_list, key, user, git_project_id, dont_scan_string, git_ssh_proto, git_ssh_port, git_base_url, git_collection_path, git_repo_url_prefix) {
        def dyn_params = []
        def dyn_repo_name = ""
        projects_list.unique().eachWithIndex { repo_name, index ->
            script.sh """
            echo 'ssh -i ${key} -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no \$*' > ${user}.ssh
            chmod +x ${user}.ssh

            GIT_SSH='./${user}.ssh' git ls-remote --quiet --tags --heads "${getGitFullPath(repo_name, user, git_project_id, git_ssh_proto, git_ssh_port, git_base_url, git_collection_path, git_repo_url_prefix)}" | \
            awk '{print \$2}' | \
            grep -vi '{}' | \
            cut -d/ -f1,2 --complement > ${repo_name.replaceAll("\\s", "_")}.txt

            if [ -s ${repo_name.replaceAll("\\s", "_")}.txt ]; \
            then sed -i "1 i\\${dont_scan_string}" ${repo_name.replaceAll("\\s", "_")}.txt; \
            else echo "${dont_scan_string}" > ${repo_name.replaceAll("\\s", "_")}.txt; fi;
            """
            def listOfBranchesTags = script.readFile("${repo_name.replaceAll("\\s", "_")}.txt").trim()
            dyn_params << script.choice(name: repo_name, choices: listOfBranchesTags, description: '')
            dyn_repo_name = repo_name
        }
        return [dyn_params, dyn_repo_name]
    }

    // Cretate zip archive with all interesting data and send it to SAST server for scan proccess
    public def doSastScan(project_id, sast_proto, sast_base_url, sast_port, sast_generate_pdf_report, sast_password, sast_preset,
                          sast_filter_pattern, sast_project_policy_enforce, sast_hide_debug, sast_cac, sast_incremental, sast_group_id,
                          sast_vulnerability_threshold_enabled, sast_high_threshold, sast_medium_threshold, sast_low_threshold) 
    {
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
            projectName: "${project_id}", 
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

    // Set current job build name with additional help-info
    public def setBuildName(func_name, repos) {
    
        script.buildName "${script.env.BUILD_NUMBER}-${func_name}"
    
        script.wrap([$class: 'BuildUser']){
            script.USER = script.env.BUILD_USER ?: "Jenkins"
            script.USER_EMAIL = script.env.BUILD_USER_EMAIL
            script.buildDescription "SAST check selected repo(s): ${repos}\nBuild started by ${script.USER}\nExecuted @ ${script.env.NODE_NAME}."
        }
    }

    // Generate SAST reports
    public def genSastReport(sast_srv_cred_id, sast_project_name, email_list) {
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
                            python3 /app/app.py -u $script.USERNAME -p '$script.PASSWORD' -n $sast_project_name -d $sast_reports_output_dir
                            """
                        }
                }
            }
        }

        // Create tar.gz archive from reports dir
        script.sh "tar -czf ./${reports_dir}.tar.gz ${reports_dir}"
        // Add generated file as archive artifact to build
        script.archiveArtifacts artifacts: "${reports_dir}.tar.gz"

        // Send generated archive to users via email
        if (email_list != "") {
            script.emailext (
                        subject: "Checkmarx SAST reports from Jenkins build (${script.env.BUILD_TAG})",
                        body: """\
                        Вы получили это сообщение потому что ваш email был указан в списке рассылки
                        для получения отчета Checkmarx SAST в рамках запуска сборки Jenkins:
                        ${script.env.RUN_DISPLAY_URL} 

                        Отчет для данной сборки вы сможете найти во вложении к данному письму, а так же по ссылке:
                        ${script.env.RUN_ARTIFACTS_DISPLAY_URL}
                        """.stripIndent(),
                        attachmentsPattern: "${reports_dir}.tar.gz",
                        to: email_list
                    )
        }
    }
}
