package com.telecom.monitor.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "alerts")
public class Alert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "incident_id", nullable = false, length = 32)
    private String incidentId;

    @Enumerated(EnumType.STRING)
    @Column(name = "channel", nullable = false, length = 24)
    private AlertChannel channel;

    @Column(name = "recipient", nullable = false)
    private String recipient;

    @Column(name = "message", nullable = false, length = 1024)
    private String message;

    @Column(name = "sent_at", nullable = false)
    private Instant sentAt = Instant.now();

    @Column(name = "delivery_status", length = 32)
    private String deliveryStatus = "SENT";

    public Alert() {
    }

    public Alert(String incidentId, AlertChannel channel, String recipient, String message) {
        this.incidentId = incidentId;
        this.channel = channel;
        this.recipient = recipient;
        this.message = message;
        this.sentAt = Instant.now();
        this.deliveryStatus = "SENT";
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getIncidentId() {
        return incidentId;
    }

    public void setIncidentId(String incidentId) {
        this.incidentId = incidentId;
    }

    public AlertChannel getChannel() {
        return channel;
    }

    public void setChannel(AlertChannel channel) {
        this.channel = channel;
    }

    public String getRecipient() {
        return recipient;
    }

    public void setRecipient(String recipient) {
        this.recipient = recipient;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Instant getSentAt() {
        return sentAt;
    }

    public void setSentAt(Instant sentAt) {
        this.sentAt = sentAt;
    }

    public String getDeliveryStatus() {
        return deliveryStatus;
    }

    public void setDeliveryStatus(String deliveryStatus) {
        this.deliveryStatus = deliveryStatus;
    }
}
