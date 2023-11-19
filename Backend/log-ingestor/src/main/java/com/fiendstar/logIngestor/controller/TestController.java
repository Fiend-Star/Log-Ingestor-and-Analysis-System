package com.fiendstar.logIngestor.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    @GetMapping("/test")
    public ResponseEntity<String> testController() {
        try {
            return ResponseEntity.ok("Log Received");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error processing log");
        }
    }
}
