package com.telecom.monitor.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.telecom.monitor.dto.NetworkEventDto;
import com.telecom.monitor.service.NetworkEventService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "telecom.kafka.enabled", havingValue = "true")
public class KafkaTelemetryConsumer {

    private static final Logger log = LoggerFactory.getLogger(KafkaTelemetryConsumer.class);

    private final NetworkEventService networkEventService;
    private final ObjectMapper objectMapper;

    public KafkaTelemetryConsumer(NetworkEventService networkEventService, ObjectMapper objectMapper) {
        this.networkEventService = networkEventService;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = KafkaTelemetryProducer.TOPIC, groupId = "telecom-incident-engine-group")
    public void consumeTelemetry(String message) {
        try {
            log.info("Kafka consumer received telemetry event: {}", message);
            NetworkEventDto dto = objectMapper.readValue(message, NetworkEventDto.class);
            networkEventService.processEvent(dto);
        } catch (Exception e) {
            log.error("Error processing incoming Kafka telemetry message: {}", message, e);
        }
    }
}
