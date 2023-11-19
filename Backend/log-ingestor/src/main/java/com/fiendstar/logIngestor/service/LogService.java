package com.fiendstar.logIngestor.service;

import com.fiendstar.logIngestor.model.LogEntry;
import com.fiendstar.logIngestor.model.ScyllaDbEntity;
import com.fiendstar.logIngestor.repository.LogEventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.log.LogMessage;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
public class LogService {

    @Autowired
    private LogEventRepository logRepository;

    public void processLogData(byte[] logData) throws Exception {
        //LogEntry logEntry = LogEntry.parseFrom(logData);
        //logRepository.save(convertToEntity(logEntry));
    }

    @Async
    public CompletableFuture<Void> processLogBatch(List<ScyllaDbEntity> logBatch) {
        logRepository.saveAll(logBatch);
        return CompletableFuture.completedFuture(null);
    }
    private ScyllaDbEntity convertToEntity(LogEntry logEntry) {
        ScyllaDbEntity entity = new ScyllaDbEntity();
        // Populate entity from logEntry
        return entity;
    }
}
