---
sidebar_position: 1
title: Docker Deployment
---

# Docker Deployment

## Tổng quan

SmartCity-Platform sử dụng **Docker Compose** để triển khai toàn bộ hệ thống với **single command deployment**. Tất cả services được containerized và orchestrated thông qua `docker-compose.yml`.

## Kiến trúc Container

```mermaid
graph TB
    subgraph "Docker Network: smartcity-network"
        subgraph "Edge Layer"
            A[rabbitmq-edge-1<br/>Port: 5672, 15672]
            B[rabbitmq-edge-2<br/>Port: 5673, 15673]
        end
        
        subgraph "Storage Layer"
            C[core-redis-hot<br/>Port: 6379]
            D[core-mongo-warm<br/>Port: 27018]
            E[core-mongo-cold<br/>Port: 27019]
        end
        
        subgraph "Processing Layer"
            F[smart-city-backend<br/>Port: 8080]
            G[smart-city-ml<br/>Port: 8000]
        end
        
        subgraph "Presentation Layer"
            H[smart-city-frontend<br/>Port: 3000]
        end
    end
    
    F --> A & B & C & D & E & G
    H --> F
    
    style F fill:#90EE90
    style G fill:#FFB6C1
    style H fill:#FFD700
```

## Prerequisites

### Yêu cầu Hệ thống

**Minimum:**
- Docker Engine 20.x+
- Docker Compose 2.x+
- RAM: 8GB
- Disk: 20GB free space
- CPU: 2 cores

**Recommended:**
- Docker Engine 24.x+
- Docker Compose 2.20+
- RAM: 16GB
- Disk: 50GB SSD
- CPU: 4+ cores

### Cài đặt Docker

**Windows:**
```powershell
# Install Docker Desktop
# Download from: https://www.docker.com/products/docker-desktop

# Verify installation
docker --version
docker-compose --version
```

**Linux (Ubuntu/Debian):**
```bash
# Install Docker
curl -fsSL https://get.docker.com -o get-docker.sh
sudo sh get-docker.sh

# Install Docker Compose
sudo apt-get update
sudo apt-get install docker-compose-plugin

# Add user to docker group
sudo usermod -aG docker $USER
newgrp docker

# Verify
docker --version
docker compose version
```

## Docker Compose Configuration

### File Structure

```
SmartCity-Platform/
├── docker-compose.yml         # Main orchestration file
├── backend/
│   ├── Dockerfile
│   └── target/
│       └── smart-city-backend.jar
├── ml-service/
│   ├── Dockerfile
│   ├── app/
│   └── requirements.txt
├── frontend/
│   ├── Dockerfile
│   └── .output/
├── python-data-simulator/
│   ├── Dockerfile
│   └── simulator.py
└── .env                       # Environment variables (optional)
```

### Services Overview

| Service | Image | Port(s) | Purpose |
|---------|-------|---------|---------|
| rabbitmq-edge-1 | rabbitmq:3-management | 5672, 15672 | Message queue node 1 |
| rabbitmq-edge-2 | rabbitmq:3-management | 5673, 15673 | Message queue node 2 |
| core-redis-hot | redis:alpine | 6379 | HOT tier storage |
| core-mongo-warm | mongo:7.0 | 27018 | WARM tier database |
| core-mongo-cold | mongo:7.0 | 27019 | COLD tier database |
| smart-city-ml | custom | 8000 | ML classification service |
| smart-city-backend | custom | 8080 | Spring Boot backend |
| smart-city-frontend | custom | 3000 | NuxtJS frontend |

## Deployment

### Quick Start (One Command)

```bash
# Navigate to project root
cd SmartCity-Platform

# Start all services
docker-compose up -d

# View logs
docker-compose logs -f

# Check status
docker-compose ps
```

### Step-by-Step Deployment

**1. Build Backend**
```bash
cd backend
mvn clean package -DskipTests
cd ..
```

**2. Build Frontend**
```bash
cd frontend
npm install
npm run build
cd ..
```

**3. Start Infrastructure Services**
```bash
# Start databases and message queues first
docker-compose up -d rabbitmq-edge-1 rabbitmq-edge-2
docker-compose up -d core-redis-hot core-mongo-warm core-mongo-cold

# Wait for services to be healthy
sleep 10
```

**4. Start Application Services**
```bash
# Start ML service
docker-compose up -d smart-city-ml

# Wait for ML models to load
sleep 5

# Start backend
docker-compose up -d smart-city-backend

# Wait for backend to be ready
sleep 10

# Start frontend
docker-compose up -d smart-city-frontend
```

**5. Verify Deployment**
```bash
# Check all services are running
docker-compose ps

# Test endpoints
curl http://localhost:8080/actuator/health  # Backend
curl http://localhost:8000/health           # ML Service
curl http://localhost:3000                   # Frontend
```

## Service Configuration

### Environment Variables

**Backend (smart-city-backend):**
```yaml
environment:
  - SPRING_PROFILES_ACTIVE=docker
  - MONGODB_WARM_URI=mongodb://admin:password123@core-mongo-warm:27017/warm_db?authSource=admin
  - MONGODB_COLD_URI=mongodb://admin:password123@core-mongo-cold:27017/cold_db?authSource=admin
  - REDIS_HOST=core-redis-hot
  - RABBITMQ_ADDRESSES=rabbitmq-edge-1:5672,rabbitmq-edge-2:5673
  - ML_SERVICE_URL=http://smart-city-ml:8000
```

**RabbitMQ:**
```yaml
environment:
  - RABBITMQ_DEFAULT_USER=edge_user
  - RABBITMQ_DEFAULT_PASS=edge_pass
```

**MongoDB:**
```yaml
environment:
  - MONGO_INITDB_ROOT_USERNAME=admin
  - MONGO_INITDB_ROOT_PASSWORD=password123
```

### Volume Mounting

**Persistent Volumes:**
```yaml
volumes:
  rabbitmq_edge1_data:
    driver: local
  rabbitmq_edge2_data:
    driver: local
  redis_hot_data:
    driver: local
  mongo_warm_data:
    driver: local
  mongo_cold_data:
    driver: local
```

**Mount Points:**
- RabbitMQ: `/var/lib/rabbitmq`
- Redis: `/data`
- MongoDB: `/data/db`

### Networking

**Bridge Network:**
```yaml
networks:
  smartcity-network:
    driver: bridge
```

**Service Discovery:**
- Services communicate via container names (e.g., `rabbitmq-edge-1`, `core-redis-hot`)
- DNS resolution handled by Docker network
- No need for IP addresses

## Container Management

### Start/Stop Services

```bash
# Start all
docker-compose up -d

# Start specific service
docker-compose up -d smart-city-backend

# Stop all
docker-compose down

# Stop but keep volumes
docker-compose stop

# Restart service
docker-compose restart smart-city-backend
```

### Logs và Monitoring

```bash
# View all logs
docker-compose logs -f

# View specific service logs
docker-compose logs -f smart-city-backend

# Last 100 lines
docker-compose logs --tail=100 smart-city-ml

# Follow logs from specific time
docker-compose logs --since 30m smart-city-frontend
```

### Resource Management

```bash
# View resource usage
docker stats

# Limit resources in docker-compose.yml
services:
  smart-city-backend:
    deploy:
      resources:
        limits:
          cpus: '2'
          memory: 2G
        reservations:
          cpus: '1'
          memory: 1G
```

### Health Checks

```bash
# Check container health
docker-compose ps

# Detailed health status
docker inspect smart-city-backend | jq '.[0].State.Health'

# Custom health check in docker-compose.yml
services:
  smart-city-backend:
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8080/actuator/health"]
      interval: 30s
      timeout: 10s
      retries: 3
      start_period: 40s
```

## Data Management

### Backup

**MongoDB Backup:**
```bash
# Backup WARM database
docker exec core-mongo-warm mongodump \
  --username admin \
  --password password123 \
  --authenticationDatabase admin \
  --db warm_db \
  --out /backup

# Copy backup from container
docker cp core-mongo-warm:/backup ./backup-warm
```

**Redis Backup:**
```bash
# Trigger Redis save
docker exec core-redis-hot redis-cli SAVE

# Copy RDB file
docker cp core-redis-hot:/data/dump.rdb ./backup-redis/
```

### Restore

**MongoDB Restore:**
```bash
# Copy backup to container
docker cp ./backup-warm core-mongo-warm:/backup

# Restore
docker exec core-mongo-warm mongorestore \
  --username admin \
  --password password123 \
  --authenticationDatabase admin \
  --db warm_db \
  /backup/warm_db
```

### Reset All Data

```bash
# WARNING: Destructive operation!

# Stop all services
docker-compose down

# Remove all volumes
docker volume rm $(docker volume ls -q | grep smartcity)

# Restart
docker-compose up -d
```

## Troubleshooting

### Common Issues

**1. Port Already in Use**
```bash
# Find process using port
netstat -ano | findstr :8080  # Windows
lsof -i :8080                  # Linux/Mac

# Kill process or change port in docker-compose.yml
```

**2. Container Won't Start**
```bash
# Check logs
docker-compose logs smart-city-backend

# Check container status
docker inspect smart-city-backend

# Force recreate
docker-compose up -d --force-recreate smart-city-backend
```

**3. Network Issues**
```bash
# Recreate network
docker-compose down
docker network prune
docker-compose up -d
```

**4. Out of Disk Space**
```bash
# Clean up
docker system prune -a --volumes

# Check disk usage
docker system df
```

### Debug Mode

```bash
# Run in foreground with logs
docker-compose up

# Run single service in debug mode
docker-compose run --rm smart-city-backend

# Access container shell
docker exec -it smart-city-backend /bin/bash
docker exec -it core-mongo-warm mongosh
```

## Production Deployment

### Security Hardening

**1. Change Default Passwords**
```yaml
# Use environment file
environment:
  - MONGO_INITDB_ROOT_PASSWORD=${MONGO_PASSWORD}
  - RABBITMQ_DEFAULT_PASS=${RABBITMQ_PASSWORD}
```

**2. Network Isolation**
```yaml
# Separate networks for internal/external
networks:
  frontend-network:
    driver: bridge
  backend-network:
    driver: bridge
    internal: true
```

**3. Resource Limits**
```yaml
# Set limits for all services
deploy:
  resources:
    limits:
      memory: 2G
```

### Performance Tuning

**MongoDB:**
```yaml
command: mongod --wiredTigerCacheSizeGB 2
```

**Redis:**
```yaml
command: redis-server --maxmemory 512mb --maxmemory-policy allkeys-lru
```

**RabbitMQ:**
```yaml
environment:
  - RABBITMQ_VM_MEMORY_HIGH_WATERMARK=2GB
```

## CI/CD Integration

### GitHub Actions Example

```yaml
name: Deploy to Production

on:
  push:
    branches: [ main ]

jobs:
  deploy:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      
      - name: Build and Deploy
        run: |
          docker-compose build
          docker-compose up -d
      
      - name: Health Check
        run: |
          sleep 30
          curl -f http://localhost:8080/actuator/health
```

## Monitoring

### Prometheus + Grafana (Optional)

```yaml
# Add to docker-compose.yml
services:
  prometheus:
    image: prom/prometheus
    ports:
      - "9090:9090"
    volumes:
      - ./prometheus.yml:/etc/prometheus/prometheus.yml
  
  grafana:
    image: grafana/grafana
    ports:
      - "3001:3000"
```

## Summary

**Quick Reference:**

```bash
# Deploy everything
docker-compose up -d

# View status
docker-compose ps

# View logs
docker-compose logs -f

# Stop everything
docker-compose down

# Clean up
docker-compose down -v
```

**Access Points:**
- Frontend: http://localhost:3000
- Backend API: http://localhost:8080
- ML Service: http://localhost:8000
- RabbitMQ Management: http://localhost:15672 (edge_user / edge_pass)
