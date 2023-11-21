package com.fiendstar.logIngestor.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.cassandra.core.cql.Ordering;
import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyClass;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;

import java.time.Instant;

import static org.springframework.data.cassandra.core.cql.PrimaryKeyType.CLUSTERED;
import static org.springframework.data.cassandra.core.cql.PrimaryKeyType.PARTITIONED;

@PrimaryKeyClass
@Data
@AllArgsConstructor
@NoArgsConstructor
public class LogKey {

    @PrimaryKeyColumn(name = "traceid", type = PARTITIONED)
    private String traceId;

    @PrimaryKeyColumn(name = "spanid", type = PARTITIONED)
    private String spanId;

    @PrimaryKeyColumn(name = "timestamp", type = CLUSTERED, ordering = Ordering.DESCENDING)
    private Instant timestamp;

}


