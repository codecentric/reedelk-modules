package com.reedelk.esb.pubsub;

import java.lang.ref.WeakReference;
import java.util.concurrent.ConcurrentHashMap;

public class Event {

    static {
        init();
    }

    public static Operation operation;

    static ConcurrentHashMap<String, ConcurrentHashMap<Integer, WeakReference<Object>>> channels;

    static void init() {
        channels = new ConcurrentHashMap<>();
        operation = new Operation();
    }
}
