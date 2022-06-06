#!groovy

def call(String path, String email, List jobParameters) {
    def approver
    script {
        emailext (
            subject: "$aprv_mail_subject",
            body: """$aprv_mail_message Job ${env.JOB_NAME} ${env.BUILD_URL}input""",
            to: "$aprv_approvers_mail"
        )

        def userInput = input(
            id: 'Approve1', 
            message: "$aprv_input_message", 
            submitter: "$aprv_approvers", 
            submitterParameter: "approver", 
            parameters: [
                [$class: 'BooleanParameterDefinition', defaultValue: false, description: '', name: 'Please confirm you agree with this']
                ]
            )

        approver = "${userInput.approver}"

        echo 'userInput: ' + userInput

            if(userInput['Please confirm you agree with this'] == true) {
                def outSideJob = build job: path, parameters: jobParameters
                emailext (
                    subject: "$aprv_mail_subject_success",
                    body: """$aprv_mail_message_success Job ${env.JOB_NAME}""",
                    to: "$email"
                )
            } else {
                emailext (
                    subject: "$aprv_mail_subject_decline",
                    body: """$aprv_mail_message_decline Job ${env.JOB_NAME}""",
                    to: "$email"
                )
                catchError(buildResult: 'ABORTED', stageResult: 'ABORTED') {
                    error 'Action was aborted.'
            }
        }
    }
    return approver
}
