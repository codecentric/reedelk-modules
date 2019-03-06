package com.esb.services.hotswap;

public interface HotSwapListener {

    void hotSwap(long moduleId, String resourcesRootDirectory);

}
