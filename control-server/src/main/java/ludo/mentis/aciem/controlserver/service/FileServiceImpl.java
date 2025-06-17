package ludo.mentis.aciem.controlserver.service;

import ludo.mentis.aciem.controlserver.model.FileInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class FileServiceImpl implements FileService {

    private static final Logger logger = LoggerFactory.getLogger(FileServiceImpl.class);

    private final PathValidationService pathValidationService;

    public FileServiceImpl(PathValidationService pathValidationService) {
        this.pathValidationService = pathValidationService;
        logger.info("FileService initialized with path validation");
    }

    /**
     * Upload a file to the specified directory
     * 
     * @param file The file to upload
     * @param directory The directory to upload to
     * @return The path where the file was saved
     * @throws IOException If an I/O error occurs or if the directory is not allowed
     */
    @Override
    public Path uploadFile(MultipartFile file, String directory) throws IOException {
        // Validate and sanitize the directory path
        Path validatedDirPath = pathValidationService.validateAndSanitizePath(directory);
        logger.debug("Validated upload directory: {}", validatedDirPath);

        // Resolve the target file path and ensure the filename is safe
        String originalFilename = Objects.requireNonNull(file.getOriginalFilename());
        // Sanitize filename - remove path traversal sequences and dangerous characters
        String safeFilename = originalFilename
                .replaceAll("\\.\\.", "") // Remove path traversal sequences
                .replaceAll("[^a-zA-Z0-9._-]", "_"); // Replace other dangerous chars with underscore
        Path targetLocation = validatedDirPath.resolve(safeFilename);

        // Ensure the target location is still within allowed directories
        if (!pathValidationService.isPathAllowed(targetLocation)) {
            throw new IOException("Access denied: Target file location is not within allowed directories");
        }

        Files.createDirectories(targetLocation.getParent());
        Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
        logger.info("File uploaded successfully to: {}", targetLocation);
        return targetLocation;
    }

    /**
     * Download a file from the specified path
     * 
     * @param filePath The path of the file to download
     * @return The file as a Resource
     * @throws IOException If an I/O error occurs, or if the file path is not allowed
     */
    @Override
    public Resource downloadFile(String filePath) throws IOException {
        // Validate and sanitize the file path
        Path validatedPath = pathValidationService.validateAndSanitizePath(filePath);
        logger.debug("Validated download file path: {}", validatedPath);

        Resource resource = new UrlResource(validatedPath.toUri());

        if (resource.exists() && resource.isReadable()) {
            logger.info("File download requested: {}", validatedPath);
            return resource;
        } else {
            logger.warn("Could not read file: {}", validatedPath);
            throw new IOException("Could not read file: " + filePath);
        }
    }

    /**
     * List all files in the specified directory
     * 
     * @param directory The directory to list files from
     * @return A list of file information
     * @throws IOException If an I/O error occurs or if the directory is not allowed
     */
    @Override
    public List<FileInfo> listFiles(String directory) throws IOException {
        // Validate and sanitize the directory path
        Path dirPath = pathValidationService.validateAndSanitizePath(directory);
        logger.debug("Validated list directory: {}", dirPath);

        if (!Files.exists(dirPath)) {
            logger.warn("Directory does not exist: {}", dirPath);
            throw new IOException("Directory does not exist: " + directory);
        }

        if (!Files.isDirectory(dirPath)) {
            logger.warn("Path is not a directory: {}", dirPath);
            throw new IOException("Path is not a directory: " + directory);
        }

        try (Stream<Path> paths = Files.list(dirPath)) {
            return paths.map(path -> {
                try {
                    return new FileInfo(
                        path.getFileName().toString(),
                        Files.isDirectory(path),
                        Files.size(path),
                        Files.getLastModifiedTime(path).toMillis()
                    );
                } catch (IOException e) {
                    return new FileInfo(
                        path.getFileName().toString(),
                        Files.isDirectory(path),
                        -1,
                        -1
                    );
                }
            }).collect(Collectors.toList());
        }
    }
}
