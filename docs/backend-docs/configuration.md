---
sidebar_position: 4
title: Configuration
---

# Configuration Guide

## application.yml Structure

```yaml
spring:
  profiles:
    active: docker  # Options: local, docker, prod
  
  application:
    name: smart-city-platform
  
  # MongoDB Configuration
  data:
    mongodb:
      warm:
        uri: mongodb://admin:password123@core-mongo-warm:27017/warm_db?authSource=admin
      cold:
        uri: mongodb://admin:password123@core-mongo-cold:27017/cold_db?authSource=admin
  
  # Redis Configuration
  redis:
    host: core-redis-hot
    port: 6379
    timeout: 60000
  
  # RabbitMQ Configuration
  rabbitmq:
    addresses: rabbitmq-edge-1:5672,rabbitmq-edge-2:5673
    username: edge_user
    password: edge_pass
    template:
      receive-timeout: 2000

# ML Service Configuration
ml:
  service:
    url: http://smart-city-ml:8000
    timeout: 5000  # milliseconds

# Data Ingestion Configuration
ingestion:
  batch:
    size: 1000
    max-size: 5000
  schedule:
    fixed-rate: 10000  # milliseconds (10 seconds)

# Storage Configuration
storage:
  hot:
    ttl-seconds: 3600  # 1 hour
  warm:
    retention-days: 30
  cold:
    retention-days: 365

# Server Configuration
server:
  port: 8080

# Logging Configuration
logging:
  level:
    root: INFO
    com.smartcity: DEBUG
```

---

## Environment Variables

### MongoDB

```bash
# WARM Tier
MONGODB_WARM_URI=mongodb://admin:password123@core-mongo-warm:27017/warm_db?authSource=admin

# COLD Tier
MONGODB_COLD_URI=mongodb://admin:password123@core-mongo-cold:27017/cold_db?authSource=admin
```

### Redis

```bash
REDIS_HOST=core-redis-hot
REDIS_PORT=6379
```

### RabbitMQ

```bash
RABBITMQ_ADDRESSES=rabbitmq-edge-1:5672,rabbitmq-edge-2:5673
RABBITMQ_USERNAME=edge_user
RABBITMQ_PASSWORD=edge_pass
```

### ML Service

```bash
ML_SERVICE_URL=http://smart-city-ml:8000
ML_SERVICE_TIMEOUT=5000
```

### Application

```bash
SERVER_PORT=8080
SPRING_PROFILES_ACTIVE=docker
```

---

## Profile Configurations

### local Profile (application-local.yml)

```yaml
spring:
  data:
    mongodb:
      warm:
        uri: mongodb://localhost:27018/warm_db
      cold:
        uri: mongodb://localhost:27019/cold_db
  redis:
    host: localhost
    port: 6379
  rabbitmq:
    addresses: localhost:5672,localhost:5673

ml:
  service:
    url: http://localhost:8000
```

### docker Profile (application-docker.yml)

```yaml
spring:
  data:
    mongodb:
      warm:
        uri: mongodb://admin:password123@core-mongo-warm:27017/warm_db?authSource=admin
      cold:
        uri: mongodb://admin:password123@core-mongo-cold:27017/cold_db?authSource=admin
  redis:
    host: core-redis-hot
  rabbitmq:
    addresses: rabbitmq-edge-1:5672,rabbitmq-edge-2:5673

ml:
  service:
    url: http://smart-city-ml:8000
```

---

## Configuration Properties Reference

### Ingestion Properties

| Property | Type | Default | Description |
|----------|------|---------|-------------|
| `ingestion.batch.size` | int | 1000 | Batch size cho mỗi pull |
| `ingestion.batch.max-size` | int | 5000 | Maximum batch size |
| `ingestion.schedule.fixed-rate` | long | 10000 | Pull interval (ms) |

### Storage Properties

| Property | Type | Default | Description |
|----------|------|---------|-------------|
| `storage.hot.ttl-seconds` | int | 3600 | Redis TTL (seconds) |
| `storage.warm.retention-days` | int | 30 | MongoDB WARM retention |
| `storage.cold.retention-days` | int | 365 | MongoDB COLD retention |

### ML Service Properties

| Property | Type | Default | Description |
|----------|------|---------|-------------|
| `ml.service.url` | string | - | ML Service base URL |
| `ml.service.timeout` | int | 5000 | Request timeout (ms) |

---

## Docker Compose Configuration

```yaml
services:
  smart-city-backend:
    image: smart-city-backend:latest
    environment:
      - SPRING_PROFILES_ACTIVE=docker
      - MONGODB_WARM_URI=mongodb://admin:password123@core-mongo-warm:27017/warm_db?authSource=admin
      - MONGODB_COLD_URI=mongodb://admin:password123@core-mongo-cold:27017/cold_db?authSource=admin
      - REDIS_HOST=core-redis-hot
      - RABBITMQ_ADDRESSES=rabbitmq-edge-1:5672,rabbitmq-edge-2:5673
      - ML_SERVICE_URL=http://smart-city-ml:8000
    ports:
      - "8080:8080"
    depends_on:
      - rabbitmq-edge-1
      - rabbitmq-edge-2
      - core-redis-hot
      - core-mongo-warm
      - core-mongo-cold
      - smart-city-ml
    networks:
      - smartcity-network
```

---

## Tuning Guide

### High Throughput

Để tăng throughput, adjust các parameters sau:

```yaml
ingestion:
  batch:
    size: 5000      # Tăng batch size
    max-size: 10000
  schedule:
    fixed-rate: 5000  # Giảm interval (pull frequency hơn)
```

### Low Memory

Nếu memory hạn chế:

```yaml
ingestion:
  batch:
    size: 500       # Giảm batch size
    max-size: 1000
    
storage:
  hot:
    ttl-seconds: 1800  # Giảm TTL (30 phút)
```

### Production Deployment

```yaml
logging:
  level:
    root: WARN
    com.smartcity: INFO  # Giảm logging

storage:
  warm:
    retention-days: 90   # Tăng retention
  cold:
    retention-days: 730  # 2 years
```
