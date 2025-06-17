package ludo.mentis.aciem.controlclient.service;

import ludo.mentis.aciem.controlclient.client.ControlServerClient;
import ludo.mentis.aciem.controlclient.model.FileInfo;
import ludo.mentis.aciem.controlclient.util.FileMultipartFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;

/**
 * Service for handling file operations with the control-server
 */
@Service
public class FileClientService {

    private static final Logger logger = LoggerFactory.getLogger(FileClientService.class);
    private final ControlServerClient controlServerClient;

    public FileClientService(ControlServerClient controlServerClient) {
        this.controlServerClient = controlServerClient;
    }

    /**
     * Upload a file to the specified directory
     *
     * @param filePath The path of the file to upload
     * @param targetDirectory The directory to upload to
     * @return Response message from the server
     * @throws IOException If an I/O error occurs
     */
    public String uploadFile(String filePath, String targetDirectory) throws IOException {
        logger.info("Uploading file {} to directory {}", filePath, targetDirectory);

        Path path = Paths.get(filePath);
        if (!Files.exists(path)) {
            throw new IOException("File does not exist: " + filePath);
        }

        // Create a MultipartFile from the file
        MultipartFile multipartFile = new FileMultipartFile(path);

        ResponseEntity<String> response = controlServerClient.uploadFile(multipartFile, targetDirectory);
        return response.getBody();
    }

    /**
     * Download a file from the specified path
     *
     * @param remoteFilePath The path of the file on the server
     * @param localDirectory The local directory to save the file to
     * @return The path where the file was saved
     * @throws IOException If an I/O error occurs
     */
    public Path downloadFile(String remoteFilePath, String localDirectory) throws IOException {
        logger.info("Downloading file from path: {} to directory: {}", remoteFilePath, localDirectory);

        ResponseEntity<Resource> response = controlServerClient.downloadFile(remoteFilePath);
        Resource resource = response.getBody();

        if (resource == null) {
            throw new IOException("Failed to download file: Resource is null");
        }

        Path targetPath = Paths.get(localDirectory).resolve(resource.getFilename());
        Files.createDirectories(targetPath.getParent());
        Files.copy(resource.getInputStream(), targetPath);

        return targetPath;
    }

    /**
     * List all files in the specified directory
     *
     * @param directory The directory to list files from
     * @return A list of file information
     * @throws IOException If an I/O error occurs
     */
    public List<FileInfo> listFiles(String directory) throws IOException {
        logger.info("Listing files in directory: {}", directory);

        ResponseEntity<List<FileInfo>> response = controlServerClient.listFiles(directory);
        List<FileInfo> files = response.getBody();

        if (files == null) {
            throw new IOException("Failed to list files: Response body is null");
        }

        return files;
    }
}
