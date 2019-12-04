package com.reedelk.esb.services.module;

import com.reedelk.esb.module.ModulesManager;
import com.reedelk.runtime.api.exception.ESBException;
import com.reedelk.runtime.system.api.ModuleDto;
import com.reedelk.runtime.system.api.ModuleService;
import com.reedelk.runtime.system.api.ModulesDto;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.Set;

import static com.reedelk.esb.commons.FunctionWrapper.uncheckedConsumer;
import static com.reedelk.esb.commons.Messages.Module.*;
import static com.reedelk.esb.commons.Preconditions.*;
import static java.lang.String.format;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toSet;

public class DefaultModuleService implements ModuleService {

    private static final Logger logger = LoggerFactory.getLogger(DefaultModuleService.class);

    private final ModulesMapper mapper = new ModulesMapper();

    private final EventListener listener;
    private final BundleContext context;
    private final ModulesManager modulesManager;

    public DefaultModuleService(BundleContext context, ModulesManager modulesManager, EventListener listener) {
        this.modulesManager = modulesManager;
        this.listener = listener;
        this.context = context;
    }

    @Override
    public long update(String modulePath) {
        Optional<Bundle> optionalBundle = getModuleAtPath(modulePath);
        Bundle bundleAtPath = checkIsPresentAndGetOrThrow(optionalBundle, "Update failed: could not find registered bundle in target file path=%s", modulePath);

        if (Bundle.INSTALLED == bundleAtPath.getState()) {
            // It is installed but not started (we don't have to call listener's moduleStopping event)
            executeOperation(bundleAtPath, Bundle::update, Bundle::start);
        } else {
            // It is installed and started (we must stop it, update it and start it again)
            listener.moduleStopping(bundleAtPath.getBundleId());
            executeOperation(bundleAtPath, Bundle::stop, Bundle::update, Bundle::start);
        }

        if (logger.isInfoEnabled()) {
            logger.info(UPDATED.format(bundleAtPath.getSymbolicName()));
        }

        return bundleAtPath.getBundleId();
    }

    @Override
    public long uninstall(String modulePath) {
        return getModuleAtPath(modulePath).map(bundleAtPath -> {
            listener.moduleStopping(bundleAtPath.getBundleId());
            executeOperation(bundleAtPath, Bundle::stop, Bundle::uninstall);
            if (logger.isInfoEnabled()) {
                logger.info(UNINSTALLED.format(bundleAtPath.getSymbolicName()));
            }
            return bundleAtPath.getBundleId();
        }).orElse(-1L);
    }

    @Override
    public long install(String modulePath) {
        Optional<Bundle> optionalBundle = getModuleAtPath(modulePath);
        checkState(!optionalBundle.isPresent(), format("Install failed: the bundle in target file path=%s is already installed. Did you mean update?", modulePath));
        try {
            Bundle installedBundle = context.installBundle(modulePath);

            if (logger.isInfoEnabled()) {
                logger.info(INSTALLED.format(installedBundle.getSymbolicName()));
            }

            return start(installedBundle);
        } catch (BundleException e) {
            String errorMessage = INSTALL_FAILED.format(modulePath);
            throw new ESBException(errorMessage, e);
        }
    }

    @Override
    public long installOrUpdate(String modulePath) {
        Optional<Bundle> optionalBundle = getModuleAtPath(modulePath);
        if (optionalBundle.isPresent()) {
            return update(modulePath);
        } else {
            return install(modulePath);
        }
    }

    @Override
    public ModulesDto modules() {
        Set<ModuleDto> mappedModuleDtos = modulesManager.allModules()
                .stream()
                .map(mapper::map)
                .collect(toSet());
        ModulesDto modulesDto = new ModulesDto();
        modulesDto.setModuleDtos(mappedModuleDtos);
        return modulesDto;
    }

    private long start(Bundle installedBundle) {
        try {
            checkNotNull(installedBundle, "installedBundle");
            installedBundle.start();

            if (logger.isInfoEnabled()) {
                logger.info(STARTED.format(installedBundle.getSymbolicName()));
            }

            return installedBundle.getBundleId();
        } catch (BundleException e) {
            String errorMessage = START_FAILED.format(installedBundle.getSymbolicName());
            throw new ESBException(errorMessage, e);
        }
    }

    private Optional<Bundle> getModuleAtPath(String bundlePath) {
        return Optional.ofNullable(context.getBundle(bundlePath));
    }

    private interface Operation {
        void execute(Bundle bundle) throws BundleException;
    }

    private void executeOperation(Bundle bundle, Operation... operations) {
        stream(operations).forEachOrdered(
                uncheckedConsumer(operation -> operation.execute(bundle)));

    }
}
