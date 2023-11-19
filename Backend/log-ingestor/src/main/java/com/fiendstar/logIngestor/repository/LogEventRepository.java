package com.fiendstar.logIngestor.repository;

import com.fiendstar.logIngestor.model.ScyllaDbEntity;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.data.cassandra.repository.Query;
import org.springframework.stereotype.Repository;
import java.time.ZonedDateTime;
import java.util.List;

@Repository
public interface LogEventRepository extends CassandraRepository<ScyllaDbEntity, String> {

    @Query("SELECT * FROM logs WHERE level = ?0 ALLOW FILTERING")
    List<ScyllaDbEntity> findByLevel(String level);

    @Query("SELECT * FROM logs WHERE message LIKE ?0 ALLOW FILTERING")
    List<ScyllaDbEntity> findByMessageContaining(String message);

    @Query("SELECT * FROM logs WHERE resourceId = ?0 ALLOW FILTERING")
    List<ScyllaDbEntity> findByResourceId(String resourceId);

    @Query("SELECT * FROM logs WHERE timestamp = ?0 ALLOW FILTERING")
    List<ScyllaDbEntity> findByTimestamp(ZonedDateTime timestamp);

    List<ScyllaDbEntity> findByMessageContainingIgnoreCase(String message);

    // For date range
    @Query("SELECT * FROM logs WHERE timestamp >= ?0 AND timestamp <= ?1 ALLOW FILTERING")
    List<ScyllaDbEntity> findByTimestampRange(ZonedDateTime start, ZonedDateTime end);

}
