<!--

  Copyright 2025 Haui.HIT - H2K

  Licensed under the Apache License, Version 2.0

  http://www.apache.org/licenses/LICENSE-2.0

-->

---
sidebar_position: 2
title: Luồng Dữ liệu (Data Flow)
---

Đây là luồng dữ liệu (data flow) End-to-End của hệ thống SmartCity-Platform, kết hợp giữa ý tưởng **DNS Routing & PULL Model** (Tiêu chí 7) và yêu cầu **NGSI-LD** (Tiêu chí Kỹ thuật).

---

## 1. Sơ đồ Kiến trúc Tổng thể
```mermaid
graph TD;
    %% ---- 1. Nguồn ----
    A["A. Nguồn Dữ liệu (Sensor, Camera...)"];
    
    %% ---- 2. Phân loại (Ý tưởng của bạn) ----
    B("B. DNS Routing (Phân loại)");
    
    %% ---- 3. Lớp Đệm (Ý tưởng của bạn) ----
    subgraph "Lớp Đệm (Edge Storage)"
        C["C1. Edge Nóng"];
        D["C2. Edge Ấm"];
        E["C3. Edge Lạnh"];
    end

    %% ---- 4. Logic Backend (Ý tưởng của bạn) ----
    F["F. Smart Agent (Backend Logic)"];

    %% ---- 5. Lớp Lõi (Yêu cầu Đề bài) ----
    subgraph "Lớp Lõi (Hệ thống Server)"
        G["G. Lớp Nóng (Orion-LD Broker)"];
        H["H. Lớp Ấm (TimescaleDB)"];
        I["I. Lớp Lạnh (MinIO)"];
    end

    %% ---- 6. Ứng dụng Demo ----
    J["J. Ứng dụng Demo (GreenX)"];

    %% ---- ĐỊNH NGHĨA LUỒNG DỮ LIỆU ----
    
    %% Luồng 1+2: PUSH & Route
    A -- "PUSH Raw Data" --> B;
    B -- "Route 'Nóng'" --> C;
    B -- "Route 'Ấm'" --> D;
    B -- "Route 'Lạnh'" --> E;

    %% Luồng 3: PULL (Ý tưởng của bạn)
    F -- "PULL (Ưu tiên 1)" --> C;
    F -- "PULL (Ưu tiên 2)" --> D;
    F -- "PULL (Ưu tiên 3)" --> E;
    
    %% Luồng 4: Xử lý NGSI-LD (Yêu cầu Đề bài)
    F -- "PUSH Chuẩn hóa (Nóng/Ấm)" --> G;
    G -- "Auto-subscribe (QuantumLeap)" --> H;
    
    %% Luồng 5: Xử lý Lạnh (Ý tưởng của bạn)
    F -- "PUSH Dữ liệu Lạnh" --> I;

    %% Luồng 6: Ứng dụng
    J -- "Query Real-time (NGSI-LD)" --> G;
    J -- "Query Lịch sử" --> H;
```


---

## 2. Giải thích Luồng Dữ liệu

**Luồng 1 & 2: PUSH & Phân loại (Routing)**  
**A (Nguồn Dữ liệu):** Các thiết bị (Sensor, Camera, User App) gửi dữ liệu thô (raw data) đến một endpoint duy nhất.

**B (DNS Routing):** Dịch vụ DNS Routing (hoặc một API Gateway) "đọc" loại dữ liệu (ví dụ: qua URL hoặc header) và phân loại nó ngay lập tức.

**Dữ liệu PUSH được điều hướng vào 3 kho đệm riêng biệt:**
- Cảnh báo khẩn cấp vào **C1 (Edge Nóng)**.
- Dữ liệu cập nhật thường xuyên vào **C2 (Edge Ấm)**.
- Dữ liệu log/thống kê vào **C3 (Edge Lạnh)**.

**Luồng 3: PULL Ưu tiên (Ý tưởng Cốt lõi)**  
**F (Smart Agent):** Đây là "bộ não" (backend do đội phát triển). Nó chủ động PULL dữ liệu từ 3 kho đệm với thứ tự ưu tiên nghiêm ngặt:
- **Ưu tiên 1:** Luôn PULL và xử lý **C1 (Nóng)** trước.
- **Ưu tiên 2:** Chỉ PULL **C2 (Ấm)** khi C1 rỗng.
- **Ưu tiên 3:** Chỉ PULL **C3 (Lạnh)** khi C1 và C2 đều rỗng.

Đây chính là cơ chế chống quá tải và đảm bảo HA cho hệ thống.

**Luồng 4: Xử lý NGSI-LD (Đáp ứng Đề bài)**  
**F (Smart Agent)** sau khi PULL dữ liệu Nóng/Ấm, sẽ tiến hành chuẩn hóa chúng sang định dạng **NGSI-LD** (sử dụng **FIWARE Data Models**).

F **PUSH** dữ liệu đã chuẩn hóa vào **G (Orion-LD Broker)**. Đây chính là **Lớp Nóng (In-Memory)**, chứa trạng thái hiện tại của thành phố.

**H (Lớp Ấm)** (sử dụng QuantumLeap) tự động "đăng ký" (subscribe) với G. Ngay khi G có dữ liệu mới, nó sẽ tự động sao chép sang **H (TimescaleDB)** để lưu trữ lịch sử.

**Luồng 5: Xử lý Dữ liệu Lạnh**  
Khi F (Smart Agent) PULL dữ liệu từ **E (Lạnh)**, nó sẽ **PUSH** thẳng dữ liệu này vào **I (MinIO)** để lưu trữ dài hạn với chi phí rẻ.

**Luồng 6: Ứng dụng (Demo)**  
**J (Ứng dụng GreenX)** khi cần dữ liệu thời gian thực (ví dụ: "AQI bây giờ?") sẽ truy vấn trực tiếp **G (Lớp Nóng)**.

Khi cần dữ liệu lịch sử (ví dụ: "biểu đồ 7 ngày qua?") sẽ truy vấn **H (Lớp Ấm)**.
