package com.esb.services.hotswap;

import com.esb.internal.api.HotSwapService;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

import java.util.Optional;

import static com.esb.commons.Preconditions.checkIsPresentAndGetOrThrow;

public class ESBHotSwapService implements HotSwapService {

    private final BundleContext context;
    private final HotSwapListener listener;

    public ESBHotSwapService(BundleContext context, HotSwapListener listener) {
        this.context = context;
        this.listener = listener;
    }

    @Override
    public long hotSwap(String modulePath, String resourcesRootDirectory) {
        Optional<Bundle> optionalBundle = getModuleAtPath(modulePath);
        Bundle bundleAtPath = checkIsPresentAndGetOrThrow(optionalBundle, "Hot Swap failed: could not find registered bundle in target file path=%s", modulePath);

        listener.hotSwap(bundleAtPath.getBundleId(), resourcesRootDirectory);
        return bundleAtPath.getBundleId();
    }

    private Optional<Bundle> getModuleAtPath(String bundlePath) {
        return Optional.ofNullable(context.getBundle(bundlePath));
    }

}
