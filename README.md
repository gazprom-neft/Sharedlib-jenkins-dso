[![License](https://img.shields.io/github/license/analysiscenter/batchflow.svg)](https://www.apache.org/licenses/LICENSE-2.0)
# Welcome Gazprom Neft!

![image](https://user-images.githubusercontent.com/46698191/129506113-b6080e04-0dfc-4db3-be88-5095576e7227.png)

### ❓ What is this?

- The purpose of this repository is to allow GitHub and the Gazprom Neft community to use jenkins libraries for external teams
- Shared Libraries - source code (groovy based) which can be managed in a dedicated repository of the version control system and loaded on demand from the Jenkins pipeline.

- Using Gazprom Neft libraries
- Feedback and features requests

## Get started with the library

    // import shared library
       @Library("dsoLibrary") _

## **1. doOc**
- OcFunction - Login to ocp cluster; Get UID or GID;  Install Moon (or like this) template;
- required unification from CaaS side with service naming;

## Example of usage

- **login**:

    >##### **Login to ocp cluster**
    *@param* **ocpCredId**\
    *@param* **ocpUrlTarget**\
    *@param* **ocpNamespace** 

        stage("OCP login") {
            steps {
			doOc("login", ["ocpCredId"   : "$OCP_CRED_ID",
				       "ocpUrlTarget": "$OCP_URL_TARGET",
				       "ocpNamespace": "$OCP_NAMESPACE"])
            }
        }

- **loginVault**:

    >##### **Login to ocp cluster with vault**
    *@param* **ocpCredId**\
    *@param* **ocpUrlTarget**\
    *@param* **ocpNamespace**\
    *@param* **vaultUrl**\
    *@param* **secretPrefixPath**\
    *@param* **secretPath**

        stage("OCP login with vault") {
            steps {
			doOc("loginVault", ["ocpCredId"       : "$VAULT_APPROLE_CRED_ID",
					    "ocpUrlTarget"    : "$OCP_URL_TARGET",
					    "ocpNamespace"    : "$OCP_NAMESPACE",
					    "vaultUrl"        : "$VAULT_TRUST_URL",
					    "secretPrefixPath": "$VAULT_SECRET_PREFIX_PATH",            
					    "secretPath"      : "$VAULT_SECRET_PATH"])
            }
        }

- **createSecret**:

    >##### **Create OCP secrets for registry**
    *@param* **ocpCredId**\
    *@param* **registryList** Comma seprated list\
    *@param* **registryPort**\
    *@param* **ocpNamespace**

        stage("OCP create secret") {
            steps {
			doOc("createSecret", ["registryCredId": "$REGISTRY_CRED_ID",
					      "registryList"  : "$REGISTRY_LIST",
					      "registryPort"  : "$REGISTRY_PORT",
					      "ocpNamespace"  : "$OCP_NAMESPACE"])
            }
        }

- **tag**:

    >##### **Tag image stream (gitCommitShort) as latest**
    *@param* **registry**\
    *@param* **ocpAppName**\
    *@param* **ocpNamespace**\
    *@param* **gitCommitShort**

        stage("OCP tag image") {
            steps {
			doOc("tag", ["registry"      : "$REGISTRY",
				     "ocpAppName"    : "$OCP_APP_NAME",
				     "ocpNamespace"  : "$OCP_NAMESPACE",
				     "gitCommitShort": "$GIT_COMMIT_SHORT"])
            }
        }
	
- **checkIS & status**:

    >##### **Get status of image stream**
    *@param* **ocpAppName**\
    *@param* **ocpNamespace**

        stage("OCP check image") {
			when {
			    not {
				expression {
				    doOc("checkIS", ["ocpAppName"  : "$OCP_APP_NAME",
						     "ocpNamespace": "$OCP_NAMESPACE"])
				}
			    }
			}
			steps {
			    	doOc("status", ["ocpAppName": "$OCP_APP_NAME"])
			}
        }

- **deployTemplate**:

    >##### **Deploy or change templates from specific directory**
    *@param* **ocpNamespace**

        stage("OCP deploy template") {
            steps {
			doOc("deployTemplate", ["ocpNamespace": "$OCP_NAMESPACE"])
            }
        }

## **2. doSastCheck**
- With Checkmarx SAST™, you can run fast and accurate incremental or full scans whenever you need them. 
- Trust our industry-leading SAST solution to give you the flexibility, accuracy, and coverage to secure your most critical code commits, within your rule sets, at scale.

## Example of usage

- **Interactive**:

    >Allows to start SAST scan check with interactive jenkins UI promt
    to allow granulary selection of git repository names and branches/tags
    for your ADS project.
    
		@Library("dsoLibrary") _
			pipeline {
				agent {
					label "${BUILD_ENVIRONMENT}"
				}
			 
				stages {
					stage('Sast scan') {
						steps {
							doSastCheck "interactive"
						}
					}
				}
			}
			
- **Non Interactive**:
	
    >Allows to start SAST scan check with fully automated process with predefined list
    of git repository names and branches/tags for your ADS project.
    
		@Library("dsoLibrary") _ 
			// Defining list with repo names и branch/tag to checkout and scan with SAST
			def my_repos = [
				"repo1": "master",
				"repo2": "v1.0.1"
			]
			 
			pipeline {
				agent {
					label "${BUILD_ENVIRONMENT}"
				}
			 
				stages {
					stage('Sast scan') {
						steps {
							doSastCheck "manualWithParameters", ["repos_for_scan": my_repos]
						}
					}
				}
			}


## Available parameters:

- **set_build_name**:
    >**Optional**\
    Set currect build name with additional info.
    Must be bool "false/true".\
    **Default:** "false"

    **Example**:
        
        doSastCheck "manualWithParameters", ["repos_for_scan": my_repos, "set_build_name": true]
        


- **git_project_name**:

    >**Optional**\
    Set project id for searching across ADS collections (use \$ at the end of string to allow precise searching. Example: "TEST_PROJECT\$").
    
    **Example**:
    
    	doSastCheck "manualWithParameters", ["repos_for_scan": my_repos, "git_project_name": "MY_ADS_PROJECT"]


- **sast_hide_debug**:

    >**Optional**\
    Allows to reduce SAST log generations is jenkins console output.\
    **Default:** "true"
    
    **Example**:
    	 
    	doSastCheck "manualWithParameters", ["repos_for_scan": my_repos, "sast_hide_debug": false]


- **is_debug**:

    >**Optional**\
    Turn on additional debuging info for shared library functions (Output to jenkins console log)\
    **Default:** "false"
    
    **Example**:
    	 
    	doSastCheck "manualWithParameters", ["repos_for_scan": my_repos, "is_debug": true]


- **sast_cac**:

    >**Optional**\
    Enables SAST configuration as code.\
	Please refer Checkmarx manual:
    https://checkmarx.atlassian.net/wiki/spaces/SD/pages/1457226433/Setting+up+Scans+in+Jenkins
	**Default:** "false"
	
	**Example**:
    	 
    	doSastCheck "manualWithParameters", ["repos_for_scan": my_repos, "sast_cac": true]


- **sast_incremental**:

    >**Optional**\
    Enables SAST encremental scans. Defaultt to false.\
    **Default:** "false"
    	
    **Example**:
    	 
    	doSastCheck "manualWithParameters", ["repos_for_scan": my_repos, "sast_incremental": true]


- **sast_project_name**:

    >**Optional**\
    Sets SAST project name.\
    **Default:** "${env.PROJECT_ID}_${env.PROJECT_NAME}"
    
    **Example**:
    	 
    	doSastCheck "manualWithParameters", ["repos_for_scan": my_repos, "sast_project_name": "SAST123"]


- **sast_group_id**:

    >**Optional**\
    Sets SAST groupId paramter.\
    **Default:** "1"
    
    **Example**:
    	 
    	doSastCheck "manualWithParameters", ["repos_for_scan": my_repos, "sast_group_id": "3"]
	
	
- **sast_generate_reports**:

    >**Optional**\
    If defined, SAST scan reports in xml, csv, pdf formats will be generated upon successful project scan.\
    **Default:** "false"
    
    **Example**:
    	 
    	doSastCheck "manualWithParameters", ["repos_for_scan": my_repos, "sast_generate_reports": true]
	
	
- **sast_generate_reports_email_list**:

    >**Optional**\
    If defined, the reports was generated will be send over email to target recipients.
    Provided list must be comma or whitespace separated. \
    **Default:** ""
    
    **Example**:
    	 
        doSastCheck "manualWithParameters", ["repos_for_scan": my_repos, "sast_generate_reports": true, "sast_generate_reports_email_list": "abc@abc.ru, xyz@xyz.ru"]


- **sast_preset**:

    >**Optional**\
    Checkmarx preset\
    **Default:** "100009" (GAZPROM-DEFAULT01)
    
    **Example**:
    	 
    	doSastCheck "manualWithParameters", ["repos_for_scan": my_repos, "sast_preset": '100009']


- **sast_vulnerability_threshold_enabled**:

    >**Optional**
    Mark the build as FAILED if the number of high severity vulnerabilities is above the specified threshold.
    **Default:** false
    
    ***sast_high_threshold, sast_medium_threshold, sast_low_threshold***:
    severity vulnerability threshold
    ***Default*** 0


    **Example**:
    	 
    	doSastCheck "manualWithParameters", ["repos_for_scan": my_repos, "sast_vulnerability_threshold_enabled": true, \
                "sast_high_threshold": 0, "sast_medium_threshold": 10, "sast_low_threshold": 100]

## **3. doDocker**
- DockerFunction - login, build and publish

## Example of usage

- **login**:

    >##### **Log in to a Docker registry**
    *@param* **registryCred** ID of cred for login to docker registry\
    *@param* **registry** URL of desired docker-registry service

        stage("Docker registry login") {
            steps {
                doDocker("login", ["registryCred": "$REGISTRY_CRED_ID", 
                                   "registry"    : "$REGISTRY"])
            }
        }

- **build**:

    >##### **Build an image from a Dockerfile**
    *@param* **registry** URL of desired docker-registry service\
    *@param* **ocpNamespace** project OCP namespace\
    *@param* **ocpAppName** OCP name used for application\
    *@param* **gitCommitShort** short hash id of used git commit\
    *@param* **dockerfileName** Dockerfile name\
    *@param* **additionalArgs** extra variable, should consist of command flag (for ex., -build-arg) and value

        stage("Docker build image") {
            steps {
            doDocker("build", ["registry"      : "$REGISTRY",
                               "ocpNamespace"  : "$OCP_NAMESPACE",
                               "ocpAppName"    : "$OCP_APP_NAME",
                               "gitCommitShort": "$GIT_COMMIT_SHORT",
                               "dockerfileName": "$DOCKERFILE_NAME",
                               "additionalArgs": "--build-arg RH_REGISTRY_ARG=${RH_REGISTRY} \
                                                  --build-arg NEXUS_URL=${NEXUS_URL}"])
            }
        }

- **push**:

    >##### **Push an image or a repository to a registry**
    *@param* **registry** URL of desired docker-registry service\
    *@param* **ocpNamespace** project OCP namespace\
    *@param* **ocpAppName** OCP name used for application\
    *@param* **gitCommitShort** short hash id of used git commit   

        stage("Docker push image") {
            steps {
                doDocker("push", ["registry"      : "$REGISTRY",
                                  "ocpNamespace"  : "$OCP_NAMESPACE",
                                  "ocpAppName"    : "$OCP_APP_NAME",
                                  "gitCommitShort": "$GIT_COMMIT_SHORT"])
            }
        }

- **dockerFull**:

    >##### **Typical docker build pipeline: Log in, Build and Push an image to a registry**
    *@param* **registryCred** ID of cred for login to docker registry\
    *@param* **registry** URL of desired docker-registry service\
    *@param* **ocpNamespace** project OCP namespace\
    *@param* **ocpAppName** OCP name used for application\
    *@param* **gitCommitShort** short hash id of used git commit\
    *@param* **dockerfileName** Dockerfile name\
    *@param* **additionalArgs** extra variable, should consist of command flag (for ex., -build-arg) and value

        stage("Docker full (login, build, push)") {
            steps {
                doDocker("dockerFull", ["registryCred"  : "$REGISTRY_CRED_ID", 
                                        "registry"      : "$REGISTRY",
                                        "ocpNamespace"  : "$OCP_NAMESPACE",
                                        "ocpAppName"    : "$OCP_APP_NAME",
                                        "gitCommitShort": "$GIT_COMMIT_SHORT",
                                        "dockerfileName": "$DOCKERFILE_NAME",
                                        "additionalArgs": "--build-arg RH_REGISTRY_ARG=${RH_REGISTRY} \
                                                           --build-arg NEXUS_URL=${NEXUS_URL}"])
            }
        }    


## Glossary General Terms
[Glossary](https://www.jenkins.io/doc/book/glossary/#glossary) 

The flowchart below is an example of one CD scenario easily modeled in Jenkins Pipeline:

![i9BUJ](https://user-images.githubusercontent.com/82883746/143893316-daf6e8e1-dffe-4aad-857f-c1415755d7c2.png)

# 👥  Gazprom Team
| Name | Handle | Company | Title |
| --- | --- | --- | --- |
|Pavel Lipatov | @pabloli84 | Gazprom-Neft | Head of DevOps |
|Lev Melentev | @levurevich | Gazprom-Neft | Team Tech Lead |
|Artem Batalov| @aruchibarudo | Gazprom-Neft | Sr. System Engineer |
|Evgenij Ovchinnikov | @benderit |  Gazprom-Neft | Sr. System Engineer |
|Evgenij Kurnosov | @olmerdale | Gazprom-Neft | Sr. System Engineer |
|Evgenij Khokhlov | @rageofgods | Gazprom-Neft | Sr. System Engineer |
|Georgij Anikin | @g-anikin |  Gazprom-Neft | Sr. System Engineer |
|Mihail Perov | @perovma | Gazprom-Neft | Sr. System Engineer |
|Aleksandr Petlin | @jakondo09 | Gazprom-Neft | Sr. System Engineer |
|Dmitrij Seleznev | @dimako12 | Gazprom-Neft | Sr. System Engineer |
|Stanislav Sobol' | @pokamolodoy406 | Gazprom-Neft | IT.security |
