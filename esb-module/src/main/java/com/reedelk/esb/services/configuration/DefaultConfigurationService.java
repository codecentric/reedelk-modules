package com.reedelk.esb.services.configuration;

import com.reedelk.esb.services.configuration.configurer.*;
import com.reedelk.runtime.api.exception.InvalidConfigPropertyException;
import com.reedelk.runtime.api.service.ConfigurationService;
import com.reedelk.runtime.system.api.SystemProperty;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.function.Function;

import static java.util.Arrays.asList;
import static java.util.Arrays.stream;
import static java.util.Collections.emptyList;
import static java.util.Collections.list;
import static java.util.stream.Collectors.toList;

public class DefaultConfigurationService implements ConfigurationService {

    private static final List<Configurer> CONFIGURERS = asList(new LogbackConfigurer(), new PidConfigConfigurer());

    private static final Logger logger = LoggerFactory.getLogger(DefaultConfigurationService.class);

    private final ConfigurationAdmin configurationAdmin;
    private final SystemProperty systemProperty;

    public DefaultConfigurationService(ConfigurationAdmin configurationAdmin, SystemProperty systemProperty) {
        this.configurationAdmin = configurationAdmin;
        this.systemProperty = systemProperty;
    }

    @Override
    public String getString(String configPid, String configKey, String defaultValue) {
        return Optional.ofNullable(getStringSystemProperty(configKey))
                .orElseGet(() -> getConfigAdminProperty(configPid, configKey, defaultValue, TO_STRING));
    }

    @Override
    public String getString(String configPid, String configKey) {
        return Optional.ofNullable(getStringSystemProperty(configKey))
                .orElseGet(() -> getConfigAdminPropertyOrThrow(configPid, configKey, TO_STRING));
    }

    @Override
    public int getInt(String configPid, String configKey, int defaultValue) {
        return Optional.ofNullable(getIntSystemProperty(configKey))
                .orElseGet(() -> getConfigAdminProperty(configPid, configKey, defaultValue, TO_INT));
    }

    @Override
    public int getInt(String configPid, String configKey) {
        return Optional.ofNullable(getIntSystemProperty(configKey))
                .orElseGet(() -> getConfigAdminPropertyOrThrow(configPid, configKey, TO_INT));
    }

    @Override
    public long getLong(String configPid, String configKey, long defaultValue) {
        return Optional.ofNullable(getLongSystemProperty(configKey))
                .orElseGet(() -> getConfigAdminProperty(configPid, configKey, defaultValue, TO_LONG));
    }

    @Override
    public long getLong(String configPid, String configKey) {
        return Optional.ofNullable(getLongSystemProperty(configKey))
                .orElseGet(() -> getConfigAdminPropertyOrThrow(configPid, configKey, TO_LONG));
    }

    @Override
    public boolean getBoolean(String configPid, String configKey, boolean defaultValue) {
        return Optional.ofNullable(getBooleanSystemProperty(configKey))
                .orElseGet(() -> getConfigAdminProperty(configPid, configKey, defaultValue, TO_BOOLEAN));

    }

    @Override
    public boolean getBoolean(String configPid, String configKey) {
        return Optional.ofNullable(getBooleanSystemProperty(configKey))
                .orElseGet(() -> getConfigAdminPropertyOrThrow(configPid, configKey, TO_BOOLEAN));
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T get(String configPid, String configKey, Class<T> type) {
        return (T) MAP.get(type).convert(this, configPid, configKey);
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
                null : Integer.valueOf(getStringSystemProperty(key));
    }

    Long getLongSystemProperty(String key) {
        return getStringSystemProperty(key) == null ?
                null : Long.valueOf(getStringSystemProperty(key));
    }

    Boolean getBooleanSystemProperty(String key) {
        return getStringSystemProperty(key) == null ?
                null : Boolean.valueOf(getStringSystemProperty(key));
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

    <T> T getConfigAdminProperty(String configPid, String configKey, T defaultValue, Function<Object, T> mapper) {
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

    private <T> T getConfigAdminPropertyOrThrow(String configPid, String configKey, Function<Object, T> mapper) {
        try {
            Configuration configuration = configurationAdmin.getConfiguration(configPid);
            Dictionary<String, Object> properties = configuration.getProperties();
            return getPropertyOrThrow(properties, configKey, mapper);
        } catch (IOException e) {
            throw new InvalidConfigPropertyException(String.format("Could not find config property with key='%s' for config pid='%s'", configKey, configPid));
        }
    }

    private <T> T getPropertyOrThrow(Dictionary<String, Object> dictionary, String configKey, Function<Object, T> mapper) {
        if (dictionary != null && list(dictionary.keys()).contains(configKey)) {
            return mapper.apply(dictionary.get(configKey));
        } else {
            throw new InvalidConfigPropertyException(String.format("Could not find config property with key='%s'.", configKey));
        }
    }

    private <T> T getPropertyOrDefault(Dictionary<String, Object> dictionary, String configKey, T defaultValue, Function<Object, T> mapper) {
        if(list(dictionary.keys()).contains(configKey)) {
            return mapper.apply(dictionary.get(configKey));
        } else {
            return defaultValue;
        }
    }

    private static final Map<Class, ConfigConverter> MAP;
    static {
        Map<Class, ConfigConverter> tmp = new HashMap<>();
        tmp.put(String.class, (ConfigConverter<String>) ConfigurationService::getString);
        tmp.put(int.class, (ConfigConverter<Integer>) ConfigurationService::getInt);
        tmp.put(Integer.class, (ConfigConverter<Integer>) ConfigurationService::getInt);
        tmp.put(boolean.class, (ConfigConverter<Boolean>) ConfigurationService::getBoolean);
        tmp.put(Boolean.class, (ConfigConverter<Boolean>) ConfigurationService::getBoolean);
        tmp.put(long.class, (ConfigConverter<Long>) ConfigurationService::getLong);
        tmp.put(Long.class, (ConfigConverter<Long>) ConfigurationService::getLong);
        MAP = tmp;
    }

    interface ConfigConverter<T> {
        T convert(ConfigurationService configurationService, String pid, String key);
    }

    private static final Function<Object, String> TO_STRING = input -> (String) input;
    private static final Function<Object, Long> TO_LONG = input -> input instanceof String ? Long.valueOf((String) input) : (Long) input;
    private static final Function<Object, Integer> TO_INT = input -> input instanceof String ? Integer.valueOf((String) input) : (Integer) input;
    private static final Function<Object, Boolean> TO_BOOLEAN = input -> input instanceof String ? Boolean.valueOf((String) input) : (Boolean) input;
}
