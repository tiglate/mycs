package ludo.mentis.aciem.controlserver.service;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public interface PathValidationService {
    boolean isPathAllowed(Path path);

    boolean isPathAllowed(String pathString);

    Path validateAndSanitizePath(String pathString) throws IOException;

    List<Path> getAllowedDirectories();
}
