package com.fiendstar.logIngestor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class LogIngestorApplication {

    private static final Logger logger = LoggerFactory.getLogger(LogIngestorApplication.class);


    public static void main(String[] args) {

        SpringApplication.run(LogIngestorApplication.class, args);

        // Example log messages
        logger.info("**** These are just to test the logger ****");
        logger.error("     This is an ERROR message.");
        logger.warn("     This is a WARN message.");
        logger.info("     This is an INFO message.");
        logger.debug("     This is a DEBUG message.");
        logger.trace("     This is a TRACE message.");
        logger.info("**** These are just to test the logger ****");
    }

}
