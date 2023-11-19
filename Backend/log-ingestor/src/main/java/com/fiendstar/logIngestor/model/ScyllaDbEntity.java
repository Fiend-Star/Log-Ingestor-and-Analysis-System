package com.fiendstar.logIngestor.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.cassandra.core.cql.Ordering;
import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.*;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Table("logs")
public class ScyllaDbEntity {

    @PrimaryKey
    private LogKey key;

    @Column("level")
    private String level;

    @Column("message")
    private String message;

    @Column("resourceId")
    private String resourceId;

    @Column("commit")
    private String commit;

    @Column("metadata")
    private Map<String, String> metadata;
}

