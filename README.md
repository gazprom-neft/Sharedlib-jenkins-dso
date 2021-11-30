[![License](https://img.shields.io/github/license/analysiscenter/batchflow.svg)](https://www.apache.org/licenses/LICENSE-2.0)
# Welcome Gazprom Neft!

![image](https://user-images.githubusercontent.com/46698191/129506113-b6080e04-0dfc-4db3-be88-5095576e7227.png)

### ❓ What is this?

- The purpose of this repository is to allow GitHub and the Gazprom Neft community to use jenkins libraries for external teams
- Shared Libraries - source code (groovy based) which can be managed in a dedicated repository of the version control system and loaded on demand from the Jenkins pipeline.

- Using Gazprom Neft libraries
- Feedback and features requests

### Jenkins Library

-- Jenkins library repository on GitHub
- [Repository](https://github.com/gazprom-neft/Sharedlib-jenkins-dso)

-- Repository inside the Gazprom-neft perimeter
* Avilable only from the internal network
- [Repository](https://alm-itsk.gazprom-neft.local:8080/TFS/GPN/DSO_SERVICE/_git/sharedlib-jenkins-dso)

## Get started with the library

    // import shared library
       @Library("dsoLibrary") _
 ## Example
  
      №1  steps {
                // use name of the patchset as the build name
                wrap([$class: 'BuildUser']){
                    script {
                        if ("${params.BRANCH}" == 'null'){
                            buildName "$BUILD_NUMBER-$GIT_COMMIT_SHORT"
                            }
                        else {
                            buildName "$BUILD_NUMBER-$BUILD_ENVIRONMENT-${params.BRANCH}-$GIT_COMMIT_SHORT"
                            }
                            }
       
     №2   stage("OCP login") {
            steps {
                echo "=====ocp login====="
                logMeIn("ocp", ["registryCred": "$OCP_CRED_ID", "urlTarget": "$OCP_URL_TARGET", "ocpNamespace": "$OCP_NAMESPACE"])
            }
        }

## Pipeline
-  DockerFunction - build and publish
-  LoginUtils - authorization using the "oc" utility
-  OcFunction - Login to ocp cluster; Get UID or GID;  Install Moon (or like this) template; required unification from CaaS side with service naming; Create OCP secrets for registry; Get status of image stream; Deploy or change templates from specific directory

[vars] 
-  UiTesting - includes maven from software , run with keys pointing to settings
-- 

## Glossary General Terms
--[Glossary](https://www.jenkins.io/doc/book/glossary/#glossary) 

--The flowchart below is an example of one CD scenario easily modeled in Jenkins Pipeline:

![i9BUJ](https://user-images.githubusercontent.com/82883746/143893316-daf6e8e1-dffe-4aad-857f-c1415755d7c2.png)



# 👥  Gazprom Team
| Name | Handle | Company | Title |
| --- | --- | --- | --- |
|Pavel Lipatov | @pabloli84 | Gazprom-Neft | Head of DevOps |
|Lev Melentev | @levurevich | Gazprom-Neft | Team Tech Lead |
|Artem Batalov| @aruchibarudo | Gazprom-Neft | Sr. System Engineer |
|Andrey Kuzmin | @HillReywer |  Gazprom-Neft | Sr. System Engineer |
|Evgenij Kurnosov | @olmerdale | Gazprom-Neft | Sr. System Engineer |
|Eugene Khokhlov | @rageofgods | Gazprom-Neft | Sr. System Engineer |
|Georgij Anikin | @g-anikin |  Gazprom-Neft | Sr. System Engineer |
|Mihail Perov | @perovma | Gazprom-Neft | Sr. System Engineer |
|Aleksandr Petlin | @jakondo09 | Gazprom-Neft | Sr. System Engineer |
|Dmitrij Seleznev | @dimako12 | Gazprom-Neft | Sr. System Engineer |
|Stanislav Sobol' | @pokamolodoy406 | Gazprom-Neft | IT.security |
