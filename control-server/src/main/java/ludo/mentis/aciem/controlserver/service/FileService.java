package ludo.mentis.aciem.controlserver.service;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class FileService {

    /**
     * Upload a file to the specified directory
     * 
     * @param file The file to upload
     * @param directory The directory to upload to
     * @return The path where the file was saved
     * @throws IOException If an I/O error occurs
     */
    public Path uploadFile(MultipartFile file, String directory) throws IOException {
        Path targetLocation = Paths.get(directory).resolve(Objects.requireNonNull(file.getOriginalFilename()));
        Files.createDirectories(targetLocation.getParent());
        Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
        return targetLocation;
    }

    /**
     * Download a file from the specified path
     * 
     * @param filePath The path of the file to download
     * @return The file as a Resource
     * @throws IOException If an I/O error occurs
     */
    public Resource downloadFile(String filePath) throws IOException {
        Path path = Paths.get(filePath);
        Resource resource = new UrlResource(path.toUri());
        
        if (resource.exists() && resource.isReadable()) {
            return resource;
        } else {
            throw new IOException("Could not read file: " + filePath);
        }
    }

    /**
     * List all files in the specified directory
     * 
     * @param directory The directory to list files from
     * @return A list of file information
     * @throws IOException If an I/O error occurs
     */
    public List<FileInfo> listFiles(String directory) throws IOException {
        Path dirPath = Paths.get(directory);
        
        if (!Files.exists(dirPath)) {
            throw new IOException("Directory does not exist: " + directory);
        }
        
        if (!Files.isDirectory(dirPath)) {
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

    /**
     * Class to represent file information
     */
    public record FileInfo(String name, boolean directory, long size, long lastModified) {
    }
}