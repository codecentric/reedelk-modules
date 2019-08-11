package com.reedelk.esb.lifecycle;

import com.reedelk.esb.module.Module;
import com.reedelk.esb.module.ModuleDeserializer;
import com.reedelk.esb.module.ModulesManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RemoveModuleTest {

    @Mock
    private ModulesManager modulesManager;
    @Mock
    private ModuleDeserializer deserializer;

    private RemoveModule step;

    @BeforeEach
    void setUp() {
        step = spy(new RemoveModule());
        doReturn(modulesManager).when(step).modulesManager();
    }

    @Test
    void shouldRemoveModuleFormModulesManager() {
        // Given
        String testVersion = "1.0.0-SNAPSHOT";
        String testLocation = "file://location/test";
        Module module = Module.builder()
                .moduleId(14L)
                .name("TestModule")
                .version(testVersion)
                .deserializer(deserializer)
                .moduleFilePath(testLocation)
                .build();

        // When
        step.run(module);

        // Then
        verify(modulesManager).removeModuleById(module.id());
        verifyNoMoreInteractions(modulesManager);
    }

}