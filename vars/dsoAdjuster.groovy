#!groovy
import com.gpn.pipeline.ExtendDescription

def call() {
    ExtendDescription extDesc = new ExtendDescription(this)

    extDesc.addString('DEPRECATEDFUNCTION', 'dsoAdjuster')
    println('DEPRECATEDFUNCTION: dsoAdjuster')

    properties([[$class: 'BuildDiscarderProperty', strategy: [$class: 'LogRotator', artifactDaysToKeepStr: '', artifactNumToKeepStr: '', daysToKeepStr: '90', numToKeepStr: '1000']]]);
}
