package com.fiendstar.logIngestor.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Table("logs")
public class ScyllaDbEntity {

    @PrimaryKey
    private LogPrimaryKey key;

    private String level;
    private String message;
    private String resourceId;
    private String commit;
    private Map<String, String> metadata;
}

