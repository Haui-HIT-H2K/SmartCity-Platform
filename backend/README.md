SPDX-License-Identifier: Apache-2.0
<!--

  Copyright 2025 Haui.HIT - H2K

  Licensed under the Apache License, Version 2.0

  http://www.apache.org/licenses/LICENSE-2.0

-->

# Smart City Platform - Core Backend

Nền tảng thu thập và lưu trữ dữ liệu IoT hiệu năng cao cho hạ tầng Smart City, được xây dựng với Spring Boot và có kiến trúc lưu trữ 3 tầng (HOT/WARM/COLD).

![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.2.0-brightgreen)
![Java](https://img.shields.io/badge/Java-17-blue)
![MongoDB](https://img.shields.io/badge/MongoDB-Latest-green)
![Redis](https://img.shields.io/badge/Redis-Latest-red)

## 🚀 Tính năng

### Khám phá Edge Node Động
- **Registry dựa trên DNS**: Tự động khám phá và đăng ký các RabbitMQ edge node
- **Giám sát Sức khỏe**: Theo dõi real-time trạng thái và kết nối của edge node
- **Hỗ trợ Multi-node**: Xử lý đồng thời nhiều edge storage node
- **Khả năng Chịu lỗi**: Xử lý mượt mà khi node bị lỗi

### Data Ingestion Pipeline
- **Batch Processing**: Pull dữ liệu từ RabbitMQ queue theo batch size có thể cấu hình
- **Multi-threaded**: Xử lý song song message từ nhiều edge node
- **Xử lý Lỗi**: Cơ chế retry mạnh mẽ và hỗ trợ dead-letter queue
- **Rate Limiting**: Throttling có thể cấu hình để tránh quá tải hệ thống

### Kiến trúc Lưu trữ 3 Tầng
- **HOT (Redis)**: Dữ liệu truy cập tần suất cao với TTL có thể cấu hình
- **WARM (MongoDB)**: Dữ liệu gần đây cho phân tích và truy vấn (lưu trữ 30 ngày)
- **COLD (MongoDB Archive)**: Lưu trữ dài hạn cho phân tích lịch sử
- **Phân loại Tự động**: Routing dựa trên rule theo loại sensor, giá trị và thời gian
- **Bulk Operations**: Batch write được tối ưu để giảm thiểu tải database

### REST API
- **Thống kê Hệ thống**: Metrics real-time về data ingestion và storage
- **Truy vấn Dữ liệu**: Endpoint phân trang với filter theo type và sensor ID
- **Điều khiển Thủ công**: Trigger đồng bộ hóa và quản lý hệ thống
- **Trạng thái Edge Node**: Giám sát tất cả edge node đã đăng ký

## 🛠️ Tech Stack

- **Framework**: Spring Boot 3.2.0
- **Language**: Java 17
- **Build Tool**: Maven
- **Databases**: 
  - Redis (HOT tier storage)
  - MongoDB (WARM/COLD tier storage)
- **Message Queue**: RabbitMQ (AMQP)
- **Libraries**:
  - Spring Data MongoDB
  - Spring Data Redis
  - Spring AMQP
  - Lombok

## 📋 Yêu cầu Hệ thống

- Java 17 hoặc cao hơn
- Maven 3.6+
- Redis Server (chạy trên localhost:6379)
- MongoDB Server (chạy trên localhost:27017)
- RabbitMQ Server (nhiều edge node)
- Docker (tùy chọn, cho containerized deployment)

## 🔧 Cài đặt

1. **Clone repository**
```bash
git clone <repository-url>
cd backend
```

2. **Cấu hình application properties**
```bash
# Chỉnh sửa src/main/resources/application.yml
# Cấu hình kết nối MongoDB, Redis, và RabbitMQ
```

3. **Build project**
```bash
mvn clean install
```

4. **Chạy ứng dụng**
```bash
mvn spring-boot:run
```

Backend API sẽ khả dụng tại `http://localhost:8080`

## 📦 Các lệnh Maven có sẵn

- `mvn spring-boot:run` - Khởi động ứng dụng ở development mode
- `mvn clean install` - Build project và tạo file JAR
- `mvn clean package` - Package ứng dụng mà không chạy test
- `mvn test` - Chạy unit test (hiện đang bị skip trong config)

## 🐳 Docker Deployment

### Build Docker Image
```bash
docker build -t smart-city-backend .
```

### Chạy với Docker
```bash
docker run -p 8080:8080 smart-city-backend
```

### Sử dụng Docker Compose
```bash
# Từ thư mục gốc project
docker-compose up -d
```

## 🏗️ Cấu trúc Project

```
backend/
├── src/
│   ├── main/
│   │   ├── java/com/smartcity/
│   │   │   ├── SmartCityApplication.java    # Main application entry point
│   │   │   ├── config/
│   │   │   │   ├── MongoConfig.java         # MongoDB multi-datasource config
│   │   │   │   ├── RedisConfig.java         # Redis configuration
│   │   │   │   └── RabbitMQConfig.java      # RabbitMQ connection config
│   │   │   ├── controller/
│   │   │   │   ├── DataController.java      # REST API endpoints
│   │   │   │   └── SystemController.java    # System control endpoints
│   │   │   ├── dto/
│   │   │   │   ├── SystemStatsDTO.java      # System statistics response
│   │   │   │   ├── EdgeNodeDTO.java         # Edge node information
│   │   │   │   └── DataQueryDTO.java        # Data query request/response
│   │   │   ├── model/
│   │   │   │   ├── CityData.java            # Main data entity
│   │   │   │   ├── DataType.java            # HOT/WARM/COLD enum
│   │   │   │   └── EdgeNodeConfig.java      # Edge node configuration
│   │   │   ├── repository/
│   │   │   │   ├── WarmDataRepository.java  # MongoDB WARM tier
│   │   │   │   ├── ColdDataRepository.java  # MongoDB COLD tier
│   │   │   │   └── HotDataRepository.java   # Redis repository
│   │   │   └── service/
│   │   │       ├── EdgeNodeRegistry.java    # DNS-based node discovery
│   │   │       ├── RabbitMQIngestionService.java  # Data ingestion
│   │   │       ├── DataRoutingService.java  # Storage tier routing
│   │   │       ├── CityDataQueryService.java # Data query service
│   │   │       └── SystemStatsService.java  # Statistics aggregation
│   │   └── resources/
│   │       ├── application.yml              # Main configuration
│   │       └── application-prod.yml         # Production config
│   └── test/
│       └── java/com/smartcity/              # Unit tests
├── Dockerfile                               # Docker build configuration
├── pom.xml                                  # Maven dependencies
└── README.md                                # File này
```

## ⚙️ Cấu hình

### Application Properties (application.yml)

```yaml
spring:
  application:
    name: smart-city-platform
  
  # MongoDB Configuration (Multi-datasource)
  data:
    mongodb:
      warm:
        uri: mongodb://localhost:27017/smartcity_warm
      cold:
        uri: mongodb://localhost:27017/smartcity_cold
  
  # Redis Configuration
  redis:
    host: localhost
    port: 6379
    timeout: 60000
  
  # RabbitMQ Configuration
  rabbitmq:
    template:
      receive-timeout: 2000

# Edge Node Discovery
edge-nodes:
  dns:
    service-name: rabbitmq-edge
    lookup-interval: 30000  # 30 giây

# Data Ingestion Settings
ingestion:
  batch-size: 1000
  thread-pool-size: 10
  queue-name: smartcity.data

# Storage Configuration
storage:
  hot:
    ttl-seconds: 3600      # 1 giờ
  warm:
    retention-days: 30
  cold:
    retention-days: 365
```

## 📡 API Endpoints

Backend cung cấp các REST API sau:

### Thống kê Hệ thống
- `GET /api/stats` - Lấy thống kê hệ thống
  - Response: Số lượng HOT/WARM/COLD, tỷ lệ ingestion, trạng thái node

### Quản lý Edge Node
- `GET /api/nodes` - Liệt kê tất cả edge node đã đăng ký
  - Response: Mảng các edge node với status và configuration

### Truy vấn Dữ liệu
- `GET /api/data` - Truy vấn city data với phân trang
  - Query params: `type` (HOT/WARM/COLD), `sensorId`, `page`, `size`
  - Response: Danh sách phân trang các CityData record

### Đồng bộ Dữ liệu
- `POST /api/sync/trigger` - Trigger thủ công data sync từ tất cả edge node
  - Response: Trạng thái sync operation và số record đã xử lý

### Quản lý Hệ thống
- `POST /api/system/reset` - Xóa toàn bộ dữ liệu (cho mục đích demo)
  - Response: Trạng thái reset operation

### Health Check
- `GET /actuator/health` - Trạng thái health của ứng dụng
- `GET /actuator/metrics` - Metrics của ứng dụng

## 🔄 Kiến trúc Data Flow

```
┌─────────────────┐
│  RabbitMQ Edge  │
│    Nodes 1-N    │
└────────┬────────┘
         │
         ▼
┌─────────────────────────┐
│ EdgeNodeRegistry        │
│ (DNS-based Discovery)   │
└──────────┬──────────────┘
           │
           ▼
┌─────────────────────────┐
│ RabbitMQIngestionService│
│ (Batch Pull)            │
└──────────┬──────────────┘
           │
           ▼
┌─────────────────────────┐
│ DataRoutingService      │
│ (Classification Logic)  │
└──────────┬──────────────┘
           │
     ┌─────┴─────┬─────────┐
     ▼           ▼         ▼
┌─────────┐ ┌─────────┐ ┌─────────┐
│  Redis  │ │ MongoDB │ │ MongoDB │
│  (HOT)  │ │ (WARM)  │ │ (COLD)  │
└─────────┘ └─────────┘ └─────────┘
```

## 📊 Quy tắc Phân loại Storage

Dữ liệu được tự động phân loại vào các tầng dựa trên:

1. **HOT Tier (Redis)**:
   - Loại sensor: `TEMPERATURE`, `MOTION`, `OCCUPANCY`
   - Ngưỡng giá trị: Sensor ưu tiên với giá trị quan trọng
   - TTL: 1 giờ
   - Use case: Dashboard real-time, cảnh báo

2. **WARM Tier (MongoDB)**:
   - Dữ liệu gần đây (30 ngày gần nhất)
   - Tần suất truy cập vừa phải
   - Retention: 30 ngày, sau đó chuyển sang COLD
   - Use case: Phân tích, phân tích xu hướng

3. **COLD Tier (MongoDB Archive)**:
   - Dữ liệu lịch sử (cũ hơn 30 ngày)
   - Tần suất truy cập thấp
   - Lưu trữ dài hạn
   - Use case: Phân tích lịch sử, báo cáo

## 🚀 Tối ưu Hiệu năng

- **Batch Processing**: Xử lý message theo batch 1000 (có thể cấu hình)
- **Bulk Writes**: MongoDB bulk insert operation cho WARM/COLD tier
- **Connection Pooling**: Redis và MongoDB connection pool
- **Async Processing**: Data ingestion không chặn với CompletableFuture
- **Caching**: In-memory cache cho dữ liệu truy cập thường xuyên
- **Index Optimization**: MongoDB index trên `timestamp`, `sensorId`, `type`

## 🧪 Testing

Chạy unit test:
```bash
mvn test
```

Lưu ý: Test hiện đang bị skip trong Maven configuration. Để bật:
```xml
<!-- Trong pom.xml -->
<configuration>
    <skipTests>false</skipTests>
</configuration>
```

## 📝 Biến Môi trường

Các biến môi trường sau có thể được dùng để ghi đè configuration:

```bash
# MongoDB
MONGODB_WARM_URI=mongodb://localhost:27017/smartcity_warm
MONGODB_COLD_URI=mongodb://localhost:27017/smartcity_cold

# Redis
REDIS_HOST=localhost
REDIS_PORT=6379

# RabbitMQ
RABBITMQ_HOSTS=edge1:5672,edge2:5672,edge3:5672

# Application
SERVER_PORT=8080
LOGGING_LEVEL=INFO
```

## 🌐 Tích hợp với Frontend

Backend này được thiết kế để hoạt động với Smart City Dashboard (Nuxt 3 frontend). Frontend nên:

- Kết nối tới `http://localhost:8080` (hoặc API base URL đã cấu hình)
- Poll `/api/stats` mỗi 2-5 giây cho cập nhật real-time
- Sử dụng `/api/data` cho data exploration và pagination
- Trigger manual sync qua `/api/sync/trigger`

Bật CORS cho tích hợp frontend:
```java
@Configuration
public class CorsConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
            .allowedOrigins("http://localhost:3000")
            .allowedMethods("GET", "POST", "PUT", "DELETE");
    }
}
```

## 📚 Chi tiết Triển khai

### Khám phá Edge Node

Service `EdgeNodeRegistry` thực hiện khám phá dựa trên DNS:
- Query DNS cho RabbitMQ edge node service record
- Tự động đăng ký/hủy đăng ký node dựa trên DNS update
- Duy trì connection pool cho mỗi node được khám phá
- Thực hiện health check mỗi 30 giây

### Quy trình Data Ingestion

1. **Discovery Phase**: EdgeNodeRegistry khám phá các RabbitMQ node có sẵn
2. **Pull Phase**: RabbitMQIngestionService pull batch từ mỗi node
3. **Routing Phase**: DataRoutingService phân loại mỗi message
4. **Storage Phase**: Bulk write tới tier phù hợp (Redis/MongoDB)
5. **Cleanup Phase**: Định kỳ cleanup HOT data đã hết hạn và migrate WARM→COLD

### Tổng hợp Thống kê

Service `SystemStatsService` cung cấp metrics real-time:
- **Data Count**: Số lượng record HOT/WARM/COLD
- **Ingestion Rate**: Message mỗi giây (incoming vs processed)
- **Node Health**: Trạng thái online/offline của mỗi edge node
- **Performance Metrics**: Thời gian xử lý trung bình, batch size

## 🔐 Cân nhắc Bảo mật

- **Authentication**: Triển khai Spring Security cho API authentication
- **Authorization**: Role-based access control (RBAC)
- **Data Validation**: Input validation trên tất cả API endpoint
- **Rate Limiting**: Ngăn chặn lạm dụng với rate limiting middleware
- **Encryption**: Sử dụng TLS cho RabbitMQ và MongoDB connection

## 🐛 Xử lý Sự cố

**Vấn đề Kết nối RabbitMQ:**
- Kiểm tra RabbitMQ đang chạy: `rabbitmqctl status`
- Kiểm tra DNS resolution của edge node
- Review firewall rule cho port 5672

**Vấn đề Kết nối MongoDB:**
- Kiểm tra MongoDB đang chạy: `mongosh`
- Kiểm tra connection URI trong `application.yml`
- Đảm bảo database user có quyền phù hợp

**Vấn đề Kết nối Redis:**
- Kiểm tra Redis đang chạy: `redis-cli ping`
- Kiểm tra cấu hình Redis host/port
- Review Redis log tìm lỗi

**Vấn đề Hiệu năng:**
- Tăng batch size cho throughput cao hơn
- Điều chỉnh thread pool size dựa trên số CPU core
- Giám sát MongoDB index và query performance
- Kiểm tra Redis memory usage và eviction policy

## 📄 Giấy phép

MIT License - thoải mái sử dụng project này cho mục đích của bạn.

## 👨‍💻 Development

Được xây dựng với ❤️ cho dự án OLP 2025 Smart City Platform.

Nếu có vấn đề hoặc câu hỏi, vui lòng mở issue trong repository.

---

**Lưu ý**: Đây là core backend service. Đối với monitoring dashboard, tham khảo Frontend (Nuxt 3) repository. Đối với data simulation, tham khảo Python Data Simulator repository.
