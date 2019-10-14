package com.reedelk.esb.services.configuration;

import com.reedelk.esb.services.configuration.configurer.ConfigFile;
import com.reedelk.esb.services.configuration.configurer.PropertiesConfigFile;
import com.reedelk.esb.services.configuration.configurer.XmlConfigFile;
import com.reedelk.runtime.api.exception.InvalidConfigPropertyException;
import com.reedelk.runtime.system.api.SystemProperty;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
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
import java.math.BigDecimal;
import java.util.*;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class DefaultConfigurationServiceTest {

    private final String TEST_CONFIG_PID = "my.test.config.pid";
    private final String TEST_CONFIG_KEY = "name.endpoint";

    @Mock
    private SystemProperty systemProperty;
    @Mock
    private ConfigurationAdmin mockConfigurationAdmin;
    @Captor
    private ArgumentCaptor<Dictionary<String, Object>> dictionaryCaptor;

    private DefaultConfigurationService service;

    @BeforeEach
    void setUp() {
        service = spy(new DefaultConfigurationService(mockConfigurationAdmin, systemProperty));
    }

    @Nested
    @DisplayName("String property")
    class StringProperty {

        @Test
        void shouldReturnSystemStringConfigProperty() {
            // Given
            String expectedValue = "testValue";

            doReturn(expectedValue)
                    .when(service)
                    .getStringSystemProperty(TEST_CONFIG_KEY);

            // When
            String actualConfigProperty = service.getString(TEST_CONFIG_PID, TEST_CONFIG_KEY, "DefaultValue");

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
            String actualConfigProperty = service.getString(TEST_CONFIG_PID, TEST_CONFIG_KEY, "DefaultValue");

            // Then
            assertThat(actualConfigProperty).isEqualTo(expectedValue);
        }

        @Test
        void shouldReturnDefaultStringConfigPropertyWhenDictionaryIsNull() throws IOException {
            // Given
            doReturn(null)
                    .when(service)
                    .getStringSystemProperty(TEST_CONFIG_KEY);
            mockConfigurationWithProperties(TEST_CONFIG_PID, null);

            // When
            String actualConfigProperty = service.getString(TEST_CONFIG_PID, TEST_CONFIG_KEY, "MyDefaultValue");

            // Then
            assertThat(actualConfigProperty).isEqualTo("MyDefaultValue");
        }

        @Test
        void shouldReturnDefaultStringConfigPropertyWhenDictionaryDoesNotContainKey() throws IOException {
            // Given
            Dictionary<String, Object> dictionaryNotContainingTargetKey = new Hashtable<>();
            dictionaryNotContainingTargetKey.put("anotherKey", "aString");

            doReturn(null)
                    .when(service)
                    .getStringSystemProperty(TEST_CONFIG_KEY);
            mockConfigurationWithProperties(TEST_CONFIG_PID, dictionaryNotContainingTargetKey);

            // When
            String actualConfigProperty = service.getString(TEST_CONFIG_PID, TEST_CONFIG_KEY, "MyDefaultValue");

            // Then
            assertThat(actualConfigProperty).isEqualTo("MyDefaultValue");
        }

    }

    @Nested
    @DisplayName("Integer property")
    class IntegerProperty {

        @Test
        void shouldReturnSystemIntConfigProperty() {
            // Given
            int expectedValue = 23434;

            doReturn(expectedValue)
                    .when(service)
                    .getIntSystemProperty(TEST_CONFIG_KEY);

            // When
            int actualConfigProperty = service.getInt(TEST_CONFIG_PID, TEST_CONFIG_KEY, 7777);

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
            int actualConfigProperty = service.getInt(TEST_CONFIG_PID, TEST_CONFIG_KEY, 888);

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
            int actualConfigProperty = service.getInt(TEST_CONFIG_PID, TEST_CONFIG_KEY, 11109);

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
            int actualConfigProperty = service.getInt(TEST_CONFIG_PID, TEST_CONFIG_KEY, 653333);

            // Then
            assertThat(actualConfigProperty).isEqualTo(653333);
        }
    }

    @Nested
    @DisplayName("Long property")
    class LongProperty {

        @Test
        void shouldReturnSystemLongConfigProperty() {
            // Given
            long expectedValue = 54L;

            doReturn(expectedValue)
                    .when(service)
                    .getLongSystemProperty(TEST_CONFIG_KEY);

            // When
            long actualConfigProperty = service.getLong(TEST_CONFIG_PID, TEST_CONFIG_KEY, 11L);

            // Then
            assertThat(actualConfigProperty).isEqualTo(expectedValue);
        }

        @Test
        void shouldReturnLongConfigPropertyFromConfigurationAdminServiceWhenLongAlready() throws IOException {
            // Given
            long expectedValue = 658L;

            doReturn(null)
                    .when(service)
                    .getStringSystemProperty(TEST_CONFIG_KEY);

            Dictionary<String, Object> properties = new Hashtable<>();
            properties.put(TEST_CONFIG_KEY, expectedValue);
            mockConfigurationWithProperties(TEST_CONFIG_PID, properties);

            // When
            long actualConfigProperty = service.getLong(TEST_CONFIG_PID, TEST_CONFIG_KEY, 88L);

            // Then
            assertThat(actualConfigProperty).isEqualTo(expectedValue);
        }

        @Test
        void shouldReturnLongConfigPropertyFromConfigurationAdminServiceWhenString() throws IOException {
            // Given
            long expectedValue = Long.MAX_VALUE;

            doReturn(null)
                    .when(service)
                    .getStringSystemProperty(TEST_CONFIG_KEY);

            Dictionary<String, Object> properties = new Hashtable<>();
            properties.put(TEST_CONFIG_KEY, Long.toString(expectedValue));
            mockConfigurationWithProperties(TEST_CONFIG_PID, properties);

            // When
            long actualConfigProperty = service.getLong(TEST_CONFIG_PID, TEST_CONFIG_KEY, 11109L);

            // Then
            assertThat(actualConfigProperty).isEqualTo(expectedValue);
        }

        @Test
        void shouldReturnDefaultLongConfigProperty() throws IOException {
            // Given
            doReturn(null)
                    .when(service)
                    .getStringSystemProperty(TEST_CONFIG_KEY);
            mockConfigurationWithProperties(TEST_CONFIG_PID, null);

            // When
            long actualConfigProperty = service.getLong(TEST_CONFIG_PID, TEST_CONFIG_KEY, Long.MIN_VALUE);

            // Then
            assertThat(actualConfigProperty).isEqualTo(Long.MIN_VALUE);
        }
    }

    @Nested
    @DisplayName("Boolean property")
    class BooleanProperty {

        @Test
        void shouldReturnSystemBooleanConfigProperty() {
            // Given
            boolean expectedValue = true;

            doReturn(expectedValue)
                    .when(service)
                    .getBooleanSystemProperty(TEST_CONFIG_KEY);

            // When
            boolean actualConfigProperty = service.getBoolean(TEST_CONFIG_PID, TEST_CONFIG_KEY, false);

            // Then
            assertThat(actualConfigProperty).isEqualTo(expectedValue);
        }

        @Test
        void shouldReturnBooleanConfigPropertyFromConfigurationAdminServiceWhenBooleanAlready() throws IOException {
            // Given
            boolean expectedValue = true;

            doReturn(null)
                    .when(service)
                    .getStringSystemProperty(TEST_CONFIG_KEY);

            Dictionary<String, Object> properties = new Hashtable<>();
            properties.put(TEST_CONFIG_KEY, expectedValue);
            mockConfigurationWithProperties(TEST_CONFIG_PID, properties);

            // When
            boolean actualConfigProperty = service.getBoolean(TEST_CONFIG_PID, TEST_CONFIG_KEY, false);

            // Then
            assertThat(actualConfigProperty).isEqualTo(expectedValue);
        }

        @Test
        void shouldReturnBooleanConfigPropertyFromConfigurationAdminServiceWhenString() throws IOException {
            // Given
            boolean expectedValue = true;

            doReturn(null)
                    .when(service)
                    .getStringSystemProperty(TEST_CONFIG_KEY);

            Dictionary<String, Object> properties = new Hashtable<>();
            properties.put(TEST_CONFIG_KEY, Boolean.toString(expectedValue));
            mockConfigurationWithProperties(TEST_CONFIG_PID, properties);

            // When
            boolean actualConfigProperty = service.getBoolean(TEST_CONFIG_PID, TEST_CONFIG_KEY, false);

            // Then
            assertThat(actualConfigProperty).isEqualTo(expectedValue);
        }

        @Test
        void shouldReturnDefaultBooleanConfigProperty() throws IOException {
            // Given
            doReturn(null)
                    .when(service)
                    .getStringSystemProperty(TEST_CONFIG_KEY);
            mockConfigurationWithProperties(TEST_CONFIG_PID, null);

            // When
            boolean actualConfigProperty = service.getBoolean(TEST_CONFIG_PID, TEST_CONFIG_KEY, true);

            // Then
            assertThat(actualConfigProperty).isEqualTo(true);
        }

    }

    @Nested
    @DisplayName("Get property by class type")
    class GetPropertyByClassType {

        @Test
        void shouldReturnStringProperty() throws IOException {
            // Given
            String expectedValue = "Test property";

            mockConfigurationWithTestPropertyValue(expectedValue);

            // When
            String actualConfigProperty = service.get(TEST_CONFIG_PID, TEST_CONFIG_KEY, String.class);

            // Then
            assertThat(actualConfigProperty).isEqualTo(expectedValue);
        }

        @Test
        void shouldReturnIntProperty() throws IOException {
            // Given
            int expectedValue = 34;

            mockConfigurationWithTestPropertyValue(expectedValue);

            // When
            int actualConfigProperty = service.get(TEST_CONFIG_PID, TEST_CONFIG_KEY, int.class);

            // Then
            assertThat(actualConfigProperty).isEqualTo(expectedValue);
        }

        @Test
        void shouldReturnIntObjectProperty() throws IOException {
            // Given
            Integer expectedValue = 653;

            mockConfigurationWithTestPropertyValue(expectedValue);

            // When
            Integer actualConfigProperty = service.get(TEST_CONFIG_PID, TEST_CONFIG_KEY, Integer.class);

            // Then
            assertThat(actualConfigProperty).isEqualTo(expectedValue);
        }

        @Test
        void shouldReturnLongProperty() throws IOException {
            // Given
            long expectedValue = 543L;

            mockConfigurationWithTestPropertyValue(expectedValue);

            // When
            long actualConfigProperty = service.get(TEST_CONFIG_PID, TEST_CONFIG_KEY, long.class);

            // Then
            assertThat(actualConfigProperty).isEqualTo(expectedValue);
        }

        @Test
        void shouldReturnLongObjectProperty() throws IOException {
            // Given
            Long expectedValue = 234L;

            mockConfigurationWithTestPropertyValue(expectedValue);

            // When
            Long actualConfigProperty = service.get(TEST_CONFIG_PID, TEST_CONFIG_KEY, Long.class);

            // Then
            assertThat(actualConfigProperty).isEqualTo(expectedValue);
        }

        @Test
        void shouldReturnBooleanProperty() throws IOException {
            // Given
            boolean expectedValue = true;

            mockConfigurationWithTestPropertyValue(expectedValue);

            // When
            boolean actualConfigProperty = service.get(TEST_CONFIG_PID, TEST_CONFIG_KEY, boolean.class);

            // Then
            assertThat(actualConfigProperty).isEqualTo(expectedValue);
        }

        @Test
        void shouldReturnBooleanObjectProperty() throws IOException {
            // Given
            Boolean expectedValue = true;

            mockConfigurationWithTestPropertyValue(expectedValue);

            // When
            Boolean actualConfigProperty = service.get(TEST_CONFIG_PID, TEST_CONFIG_KEY, Boolean.class);

            // Then
            assertThat(actualConfigProperty).isEqualTo(expectedValue);
        }

        @Test
        void shouldThrowExceptionWhenPropertyWithGivenKeyDoesNotExist() throws IOException {
            // Given
            doReturn(null)
                    .when(service)
                    .getStringSystemProperty(TEST_CONFIG_KEY);

            Dictionary<String, Object> properties = new Hashtable<>();
            mockConfigurationWithProperties(TEST_CONFIG_PID, properties);

            // When
            InvalidConfigPropertyException thrown =
                    assertThrows(InvalidConfigPropertyException.class,
                            () -> service.get(TEST_CONFIG_PID, TEST_CONFIG_KEY, String.class));

            // Then
            assertThat(thrown).hasMessage("Could not find config property with key='name.endpoint'.");
        }

        @Test
        void shouldThrowExceptionWhenPropertyWithTargetClazzUnsupportedIsRetrieved() {
            // Expect
            InvalidConfigPropertyException thrown =
                    assertThrows(InvalidConfigPropertyException.class,
                            () -> service.get(TEST_CONFIG_PID, TEST_CONFIG_KEY, BigDecimal.class));

            // Then
            assertThat(thrown).hasMessage("Unsupported conversion. Could not convert config property with key='name.endpoint' for config pid='my.test.config.pid' to type='java.math.BigDecimal'.");
        }
    }

    @Nested
    @DisplayName("Get config Admin property")
    class ConfigAdminProperty {

        @Test
        void getConfigAdminPropertyShouldReturnDefaultValueWhenPropertyNotDefinedInDictionary() throws IOException {
            // Given
            Dictionary<String, Object> properties = new Hashtable<>();
            mockConfigurationWithProperties(TEST_CONFIG_PID, properties);

            // When
            String actualProperty = service.getConfigAdminProperty(TEST_CONFIG_PID, TEST_CONFIG_KEY, "Default", input -> (String) input);

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
            String actualProperty = service.getConfigAdminProperty(TEST_CONFIG_PID, TEST_CONFIG_KEY, "Default", input -> (String) input);

            // Then
            assertThat(actualProperty).isEmpty();
        }
    }

    @Nested
    @DisplayName("Initialize service tests")
    class InitializeService {

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
            String pid1 = "com.reedelk.esb.custom.components1";
            String pid2 = "com.reedelk.esb.custom.components2";

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
    }


    private Properties singleKeyValueProperty(String key, String value) {
        Properties properties = new Properties();
        properties.put(key, value);
        return properties;
    }

    private void mockConfigurationWithTestPropertyValue(Object propertyValue) throws IOException {
        Dictionary<String,Object> properties = new Hashtable<>();
        properties.put(TEST_CONFIG_KEY, propertyValue);
        mockConfigurationWithProperties(TEST_CONFIG_PID, properties);
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