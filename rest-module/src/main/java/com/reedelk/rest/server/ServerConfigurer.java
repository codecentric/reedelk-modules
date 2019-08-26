package com.reedelk.rest.server;

import com.reedelk.rest.configuration.*;
import com.reedelk.runtime.api.exception.ESBException;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelOption;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.timeout.ReadTimeoutHandler;
import reactor.netty.Connection;
import reactor.netty.http.server.HttpRequestDecoderSpec;
import reactor.netty.http.server.HttpServer;
import reactor.netty.tcp.TcpServer;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLException;
import javax.net.ssl.TrustManagerFactory;
import java.io.File;
import java.io.FileInputStream;
import java.security.KeyStore;
import java.util.function.Consumer;
import java.util.function.Function;

import static io.netty.channel.ChannelOption.*;
import static java.util.Objects.requireNonNull;

class ServerConfigurer {

    static void configure(ServerBootstrap bootstrap, RestListenerConfiguration configuration) {
        setChannelOption(bootstrap, SO_BACKLOG, configuration.getSocketBacklog());
        setChannelOption(bootstrap, CONNECT_TIMEOUT_MILLIS, configuration.getConnectionTimeoutMillis());
        setChannelChildOption(bootstrap, SO_KEEPALIVE, configuration.getKeepAlive());
    }

    static Consumer<Connection> onConnection(RestListenerConfiguration configuration) {
        return connection -> {
            if (configuration.getReadTimeoutMillis() != null) {
                connection.addHandlerLast("readTimeout", new ReadTimeoutHandler(configuration.getReadTimeoutMillis()));
            }
        };
    }

    static HttpServer configure(HttpServer server, RestListenerConfiguration configuration) {
        server = configureHost(server, configuration);
        server = configurePort(server, configuration);
        server = configureCompress(server, configuration);
        return server.httpRequestDecoder(ServerConfigurer.configure(configuration));
    }

    private static HttpServer configureCompress(HttpServer server, RestListenerConfiguration configuration) {
        if (configuration.getCompress() != null) {
            return server.compress(configuration.getCompress());
        }
        return server;
    }

    private static HttpServer configureHost(HttpServer server, RestListenerConfiguration configuration) {
        if (configuration.getHost() != null) {
            return server.host(configuration.getHost());
        }
        return server;
    }

    private static HttpServer configurePort(HttpServer server, RestListenerConfiguration configuration) {
        return server.port(configuration.getPort());
    }

    private static <T> void setChannelOption(ServerBootstrap serverBootstrap, ChannelOption<T> channelOption, T value) {
        if (value != null) {
            serverBootstrap.option(channelOption, value);
        }
    }

    private static <T> void setChannelChildOption(ServerBootstrap serverBootstrap, ChannelOption<T> channelOption, T value) {
        if (value != null) {
            serverBootstrap.childOption(channelOption, value);
        }
    }

    private static Function<HttpRequestDecoderSpec, HttpRequestDecoderSpec> configure(RestListenerConfiguration configuration) {
        return decoder -> {
            if (configuration.getMaxChunkSize() != null) {
                decoder.maxChunkSize(configuration.getMaxChunkSize());
            }
            if (configuration.getValidateHeaders() != null) {
                decoder.validateHeaders(configuration.getValidateHeaders());
            }
            if (configuration.getMaxLengthOfAllHeaders() != null) {
                decoder.maxHeaderSize(configuration.getMaxLengthOfAllHeaders());
            }
            return decoder;
        };
    }

    static TcpServer configureSecurity(TcpServer bootstrap, RestListenerConfiguration configuration) {
        // Security is configured if and only if the protocol is HTTPS
        if (!HttpProtocol.HTTPS.equals(configuration.getProtocol())) return bootstrap;
        if (configuration.getSecurityConfiguration() == null) return bootstrap;

        SecurityConfiguration securityConfig = configuration.getSecurityConfiguration();

        return bootstrap.secure(sslContextSpec -> {

            SslContextBuilder contextBuilder;

            ServerSecurityType configurationType = securityConfig.getType();
            if (ServerSecurityType.KEY_STORE.equals(configurationType)) {
                KeyStoreConfiguration keyStoreConfig = requireNonNull(securityConfig.getKeyStore(), "key store config");
                contextBuilder = SslContextBuilder.forServer(getKeyManagerFactory(keyStoreConfig));

            } else if (ServerSecurityType.CERTIFICATE_AND_PRIVATE_KEY.equals(configurationType)) {
                // TODO: Error handilng
                CertificateAndPrivateKeyConfiguration config =
                        requireNonNull(securityConfig.getCertificateAndPrivateKey(), "certificate and private key configuration");
                File certificateFile = new File(config.getCertificateFile());
                File privateKeyFile = new File(config.getPrivateKeyFile());
                contextBuilder = SslContextBuilder.forServer(certificateFile, privateKeyFile);

            } else {
                throw new ESBException("Wrong config");
            }

            if (securityConfig.getUseTrustStore() != null && securityConfig.getUseTrustStore()) {
                TrustStoreConfiguration trustStoreConfiguration =
                        requireNonNull(securityConfig.getTrustStoreConfiguration(), "trust store config");
                    contextBuilder.trustManager(getTrustManagerFactory(trustStoreConfiguration));
            }

            try {
                sslContextSpec.sslContext(contextBuilder.build());
            } catch (SSLException e) {
                throw new ESBException(e);
            }
        });
    }

    private static TrustManagerFactory getTrustManagerFactory(TrustStoreConfiguration config) {
        String type = config.getType();
        String location = config.getPath();
        String password = config.getPassword();
        String algorithm = config.getAlgorithm();
        try {
            String alg = algorithm == null ? TrustManagerFactory.getDefaultAlgorithm() : algorithm;
            TrustManagerFactory factory = TrustManagerFactory.getInstance(alg);
            KeyStore keyStore = type == null ? KeyStore.getInstance(KeyStore.getDefaultType()) : KeyStore.getInstance(type);
            try (FileInputStream fileInputStream = new FileInputStream(location)) {
                keyStore.load(fileInputStream, password.toCharArray());
            }
            factory.init(keyStore);
            return factory;
        } catch (Exception e) {
            throw new ESBException(e);
        }
    }

    private static KeyManagerFactory getKeyManagerFactory(KeyStoreConfiguration config) {
        try {
            String type = config.getType();
            String location = config.getPath();
            String password = config.getPassword();
            String algorithm = config.getAlgorithm();
            String alg = algorithm == null ? KeyManagerFactory.getDefaultAlgorithm() : algorithm;
            KeyManagerFactory factory = KeyManagerFactory.getInstance(alg);
            KeyStore keyStore = type == null ? KeyStore.getInstance(KeyStore.getDefaultType()) : KeyStore.getInstance(type);
            try (FileInputStream fileInputStream = new FileInputStream(location)) {
                keyStore.load(fileInputStream, password.toCharArray());
            }
            factory.init(keyStore, password.toCharArray());
            return factory;
        } catch (Exception e) {
            throw new ESBException(e);
        }
    }
}
