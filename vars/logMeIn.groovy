#!groovy

import com.gpn.pipeline.LoginUtils

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