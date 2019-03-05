package com.esb.services.configuration.configurer;

import org.osgi.service.cm.ConfigurationAdmin;

import java.util.Properties;

public class LogbackConfigurer extends AbstractConfigurer {

    private static final String LOGBACK_CONFIG_FILE_NAME = "logback.xml";

    private static final String PAX_LOGGING_CONFIG_PID = "org.ops4j.pax.logging";
    private static final String LOGBACK_CONFIG_FILE_PROPERTY_NAME = "org.ops4j.pax.logging.logback.config.file";

    @Override
    public boolean apply(ConfigurationAdmin configService, ConfigFile configFile) {
        if (!matches(configFile, LOGBACK_CONFIG_FILE_NAME)) return false;

        XmlConfigFile logback = (XmlConfigFile) configFile;
        Properties properties = new Properties();
        properties.put(LOGBACK_CONFIG_FILE_PROPERTY_NAME, logback.getFilePath());
        return updateConfigurationForPid(PAX_LOGGING_CONFIG_PID, configService, properties);
    }

    private boolean matches(ConfigFile configFile, String match) {
        return match.equalsIgnoreCase(configFile.getFileName());
    }

}
