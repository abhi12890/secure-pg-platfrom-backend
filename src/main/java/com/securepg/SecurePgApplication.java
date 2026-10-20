package com.securepg;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SecurePgApplication {

    public static void main(String[] args) {
        SpringApplication.run(SecurePgApplication.class, args);
    }
}
