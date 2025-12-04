# Lịch sử thay đổi

## [v1.0.0] - 2025-10-30
### Đã thêm (Added)
- Khởi tạo dự án và kho mã nguồn trên GitHub.
- Thêm giấy phép Apache 2.0.
- Xây dựng cấu trúc tài liệu cơ bản (README, CHANGELOG).


## [v2.0.0] - 2025-12-04
### Đã thêm (Added)
- **Machine Learning Integration**
  - Anomaly Detection: Tự động phát hiện bất thường cho Temperature, Humidity, CO2
  - IsolationForest Models: 3 trained models với accuracy cao
  - Real-time Classification: Phân loại dữ liệu thành HOT/WARM/COLD tiers
  - FastAPI Service: ML service với /predict và /health endpoints
  - Auto-training: Tự động train models khi deploy lần đầu

- **Tiered Storage Architecture**
  - Redis HOT Storage: In-memory cache cho anomaly data (TTL 1 giờ)
  - MongoDB WARM Storage: Persistent storage cho dữ liệu quan trọng
  - MongoDB COLD Storage: Archive storage cho normal readings
  - Smart Routing: Tự động route data dựa trên ML classification

- **Pull-based Ingestion**
  - RabbitMQ Edge Nodes: 2 edge nodes cho high availability
  - Batch Processing: Pull 5,000 messages/batch mỗi 10 giây
  - Resilient Design: Tiếp tục hoạt động khi edge node fail
  - Rate Tracking: Real-time monitoring của ingestion rates

- **Real-time Dashboard**
  - Edge Node Monitoring: Hiển thị status của tất cả edge nodes
  - Ingestion Rate Chart: Visualization của incoming/processed rates
  - Storage Statistics: HOT/WARM/COLD data counts
  - System Health: Overall system health monitoring
  - Auto-refresh: Update metrics mỗi 2 giây

- **Data Explorer**
  - Advanced Filtering: Filter by type, sensor ID
  - Pagination: 20 records per page với smart navigation
  - Record Details: View chi tiết từng record
  - Responsive UI: Modern glassmorphism design với dark mode

- **RESTful APIs**
  - GET /api/data: Query data với pagination và filtering
  - GET /api/data/:id: Get individual record
  - GET /api/stats: System statistics và metrics
  - GET /api/stats/health: Health check endpoint
  - GET /api/nodes: Edge nodes information
  - POST /api/system/reset: System reset (placeholder)

- **Docker Deployment**
  - 100% Containerized: Tất cả services chạy trên Docker
  - Single Command: docker-compose up -d --build
  - 9 Services: Backend, ML, Frontend, 2x RabbitMQ, Redis, 2x MongoDB, Mongo Express
  - Health Checks: Auto-restart on failure
  - Volume Persistence: Data persistence cho databases

## [v2.0.1] - 2025-12-04
### Đã thêm (Added)
- Redesign the chart interface displaying pull data
- Add a license at the top of each source file
- Improve system performance using streaming methods
- Fix the error of adding a License at the beginning of each code file
