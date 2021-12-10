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

    // Search across ADS collections for a requested project string and returns project id if found some one
    public def getGitProjects(user_name, git_api_proto, git_api_port, git_base_url, git_collection_path, git_project_name, git_project_req_elements, git_api_url_prefix, git_api_cred_id, is_debug) {
        def project_id = script.sh(script: """curl -su :${git_api_cred_id} '${git_api_proto}://${user_name}@${git_base_url}:${git_api_port}${git_collection_path}${git_api_url_prefix}projects?\$top=${git_project_req_elements}' | \
        jq -r '.value[] | \
        select(.name|test("^${git_project_name}.*?(?=\\\\\$|\$)"; "i")) | \
        .id'""", returnStdout: true).trim()

        // Generate error if nothing found
        if (project_id == "" || project_id == null) {
            script.echo("Error: zero projects found with id ${git_project_name}. Please refine your querry.")
            script.currentBuild.result = "ABORTED"
            script.error("Aborting the build.")
        }

        // Generate error if found more than one project id 
        if (project_id.split('\n').size() > 1) {
            script.echo("Error: found more than one project with id ${git_project_name}. Please refine your querry.")
            script.currentBuild.result = "ABORTED"
            script.error("Aborting the build.")
        }

        if (is_debug) {
            script.echo(project_id)
        }

        return project_id
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
                script.dir(repo_name) {
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

            GIT_SSH='./${user}.ssh' git ls-remote --quiet --tags --heads ${getGitFullPath(repo_name, user, git_project_id, git_ssh_proto, git_ssh_port, git_base_url, git_collection_path, git_repo_url_prefix)} | \
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
    public def doSastScan(project_id, sast_proto, sast_base_url, sast_port, sast_generate_pdf_report, sast_password, sast_filter_pattern, sast_project_policy_enforce, sast_hide_debug, sast_cac, sast_incremental) {
        script.step([$class: 'CxScanBuilder', comment: '', configAsCode: sast_cac, credentialsId: '', excludeFolders: '', exclusionsSetting: 'global', failBuildOnNewResults: false, failBuildOnNewSeverity: 'HIGH',
        filterPattern: sast_filter_pattern, fullScanCycle: 10, generatePdfReport: sast_generate_pdf_report, groupId: '3', password: sast_password, preset: '36', enableProjectPolicyEnforcement: sast_project_policy_enforce,
        projectName: "${project_id}", sastEnabled: true, serverUrl: "${sast_proto}://${sast_base_url}:${sast_port}", sourceEncoding: '1', hideDebugLogs: sast_hide_debug, incremental: sast_incremental,
        username: '', vulnerabilityThresholdResult: 'FAILURE', waitForResultsEnabled: true])
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
}
