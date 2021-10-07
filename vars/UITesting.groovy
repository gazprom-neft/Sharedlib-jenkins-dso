def call(String MavenArgs) {

    sh """
    /usr/bin/scl enable rh-maven36 "mvn -ntp -s ./settings.xml test $MavenArgs"
    """

}