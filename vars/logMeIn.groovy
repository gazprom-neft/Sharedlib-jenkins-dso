import com.gpn.pipeline.LoginUtils

/**
* Executes a login function based on place
* @param place String place where you want to login
* @param parameters Map with specific for place parameters. For now registryCred is for Jenkins credId and registry or urlTarget for path of place
*/
def call(String place, Map parameters = [:]) {
    LoginUtils logUtils = new LoginUtils(this)

    switch(place) {
        case "docker":
            logUtils.dockerLogin(parameters.registryCred, parameters.registry)      
            break
        case "ocp":
            logUtils.ocpLogin(parameters.registryCred, parameters.urlTarget)
            break
    }
}