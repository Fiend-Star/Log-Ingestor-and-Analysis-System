package com.fiendstar.logIngestor.controller;

import com.fiendstar.logIngestor.model.LogEntry;
import com.fiendstar.logIngestor.model.LogKey;
import com.fiendstar.logIngestor.model.ScyllaDbEntity;
import com.fiendstar.logIngestor.repository.LogEventRepository;
import com.fiendstar.logIngestor.service.LogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@RestController
@CrossOrigin
public class LogController {

    private static final Logger logger = LoggerFactory.getLogger(LogController.class);


    @Autowired
    private LogService logService;


    @Autowired
    private LogEventRepository logEventRepository;

    @Autowired
    private KafkaTemplate<String, ScyllaDbEntity> kafkaTemplate;

    @Value("${kafka.topic.name}")
    private String logCreationTopicName;

    // Retrieve all log events from the database
    @GetMapping("/all-non-paged")
    public ResponseEntity<List<ScyllaDbEntity>> getAllLogEvents() {
        logger.info("Request to fetch all log events");

        try {

            List<ScyllaDbEntity> events = logEventRepository.findAll();

            if (events.isEmpty()) {
                logger.info("No log events found");
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            }
            return new ResponseEntity<>(events, HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error retrieving all log events", e);
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/all")
    public ResponseEntity<Slice<ScyllaDbEntity>> getAllLogEvents(
            Pageable pageable,
            @RequestParam(required = false) String traceId,
            @RequestParam(required = false) String spanId,
            @RequestParam(required = false) String fromTimestampStr,
            @RequestParam(required = false) String toTimestampStr) {
        logger.info("Request to fetch all log events - pageable: {}, traceId: '{}', spanId: '{}', fromTimestampStr: '{}', toTimestampStr: '{}'", pageable, traceId, spanId, fromTimestampStr, toTimestampStr);
        try {

            logger.info(" pageable: {}", pageable);
            logger.info(" traceId: {}", traceId);
            logger.info(" spanId: {}", spanId);

            Instant fromTimestamp = fromTimestampStr != null ? Instant.parse(fromTimestampStr + ":00.000Z") : Instant.EPOCH; // Default to the start of the epoch
            Instant toTimestamp = toTimestampStr != null ? Instant.parse(toTimestampStr + ":00.000Z") : Instant.now(); // Default to current time

            Slice<ScyllaDbEntity> events;

            if (isNullOrEmpty(traceId) && isNullOrEmpty(spanId)) {
                // Fetch all logs within the timestamp range if both are null or empty
                events = logEventRepository.findByKeyTimestampBetween(fromTimestamp, toTimestamp, pageable);
            } else {
                // Fetch logs based on either or both traceId and spanId, and timestamps
                List<ScyllaDbEntity> eventList = logService.findLogs(traceId, spanId, fromTimestamp, toTimestamp, pageable);
                events = new SliceImpl<>(eventList, pageable, eventList.size() == pageable.getPageSize());
            }

            if (!events.hasNext()) {
                logger.info("No log events found");
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            }

            logger.info("Returning {} log events", events.getNumberOfElements());

            return new ResponseEntity<>(events, HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error retrieving all log events", e);
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private boolean isNullOrEmpty(String str) {
        return str == null || str.isEmpty();
    }


    // Retrieve log events by Trace ID
    @GetMapping("/{traceId}")
    public ResponseEntity<List<ScyllaDbEntity>> getLogEventsByTraceId(@PathVariable("traceId") String traceId) {
        logger.info("Request to fetch log events with traceId: {}", traceId);
        try {
            List<ScyllaDbEntity> logEvents = logEventRepository.findByTraceId(traceId);
            if (logEvents.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
            return new ResponseEntity<>(logEvents, HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error fetching log events with traceId: {}", traceId, e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Create a new log event
    @PostMapping
    public ResponseEntity<ScyllaDbEntity> createLogEvent(@RequestBody LogEntry logEntry) {
        logger.info("Request to create a new log event: {}", logEntry);
        try {

            StringBuilder sb = new StringBuilder();
            sb.append(logEntry.getTimestamp().replace(" ", "T")).append("Z");
            LogKey pk = new LogKey(logEntry.getTraceId(), logEntry.getSpanId(), Instant.parse(sb.toString()));
            ScyllaDbEntity newLogEvent = new ScyllaDbEntity(pk, logEntry.getLevel(), logEntry.getMessage(), logEntry.getResourceId(), logEntry.getCommit(), logEntry.getMetadata());

            // Send message to Kafka topic
            kafkaTemplate.send(logCreationTopicName, newLogEvent);

            logger.info("Log event sent to Kafka successfully: {}", newLogEvent);
            return new ResponseEntity<>(HttpStatus.CREATED);

        } catch (Exception e) {
            logger.error("Error creating a new log event for entry: {}", logEntry, e);
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    // Update an existing log event
    @PutMapping("/{traceId}/{spanId}/{timestamp}")
    public ResponseEntity<ScyllaDbEntity> updateLogEvent(@PathVariable("traceId") String traceId, @PathVariable("spanId") String spanId, @PathVariable("timestamp") String timestamp, @RequestBody LogEntry logEntry) {
        logger.info("Request to update a log event with traceId: {}, spanId: {}, timestamp: {}", traceId, spanId, timestamp);
        try {
            Instant timeStmp = Instant.parse(timestamp);
            LogKey pk = new LogKey(traceId, spanId, timeStmp);
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
                logger.info("Log event not found with traceId: {}, spanId: {}, timestamp: {}", traceId, spanId, timestamp);
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            logger.error("Error updating log event with traceId: {}, spanId: {}, timestamp: {}", traceId, spanId, timestamp, e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Delete a log event by Trace ID and Span ID
    @DeleteMapping("/{traceId}/{spanId}")
    public ResponseEntity<HttpStatus> deleteLogEvents(@PathVariable("traceId") String traceId, @PathVariable("spanId") String spanId) {
        logger.info("Request to delete log events with traceId: {}, spanId: {}", traceId, spanId);
        try {
            logEventRepository.deleteByTraceIdAndSpanId(traceId, spanId);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (Exception e) {
            logger.error("Error deleting log events with traceId: {}, spanId: {}", traceId, spanId, e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Delete all log events
    @DeleteMapping
    public ResponseEntity<HttpStatus> deleteAllLogEvents() {
        logger.info("Request to delete all log events");
        try {
            logEventRepository.deleteAll();
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (Exception e) {
            logger.error("Error deleting all log events", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    // Search logs with multiple filter options and regex support
    @GetMapping("/search")
    public ResponseEntity<List<ScyllaDbEntity>> searchLogs(@RequestParam(required = false) String level, @RequestParam(required = false) String resourceId, @RequestParam(required = false) String regex) {
        logger.info("Search request received - level: {}, resourceId: {}, regex: {}", level, resourceId, regex);
        try {
            List<ScyllaDbEntity> logs = logEventRepository.findByLevelAndResourceId(level, resourceId);
            logger.debug("Logs filtered by level and resourceId, count: {}", logs.size());

            if (regex != null && !regex.isEmpty()) {
                Pattern pattern = Pattern.compile(regex);
                logs = logs.stream().filter(log -> pattern.matcher(log.getMessage()).find()).collect(Collectors.toList());
                logger.debug("Logs filtered by regex, count: {}", logs.size());
            }
            return new ResponseEntity<>(logs, HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error during search", e);
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

}

