package com.reedelk.esb.module.deserializer;


import com.reedelk.esb.module.DeserializedModule;
import com.reedelk.esb.test.utils.FileUtils;
import com.reedelk.esb.test.utils.TmpDir;
import org.json.JSONObject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class FileSystemDeserializerTest {

    private Path projectDir;

    @BeforeEach
    void setUp() {
        String tmpDir = TmpDir.get();
        this.projectDir = Paths.get(tmpDir);
    }

    @AfterEach
    void tearDown() {
        projectDir.toFile().delete();
    }

    @Test
    void shouldDeserializeFlows() throws IOException {
        // Given
        createProjectFile(Paths.get("flows", "flow1.flow"), flowWith("aaa", "Flow1"));
        createProjectFile(Paths.get("flows", "flow2.flow"), flowWith("bbb", "Flow2"));
        createProjectFile(Paths.get("flows", "flow3.flow"), flowWith("ccc", "Flow3"));
        createProjectFile(Paths.get("flows", "flow4.flow"), flowWith("ddd", "Flow4"));
        createProjectFile(Paths.get("flows", "nested", "flow4.flow"), flowWith("eee", "Flow5"));

        FileSystemDeserializer deserializer = new FileSystemDeserializer(projectDir.toString());

        // When
        DeserializedModule deserializedModule = deserializer.deserialize();

        // Then
        Set<JSONObject> flows = deserializedModule.getFlows();
        assertExists(flows, "id", "aaa");
        assertExists(flows, "id", "bbb");
        assertExists(flows, "id", "ccc");
        assertExists(flows, "id", "ddd");
        assertExists(flows, "id", "eee");
        assertThat(flows).hasSize(5);

        assertThat(deserializedModule.getConfigurations()).isEmpty();
        assertThat(deserializedModule.getSubflows()).isEmpty();
        assertThat(deserializedModule.getScriptResources()).isEmpty();
    }

    @Test
    void shouldDeserializeSubFlows() throws IOException {
        // Given
        createProjectFile(Paths.get("flows", "subflow1.subflow"), subFlowWith("aaa", "SubFlow1"));
        createProjectFile(Paths.get("flows", "subflow2.subflow"), subFlowWith("bbb", "SubFlow2"));
        createProjectFile(Paths.get("flows", "subflow3.subflow"), subFlowWith("ccc", "SubFlow3"));
        createProjectFile(Paths.get("flows", "subflow4.subflow"), subFlowWith("ddd", "SubFlow4"));
        createProjectFile(Paths.get("flows", "nested", "subflow5.subflow"), subFlowWith("eee", "SubFlow5"));

        FileSystemDeserializer deserializer = new FileSystemDeserializer(projectDir.toString());

        // When
        DeserializedModule deserializedModule = deserializer.deserialize();

        // Then
        Set<JSONObject> subFlows = deserializedModule.getSubflows();
        assertExists(subFlows, "id", "aaa");
        assertExists(subFlows, "id", "bbb");
        assertExists(subFlows, "id", "ccc");
        assertExists(subFlows, "id", "ddd");
        assertExists(subFlows, "id", "eee");
        assertThat(subFlows).hasSize(5);

        assertThat(deserializedModule.getConfigurations()).isEmpty();
        assertThat(deserializedModule.getFlows()).isEmpty();
        assertThat(deserializedModule.getScriptResources()).isEmpty();
    }

    @Test
    void shouldDeserializeConfigurations() throws IOException {
        // Given
        createProjectFile(Paths.get("configs", "config1.fconfig"), configWith("aaa", "Config1"));
        createProjectFile(Paths.get("configs", "config2.fconfig"), configWith("bbb", "Config2"));
        createProjectFile(Paths.get("configs", "config3.fconfig"), configWith("ccc", "Config3"));
        createProjectFile(Paths.get("configs", "config4.fconfig"), configWith("ddd", "Config4"));
        createProjectFile(Paths.get("configs", "nested", "config5.fconfig"), configWith("eee", "Config5"));

        FileSystemDeserializer deserializer = new FileSystemDeserializer(projectDir.toString());

        // When
        DeserializedModule deserializedModule = deserializer.deserialize();

        // Then
        Collection<JSONObject> configurations = deserializedModule.getConfigurations();
        assertExists(configurations, "id", "aaa");
        assertExists(configurations, "id", "bbb");
        assertExists(configurations, "id", "ccc");
        assertExists(configurations, "id", "ddd");
        assertExists(configurations, "id", "eee");
        assertThat(configurations).hasSize(5);

        assertThat(deserializedModule.getFlows()).isEmpty();
        assertThat(deserializedModule.getSubflows()).isEmpty();
        assertThat(deserializedModule.getScriptResources()).isEmpty();
    }

    @Test
    void shouldDeserializeScripts() throws IOException {
        // Given
        String script1Body = "return 'aaa'";
        createProjectFile(Paths.get("com/reedelk/esb/lifecycle/scripts", "script1.js"), script1Body);
        createProjectFile(Paths.get("com/reedelk/esb/lifecycle/scripts", "script2.js"), "return 'bbb'");
        createProjectFile(Paths.get("com/reedelk/esb/lifecycle/scripts", "script3.js"), "return 'ccc'");
        createProjectFile(Paths.get("com/reedelk/esb/lifecycle/scripts", "script4.js"), "return 'ddd'");
        createProjectFile(Paths.get("com/reedelk/esb/lifecycle/scripts", "nested", "script5.js"), "return 'eee'");

        FileSystemDeserializer deserializer = new FileSystemDeserializer(projectDir.toString());

        // When
        DeserializedModule deserializedModule = deserializer.deserialize();

        // Then
        Collection<ScriptResource> scriptResources = deserializedModule.getScriptResources();
        assertExist(scriptResources,  Paths.get(projectDir.toString(), "com/reedelk/esb/lifecycle/scripts", "script1.js"), script1Body);
        assertThat(scriptResources).hasSize(5);

        assertThat(deserializedModule.getFlows()).isEmpty();
        assertThat(deserializedModule.getSubflows()).isEmpty();
        assertThat(deserializedModule.getConfigurations()).isEmpty();
    }

    private void createProjectFile(Path filePathAndName, String fileContent) throws IOException {
        Path path = Paths.get(projectDir.toString(), filePathAndName.toString());
        FileUtils.createFile(path, fileContent);
    }

    private String flowWith(String id, String title) {
        return "{ \"id\": " + id +", \"title\":" + title + ", \"flow\": []}";
    }

    private String subFlowWith(String id, String title) {
        return "{ \"id\": " + id +", \"title\":" + title + ", \"subflow\": []}";
    }

    private String configWith(String id, String title) {
        return "{ \"id\": " + id +", \"title\":" + title + "}";
    }

    private void assertExists(Collection<JSONObject> objects, String propertyKey, String propertyValue) {
        boolean found = objects.stream()
                .anyMatch(jsonObj -> jsonObj.has(propertyKey) && jsonObj.get(propertyKey).equals(propertyValue));
        assertThat(found)
                .withFailMessage("Object with property key=[%s] and value=[%s] not found", propertyKey, propertyValue)
                .isTrue();
    }

    private void assertExist(Collection<ScriptResource> scriptResources, Path scriptFilePath, String scriptBody) {
        boolean found = scriptResources.stream()
                .anyMatch(scriptResource -> scriptResource.getBody().equals(scriptBody) && scriptResource.getScriptFilePath().equals(scriptFilePath.toString()));
        assertThat(found)
                .withFailMessage("Script with file path=[%s] and body=[%s] not found", scriptFilePath, scriptBody)
                .isTrue();
    }
}
