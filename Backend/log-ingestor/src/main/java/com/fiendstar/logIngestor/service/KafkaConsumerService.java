package com.fiendstar.logIngestor.service;

import com.fiendstar.logIngestor.model.ScyllaDbEntity;
import com.fiendstar.logIngestor.repository.LogEventRepository;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class KafkaConsumerService {

    @Autowired
    private LogEventRepository scyllaDbRepository; // Your ScyllaDB repository

    @KafkaListener(topics = "yourTopicName", groupId = "yourGroupId")
    public void consume(ConsumerRecord<String, ScyllaDbEntity> record) {
        ScyllaDbEntity entity = record.value();
        // Logic to save the entity to ScyllaDB
        scyllaDbRepository.save(entity);
    }
}
