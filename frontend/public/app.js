// Telecom Network Operations Dashboard JavaScript
const API_BASE = '/api';

let incidentsCache = [];
let autoFeedTimer = null;
let currentIncidentForModal = null;

// Initialize on DOM Load
document.addEventListener('DOMContentLoaded', () => {
    initTabs();
    initFilters();
    initSimButtons();
    initModals();
    initRefreshButton();
    fetchAllData();
});

// Tab Navigation
function initTabs() {
    const tabButtons = document.querySelectorAll('.tab-btn');
    tabButtons.forEach(btn => {
        btn.addEventListener('click', () => {
            tabButtons.forEach(b => b.classList.remove('active'));
            document.querySelectorAll('.tab-content').forEach(c => c.classList.remove('active'));
            btn.classList.add('active');
            const targetId = btn.getAttribute('data-tab');
            document.getElementById(targetId).classList.add('active');

            if (targetId === 'tab-telemetry') loadTelemetry();
            if (targetId === 'tab-towers') loadTowers();
        });
    });
}

// Fetch All Dashboard Data
async function fetchAllData() {
    await Promise.all([
        loadHealthSummary(),
        loadIncidents(),
        loadTelemetry(),
        loadTowers()
    ]);
}

// Load Health Summary and KPIs
async function loadHealthSummary() {
    try {
        const res = await fetch(`${API_BASE}/analytics/network-health`);
        if (!res.ok) throw new Error(`HTTP ${res.status}`);
        const data = await res.json();

        // Update KPIs
        document.getElementById('health-index-val').innerText = `${data.networkHealthIndex}%`;
        const healthBadge = document.getElementById('health-status-badge');
        if (data.networkHealthIndex >= 90) {
            healthBadge.className = 'badge badge-success';
            healthBadge.innerText = 'OPTIMAL';
        } else if (data.networkHealthIndex >= 70) {
            healthBadge.className = 'badge badge-warning';
            healthBadge.innerText = 'DEGRADED';
        } else {
            healthBadge.className = 'badge badge-danger';
            healthBadge.innerText = 'CRITICAL SLA';
        }

        document.getElementById('active-towers-val').innerText = `${data.activeTowers} / ${data.totalTowers}`;
        document.getElementById('towers-breakdown-subtext').innerText = `Degraded: ${data.degradedTowers} | Down: ${data.downTowers}`;

        const activeTotal = data.openIncidents + data.acknowledgedIncidents + data.inProgressIncidents;
        document.getElementById('active-incidents-val').innerText = activeTotal;
        document.getElementById('critical-incidents-val').innerText = `${data.criticalIncidents} CRITICAL`;
        document.getElementById('high-incidents-val').innerText = `${data.highIncidents} HIGH`;

        document.getElementById('mttr-val').innerText = `${data.averageResolutionTimeMinutes} min`;

        // Update Regional Breakdown
        const regionalContainer = document.getElementById('regional-distribution-list');
        if (regionalContainer && data.incidentsByRegion) {
            regionalContainer.innerHTML = '';
            for (const [region, count] of Object.entries(data.incidentsByRegion)) {
                const item = document.createElement('div');
                item.className = 'regional-item';
                item.innerHTML = `
                    <span>📍 <strong>${escapeHtml(region)}</strong></span>
                    <span class="badge ${count > 2 ? 'badge-danger' : 'badge-info'}">${count} Incident(s)</span>
                `;
                regionalContainer.appendChild(item);
            }
        }

        setBackendStatus(true);
    } catch (err) {
        console.error('Failed to load health summary:', err);
        setBackendStatus(false);
    }
}

// Load Incidents Table
async function loadIncidents() {
    try {
        const severity = document.getElementById('filter-severity').value;
        const status = document.getElementById('filter-status').value;
        const region = document.getElementById('filter-region').value;

        const params = new URLSearchParams();
        if (severity) params.append('severity', severity);
        if (status) params.append('status', status);
        if (region) params.append('region', region);

        const res = await fetch(`${API_BASE}/incidents?${params.toString()}`);
        if (!res.ok) throw new Error(`HTTP ${res.status}`);
        const data = await res.json();
        incidentsCache = data;

        document.getElementById('incident-count-display').innerText = data.length;
        renderIncidentsTable(data);
    } catch (err) {
        console.error('Failed to load incidents:', err);
        document.getElementById('incidents-table-body').innerHTML = `
            <tr><td colspan="8" class="text-center text-warning">Unable to reach incident service. Check if backend is running.</td></tr>
        `;
    }
}

function renderIncidentsTable(incidents) {
    const tbody = document.getElementById('incidents-table-body');
    if (!incidents || incidents.length === 0) {
        tbody.innerHTML = `<tr><td colspan="8" class="text-center">No matching network incidents recorded. Infrastructure healthy!</td></tr>`;
        return;
    }

    tbody.innerHTML = incidents.map(inc => {
        const severityBadge = getSeverityBadge(inc.severity);
        const statusBadge = getStatusBadge(inc.status);
        const ageText = inc.durationMinutes != null ? `${inc.durationMinutes}m ago` : 'Just now';

        let actionButtons = '';
        if (inc.status === 'OPEN') {
            actionButtons = `
                <button class="btn btn-sm btn-outline" onclick="openStatusModal('${inc.id}', 'ACKNOWLEDGED')">Acknowledge</button>
                <button class="btn btn-sm btn-primary" onclick="openStatusModal('${inc.id}', 'IN_PROGRESS')">Investigate</button>
            `;
        } else if (inc.status === 'ACKNOWLEDGED') {
            actionButtons = `
                <button class="btn btn-sm btn-primary" onclick="openStatusModal('${inc.id}', 'IN_PROGRESS')">Investigate</button>
                <button class="btn btn-sm btn-success" onclick="openStatusModal('${inc.id}', 'RESOLVED')">Resolve</button>
            `;
        } else if (inc.status === 'IN_PROGRESS') {
            actionButtons = `
                <button class="btn btn-sm btn-success" onclick="openStatusModal('${inc.id}', 'RESOLVED')">Resolve</button>
            `;
        } else {
            actionButtons = `<span class="badge badge-success">Closed (${escapeHtml(inc.resolvedBy || 'System')})</span>`;
        }

        return `
            <tr>
                <td><strong>${escapeHtml(inc.id)}</strong></td>
                <td>${severityBadge}</td>
                <td>${statusBadge}</td>
                <td>
                    <div><strong>${escapeHtml(inc.towerId)}</strong> (${escapeHtml(inc.technology || '4G/5G')})</div>
                    <div style="font-size:11px; color:#9ca3af;">${escapeHtml(inc.region)}</div>
                </td>
                <td style="max-width:280px; font-size:12px;">
                    <div><strong>${escapeHtml(inc.title)}</strong></div>
                    <div style="color:#94a3b8;">${escapeHtml(inc.rootCauseAnalysis || inc.description)}</div>
                </td>
                <td style="font-family:'JetBrains Mono', monospace; font-size:12px;">
                    <div>Latency: ${inc.triggerLatencyMs ? inc.triggerLatencyMs.toFixed(1) + 'ms' : 'N/A'}</div>
                    <div>Loss: ${inc.triggerPacketLossPct ? inc.triggerPacketLossPct.toFixed(1) + '%' : 'N/A'}</div>
                </td>
                <td style="font-size:12px; color:#9ca3af;">${ageText}</td>
                <td>
                    <div class="action-btn-group">
                        ${actionButtons}
                        <button class="btn btn-sm btn-outline" onclick="viewAlertsAudit('${inc.id}')">Audit</button>
                    </div>
                </td>
            </tr>
        `;
    }).join('');
}

// Load Telemetry Packets
async function loadTelemetry() {
    try {
        const res = await fetch(`${API_BASE}/network-events?limit=50`);
        if (!res.ok) return;
        const data = await res.json();
        const tbody = document.getElementById('telemetry-table-body');

        if (!data || data.length === 0) {
            tbody.innerHTML = `<tr><td colspan="9" class="text-center">No telemetry packets found. Use the simulator above to stream events.</td></tr>`;
            return;
        }

        tbody.innerHTML = data.map(ev => {
            const timeStr = new Date(ev.timestamp).toLocaleTimeString();
            const latencyColor = ev.latencyMs > 200 ? '#ef4444' : (ev.latencyMs > 100 ? '#f59e0b' : '#10b981');
            const lossColor = ev.packetLossPct > 10 ? '#ef4444' : (ev.packetLossPct > 3 ? '#f59e0b' : '#10b981');

            return `
                <tr>
                    <td style="font-family:'JetBrains Mono';">#${ev.id}</td>
                    <td>${timeStr}</td>
                    <td><strong>${escapeHtml(ev.towerId)}</strong></td>
                    <td style="color:${latencyColor}; font-weight:700;">${ev.latencyMs.toFixed(1)} ms</td>
                    <td style="color:${lossColor}; font-weight:700;">${ev.packetLossPct.toFixed(1)} %</td>
                    <td>${ev.throughputMbps.toFixed(1)} Mbps</td>
                    <td>${ev.activeUsers.toLocaleString()}</td>
                    <td>${ev.signalStrengthDbm ? ev.signalStrengthDbm.toFixed(1) + ' dBm' : 'N/A'}</td>
                    <td><span class="badge ${ev.towerOperationalStatus === 'DOWN' ? 'badge-danger' : (ev.towerOperationalStatus === 'DEGRADED' ? 'badge-warning' : 'badge-success')}">${escapeHtml(ev.towerOperationalStatus)}</span></td>
                </tr>
            `;
        }).join('');
    } catch (err) {
        console.error('Failed to load telemetry:', err);
    }
}

// Load Towers Directory
async function loadTowers() {
    try {
        const res = await fetch(`${API_BASE}/towers`);
        if (!res.ok) return;
        const data = await res.json();
        const grid = document.getElementById('towers-grid-container');

        grid.innerHTML = data.map(t => {
            const statusClass = t.status === 'DOWN' ? 'badge-danger' : (t.status === 'DEGRADED' ? 'badge-warning' : 'badge-success');
            return `
                <div class="tower-card">
                    <div class="tower-top">
                        <span class="tower-id">${escapeHtml(t.id)}</span>
                        <span class="badge ${statusClass}">${escapeHtml(t.status)}</span>
                    </div>
                    <div class="tower-name">${escapeHtml(t.name)}</div>
                    <div class="tower-meta">📍 Region: <strong>${escapeHtml(t.region)}</strong> | Tech: <strong>${escapeHtml(t.technology)}</strong></div>
                    <div class="tower-meta">📶 Capacity: <strong>${t.capacityBandwidthMbps} Mbps</strong></div>
                    <div class="tower-meta">👥 Baseline Users: <strong>${t.activeUsersBaseline ? t.activeUsersBaseline.toLocaleString() : 'N/A'}</strong></div>
                    <div style="font-size:11px; color:#64748b; font-family:'JetBrains Mono';">GPS: (${t.latitude ? t.latitude.toFixed(4) : '--'}, ${t.longitude ? t.longitude.toFixed(4) : '--'})</div>
                </div>
            `;
        }).join('');
    } catch (err) {
        console.error('Failed to load towers:', err);
    }
}

// Simulator Buttons
function initSimButtons() {
    const simButtons = document.querySelectorAll('.sim-btn');
    simButtons.forEach(btn => {
        btn.addEventListener('click', async () => {
            const scenario = btn.getAttribute('data-scenario');
            const tower = btn.getAttribute('data-tower');
            await triggerSimulation(scenario, tower);
        });
    });

    const autoFeedCheckbox = document.getElementById('auto-feed-checkbox');
    autoFeedCheckbox.addEventListener('change', (e) => {
        if (e.target.checked) {
            autoFeedTimer = setInterval(async () => {
                await triggerSimulation('NORMAL', null, true);
            }, 3000);
        } else {
            clearInterval(autoFeedTimer);
            autoFeedTimer = null;
        }
    });
}

async function triggerSimulation(scenario, towerId, silent = false) {
    const feedback = document.getElementById('sim-feedback-message');
    try {
        const res = await fetch(`${API_BASE}/network-events/simulate`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ scenario, towerId })
        });
        if (!res.ok) throw new Error(`HTTP ${res.status}`);
        const event = await res.json();

        if (!silent) {
            feedback.innerText = `[SUCCESS] Ingested ${scenario} on ${event.towerId}: Latency=${event.latencyMs}ms, Loss=${event.packetLossPct}%, Status=${event.towerOperationalStatus}`;
            setTimeout(() => { feedback.innerText = ''; }, 6000);
        }

        // Refresh board
        await fetchAllData();
    } catch (err) {
        console.error('Simulation error:', err);
        feedback.innerText = `[ERROR] Failed to inject simulation: ${err.message}`;
    }
}

// Modals Handling
function initModals() {
    const statusModal = document.getElementById('status-modal');
    document.getElementById('modal-close-btn').addEventListener('click', () => statusModal.classList.remove('open'));
    document.getElementById('modal-cancel-btn').addEventListener('click', () => statusModal.classList.remove('open'));

    document.getElementById('modal-submit-btn').addEventListener('click', async () => {
        const incidentId = document.getElementById('modal-incident-id').value;
        const status = document.getElementById('modal-target-status').value;
        const operator = document.getElementById('modal-operator').value.trim() || 'Shift-Lead-Operator';
        const notes = document.getElementById('modal-notes').value.trim();

        try {
            const res = await fetch(`${API_BASE}/incidents/${incidentId}/status`, {
                method: 'PUT',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ status, operator, notes })
            });

            if (!res.ok) {
                const errJson = await res.json();
                alert(`Status update failed: ${errJson.message || 'Invalid transition'}`);
                return;
            }

            statusModal.classList.remove('open');
            await fetchAllData();
        } catch (err) {
            alert(`Error updating incident: ${err.message}`);
        }
    });

    const alertsModal = document.getElementById('alerts-modal');
    document.getElementById('alerts-modal-close-btn').addEventListener('click', () => alertsModal.classList.remove('open'));
    document.getElementById('alerts-modal-ok-btn').addEventListener('click', () => alertsModal.classList.remove('open'));
}

window.openStatusModal = function(id, defaultStatus) {
    document.getElementById('modal-incident-id').value = id;
    document.getElementById('modal-target-status').value = defaultStatus;
    document.getElementById('status-modal').classList.add('open');
};

window.viewAlertsAudit = async function(incidentId) {
    const alertsModal = document.getElementById('alerts-modal');
    const container = document.getElementById('alerts-list-container');
    container.innerHTML = 'Loading dispatched alert audit trail...';
    alertsModal.classList.add('open');

    try {
        const res = await fetch(`${API_BASE}/incidents/${incidentId}/alerts`);
        if (!res.ok) throw new Error(`HTTP ${res.status}`);
        const alerts = await res.json();

        if (alerts.length === 0) {
            container.innerHTML = '<p>No alerts dispatched for this incident yet.</p>';
            return;
        }

        container.innerHTML = alerts.map(a => `
            <div style="background:#1e293b; padding:12px; border-radius:8px; margin-bottom:10px;">
                <div style="display:flex; justify-content:space-between; margin-bottom:6px;">
                    <span class="badge badge-info">📣 ${escapeHtml(a.channel)}</span>
                    <span style="font-size:11px; color:#94a3b8;">${new Date(a.sentAt).toLocaleString()}</span>
                </div>
                <div style="font-size:13px; font-weight:600; margin-bottom:4px;">To: ${escapeHtml(a.recipient)}</div>
                <div style="font-size:12px; color:#cbd5e1; font-family:'JetBrains Mono';">${escapeHtml(a.message)}</div>
            </div>
        `).join('');
    } catch (err) {
        container.innerHTML = `<p style="color:#ef4444;">Failed to load alerts: ${err.message}</p>`;
    }
};

// Filter Changes
function initFilters() {
    ['filter-severity', 'filter-status', 'filter-region'].forEach(id => {
        document.getElementById(id).addEventListener('change', () => loadIncidents());
    });
}

function initRefreshButton() {
    document.getElementById('refresh-all-btn').addEventListener('click', () => fetchAllData());
}

// Helpers
function getSeverityBadge(severity) {
    switch (severity) {
        case 'CRITICAL': return '<span class="badge badge-danger">CRITICAL</span>';
        case 'HIGH': return '<span class="badge badge-warning">HIGH</span>';
        case 'MEDIUM': return '<span class="badge badge-info">MEDIUM</span>';
        default: return '<span class="badge badge-primary">LOW</span>';
    }
}

function getStatusBadge(status) {
    switch (status) {
        case 'OPEN': return '<span class="badge badge-danger">OPEN</span>';
        case 'ACKNOWLEDGED': return '<span class="badge badge-warning">ACKNOWLEDGED</span>';
        case 'IN_PROGRESS': return '<span class="badge badge-info">IN PROGRESS</span>';
        default: return '<span class="badge badge-success">RESOLVED</span>';
    }
}

function setBackendStatus(online) {
    const dot = document.getElementById('backend-dot');
    const txt = document.getElementById('backend-status-text');
    if (online) {
        dot.className = 'pulse-dot green';
        txt.innerText = 'Connected to Spring Boot (:8080)';
    } else {
        dot.className = 'pulse-dot red';
        txt.innerText = 'Disconnected from Backend (Check Port 8080)';
    }
}

function escapeHtml(str) {
    if (!str) return '';
    return str.replace(/[&<>"']/g, (m) => ({
        '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#039;'
    }[m]));
}
