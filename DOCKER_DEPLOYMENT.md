# Smart City Platform - Docker Deployment Guide

## 🚀 Quick Start

Chỉ cần 1 lệnh để chạy toàn bộ hệ thống:

```bash
docker-compose up -d --build
```

Đợi khoảng 2-3 phút để tất cả services khởi động, sau đó:
- **Frontend**: http://localhost:3000
- **Backend API**: http://localhost:8080
- **ML Service**: http://localhost:8000
- **RabbitMQ Management**: http://localhost:15672 (user: `edge_user`, pass: `edge_pass`)
- **Mongo Express**: http://localhost:8081

## 📦 Services Overview

Hệ thống bao gồm 9 containers:

| Service | Container Name | Port | Description |
|---------|---------------|------|-------------|
| **Frontend** | smart-city-frontend | 3000 | Nuxt.js Dashboard |
| **Backend** | smart-city-backend | 8080 | Spring Boot API |
| **ML Service** | smart-city-ml | 8000 | FastAPI Anomaly Detection |
| **RabbitMQ Edge-1** | rabbit-edge-1 | 5672, 15672 | Message Queue khu vực A |
| **RabbitMQ Edge-2** | rabbit-edge-2 | 5673, 15673 | Message Queue khu vực B |
| **Redis** | core-redis-hot | 6379 | HOT Storage (In-Memory) |
| **MongoDB Warm** | core-mongo-warm | 27018 | WARM Storage |
| **MongoDB Cold** | core-mongo-cold | 27019 | COLD Storage |
| **Mongo Express** | core-ui | 8081 | MongoDB Web UI |

## 🏗️ Build Details

### Backend (Spring Boot)
- **Base Image**: `eclipse-temurin:17-jre-alpine`
- **Build**: Multi-stage với Maven
- **Health Check**: `GET /api/stats/health`
- **JVM Settings**: Xms=256m, Xmx=512m

### Frontend (Nuxt.js)
- **Base Image**: `node:20-alpine`
- **Build**: Multi-stage với npm build
- **Health Check**: HTTP GET localhost:3000

### ML Service (FastAPI)
- **Base Image**: `python:3.10-slim`
- **Health Check**: `GET /health`

## 🛠️ Common Commands

### Khởi động hệ thống
```bash
# Khởi động tất cả services
docker-compose up -d

# Khởi động và rebuild
docker-compose up -d --build

# Khởi động với logs
docker-compose up
```

### Kiểm tra trạng thái
```bash
# Xem tất cả containers
docker-compose ps

# Xem logs
docker-compose logs -f

# Xem logs của service cụ thể
docker-compose logs -f backend
docker-compose logs -f frontend
```

### Dừng và xóa
```bash
# Dừng tất cả services
docker-compose down

# Dừng và xóa volumes (cẩn thận: mất data!)
docker-compose down -v

# Dừng và xóa images
docker-compose down --rmi all
```

### Rebuild specific service
```bash
# Rebuild backend
docker-compose up -d --build backend

# Rebuild frontend
docker-compose up -d --build frontend

# Rebuild ml-service
docker-compose up -d --build ml-service
```

### Scale services (nếu cần)
```bash
# Scale backend to 3 instances
docker-compose up -d --scale backend=3
```

## 🔍 Troubleshooting

### Backend không khởi động
```bash
# Check logs
docker-compose logs backend

# Common issues:
# 1. Không kết nối được MongoDB/Redis → Đợi services khởi động (30-60s)
# 2. Port 8080 đã được dùng → Dừng service đang dùng port
```

### Frontend lỗi CORS
```bash
# Kiểm tra backend đã chạy chưa
curl http://localhost:8080/api/stats/health

# Nếu backend chưa chạy, frontend sẽ lỗi
docker-compose restart backend
```

### Database không có data
```bash
# Kiểm tra RabbitMQ có messages không
# Truy cập http://localhost:15672
# Check queue: city-data-queue-1, city-data-queue-2

# Trigger manual sync
curl -X POST http://localhost:8080/api/sync/trigger
```

### Container bị crash
```bash
# Restart specific container
docker-compose restart backend

# Force recreate
docker-compose up -d --force-recreate backend
```

## 📊 Health Checks

Tất cả services đều có health check tự động:

```bash
# Check health của tất cả containers
docker ps --format "table {{.Names}}\t{{.Status}}"

# Hoặc
docker-compose ps
```

Expected status:
```
smart-city-backend      Up (healthy)
smart-city-frontend     Up (healthy)
smart-city-ml          Up (healthy)
rabbit-edge-1          Up
rabbit-edge-2          Up
core-redis-hot         Up
core-mongo-warm        Up
core-mongo-cold        Up
```

## 🧪 Testing Deployment

### 1. Test Backend API
```bash
# Health check
curl http://localhost:8080/api/stats/health

# Get statistics
curl http://localhost:8080/api/stats

# Get edge nodes
curl http://localhost:8080/api/nodes

# Trigger sync
curl -X POST http://localhost:8080/api/sync/trigger
```

### 2. Test ML Service
```bash
# Health check
curl http://localhost:8000/health

# Test prediction
curl -X POST http://localhost:8000/predict \
  -H "Content-Type: application/json" \
  -d '{"source":"sensor","value":25.5,"metric_type":"temperature"}'
```

### 3. Test Frontend
Mở browser: http://localhost:3000

Kiểm tra:
- ✅ Dashboard hiển thị
- ✅ Statistics cập nhật
- ✅ Edge nodes hiển thị
- ✅ Không có CORS errors trong console

## 🔧 Environment Variables

### Backend
```yaml
SPRING_PROFILES_ACTIVE: docker  # Sử dụng container hostnames
```

### Frontend
```yaml
NUXT_PUBLIC_API_BASE: http://backend:8080  # Internal container communication
```

## 📁 Volumes & Data Persistence

Data được lưu trong volumes:

```bash
# Xem volumes
docker volume ls | grep smartcity

# Backup MongoDB
docker exec core-mongo-warm mongodump --out /backup
docker cp core-mongo-warm:/backup ./backup-warm

# Restore MongoDB
docker cp ./backup-warm core-mongo-warm:/backup
docker exec core-mongo-warm mongorestore /backup
```

## 🚨 Production Considerations

### Security
- [ ] Change default passwords (RabbitMQ, MongoDB)
- [ ] Use Docker secrets instead of plain env vars
- [ ] Enable SSL/TLS for external APIs
- [ ] Set up firewall rules

### Performance
- [ ] Adjust JVM heap size based on load
- [ ] Configure MongoDB replica sets
- [ ] Set up Redis persistence if needed
- [ ] Use Docker Swarm or Kubernetes for orchestration

### Monitoring
- [ ] Add Prometheus for metrics
- [ ] Add Grafana for visualization
- [ ] Set up log aggregation (ELK stack)
- [ ] Configure alerts

## 🎯 Development vs Production

### Development (Current)
```bash
docker-compose up -d
```

### Production
```bash
# Use production compose file
docker-compose -f docker-compose.yml -f docker-compose.prod.yml up -d
```

## 📝 Notes

1. **First startup**: Mất 2-3 phút để build images và khởi động
2. **Data persistence**: Data lưu trong volumes, không mất khi restart
3. **Network**: Tất cả services dùng chung network `h2k-network`
4. **Dependencies**: Backend đợi databases khởi động trước khi start

## 🆘 Support

Nếu gặp vấn đề:
1. Check logs: `docker-compose logs -f [service-name]`
2. Check health: `docker-compose ps`
3. Restart services: `docker-compose restart [service-name]`
4. Full reset: `docker-compose down -v && docker-compose up -d --build`
