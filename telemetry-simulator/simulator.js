/**
 * Telecom Network Telemetry Simulator (Node.js)
 * Emits continuous realistic network telemetry metrics to the Spring Boot ingestion API
 * Simulates normal network background traffic and stochastic anomalies (fiber cuts, congestion, power loss).
 */

const http = require('http');

const API_HOST = process.env.BACKEND_HOST || 'localhost';
const API_PORT = process.env.BACKEND_PORT || 8080;
const EMIT_INTERVAL_MS = parseInt(process.env.INTERVAL_MS || '4000', 10);

const TOWERS = [
    { id: 'HYD-4521', region: 'Hyderabad', tech: '5G', baselineUsers: 12000 },
    { id: 'HYD-1092', region: 'Hyderabad', tech: '5G', baselineUsers: 9500 },
    { id: 'BLR-1001', region: 'Bengaluru', tech: '5G', baselineUsers: 14000 },
    { id: 'BLR-2045', region: 'Bengaluru', tech: '4G', baselineUsers: 8500 },
    { id: 'MUM-3012', region: 'Mumbai', tech: '5G', baselineUsers: 18000 },
    { id: 'MUM-4109', region: 'Mumbai', tech: '4G', baselineUsers: 11000 },
    { id: 'DEL-5001', region: 'Delhi NCR', tech: '5G', baselineUsers: 16000 },
    { id: 'DEL-5088', region: 'Delhi NCR', tech: '5G', baselineUsers: 15000 },
    { id: 'CHN-6020', region: 'Chennai', tech: '5G', baselineUsers: 9000 },
    { id: 'PUN-7014', region: 'Pune', tech: '4G', baselineUsers: 7500 }
];

console.log('================================================================');
console.log(' TELECOM NETWORK TELEMETRY STREAM SIMULATOR (NODE.JS)');
console.log(` Target Ingestion API: http://${API_HOST}:${API_PORT}/api/network-events`);
console.log(` Interval: ${EMIT_INTERVAL_MS} ms`);
console.log(' Emitting realistic operational metrics across Metro Towers...');
console.log('================================================================\n');

function getRandomInt(min, max) {
    return Math.floor(Math.random() * (max - min + 1)) + min;
}

function getRandomFloat(min, max, decimals = 1) {
    const val = (Math.random() * (max - min)) + min;
    return parseFloat(val.toFixed(decimals));
}

function generateTelemetryPacket() {
    const tower = TOWERS[getRandomInt(0, TOWERS.length - 1)];
    const roll = Math.random();

    let latencyMs;
    let packetLossPct;
    let throughputMbps;
    let activeUsers;
    let signalDbm;
    let jitterMs;
    let status = 'ACTIVE';

    if (roll < 0.08) {
        // 8% chance of Critical Optical Fiber Cut anomaly
        console.log(`\n💥 [SIMULATOR CHAOS INJECTION] Fiber Cut Anomaly on ${tower.id} (${tower.region})`);
        latencyMs = getRandomFloat(280.0, 420.0);
        packetLossPct = getRandomFloat(22.0, 45.0);
        throughputMbps = getRandomFloat(4.0, 12.0);
        activeUsers = Math.floor(tower.baselineUsers * getRandomFloat(1.1, 1.4));
        signalDbm = getRandomFloat(-112.0, -104.0);
        jitterMs = getRandomFloat(35.0, 65.0);
        status = 'DEGRADED';
    } else if (roll < 0.16) {
        // 8% chance of High Traffic Congestion anomaly
        console.log(`\n⚠️ [SIMULATOR CHAOS INJECTION] Congestion Anomaly on ${tower.id} (${tower.region})`);
        latencyMs = getRandomFloat(210.0, 260.0);
        packetLossPct = getRandomFloat(12.5, 19.0);
        throughputMbps = getRandomFloat(14.0, 28.0);
        activeUsers = Math.floor(tower.baselineUsers * getRandomFloat(1.3, 1.6));
        signalDbm = getRandomFloat(-96.0, -90.0);
        jitterMs = getRandomFloat(22.0, 38.0);
        status = 'DEGRADED';
    } else {
        // 84% Normal baseline operational telemetry
        latencyMs = tower.tech === '5G' ? getRandomFloat(20.0, 55.0) : getRandomFloat(45.0, 85.0);
        packetLossPct = getRandomFloat(0.1, 1.2);
        throughputMbps = tower.tech === '5G' ? getRandomFloat(120.0, 380.0) : getRandomFloat(45.0, 95.0);
        activeUsers = Math.floor(tower.baselineUsers * getRandomFloat(0.7, 1.05));
        signalDbm = getRandomFloat(-78.0, -68.0);
        jitterMs = getRandomFloat(2.0, 7.0);
        status = 'ACTIVE';
    }

    return {
        towerId: tower.id,
        timestamp: new Date().toISOString(),
        latencyMs,
        packetLossPct,
        throughputMbps,
        activeUsers,
        signalStrengthDbm: signalDbm,
        jitterMs,
        towerOperationalStatus: status
    };
}

function sendTelemetry(packet) {
    const payload = JSON.stringify(packet);

    const options = {
        hostname: API_HOST,
        port: API_PORT,
        path: '/api/network-events',
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            'Content-Length': Buffer.byteLength(payload)
        }
    };

    const req = http.request(options, (res) => {
        let responseBody = '';
        res.on('data', chunk => { responseBody += chunk; });
        res.on('end', () => {
            if (res.statusCode === 201) {
                const badge = packet.towerOperationalStatus === 'DEGRADED' ? '⚠️ DEGRADED' : '✅ OK';
                console.log(`[${new Date().toLocaleTimeString()}] Sent ${packet.towerId} (${badge}) | Latency: ${packet.latencyMs}ms | Loss: ${packet.packetLossPct}% | Users: ${packet.activeUsers}`);
            } else {
                console.warn(`[WARN] Backend returned status ${res.statusCode}: ${responseBody}`);
            }
        });
    });

    req.on('error', (err) => {
        console.error(`[CONN ERROR] Could not connect to Spring Boot backend at ${API_HOST}:${API_PORT} - is it running? (${err.message})`);
    });

    req.write(payload);
    req.end();
}

// Start emission interval
setInterval(() => {
    const packet = generateTelemetryPacket();
    sendTelemetry(packet);
}, EMIT_INTERVAL_MS);
