package com.fiendstar.logIngestor.repository;

import com.fiendstar.logIngestor.model.ScyllaDbEntity;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.data.cassandra.repository.Query;
import org.springframework.stereotype.Repository;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface LogEventRepository extends CassandraRepository<ScyllaDbEntity, ScyllaDbEntity.LogKey> {

    @Query("SELECT * FROM logs WHERE level = ?0 ALLOW FILTERING")
    List<ScyllaDbEntity> findByLevel(String level);

    @Query("SELECT * FROM logs WHERE message LIKE ?0 ALLOW FILTERING")
    List<ScyllaDbEntity> findByMessageContaining(String message);

    @Query("SELECT * FROM logs WHERE resourceId = ?0 ALLOW FILTERING")
    List<ScyllaDbEntity> findByResourceId(String resourceId);

    List<ScyllaDbEntity> findByMessageContainingIgnoreCase(String message);

    @Query("SELECT * FROM logs WHERE traceId = ?0 AND spanId = ?1 AND timestamp >= ?2 AND timestamp <= ?3 ALLOW FILTERING")
    List<ScyllaDbEntity> findByTraceIdAndSpanIdAndTimestampRange(String traceId, String spanId, ZonedDateTime start, ZonedDateTime end);

    @Query("SELECT * FROM logKeySpace.logs_by_timestamp WHERE timestamp >= ?0 AND timestamp <= ?1")
    List<ScyllaDbEntity> findByTimestampRange(ZonedDateTime start, ZonedDateTime end);


    Optional<ScyllaDbEntity> findById(String traceId);

    void deleteById(String traceId);
}
