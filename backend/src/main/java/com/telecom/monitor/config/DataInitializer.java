package com.telecom.monitor.config;

import com.telecom.monitor.model.*;
import com.telecom.monitor.repository.AlertRepository;
import com.telecom.monitor.repository.IncidentRepository;
import com.telecom.monitor.repository.NetworkEventRepository;
import com.telecom.monitor.repository.TowerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final TowerRepository towerRepository;
    private final NetworkEventRepository networkEventRepository;
    private final IncidentRepository incidentRepository;
    private final AlertRepository alertRepository;

    public DataInitializer(TowerRepository towerRepository,
                           NetworkEventRepository networkEventRepository,
                           IncidentRepository incidentRepository,
                           AlertRepository alertRepository) {
        this.towerRepository = towerRepository;
        this.networkEventRepository = networkEventRepository;
        this.incidentRepository = incidentRepository;
        this.alertRepository = alertRepository;
    }

    @Override
    public void run(String... args) {
        if (towerRepository.count() > 0) {
            log.info("Database already seeded with telecom infrastructure data.");
            return;
        }

        log.info("Initializing Telecom Network Infrastructure Seed Dataset...");

        // 1. Seed Cell Towers (Metro Indian hubs)
        List<Tower> towers = List.of(
                new Tower("HYD-4521", "Hitec City 5G Alpha", "Hyderabad", "5G", TowerStatus.DEGRADED, 17.4435, 78.3772, 1000, 12000),
                new Tower("HYD-1092", "Gachibowli Financial District", "Hyderabad", "5G", TowerStatus.ACTIVE, 17.4399, 78.3489, 1000, 9500),
                new Tower("BLR-1001", "Electronic City Phase 1", "Bengaluru", "5G", TowerStatus.ACTIVE, 12.8399, 77.6770, 1200, 14000),
                new Tower("BLR-2045", "Koramangala 80ft Hub", "Bengaluru", "4G", TowerStatus.ACTIVE, 12.9352, 77.6245, 500, 8500),
                new Tower("MUM-3012", "Bandra Kurla Complex (BKC)", "Mumbai", "5G", TowerStatus.ACTIVE, 19.0657, 72.8687, 1500, 18000),
                new Tower("MUM-4109", "Andheri Metro West", "Mumbai", "4G", TowerStatus.ACTIVE, 19.1197, 72.8464, 600, 11000),
                new Tower("DEL-5001", "Connaught Place Inner Circle", "Delhi NCR", "5G", TowerStatus.ACTIVE, 28.6315, 77.2167, 1200, 16000),
                new Tower("DEL-5088", "Cyber City DLF Phase 2", "Delhi NCR", "5G", TowerStatus.ACTIVE, 28.4906, 77.0898, 1200, 15000),
                new Tower("CHN-6020", "OMR IT Express Corridor", "Chennai", "5G", TowerStatus.ACTIVE, 12.9249, 80.2299, 800, 9000),
                new Tower("PUN-7014", "Hinjawadi Tech Park Phase 1", "Pune", "4G", TowerStatus.ACTIVE, 18.5913, 73.7389, 500, 7500)
        );
        towerRepository.saveAll(towers);
        log.info("Saved {} Cell Towers.", towers.size());

        // 2. Seed Baseline Historical Events for HYD-4521 and BLR-1001
        Instant now = Instant.now();
        List<NetworkEvent> events = List.of(
                new NetworkEvent("HYD-4521", now.minus(25, ChronoUnit.MINUTES), 48.0, 0.4, 180.0, 9500, -78.0, 4.2, "ACTIVE"),
                new NetworkEvent("HYD-4521", now.minus(20, ChronoUnit.MINUTES), 62.0, 1.1, 140.0, 10200, -82.0, 6.0, "ACTIVE"),
                new NetworkEvent("HYD-4521", now.minus(15, ChronoUnit.MINUTES), 185.0, 7.5, 60.0, 11800, -96.0, 18.5, "DEGRADED"),
                new NetworkEvent("HYD-4521", now.minus(10, ChronoUnit.MINUTES), 245.0, 18.2, 32.0, 12450, -104.0, 32.0, "DEGRADED"),
                new NetworkEvent("HYD-4521", now.minus(5, ChronoUnit.MINUTES), 280.0, 21.0, 24.0, 13100, -108.0, 41.0, "DEGRADED"),

                new NetworkEvent("BLR-1001", now.minus(10, ChronoUnit.MINUTES), 34.0, 0.2, 220.0, 11000, -72.0, 2.5, "ACTIVE"),
                new NetworkEvent("BLR-1001", now.minus(5, ChronoUnit.MINUTES), 38.0, 0.3, 210.0, 11400, -74.0, 2.8, "ACTIVE"),
                new NetworkEvent("MUM-3012", now.minus(8, ChronoUnit.MINUTES), 42.0, 0.5, 310.0, 15000, -70.0, 3.1, "ACTIVE")
        );
        networkEventRepository.saveAll(events);
        log.info("Saved {} historical telemetry events.", events.size());

        // 3. Seed Seed Incidents
        Incident inc1 = new Incident(
                "INC-1001",
                "HYD-4521",
                "Hyderabad",
                IncidentSeverity.HIGH,
                IncidentStatus.OPEN,
                "HIGH Network Degradation on Tower HYD-4521",
                "High Latency detected (245.0 ms > 200 ms) | Severe Packet Loss (18.2% > 12%) | High subscriber volume congestion (12450 active users)",
                "Automated detection rule triggered with score 65: Optical fiber feeder interface degradation suspected near Hitec City interchange.",
                245.0,
                18.2
        );
        inc1.setCreatedAt(now.minus(10, ChronoUnit.MINUTES));
        inc1.setUpdatedAt(now.minus(5, ChronoUnit.MINUTES));
        incidentRepository.save(inc1);

        // Seed sample resolved incident for MTTR calculation
        Incident inc2 = new Incident(
                "INC-1002",
                "BLR-2045",
                "Bengaluru",
                IncidentSeverity.MEDIUM,
                IncidentStatus.RESOLVED,
                "Elevated Packet Loss on Tower BLR-2045",
                "Moderate Packet Loss (6.5%) during evening peak traffic",
                "Core link dynamic routing re-converged. Congestion cleared after microwave dish realignment.",
                110.0,
                6.5
        );
        inc2.setCreatedAt(now.minus(90, ChronoUnit.MINUTES));
        inc2.setUpdatedAt(now.minus(30, ChronoUnit.MINUTES));
        inc2.setResolvedAt(now.minus(30, ChronoUnit.MINUTES));
        inc2.setResolvedBy("Operator-Suresh");
        inc2.setResolutionNotes("Dish realignment complete. Signal dBm normalized.");
        incidentRepository.save(inc2);

        // 4. Seed Alerts
        Alert alert1 = new Alert("INC-1001", AlertChannel.PAGERDUTY, "noc-oncall-leads@telecom.net",
                "[HIGH ALERT] Incident INC-1001 on Tower HYD-4521 (Hyderabad, 5G): High Latency and Packet Loss detected");
        alert1.setSentAt(now.minus(10, ChronoUnit.MINUTES));
        alertRepository.save(alert1);

        Alert alert2 = new Alert("INC-1001", AlertChannel.SLACK, "#telco-network-alerts",
                "[HIGH ALERT] Incident INC-1001 on Tower HYD-4521 (Hyderabad, 5G): Automated ticket created");
        alert2.setSentAt(now.minus(10, ChronoUnit.MINUTES));
        alertRepository.save(alert2);

        log.info("Telecom data initialization finished successfully.");
    }
}
