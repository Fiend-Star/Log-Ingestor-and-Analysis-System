package com.fiendstar.logIngestor.service;

import com.fiendstar.logIngestor.model.LogEntry;
import com.fiendstar.logIngestor.model.LogKey;
import com.fiendstar.logIngestor.model.ScyllaDbEntity;
import com.fiendstar.logIngestor.repository.LogEventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

@Service
public class LogService {

    @Autowired
    private LogEventRepository logRepository;
    @Autowired
    private LogEventRepository logEventRepository;

    public List<ScyllaDbEntity> findLogs(String traceId, String spanId, Instant fromTimestamp, Instant toTimestamp, Pageable pageable) {

        Slice<ScyllaDbEntity> traceIdResults = logEventRepository.findByKeyTraceIdAndKeyTimestampBetween(
                traceId, fromTimestamp, toTimestamp, pageable);

        Slice<ScyllaDbEntity> spanIdResults = logEventRepository.findByKeySpanIdAndKeyTimestampBetween(
                spanId, fromTimestamp, toTimestamp, pageable);

        // Merge and remove duplicates
        Set<ScyllaDbEntity> mergedResults = new HashSet<>();

        mergedResults.addAll(traceIdResults.getContent());
        mergedResults.addAll(spanIdResults.getContent());

        return new ArrayList<>(mergedResults);
    }

    @Async
    public CompletableFuture<Void> processLogBatch(List<ScyllaDbEntity> logBatch) {
        logRepository.saveAll(logBatch);
        return CompletableFuture.completedFuture(null);
    }

    private ScyllaDbEntity convertToEntity(LogEntry logEntry) {

        ScyllaDbEntity entity = new ScyllaDbEntity();
        StringBuilder sb = new StringBuilder();
        sb.append(logEntry.getTimestamp().replace(" ", "T")).append("Z");

        entity.setKey(new LogKey(logEntry.getTraceId(), logEntry.getSpanId(),  Instant.parse(logEntry.getTimestamp())));
        entity.setLevel(logEntry.getLevel());
        entity.setMessage(logEntry.getMessage());
        entity.setResourceId(logEntry.getResourceId());
        entity.setCommit(logEntry.getCommit());
        entity.setMetadata(logEntry.getMetadata());

        return entity;
    }
}
