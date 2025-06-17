package ludo.mentis.aciem.controlserver.service;

import ludo.mentis.aciem.controlserver.model.FileInfo;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public interface FileService {
    Path uploadFile(MultipartFile file, String directory) throws IOException;

    Resource downloadFile(String filePath) throws IOException;

    List<FileInfo> listFiles(String directory) throws IOException;
}
