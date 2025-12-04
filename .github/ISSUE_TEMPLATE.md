<!--

  Copyright 2025 Haui.HIT - H2K

  Licensed under the Apache License, Version 2.0

  http://www.apache.org/licenses/LICENSE-2.0

-->

# Quy định thông báo lỗi

Khi báo lỗi bạn vui lòng cung cấp các thông tin sau để chúng tôi có thể xây dựng môi trường thử nghiệm giống của bạn để kiểm tra. Các thông tin này rất cần thiết trong quá trình đội code kiểm tra và sửa lỗi, đề nghị thành viên cung cấp đầy đủ thông tin để việc sửa lỗi được nhanh chóng.

## 1. Tiêu đề và mô tả

**Tiêu đề thông báo lỗi** ngắn gọn, xúc tích, đảm bảo tóm tắt được nội dung.

**Mô tả chi tiết lỗi** gặp phải, quá trình hoặc thao tác dẫn đến lỗi. Việc mô tả quá trình dẫn đến lỗi đặc biệt quan trọng vì nhiều khi lỗi chỉ xảy ra đúng với một số thao tác nhất định mà với các thao tác khác thì hệ thống không bị lỗi.

## 2. Cung cấp thông tin máy chủ

### Nếu xác định lỗi từ code, vui lòng cung cấp thêm các thông tin về môi trường và services

- **Phiên bản SmartCity-Platform:** (Ví dụ: commit hash `2574f2bc8b6f23f9fcfefc021554034e33fbc695` hoặc tag version nếu có)
- **Môi trường thử nghiệm:** Localhost / Docker Desktop / Linux Server
- **Hệ điều hành:** (Ví dụ: Windows 10, Ubuntu 22.04, macOS 14.0)
- **Docker version:** (Ví dụ: Docker Desktop 4.25.0 hoặc Docker Engine 24.0.7)
- **Docker Compose version:** (Ví dụ: Docker Compose v2.23.0)
- **Backend Service:** (Ví dụ: Spring Boot 3.2, Java 17)
- **ML Service:** (Ví dụ: FastAPI, Python 3.10)
- **Frontend Service:** (Ví dụ: NuxtJS 3, Node.js 20)
- **Trình duyệt web:** (Ví dụ: Chrome 120.0, Firefox 121.0, Edge 120.0)

### Nếu SmartCity-Platform chạy trên server (production environment) cần có thêm thông tin

- **Công cụ quản lý server:** (Ví dụ: SSH, Docker Swarm, Kubernetes, Portainer...)
- **Các thông tin khác về infrastructure:**
  - RabbitMQ version: (Ví dụ: RabbitMQ 3.12)
  - Redis version: (Ví dụ: Redis 7.2)
  - MongoDB version: (Ví dụ: MongoDB 7.0)
  - Network configuration: (Ví dụ: Bridge network, Host network)

### Nếu xác định lỗi từ giao diện frontend, vui lòng cung cấp thêm thông tin

- **Tên gói giao diện:** (Nếu có custom theme hoặc UI components)
- **Console errors:** (Lỗi từ browser console nếu có)
- **Network errors:** (Lỗi từ Network tab trong DevTools nếu có)

## 3. Lưu ý

Nếu là các lỗi nhỏ, cơ bản, dễ xác định lỗi... hay các câu hỏi, đề xuất, góp ý... thì không cần cung cấp thông tin về máy chủ, services tại phần 2.

