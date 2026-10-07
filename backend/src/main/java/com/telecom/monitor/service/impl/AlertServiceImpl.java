package com.telecom.monitor.service.impl;

import com.telecom.monitor.model.Alert;
import com.telecom.monitor.model.AlertChannel;
import com.telecom.monitor.model.Incident;
import com.telecom.monitor.repository.AlertRepository;
import com.telecom.monitor.service.AlertService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class AlertServiceImpl implements AlertService {

    private static final Logger log = LoggerFactory.getLogger(AlertServiceImpl.class);
    private final AlertRepository alertRepository;

    public AlertServiceImpl(AlertRepository alertRepository) {
        this.alertRepository = alertRepository;
    }

    @Override
    public Alert dispatchAlert(Incident incident, AlertChannel channel, String recipient) {
        String message = String.format("[%s ALERT] Incident %s on Tower %s (%s, %s): %s",
                incident.getSeverity(),
                incident.getId(),
                incident.getTowerId(),
                incident.getRegion(),
                incident.getTitle(),
                incident.getDescription()
        );

        log.warn("DISPATCHING TELECOM ALERT -> Channel: {}, Recipient: {}, Message: {}",
                channel, recipient, message);

        Alert alert = new Alert(incident.getId(), channel, recipient, message);
        alert.setSentAt(Instant.now());
        alert.setDeliveryStatus("DELIVERED");
        return alertRepository.save(alert);
    }

    @Override
    public List<Alert> getAlertsByIncident(String incidentId) {
        return alertRepository.findByIncidentIdOrderBySentAtDesc(incidentId);
    }

    @Override
    public List<Alert> getRecentAlerts() {
        return alertRepository.findTop20ByOrderBySentAtDesc();
    }
}
