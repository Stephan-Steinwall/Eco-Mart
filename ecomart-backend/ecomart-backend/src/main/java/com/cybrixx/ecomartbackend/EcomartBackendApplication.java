package com.cybrixx.ecomartbackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync // push notifications are sent off the request thread
public class EcomartBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(EcomartBackendApplication.class, args);
    }

}
