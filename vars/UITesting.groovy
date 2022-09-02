import com.gpn.pipeline.ExtendDescription

def call(String MavenArgs) {
    ExtendDescription extDesc = new ExtendDescription(this)

    extDesc.addString('DEPRECATEDFUNCTION', 'UITesting')
    println('DEPRECATEDFUNCTION: UITesting')

    sh """
    /usr/bin/scl enable rh-maven36 "mvn -ntp -s ./settings.xml test $MavenArgs"
    """
}
