const express = require('express');
const path = require('path');
const cors = require('cors');
const http = require('http');

const app = express();
const PORT = process.env.PORT || 3000;
const BACKEND_URL = process.env.BACKEND_URL || 'http://localhost:8080';

app.use(cors());
app.use(express.json());
app.use(express.static(path.join(__dirname, 'public')));

// Node.js Backend-For-Frontend (BFF) Gateway Proxy
app.all('/api/*', (req, res) => {
    const targetUrl = `${BACKEND_URL}${req.originalUrl}`;
    const parsedUrl = new URL(targetUrl);

    const options = {
        hostname: parsedUrl.hostname,
        port: parsedUrl.port || 80,
        path: parsedUrl.pathname + parsedUrl.search,
        method: req.method,
        headers: {
            ...req.headers,
            host: parsedUrl.host
        }
    };

    const proxyReq = http.request(options, (backendRes) => {
        res.writeHead(backendRes.statusCode, backendRes.headers);
        backendRes.pipe(res, { end: true });
    });

    proxyReq.on('error', (err) => {
        console.error(`[Node Gateway] Error connecting to Spring Boot backend at ${BACKEND_URL}:`, err.message);
        res.status(503).json({
            error: 'Backend Service Unavailable',
            message: `Node.js Gateway could not reach Spring Boot backend on ${BACKEND_URL}. Ensure Spring Boot is running on port 8080.`,
            details: err.message
        });
    });

    if (['POST', 'PUT', 'PATCH'].includes(req.method) && req.body) {
        const bodyData = JSON.stringify(req.body);
        proxyReq.setHeader('Content-Type', 'application/json');
        proxyReq.setHeader('Content-Length', Buffer.byteLength(bodyData));
        proxyReq.write(bodyData);
    }

    proxyReq.end();
});

// Single Page Application Fallback Route
app.get('*', (req, res) => {
    res.sendFile(path.join(__dirname, 'public', 'index.html'));
});

app.listen(PORT, () => {
    console.log(`=======================================================`);
    console.log(` Telecom Operations Dashboard (Node.js Gateway)`);
    console.log(` Running on: http://localhost:${PORT}`);
    console.log(` Forwarding API calls to Spring Boot on: ${BACKEND_URL}`);
    console.log(`=======================================================`);
});
