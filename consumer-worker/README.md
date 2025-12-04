SPDX-License-Identifier: Apache-2.0
<!--

  Copyright 2025 Haui.HIT - H2K

  Licensed under the Apache License, Version 2.0

  http://www.apache.org/licenses/LICENSE-2.0

-->

# Consumer Worker

RabbitMQ consumer service that bridges **python-data-simulator** and **ml-service** for Smart City sensor anomaly detection.

## 🎯 Purpose

Consumes sensor data from RabbitMQ queues, transforms the schema, and sends prediction requests to ml-service.

```
python-data-simulator → RabbitMQ → consumer-worker → ml-service
```

---

## 📊 Data Flow

**Input (from RabbitMQ):**
```json
{
  "sourceId": "SENSOR_042",
  "payload": {
    "temperature": 28.3,
    "humidity": 65,
    "co2_level": 420
  },
  "timestamp": 1700000000000
}
```

**Processing:**
- Transform to 3 API requests (temperature, humidity, CO2)
- Call ml-service for each metric
- Log predictions

**Output (to console/logs):**
```
[SENSOR_042] temperature=28.3 → COLD (Normal Temperature Reading)
[SENSOR_042] humidity=65.0 → COLD (Normal Humidity Reading)
[SENSOR_042] co2_level=420.0 → HOT (Co2 Anomaly Detected)
```

---

## 🚀 Quick Start

### Option 1: Docker (Recommended)

```bash
# Build image
docker build -t consumer-worker:latest .

# Run worker
docker run --rm --name consumer-worker \
  -e ML_SERVICE_URL=http://host.docker.internal:8000 \
  -e RABBITMQ_HOST_1=host.docker.internal \
  consumer-worker:latest
```

### Option 2: Local Python

```bash
# Install dependencies
pip install -r requirements.txt

# Configure (optional)
cp .env.example .env
# Edit .env with your settings

# Run worker
python3 main.py
```

---

## ⚙️ Configuration

### Environment Variables

Create `.env` file or set environment variables:

```bash
# RabbitMQ Settings
RABBITMQ_HOST_1=localhost
RABBITMQ_PORT_1=5672
RABBITMQ_QUEUE_1=sensor_queue_1
RABBITMQ_USER=edge_user
RABBITMQ_PASS=edge_pass

# Which queue to consume (1 or 2)
CONSUME_QUEUE=1

# ML Service
ML_SERVICE_URL=http://localhost:8000

# Performance
PREFETCH_COUNT=10

# Logging
LOG_LEVEL=INFO
```

---

## 📁 Project Structure

```
consumer-worker/
├── main.py              # Entry point
├── consumer.py          # RabbitMQ consumer
├── transformer.py       # Schema transformation
├── ml_client.py         # ML service HTTP client
├── config.py            # Configuration
├── requirements.txt     # Dependencies
├── Dockerfile          # Container config
├── .env.example        # Config template
└── README.md          # This file
```

---

## 🧪 Testing

### Prerequisites

Start required services:

```bash
# 1. RabbitMQ (Docker or local)
docker run -d --name rabbitmq \
  -p 5672:5672 -p 15672:15672 \
  -e RABBITMQ_DEFAULT_USER=edge_user \
  -e RABBITMQ_DEFAULT_PASS=edge_pass \
  rabbitmq:3-management

# 2. ML Service
cd ../ml-service
docker run -d -p 8000:8000 --name ml-service ml-service:latest

# 3. Verify ml-service health
curl http://localhost:8000/health
```

### Run Consumer

```bash
cd consumer-worker
python3 main.py
```

**Expected output:**
```
============================================================
CONSUMER WORKER CONFIGURATION
============================================================
RabbitMQ Host:  localhost:5672
RabbitMQ Queue: sensor_queue_1
RabbitMQ User:  edge_user
ML Service:     http://localhost:8000
Prefetch Count: 10
Log Level:      INFO
============================================================
Checking ml-service availability...
✓ ML Service healthy - 3 models loaded
✓ Connected to RabbitMQ: localhost:5672 → sensor_queue_1
🚀 Starting to consume from queue: sensor_queue_1
Waiting for messages... (Press Ctrl+C to stop)
```

### Test with Simulator

In another terminal:

```bash
cd ../python-data-simulator
python3 main.py
```

**Expected consumer logs:**
```
[SENSOR_001] temperature=28.3 → COLD (Normal Temperature Reading)
[SENSOR_001] humidity=65.0 → COLD (Normal Humidity Reading)
[SENSOR_001] co2_level=420.0 → HOT (Co2 Anomaly Detected)
[SENSOR_002] temperature=32.1 → COLD (Normal Temperature Reading)
...
📊 Stats: Processed=100, Failed=0, Rate=15.3 msg/s
```

---

## 🔧 Features

### ✅ Robust Error Handling

- Auto-retry on connection failures
- Exponential backoff for HTTP requests
- Message requeuing on transient errors
- Graceful shutdown on SIGTERM/SIGINT

### ✅ Performance

- Prefetch limit for flow control
- HTTP connection pooling
- Batch processing capability
- Statistics tracking

### ✅ Monitoring

- Processing rate (msg/s)
- Success/failure counts
- Detailed logging

---

## 📈 Performance Tuning

### Prefetch Count

Control how many messages to fetch:

```bash
# Low throughput, safer
PREFETCH_COUNT=1

# Balanced (default)
PREFETCH_COUNT=10

# High throughput
PREFETCH_COUNT=50
```

### Concurrency

Run multiple workers for different queues:

```bash
# Worker 1 - Queue 1
CONSUME_QUEUE=1 python3 main.py &

# Worker 2 - Queue 2
CONSUME_QUEUE=2 python3 main.py &
```

---

## 🐳 Docker Compose

Create `docker-compose.yml` in project root:

```yaml
version: '3.8'

services:
  rabbitmq:
    image: rabbitmq:3-management
    ports:
      - "5672:5672"
      - "15672:15672"
    environment:
      RABBITMQ_DEFAULT_USER: edge_user
      RABBITMQ_DEFAULT_PASS: edge_pass

  ml-service:
    build: ./ml-service
    ports:
      - "8000:8000"
    depends_on:
      - rabbitmq

  consumer-worker:
    build: ./consumer-worker
    depends_on:
      - rabbitmq
      - ml-service
    environment:
      RABBITMQ_HOST_1: rabbitmq
      ML_SERVICE_URL: http://ml-service:8000
      CONSUME_QUEUE: 1
```

Run entire stack:

```bash
docker-compose up
```

---

## 🛠️ Troubleshooting

### Consumer can't connect to RabbitMQ

```bash
# Check RabbitMQ is running
docker ps | grep rabbitmq

# Check queue exists
docker exec rabbitmq rabbitmqctl list_queues
```

### ML Service connection error

```bash
# Verify ml-service is running
curl http://localhost:8000/health

# Check network connectivity from Docker
docker run --rm curlimages/curl http://host.docker.internal:8000/health
```

### Messages not being consumed

```bash
# Check queue has messages
docker exec rabbitmq rabbitmqctl list_queues

# Check consumer is connected
docker exec rabbitmq rabbitmqctl list_consumers
```

---

## 📊 Monitoring

### Logs

```bash
# Follow logs
docker logs -f consumer-worker

# Filter for errors
docker logs consumer-worker | grep ERROR

# Statistics
docker logs consumer-worker | grep "Stats:"
```

### RabbitMQ Management UI

Access at: http://localhost:15672
- Username: `edge_user`
- Password: `edge_pass`

Monitor:
- Queue depth
- Consumer count
- Message rate

---

## 🚀 Future Enhancements

- [ ] Database storage for predictions
- [ ] Prometheus metrics export
- [ ] Dead letter queue handling
- [ ] Async processing with asyncio
- [ ] Batch predictions (reduce API calls)

---

## 📄 License

Apache 2.0 License

---

**Last Updated**: 2025-11-28  
**Status**: ✅ Production Ready
