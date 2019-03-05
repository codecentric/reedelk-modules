package com.esb.commons;

@FunctionalInterface
public interface ConsumerWithException<T, E extends Exception> {

    void accept(T t) throws E;

}
