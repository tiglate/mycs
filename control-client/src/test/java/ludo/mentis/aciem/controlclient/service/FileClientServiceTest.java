package ludo.mentis.aciem.controlclient.service;

import ludo.mentis.aciem.controlclient.client.ControlServerClient;
import ludo.mentis.aciem.controlclient.model.FileInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FileClientServiceTest {

    @Mock
    private ControlServerClient controlServerClient;

    private FileClientService fileClientService;

    @BeforeEach
    void setUp() {
        fileClientService = new FileClientService(controlServerClient);
    }

    @Test
    void testListFiles() throws IOException {
        // Arrange
        String directory = "/test/directory";
        List<FileInfo> expectedFiles = Arrays.asList(
                new FileInfo("file1.txt", false, 100, 1620000000000L),
                new FileInfo("file2.txt", false, 200, 1620000000000L),
                new FileInfo("dir1", true, 0, 1620000000000L)
        );
        
        when(controlServerClient.listFiles(eq(directory)))
                .thenReturn(ResponseEntity.ok(expectedFiles));

        // Act
        List<FileInfo> actualFiles = fileClientService.listFiles(directory);

        // Assert
        assertEquals(expectedFiles.size(), actualFiles.size());
        assertEquals(expectedFiles.get(0).name(), actualFiles.get(0).name());
        assertEquals(expectedFiles.get(1).name(), actualFiles.get(1).name());
        assertEquals(expectedFiles.get(2).name(), actualFiles.get(2).name());
    }

    @Test
    void testListFilesWithNullResponse() {
        // Arrange
        String directory = "/test/directory";
        when(controlServerClient.listFiles(eq(directory)))
                .thenReturn(ResponseEntity.ok(null));

        // Act & Assert
        Exception exception = assertThrows(IOException.class, () -> {
            fileClientService.listFiles(directory);
        });
        
        assertTrue(exception.getMessage().contains("Failed to list files"));
    }

    @Test
    void testDownloadFile() throws IOException {
        // Arrange
        String remoteFilePath = "/test/file.txt";
        String localDirectory = System.getProperty("java.io.tmpdir");
        byte[] fileContent = "test content".getBytes();
        
        ByteArrayResource resource = new ByteArrayResource(fileContent) {
            @Override
            public String getFilename() {
                return "file.txt";
            }
        };
        
        when(controlServerClient.downloadFile(eq(remoteFilePath)))
                .thenReturn(ResponseEntity.ok(resource));

        // Act
        Path downloadedPath = fileClientService.downloadFile(remoteFilePath, localDirectory);

        // Assert
        assertTrue(Files.exists(downloadedPath));
        assertEquals("file.txt", downloadedPath.getFileName().toString());
        
        // Cleanup
        Files.deleteIfExists(downloadedPath);
    }

    @Test
    void testDownloadFileWithNullResponse() {
        // Arrange
        String remoteFilePath = "/test/file.txt";
        String localDirectory = System.getProperty("java.io.tmpdir");
        
        when(controlServerClient.downloadFile(eq(remoteFilePath)))
                .thenReturn(ResponseEntity.ok(null));

        // Act & Assert
        Exception exception = assertThrows(IOException.class, () -> {
            fileClientService.downloadFile(remoteFilePath, localDirectory);
        });
        
        assertTrue(exception.getMessage().contains("Failed to download file"));
    }

    @Test
    void testUploadFile() throws IOException {
        // Arrange
        Path tempFile = Files.createTempFile("test-upload", ".txt");
        Files.write(tempFile, "test content".getBytes());
        
        String targetDirectory = "/test/directory";
        String expectedResponse = "File uploaded successfully to: /test/directory/test-upload.txt";
        
        when(controlServerClient.uploadFile(any(), eq(targetDirectory)))
                .thenReturn(ResponseEntity.ok(expectedResponse));

        // Act
        String response = fileClientService.uploadFile(tempFile.toString(), targetDirectory);

        // Assert
        assertEquals(expectedResponse, response);
        
        // Cleanup
        Files.deleteIfExists(tempFile);
    }

    @Test
    void testUploadNonExistentFile() {
        // Arrange
        String filePath = "/non/existent/file.txt";
        String targetDirectory = "/test/directory";

        // Act & Assert
        Exception exception = assertThrows(IOException.class, () -> {
            fileClientService.uploadFile(filePath, targetDirectory);
        });
        
        assertTrue(exception.getMessage().contains("File does not exist"));
    }
}