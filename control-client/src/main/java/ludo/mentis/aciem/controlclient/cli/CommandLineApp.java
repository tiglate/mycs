package ludo.mentis.aciem.controlclient.cli;

import ludo.mentis.aciem.controlclient.model.FileInfo;
import ludo.mentis.aciem.controlclient.service.FileClientService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Command line application for the control-client
 */
@Component
public class CommandLineApp implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(CommandLineApp.class);
    private final FileClientService fileClientService;

    public CommandLineApp(FileClientService fileClientService) {
        this.fileClientService = fileClientService;
    }

    @Override
    public void run(String... args) throws Exception {
        if (args.length == 0) {
            printUsage();
            return;
        }

        String command = args[0].toLowerCase();
        try {
            switch (command) {
                case "upload":
                    if (args.length < 3) {
                        System.out.println("Error: Missing arguments for upload command");
                        printUsage();
                        return;
                    }
                    uploadFile(args[1], args[2]);
                    break;
                case "download":
                    if (args.length < 3) {
                        System.out.println("Error: Missing arguments for download command");
                        printUsage();
                        return;
                    }
                    downloadFile(args[1], args[2]);
                    break;
                case "list":
                    if (args.length < 2) {
                        System.out.println("Error: Missing arguments for list command");
                        printUsage();
                        return;
                    }
                    listFiles(args[1]);
                    break;
                default:
                    System.out.println("Error: Unknown command: " + command);
                    printUsage();
            }
        } catch (Exception e) {
            logger.error("Error executing command: {}", command, e);
            System.out.println("Error: " + e.getMessage());
        }
    }

    private void uploadFile(String filePath, String targetDirectory) throws IOException {
        System.out.println("Uploading file: " + filePath + " to directory: " + targetDirectory);
        String result = fileClientService.uploadFile(filePath, targetDirectory);
        System.out.println("Upload result: " + result);
    }

    private void downloadFile(String remoteFilePath, String localDirectory) throws IOException {
        System.out.println("Downloading file from: " + remoteFilePath + " to directory: " + localDirectory);
        Path savedPath = fileClientService.downloadFile(remoteFilePath, localDirectory);
        System.out.println("File downloaded to: " + savedPath);
    }

    private void listFiles(String directory) throws IOException {
        System.out.println("Listing files in directory: " + directory);
        List<FileInfo> files = fileClientService.listFiles(directory);
        
        System.out.println("Files in directory: " + directory);
        System.out.println("------------------------------------------------------");
        System.out.printf("%-30s %-10s %-10s %-20s%n", "Name", "Type", "Size (B)", "Last Modified");
        System.out.println("------------------------------------------------------");
        
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        
        for (FileInfo file : files) {
            String type = file.directory() ? "Directory" : "File";
            String size = file.directory() ? "-" : String.valueOf(file.size());
            String lastModified = file.lastModified() > 0 ? 
                    dateFormat.format(new Date(file.lastModified())) : "-";
            
            System.out.printf("%-30s %-10s %-10s %-20s%n", 
                    file.name(), type, size, lastModified);
        }
        
        System.out.println("------------------------------------------------------");
        System.out.println("Total: " + files.size() + " items");
    }

    private void printUsage() {
        System.out.println("Usage: control-client <command> [args]");
        System.out.println("Commands:");
        System.out.println("  upload <filePath> <targetDirectory>  - Upload a file to the specified directory");
        System.out.println("  download <filePath> <localDirectory> - Download a file from the specified path");
        System.out.println("  list <directory>                     - List all files in the specified directory");
    }
}