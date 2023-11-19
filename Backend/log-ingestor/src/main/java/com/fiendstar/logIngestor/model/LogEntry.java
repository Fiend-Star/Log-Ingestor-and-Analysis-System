package com.fiendstar.logIngestor.model;

import lombok.*;

import java.util.Map;

@Getter
@Setter
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LogEntry {
    private String level;
    private String message;
    private String resourceId;
    private String timestamp;
    private String traceId;
    private String spanId;
    private String commit;
    private Map<String, String> metadata;

}

