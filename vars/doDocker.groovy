import com.gpn.pipeline.DockerFunction

/**
*/
def call(String func, Map parameters = [:]) {
    DockerFunction dockerFunc = new DockerFunction(this)

    switch(func) {
        case "login":
            echo "=====docker login registry function is DEPRECEATED=====" +
                  "=====use docker functions directly and login functionality will be applied automatically"
            break
        case "build":
            echo "=====docker build====="
            dockerFunc.dockerBuild(parameters.registry, 
                                    parameters.ocpNamespace, 
                                    parameters.ocpAppName, 
                                    parameters.gitCommitShort, 
                                    parameters.dockerfileName,
                                    parameters.additionalArgs)
            break
        case "push":
            echo "=====docker push====="
            docker.withRegistry(
                parameters.registryUrl,
                parameters.registryCred
            ) {
                dockerFunc.dockerPush(parameters.registry,
                                    parameters.ocpNamespace,
                                    parameters.ocpAppName,
                                    parameters.gitCommitShort)
            }
            break
        case "full":
            echo "===This is full docker build pipeline==="
            docker.withRegistry(
                parameters.registryUrl,
                parameters.registryCred
            ) {
                dockerFunc.dockerFull(parameters.registry, 
                                    parameters.ocpNamespace, 
                                    parameters.ocpAppName, 
                                    parameters.gitCommitShort, 
                                    parameters.dockerfileName,
                                    parameters.additionalArgs)
            }
            break
    }
}