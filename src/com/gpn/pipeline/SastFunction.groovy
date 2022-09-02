package com.gpn.pipeline

class SastFunction {
    
    Script script
    Map scan_map = [:]

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
        def project_id = script.sh(script: """
        set +x
        curl -su :${git_api_cred_id} '${git_api_proto}://${user_name}@${git_base_url}:${git_api_port}${git_collection_path}${git_api_url_prefix}projects?\$top=${git_project_req_elements}' | \
        jq -r '.value[] | \
        select(.name|test("^${git_project_name}.*?(?=\\\\\$|\$)"; "i")) | \
        .id,.name'
        set -x
        """, returnStdout: true).trim()

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
        def repos = script.sh(script: """set +x
        curl -su :${git_api_cred_id} '${git_api_proto}://${user_name}@${git_base_url}:${git_api_port}${git_collection_path}${project_id}${git_api_url_prefix}git/repositories?api-version=5.0' | \
        jq -r '.value[] | .name'
        set -x
        """, returnStdout: true).trim()

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

    // Will return formated String with repo and branch/tag for scan
    String scanMapToText(Map scanMap) {
        String result = ''

        for (sm in scanMap) {
            result += "Repository: ${sm.key}, branch/tag: ${sm.value}\n"
        }

        return result
    }

    // Checkout branches/tags for selected repos
    public def gitCheckoutRepos(dont_scan_string, git_cred_id, git_project_id, git_ssh_proto, git_ssh_port, git_base_url, git_collection_path, git_repo_url_prefix, USER, func_name, is_set_build_name) {
        script.echo("Checkouting repos following this list:\n${scanMapToText(this.scan_map)}")

        def selected_repos = []
        for (def repo_name in this.scan_map.keySet()){
            if (this.scan_map[repo_name] != dont_scan_string){
                selected_repos.add(repo_name)
                script.dir(repo_name.replaceAll("\\s", "_")) {
                    script.checkout scm: [$class: 'GitSCM', userRemoteConfigs: [[url: getGitFullPath(repo_name, USER, git_project_id, git_ssh_proto, git_ssh_port, git_base_url, git_collection_path, git_repo_url_prefix),
                    credentialsId: git_cred_id ]], branches: [[name: this.scan_map[repo_name]]]], poll: false
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
            set +x
            echo 'ssh -i ${key} -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no \$*' > ${user}.ssh
            chmod +x ${user}.ssh

            GIT_SSH='./${user}.ssh' git ls-remote --quiet --tags --heads "${getGitFullPath(repo_name, user, git_project_id, git_ssh_proto, git_ssh_port, git_base_url, git_collection_path, git_repo_url_prefix)}" | \
            awk '{print \$2}' | \
            grep -vi '{}' | \
            cut -d/ -f1,2 --complement > ${repo_name.replaceAll("\\s", "_")}.txt

            if [ -s ${repo_name.replaceAll("\\s", "_")}.txt ]; \
            then sed -i "1 i\\${dont_scan_string}" ${repo_name.replaceAll("\\s", "_")}.txt; \
            else echo "${dont_scan_string}" > ${repo_name.replaceAll("\\s", "_")}.txt; fi;
            set -x
            """
            def listOfBranchesTags = script.readFile("${repo_name.replaceAll("\\s", "_")}.txt").trim()
            dyn_params << script.choice(name: repo_name, choices: listOfBranchesTags, description: '')
            dyn_repo_name = repo_name
        }
        return [dyn_params, dyn_repo_name]
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

    String createReportsArtifcat(String reports_dir) {
        String reports_file_name = 'reports.tar.gz'
        // Create tar.gz archive from reports dir
        script.sh """
        set +x
        tar -czf ./${reports_file_name} -C ${reports_dir} .
        set -x
        """
        // Add generated file as archive artifact to build
        script.archiveArtifacts artifacts: reports_file_name
        return reports_file_name
    }

    void sendReportsByEmail(String sast_report_email_list, String reports_attachment_name) {
        if (sast_report_email_list != "") {
            script.emailext (
                        subject: "Checkmarx SAST reports from Jenkins build (${script.env.BUILD_TAG})",
                        body: genMailBodyText(),
                        attachmentsPattern: reports_attachment_name,
                        to: sast_report_email_list
                    )
        }
    }

    String genMailBodyText() {
        String result = """\
        |Вы получили это сообщение потому что ваш email был указан в списке рассылки для получения отчета SAST в рамках запуска сборки Jenkins:\n
        |${script.env.RUN_DISPLAY_URL}\n
        |
        |Для сканирования были выбраны следующие репозитории, ветки\\тэги:\n
        |${scanMapToText(this.scan_map)}
        |
        |Отчет для данной сборки вы сможете найти во вложении к данному письму, а так же по ссылке:\n
        |${script.env.RUN_ARTIFACTS_DISPLAY_URL}\n
        """.stripMargin().stripIndent()
        return result
    }
}