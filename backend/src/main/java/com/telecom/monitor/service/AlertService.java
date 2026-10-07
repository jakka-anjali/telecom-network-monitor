package com.telecom.monitor.service;

import com.telecom.monitor.model.Alert;
import com.telecom.monitor.model.AlertChannel;
import com.telecom.monitor.model.Incident;

import java.util.List;

public interface AlertService {
    Alert dispatchAlert(Incident incident, AlertChannel channel, String recipient);
    List<Alert> getAlertsByIncident(String incidentId);
    List<Alert> getRecentAlerts();
}
