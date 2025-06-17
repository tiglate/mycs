package ludo.mentis.aciem.controlclient.util;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Implementation of MultipartFile that wraps a file
 */
public class FileMultipartFile implements MultipartFile {
    private final Path path;
    private final String name;
    private final String originalFilename;
    private final String contentType;

    public FileMultipartFile(Path path) throws IOException {
        this.path = path;
        this.name = path.getFileName().toString();
        this.originalFilename = path.getFileName().toString();
        this.contentType = Files.probeContentType(path);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getOriginalFilename() {
        return originalFilename;
    }

    @Override
    public String getContentType() {
        return contentType;
    }

    @Override
    public boolean isEmpty() {
        try {
            return Files.size(path) == 0;
        } catch (IOException e) {
            return true;
        }
    }

    @Override
    public long getSize() {
        try {
            return Files.size(path);
        } catch (IOException e) {
            return 0;
        }
    }

    @Override
    public byte[] getBytes() throws IOException {
        return Files.readAllBytes(path);
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return Files.newInputStream(path);
    }

    @Override
    public Resource getResource() {
        try {
            return new UrlResource(path.toUri());
        } catch (IOException e) {
            throw new RuntimeException("Failed to get resource", e);
        }
    }

    @Override
    public void transferTo(File dest) throws IOException, IllegalStateException {
        Files.copy(path, dest.toPath());
    }

    @Override
    public void transferTo(Path dest) throws IOException, IllegalStateException {
        Files.copy(path, dest);
    }
}