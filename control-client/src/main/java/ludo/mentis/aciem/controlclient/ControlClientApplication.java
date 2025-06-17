package ludo.mentis.aciem.controlclient;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * Main application class for the control-client
 * This client connects to the control-server via Eureka and OpenFeign
 * It provides command line functionality for downloading, uploading, and listing files
 */
@EnableDiscoveryClient
@EnableFeignClients
@SpringBootApplication
public class ControlClientApplication {

    public static void main(String[] args) {
        SpringApplication.run(ControlClientApplication.class, args);
    }
}
