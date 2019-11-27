package com.reedelk.esb.flow.deserializer.typefactory;

import com.reedelk.esb.module.DeserializedModule;
import com.reedelk.esb.module.deserializer.ScriptResource;
import com.reedelk.runtime.api.exception.ESBException;
import com.reedelk.runtime.api.script.Script;
import com.reedelk.runtime.commons.TypeFactory;
import com.reedelk.runtime.commons.TypeFactoryContext;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doReturn;

@ExtendWith(MockitoExtension.class)
class ScriptFunctionBodyResolverDecoratorTest {

    private long testModuleId = 10L;

    @Mock
    private DeserializedModule mockDeserializedModule;

    private TypeFactoryContext factoryContext = new TypeFactoryContext(testModuleId);

    private ScriptFunctionBodyResolverDecorator decorator;

    @BeforeEach
    void setUp() {
        decorator = new ScriptFunctionBodyResolverDecorator(TypeFactory.getInstance(), mockDeserializedModule);
    }

    /**
     * We expect that the script resource path in the Script
     * object is replaced with the script function body.
     */
    @Test
    void shouldCorrectlyLoadScriptFunctionBody() {
        // Given
        String propertyName = "integrationScript";

        JSONObject componentDefinition = new JSONObject();
        componentDefinition.put(propertyName, "/integration/map_data.js");

        String scriptResource1Body = "return 'map data'";
        String scriptResource2Body = "return 'my script'";
        ScriptResource scriptResource1 = new ScriptResource("/user/local/project/myProject/src/main/resources/scripts/integration/map_data.js", scriptResource1Body);
        ScriptResource scriptResource2 = new ScriptResource("/user/local/project/myProject/src/main/resources/scripts/integration/my_script.js", scriptResource2Body);
        Collection<ScriptResource> scriptResources = Arrays.asList(scriptResource1, scriptResource2);

        doReturn(scriptResources).when(mockDeserializedModule).getScriptResources();

        // When
        Script actualScript = decorator.create(Script.class, componentDefinition, propertyName, factoryContext);

        // Then
        assertThat(actualScript.functionName()).isNotNull();
        assertThat(actualScript.functionName()).contains("_" + testModuleId + "_"); // make sure that the function UUID contains the module id in its name as well.
        assertThat(actualScript.context().getModuleId()).isEqualTo(testModuleId);
        assertThat(actualScript.body()).isEqualTo(scriptResource1Body);
    }

    @Test
    void shouldThrowExceptionWhenScriptFunctionBodyCouldNotBeLoadedBecauseScriptPathDoesNotMatchDeSerializedModuleScriptResources() {
        // Given
        String propertyName = "integrationScript";

        JSONObject componentDefinition = new JSONObject();
        componentDefinition.put(propertyName, "/integration/not_existent.js");

        String scriptResource1Body = "return 'map data'";
        String scriptResource2Body = "return 'my script'";
        ScriptResource scriptResource1 = new ScriptResource("/user/local/project/myProject/src/main/resources/scripts/integration/map_data.js", scriptResource1Body);
        ScriptResource scriptResource2 = new ScriptResource("/user/local/project/myProject/src/main/resources/scripts/integration/my_script.js", scriptResource2Body);
        Collection<ScriptResource> scriptResources = Arrays.asList(scriptResource1, scriptResource2);

        doReturn(scriptResources).when(mockDeserializedModule).getScriptResources();

        // When
        ESBException thrown = assertThrows(ESBException.class,
                () -> decorator.create(Script.class, componentDefinition, propertyName, factoryContext));

        // Then
        assertThat(thrown).hasMessage("Could not find script named=[/integration/not_existent.js] defined in resources/scripts folder. Please make sure that the referenced script exists.");
    }
}