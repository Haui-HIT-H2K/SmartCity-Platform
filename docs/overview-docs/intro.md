<!--

  Copyright 2025 Haui.HIT - H2K

  Licensed under the Apache License, Version 2.0

  http://www.apache.org/licenses/LICENSE-2.0

-->

---
sidebar_position: 1
title: Giới Thiệu Tổng Quan
---

**SmartCity-Platform** là một nền tảng dữ liệu đô thị thông minh, được xây dựng cho cuộc thi PMNM 2025. Sứ mệnh của dự án là giải quyết bài toán cốt lõi về **tính sẵn sàng cao (High Availability)** và **chống quá tải (Overload Protection)** khi thu thập dữ liệu từ hàng triệu cảm biến trong thành phố.

---

## 1. Vấn đề Đặt ra

Trong một Thành phố Thông minh, dữ liệu được thu thập từ vô số nguồn (sensor, camera AI, người dân). Tuy nhiên, các kiến trúc truyền thống thường gặp vấn đề nghiêm trọng:

- **Quá tải & Sập hệ thống:** Khi xảy ra sự kiện lớn (kẹt xe, ô nhiễm, thiên tai), hàng triệu thiết bị đồng loạt PUSH dữ liệu về máy chủ trung tâm, gây nghẽn và sập toàn bộ hệ thống.
- **Mất dữ liệu:** Khi hệ thống sập, toàn bộ dữ liệu quan trọng đang gửi đến sẽ bị mất, không thể phục hồi.
- **Không có ưu tiên:** Dữ liệu cảnh báo khẩn cấp (ví dụ: `Cháy`) bị xử lý lẫn lộn và chậm trễ ngang hàng với dữ liệu thống kê thông thường (ví dụ: `Nhiệt độ`).

## 2. Giải pháp của SmartCity-Platform

SmartCity-Platform giải quyết triệt để vấn đề này bằng một kiến trúc thu thập dữ liệu (Ingestion) độc đáo và vững chắc.

> Thay vì để sensor PUSH dữ liệu trực tiếp vào hệ thống lõi, chúng tôi áp dụng mô hình **PULL chủ động** thông qua một lớp **Đệm (Edge Storage Buffer)**.

Kiến trúc này hoạt động như sau:

1.  **Lớp Đệm (Edge Storage):** Tất cả sensor PUSH dữ liệu thô vào một hệ thống đệm (ví dụ: NATS/RabbitMQ). Lớp này có khả năng chịu tải cực cao.
2.  **Smart Agent (PULL Model):** Các "Agent" thông minh của chúng tôi sẽ **chủ động PULL** dữ liệu từ lớp đệm để xử lý. Điều này cho phép chúng tôi kiểm soát 100% tốc độ xử lý, đảm bảo hệ thống lõi không bao giờ bị quá tải.
3.  **Chuẩn hóa NGSI-LD:** Sau khi PULL, Smart Agent sẽ chuẩn hóa dữ liệu thô sang định dạng **NGSI-LD** (theo yêu cầu đề bài) trước khi đưa vào Context Broker.

## 3. Các Nguyên tắc Cốt lõi

Hoạt động của SmartCity-Platform dựa trên bốn nguyên tắc chính:

- **Tính Sẵn sàng Cao (High Availability):** Mô hình PULL + Buffer đảm bảo hệ thống không bao giờ sập do quá tải đầu vào. Nếu Agent quá tải, nó sẽ tự động **ngừng PULL** dữ liệu không quan trọng để bảo vệ hệ thống.
- **Xử lý Ưu tiên Thông minh:** Smart Agent áp dụng logic **Nóng / Ấm / Lạnh**. Dữ liệu "Nóng" (cảnh báo) luôn được ưu tiên PULL và xử lý tức thì (delay bằng giây), trong khi dữ liệu "Lạnh" (thống kê) sẽ được xử lý sau.
- **Tuân thủ Chuẩn Quốc tế:** Nền tảng tuân thủ 100% tiêu chuẩn **NGSI-LD** và **FIWARE Smart Data Models** (yêu cầu bắt buộc của đề thi).
- **Linh hoạt & Dễ triển khai:** Toàn bộ hệ thống được đóng gói bằng **Docker Compose**, cho phép bất kỳ ai cũng có thể khởi chạy toàn bộ nền tảng chỉ bằng một lệnh.

## 4. Tầm nhìn

SmartCity-Platform được kỳ vọng trở thành một kiến trúc tham chiếu (reference architecture) cho các hệ thống Smart City tại Việt Nam, chứng minh rằng có thể xây dựng một nền tảng vừa tuân thủ chuẩn quốc tế, vừa đảm bảo hoạt động **ổn định, tin cậy và không bao giờ sập** ngay cả dưới áp lực dữ liệu lớn nhất.