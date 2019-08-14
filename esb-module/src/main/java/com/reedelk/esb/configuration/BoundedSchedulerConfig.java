package com.reedelk.esb.configuration;

public class BoundedSchedulerConfig implements SchedulerConfig {

    private final int poolMinSize;
    private final int poolMaxSize;
    private final int keepAliveTime;

    public BoundedSchedulerConfig(int poolMinSize, int poolMaxSize, int keepAliveTime) {
        this.poolMinSize = poolMinSize;
        this.poolMaxSize = poolMaxSize;
        this.keepAliveTime = keepAliveTime;
    }

    @Override
    public boolean isBounded() {
        return true;
    }

    @Override
    public int keepAliveTime() {
        return keepAliveTime;
    }

    @Override
    public int poolMinSize() {
        return poolMinSize;
    }

    @Override
    public int poolMaxSize() {
        return poolMaxSize;
    }
}
