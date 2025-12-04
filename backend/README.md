SPDX-License-Identifier: Apache-2.0
<!--

  Copyright 2025 Haui.HIT - H2K

  Licensed under the Apache License, Version 2.0

  http://www.apache.org/licenses/LICENSE-2.0

-->

# Smart City Platform - Core Backend

A high-performance IoT data ingestion and storage platform for Smart City infrastructure, built with Spring Boot and featuring a 3-tier storage architecture (HOT/WARM/COLD).

![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.2.0-brightgreen)
![Java](https://img.shields.io/badge/Java-17-blue)
![MongoDB](https://img.shields.io/badge/MongoDB-Latest-green)
![Redis](https://img.shields.io/badge/Redis-Latest-red)

## 🚀 Features

### Dynamic Edge Node Discovery
- **DNS-based Registry**: Automatic discovery and registration of RabbitMQ edge nodes
- **Health Monitoring**: Real-time tracking of edge node status and connectivity
- **Multi-node Support**: Handles multiple edge storage nodes simultaneously
- **Fault Tolerance**: Graceful handling of node failures

### Data Ingestion Pipeline
- **Batch Processing**: Pulls data from RabbitMQ queues in configurable batch sizes
- **Multi-threaded**: Parallel processing of messages from multiple edge nodes
- **Error Handling**: Robust retry mechanisms and dead-letter queue support
- **Rate Limiting**: Configurable throttling to prevent system overload

### 3-Tier Storage Architecture
- **HOT (Redis)**: High-frequency access data with configurable TTL
- **WARM (MongoDB)**: Recent data for analytics and queries (30-day retention)
- **COLD (MongoDB Archive)**: Long-term storage for historical analysis
- **Automatic Classification**: Rule-based routing based on sensor type, value, and age
- **Bulk Operations**: Optimized batch writes to minimize database load

### REST API
- **System Statistics**: Real-time metrics on data ingestion and storage
- **Data Query**: Paginated endpoints with filtering by type and sensor ID
- **Manual Controls**: Trigger synchronization and system management
- **Edge Node Status**: Monitor all registered edge nodes

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

## 📋 Prerequisites

- Java 17 or higher
- Maven 3.6+
- Redis Server (running on localhost:6379)
- MongoDB Server (running on localhost:27017)
- RabbitMQ Server (multiple edge nodes)
- Docker (optional, for containerized deployment)

## 🔧 Installation

1. **Clone the repository**
```bash
git clone <repository-url>
cd backend
```

2. **Configure application properties**
```bash
# Edit src/main/resources/application.yml
# Configure MongoDB, Redis, and RabbitMQ connections
```

3. **Build the project**
```bash
mvn clean install
```

4. **Run the application**
```bash
mvn spring-boot:run
```

The backend API will be available at `http://localhost:8080`

## 📦 Available Maven Commands

- `mvn spring-boot:run` - Start the application in development mode
- `mvn clean install` - Build the project and create JAR file
- `mvn clean package` - Package the application without running tests
- `mvn test` - Run unit tests (currently skipped in config)

## 🐳 Docker Deployment

### Build Docker Image
```bash
docker build -t smart-city-backend .
```

### Run with Docker
```bash
docker run -p 8080:8080 smart-city-backend
```

### Using Docker Compose
```bash
# From project root
docker-compose up -d
```

## 🏗️ Project Structure

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
└── README.md                                # This file
```

## ⚙️ Configuration

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
    lookup-interval: 30000  # 30 seconds

# Data Ingestion Settings
ingestion:
  batch-size: 1000
  thread-pool-size: 10
  queue-name: smartcity.data

# Storage Configuration
storage:
  hot:
    ttl-seconds: 3600      # 1 hour
  warm:
    retention-days: 30
  cold:
    retention-days: 365
```

## 📡 API Endpoints

The backend exposes the following REST APIs:

### System Statistics
- `GET /api/stats` - Get system statistics
  - Response: HOT/WARM/COLD counts, ingestion rates, node status

### Edge Node Management
- `GET /api/nodes` - List all registered edge nodes
  - Response: Array of edge nodes with status and configuration

### Data Query
- `GET /api/data` - Query city data with pagination
  - Query params: `type` (HOT/WARM/COLD), `sensorId`, `page`, `size`
  - Response: Paginated list of CityData records

### Data Synchronization
- `POST /api/sync/trigger` - Manually trigger data sync from all edge nodes
  - Response: Sync operation status and records processed

### System Management
- `POST /api/system/reset` - Clear all data (for demo purposes)
  - Response: Reset operation status

### Health Check
- `GET /actuator/health` - Application health status
- `GET /actuator/metrics` - Application metrics

## 🔄 Data Flow Architecture

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

## 📊 Storage Classification Rules

Data is automatically classified into tiers based on:

1. **HOT Tier (Redis)**:
   - Sensor types: `TEMPERATURE`, `MOTION`, `OCCUPANCY`
   - Value threshold: Priority sensors with critical values
   - TTL: 1 hour
   - Use case: Real-time dashboards, alerts

2. **WARM Tier (MongoDB)**:
   - Recent data (last 30 days)
   - Moderate access frequency
   - Retention: 30 days, then moved to COLD
   - Use case: Analytics, trend analysis

3. **COLD Tier (MongoDB Archive)**:
   - Historical data (older than 30 days)
   - Low access frequency
   - Long-term retention
   - Use case: Historical analysis, reporting

## 🚀 Performance Optimizations

- **Batch Processing**: Processes messages in batches of 1000 (configurable)
- **Bulk Writes**: MongoDB bulk insert operations for WARM/COLD tiers
- **Connection Pooling**: Redis and MongoDB connection pools
- **Async Processing**: Non-blocking data ingestion with CompletableFuture
- **Caching**: In-memory caching of frequently accessed data
- **Index Optimization**: MongoDB indexes on `timestamp`, `sensorId`, `type`

## 🧪 Testing

Run unit tests:
```bash
mvn test
```

Note: Tests are currently skipped in the Maven configuration. To enable:
```xml
<!-- In pom.xml -->
<configuration>
    <skipTests>false</skipTests>
</configuration>
```

## 📝 Environment Variables

The following environment variables can be used to override configuration:

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

## 🌐 Integration with Frontend

This backend is designed to work with the Smart City Dashboard (Nuxt 3 frontend). The frontend should:

- Connect to `http://localhost:8080` (or configured API base URL)
- Poll `/api/stats` every 2-5 seconds for real-time updates
- Use `/api/data` for data exploration and pagination
- Trigger manual sync via `/api/sync/trigger`

Enable CORS for frontend integration:
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

## 📚 Implementation Details

### Edge Node Discovery

The `EdgeNodeRegistry` service performs DNS-based discovery:
- Queries DNS for RabbitMQ edge node service records
- Automatically registers/deregisters nodes based on DNS updates
- Maintains connection pools for each discovered node
- Performs health checks every 30 seconds

### Data Ingestion Process

1. **Discovery Phase**: EdgeNodeRegistry discovers available RabbitMQ nodes
2. **Pull Phase**: RabbitMQIngestionService pulls batches from each node
3. **Routing Phase**: DataRoutingService classifies each message
4. **Storage Phase**: Bulk writes to appropriate tier (Redis/MongoDB)
5. **Cleanup Phase**: Periodic cleanup of expired HOT data and WARM→COLD migration

### Statistics Aggregation

The `SystemStatsService` provides real-time metrics:
- **Data Counts**: HOT/WARM/COLD record counts
- **Ingestion Rate**: Messages per second (incoming vs processed)
- **Node Health**: Online/offline status of each edge node
- **Performance Metrics**: Average processing time, batch sizes

## 🔐 Security Considerations

- **Authentication**: Implement Spring Security for API authentication
- **Authorization**: Role-based access control (RBAC)
- **Data Validation**: Input validation on all API endpoints
- **Rate Limiting**: Prevent abuse with rate limiting middleware
- **Encryption**: Use TLS for RabbitMQ and MongoDB connections

## 🐛 Troubleshooting

**RabbitMQ Connection Issues:**
- Verify RabbitMQ is running: `rabbitmqctl status`
- Check edge node DNS resolution
- Review firewall rules for port 5672

**MongoDB Connection Issues:**
- Verify MongoDB is running: `mongosh`
- Check connection URI in `application.yml`
- Ensure database user has proper permissions

**Redis Connection Issues:**
- Verify Redis is running: `redis-cli ping`
- Check Redis host/port configuration
- Review Redis logs for errors

**Performance Issues:**
- Increase batch size for higher throughput
- Tune thread pool size based on CPU cores
- Monitor MongoDB indexes and query performance
- Check Redis memory usage and eviction policies

## 📄 License

MIT License - feel free to use this project for your own purposes.

## 👨‍💻 Development

Built with ❤️ for the OLP 2025 Smart City Platform project.

For issues or questions, please open an issue in the repository.

---

**Note**: This is the core backend service. For the monitoring dashboard, refer to the Frontend (Nuxt 3) repository. For data simulation, refer to the Python Data Simulator repository.
