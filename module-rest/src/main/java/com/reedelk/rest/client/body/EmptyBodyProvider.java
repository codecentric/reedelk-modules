package com.reedelk.rest.client.body;

class EmptyBodyProvider implements BodyProvider {

    static final BodyProvider INSTANCE = new EmptyBodyProvider();

    private EmptyBodyProvider() {
    }
}
