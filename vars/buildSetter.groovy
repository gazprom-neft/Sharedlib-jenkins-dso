#!groovy

import groovy.transform.Field

@Field String ENVIRONMENT
@Field String USER

def call(String branch) {
    if (!branch?.trim()) {
        buildName "$BUILD_NUMBER-$GIT_COMMIT_SHORT"
    } else {
        ENVIRONMENT = env.DEPLOY_ENVIRONMENT ?: ""
        buildName "$BUILD_NUMBER-$ENVIRONMENT-$branch-$GIT_COMMIT_SHORT"
    }
    USER = env.BUILD_USER ?: "Jenkins"
    buildDescription "Executed @ ${NODE_NAME}. Build started by ${USER}"
}