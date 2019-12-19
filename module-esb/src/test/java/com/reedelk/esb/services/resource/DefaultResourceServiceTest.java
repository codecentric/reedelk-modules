package com.reedelk.esb.services.resource;

import com.reedelk.esb.module.Module;
import com.reedelk.esb.module.ModulesManager;
import com.reedelk.runtime.api.resource.ResourceService;
import com.reedelk.runtime.api.script.ScriptEngineService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

import static org.mockito.Mockito.doReturn;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class DefaultResourceServiceTest {

    private final long testModuleId = 234L;
    private final String testModuleName = "test-module";
    private final String testVersion = "1.0.0-SNAPSHOT";
    private final String testFilePath = "/users/user/test-module-1.0.0-SNAPSHOT.jar";

    @Mock
    private Bundle bundle;
    @Mock
    private Module module;
    @Mock
    private BundleContext context;
    @Mock
    private ModulesManager modulesManager;
    @Mock
    private ScriptEngineService scriptEngineService;

    private ResourceService fileProvider;

    @BeforeEach
    void setUp() {
        fileProvider = new DefaultResourceService(scriptEngineService);
        doReturn(testModuleId).when(module).id();
        doReturn(testModuleName).when(module).name();
        doReturn(testVersion).when(module).version();
        doReturn(testFilePath).when(module).filePath();
    }

    // TODO: Test this service and fix these tests

    /**
    @Test
    void shouldCorrectlyReturnFileBytes() throws IOException, InterruptedException {
        // Given
        String content = "my content";
        String tmpDirectory = TmpDir.get();

        String resource = "/tests/sample.txt";
        ModuleId moduleId = new ModuleId(testModuleId);

        doReturn(bundle).when(context).getBundle(testModuleId);
        doReturn(module).when(modulesManager).getModuleById(testModuleId);

        URL writtenFile = FileUtils.createFile(Paths.get(tmpDirectory, "tests").toString(), "sample.txt", content);

        Enumeration<URL> fileURLs = Collections.enumeration(Collections.singletonList(writtenFile));
        doReturn(fileURLs).when(bundle).getResources(resource);

        // When
        Publisher<byte[]> stream = fileProvider.findResourceBy(moduleId, resource);

        // Then
        StepVerifier.create(stream)
                .expectNextMatches(bytes -> Arrays.equals(content.getBytes(), bytes))
                .verifyComplete();
    }


    @Test
    void shouldThrowFileNotFoundException() throws IOException {
        // Given
        String resource = "/tests/sample.txt";
        ModuleId moduleId = new ModuleId(testModuleId);

        doReturn(bundle).when(context).getBundle(testModuleId);
        doReturn(module).when(modulesManager).getModuleById(testModuleId);


        Enumeration<URL> fileURLs = Collections.enumeration(Collections.emptyList());
        doReturn(fileURLs).when(bundle).getResources(resource);

        // When
        FileNotFoundException thrown = assertThrows(FileNotFoundException.class,
                () -> fileProvider.findResourceBy(moduleId, resource));

        assertThat(thrown)
                .hasMessage("Could not find local file file=[/tests/sample.txt] in module with id=[234], name=[test-module].");
    }

    @Test
    void shouldThrowFileNotFoundExceptionWhenResourcesAreNull() throws IOException {
        // Given
        String resource = "/tests/sample.txt";
        ModuleId moduleId = new ModuleId(testModuleId);

        doReturn(bundle).when(context).getBundle(testModuleId);
        doReturn(module).when(modulesManager).getModuleById(testModuleId);

        doReturn(null).when(bundle).getResources(resource);

        // When
        FileNotFoundException thrown = assertThrows(FileNotFoundException.class,
                () -> fileProvider.findResourceBy(moduleId, resource));

        assertThat(thrown)
                .hasMessage("Could not find local file file=[/tests/sample.txt] in module with id=[234], name=[test-module].");
    }

    @Test
    void shouldThrowExceptionWhenErrorWhileReadingDataFromBundle() throws IOException {
        // Given
        String resource = "/tests/sample.txt";
        ModuleId moduleId = new ModuleId(testModuleId);

        doReturn(bundle).when(context).getBundle(testModuleId);
        doReturn(module).when(modulesManager).getModuleById(testModuleId);

        doThrow(new IOException("Error while reading data")).when(bundle).getResources(resource);

        // When
        ESBException thrown = assertThrows(ESBException.class,
                () -> fileProvider.findResourceBy(moduleId, resource));

        assertThat(thrown)
                .hasMessage("An I/O occurred while reading file=[/tests/sample.txt] in module with id=[234], name=[test-module]: Error while reading data");
    }*/
}