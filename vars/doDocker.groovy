import com.gpn.pipeline.DockerFunction

/**
*/
def call(String func, Map parameters = [:]) {
    DockerFunction dockerFunc = new DockerFunction(this)

    switch(func) {
        case "login":
            echo "=====docker login registry====="
            dockerFunc.dockerLogin(parameters.registryCred, parameters.registry)
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
            dockerFunc.dockerPush(parameters.registry,
                                    parameters.ocpNamespace,
                                    parameters.ocpAppName,
                                    parameters.gitCommitShort)
            break
        case "full":
            echo "===This is full docker build pipeline==="
            dockerFunc.dockerFull(parameters.registryCred,
                                    parameters.registry, 
                                    parameters.ocpNamespace, 
                                    parameters.ocpAppName, 
                                    parameters.gitCommitShort, 
                                    parameters.dockerfileName,
                                    parameters.additionalArgs)
            break
    }
}