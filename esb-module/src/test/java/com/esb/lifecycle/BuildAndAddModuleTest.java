package com.esb.lifecycle;

import com.esb.module.ModulesManager;
import com.esb.module.Module;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.osgi.framework.Bundle;
import org.osgi.framework.Version;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BuildAndAddModuleTest {

    private static final Void VOID = null;

    @Mock
    private Bundle bundle;
    @Mock
    private ModulesManager modulesManager;

    private BuildAndAddModule step;

    @BeforeEach
    void setUp() {
        step = spy(new BuildAndAddModule(modulesManager));
        doReturn(bundle).when(step).bundle();
    }

    @Test
    void shouldAddModuleToModulesManager() {
        // Given
        doReturn(123L).when(bundle).getBundleId();
        doReturn(new Version("1.0.0")).when(bundle).getVersion();
        doReturn("test-bundle").when(bundle).getSymbolicName();
        doReturn("file:/usr/local/desktop/my-bundle-1.0.0.jar").when(bundle).getLocation();

        // When
        Module created = step.run(VOID);

        // Then
        assertThat(created).isNotNull();
        verify(modulesManager).add(created);
        verifyNoMoreInteractions(modulesManager);
    }
}
