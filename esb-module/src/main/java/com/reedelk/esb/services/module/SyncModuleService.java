package com.reedelk.esb.services.module;

import com.reedelk.runtime.api.exception.ESBException;
import com.reedelk.runtime.commons.ModuleUtils;
import com.reedelk.runtime.system.api.ModuleService;
import com.reedelk.runtime.system.api.SystemProperty;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URI;
import java.util.Optional;

import static com.reedelk.esb.commons.Messages.Module.*;
import static java.util.Arrays.stream;

class SyncModuleService {

    private static final Logger logger = LoggerFactory.getLogger(SyncModuleService.class);

    private final BundleContext context;
    private final ModuleService moduleService;
    private final SystemProperty systemProperty;

    SyncModuleService(ModuleService moduleService, SystemProperty systemProperty, BundleContext context) {
        this.moduleService = moduleService;
        this.systemProperty = systemProperty;
        this.context = context;
    }

    void unInstallIfModuleExistsAlready(String moduleJarPath) {
        String filePath = URI.create(moduleJarPath).getPath();
        String toBeInstalledModuleName = ModuleUtils.getModuleName(filePath).orElseThrow(() -> {
            String errorMessage = INSTALL_FAILED_MODULE_NAME_NOT_FOUND.format(moduleJarPath);
            return new ESBException(errorMessage);
        });

        findInstalledBundleMatchingModuleName(toBeInstalledModuleName).ifPresent(installedBundle -> {

            if (logger.isInfoEnabled()) {
                String message = INSTALL_MODULE_DIFFERENT_VERSION_PRESENT.format(
                        toBeInstalledModuleName,
                        installedBundle.getVersion(),
                        ModuleUtils.getModuleVersion(filePath).orElse("UNKNOWN"));
                logger.info(message);
            }

            // Exists a module with the same module name of the module we want to install.
            // We must uninstall the currently installed bundle. If the bundle belongs to the
            // runtime's modules directory, we also must remove it so that it will not be installed
            // again the next time the system restarts.
            String toBeUninstalled = installedBundle.getLocation();
            moduleService.uninstall(toBeUninstalled);

            // The 'toBeUninstalled' location is a URI, but we need the file path.
            URI uri = URI.create(toBeUninstalled);
            String toBeUninstalledFilePath = uri.getPath();

            // We remove the file if and only if it belongs to the modules directory.
            if (toBeUninstalledFilePath.startsWith(systemProperty.modulesDirectory())) {
                boolean delete = new File(uri.getPath()).delete();
                if (delete && logger.isInfoEnabled()) {
                    String message = REMOVED_FROM_MODULES_DIRECTORY.format(
                            toBeInstalledModuleName,
                            installedBundle.getVersion());
                    logger.info(message);
                }
            }
        });
    }

    private Optional<Bundle> findInstalledBundleMatchingModuleName(String targetModuleName) {
        return stream(context.getBundles())
                .filter(installedBundle -> installedBundle.getSymbolicName().equals(targetModuleName))
                .findFirst();
    }
}
