package com.esb.services.module;

public interface EventListener {

    void moduleStarted(long moduleId);

    void moduleStopping(long moduleId);

    void moduleStopped(long moduleId);

    void componentRegistered(String componentName);

    void componentUnregistering(String componentName);

}