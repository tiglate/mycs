package ludo.mentis.aciem.controlserver.controller;

import ludo.mentis.aciem.controlserver.model.FileInfo;
import ludo.mentis.aciem.controlserver.service.FileService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FileControllerTest {

    @Mock
    private FileService fileService;

    @InjectMocks
    private FileController fileController;

    private MockMultipartFile testFile;
    private String testDirectory;
    private String testFilePath;

    @BeforeEach
    void setUp() {
        testFile = new MockMultipartFile(
                "file",
                "test.txt",
                MediaType.TEXT_PLAIN_VALUE,
                "Test content".getBytes()
        );
        testDirectory = "C:\\test\\directory";
        testFilePath = "C:\\test\\directory\\test.txt";
    }

    @Test
    void uploadFile_shouldReturnSuccessResponse() throws IOException {
        // Arrange
        Path savedPath = Paths.get(testFilePath);
        when(fileService.uploadFile(any(MultipartFile.class), anyString())).thenReturn(savedPath);

        // Act
        ResponseEntity<String> response = fileController.uploadFile(testFile, testDirectory);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody() != null && response.getBody().contains("File uploaded successfully"));
        verify(fileService).uploadFile(testFile, testDirectory);
    }

    @Test
    void uploadFile_shouldReturnErrorResponseWhenUploadFails() throws IOException {
        // Arrange
        when(fileService.uploadFile(any(MultipartFile.class), anyString()))
                .thenThrow(new IOException("Upload failed"));

        // Act
        ResponseEntity<String> response = fileController.uploadFile(testFile, testDirectory);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody() != null && response.getBody().contains("Failed to upload file"));
        verify(fileService).uploadFile(testFile, testDirectory);
    }

    @Test
    void uploadFile_shouldReturnForbiddenWhenDirectoryIsNotAllowed() throws IOException {
        // Arrange
        when(fileService.uploadFile(any(MultipartFile.class), anyString()))
                .thenThrow(new IOException("Access denied: Path is not within allowed directories"));

        // Act
        ResponseEntity<String> response = fileController.uploadFile(testFile, testDirectory);

        // Assert
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertTrue(response.getBody() != null && response.getBody().contains("Access denied"));
        verify(fileService).uploadFile(testFile, testDirectory);
    }

    @Test
    void downloadFile_shouldReturnFileResource() throws IOException {
        // Arrange
        Resource mockResource = mock(Resource.class);
        lenient().when(mockResource.exists()).thenReturn(true);
        lenient().when(mockResource.isReadable()).thenReturn(true);
        when(mockResource.getFilename()).thenReturn("test.txt");

        when(fileService.downloadFile(testFilePath)).thenReturn(mockResource);

        // Act
        ResponseEntity<Resource> response = fileController.downloadFile(testFilePath);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(mockResource, response.getBody());
        assertNotNull(response.getHeaders().getContentDisposition());
        assertTrue(response.getHeaders().getContentDisposition().toString().contains("test.txt"));
        verify(fileService).downloadFile(testFilePath);
    }

    @Test
    void downloadFile_shouldReturnNotFoundWhenDownloadFails() throws IOException {
        // Arrange
        when(fileService.downloadFile(testFilePath)).thenThrow(new IOException("File not found"));

        // Act
        ResponseEntity<Resource> response = fileController.downloadFile(testFilePath);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(fileService).downloadFile(testFilePath);
    }

    @Test
    void downloadFile_shouldReturnForbiddenWhenFilePathIsNotAllowed() throws IOException {
        // Arrange
        when(fileService.downloadFile(testFilePath))
                .thenThrow(new IOException("Access denied: Path is not within allowed directories"));

        // Act
        ResponseEntity<Resource> response = fileController.downloadFile(testFilePath);

        // Assert
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        verify(fileService).downloadFile(testFilePath);
    }

    @Test
    void listFiles_shouldReturnListOfFiles() throws IOException {
        // Arrange
        List<FileInfo> mockFiles = Arrays.asList(
                new FileInfo("file1.txt", false, 100, 1000),
                new FileInfo("file2.txt", false, 200, 2000),
                new FileInfo("subdir", true, 0, 3000)
        );

        when(fileService.listFiles(testDirectory)).thenReturn(mockFiles);

        // Act
        ResponseEntity<?> response = fileController.listFiles(testDirectory);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(mockFiles, response.getBody());
        verify(fileService).listFiles(testDirectory);
    }

    @Test
    void listFiles_shouldReturnErrorResponseWhenListingFails() throws IOException {
        // Arrange
        when(fileService.listFiles(testDirectory)).thenThrow(new IOException("Directory not found"));

        // Act
        ResponseEntity<?> response = fileController.listFiles(testDirectory);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody() != null && response.getBody().toString().contains("Failed to list files"));
        verify(fileService).listFiles(testDirectory);
    }

    @Test
    void listFiles_shouldReturnForbiddenWhenDirectoryIsNotAllowed() throws IOException {
        // Arrange
        when(fileService.listFiles(testDirectory))
                .thenThrow(new IOException("Access denied: Path is not within allowed directories"));

        // Act
        ResponseEntity<?> response = fileController.listFiles(testDirectory);

        // Assert
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertTrue(response.getBody() != null && response.getBody().toString().contains("Access denied"));
        verify(fileService).listFiles(testDirectory);
    }
}
