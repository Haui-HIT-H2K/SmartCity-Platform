# XÂY DỰNG CORE BACKEND CHO SMART CITY PLATFORM (OLP 2025)

## 1. Vai trò (Role)

Bạn là một **Senior Java Backend Developer** chuyên về **Spring Boot** và **xử lý dữ liệu lớn (Big Data Processing)**.  
Nhiệm vụ của bạn là **xây dựng Core Backend** cho hệ thống **IoT thành phố thông minh (Smart City Platform)**.

---

## 2. Bối cảnh Hạ tầng (Infrastructure Context)

Hệ thống đã được triển khai trên **Docker** với các thông số kết nối cứng (Hardcoded) như sau:

### 2.1. Edge Layer (Nguồn dữ liệu)

- `rabbit-edge-1` (Port `5672`): RabbitMQ đại diện cho **khu vực 1**.
- `rabbit-edge-2` (Port `5672`): RabbitMQ đại diện cho **khu vực 2**.

**Cơ chế xử lý:**

- Backend đóng vai trò **Consumer**.
- **Phải chủ động PULL (kéo) dữ liệu theo chu kỳ (Batch Pull)**.
- **Không** dùng cơ chế **Listener thụ động**.

---

### 2.2. Storage Layer (Lưu trữ)

- **Redis Hot**
  - Host: `core-redis-hot`
  - Port: `6379`
  - Mục đích: Lưu **Cache/Buffer nóng**.

- **MongoDB Warm**
  - Host: `core-mongo-warm`
  - Port: `27017`
  - Database: `warm_db`
  - Mục đích: Lưu **dữ liệu tuần**.

- **MongoDB Cold**
  - Host: `core-mongo-cold`
  - Port: `27017`
  - Database: `cold_db`
  - Mục đích: Lưu **dữ liệu tháng**.

---

## 3. Yêu cầu Chức năng & Logic

### 3.1. Luồng dữ liệu (Data Flow)

1. **Scheduled Ingestion**
   - Cứ mỗi **10 giây** một lần, hệ thống kết nối tới các Queue của RabbitMQ.

2. **Batch Fetch**
   - Lấy về **một lô lớn** (ví dụ: từ **1000 - 5000 messages**) cùng lúc.
   - Coi mỗi lô này như một **"File dữ liệu"** (một batch thống nhất).

3. **Routing (Định tuyến dữ liệu)**

   - Nếu dữ liệu được đánh nhãn **HOT**  
     → Lưu vào **Redis** với **TTL = 1 tiếng**.

   - Nếu dữ liệu là **WARM**  
     → Lưu vào **MongoDB Warm**.

   - Nếu dữ liệu là **COLD**  
     → Lưu vào **MongoDB Cold**.

4. **Bulk Write (Quan trọng)**

   - Khi lưu xuống **MongoDB**, **BẮT BUỘC** dùng:
     - `BulkOperations` để ghi **cả lô 1000 bản ghi** một lúc.
   - **Tuyệt đối không** dùng `save()` từng bản ghi (vì hệ thống cần **chịu tải 40 triệu request**).

---

### 3.2. Data Model

**Entity:** `CityData`

- `id: String` – UUID.
- `sourceId: String` – ID thiết bị (device id).
- `payload: Map<String, Object>` – Dữ liệu cảm biến (sensor data).
- `dataType: Enum` – Một trong các giá trị: `HOT`, `WARM`, `COLD`.
- `timestamp: Long` – Thời gian ghi nhận dữ liệu (epoch millis).

---

## 4. Hướng dẫn thực hiện (Step-by-step)

> Lưu ý: Thực hiện code lần lượt từng bước.  
> Sau **mỗi bước** hãy **dừng lại đợi xác nhận** (từ người review / product owner).

---

### Bước 1: Khởi tạo & Cấu hình (Configuration)

**Yêu cầu:**

1. Tạo file `pom.xml` với các dependencies:

   - `spring-boot-starter-data-mongodb`
   - `spring-boot-starter-data-redis`
   - `spring-boot-starter-amqp` (RabbitMQ)
   - `spring-boot-starter-web`
   - `lombok`

2. Tạo file `application.yml` cấu hình kết nối tới tất cả các service:

   - Redis
   - MongoDB Warm
   - MongoDB Cold
   - RabbitMQ (2 node: `rabbit-edge-1`, `rabbit-edge-2`)

3. Viết class `MongoConfig` để cấu hình **Multi-Datasource**:

   - Tạo **2 bean**:
     - `warmMongoTemplate`
     - `coldMongoTemplate`

---

### Bước 2: Model & Repository

1. Tạo **Enum** `DataType` với các giá trị:

   - `HOT`
   - `WARM`
   - `COLD`

2. Tạo **class** `CityData` với các field:

   - `id: String`
   - `sourceId: String`
   - `payload: Map<String, Object>`
   - `dataType: DataType`
   - `timestamp: Long`

3. Tạo các **Repository Interface** cần thiết:

   - Nếu dùng `MongoTemplate` hoàn toàn để thao tác DB, có thể **bỏ qua** Repository interface cho Mongo.

---

### Bước 3: Ingestion Service (Logic PULL)

1. Tạo `RabbitMQIngestionService`.

2. Sử dụng `RabbitTemplate` để thực hiện lệnh:

   - `receiveAndConvert` trong vòng lặp để lấy **1 batch**.
   - Ví dụ: loop **1000 lần** hoặc cho tới khi **queue rỗng**.

3. Sử dụng `@Scheduled(fixedRate = 10000)` để:

   - Kích hoạt việc **kéo dữ liệu** từ cả:
     - `rabbit-edge-1`
     - `rabbit-edge-2`

---

### Bước 4: Storage Service (Logic Routing & Bulk Write)

1. Tạo `DataRoutingService`.

2. Service này **nhận vào** một `List<CityData>`.

3. Thực hiện:

   - Tách list thành 3 sub-list:
     - `hotList`
     - `warmList`
     - `coldList`

4. Lưu dữ liệu:

   - **HOT**
     - Lưu `hotList` vào **Redis** (dùng **Pipelining** hoặc lưu thường).
     - Set **TTL = 1 giờ** cho mỗi bản ghi.

   - **WARM**
     - Lưu `warmList` vào `warmMongoTemplate`:
       - Dùng `BulkOps.insert()`.

   - **COLD**
     - Lưu `coldList` vào `coldMongoTemplate`:
       - Dùng `BulkOps.insert()`.

---

### Bước 5: Controller (API Demo)

1. Tạo API: `GET /api/sync/trigger`

   - Mục đích: **Kích hoạt thủ công** việc kéo dữ liệu (phục vụ demo, test).

2. Tạo API: `GET /api/stats`

   - Mục đích: Trả về **thống kê số lượng bản ghi**:
     - Trong **Redis**
     - Trong **MongoDB Warm**
     - Trong **MongoDB Cold** (nếu cần)

---

## YÊU CẦU CHẤT LƯỢNG CODE

1. **Clean Architecture**

   - Tách lớp:
     - Controller
     - Service
     - Repository / Infrastructure
     - Config
   - Code rõ ràng, dễ mở rộng.

2. **Log đầy đủ (sử dụng `Slf4j`)**

   - Log:
     - Khi bắt đầu batch pull.
     - Khi nhận được số lượng message từ RabbitMQ.
     - Khi routing dữ liệu HOT/WARM/COLD.
     - Khi thực hiện bulk insert.
     - Khi có lỗi kết nối, lỗi ghi DB, v.v.

3. **Xử lý lỗi (Resilience)**

   - Nếu **RabbitMQ mất kết nối**, hệ thống:
     - **Không được crash**.
     - Chỉ `log.error` thông tin chi tiết.
     - Lần schedule tiếp theo sẽ **retry tự động**.

---

> Tài liệu này đóng vai trò như một **đặc tả kỹ thuật (Technical Spec)**  
> để xây dựng **Core Backend cho Smart City Platform (OLP 2025)**.
