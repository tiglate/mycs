package ludo.mentis.aciem.controlserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@EnableDiscoveryClient
@SpringBootApplication
public class ControlServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(ControlServerApplication.class, args);
    }

}
