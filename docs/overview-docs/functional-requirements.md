---
sidebar_position: 4
title: "4. Yêu Cầu Chức Năng (Functional Requirements)"
---

# 4. Yêu cầu Chức năng (Functional Requirements)

## Giới thiệu

Phần này mô tả chi tiết các yêu cầu chức năng của SmartCity-Platform. Mỗi yêu cầu được định danh bằng ID duy nhất (FR-XX), có mức độ ưu tiên, mô tả chi tiết, và tiêu chí chấp nhận.

## FR-1: Data Ingestion from Message Queue

**ID:** FR-1  
**Priority:** High (Must Have)  
**Module:** Backend - RabbitMQIngestionService

### Mô tả
Hệ thống phải có khả năng pull dữ liệu IoT từ RabbitMQ edge nodes theo batch để xử lý.

### Yêu cầu Chi tiết
1. Backend service phải kết nối tới ít nhất 2 RabbitMQ edge nodes
2. Pull messages theo batch với size có thể cấu hình (default 5000)
3. Schedule pull operation mỗi 10 giây (configurable)
4. Support concurrent pulling từ multiple queues
5. Implement retry mechanism khi connection failed
6. Log số lượng messages pulled cho mỗi batch

### Acceptance Criteria
- [ ] Backend successfully connects to 2 RabbitMQ nodes
- [ ] Pull 5000 messages per batch within 5 seconds
- [ ] Handle gracefully khi RabbitMQ node offline
- [ ] Log ingestion rate (messages/second)
- [ ] Zero message loss during pull operation

### Dependencies
- RabbitMQ 3.x running và accessible
- Spring AMQP library
- Network connectivity giữa Backend và RabbitMQ

### Test Case
```gherkin
Given: 10000 messages sẵn có trong RabbitMQ queue
When: Backend service pull batch với batch-size=5000
Then: Exactly 5000 messages được consumed
And: Remaining 5000 messages vẫn trong queue
And: Ingestion log hiển thị "Pulled 5000 messages"
```

---

## FR-2: Machine Learning Data Classification

**ID:** FR-2  
**Priority:** High (Must Have)  
**Module:** ML Service + Backend

### Mô tả
Hệ thống phải tự động phân loại mỗi message IoT thành 3 categories (HOT/WARM/COLD) bằng Machine Learning models.

### Yêu cầu Chi tiết
1. ML Service phải load 3 pre-trained IsolationForest models:
   - `temperature_model.pkl`
   - `humidity_model.pkl`
   - `co2_model.pkl`
2. Backend gửi mỗi message đến ML Service qua REST API `POST /predict`
3. ML Service trả về label: HOT (anomaly) hoặc COLD (normal)
4. Classification latency phải < 100ms per message
5. Backend phải handle ML Service timeout (fallback: label as COLD)

### Classification Logic
```
IsolationForest prediction:
  -1 (outlier/anomaly) → Label = HOT
   1 (inlier/normal) → Label = COLD

WARM tier: Reserved for future use (có thể là partially anomalous)
```

### Acceptance Criteria
- [ ] ML Service loads all 3 models successfully at startup
- [ ] Prediction API responds within 100ms
- [ ] Accuracy: ≥ 90% detection rate cho anomalies
- [ ] Backend handles ML Service downtime gracefully
- [ ] Classification result logged cho mỗi batch

### Dependencies
- scikit-learn IsolationForest models trained
- FastAPI ML Service running tại port 8000
- HTTP connectivity giữa Backend và ML Service

### Test Case
```gherkin
Given: Temperature value = 65°C (anomaly, normal range 15-35°C)
When: Backend sends {"metric_type": "temperature", "value": 65} to ML Service
Then: ML Service returns {"label": "HOT", "desc": "Temperature Anomaly Detected"}
And: Backend tags message with dataType="HOT"
```

---

## FR-3: Tiered Storage Management

**ID:** FR-3  
**Priority:** High (Must Have)  
**Module:** Backend - DataRoutingService

### Mô tả
Hệ thống phải route và lưu trữ dữ liệu vào 3 storage tiers khác nhau dựa trên classification result.

### Yêu cầu Chi tiết

**HOT Tier (Redis):**
1. Store messages labeled "HOT" vào Redis
2. Key format: `hot:sensor:{sensorId}:{timestamp}`
3. TTL: 3600 seconds (1 giờ)
4. Data structure: Hash với fields: sensorId, value, timestamp, metricType

**WARM Tier (MongoDB):**
1. Store messages labeled "WARM" vào MongoDB database `warm_db`
2. Collection: `city_data`
3. TTL index: 30 days (auto-delete sau 30 ngày)
4. Bulk insert với batch size 1000

**COLD Tier (MongoDB):**
1. Store messages labeled "COLD" vào MongoDB database `cold_db`
2. Collection: `city_data`
3. No TTL (permanent storage)
4. Bulk insert với batch size 1000

### Acceptance Criteria
- [ ] HOT data stored trong Redis với đúng TTL
- [ ] WARM data stored trong MongoDB warm_db
- [ ] COLD data stored trong MongoDB cold_db
- [ ] Bulk insert hoàn thành trong < 5 seconds cho 5000 documents
- [ ] Zero data loss trong quá trình routing

### Dependencies
- Redis server running tại port 6379
- MongoDB instances: warm (27018), cold (27019)
- Spring Data Redis, Spring Data MongoDB

### Test Case
```gherkin
Given: Batch of 100 messages classified: 30 HOT, 0 WARM, 70 COLD
When: DataRoutingService processes the batch
Then: 30 messages found in Redis (key pattern hot:sensor:*)
And: 0 messages found in warm_db.city_data
And: 70 messages found in cold_db.city_data
```

---

## FR-4: Real-time Dashboard Monitoring

**ID:** FR-4  
**Priority:** High (Must Have)  
**Module:** Frontend - Dashboard Page

### Mô tả
Hệ thống phải cung cấp web dashboard để giám sát real-time trạng thái hệ thống.

### Yêu cầu Chi tiết
1. Dashboard auto-refresh mỗi 2 giây
2. Hiển thị statistics:
   - HOT data count
   - WARM data count
   - COLD data count
   - Incoming rate (messages/second)
   - Processed rate (messages/second)
3. Hiển thị edge node status (online/offline)
4. Real-time line chart cho ingestion rate
5. Responsive design (desktop, tablet, mobile)

### UI Components
- `StatCard`: Hiển thị HOT/WARM/COLD counts
- `NodeCard`: Hiển thị RabbitMQ edge node status
- `RealtimeLineChart`: Line chart với data ingestion metrics

### Acceptance Criteria
- [ ] Dashboard loads trong < 3 seconds
- [ ] Statistics update every 2 seconds
- [ ] Chart displays last 30 data points
- [ ] Node status indicators (green=online, red=offline) accurate
- [ ] UI responsive trên các screen sizes

### Dependencies
- Backend API `/api/stats` available
- NuxtJS frontend running tại port 3000
- ECharts library loaded

### Test Case
```gherkin
Given: Backend reports HOT=1000, WARM=0, COLD=5000
When: Dashboard polls /api/stats
Then: StatCard HOT displays "1,000"
And: StatCard COLD displays "5,000"
And: Chart updates với new data point
```

---

## FR-5: Data Query API with Pagination

**ID:** FR-5  
**Priority:** Medium (Should Have)  
**Module:** Backend - DataController

### Mô tả
Hệ thống phải cung cấp REST API để query dữ liệu IoT với pagination và filtering.

### Yêu cầu Chi tiết
1. Endpoint: `GET /api/data`
2. Query parameters:
   - `type` (HOT/WARM/COLD): Filter theo data tier
   - `sensorId`: Filter theo sensor ID
   - `page`: Page number (default 0)
   - `size`: Page size (default 20, max 100)
3. Response format: JSON với paginated result
4. Support sorting theo timestamp (desc)

### Request Example
```http
GET /api/data?type=HOT&sensorId=SENSOR_0042&page=0&size=20
```

### Response Example
```json
{
  "data": [
    {
      "timestamp": "2025-12-04T22:57:00Z",
      "sensorId": "SENSOR_0042",
      "metricType": "temperature",
      "value": 45.5,
      "dataType": "HOT"
    }
  ],
  "page": 0,
  "size": 20,
  "total": 150,
  "totalPages": 8
}
```

### Acceptance Criteria
- [ ] API returns correct paginated results
- [ ] Filter by type works correctly
- [ ] Filter by sensorId works correctly
- [ ] Response time < 500ms cho page size ≤ 100
- [ ] Total count accurate

### Dependencies
- MongoDB indexes trên timestamp, sensorId, dataType
- Spring Data MongoDB Pagination support

### Test Case
```gherkin
Given: Database contains 1000 HOT messages và 5000 COLD messages
When: Client requests GET /api/data?type=HOT&page=0&size=50
Then: Response contains exactly 50 HOT messages
And: Response.total = 1000
And: Response.totalPages = 20
```

---

## FR-6: Edge Node Discovery and Monitoring

**ID:** FR-6  
**Priority:** Medium (Should Have)  
**Module:** Backend - EdgeNodeRegistry

### Mô tả
Hệ thống phải tự động discover và monitor RabbitMQ edge nodes.

### Yêu cầu Chi tiết
1. DNS-based discovery cho RabbitMQ edge nodes
2. Maintain active connection pool cho mỗi discovered node
3. Health check mỗi 30 giây
4. Auto-reconnect khi node comes back online
5. API endpoint `GET /api/nodes` để list all nodes

### Node Information
- host: Node hostname/IP
- port: RabbitMQ port
- status: online/offline
- lastPing: Timestamp of last successful health check

### Acceptance Criteria
- [ ] Discover both rabbitmq-edge-1 và rabbitmq-edge-2
- [ ] Health check successful every 30 seconds
- [ ] Auto-reconnect when node recovered
- [ ] API returns accurate node status
- [ ] No crash khi all nodes offline

### Dependencies
- RabbitMQ Management API enabled
- DNS resolution cho service names
- Spring Boot Actuator (optional)

### Test Case
```gherkin
Given: rabbitmq-edge-1 online, rabbitmq-edge-2 offline
When: EdgeNodeRegistry performs health check
Then: rabbitmq-edge-1 status = "online"
And: rabbitmq-edge-2 status = "offline"
And: GET /api/nodes returns both với correct status
```

---

## FR-7: Manual Data Synchronization

**ID:** FR-7  
**Priority:** Low (Nice to Have)  
**Module:** Backend - SystemController

### Mô tả
Hệ thống phải cho phép administrator trigger manual data sync từ RabbitMQ.

### Yêu cầu Chi tiết
1. API endpoint: `POST /api/sync/trigger`
2. Thực hiện immediate pull từ tất cả active edge nodes
3. Return số lượng messages đã processed
4. Log sync operation với timestamp

### Response Example
```json
{
  "status": "success",
  "messagesProcessed": 5247,
  "timestamp": "2025-12-04T22:57:00Z"
}
```

### Acceptance Criteria
- [ ] Manual sync pulls messages immediately
- [ ] Response chứa accurate message count
- [ ] Không conflict với scheduled pull operation
- [ ] Frontend có button để trigger sync
- [ ] Action logged trong system logs

### Test Case
```gherkin
Given: 1000 messages pending trong RabbitMQ queue
When: Admin triggers POST /api/sync/trigger
Then: All 1000 messages được processed
And: Response.messagesProcessed = 1000
And: System log contains "Manual sync completed: 1000 messages"
```

---

## FR-8: System Reset (Demo Purpose)

**ID:** FR-8  
**Priority:** Low (Nice to Have)  
**Module:** Backend - SystemController

### Mô tả
Hệ thống phải có chức năng reset toàn bộ dữ liệu cho demo purposes.

### Yêu cầu Chi tiết
1. API endpoint: `POST /api/system/reset`
2. Xóa tất cả data từ Redis HOT tier
3. Xóa tất cả documents từ MongoDB WARM tier
4. Xóa tất cả documents từ MongoDB COLD tier
5. Return confirmation message

### Warning
⚠️ **DESTRUCTIVE OPERATION** - Không có undo!

### Acceptance Criteria
- [ ] Tất cả Redis keys deleted
- [ ] Tất cả MongoDB documents deleted
- [ ] Frontend hiển thị confirmation dialog
- [ ] Operation logged với WARNING level
- [ ] Counts reset về 0 sau operation

### Test Case
```gherkin
Given: Redis contains 1000 keys, MongoDB contains 50000 documents
When: Admin confirms POST /api/system/reset
Then: Redis contains 0 keys
And: MongoDB warm_db.city_data contains 0 documents
And: MongoDB cold_db.city_data contains 0 documents
And: GET /api/stats returns HOT=0, WARM=0, COLD=0
```

---

## Summary Table

| FR ID | Name | Priority | Status |
|-------|------|----------|--------|
| FR-1 | Data Ingestion from Message Queue | High | ✅ Implemented |
| FR-2 | ML Data Classification | High | ✅ Implemented |
| FR-3 | Tiered Storage Management | High | ✅ Implemented |
| FR-4 | Real-time Dashboard | High | ✅ Implemented |
| FR-5 | Data Query API | Medium | ✅ Implemented |
| FR-6 | Edge Node Discovery | Medium | ✅ Implemented |
| FR-7 | Manual Sync | Low | ✅ Implemented |
| FR-8 | System Reset | Low | ✅ Implemented |
