package ludo.mentis.aciem.controlserver.controller;

import ludo.mentis.aciem.controlserver.model.FileInfo;
import ludo.mentis.aciem.controlserver.service.FileService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

@RestController
@RequestMapping("/api/files")
public class FileController {

    private static final Logger logger = LoggerFactory.getLogger(FileController.class);
    public static final String ACCESS_DENIED = "Access denied";
    private final FileService fileService;

    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

    /**
     * Upload a file to the specified directory
     * 
     * @param file The file to upload
     * @param directory The directory to upload to
     * @return Response with the path where the file was saved
     */
    @PostMapping("/upload")
    public ResponseEntity<String> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam("directory") String directory) {

        try {
            logger.info("Uploading file {} to directory {}", file.getOriginalFilename(), directory);
            Path savedPath = fileService.uploadFile(file, directory);
            return ResponseEntity.ok("File uploaded successfully to: " + savedPath);
        } catch (IOException e) {
            logger.error("Failed to upload file", e);

            // Return FORBIDDEN status for access-denied errors
            if (e.getMessage() != null && e.getMessage().contains(ACCESS_DENIED)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("Access denied: Directory is not in the allowed list");
            }

            return ResponseEntity.badRequest().body("Failed to upload file: " + e.getMessage());
        }
    }

    /**
     * Download a file from the specified path
     * 
     * @param filePath The path of the file to download
     * @return The file as a downloadable resource
     */
    @GetMapping("/download")
    public ResponseEntity<Resource> downloadFile(@RequestParam("filePath") String filePath) {
        try {
            logger.info("Downloading file from path: {}", filePath);
            Resource resource = fileService.downloadFile(filePath);

            String contentType = "application/octet-stream";
            String filename = resource.getFilename();

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .body(resource);
        } catch (IOException e) {
            logger.error("Failed to download file", e);

            // Return FORBIDDEN status for access-denied errors
            if (e.getMessage() != null && e.getMessage().contains(ACCESS_DENIED)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            return ResponseEntity.notFound().build();
        }
    }

    /**
     * List all files in the specified directory
     * 
     * @param directory The directory to list files from
     * @return A list of file information
     */
    @GetMapping("/list")
    public ResponseEntity<?> listFiles(@RequestParam("directory") String directory) {
        try {
            logger.info("Listing files in directory: {}", directory);
            List<FileInfo> files = fileService.listFiles(directory);
            return ResponseEntity.ok(files);
        } catch (IOException e) {
            logger.error("Failed to list files", e);

            // Return FORBIDDEN status for access-denied errors
            if (e.getMessage() != null && e.getMessage().contains(ACCESS_DENIED)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("Access denied: Directory is not in the allowed list");
            }

            return ResponseEntity.badRequest().body("Failed to list files: " + e.getMessage());
        }
    }
}
