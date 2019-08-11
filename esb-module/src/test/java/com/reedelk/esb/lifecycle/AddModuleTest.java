package com.reedelk.esb.lifecycle;

import com.reedelk.esb.module.Module;
import com.reedelk.esb.module.ModulesManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AddModuleTest {

    @Mock
    private ModulesManager modulesManager;

    private AddModule step;

    @BeforeEach
    void setUp() {
        step = spy(new AddModule());
    }

    @Test
    void shouldAddModuleToManager() {
        // Given
        doReturn(modulesManager).when(step).modulesManager();
        Module module = mock(Module.class);

        // When
        step.run(module);

        // Then
        verify(modulesManager).add(module);
        verifyNoMoreInteractions(modulesManager);
    }
}
