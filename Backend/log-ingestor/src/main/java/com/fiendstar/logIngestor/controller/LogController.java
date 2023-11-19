package com.fiendstar.logIngestor.controller;

import com.fiendstar.logIngestor.model.LogEntry;
import com.fiendstar.logIngestor.model.ScyllaDbEntity;
import com.fiendstar.logIngestor.repository.LogEventRepository;
import com.fiendstar.logIngestor.service.LogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@RestController
public class LogController {

    private static final Logger logger = LoggerFactory.getLogger(LogController.class);


    @Autowired
    private LogService logService;

    @Autowired
    private LogEventRepository logEventRepository;

    // GET: Retrieve all log events
    @GetMapping
    public ResponseEntity<List<ScyllaDbEntity>> getAllLogEvents() {
        logger.info("getAllLogEvents - Entry into the method");

        try {
            List<ScyllaDbEntity> events = logEventRepository.findAll();

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
    public ResponseEntity<List<ScyllaDbEntity>> getLogEventsByTraceId(@PathVariable("traceId") String traceId) {
        try {
            // Assuming you have a method in your repository to find by traceId
            List<ScyllaDbEntity> logEvents = logEventRepository.findByTraceId(traceId);
            if (logEvents.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
            return new ResponseEntity<>(logEvents, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // POST: Create a new log event
    @PostMapping
    public ResponseEntity<ScyllaDbEntity> createLogEvent(@RequestBody LogEntry logEntry) {
        try {
            ScyllaDbEntity.LogKey pk = new ScyllaDbEntity.LogKey(logEntry.getTraceId(), logEntry.getSpanId(), logEntry.getTimestamp());
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

        ScyllaDbEntity.LogKey pk = new ScyllaDbEntity.LogKey(traceId, spanId, timestamp);

        Optional<ScyllaDbEntity> logEventData = logEventRepository.findById(pk);

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
    @DeleteMapping("/log-events/{traceId}/{spanId}")
    public ResponseEntity<HttpStatus> deleteLogEvents(@PathVariable("traceId") String traceId,
                                                      @PathVariable("spanId") String spanId) {
        try {
            logEventRepository.deleteByTraceIdAndSpanId(traceId, spanId);
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

    @GetMapping("/by-level/{level}")
    public ResponseEntity<List<ScyllaDbEntity>> getLogsByLevel(@PathVariable String level) {
        List<ScyllaDbEntity> logs = logEventRepository.findByLevel(level);
        return new ResponseEntity<>(logs, HttpStatus.OK);
    }

    @GetMapping("/by-message")
    public ResponseEntity<List<ScyllaDbEntity>> getLogsByMessageContaining(@RequestParam String message) {
        List<ScyllaDbEntity> logs = logEventRepository.findByMessageContaining(message);
        return new ResponseEntity<>(logs, HttpStatus.OK);
    }

    @GetMapping("/by-resource/{resourceId}")
    public ResponseEntity<List<ScyllaDbEntity>> getLogsByResourceId(@PathVariable String resourceId) {
        List<ScyllaDbEntity> logs = logEventRepository.findByResourceId(resourceId);
        return new ResponseEntity<>(logs, HttpStatus.OK);
    }

    @GetMapping("/by-message-ignore-case")
    public ResponseEntity<List<ScyllaDbEntity>> getLogsByMessageContainingIgnoreCase(@RequestParam String message) {
        List<ScyllaDbEntity> logs = logEventRepository.findByMessageContainingIgnoreCase(message);
        return new ResponseEntity<>(logs, HttpStatus.OK);
    }

    @GetMapping("/by-traceId-spanId-timestamp-range")
    public ResponseEntity<List<ScyllaDbEntity>> getLogsByTraceIdAndSpanIdAndTimestampRange(
            @RequestParam String traceId,
            @RequestParam String spanId,
            @RequestParam ZonedDateTime start,
            @RequestParam ZonedDateTime end) {
        List<ScyllaDbEntity> logs = logEventRepository.findByTraceIdAndSpanIdAndTimestampRange(traceId, spanId, start, end);
        return new ResponseEntity<>(logs, HttpStatus.OK);
    }

    @GetMapping("/by-timestamp-range")
    public ResponseEntity<List<ScyllaDbEntity>> getLogsByTimestampRange(
            @RequestParam ZonedDateTime start,
            @RequestParam ZonedDateTime end) {
        List<ScyllaDbEntity> logs = logEventRepository.findByTimestampRange(start, end);
        return new ResponseEntity<>(logs, HttpStatus.OK);
    }

    @GetMapping("/search")
    public ResponseEntity<List<ScyllaDbEntity>> searchLogs(
            @RequestParam(required = false) String level,
            @RequestParam(required = false) String resourceId,
            @RequestParam(required = false) String regex) {
        logger.info("Search request received - level: {}, resourceId: {}, regex: {}", level, resourceId, regex);
        try {
            List<ScyllaDbEntity> logs = logEventRepository.findByLevelAndResourceId(level, resourceId);
            logger.debug("Logs filtered by level and resourceId, count: {}", logs.size());

            if (regex != null && !regex.isEmpty()) {
                Pattern pattern = Pattern.compile(regex);
                logs = logs.stream()
                        .filter(log -> pattern.matcher(log.getMessage()).find())
                        .collect(Collectors.toList());
                logger.debug("Logs filtered by regex, count: {}", logs.size());
            }
            return new ResponseEntity<>(logs, HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error during search", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


}

