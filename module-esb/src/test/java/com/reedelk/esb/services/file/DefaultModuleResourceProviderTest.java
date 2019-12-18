package com.reedelk.esb.services.file;

import com.reedelk.esb.exception.FileNotFoundException;
import com.reedelk.esb.module.Module;
import com.reedelk.esb.module.ModulesManager;
import com.reedelk.esb.test.utils.FileUtils;
import com.reedelk.esb.test.utils.TmpDir;
import com.reedelk.runtime.api.commons.ModuleId;
import com.reedelk.runtime.api.exception.ESBException;
import com.reedelk.runtime.api.resource.ModuleResourceProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.reactivestreams.Publisher;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class DefaultModuleResourceProviderTest {

    private final int BUFFER_SIZE = 65536;
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

    private ModuleResourceProvider fileProvider;

    @BeforeEach
    void setUp() {
        fileProvider = new DefaultModuleResourceProvider(context, modulesManager);
        doReturn(testModuleId).when(module).id();
        doReturn(testModuleName).when(module).name();
        doReturn(testVersion).when(module).version();
        doReturn(testFilePath).when(module).filePath();
    }

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
        Publisher<byte[]> stream = fileProvider.findBy(moduleId, resource, BUFFER_SIZE);

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
                () -> fileProvider.findBy(moduleId, resource, BUFFER_SIZE));

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
                () -> fileProvider.findBy(moduleId, resource, BUFFER_SIZE));

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
                () -> fileProvider.findBy(moduleId, resource, BUFFER_SIZE));

        assertThat(thrown)
                .hasMessage("An I/O occurred while reading file=[/tests/sample.txt] in module with id=[234], name=[test-module]: Error while reading data");
    }
}