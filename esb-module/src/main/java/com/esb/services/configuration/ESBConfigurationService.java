package com.esb.services.configuration;

import com.esb.api.service.ConfigurationService;
import com.esb.services.configuration.configurer.*;
import com.esb.system.api.SystemProperty;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.*;

import static java.util.Arrays.asList;
import static java.util.Arrays.stream;
import static java.util.Collections.emptyList;
import static java.util.Collections.list;
import static java.util.stream.Collectors.toList;

public class ESBConfigurationService implements ConfigurationService {

    private static final List<Configurer> CONFIGURERS = asList(
            new LogbackConfigurer(),
            new PidConfigConfigurer());

    private static final Logger logger = LoggerFactory.getLogger(ESBConfigurationService.class);

    private final ConfigurationAdmin configurationAdmin;
    private final SystemProperty systemProperty;

    public ESBConfigurationService(ConfigurationAdmin configurationAdmin, SystemProperty systemProperty) {
        this.configurationAdmin = configurationAdmin;
        this.systemProperty = systemProperty;
    }

    @Override
    public String getStringConfigProperty(String configPid, String configKey, String defaultValue) {
        return Optional
                .ofNullable(getStringSystemProperty(configKey))
                .orElseGet(() ->
                        getConfigAdminProperty(configPid, configKey, defaultValue, TO_STRING));
    }

    @Override
    public int getIntConfigProperty(String configPid, String configKey, int defaultValue) {
        return Optional
                .ofNullable(getIntSystemProperty(configKey))
                .orElseGet(() ->
                        getConfigAdminProperty(configPid, configKey, defaultValue, TO_INT));
    }

    /**
     * Applies a given Configurer object to each config file found in the config directory.
     * For a given file type (e.g .property) there might be multiple configurers.
     */
    public void initialize() {
        listConfigFilesFromConfigDirectory()
                .forEach(configFile -> {
                    for (Configurer configurer : CONFIGURERS) {
                        // We break loop if configuration has been applied.
                        if (configurer.apply(configurationAdmin, configFile)) break;
                    }
                });
    }

    Integer getIntSystemProperty(String key) {
        return getStringSystemProperty(key) == null ?
                null :
                Integer.valueOf(getStringSystemProperty(key));
    }

    String getStringSystemProperty(String key) {
        return System.getProperty(key);
    }

    Collection<ConfigFile> listConfigFilesFromConfigDirectory() {
        File configDirectory = new File(systemProperty.configDirectory());
        File[] files = configDirectory.listFiles(File::isFile);
        if (files == null) return emptyList();
        return stream(files)
                .map(ConfigFileFactory::get)
                .filter(Objects::nonNull)
                .collect(toList());
    }

    <T> T getConfigAdminProperty(String configPid, String configKey, T defaultValue, DataMapper<T> mapper) {
        try {
            Configuration configuration = configurationAdmin.getConfiguration(configPid);
            Dictionary<String, Object> properties = configuration.getProperties();
            if (properties == null) return defaultValue;
            return getPropertyOrDefault(properties, configKey, defaultValue, mapper);
        } catch (IOException e) {
            logger.warn("Could not find config property with key={} for config pid={}, using defaultValue={}", configKey, configPid, defaultValue);
            return defaultValue;
        }
    }

    static final DataMapper<String> TO_STRING = input -> (String) input;

    static final DataMapper<Integer> TO_INT = input -> input instanceof String ? Integer.valueOf((String) input) : (Integer) input;

    private interface DataMapper<O> {
        O map(Object input);

    }

    private <T> T getPropertyOrDefault(Dictionary<String, Object> dictionary, String configKey, T defaultValue, DataMapper<T> mapper) {
        boolean isKeyPresent = list(dictionary.keys())
                .stream()
                .anyMatch(configKey::equals);
        if (isKeyPresent) {
            return mapper.map(dictionary.get(configKey));
        } else {
            return defaultValue;
        }
    }

}
