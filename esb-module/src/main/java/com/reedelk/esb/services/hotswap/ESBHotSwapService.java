package com.reedelk.esb.services.hotswap;

import com.reedelk.runtime.system.api.BundleNotFoundException;
import com.reedelk.runtime.system.api.HotSwapService;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

import static java.lang.String.format;

public class ESBHotSwapService implements HotSwapService {

    private static final Logger logger = LoggerFactory.getLogger(ESBHotSwapService.class);

    private final BundleContext context;
    private final HotSwapListener listener;

    public ESBHotSwapService(BundleContext context, HotSwapListener listener) {
        this.context = context;
        this.listener = listener;
    }

    @Override
    public long hotSwap(String modulePath, String resourcesRootDirectory) throws BundleNotFoundException {
        Optional<Bundle> optionalBundle = getModuleAtPath(modulePath);
        Bundle bundleAtPath = optionalBundle
                .orElseThrow(() ->
                        new BundleNotFoundException(format("Hot Swap failed: could not find registered bundle in target file path=%s", modulePath)));

        listener.hotSwap(bundleAtPath.getBundleId(), resourcesRootDirectory);

        logger.info("Module [{}] updated", bundleAtPath.getSymbolicName());
        return bundleAtPath.getBundleId();
    }

    private Optional<Bundle> getModuleAtPath(String bundlePath) {
        return Optional.ofNullable(context.getBundle(bundlePath));
    }

}
