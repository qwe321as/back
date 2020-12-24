package com.crepass.restfulapi.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ExloggerApplication {
    private static final Logger logger = LoggerFactory.getLogger(ExloggerApplication.class);
    public static void main(String[] args) {
        SpringApplication.run(ExloggerApplication.class, args);
        logger.error("Message logged at ERROR level");
        logger.warn("Message logged at WARN level");
        logger.info("Message logged at INFO level");
        logger.debug("Message logged at DEBUG level");
    }
}
