package com.esb.services.module;

import com.esb.api.exception.ESBException;
import com.esb.module.ModulesManager;
import com.esb.internal.api.module.v1.ModuleGETRes;
import com.esb.internal.api.module.v1.ModuleService;
import com.esb.internal.api.module.v1.ModulesGETRes;
import com.esb.services.event.EventListener;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.Set;

import static com.esb.commons.FunctionWrapper.uncheckedConsumer;
import static com.esb.commons.Preconditions.checkIsPresentAndGetOrThrow;
import static com.esb.commons.Preconditions.checkState;
import static java.lang.String.format;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toSet;

public class ESBModuleService implements ModuleService {

    private static final Logger logger = LoggerFactory.getLogger(ESBModuleService.class);

    private final ModulesMapper mapper = new ModulesMapper();

    private final BundleContext bundleContext;
    private final EventListener eventListener;
    private final ModulesManager modulesManager;

    public ESBModuleService(BundleContext bundleContext, ModulesManager modulesManager, EventListener eventListener) {
        this.eventListener = eventListener;
        this.bundleContext = bundleContext;
        this.modulesManager = modulesManager;
    }

    @Override
    public long update(String modulePath) {
        Optional<Bundle> optionalBundleAtPath = getModuleAtPath(modulePath);
        Bundle bundleAtPath = checkIsPresentAndGetOrThrow(optionalBundleAtPath, "Update failed: could not find registered bundle in target file path=%s", modulePath);

        eventListener.moduleStopping(bundleAtPath.getBundleId());
        executeOperation(bundleAtPath, Bundle::stop, Bundle::update, Bundle::start);

        logger.debug("Module [{}] updated", bundleAtPath.getSymbolicName());

        return bundleAtPath.getBundleId();
    }

    @Override
    public long uninstall(String modulePath) {
        Optional<Bundle> optionalBundleAtPath = getModuleAtPath(modulePath);
        Bundle bundleAtPath = checkIsPresentAndGetOrThrow(optionalBundleAtPath, "Uninstall failed: could not find registered bundle in target file path=%s", modulePath);

        eventListener.moduleStopping(bundleAtPath.getBundleId());
        executeOperation(bundleAtPath, Bundle::stop, Bundle::uninstall);

        logger.debug("Module [{}] uninstalled", bundleAtPath.getSymbolicName());

        return bundleAtPath.getBundleId();
    }

    @Override
    public long install(String modulePath) {
        Optional<Bundle> optionalBundleAtPath = getModuleAtPath(modulePath);
        checkState(!optionalBundleAtPath.isPresent(), format("Install failed: the bundle in target file path=%s is already installed. Did you mean update?", modulePath));
        try {

            Bundle installedBundle = bundleContext.installBundle(modulePath);
            installedBundle.start();

            logger.debug("Module [{}] installed", installedBundle.getSymbolicName());

            return installedBundle.getBundleId();
        } catch (BundleException e) {
            throw new ESBException(e);
        }
    }

    @Override
    public long installOrUpdate(String modulePath) {
        Optional<Bundle> optionalBundleAtPath = getModuleAtPath(modulePath);
        if (optionalBundleAtPath.isPresent()) {
            return update(modulePath);
        } else {
            return install(modulePath);
        }
    }

    // TODO: ModulesGETRes should be a DTO to be used only in the REST.
    @Override
    public ModulesGETRes modules() {
        Set<ModuleGETRes> mappedModules = modulesManager.allModules()
                .stream()
                .map(mapper::map)
                .collect(toSet());

        ModulesGETRes modules = new ModulesGETRes();
        modules.setModules(mappedModules);
        return modules;
    }

    private Optional<Bundle> getModuleAtPath(String bundlePath) {
        return Optional.ofNullable(bundleContext.getBundle(bundlePath));
    }

    private interface Operation {
        void execute(Bundle bundle) throws BundleException;
    }

    private void executeOperation(Bundle bundle, Operation... operations) {
        stream(operations)
                .forEachOrdered(
                        uncheckedConsumer(operation -> operation.execute(bundle)));

    }
}
