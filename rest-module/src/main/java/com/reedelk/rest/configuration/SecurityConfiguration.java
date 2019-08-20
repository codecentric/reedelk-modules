package com.reedelk.rest.configuration;

import com.reedelk.runtime.api.annotation.Default;
import com.reedelk.runtime.api.annotation.Property;
import com.reedelk.runtime.api.component.Implementor;
import org.osgi.service.component.annotations.Component;

import static org.osgi.service.component.annotations.ServiceScope.PROTOTYPE;

@Component(service = SecurityConfiguration.class, scope = PROTOTYPE)
public class SecurityConfiguration implements Implementor {

    @Property("Type")
    @Default("CERTIFICATE_AND_PRIVATE_KEY")
    private ServerSecurityType configurationType;

    @Property("X.509 Certificate and private key")
    private CertificateAndPrivateKeyConfiguration certificateAndPrivateKeyConfiguration;

    @Property("Key store")
    private KeyStoreConfiguration keyStoreConfiguration;

    @Property("Use trust store")
    private Boolean useTrustStore;

    @Property("Trust store configuration")
    private TrustStoreConfiguration trustStoreConfiguration;

    public ServerSecurityType getConfigurationType() {
        return configurationType;
    }

    public void setConfigurationType(ServerSecurityType configurationType) {
        this.configurationType = configurationType;
    }

    public CertificateAndPrivateKeyConfiguration getCertificateAndPrivateKeyConfiguration() {
        return certificateAndPrivateKeyConfiguration;
    }

    public void setCertificateAndPrivateKeyConfiguration(CertificateAndPrivateKeyConfiguration certificateAndPrivateKeyConfiguration) {
        this.certificateAndPrivateKeyConfiguration = certificateAndPrivateKeyConfiguration;
    }

    public KeyStoreConfiguration getKeyStoreConfiguration() {
        return keyStoreConfiguration;
    }

    public void setKeyStoreConfiguration(KeyStoreConfiguration keyStoreConfiguration) {
        this.keyStoreConfiguration = keyStoreConfiguration;
    }

    public Boolean getUseTrustStore() {
        return useTrustStore;
    }

    public void setUseTrustStore(Boolean useTrustStore) {
        this.useTrustStore = useTrustStore;
    }

    public TrustStoreConfiguration getTrustStoreConfiguration() {
        return trustStoreConfiguration;
    }

    public void setTrustStoreConfiguration(TrustStoreConfiguration trustStoreConfiguration) {
        this.trustStoreConfiguration = trustStoreConfiguration;
    }
}
