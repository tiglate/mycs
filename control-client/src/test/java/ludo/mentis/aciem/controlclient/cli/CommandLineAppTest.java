package ludo.mentis.aciem.controlclient.cli;

import ludo.mentis.aciem.controlclient.model.FileInfo;
import ludo.mentis.aciem.controlclient.service.FileClientService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CommandLineAppTest {

    @Mock
    private FileClientService fileClientService;

    private CommandLineApp commandLineApp;
    
    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;

    @BeforeEach
    void setUp() {
        commandLineApp = new CommandLineApp(fileClientService);
        System.setOut(new PrintStream(outContent));
    }
    
    @AfterEach
    void tearDown() {
        System.setOut(originalOut);
    }

    @Test
    void testRunWithNoArgs() throws Exception {
        // Act
        commandLineApp.run();
        
        // Assert
        String output = outContent.toString();
        assertTrue(output.contains("Usage: control-client <command> [args]"));
        assertTrue(output.contains("upload <filePath> <targetDirectory>"));
        assertTrue(output.contains("download <filePath> <localDirectory>"));
        assertTrue(output.contains("list <directory>"));
    }

    @Test
    void testRunWithUnknownCommand() throws Exception {
        // Act
        commandLineApp.run("unknown");
        
        // Assert
        String output = outContent.toString();
        assertTrue(output.contains("Error: Unknown command: unknown"));
        assertTrue(output.contains("Usage: control-client <command> [args]"));
    }

    @Test
    void testRunUploadCommand() throws Exception {
        // Arrange
        String filePath = "test.txt";
        String targetDirectory = "/target/dir";
        String expectedResponse = "File uploaded successfully";
        
        when(fileClientService.uploadFile(eq(filePath), eq(targetDirectory)))
                .thenReturn(expectedResponse);
        
        // Act
        commandLineApp.run("upload", filePath, targetDirectory);
        
        // Assert
        String output = outContent.toString();
        assertTrue(output.contains("Uploading file: " + filePath));
        assertTrue(output.contains("Upload result: " + expectedResponse));
        
        verify(fileClientService).uploadFile(eq(filePath), eq(targetDirectory));
    }

    @Test
    void testRunUploadCommandWithMissingArgs() throws Exception {
        // Act
        commandLineApp.run("upload");
        
        // Assert
        String output = outContent.toString();
        assertTrue(output.contains("Error: Missing arguments for upload command"));
        assertTrue(output.contains("Usage: control-client <command> [args]"));
    }

    @Test
    void testRunDownloadCommand() throws Exception {
        // Arrange
        String remoteFilePath = "/remote/file.txt";
        String localDirectory = "/local/dir";
        Path expectedPath = Paths.get("/local/dir/file.txt");
        
        when(fileClientService.downloadFile(eq(remoteFilePath), eq(localDirectory)))
                .thenReturn(expectedPath);
        
        // Act
        commandLineApp.run("download", remoteFilePath, localDirectory);
        
        // Assert
        String output = outContent.toString();
        assertTrue(output.contains("Downloading file from: " + remoteFilePath));
        assertTrue(output.contains("File downloaded to: " + expectedPath));
        
        verify(fileClientService).downloadFile(eq(remoteFilePath), eq(localDirectory));
    }

    @Test
    void testRunDownloadCommandWithMissingArgs() throws Exception {
        // Act
        commandLineApp.run("download");
        
        // Assert
        String output = outContent.toString();
        assertTrue(output.contains("Error: Missing arguments for download command"));
        assertTrue(output.contains("Usage: control-client <command> [args]"));
    }

    @Test
    void testRunListCommand() throws Exception {
        // Arrange
        String directory = "/test/dir";
        List<FileInfo> files = Arrays.asList(
                new FileInfo("file1.txt", false, 100, 1620000000000L),
                new FileInfo("dir1", true, 0, 1620000000000L)
        );
        
        when(fileClientService.listFiles(eq(directory)))
                .thenReturn(files);
        
        // Act
        commandLineApp.run("list", directory);
        
        // Assert
        String output = outContent.toString();
        assertTrue(output.contains("Listing files in directory: " + directory));
        assertTrue(output.contains("file1.txt"));
        assertTrue(output.contains("dir1"));
        assertTrue(output.contains("Total: 2 items"));
        
        verify(fileClientService).listFiles(eq(directory));
    }

    @Test
    void testRunListCommandWithMissingArgs() throws Exception {
        // Act
        commandLineApp.run("list");
        
        // Assert
        String output = outContent.toString();
        assertTrue(output.contains("Error: Missing arguments for list command"));
        assertTrue(output.contains("Usage: control-client <command> [args]"));
    }

    @Test
    void testRunCommandWithException() throws Exception {
        // Arrange
        String directory = "/test/dir";
        String errorMessage = "Test error message";
        
        when(fileClientService.listFiles(anyString()))
                .thenThrow(new IOException(errorMessage));
        
        // Act
        commandLineApp.run("list", directory);
        
        // Assert
        String output = outContent.toString();
        assertTrue(output.contains("Error: " + errorMessage));
    }
}