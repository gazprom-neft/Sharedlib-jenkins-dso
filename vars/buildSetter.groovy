#!groovy

import groovy.transform.Field

@Field String ENVIRONMENT
@Field String USER
@Field String USER_EMAIL

def call(String branch=null, Map parameters = [:]) {
    def OCP_APP_NAME
    def UPSTREAM_BUILD_DESCRIPTION
    def UPSTREAM_BUILD
    def UPSTREAM_BUILD_NUMBER
    def DEPLOY_ENVIRONMENT
    def EXT_DESCRIPTION = ""

//====== Set properties from project folders ======
    withFolderProperties{
        LANDSCAPE_ID = "${env.LANDSCAPE_ID}"
        OCP_URL_TARGET = "${env.OCP_URL_TARGET}"
        DEPLOY_ENVIRONMENT = "${env.DEPLOY_ENVIRONMENT}"
        DSO_TYPE = "${env.DSO_TYPE}"
    }

//====== Check & set parameters for main description block ======
    if (env.GIT_URL) {
        GIT_URL = env.GIT_URL
    } else {
        GIT_URL = ""
    }
    if (env.GIT_BRANCH) {
        GIT_BRANCH = env.GIT_BRANCH
    } else {
        GIT_BRANCH = ""
    }
    if (env.GIT_COMMIT) {
        GIT_COMMIT = env.GIT_COMMIT
        GIT_COMMIT_SHORT = env.GIT_COMMIT[0..6]
    } else {
        GIT_COMMIT = ""
        GIT_COMMIT_SHORT = ""
    }

    if (env.OCP_APP_NAME) {
        OCP_APP_NAME = env.OCP_APP_NAME
    } else {
        OCP_APP_NAME = ""
    }

    if (env.BUILD_ENVIRONMENT) {
        ENVIRONMENT = env.BUILD_ENVIRONMENT
    } else if ("${DEPLOY_ENVIRONMENT}") {
        ENVIRONMENT = "${DEPLOY_ENVIRONMENT}"
    } else {
        ENVIRONMENT = ""
    }

//------ Get params from upstream job (if exist) ------
    try {
        def upstreamBuild = currentBuild.rawBuild.getCause(hudson.model.Cause$UpstreamCause)
        UPSTREAM_BUILD_DESCRIPTION = upstreamBuild?.shortDescription
        println UPSTREAM_BUILD_DESCRIPTION
    } catch(e) {
        println 'Error due to upstream job failure'
        println(e.toString())
        UPSTREAM_BUILD_DESCRIPTION = ""
    }

    if (UPSTREAM_BUILD_DESCRIPTION) {
        def ub = UPSTREAM_BUILD_DESCRIPTION =~ /"(.+)"/
        if (ub) {
            UPSTREAM_BUILD = ub[0][1]
        }

        def bn = UPSTREAM_BUILD_DESCRIPTION =~ /build\snumber\s([0-9,]+)$/
        if (bn) {
            UPSTREAM_BUILD_NUMBER = bn[0][1].split(',').join('')
        }
    } else {
        UPSTREAM_BUILD = ""
        UPSTREAM_BUILD_NUMBER = ""
    }

//====== Check & set parameters for extended description block ======
    if (parameters.registry) {
        EXT_DESCRIPTION = """$EXT_DESCRIPTION
        !!!REGISTRY = $parameters.registry !!!"""
    }

    if (parameters.image) {
        EXT_DESCRIPTION = """$EXT_DESCRIPTION
        !!!IMAGE = $parameters.image !!!"""
    }

    if (parameters.image_tag) {
        EXT_DESCRIPTION = """$EXT_DESCRIPTION
        !!!IMAGE_TAG = $parameters.image_tag !!!"""
    }

    if (parameters.approver_name) {
        EXT_DESCRIPTION = """$EXT_DESCRIPTION
        !!!APPROVER = $parameters.approver_name !!!"""
    }

    if (DSO_TYPE != 'null') {
        EXT_DESCRIPTION = """$EXT_DESCRIPTION
        !!!DSO_TYPE = $DSO_TYPE !!!"""
    }

//====== Set custom build name ======
    if (parameters.customBuildName) {
        buildName "$BUILD_NUMBER-$parameters.customBuildName"
    } else if (!GIT_BRANCH?.trim()) {
        buildName "$BUILD_NUMBER-$ENVIRONMENT"
    } else {
        buildName "$BUILD_NUMBER-$ENVIRONMENT-$GIT_BRANCH-$GIT_COMMIT_SHORT"
    }

//====== Set description. Main block + extended block (if exist) ======
    wrap([$class: 'BuildUser']){
    USER_EMAIL = env.BUILD_USER_EMAIL
    USER_ID = env.BUILD_USER_ID ?: "Jenkins"
    buildDescription """\
        Executed @ ${NODE_NAME}. Build started by ${USER_ID}
        !!!LANDSCAPE_ID = $LANDSCAPE_ID !!!
        !!!BUILD_NUMBER = $BUILD_NUMBER !!!
        !!!GIT_URL = $GIT_URL !!!
        !!!GIT_BRANCH = $GIT_BRANCH !!!
        !!!GIT_COMMIT = $GIT_COMMIT !!!
        !!!GIT_COMMIT_SHORT = $GIT_COMMIT_SHORT !!!
        !!!APP_NAME = $OCP_APP_NAME !!!
        !!!BUILD_USER = $USER_ID !!!
        !!!NODE_NAME = $NODE_NAME !!!
        !!!NODE_LABELS = $NODE_LABELS !!!
        !!!OCP_URL_TARGET = $OCP_URL_TARGET !!!
        !!!UPSTREAM_BUILD = $UPSTREAM_BUILD !!!
        !!!UPSTREAM_BUILD_NUMBER = $UPSTREAM_BUILD_NUMBER !!!
        $EXT_DESCRIPTION
        """.stripIndent()
    }
}