package ludo.mentis.aciem.controlserver.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.core.io.Resource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FileServiceTest {

    private FileService fileService;
    
    @TempDir
    Path tempDir;
    
    @BeforeEach
    void setUp() {
        fileService = new FileService();
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
        List<FileService.FileInfo> files = fileService.listFiles(tempDir.toString());
        
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
}