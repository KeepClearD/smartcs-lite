// src/main/java/com/smartcs/lite/SmartCsLiteApplication.java
package com.smartcs.lite;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class SmartCsLiteApplication {

    public static void main(String[] args) {
        SpringApplication.run(SmartCsLiteApplication.class, args);
    }
}
