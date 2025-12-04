<!--

  Copyright 2025 Haui.HIT - H2K

  Licensed under the Apache License, Version 2.0

  http://www.apache.org/licenses/LICENSE-2.0

-->

---
sidebar_position: 2
title: Chức Năng Chính
---

SmartCity-Platform được xây dựng với một bộ tính năng chuyên biệt, tập trung vào việc thu thập, xử lý và cung cấp dữ liệu đô thị **thời gian thực** một cách **tin cậy** và **hiệu quả**.

---

### 1. Thu thập & Đệm Dữ liệu (Lớp Edge)

Đây là chức năng nguyên gốc (Tiêu chí 7) giúp hệ thống chống sập.

- **Tiếp nhận Dữ liệu (Edge Storage):** Cung cấp một "điểm cuối" (endpoint) duy nhất, siêu chịu tải (dựa trên NATS/RabbitMQ) để tiếp nhận dữ liệu thô từ hàng triệu sensor.
- **Cơ chế Đệm (Buffering):** Dữ liệu được đưa vào một hàng đợi (queue) đệm, tách biệt hoàn toàn sensor khỏi hệ thống xử lý lõi. Sensor PUSH dữ liệu và nhận phản hồi ngay lập tức, không cần chờ xử lý.

### 2. Xử lý Thông minh (Smart Agent)

Đây là "bộ não" của hệ thống, thực thi logic PULL và ưu tiên.

- **Chủ động PULL dữ liệu:** Smart Agent sẽ chủ động "PULL" dữ liệu từ Lớp Đệm (chức năng 1) với tốc độ mà nó có thể xử lý. Điều này đảm bảo hệ thống lõi không bao giờ bị quá tải.
- **Phân loại Ưu tiên (Nóng/Ấm/Lạnh):** Agent tự động đọc và phân loại dữ liệu:
    - **Nóng (Hot):** Cảnh báo khẩn cấp (cháy, tai nạn). Được ưu tiên PULL và xử lý tức thì (delay < 1 giây).
    - **Ấm (Warm):** Dữ liệu cập nhật (kẹt xe, AQI). Xử lý với độ trễ thấp (delay vài giây/phút).
    - **Lạnh (Cold):** Dữ liệu log, thống kê. PULL khi hệ thống rảnh rỗi.
- **Chuẩn hóa NGSI-LD (Yêu cầu Đề bài):** Smart Agent chịu trách nhiệm chuyển đổi (transform) dữ liệu thô (JSON, text...) sang định dạng chuẩn **NGSI-LD** và sử dụng **FIWARE Smart Data Models** (`AirQualityObserved`, `TrafficFlowObserved`...).

### 3. Quản lý Bối cảnh (Lớp Nóng - NGSI-LD)

Trái tim của hệ thống, tuân thủ 100% yêu cầu kỹ thuật của đề bài.

- **Context Broker (Orion-LD):** Cung cấp API NGSI-LD để quản lý **trạng thái hiện tại** (context) của thành phố. Trả lời câu hỏi: "Bây giờ, AQI ở điểm X là bao nhiêu?"
- **Đăng ký & Thông báo (Subscribe):** Cho phép các ứng dụng (ví dụ: GreenX App) đăng ký để nhận thông báo (qua Webhook) ngay lập tức khi một trạng thái thay đổi (ví dụ: khi `aqi > 150`).

### 4. Lưu trữ Lịch sử (Lớp Ấm & Lạnh)

- **Lưu trữ Chuỗi thời gian (Lớp Ấm):** Tự động lưu trữ lịch sử của các thực thể (entities) vào CSDL chuỗi thời gian (như **TimescaleDB**). Phục vụ cho các dashboard phân tích, vẽ biểu đồ.
- **Hồ dữ liệu (Lớp Lạnh):** Lưu trữ dữ liệu thô và dữ liệu nén (archived) dài hạn vào Data Lake (như **MinIO**) với chi phí rẻ. Phục vụ cho Big Data và huấn luyện AI/ML.

### 5. Truy xuất & Tích hợp (API Đa tầng)

Nền tảng cung cấp các API chuyên biệt cho từng nhu cầu:

- **API Thời gian thực (Lớp Nóng):** Truy vấn trạng thái tức thì thông qua API **NGSI-LD** chuẩn (JSON-LD).
- **API Lịch sử (Lớp Ấm):** Cung cấp API RESTful (ví dụ: qua Spring Boot) để truy vấn dữ liệu lịch sử (đã xử lý) từ TimescaleDB.
- **Hỗ trợ Dữ liệu Không gian:** Tích hợp sẵn `GeoProperty` (chuẩn NGSI-LD), cho phép truy vấn dữ liệu theo vị trí địa lý (ví dụ: "tìm 5 cảm biến gần tôi nhất").

### 6. Vận hành & Ứng dụng Demo

- **Đóng gói Docker Compose (Tiêu chí 4):** Toàn bộ nền tảng (Agent, Orion, NATS, TimescaleDB, MinIO) được đóng gói trong một tệp `docker-compose.yml` duy nhất, cho phép triển khai toàn bộ hệ thống bằng một lệnh.
- **Giám sát (Monitoring):** Cung cấp các endpoint để tích hợp với `Prometheus`/`Grafana`, cho phép theo dõi sức khỏe của từng dịch vụ.
- **Ứng dụng Demo (GreenX):** Xây dựng ứng dụng web/mobile "GreenX" (Demo cảnh báo Ô nhiễm & Cây xanh) để chứng minh khả năng hoạt động của nền tảng (Tiêu chí 8).