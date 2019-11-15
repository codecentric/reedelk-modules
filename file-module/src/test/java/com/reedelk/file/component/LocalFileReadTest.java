package com.reedelk.file.component;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LocalFileReadTest {

    @Test
    void shouldCreateCorrectPathWithNotEmptyBasePath() {
        // Given
        String basePath = "/webapp/";
        String filePath = "index.html";

        // When
        String finalPath = LocalFileRead.localFinalFilePath(basePath, filePath);

        // Then
        assertThat(finalPath).isEqualTo("/webapp/index.html");
    }

    @Test
    void shouldCreateCorrectPathWithEmptyBasePath() {
        // Given
        String basePath = "";
        String filePath = "layout.css";

        // When
        String finalPath = LocalFileRead.localFinalFilePath(basePath, filePath);

        // Then
        assertThat(finalPath).isEqualTo("layout.css");
    }
}