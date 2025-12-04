---
sidebar_position: 2
title: API Reference
---

# API Reference

## Base Configuration

**Base URL:** `http://localhost:8080`  
**Content-Type:** `application/json`  
**CORS:** Enabled for `http://localhost:3000`

---

## Data Endpoints

### GET /api/data

Query city data với pagination và filtering.

**Request:**
```http
GET /api/data?type=HOT&sensorId=SENSOR_0042&page=0&size=20
```

**Query Parameters:**

| Parameter | Type | Required | Default | Description |
|-----------|------|----------|---------|-------------|
| `type` | string | No | All | Filter by data type: HOT, WARM, COLD |
| `sensorId` | string | No | All | Filter by sensor ID |
| `page` | integer | No | 0 | Page number (0-indexed) |
| `size` | integer | No | 20 | Page size (max 100) |

**Response:** `200 OK`
```json
{
  "data": [
    {
      "timestamp": "2025-12-04T22:57:00Z",
      "sensorId": "SENSOR_0042",
      "metricType": "temperature",
      "value": 45.5,
      "dataType": "HOT",
      "location": {
        "lat": 21.0285,
        "lng": 105.8542
      }
    }
  ],
  "page": 0,
  "size": 20,
  "total": 150,
  "totalPages": 8
}
```

---

## Statistics Endpoints

### GET /api/stats

Lấy thống kê hệ thống real-time.

**Request:**
```http
GET /api/stats
```

**Response:** `200 OK`
```json
{
  "hotCount": 14250000,
  "warmCount": 0,
  "coldCount": 25750000,
  "incomingRate": 487.5,
  "processedRate": 487.5,
  "lastUpdate": "2025-12-04T22:57:00Z"
}
```

**Fields:**

| Field | Type | Description |
|-------|------|-------------|
| `hotCount` | long | Số lượng messages trong HOT tier (Redis) |
| `warmCount` | long | Số lượng documents trong WARM tier (MongoDB) |
| `coldCount` | long | Số lượng documents trong COLD tier (MongoDB) |
| `incomingRate` | double | Message rate incoming (msg/s) |
| `processedRate` | double | Message rate processed (msg/s) |
| `lastUpdate` | string | Timestamp của stats update |

---

## Node Management Endpoints

### GET /api/nodes

Liệt kê tất cả RabbitMQ edge nodes.

**Request:**
```http
GET /api/nodes
```

**Response:** `200 OK`
```json
[
  {
    "host": "rabbitmq-edge-1",
    "port": 5672,
    "status": "online",
    "lastPing": "2025-12-04T22:56:58Z"
  },
  {
    "host": "rabbitmq-edge-2",
    "port": 5673,
    "status": "online",
    "lastPing": "2025-12-04T22:56:59Z"
  }
]
```

---

## System Control Endpoints

### POST /api/sync/trigger

Trigger manual data synchronization từ tất cả edge nodes.

**Request:**
```http
POST /api/sync/trigger
```

**Response:** `200 OK`
```json
{
  "status": "success",
  "messagesProcessed": 5247,
  "timestamp": "2025-12-04T22:57:00Z"
}
```

### POST /api/system/reset

⚠️ **DESTRUCTIVE** - Xóa toàn bộ dữ liệu (demo purpose only).

**Request:**
```http
POST /api/system/reset
```

**Response:** `200 OK`
```json
{
  "status": "success",
  "message": "All data cleared successfully",
  "timestamp": "2025-12-04T22:57:00Z"
}
```

---

## Health Check

### GET /actuator/health

Spring Boot Actuator health endpoint.

**Request:**
```http
GET /actuator/health
```

**Response:** `200 OK`
```json
{
  "status": "UP"
}
```

---

## Error Responses

### 400 Bad Request
```json
{
  "error": "Invalid parameter",
  "message": "Page size must be between 1 and 100",
  "timestamp": "2025-12-04T22:57:00Z"
}
```

### 500 Internal Server Error
```json
{
  "error": "Internal server error",
  "message": "Failed to connect to MongoDB",
  "timestamp": "2025-12-04T22:57:00Z"
}
```
