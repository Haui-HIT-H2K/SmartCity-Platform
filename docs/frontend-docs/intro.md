SPDX-License-Identifier: Apache-2.0
<!--

  Copyright 2025 Haui.HIT - H2K

  Licensed under the Apache License, Version 2.0

  http://www.apache.org/licenses/LICENSE-2.0

-->

---
sidebar_position: 1
title: Giới Thiệu Tổng Quan
---

**Open Linked Hub** là nền tảng kết nối và đồng bộ hóa dữ liệu giữa nhiều hệ thống khác nhau.  
Phần **Frontend** chịu trách nhiệm hiển thị giao diện người dùng, quản lý trạng thái ứng dụng và giao tiếp với **Backend API** qua các endpoint bảo mật.

---

## 1. Mục tiêu

- Cung cấp UI/UX nhất quán, dễ sử dụng cho người dùng cuối.
- Kết nối an toàn với backend API để thực hiện xác thực, quản lý dữ liệu người dùng, và hiển thị nội dung động.
- Hỗ trợ state management tập trung bằng Pinia, giúp đồng bộ dữ liệu giữa các thành phần UI.
- Dễ dàng mở rộng module, thêm trang hoặc plugin mới mà không ảnh hưởng đến các phần khác.
- Tối ưu hiệu năng nhờ lazy loading, auto-import, và tree-shaking của Nuxt 3.

## 2. Thiết kế hệ thống

## 3. Cấu trúc dự án

```
frontend/
├── .env.example           # File mẫu biến môi trường
├── .gitignore             # Loại trừ file khi commit Git
├── .prettierrc            # Cấu hình Prettier
├── .prettierignore        # Bỏ qua định dạng một số file
├── nuxt.config.ts         # Cấu hình chính của Nuxt
├── package.json           # Thông tin package và script
├── tailwind.config.js     # Cấu hình Tailwind CSS
├── tsconfig.json          # Cấu hình TypeScript
├── public/                # Static assets (favicon, robots.txt)
├── src/                   # Mã nguồn chính của ứng dụng
│   ├── app.vue            # Root component
│   ├── app.config.ts      # Cấu hình chung cho app (theme, meta, ...)
│   ├── assets/            # Tài nguyên tĩnh (CSS, hình ảnh)
│   ├── components/        # Component dùng chung (AppHeader, ...)
│   ├── composables/       # Custom composables
│   ├── layouts/           # Giao diện khung trang (default, blank)
│   ├── middleware/        # Middleware điều hướng (auth, ...)
│   ├── pages/             # Các trang ứng dụng (route tự động)
│   ├── plugins/           # Plugin cài vào Nuxt (axios instance)
│   ├── stores/            # Pinia stores (state management)
│   ├── types/             # Kiểu dữ liệu TypeScript
│   └── utils/             # Hàm tiện ích dùng chung
```

## 4. Cài đặt & Chạy dự án

### Yêu cầu:

- Node.js: ≥ 18.x
- Yarn: ≥ 1.22.x
- Trình duyệt hiện đại hỗ trợ ES6+

### Cài đặt:

```bash
# Di chuyển đến thư mục frontend
cd frontend

# Cài đặt các gói phụ thuộc
yarn install
```

### Biến môi trường

Tạo file `.env` (tham khảo `.env.example`) với nội dung như:

```bash
API_BASE_URL=https://api.openlinkedhub.com
```

### Chạy chế độ phát triển:

```bash
# Chạy ứng dụng development
yarn dev
```

Ứng dụng sẽ chạy tại:
👉 [http://localhost:3000](http://localhost:3000)

### Build production:

```bash
yarn build
```

### Preview bản build:

```bash
yarn preview
```
