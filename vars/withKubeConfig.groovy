import com.gpn.pipeline.KubeConfig

void call(Map param, Closure payload) {
    String kubeConfig = '.kube.cfg'
    String loginType = param.login_type ?: "ocp" // ocp/k8s
    String authProvider = param.auth_provider // plain/vault
    String vaultSecretPrefixPath = param.secret_prefix_path ?: "paashub"
    String vaultUrl = param.vault_url ?: env.VAULT_TRUST_URL
    withFolderProperties {
        vaultSecretPath = param.secret_path ?: "${env.PROJECT_ID}/caas/${env.LANDSCAPE_ID}-${env.CLUSTER_ID}-${env.OCP_NAMESPACE}/${env.OCP_NAMESPACE}/accounts/sa"
        kubeServer = param.kube_server ?: env.OCP_URL_TARGET
        kubeNameSpace = param.kube_namespace ?: env.OCP_NAMESPACE
        if (authProvider == "vault") {
            credId = param.cred_id ?: "jenkins-user-${env.PROJECT_ID}-role"
        } else {
            credId = param.cred_id ?: env.OCP_CRED_ID
        }
    }

    KubeConfig kc = new KubeConfig(this, authProvider, kubeServer, kubeConfig, kubeNameSpace, credId)
    kc.doLogin(loginType, vaultUrl, vaultSecretPrefixPath, vaultSecretPath)
    kc.doCommand(payload)
}