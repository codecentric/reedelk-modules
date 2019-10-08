package com.reedelk.rest.client.authentication;

import org.apache.commons.codec.binary.Base64;

import java.nio.charset.StandardCharsets;

public class BasicAuthentication implements Authenticator {

    private final String password;
    private final String username;

    public BasicAuthentication(String username, String password) {
        this.username = username;
        this.password = password;
    }

    @Override
    public String authenticationHeader() {
        String auth = username + ":" + password;
        byte[] encodedAuth = Base64.encodeBase64(auth.getBytes(StandardCharsets.ISO_8859_1));
        return "Basic " + new String(encodedAuth);
    }
}
