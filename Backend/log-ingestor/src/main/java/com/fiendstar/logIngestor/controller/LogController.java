package com.fiendstar.logIngestor.controller;

import com.fiendstar.logIngestor.model.LogEntry;
import com.fiendstar.logIngestor.model.LogPrimaryKey;
import com.fiendstar.logIngestor.model.ScyllaDbEntity;
import com.fiendstar.logIngestor.repository.LogEventRepository;
import com.fiendstar.logIngestor.service.LogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/logs")
public class LogController {

    private static final Logger logger = LoggerFactory.getLogger(LogController.class);


    @Autowired
    private LogService logService;

//    @PostMapping
//    public ResponseEntity<String> ingestLog(@RequestBody LogEntry logEntry) {
//        try {
//            logService.processLogEntry(logEntry);
//            return ResponseEntity.ok("Log Received: " + logEntry.getMessage());
//        } catch (Exception e) {
//            return ResponseEntity.internalServerError().body("Error processing log");
//        }
//    }

    @PostMapping
    public ResponseEntity<String> ingestLog(@RequestBody byte[] logData) {
        try {
            logService.processLogData(logData);
            return ResponseEntity.ok("Log Received");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error processing log");
        }
    }

    @Autowired
    private LogEventRepository logEventRepository;

    // GET: Retrieve all log events
    @GetMapping("/log-events")
    public ResponseEntity<List<ScyllaDbEntity>> getAllLogEvents() {
        logger.info("getAllLogEvents - Entry into the method");

        try {
            List<ScyllaDbEntity> events = logEventRepository.findAll();
            logger.info("getAllLogEvents - Retrieved events: {}", events);

            if (events.isEmpty()) {
                logger.info("getAllLogEvents - No content found");
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            }
            return new ResponseEntity<>(events, HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error retrieving log events: ", e);
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // GET: Retrieve a single log event by Trace ID
    @GetMapping("/log-events/{traceId}")
    public ResponseEntity<ScyllaDbEntity> getLogEventByTraceId(@PathVariable("traceId") String traceId) {
        Optional<ScyllaDbEntity> logEventData = logEventRepository.findById(traceId);
        return logEventData.map(logEvent -> new ResponseEntity<>(logEvent, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }


    // POST: Create a new log event
    @PostMapping("/log-events")
    public ResponseEntity<ScyllaDbEntity> createLogEvent(@RequestBody LogEntry logEntry) {
        try {
            LogPrimaryKey pk = new LogPrimaryKey(logEntry.getTraceId(), logEntry.getSpanId(), logEntry.getTimestamp());
            ScyllaDbEntity newLogEvent = new ScyllaDbEntity(pk, logEntry.getLevel(), logEntry.getMessage(), logEntry.getResourceId(), logEntry.getCommit(), logEntry.getMetadata());
            ScyllaDbEntity savedLogEvent = logEventRepository.save(newLogEvent);
            return new ResponseEntity<>(savedLogEvent, HttpStatus.CREATED);
        } catch (Exception e) {
            logger.error("Error creating log event: ", e);
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // PUT: Update an existing log event
    @PutMapping("/log-events/{traceId}/{spanId}/{timestamp}")
    public ResponseEntity<ScyllaDbEntity> updateLogEvent(@PathVariable("traceId") String traceId, @PathVariable("spanId") String spanId, @PathVariable("timestamp") String timestamp, @RequestBody LogEntry logEntry) {
        LogPrimaryKey pk = new LogPrimaryKey(traceId, spanId, timestamp);

        Optional<ScyllaDbEntity> logEventData = logEventRepository.findById(String.valueOf(pk));

        if (logEventData.isPresent()) {
            ScyllaDbEntity logEventToUpdate = logEventData.get();
            logEventToUpdate.setLevel(logEntry.getLevel());
            logEventToUpdate.setMessage(logEntry.getMessage());
            logEventToUpdate.setResourceId(logEntry.getResourceId());
            logEventToUpdate.setCommit(logEntry.getCommit());
            logEventToUpdate.setMetadata(logEntry.getMetadata());
            return new ResponseEntity<>(logEventRepository.save(logEventToUpdate), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    // DELETE: Delete a log event by Trace ID
    @DeleteMapping("/log-events/{traceId}")
    public ResponseEntity<HttpStatus> deleteLogEvent(@PathVariable("traceId") String traceId) {
        try {
            logEventRepository.deleteById(traceId);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // DELETE: Delete all log events
    @DeleteMapping("/log-events")
    public ResponseEntity<HttpStatus> deleteAllLogEvents() {
        try {
            logEventRepository.deleteAll();
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Additional methods for filtering can be added as required

}

