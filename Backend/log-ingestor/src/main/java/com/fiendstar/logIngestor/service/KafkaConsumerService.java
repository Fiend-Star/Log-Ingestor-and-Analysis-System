package com.fiendstar.logIngestor.service;

import com.fiendstar.logIngestor.model.ScyllaDbEntity;
import com.fiendstar.logIngestor.repository.LogEventRepository;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.common.Metric;
import org.apache.kafka.common.MetricName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.event.ConsumerStartedEvent;
import org.springframework.kafka.event.ListenerContainerIdleEvent;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Service;

@Service
public class KafkaConsumerService {

    private final Logger logger = LoggerFactory.getLogger(KafkaConsumerService.class);

    @Autowired
    private  LogEventRepository scyllaDbRepository;

    @KafkaListener(topics = "log-creation", groupId = "log-consumer-group")
    public void consume(ScyllaDbEntity logEvent) {
        try {
            logger.info("Received Log Event: {}", logEvent);
            scyllaDbRepository.save(logEvent);
            logger.info("Saved Log Event: {}", logEvent);
        } catch (Exception e) {
            logger.error("Error processing log event: {}", logEvent, e);
            handleProcessingError(logEvent, e);
        }
    }
    private void handleProcessingError(ScyllaDbEntity logEvent, Exception exception) {
        logger.warn("Handling processing error for log event: {}", logEvent, exception);
    }

    private void handleErrorDuringConsumption(Message<?> message, Exception exception, Consumer<?, ?> consumer) {
        logger.warn("Handling error during consumption for message: {}", message.getPayload(), exception);
    }

    @EventListener
    public void onConsumerStarted(ConsumerStartedEvent event) {
        logger.info("Kafka consumer has started.");
    }

    @EventListener
    public void onIdleContainer(ListenerContainerIdleEvent event) {
        Consumer<?, ?> consumer = event.getConsumer();
        if (consumer != null) {
            boolean connected = isConsumerConnected(consumer);
            logger.info("Consumer idle event for listener ID {}. Connected: {}", event.getListenerId(), connected);
        } else {
            logger.info("Consumer is null for listener ID {}", event.getListenerId());
        }
    }


    private boolean isConsumerConnected(Consumer<?, ?> consumer) {
        for (Metric metric : consumer.metrics().values()) {
            MetricName metricName = metric.metricName();
            if ("connection-count".equals(metricName.name()) && metric.metricValue().equals(0)) {
                return false;
            }
        }
        return true;
    }

}
