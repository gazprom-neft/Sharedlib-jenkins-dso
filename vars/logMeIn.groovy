// It's depricated and will be removed soon. Use doDocker and doOc functions
import com.gpn.pipeline.LoginUtils

/**
* Executes a login function based on place
* @param place String place where you want to login
* @param parameters Map with specific for place parameters. For now registryCred is for Jenkins credId and registry or urlTarget for path of place
*/
def call(String place, Map parameters = [:], Closure body=null) {
    LoginUtils logUtils = new LoginUtils(this)

    switch(place) {
        case "ocp":
            logUtils.ocpLogin(parameters.registryCred, parameters.urlTarget, parameters.ocpNamespace)
            break
    }
    if (body != null) { body() }
}