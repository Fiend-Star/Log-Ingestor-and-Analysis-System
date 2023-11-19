package com.fiendstar.logIngestor.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyClass;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;

import static org.springframework.data.cassandra.core.cql.PrimaryKeyType.CLUSTERED;
import static org.springframework.data.cassandra.core.cql.PrimaryKeyType.PARTITIONED;

//@PrimaryKeyClass
//@Data
//@AllArgsConstructor
//@NoArgsConstructor
//public class LogPrimaryKey {
//
//    @PrimaryKeyColumn(name = "traceId", type = PARTITIONED)
//    private String traceId;
//
//    @PrimaryKeyColumn(name = "spanId", type = PARTITIONED)
//    private String spanId;
//
//    @PrimaryKeyColumn(name = "timestamp", type = CLUSTERED)
//    private String timestamp;
//}
