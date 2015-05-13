package com.github.dreamhead.moco;

import com.github.dreamhead.moco.model.MessageContent;
import com.github.dreamhead.moco.resource.ContentResource;
import com.google.common.base.Optional;
import com.google.common.io.Closer;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.Security;

import static com.github.dreamhead.moco.util.Preconditions.checkNotNullOrEmpty;
import static com.google.common.base.Preconditions.checkNotNull;

public class HttpsCertificate {

    private static final String PROTOCOL = "TLS";
    private static final String DEFAULT_ALGORITHM = "SunX509";
    private final ContentResource resource;
    private final String keyStorePassword;
    private final String certPassword;

    private HttpsCertificate(final ContentResource resource, final String keyStorePassword, final String certPassword) {
        this.resource = resource;
        this.keyStorePassword = keyStorePassword;
        this.certPassword = certPassword;
    }

    public SSLEngine createSSLEngine() {
        return createServerContext().createSSLEngine();
    }

    private SSLContext createServerContext() {
        Closer closer = Closer.create();
        try {
            KeyStore keyStore = KeyStore.getInstance("JKS");
            InputStream is = this.getKeyStore();
            closer.register(is);
            keyStore.load(is, this.getKeyStorePassword());
            KeyManagerFactory factory = KeyManagerFactory.getInstance(getAlgorithm());
            factory.init(keyStore, this.getCertPassword());

            SSLContext serverContext = SSLContext.getInstance(PROTOCOL);
            serverContext.init(factory.getKeyManagers(), null, null);
            return serverContext;
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize the server-side SSLContext", e);
        } finally {
            try {
                closer.close();
            } catch (IOException e) {
                throw new RuntimeException("failed to close keystore", e);
            }
        }
    }

    private static String getAlgorithm() {
        String algorithm = Security.getProperty("ssl.KeyManagerFactory.algorithm");
        if (algorithm == null) {
            return DEFAULT_ALGORITHM;
        }

        return algorithm;
    }

    private InputStream getKeyStore() {
        MessageContent messageContent = resource.readFor(Optional.<Request>absent());
        return messageContent.toInputStream();
    }

    private char[] getKeyStorePassword() {
        return keyStorePassword.toCharArray();
    }

    private char[] getCertPassword() {
        return certPassword.toCharArray();
    }

    public static HttpsCertificate certificate(final ContentResource resource, final String keyStorePassword, String certPassword) {
        return new HttpsCertificate(checkNotNull(resource),
                checkNotNullOrEmpty(keyStorePassword, "Key store password should not be null"),
                checkNotNullOrEmpty(certPassword, "Cert password should not be null"));
    }
}
