package com.esb.lifecycle;

import com.esb.flow.ModulesManager;
import com.esb.module.Module;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@ExtendWith(MockitoExtension.class)
class RemoveModuleTest {

    @Mock
    private ModulesManager modulesManager;

    private RemoveModule step;

    @BeforeEach
    void setUp() {
        step = new RemoveModule(modulesManager);
    }

    @Test
    void shouldRemoveModuleFormModulesManager() {
        // Given
        String testVersion = "1.0.0-SNAPSHOT";
        String testLocation = "file://location/test";
        Module module = Module.builder()
                .moduleId(14L)
                .name("TestModule")
                .moduleFilePath(testLocation)
                .version(testVersion)
                .build();

        // When
        step.run(module);

        // Then
        verify(modulesManager).removeModuleById(module.id());
        verifyNoMoreInteractions(modulesManager);
    }

}