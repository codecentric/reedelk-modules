package com.esb.services.configuration;

import com.esb.internal.api.SystemProperty;
import com.esb.services.configuration.configurer.ConfigFile;
import com.esb.services.configuration.configurer.PropertiesConfigFile;
import com.esb.services.configuration.configurer.XmlConfigFile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;

import java.io.IOException;
import java.util.*;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ConfigurationServiceTest {

    private static final String TEST_CONFIG_PID = "my.test.config.pid";
    private static final String TEST_CONFIG_KEY = "name.endpoint";

    @Mock
    private SystemProperty systemProperty;
    @Mock
    private ConfigurationAdmin mockConfigurationAdmin;
    @Captor
    private ArgumentCaptor<Dictionary<String, Object>> dictionaryCaptor;

    private ESBConfigurationService service;

    @BeforeEach
    void setUp() {
        service = spy(new ESBConfigurationService(mockConfigurationAdmin, systemProperty));
    }

    // String property
    @Test
    void shouldReturnSystemStringConfigProperty() {
        // Given
        String expectedValue = "testValue";

        doReturn(expectedValue)
                .when(service)
                .getStringSystemProperty(TEST_CONFIG_KEY);

        // When
        String actualConfigProperty = service.getStringConfigProperty(TEST_CONFIG_PID, TEST_CONFIG_KEY, "DefaultValue");

        // Then
        assertThat(actualConfigProperty).isEqualTo(expectedValue);
    }

    @Test
    void shouldReturnStringConfigPropertyFromConfigurationAdminService() throws IOException {
        // Given
        String expectedValue = "testValue";

        doReturn(null)
                .when(service)
                .getStringSystemProperty(TEST_CONFIG_KEY);

        Dictionary<String, Object> properties = new Hashtable<>();
        properties.put(TEST_CONFIG_KEY, expectedValue);
        mockConfigurationWithProperties(TEST_CONFIG_PID, properties);

        // When
        String actualConfigProperty = service.getStringConfigProperty(TEST_CONFIG_PID, TEST_CONFIG_KEY, "DefaultValue");

        // Then
        assertThat(actualConfigProperty).isEqualTo(expectedValue);
    }

    @Test
    void shouldReturnDefaultStringConfigProperty() throws IOException {
        // Given
        doReturn(null)
                .when(service)
                .getStringSystemProperty(TEST_CONFIG_KEY);
        mockConfigurationWithProperties(TEST_CONFIG_PID, null);

        // When
        String actualConfigProperty = service.getStringConfigProperty(TEST_CONFIG_PID, TEST_CONFIG_KEY, "MyDefaultValue");

        // Then
        assertThat(actualConfigProperty).isEqualTo("MyDefaultValue");
    }

    // Integer property
    @Test
    void shouldReturnSystemIntConfigProperty() {
        // Given
        int expectedValue = 23434;

        doReturn(expectedValue)
                .when(service)
                .getIntSystemProperty(TEST_CONFIG_KEY);

        // When
        int actualConfigProperty = service.getIntConfigProperty(TEST_CONFIG_PID, TEST_CONFIG_KEY, 7777);

        // Then
        assertThat(actualConfigProperty).isEqualTo(expectedValue);
    }

    @Test
    void shouldReturnIntConfigPropertyFromConfigurationAdminServiceWhenIntAlready() throws IOException {
        // Given
        int expectedValue = 111333;

        doReturn(null)
                .when(service)
                .getStringSystemProperty(TEST_CONFIG_KEY);

        Dictionary<String, Object> properties = new Hashtable<>();
        properties.put(TEST_CONFIG_KEY, expectedValue);
        mockConfigurationWithProperties(TEST_CONFIG_PID, properties);

        // When
        int actualConfigProperty = service.getIntConfigProperty(TEST_CONFIG_PID, TEST_CONFIG_KEY, 888);

        // Then
        assertThat(actualConfigProperty).isEqualTo(expectedValue);
    }

    @Test
    void shouldReturnIntConfigPropertyFromConfigurationAdminServiceWhenString() throws IOException {
        // Given
        int expectedValue = 111333;

        doReturn(null)
                .when(service)
                .getStringSystemProperty(TEST_CONFIG_KEY);

        Dictionary<String, Object> properties = new Hashtable<>();
        properties.put(TEST_CONFIG_KEY, Integer.toString(expectedValue));
        mockConfigurationWithProperties(TEST_CONFIG_PID, properties);

        // When
        int actualConfigProperty = service.getIntConfigProperty(TEST_CONFIG_PID, TEST_CONFIG_KEY, 11109);

        // Then
        assertThat(actualConfigProperty).isEqualTo(expectedValue);
    }

    @Test
    void shouldReturnDefaultIntConfigProperty() throws IOException {
        // Given
        doReturn(null)
                .when(service)
                .getStringSystemProperty(TEST_CONFIG_KEY);
        mockConfigurationWithProperties(TEST_CONFIG_PID, null);

        // When
        int actualConfigProperty = service.getIntConfigProperty(TEST_CONFIG_PID, TEST_CONFIG_KEY, 653333);

        // Then
        assertThat(actualConfigProperty).isEqualTo(653333);
    }

    @Test
    void getConfigAdminPropertyShouldReturnDefaultValueWhenPropertyNotDefinedInDictionary() throws IOException {
        // Given
        Dictionary<String, Object> properties = new Hashtable<>();
        mockConfigurationWithProperties(TEST_CONFIG_PID, properties);

        // When
        String actualProperty = service.getConfigAdminProperty(TEST_CONFIG_PID, TEST_CONFIG_KEY, "Default", ESBConfigurationService.TO_STRING);

        // Then
        assertThat(actualProperty).isEqualTo("Default");
    }

    @Test
    void getConfigAdminPropertyShouldReturnEmptyWhenPropertyDefinedInDictionaryAndIsempty() throws IOException {
        // Given
        Dictionary<String, Object> properties = new Hashtable<>();
        properties.put(TEST_CONFIG_KEY, "");
        mockConfigurationWithProperties(TEST_CONFIG_PID, properties);

        // When
        String actualProperty = service.getConfigAdminProperty(TEST_CONFIG_PID, TEST_CONFIG_KEY, "Default", ESBConfigurationService.TO_STRING);

        // Then
        assertThat(actualProperty).isEmpty();
    }

    @Test
    void shouldInitializeApplyLogbackConfigurerCorrectly() throws IOException {
        // Given
        String expectedValue = "my/logback/file/path/logback.xml";

        Configuration mockConfiguration = mock(Configuration.class);
        doReturn(mockConfiguration)
                .when(mockConfigurationAdmin)
                .getConfiguration("org.ops4j.pax.logging", "?");

        XmlConfigFile mockLogbackConfigFile = mock(XmlConfigFile.class);
        doReturn(expectedValue)
                .when(mockLogbackConfigFile)
                .getFilePath();
        doReturn("logback.xml")
                .when(mockLogbackConfigFile)
                .getFileName();

        Collection<ConfigFile> mockConfigFiles = Collections.singletonList(mockLogbackConfigFile);
        doReturn(mockConfigFiles)
                .when(service)
                .listConfigFilesFromConfigDirectory();

        // When
        service.initialize();

        // Then
        verify(mockConfiguration).update(dictionaryCaptor.capture());

        Dictionary<String, Object> updatedDictionary = dictionaryCaptor.getValue();
        String actualValue = (String) updatedDictionary.get("org.ops4j.pax.logging.logback.config.file");

        assertThat(actualValue).isEqualTo(expectedValue);
    }

    @Test
    void shouldInitializeApplyConfigPropertiesFileCorrectly() throws IOException {
        // Given
        String pid1 = "com.esb.custom.components1";
        String pid2 = "com.esb.custom.components2";

        PropertiesConfigFile propertiesConfigFile1 = mock(PropertiesConfigFile.class);
        Properties properties1 = singleKeyValueProperty("key1", "value1");
        doReturn(pid1 + ".properties").when(propertiesConfigFile1).getFileName();
        doReturn(properties1).when(propertiesConfigFile1).getContent();

        PropertiesConfigFile propertiesConfigFile2 = mock(PropertiesConfigFile.class);
        Properties properties2 = singleKeyValueProperty("key2", "value2");
        doReturn(pid2 + ".properties").when(propertiesConfigFile2).getFileName();
        doReturn(properties2).when(propertiesConfigFile2).getContent();

        Collection<ConfigFile> mockConfigFiles = asList(propertiesConfigFile1, propertiesConfigFile2);
        doReturn(mockConfigFiles)
                .when(service)
                .listConfigFilesFromConfigDirectory();

        Configuration mockConfiguration1 = mock(Configuration.class);
        doReturn(mockConfiguration1)
                .when(mockConfigurationAdmin)
                .getConfiguration(pid1, "?");

        Configuration mockConfiguration2 = mock(Configuration.class);
        doReturn(mockConfiguration2)
                .when(mockConfigurationAdmin)
                .getConfiguration(pid2, "?");

        // When
        service.initialize();

        // Then
        verify(mockConfiguration1).update(dictionaryCaptor.capture());
        Dictionary<String, Object> updatedDictionary1 = dictionaryCaptor.getValue();
        assertThat(updatedDictionary1).isEqualTo(properties1);

        verify(mockConfiguration2).update(dictionaryCaptor.capture());
        Dictionary<String, Object> updatedDictionary2 = dictionaryCaptor.getValue();
        assertThat(updatedDictionary2).isEqualTo(properties2);
    }

    private Properties singleKeyValueProperty(String key, String value) {
        Properties properties = new Properties();
        properties.put(key, value);
        return properties;
    }

    private void mockConfigurationWithProperties(String pid, Dictionary<String, Object> properties) throws IOException {
        Configuration mockConfiguration = mock(Configuration.class);
        doReturn(properties)
                .when(mockConfiguration)
                .getProperties();
        doReturn(mockConfiguration)
                .when(mockConfigurationAdmin)
                .getConfiguration(pid);
    }
}