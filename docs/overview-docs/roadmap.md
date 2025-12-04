<!--

  Copyright 2025 Haui.HIT - H2K

  Licensed under the Apache License, Version 2.0

  http://www.apache.org/licenses/LICENSE-2.0

-->

---
sidebar_position: 3
title: Lộ Trình Phát Triển
---

Lộ trình phát triển của SmartCity-Platform được chia thành các giai đoạn ngắn, thực tế, tập trung vào việc hoàn thành sản phẩm MVP (Sản phẩm Khả dụng Tối thiểu) cho cuộc thi PMNM 2025, đồng thời đặt nền móng cho sự phát triển bền vững sau này.

---

### 1. Giai đoạn 1 – Nền tảng Lõi (MVP cho Nộp bài)

**Mục tiêu:** (Trước 08/12/2025) Hoàn thành kiến trúc lõi PULL-model & NGSI-LD, đáp ứng 100% Tiêu chí PoF (Phần I) và Tiêu chí Kỹ thuật của đề bài.

- **Chức năng chính (Backend):**
    - Thiết lập Lớp Đệm (Edge Storage) dùng `NATS` (hoặc RabbitMQ).
    - Xây dựng `Smart Agent` (Spring Boot/Python) với logic:
        - PULL dữ liệu từ NATS.
        - Chuẩn hóa sang **NGSI-LD** (FIWARE Data Models).
        - Logic cơ bản phân loại **Nóng/Ấm**.
    - Triển khai Lớp Nóng (`Orion-LD` + `MongoDB`).
    - Triển khai Lớp Ấm (`TimescaleDB` + `QuantumLeap`).
- **Sản phẩm bàn giao (Tiêu chí PoF):**
    - Kho Git công khai, giấy phép `Apache 2.0`.
    - Toàn bộ hệ thống chạy bằng 1 lệnh `docker-compose up` (Tiêu chí 4).
    - Trang tài liệu Docusaurus (`README.md`, `CHANGELOG.md`, sơ đồ kiến trúc) (Tiêu chí 6).

---

### 2. Giai đoạn 2 – Ứng dụng Demo (Cho Chung kết)

**Mục tiêu:** (Sau khi nộp bài & Trước Chung kết) Xây dựng ứng dụng demo "GreenX" hoàn chỉnh (Tiêu chí 8, 9) để trình diễn luồng dữ liệu End-to-End.

- **Chức năng & Sản phẩm Demo:**
    - Xây dựng **Script Giả lập Sensor** (Python) để PUSH dữ liệu ô nhiễm & cây xanh vào `NATS`.
    - **Dashboard (Nhà quản lý):** Giao diện Web (React/Vue) hiển thị:
        - Biểu đồ, thống kê lịch sử ô nhiễm (Truy vấn Lớp Ấm - TimescaleDB).
        - Bản đồ nhiệt (heatmap) các khu vực.
    - **Ứng dụng (Người dân):** Giao diện Web/Mobile hiển thị:
        - Bản đồ hiển thị dữ liệu AQI **thời gian thực** (Truy vấn Lớp Nóng - Orion-LD).
        - Tính năng nhận **cảnh báo tức thì** (Sử dụng Orion Subscription).
- **Trình diễn (Tiêu chí 11):** Chuẩn bị bài trình bày 15 phút, nhấn mạnh vào **Tính nguyên gốc (Tiêu chí 7)** của kiến trúc PULL-model.

---

### 3. Giai đoạn 3 – Tầm nhìn & Phát triển Bền vững

**Mục tiêu:** (Sau cuộc thi - 2026+) Phát triển dự án thành một PMMN thực thụ, có khả năng mở rộng và áp dụng trong thực tế (Tiêu chí 10).

- **Định hướng Kỹ thuật:**
    - Hoàn thiện **Lớp Lạnh** (`MinIO`) và tích hợp **AI/ML** (dùng dữ liệu Lạnh để huấn luyện mô hình dự đoán ô nhiễm).
    - Nâng cấp `Smart Agent` với logic ưu tiên Nóng/Ấm/Lạnh thông minh hơn (ví dụ: tự động điều chỉnh tốc độ PULL).
    - Triển khai trên `Kubernetes` (dùng `Helm Chart`) để đảm bảo khả năng mở rộng thực tế.
- **Định hướng Cộng đồng (FOSS):**
    - Mở rộng tài liệu, tạo các `good-first-issues` để thu hút cộng đồng.
    - Phát triển thêm các ứng dụng Smart City khác (Giao thông, Cấp thoát nước) trên cùng một nền tảng.