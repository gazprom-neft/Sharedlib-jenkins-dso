#!groovy

def call(String path, List jobParameters) {
    try {
        def outSideJob = build job: path, parameters: jobParameters
        println 'last 5000 lines of build'
        def subLog = outSideJob.getRawBuild().getLog(5000)
        def stringLog = subLog.join("\n")
        println stringLog
    } catch(e) {
        println 'last 5000 lines of failed build'
        def subLog = e.getCauses()[0].getDownstreamBuild().getLog(5000)

        //This code is experimental for reducing log
        /* Get log size to reduce it */
        //def logSize = subLog.size() - 1

        /* Reduce log size */
        //def partOfLog = (int)(logSize / 2)

        /* Display it */
        // for(i = partOfLog; i <= subLog.size(); i++) {
        //     println subLog[i]
        // }

        def stringLog = subLog.join("\n")
        println stringLog
        error('Error due to downstream job failure')
    }
}