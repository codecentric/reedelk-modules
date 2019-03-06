package com.esb.lifecycle;

import com.esb.module.Module;
import com.esb.module.state.ModuleState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.osgi.framework.Bundle;
import org.osgi.framework.Version;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

@ExtendWith(MockitoExtension.class)
class CreateModuleTest {

    private static final Void VOID = null;

    @Mock
    private Bundle bundle;

    private CreateModule step;

    @BeforeEach
    void setUp() {
        step = spy(new CreateModule());
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
        assertThat(created.state()).isEqualTo(ModuleState.INSTALLED);
        assertThat(created.version()).isEqualTo("1.0.0");
        assertThat(created.name()).isEqualTo("test-bundle");
        assertThat(created.moduleFilePath()).isEqualTo("file:/usr/local/desktop/my-bundle-1.0.0.jar");
    }
}
