import com.gpn.pipeline.DockerFunction
import com.gpn.pipeline.ExtendDescription

/**
*/
def call(String func, Map parameters = [:]) {
    DockerFunction dockerFunc = new DockerFunction(this)
    ExtendDescription extDesc = new ExtendDescription(this)
    def registry = parameters.registry ?: ""
    def registryCred = parameters.registryCred ?: ""
    def dockerfileName = parameters.dockerfileName ?: ""
    def additionalArgs = parameters.additionalArgs ?: ""
    def ocpNamespace
    def ocpAppName
    def gitCommitShort
    def registry_path
    def image_name
    def image_tag

    if (parameters.registry_path) {
        registry_path = parameters.registry_path.toLowerCase()
    } else {
        registry_path = parameters.ocpNamespace
    }

    if (parameters.image_name) {
        image_name = parameters.image_name
    } else {
        image_name = parameters.ocpAppName
    }

    if (parameters.image_tag) {
        image_tag = parameters.image_tag
    } else {
        image_tag = parameters.gitCommitShort
    }

    switch(func) {
        case "login":
            echo "=====docker login registry====="
            dockerFunc.dockerLogin(registryCred, registry)
            break
        case "build":
            echo "=====docker build====="
            dockerFunc.dockerBuild(registry, registry_path, image_name, image_tag, dockerfileName, additionalArgs)
            break
        case "push":
            echo "=====docker push====="
            dockerFunc.dockerPush(registry, registry_path, image_name, image_tag)
            // add string to extend description
            extDesc.addString('IMAGE_NAME', image_name)
            extDesc.addString('IMAGE_TAG', image_tag)
            break
        case "logout":
            echo "=====docker logout registry====="
            dockerFunc.dockerLogout(registry, registry_path)
            break
        case "full":
            echo "===This is full docker build pipeline==="
            dockerFunc.dockerFull(registryCred, registry, registry_path, image_name, image_tag, dockerfileName, additionalArgs)
            // add string to extend description
            extDesc.addString('IMAGE_NAME', image_name)
            extDesc.addString('IMAGE_TAG', image_tag)           
            break
    }
}