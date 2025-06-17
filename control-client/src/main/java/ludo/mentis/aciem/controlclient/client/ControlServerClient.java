package ludo.mentis.aciem.controlclient.client;

import ludo.mentis.aciem.controlclient.model.FileInfo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Feign client for the control-server
 */
@FeignClient(name = "control-server")
public interface ControlServerClient {

    /**
     * Upload a file to the specified directory
     *
     * @param file The file to upload
     * @param directory The directory to upload to
     * @return Response with the path where the file was saved
     */
    @PostMapping(value = "/api/files/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ResponseEntity<String> uploadFile(
            @RequestPart("file") MultipartFile file,
            @RequestParam("directory") String directory);

    /**
     * Download a file from the specified path
     *
     * @param filePath The path of the file to download
     * @return The file as a downloadable resource
     */
    @GetMapping("/api/files/download")
    ResponseEntity<Resource> downloadFile(@RequestParam("filePath") String filePath);

    /**
     * List all files in the specified directory
     *
     * @param directory The directory to list files from
     * @return A list of file information
     */
    @GetMapping("/api/files/list")
    ResponseEntity<List<FileInfo>> listFiles(@RequestParam("directory") String directory);
}