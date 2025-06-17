package ludo.mentis.aciem.controlserver.security;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ApiKeySecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @TempDir
    Path tempDir;

    @Value("${api.key:default-api-key-for-development-only}")
    private String apiKey;

    @Test
    void requestWithValidApiKey_shouldSucceed() throws Exception {
        // Create a file in the temp directory for listing
        Files.writeString(tempDir.resolve("test.txt"), "test content");

        mockMvc.perform(MockMvcRequestBuilders.get("/api/files/list")
                .param("directory", tempDir.toString())
                .header("X-API-KEY", apiKey)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void requestWithInvalidApiKey_shouldReturnUnauthorized() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/files/list")
                .param("directory", tempDir.toString())
                .header("X-API-KEY", "invalid-api-key")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void requestWithoutApiKey_shouldReturnUnauthorized() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/files/list")
                .param("directory", tempDir.toString())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void uploadRequestWithValidApiKey_shouldSucceed() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.txt",
                MediaType.TEXT_PLAIN_VALUE,
                "Test content".getBytes()
        );

        mockMvc.perform(MockMvcRequestBuilders.multipart("/api/files/upload")
                .file(file)
                .param("directory", tempDir.toString())
                .header("X-API-KEY", apiKey))
                .andExpect(status().isOk());
    }

    @Test
    void actuatorEndpoint_shouldBeAccessibleWithoutApiKey() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/actuator/health")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }
}
