package ludo.mentis.aciem.controlserver.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class PathValidationServiceTest {

    private PathValidationServiceImpl pathValidationService;
    
    @TempDir
    Path tempDir;
    
    private Path allowedDir1;
    private Path allowedDir2;
    
    @BeforeEach
    void setUp() {
        // Create two allowed directories for testing
        allowedDir1 = tempDir.resolve("allowed1");
        allowedDir2 = tempDir.resolve("allowed2");
        
        // Configure the service with the allowed directories
        String allowedDirs = allowedDir1 + "," + allowedDir2;
        pathValidationService = new PathValidationServiceImpl(allowedDirs);
    }
    
    @Test
    void isPathAllowed_shouldReturnTrueForPathsInAllowedDirectories() {
        // Test paths directly in allowed directories
        assertTrue(pathValidationService.isPathAllowed(allowedDir1.toString()));
        assertTrue(pathValidationService.isPathAllowed(allowedDir2.toString()));
        
        // Test paths in subdirectories of allowed directories
        assertTrue(pathValidationService.isPathAllowed(allowedDir1.resolve("subdir").toString()));
        assertTrue(pathValidationService.isPathAllowed(allowedDir2.resolve("file.txt").toString()));
    }
    
    @Test
    void isPathAllowed_shouldReturnFalseForPathsOutsideAllowedDirectories() {
        // Test path outside allowed directories
        Path outsidePath = tempDir.resolve("outside");
        assertFalse(pathValidationService.isPathAllowed(outsidePath.toString()));
        
        // Test parent directory of allowed directories
        assertFalse(pathValidationService.isPathAllowed(tempDir.toString()));
        
        // Test root directory
        assertFalse(pathValidationService.isPathAllowed("/"));
        assertFalse(pathValidationService.isPathAllowed("C:\\"));
    }
    
    @Test
    void isPathAllowed_shouldHandlePathTraversalAttempts() {
        // Test path traversal attempts
        assertFalse(pathValidationService.isPathAllowed(allowedDir1 + "/../outside"));
        assertFalse(pathValidationService.isPathAllowed(allowedDir1 + "/../../etc/passwd"));
        
        // Test normalized paths that try to escape
        Path traversalPath = allowedDir1.resolve("..").resolve("..").resolve("outside");
        assertFalse(pathValidationService.isPathAllowed(traversalPath.toString()));
    }
    
    @Test
    void validateAndSanitizePath_shouldReturnNormalizedPathForAllowedPaths() throws IOException {
        // Test with an allowed path
        Path testPath = allowedDir1.resolve("test.txt");
        Path sanitizedPath = pathValidationService.validateAndSanitizePath(testPath.toString());
        
        // Should return a normalized absolute path
        assertEquals(testPath.normalize().toAbsolutePath(), sanitizedPath);
    }
    
    @Test
    void validateAndSanitizePath_shouldThrowExceptionForDisallowedPaths() {
        // Test with a disallowed path
        Path outsidePath = tempDir.resolve("outside").resolve("test.txt");
        
        // Should throw IOException
        IOException exception = assertThrows(IOException.class, 
                () -> pathValidationService.validateAndSanitizePath(outsidePath.toString()));
        
        // Exception message should contain "Access denied"
        assertTrue(exception.getMessage().contains("Access denied"));
    }
    
    @Test
    void validateAndSanitizePath_shouldThrowExceptionForPathTraversalAttempts() {
        // Test with a path traversal attempt
        String traversalPath = allowedDir1 + "/../outside/test.txt";
        
        // Should throw IOException
        assertThrows(IOException.class, 
                () -> pathValidationService.validateAndSanitizePath(traversalPath));
    }
    
    @Test
    void getAllowedDirectories_shouldReturnConfiguredDirectories() {
        // Should return the two configured directories
        assertEquals(2, pathValidationService.getAllowedDirectories().size());
        
        // Directories should be normalized
        assertTrue(pathValidationService.getAllowedDirectories().contains(allowedDir1.normalize()));
        assertTrue(pathValidationService.getAllowedDirectories().contains(allowedDir2.normalize()));
    }
}