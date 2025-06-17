package ludo.mentis.aciem.controlserver.service;

import ludo.mentis.aciem.controlserver.model.FileInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.core.io.Resource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class FileServiceTest {

    private FileServiceImpl fileService;

    @Mock
    private PathValidationService pathValidationService;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() throws IOException {
        MockitoAnnotations.openMocks(this);

        // Configure the mock PathValidationService to allow the temp directory
        when(pathValidationService.validateAndSanitizePath(any(String.class)))
                .thenAnswer(invocation -> {
                    String path = invocation.getArgument(0);
                    return Path.of(path).normalize().toAbsolutePath();
                });

        when(pathValidationService.isPathAllowed(any(Path.class))).thenReturn(true);
        when(pathValidationService.isPathAllowed(any(String.class))).thenReturn(true);

        fileService = new FileServiceImpl(pathValidationService);
    }

    @Test
    void uploadFile_shouldSaveFileToSpecifiedDirectory() throws IOException {
        // Arrange
        String content = "Test file content";
        MultipartFile file = new MockMultipartFile("test.txt", "test.txt", "text/plain", content.getBytes());

        // Act
        Path savedPath = fileService.uploadFile(file, tempDir.toString());

        // Assert
        assertTrue(Files.exists(savedPath));
        assertEquals(content, Files.readString(savedPath));
    }

    @Test
    void downloadFile_shouldReturnFileAsResource() throws IOException {
        // Arrange
        String content = "Test file content";
        Path filePath = tempDir.resolve("test.txt");
        Files.writeString(filePath, content);

        // Act
        Resource resource = fileService.downloadFile(filePath.toString());

        // Assert
        assertTrue(resource.exists());
        assertTrue(resource.isReadable());
        assertEquals("test.txt", resource.getFilename());
    }

    @Test
    void downloadFile_shouldThrowExceptionWhenFileDoesNotExist() {
        // Arrange
        Path nonExistentFile = tempDir.resolve("non-existent.txt");

        // Act & Assert
        assertThrows(IOException.class, () -> fileService.downloadFile(nonExistentFile.toString()));
    }

    @Test
    void listFiles_shouldReturnListOfFiles() throws IOException {
        // Arrange
        Files.writeString(tempDir.resolve("file1.txt"), "content1");
        Files.writeString(tempDir.resolve("file2.txt"), "content2");
        Files.createDirectory(tempDir.resolve("subdir"));

        // Act
        List<FileInfo> files = fileService.listFiles(tempDir.toString());

        // Assert
        assertEquals(3, files.size());
        assertTrue(files.stream().anyMatch(f -> f.name().equals("file1.txt") && !f.directory()));
        assertTrue(files.stream().anyMatch(f -> f.name().equals("file2.txt") && !f.directory()));
        assertTrue(files.stream().anyMatch(f -> f.name().equals("subdir") && f.directory()));
    }

    @Test
    void listFiles_shouldThrowExceptionWhenDirectoryDoesNotExist() {
        // Arrange
        Path nonExistentDir = tempDir.resolve("non-existent-dir");

        // Act & Assert
        assertThrows(IOException.class, () -> fileService.listFiles(nonExistentDir.toString()));
    }

    @Test
    void listFiles_shouldThrowExceptionWhenPathIsNotDirectory() throws IOException {
        // Arrange
        Path filePath = tempDir.resolve("file.txt");
        Files.writeString(filePath, "content");

        // Act & Assert
        assertThrows(IOException.class, () -> fileService.listFiles(filePath.toString()));
    }

    @Test
    void uploadFile_shouldThrowExceptionWhenDirectoryIsNotAllowed() throws IOException {
        // Arrange
        String content = "Test file content";
        MultipartFile file = new MockMultipartFile("test.txt", "test.txt", "text/plain", content.getBytes());

        // Configure mock to reject this path
        String disallowedPath = "/disallowed/directory";
        when(pathValidationService.validateAndSanitizePath(disallowedPath))
                .thenThrow(new IOException("Access denied: Path is not within allowed directories"));

        // Act & Assert
        IOException exception = assertThrows(IOException.class, 
                () -> fileService.uploadFile(file, disallowedPath));

        // Verify exception message contains "Access denied"
        assertTrue(exception.getMessage().contains("Access denied"));
    }

    @Test
    void downloadFile_shouldThrowExceptionWhenFilePathIsNotAllowed() throws IOException {
        // Arrange
        String disallowedPath = "/disallowed/file.txt";

        // Configure mock to reject this path
        when(pathValidationService.validateAndSanitizePath(disallowedPath))
                .thenThrow(new IOException("Access denied: Path is not within allowed directories"));

        // Act & Assert
        IOException exception = assertThrows(IOException.class, 
                () -> fileService.downloadFile(disallowedPath));

        // Verify exception message contains "Access denied"
        assertTrue(exception.getMessage().contains("Access denied"));
    }

    @Test
    void listFiles_shouldThrowExceptionWhenDirectoryIsNotAllowed() throws IOException {
        // Arrange
        String disallowedPath = "/disallowed/directory";

        // Configure mock to reject this path
        when(pathValidationService.validateAndSanitizePath(disallowedPath))
                .thenThrow(new IOException("Access denied: Path is not within allowed directories"));

        // Act & Assert
        IOException exception = assertThrows(IOException.class, 
                () -> fileService.listFiles(disallowedPath));

        // Verify exception message contains "Access denied"
        assertTrue(exception.getMessage().contains("Access denied"));
    }

    @Test
    void uploadFile_shouldSanitizeFilename() throws IOException {
        // Arrange
        String content = "Test file content";
        // Create a file with potentially dangerous characters in the name
        MultipartFile file = new MockMultipartFile("test.txt", "mal../icious\"file;.txt", "text/plain", content.getBytes());

        // Act
        Path savedPath = fileService.uploadFile(file, tempDir.toString());

        // Assert
        assertTrue(Files.exists(savedPath));
        // Verify the filename was sanitized (no dangerous characters)
        assertFalse(savedPath.getFileName().toString().contains(".."));
        assertFalse(savedPath.getFileName().toString().contains("/"));
        assertFalse(savedPath.getFileName().toString().contains("\""));
        assertFalse(savedPath.getFileName().toString().contains(";"));
    }
}
