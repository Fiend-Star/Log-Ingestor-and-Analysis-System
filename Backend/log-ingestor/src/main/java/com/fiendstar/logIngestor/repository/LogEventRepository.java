package com.fiendstar.logIngestor.repository;

import com.fiendstar.logIngestor.model.LogKey;
import com.fiendstar.logIngestor.model.ScyllaDbEntity;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.data.cassandra.repository.Query;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.List;

@Repository
public interface LogEventRepository extends CassandraRepository<ScyllaDbEntity, LogKey> {


    Slice<ScyllaDbEntity> findByKeyTimestampBetween(Instant fromTimestamp, Instant toTimestamp, Pageable pageable);
    Slice<ScyllaDbEntity> findByKeyTraceIdOrKeySpanIdAndKeyTimestampBetween(
            String traceId, String spanId, Instant fromTimestamp, Instant toTimestamp, Pageable pageable);

    @Query("SELECT * FROM logs WHERE traceId = ?0 AND spanId = ?1 AND timestamp <= ?2 ALLOW FILTERING")
    Slice<ScyllaDbEntity> findByTraceIdAndSpanIdBetweenTimestamps(String traceId, String spanId, Instant toTimestamp, Pageable pageable);

    @Query("SELECT * FROM logs WHERE traceId = ?0 AND spanId = ?1 AND timestamp >= ?2 AND timestamp <= ?3 ALLOW FILTERING")
    Slice<ScyllaDbEntity> findByTraceIdAndSpanIdBetweenTimestamps(String traceId, String spanId, Instant fromTimestamp, Instant toTimestamp, Pageable pageable);

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

    @Query("SELECT * FROM logs WHERE traceId = :traceId ALLOW FILTERING")
    List<ScyllaDbEntity> findByTraceId(@Param("traceId") String traceId);

    @Query("DELETE FROM logs WHERE traceId = :traceId AND spanId = :spanId")
    void deleteByTraceIdAndSpanId(@Param("traceId") String traceId, @Param("spanId") String spanId);

    @Query("SELECT * FROM logs WHERE level = :level AND resourceId = :resourceId ALLOW FILTERING")
    List<ScyllaDbEntity> findByLevelAndResourceId(@Param("level") String level, @Param("resourceId") String resourceId);


}
