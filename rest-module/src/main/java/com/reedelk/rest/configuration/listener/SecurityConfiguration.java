package com.reedelk.rest.configuration.listener;

import com.reedelk.runtime.api.annotation.Default;
import com.reedelk.runtime.api.annotation.Property;
import com.reedelk.runtime.api.annotation.When;
import com.reedelk.runtime.api.component.Implementor;
import org.osgi.service.component.annotations.Component;

import static org.osgi.service.component.annotations.ServiceScope.PROTOTYPE;

@Component(service = SecurityConfiguration.class, scope = PROTOTYPE)
public class SecurityConfiguration implements Implementor {

    @Property("Type")
    @Default("CERTIFICATE_AND_PRIVATE_KEY")
    private ServerSecurityType type;

    @Property("X.509 Certificate and private key")
    @When(propertyName = "type", propertyValue = "CERTIFICATE_AND_PRIVATE_KEY")
    private CertificateAndPrivateKeyConfiguration certificateAndPrivateKey;

    @Property("Key store")
    @When(propertyName = "type", propertyValue = "KEY_STORE")
    private KeyStoreConfiguration keyStore;

    @Property("Use trust store")
    @Default("false")
    private boolean useTrustStore;

    @Property("Trust store configuration")
    @When(propertyName = "useTrustStore", propertyValue = "true")
    private TrustStoreConfiguration trustStoreConfiguration;

    public ServerSecurityType getType() {
        return type;
    }

    public void setType(ServerSecurityType type) {
        this.type = type;
    }

    public CertificateAndPrivateKeyConfiguration getCertificateAndPrivateKey() {
        return certificateAndPrivateKey;
    }

    public void setCertificateAndPrivateKey(CertificateAndPrivateKeyConfiguration certificateAndPrivateKey) {
        this.certificateAndPrivateKey = certificateAndPrivateKey;
    }

    public KeyStoreConfiguration getKeyStore() {
        return keyStore;
    }

    public void setKeyStore(KeyStoreConfiguration keyStore) {
        this.keyStore = keyStore;
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
