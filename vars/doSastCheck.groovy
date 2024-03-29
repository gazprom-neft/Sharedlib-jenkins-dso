import groovy.transform.Field
import com.gpn.pipeline.SastFunction
import com.gpn.pipeline.CheckmarxSast
import com.gpn.pipeline.PtaiSast

// Interface for SAST related child classes
interface Saster {
    void doSastScan()
    void genSastReport()
}

// Defaults
@Field dont_scan_string = "DON'T SCAN" // default string for the first element (branch/tag) of each repo
@Field git_ssh_proto = "ssh" // ADS git access protocal. Must be "ssh"
@Field git_ssh_port = "${env.SL_SAST_ADS_GIT_SSH_PORT}" // ADS git over ssh tcp port. Example: "8081"
@Field git_api_proto = "https" // ADS api access protocol. Must be "https"
@Field git_api_port = "${env.SL_SAST_ADS_API_PORT}" //ADS api tcp port. Example: "8080"
@Field git_base_url = "${env.SL_SAST_ADS_FQDN}" // ADS server fqdn. Example: server.domain.com
@Field git_collection_path = "${env.SL_SAST_ADS_COLLECTION_PATH}" // ADS collection path. Example: "/TFS/SOME_ORG/"
@Field git_project_req_elements = "1000" // How many elements will be returned by an api call (default 100 is too small)
@Field git_repo_url_prefix = "/_git/" // ADS git url path. Must be "/_git/"
@Field git_api_url_prefix = "/_apis/" // ADS api url path. Must be"/_apis/"
@Field git_cred_id = "${env.SL_SAST_GIT_CRED_ID}" // Jenkins credential id with git private key (sshUserPrivateKey)
@Field git_api_cred_id = "${env.SL_SAST_GIT_API_CRED_ID}" // Jenkins credential id with ADS api token (string)
@Field sast_proto = "https" // SAST server access protocol. Must be "https"
@Field sast_base_url = "${env.SL_SAST_SERVER_FQDN}" // SAST server fqdn. Example: server.domain.com
@Field sast_port = "${env.SL_SAST_SERVER_PORT}" //SAST server tcp port. Example: "8443"
@Field sast_generate_pdf_report = true // Allows SAST to return pdf report to jenkins build (Default "true")
@Field sast_password = "${env.SL_SAST_PASS_CRED_ID}" // Jenkins credential id with SAST encrypted password (string)
@Field sast_preset = "100009" // Checkmarx Default - GAZPROM-DEFAULT01
@Field sast_project_policy_enforce = false // Enforce SAST policy to return error in jenkins build (Default "false")
@Field sast_server_cred_id = "${env.SL_SAST_SERVER_CRED_ID}" // SAST credential id with username/password to access REST API (get token)
@Field sast_filter_pattern = '''!**/.ptai/**, !**/_cvs/**/*, !**/.svn/**/*, !**/.hg/**/*, !**/.git/**/*, !**/.bzr/**/*,
        !**/.gitgnore/**/*, !**/.gradle/**/*, !**/.checkstyle/**/*, !**/.classpath/**/*, !**/bin/**/*,
        !**/obj/**/*, !**/backup/**/*, !**/.idea/**/*, !**/*.DS_Store, !**/*.ipr, !**/*.iws,
        !**/*.bak, !**/*.tmp, !**/*.aac, !**/*.aif, !**/*.iff, !**/*.m3u, !**/*.mid, !**/*.mp3,
        !**/*.mpa, !**/*.ra, !**/*.wav, !**/*.wma, !**/*.3g2, !**/*.3gp, !**/*.asf, !**/*.asx,
        !**/*.avi, !**/*.flv, !**/*.mov, !**/*.mp4, !**/*.mpg, !**/*.rm, !**/*.swf, !**/*.vob,
        !**/*.wmv, !**/*.bmp, !**/*.gif, !**/*.jpg, !**/*.png, !**/*.psd, !**/*.tif, !**/*.swf,
        !**/*.jar, !**/*.zip, !**/*.rar, !**/*.exe, !**/*.dll, !**/*.pdb, !**/*.7z, !**/*.gz,
        !**/*.tar.gz, !**/*.tar, !**/*.gz, !**/*.ahtm, !**/*.ahtml, !**/*.fhtml, !**/*.hdm,
        !**/*.hdml, !**/*.hsql, !**/*.ht, !**/*.hta, !**/*.htc, !**/*.htd, !**/*.war, !**/*.ear,
        !**/*.htmls, !**/*.ihtml, !**/*.mht, !**/*.mhtm, !**/*.mhtml, !**/*.ssi, !**/*.stm,
        !**/*.bin,!**/*.lock,!**/*.svg,!**/*.obj,
        !**/*.stml, !**/*.ttml, !**/*.txn, !**/*.xhtm, !**/*.xhtml, !**/*.class, !**/*.iml, !Checkmarx/Reports/*.*,
        !OSADependencies.json, !**/node_modules/**/*,
        !**/*.csv, !**/*.backup, !**/*.docx, !**/*.xlsx, !**/*.doc, !**/*.xls, !**/*.webp, !**/*.pack, !**/*.tgz'''
@Field sast_vulnerability_threshold_enabled = false        //Mark the build as unstable if the number of high severity vulnerabilities is above the specified threshold.
@Field sast_high_threshold = 0
@Field sast_medium_threshold = 0
@Field sast_low_threshold = 0
@Field sast_type = "ptai" // sast type default value to PTAI ('ptai' or 'checkmarx')
@Field sast_lang = "python" // sast default lang type

def call(String func, Map parameters = [:]) {
    SastFunction sastFunc = new SastFunction(script: this)

    // Defaults for parameters [:]
    // These may be overridden by user from the shared library function call
    if(parameters.is_debug == null) {
        parameters.is_debug = false // Define default to true if not exists
    }
    if(parameters.sast_hide_debug == null) {
        parameters.sast_hide_debug = true // Define default to true if not exists
    }
    if(parameters.sast_cac == null) {
        parameters.sast_cac = false // Define default to false if not exists
    }
    if(parameters.sast_incremental == null) {
        parameters.sast_incremental = true // Define default to true if not exists
    }
    if(parameters.set_build_name == null) {
        parameters.set_build_name = false // Define default to false if not exists
    }
    if(parameters.sast_group_id == null) {
        parameters.sast_group_id = "1" // Define default to 1 (following DSOPROJECT-2137) if not exists
    }
    if(parameters.sast_generate_reports == null) {
        parameters.sast_generate_reports = false // Define default to false
    }
    if(parameters.sast_generate_reports_email_list == null) {
        parameters.sast_generate_reports_email_list = "" // Define default to empty string
    }
    if(parameters.sast_preset != null) {
        sast_preset = parameters.sast_preset
    }
    if(parameters.sast_vulnerability_threshold_enabled != null) {
        sast_vulnerability_threshold_enabled = parameters.sast_vulnerability_threshold_enabled
    }   
    if(parameters.sast_high_threshold != null) {
        sast_high_threshold = parameters.sast_high_threshold
    }   
    if(parameters.sast_medium_threshold != null) {
        sast_medium_threshold = parameters.sast_medium_threshold
    }   
    if(parameters.sast_low_threshold != null) {
        sast_low_threshold = parameters.sast_low_threshold
    }
    if(parameters.sast_type != null) {
        sast_type = parameters.sast_type
    }
    if(parameters.sast_lang != null) {
        sast_lang = parameters.sast_lang
    }    

    switch(func) {
        /*
        Allows to start SAST scan check with fully automated process with predefined list
        of git repository names and branches/tags for your ADS project.

        Detailed reference could be found at doSastCheck.txt
        */
        case "manualWithParameters":
            // Define variable out of withCredentials scope to persists it's value
            def git_project_full_name = ""
            def git_project_id = ""

            withCredentials([sshUserPrivateKey(credentialsId: git_cred_id, keyFileVariable: 'KEY', usernameVariable: 'GIT_USER')]) {
                withCredentials([string(credentialsId: git_api_cred_id, variable: 'TOKEN')]) {
                    if(!parameters.git_project_name){
                        withFolderProperties {
                            parameters.git_project_name = "${env.PROJECT_ID}"
                        }
                    }

                    (git_project_id, git_project_full_name) = sastFunc.getGitProjects(GIT_USER, 
                                                                git_api_proto, 
                                                                git_api_port, 
                                                                git_base_url, 
                                                                git_collection_path, 
                                                                parameters.git_project_name, 
                                                                git_project_req_elements, 
                                                                git_api_url_prefix, 
                                                                TOKEN, 
                                                                parameters.is_debug)
                }

                // Setup repos for scan
                sastFunc.scan_map = parameters.repos_for_scan
                
                sastFunc.gitCheckoutRepos(
                    dont_scan_string, 
                    git_cred_id, 
                    git_project_id, 
                    git_ssh_proto, 
                    git_ssh_port, 
                    git_base_url, 
                    git_collection_path, 
                    git_repo_url_prefix, 
                    GIT_USER,
                    func,
                    parameters.set_build_name
                )
            }

            if(parameters.sast_project_name == null) {
                parameters.sast_project_name = git_project_full_name // Set SAST project name based on git_project_id (following DSOPROJECT-2150)
            }

            break
        case "interactive":
            /*
            Allows to start SAST scan check with interactive jenkins UI promt
            to allow granulary selection of git repository names and branches/tags
            for your ADS project.

            Detailed reference could be found at doSastCheck.txt
            */
            def git_project_full_name = ""  // Define variable out of withCredentials scope to persists it's value
            withCredentials([sshUserPrivateKey(credentialsId: git_cred_id, keyFileVariable: 'KEY', usernameVariable: 'GIT_USER')]) {
                def dynamicParameters = []
                user_input_repo_name = "" // Used later to save repo_name to be able to recreacte map from string if only one repo is founded

                // Search for requested repo
                def git_project_id = ""
                def projects_list = ""
                withCredentials([string(credentialsId: git_api_cred_id, variable: 'TOKEN')]) {
                    if(!parameters.git_project_name){
                        withFolderProperties {
                            parameters.git_project_name = "${env.PROJECT_ID}"
                        }
                    }
                    (git_project_id, git_project_full_name) = sastFunc.getGitProjects(GIT_USER, 
                                                                git_api_proto, 
                                                                git_api_port, 
                                                                git_base_url, 
                                                                git_collection_path, 
                                                                parameters.git_project_name, 
                                                                git_project_req_elements, 
                                                                git_api_url_prefix, 
                                                                TOKEN, 
                                                                parameters.is_debug)
                
                    // Get all repos for finded repo
                    projects_list = sastFunc.getGitProjectRepos(GIT_USER, 
                                                            git_project_id, 
                                                            git_api_proto, 
                                                            git_api_port, 
                                                            git_base_url, 
                                                            git_collection_path, 
                                                            git_api_url_prefix, 
                                                            TOKEN, 
                                                            parameters.is_debug).readLines()
                }
                
                // Get collection of repos/branches/tags
                (dynamicParameters, user_input_repo_name) = sastFunc.dynamicParametersGenerate(projects_list, 
                                                            KEY, GIT_USER, 
                                                            git_project_id, 
                                                            dont_scan_string, 
                                                            git_ssh_proto, 
                                                            git_ssh_port, 
                                                            git_base_url, 
                                                            git_collection_path, 
                                                            git_repo_url_prefix)

                // Show dynamic user input
                def userInput = sastFunc.showDynamicInput("Choose branch\\tag for repositories in project ${parameters.git_project_name} for SAST scanning:", dynamicParameters)

                // Check user input for correct map structure
                userInput = sastFunc.checkUserInput(userInput, user_input_repo_name, parameters.is_debug)

                if (parameters.is_debug) {
                    println(userInput);
                }

                // Setup repos for scan
                sastFunc.scan_map = userInput

                // Cheout selected repos/branches/tags
                sastFunc.gitCheckoutRepos(
                    dont_scan_string, 
                    git_cred_id, 
                    git_project_id, 
                    git_ssh_proto, 
                    git_ssh_port, 
                    git_base_url, 
                    git_collection_path, 
                    git_repo_url_prefix, 
                    GIT_USER,
                    func,
                    parameters.set_build_name
                )
            }
            
            if(parameters.sast_project_name == null) {
                parameters.sast_project_name = git_project_full_name // Set SAST project name based on git_project_id (following DSOPROJECT-2150)
            }

            break
    }

    switch (sast_type) {
        case "ptai":
            Saster ptai = new PtaiSast(
                script: this,
                is_debug: parameters.is_debug,
                sast_project_name: parameters.sast_project_name,
                sast_report_email_list: parameters.sast_generate_reports_email_list,
                sast_hide_debug: parameters.sast_hide_debug,
                sast_filter_pattern: sast_filter_pattern,
                sast_incremental: parameters.sast_incremental,
                sast_lang: sast_lang,
                sast_vulnerability_threshold_enabled: sast_vulnerability_threshold_enabled,
                scan_map: sastFunc.scan_map,
            )
            // Run sast check
            ptai.doSastScan()

            // Generate sast reports for target scan
            if (parameters.sast_generate_reports) {
                ptai.genSastReport()
            }

            break
        case "checkmarx":
            Saster checkmarx
            withCredentials([string(credentialsId: git_api_cred_id, variable: 'SAST_PASSWORD')]) {
                checkmarx = new CheckmarxSast(
                    script: this,
                    sast_project_name: parameters.sast_project_name,
                    sast_proto: sast_proto,
                    sast_base_url: sast_base_url,
                    sast_port: sast_port, 
                    sast_generate_pdf_report: sast_generate_pdf_report,
                    sast_password: SAST_PASSWORD,
                    sast_preset: sast_preset,
                    sast_filter_pattern: sast_filter_pattern,
                    sast_project_policy_enforce: sast_project_policy_enforce,
                    sast_hide_debug: parameters.sast_hide_debug,
                    sast_cac: parameters.sast_cac,
                    sast_incremental: parameters.sast_incremental,
                    sast_group_id: parameters.sast_group_id,
                    sast_vulnerability_threshold_enabled: sast_vulnerability_threshold_enabled,
                    sast_high_threshold: sast_high_threshold,
                    sast_medium_threshold: sast_medium_threshold,
                    sast_low_threshold: sast_low_threshold,
                    sast_srv_cred_id: sast_server_cred_id,
                    sast_report_email_list: parameters.sast_generate_reports_email_list,
                    scan_map: sastFunc.scan_map,
                )
                checkmarx.doSastScan()
            }

            // Generate sast reports for target scan
            if (parameters.sast_generate_reports) {
                checkmarx.genSastReport()
            }

            break
    }
    cleanWs() // Clean project space from sast check artifacts
}
