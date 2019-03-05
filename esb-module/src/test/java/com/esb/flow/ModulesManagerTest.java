package com.esb.flow;

import com.esb.module.Module;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.Collections;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

class ModulesManagerTest {

    private final String component1 = "com.esb.foonel.testing.AwesomeComponent1";
    private final String component2 = "com.esb.foonel.testing.AwesomeComponent2";
    private final String component3 = "com.esb.foonel.testing.AwesomeComponent3";
    private final String component4 = "com.esb.foonel.testing.AwesomeComponent4";

    private final long moduleId = 232L;
    private final String testModuleName = "TestModule";
    private final String testVersion = "1.0.0-SNAPSHOT";
    private final String testLocation = "file://location/test";

    private ModulesManager manager;

    @BeforeEach
    void setUp() {
        manager = new ModulesManager();
    }

    @Test
    void shouldAddModuleCorrectly() {
        // Given
        Module module = Module.builder()
                .moduleId(moduleId)
                .name(testModuleName)
                .moduleFilePath(testLocation)
                .version(testVersion)
                .build();

        // When
        manager.add(module);

        // Then
        Module returned = manager.getModuleById(moduleId);
        assertThat(returned).isEqualTo(module);
        assertThat(returned.id()).isEqualTo(moduleId);
    }

    @Test
    void shouldRemoveModuleCorrectly() {
        // Given
        Module module = Module.builder()
                .moduleId(moduleId)
                .name(testModuleName)
                .moduleFilePath(testLocation)
                .version(testVersion)
                .build();
        manager.add(module);

        // When
        manager.removeModuleById(moduleId);

        // Then
        assertThat(manager.getModuleById(moduleId)).isNull();
    }

    @Test
    void shouldAllModulesReturnAllRegisteredModules() {
        // Given
        Module module1 = Module.builder().moduleId(1L).name("TestModule1").moduleFilePath(testLocation).version(testVersion).build();
        Module module2 = Module.builder().moduleId(2L).name("TestModule2").moduleFilePath(testLocation).version(testVersion).build();
        Module module3 = Module.builder().moduleId(3L).name("TestModule3").moduleFilePath(testLocation).version(testVersion).build();

        manager.add(module1);
        manager.add(module2);
        manager.add(module3);

        // When
        Collection<Module> allModules = manager.allModules();

        // Then
        assertThat(allModules).hasSize(3);
        assertThat(allModules).containsExactlyInAnyOrder(module1, module2, module3);
    }

    @Test
    void shouldFindUnresolvedModulesReturnModulesWithStateUnresolved() {
        // Given
        Collection<String> unresolvedComponents = asList(component1, component3);
        Collection<String> resolvedComponents = asList(component2, component4);

        Module module1 = Module.builder().moduleId(1L).name("TestModule1").moduleFilePath(testLocation).version(testVersion).build();
        module1.unresolve(unresolvedComponents, resolvedComponents);

        Module module2 = Module.builder().moduleId(2L).name("TestModule2").moduleFilePath(testLocation).version(testVersion).build();

        Module module3 = Module.builder().moduleId(3L).name("TestModule3").moduleFilePath(testLocation).version(testVersion).build();
        module3.unresolve(unresolvedComponents, resolvedComponents);

        manager.add(module1);
        manager.add(module2);
        manager.add(module3);

        // When
        Collection<Module> unresolvedModules = manager.findUnresolvedModules();

        // Then
        assertThat(unresolvedModules).hasSize(2);
        assertThat(unresolvedModules).containsExactly(module1, module3);
    }

    @Test
    void shouldFindModulesUsingComponentReturnCorrectModulesWhenUnresolved() {
        // Given
        Collection<String> unresolvedComponents = asList(component1, component3);
        Collection<String> resolvedComponents = asList(component2, component4);

        Module module1 = Module.builder().moduleId(1L).name("TestModule1").moduleFilePath(testLocation).version(testVersion).build();
        module1.unresolve(unresolvedComponents, resolvedComponents);

        Module module2 = Module.builder().moduleId(2L).name("TestModule2").moduleFilePath(testLocation).version(testVersion).build();
        module2.error(Collections.emptyList());

        Module module3 = Module.builder().moduleId(3L).name("TestModule3").moduleFilePath(testLocation).version(testVersion).build();
        module3.unresolve(unresolvedComponents, resolvedComponents);


        manager.add(module1);
        manager.add(module2);
        manager.add(module3);

        // When
        Collection<Module> modulesUsingComponent = manager.findModulesUsingComponent(component2);

        // Then
        assertThat(modulesUsingComponent).hasSize(2);
        assertThat(modulesUsingComponent).containsExactlyInAnyOrder(module1, module3);
    }

}