package com.esb.foonnel.rest.commons;

import java.util.Objects;

public class HostNamePortKey {

    private final String hostname;
    private final int port;

    private HostNamePortKey(String hostname, int port) {
        this.hostname = hostname;
        this.port = port;
    }

    public static HostNamePortKey get(String hostname, int port) {
        return new HostNamePortKey(hostname, port);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HostNamePortKey keyEntry = (HostNamePortKey) o;
        return port == keyEntry.port &&
                hostname.equals(keyEntry.hostname);
    }

    @Override
    public int hashCode() {
        return Objects.hash(hostname, port);
    }

}
