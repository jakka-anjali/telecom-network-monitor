package com.telecom.monitor.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.telecom.monitor.dto.NetworkEventDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "telecom.kafka.enabled", havingValue = "true")
public class KafkaTelemetryProducer {

    private static final Logger log = LoggerFactory.getLogger(KafkaTelemetryProducer.class);
    public static final String TOPIC = "telecom.network.events";

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public KafkaTelemetryProducer(KafkaTemplate<String, String> kafkaTemplate, ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    public void publishEvent(NetworkEventDto eventDto) {
        try {
            String jsonPayload = objectMapper.writeValueAsString(eventDto);
            log.info("Publishing telemetry event to Kafka topic [{}]: Tower {}", TOPIC, eventDto.getTowerId());
            kafkaTemplate.send(TOPIC, eventDto.getTowerId(), jsonPayload);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize NetworkEventDto for Kafka publish", e);
        }
    }
}
