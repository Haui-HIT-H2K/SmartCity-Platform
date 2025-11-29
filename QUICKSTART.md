# 🎯 Quick Start Guide - Complete Pipeline Test

## Tổng Quan Pipeline

```
python-data-simulator → RabbitMQ → consumer-worker → ml-service → Logs
```

## ✅ Services Đang Chạy

1. **RabbitMQ**: Port 5672, 15672
2. **ml-service**: Port 8000 (Docker container)
3. **consumer-worker**: Port N/A (Docker container)

## 🚀 Cách Test Nhanh

### Option 1: Gửi Test Message (Đơn giản nhất)

```bash
cd /home/hahoang/SmartCity-Platform/consumer-worker

# Gửi 1 message test
docker run --rm --network host \
  -v $(pwd):/app \
  consumer-worker:latest \
  python3 test_send_message.py
```

**Kết quả mong đợi:**
```
✓ Test message sent to RabbitMQ
  Queue: sensor_queue_1
  Message: {
    "sourceId": "SENSOR_TEST_001",
    "payload": {
      "temperature": 28.5,
      "humidity": 65,
      "co2_level": 420
    },
    "timestamp": ...
  }
```

### Option 2: Xem Logs Consumer-Worker

```bash
# Xem logs real-time
docker logs -f consumer-worker-test

# Hoặc xem log mới nhất
docker logs consumer-worker-test --tail 20
```

**Kết quả mong đợi:**
```
[SENSOR_TEST_001] temperature=28.5 → COLD (Normal Temperature Reading)
[SENSOR_TEST_001] humidity=65.0 → COLD (Normal Humidity Reading)
[SENSOR_TEST_001] co2_level=420.0 → HOT (Co2 Anomaly Detected)
```

### Option 3: Chạy Simulator (Full Test)

```bash
# Terminal 1: Monitor consumer logs
docker logs -f consumer-worker-test

# Terminal 2: Run simulator
cd /home/hahoang/SmartCity-Platform/python-data-simulator
python3 main.py
```

## 🔍 Kiểm Tra Services

### RabbitMQ

```bash
# Check container
docker ps | grep rabbitmq

# Web UI
firefox http://localhost:15672
# Username: edge_user / Password: edge_pass
```

### ML Service

```bash
# Check health
curl http://localhost:8000/health

# Test prediction
curl -X POST http://localhost:8000/predict \
  -H "Content-Type: application/json" \
  -d '{"source":"sensor","value":28.5,"metric_type":"temperature"}'
```

### Consumer Worker

```bash
# Check container
docker ps | grep consumer-worker

# View logs
docker logs consumer-worker-test

# Stop/Start
docker stop consumer-worker-test
docker start consumer-worker-test
```

## 🛠️ Troubleshooting

### Issue: Consumer không xử lý messages

```bash
# Restart consumer
docker restart consumer-worker-test

# Check RabbitMQ queue depth
docker exec rabbitmq-test rabbitmqctl list_queues
```

### Issue: ML Service error

```bash
# Check ml-service logs
docker logs ml-service

# Restart ml-service
docker restart ml-service
```

### Issue: RabbitMQ connection error

```bash
# Restart RabbitMQ
docker restart rabbitmq-test

# Wait 10 seconds, then restart consumer
sleep 10
docker restart consumer-worker-test
```

## 📊 Monitoring

### Queue Stats

```bash
# Check queue depth
docker exec rabbitmq-test rabbitmqctl list_queues

# Expected output:
# sensor_queue_1    0  (empty if consumer is processing)
# sensor_queue_1    N  (N messages if not consumed yet)
```

### Consumer Stats

```bash
# Check processing rate
docker logs consumer-worker-test | grep "Stats:"

# Expected:
# 📊 Stats: Processed=100, Failed=0, Rate=15.3 msg/s
```

## 🧪 Test Scenarios

### 1. Normal Values Test

```python
# Send normal sensor data
{
  "temperature": 28.5,  # → COLD (Normal)
  "humidity": 65,       # → COLD (Normal)
  "co2_level": 420      # → Depends on model
}
```

### 2. Anomaly Test

```python
# Send abnormal values
{
  "temperature": 100,   # → HOT (Anomaly)
  "humidity": 5,        # → HOT (Anomaly)
  "co2_level": 2000     # → HOT (Anomaly)
}
```

## 🐳 Docker Commands Reference

```bash
# Start all services
docker start rabbitmq-test ml-service consumer-worker-test

# Stop all services
docker stop consumer-worker-test ml-service rabbitmq-test

# Remove containers
docker rm consumer-worker-test ml-service rabbitmq-test

# Rebuild consumer-worker
cd /home/hahoang/SmartCity-Platform/consumer-worker
docker build -t consumer-worker:latest .

# Restart with new build
docker stop consumer-worker-test
docker rm consumer-worker-test
docker run -d --name consumer-worker-test --network host \
  -e RABBITMQ_HOST_1=localhost \
  -e ML_SERVICE_URL=http://localhost:8000 \
  consumer-worker:latest
```

## 📝 Summary

**Current Status:**
- ✅ RabbitMQ: Running on port 5672
- ✅ ml-service: Running on port 8000
- ✅ consumer-worker: Running and connected
- ✅ Test message sent successfully

**Next Steps:**
1. Monitor consumer logs để xem predictions
2. Chạy simulator để test với volume lớn
3. Check statistics và performance

---

**Last Updated**: 2025-11-28  
**Pipeline Status**: ✅ Fully Operational
