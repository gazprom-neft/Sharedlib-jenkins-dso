package com.gpn.pipeline
import com.gpn.pipeline.OcFunction

class KubeConfig {
    private Script script
    private String authProvider
    private String kubeServer
    private String kubeConfig
    private String kubeNameSpace
    private String credId

    KubeConfig(Script script, String authProvider, String kubeServer, String kubeConfig, String kubeNameSpace, String credId) {
        this.script = script
        this.authProvider = authProvider
        this.kubeServer = kubeServer
        this.kubeConfig = kubeConfig
        this.kubeNameSpace = kubeNameSpace
        this.credId = credId
    }

    void doCommand(Closure payload) {
        script.withEnv(["KUBECONFIG=${kubeConfig}"]) {
            payload()
            wipeoutKubeConfig(this.kubeConfig)
        }
    }

    void doLogin(String loginType, String vaultUrl, String vaultSecretPrefixPath, String vaultSecretPath) {
        if(!this.credId) {
            abortBuild("Error: you must set 'credId' parameter")
        } else if (!this.kubeServer) {
            abortBuild("Error: you must set 'kubeServer' parameter")
        } else if (!this.kubeNameSpace) {
            abortBuild("Error: you must set 'kubeNameSpace' parameter")
        }
        
        switch(loginType) {
            case "k8s":
                loginWithKube(vaultUrl, vaultSecretPrefixPath, vaultSecretPath)
                break
            case "ocp":
                loginWithOcp(vaultUrl, vaultSecretPrefixPath, vaultSecretPath)
                break
            default:
                abortBuild("Error: you must define 'loginType' strictly as 'ocp' or 'k8s'")
        }
    }

    private void wipeoutKubeConfig(String configName) {
    script.sh """
        set +x
        rm -f ${script.env.WORKSPACE}/${configName}
        set -x
    """
    }

    private void abortBuild(String error) {
        script.echo(error)
        script.currentBuild.result = "ABORTED"
        script.error("Aborting the build.")
    }

    private void loginWithKube(String vUrl, String vPrefixPath, String vSecretPath) {
        switch(this.authProvider) {
            case "plain":
                kubePlain()
                break
            case "vault":
                if(!vUrl) {
                    abortBuild("Error: you must set 'vaultUrl' parameter")
                } else if(!vPrefixPath) {
                    abortBuild("Error: you must set 'vaultSecretPrefixPath' parameter")
                } else if(!vSecretPath) {
                    abortBuild("Error: you must set 'vaultSecretPath' parameter")
                }
                kubeVault(vUrl, vPrefixPath, vSecretPath)
                break
            default:
                abortBuild("Error: you must define 'authProvider' strictly as 'plain' or 'vault'")
        }
    }

    private void loginWithOcp(String vUrl, String vPrefixPath, String vSecretPath) {
        switch(this.authProvider) {
            case "plain":
                ocpPlain()
                break
            case "vault":
                if(!vUrl) {
                    abortBuild("Error: you must set 'vaultUrl' parameter")
                } else if(!vPrefixPath) {
                    abortBuild("Error: you must set 'vaultSecretPrefixPath' parameter")
                } else if(!vSecretPath) {
                    abortBuild("Error: you must set 'vaultSecretPath' parameter")
                }
                ocpVault(vUrl, vPrefixPath, vSecretPath)
                break
            default:
                abortBuild("Error: you must define 'authProvider' strictly as 'plain' or 'vault'")
        }
    }

    private void kubePlain() {
        script.withCredentials([script.string(credentialsId: "${this.credId}", variable: 'kubeToken')]) {
            if(!script.kubeToken) {
                abortBuild("Error: unable to get 'token' throught plain auth")
            } else {
                kubeSetup(script.kubeToken)
            }
        }
    }

    private void kubeVault(String vUrl, String vPrefixPath, String vSecretPath) {
        def vaultSecret = [[path: vSecretPath, engineVersion: 2, secretValues: [[envVar: 'kubeToken', vaultKey: 'token']]]]
        def vaultConf = [vaultUrl: vUrl, vaultCredentialId: this.credId, prefixPath: vPrefixPath, engineVersion: 2]
        script.withVault([configuration: vaultConf, vaultSecrets: vaultSecret]) {
            if(!script.env.kubeToken) {
                abortBuild("Error: unable to get 'token' throught Vault auth")
            } else {
                kubeSetup(script.env.kubeToken)
            }
        }
    }

    private void kubeSetup(String kubeToken) {
        script.sh """
            set +x
            kubectl config set-cluster cfc --kubeconfig=${this.kubeConfig} --server=${this.kubeServer} 2>&1 >/dev/null && \
            kubectl config set-cluster cfc --kubeconfig=${this.kubeConfig} --insecure-skip-tls-verify=true 2>&1 >/dev/null && \
            kubectl config set-context cfc --kubeconfig=${this.kubeConfig} --cluster=cfc 2>&1 >/dev/null && \
            kubectl config set-context cfc --kubeconfig=${this.kubeConfig} --namespace=${this.kubeNameSpace} 2>&1 >/dev/null && \
            kubectl config set-credentials --kubeconfig=${this.kubeConfig} user --token=${kubeToken} 2>&1 >/dev/null && \
            kubectl config set-context cfc --kubeconfig=${this.kubeConfig} --user=user 2>&1 >/dev/null && \
            kubectl config use-context cfc --kubeconfig=${this.kubeConfig} 2>&1 >/dev/null 
            set -x
        """
    }

    private void ocpPlain() {
        OcFunction ocFunc = new OcFunction(this.script)
        ocFunc.ocLogin(this.credId, this.kubeServer, this.kubeNameSpace)
        ocpSetup()
    }

    private void ocpVault(String vUrl, String vPrefixPath, String vSecretPath) {
        OcFunction ocFunc = new OcFunction(this.script)
        ocFunc.ocLoginVault(this.credId, this.kubeServer, this.kubeNameSpace, vUrl, vPrefixPath, vSecretPath)
        ocpSetup()
    }

    private void ocpSetup() {
        script.sh """
            set +x
            oc config set-cluster ${this.kubeServer.replaceFirst(/^(?i)https?:\/\//,'').replace('.','-')} --insecure-skip-tls-verify=true
            oc config view --raw > ${script.env.WORKSPACE}/${this.kubeConfig}
            chmod 600 ${script.env.WORKSPACE}/${this.kubeConfig}
            set -x
        """
    }
}