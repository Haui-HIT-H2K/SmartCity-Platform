SPDX-License-Identifier: Apache-2.0
<!--

  Copyright 2025 Haui.HIT - H2K

  Licensed under the Apache License, Version 2.0

  http://www.apache.org/licenses/LICENSE-2.0

-->

# Smart City Dashboard - IoT Monitoring Platform

Dashboard real-time để giám sát và quản lý hạ tầng IoT Smart City, được xây dựng với Nuxt 3 và có giao diện dark UI theo phong cách cyberpunk.

![Dashboard Preview](https://img.shields.io/badge/Status-Production%20Ready-brightgreen)
![Nuxt 3](https://img.shields.io/badge/Nuxt-3.x-00DC82)
![TypeScript](https://img.shields.io/badge/TypeScript-5.x-blue)

## 📸 Xem trước Giao diện

### Dashboard - Real-time Monitoring
![Smart City Dashboard](C:/Users/hoang.nguyenhuy3/.gemini/antigravity/brain/f5944823-a01e-4baf-adbe-66dfbce7b099/dashboard_preview_1764209854404.png)
*Giám sát real-time với trạng thái edge node, biểu đồ trực tiếp, và thống kê tiered storage*

### Data Explorer
![Data Explorer Page](C:/Users/hoang.nguyenhuy3/.gemini/antigravity/brain/f5944823-a01e-4baf-adbe-66dfbce7b099/data_explorer_1764209886172.png)
*Lọc dữ liệu nâng cao và phân trang với thiết kế bảng chuyên nghiệp*

## 🚀 Tính năng

### Real-time Monitoring
- **Trạng thái Edge Node**: Giám sát trực tiếp tất cả edge storage node với chỉ báo online/offline
- **Trực quan Data Ingestion**: Biểu đồ đường real-time hiển thị tỷ lệ dữ liệu incoming và processed
- **Thống kê Tiered Storage**: Số lượng dữ liệu HOT (Redis), WARM (MongoDB), và COLD (MongoDB Archive)
- **Auto-refresh**: Poll backend API mỗi 2 giây cho metric mới nhất

### Data Explorer
- **Lọc Nâng cao**: Lọc theo data type (HOT/WARM/COLD) và sensor ID
- **Bảng Phân trang**: Server-side pagination để duyệt dữ liệu hiệu quả
- **Xem Chi tiết**: Click vào bất kỳ record nào để xem chi tiết đầy đủ
- **Sẵn sàng Export**: Hiển thị dữ liệu có cấu trúc để export dễ dàng

### System Control
- **Manual Sync**: Trigger đồng bộ dữ liệu từ tất cả edge node
- **System Reset**: Xóa toàn bộ dữ liệu cho mục đích demo (với xác nhận)
- **Lịch sử Action**: Theo dõi tất cả thao tác thủ công với timestamp
- **Giám sát Trạng thái**: Kết nối backend và trạng thái node real-time

## 🛠️ Tech Stack

- **Framework**: Nuxt 3 (SSR Mode)
- **Language**: TypeScript
- **Styling**: TailwindCSS với custom cyberpunk theme
- **Charts**: ECharts cho trực quan dữ liệu real-time
- **State Management**: Pinia
- **Icons**: Lucide Vue Next
- **HTTP Client**: Nuxt `useFetch` composable

## 📋 Yêu cầu Hệ thống

- Node.js 20.x hoặc cao hơn
- npm hoặc yarn
- Backend API đang chạy trên `http://localhost:8080` (Spring Boot Smart City Platform)

## 🔧 Cài đặt

1. **Clone repository**
```bash
git clone <repository-url>
cd FE-MNM
```

2. **Cài đặt dependencies**
```bash
npm install
```

3. **Cấu hình biến môi trường**
```bash
cp .env.example .env
# Chỉnh sửa .env để cấu hình API base URL nếu cần
```

4. **Khởi động development server**
```bash
npm run dev
```

Dashboard sẽ khả dụng tại `http://localhost:3000`

## 📦 Các Script có sẵn

- `npm run dev` - Khởi động development server với hot reload
- `npm run build` - Build cho production
- `npm run preview` - Xem trước production build ở local
- `npm run generate` - Tạo static site (nếu cần)

## 🐳 Docker Deployment

### Build Docker Image
```bash
docker build -t smart-city-dashboard .
```

### Chạy với Docker
```bash
docker run -p 3000:3000 smart-city-dashboard
```

### Sử dụng Docker Compose
```bash
docker-compose up -d
```

## 🏗️ Cấu trúc Project

```
FE-MNM/
├── assets/
│   └── css/
│       └── main.css              # Global styles và Tailwind config
├── components/
│   ├── charts/
│   │   └── RealtimeLineChart.vue # Real-time data chart
│   ├── layout/
│   │   ├── Sidebar.vue           # Navigation sidebar
│   │   └── Header.vue            # App header
│   └── ui/
│       ├── Card.vue              # Base card component
│       ├── StatCard.vue          # Statistics card
│       ├── NodeCard.vue          # Edge node card
│       └── Button.vue            # Reusable button
├── composables/
│   ├── useSystemStats.ts         # System statistics polling
│   └── useSystemControl.ts       # System control actions
├── layouts/
│   └── default.vue               # Default layout với sidebar
├── pages/
│   ├── index.vue                 # Dashboard homepage
│   ├── data-explorer.vue         # Data browsing page
│   └── system-control.vue        # System control panel
├── stores/
│   ├── system.ts                 # System state management
│   └── data.ts                   # Data state management
├── nuxt.config.ts                # Nuxt configuration
├── tailwind.config.js            # Tailwind CSS configuration
├── Dockerfile                    # Docker build configuration
└── docker-compose.yml            # Docker Compose setup
```

## 🎨 Design Theme

Dashboard có **cyberpunk/Smart City aesthetic**:

- **Bảng màu**: 
  - Primary: Neon Blue (`#00f0ff`)
  - Accents: Neon Green, Purple, Pink
  - Background: Dark gradient (`#0a0e27` đến `#1a1f3a`)
  
- **Hiệu ứng**:
  - Glassmorphism card với backdrop blur
  - Neon glow shadow trên interactive element
  - Animation và transition mượt mà
  - Pulse animation cho online indicator

- **Typography**: Font family Inter cho text hiện đại, rõ ràng

## 📡 API Integration

Dashboard kết nối tới các backend endpoint sau:

- `GET /api/stats` - Thống kê hệ thống (HOT/WARM/COLD count, rate)
- `GET /api/nodes` - Trạng thái và thông tin edge node
- `GET /api/data` - Phân trang city data record với filter
- `POST /api/sync/trigger` - Trigger manual data synchronization
- `POST /api/system/reset` - Reset system data (mục đích demo)

API request được proxy qua Nuxt để tránh vấn đề CORS. Cấu hình proxy trong `nuxt.config.ts`:

```typescript
nitro: {
  devProxy: {
    '/api': {
      target: 'http://localhost:8080',
      changeOrigin: true,
    }
  }
}
```

## 🔄 Real-time Updates

Dashboard sử dụng polling để đạt được cập nhật real-time:

1. **System Stats**: Poll mỗi 2 giây qua `useSystemStats` composable
2. **Chart Update**: Tự động cập nhật khi dữ liệu mới đến
3. **Auto-cleanup**: Dừng polling khi component unmount

## 🌐 Hỗ trợ Trình duyệt

- Chrome/Edge (latest)
- Firefox (latest)
- Safari (latest)

## 📝 Biến Môi trường

Tạo file `.env` dựa trên `.env.example`:

```env
# API Configuration
NUXT_PUBLIC_API_BASE=http://localhost:8080

# Application Configuration
NODE_ENV=development
```

## 🚀 Production Deployment

1. Build ứng dụng:
```bash
npm run build
```

2. Khởi động production server:
```bash
node .output/server/index.mjs
```

Hoặc sử dụng Docker cho containerized deployment (xem phần Docker ở trên).

## 📚 Chi tiết Triển khai

### Kiến trúc Component

#### UI Components

**Card Component** - [components/ui/Card.vue](file:///c:/Users/hoang.nguyenhuy3/FE-MNM/components/ui/Card.vue)
- Base glassmorphism card với variant (default, primary, success, danger)
- Hỗ trợ title, icon, và custom content slot
- Hover effect với neon glow

**StatCard Component** - [components/ui/StatCard.vue](file:///c:/Users/hoang.nguyenhuy3/FE-MNM/components/ui/StatCard.vue)
- Hiển thị số lớn cho KPI với animated counter
- Color variant cho HOT (đỏ), WARM (vàng), COLD (xanh) data tier
- Trend indicator và subtitle support

**NodeCard Component** - [components/ui/NodeCard.vue](file:///c:/Users/hoang.nguyenhuy3/FE-MNM/components/ui/NodeCard.vue)
- Hiển thị trạng thái edge node với pulse animation
- Hiển thị host:port, status badge, và last ping time
- Phân biệt online (green glow) vs offline (dimmed) node

**Button Component** - [components/ui/Button.vue](file:///c:/Users/hoang.nguyenhuy3/FE-MNM/components/ui/Button.vue)
- Nhiều variant: primary, danger, ghost, secondary
- Tùy chọn size: small, medium, large
- Loading state với spinner, icon placement support

**RealtimeLineChart** - [components/charts/RealtimeLineChart.vue](file:///c:/Users/hoang.nguyenhuy3/FE-MNM/components/charts/RealtimeLineChart.vue)
- Tích hợp ECharts với dark cyberpunk theme
- Dual-line chart (Incoming Rate vs Processed Rate)
- Auto-scrolling time axis với gradient fill
- Duy trì rolling window 30 data point

### State Management (Pinia)

#### System Store - [stores/system.ts](file:///c:/Users/hoang.nguyenhuy3/FE-MNM/stores/system.ts)

**State:**
- `stats`: HOT/WARM/COLD count, incoming/processed rate
- `edgeNodes`: Mảng edge node configuration
- `isLoading`, `error`, `lastUpdate`: UI state

**Actions:**
- `fetchStats()`: Poll system statistics từ backend
- `fetchEdgeNodes()`: Lấy trạng thái edge node
- `syncData()`: Trigger manual data synchronization

**Getters:**
- `onlineNodes`: Lọc node với status 'online'
- `offlineNodes`: Lọc node với status 'offline'
- `hasActiveNodes`: Kiểm tra boolean cho node hoạt động

#### Data Store - [stores/data.ts](file:///c:/Users/hoang.nguyenhuy3/FE-MNM/stores/data.ts)

**State:**
- `data`: Mảng CityData record
- `filter`: Type, sensorId, page, pageSize
- `total`, `totalPages`: Pagination metadata

**Actions:**
- `fetchData()`: GET /api/data với filter
- `setFilter()`: Cập nhật filter criteria
- `nextPage()`, `prevPage()`, `goToPage()`: Pagination control

### Composables

**useSystemStats** - [composables/useSystemStats.ts](file:///c:/Users/hoang.nguyenhuy3/FE-MNM/composables/useSystemStats.ts)
- Auto-polling mỗi 2 giây (có thể cấu hình)
- Lifecycle management: bắt đầu khi mount, dừng khi unmount
- Trả về reactive computed property cho stats, node, error

**useSystemControl** - [composables/useSystemControl.ts](file:///c:/Users/hoang.nguyenhuy3/FE-MNM/composables/useSystemControl.ts)
- `syncNow()`: Trigger manual data sync
- `resetSystem()`: Xóa toàn bộ dữ liệu với xác nhận
- Xử lý success/error state với notification

### Triển khai Page

#### Dashboard (index.vue)
- Grid của edge node status card
- Real-time line chart cho data ingestion
- Ba stat card cho HOT/WARM/COLD data
- Auto-refresh mỗi 2 giây
- Xử lý lỗi với visual alert

#### Data Explorer (data-explorer.vue)
- Filter control (type dropdown, sensor ID input)
- Paginated data table với colored type badge
- Server-side pagination với page control
- Loading state và empty state handling

#### System Control (system-control.vue)
- Manual sync action với loading indicator
- System reset với confirmation modal
- Action history tracking (10 operation gần nhất)
- System status indicator

### Cách tiếp cận Styling

**Glassmorphism Effect:**
```css
.glass-card {
  background: rgba(26, 31, 58, 0.4);
  backdrop-filter: blur(12px);
  border: 1px solid rgba(42, 47, 74, 0.5);
  box-shadow: 0 8px 32px 0 rgba(0, 0, 0, 0.37);
}
```

**Neon Glow:**
```css
.neon-border {
  border: 1px solid rgba(0, 240, 255, 0.3);
  box-shadow: 0 0 10px rgba(0, 240, 255, 0.3);
  transition: all 0.3s ease;
}

.neon-border:hover {
  box-shadow: 0 0 20px rgba(0, 240, 255, 0.5);
}
```

**Animations:**
- Pulse effect cho online status indicator
- Fade-in animation cho stat card
- Smooth transition trên tất cả interactive element
- Loading spinner cho async operation

### Hướng dẫn Bắt đầu Nhanh

1. **Đảm bảo Backend đang chạy:**
   ```bash
   # Spring Boot backend nên ở port 8080
   curl http://localhost:8080/api/stats
   ```

2. **Khởi động Dashboard:**
   ```bash
   npm run dev
   # Dashboard chạy trên http://localhost:3000
   ```

3. **Điều hướng Tính năng:**
   - **Dashboard**: Homepage giám sát real-time
   - **Data Explorer**: Duyệt và lọc IoT data
   - **System Control**: Manual sync và quản lý hệ thống

4. **Test Real-time Update:**
   - Trigger data sync từ System Control
   - Xem chart tự động cập nhật trên Dashboard
   - Lọc dữ liệu trong Data Explorer

### Xử lý Sự cố

**Vấn đề Kết nối Backend:**
- Kiểm tra backend đang chạy: `curl http://localhost:8080/api/stats`
- Kiểm tra cấu hình proxy trong `nuxt.config.ts`
- Review browser console tìm lỗi CORS

**Lỗi Build:**
- Xóa folder `.nuxt`: `rm -rf .nuxt`
- Cài đặt lại dependencies: `npm install`
- Kiểm tra phiên bản Node.js: `node --version` (nên là 20.x+)

**Vấn đề Styling:**
- Kiểm tra Tailwind đã được cấu hình: kiểm tra `tailwind.config.js`
- Xóa browser cache
- Kiểm tra `assets/css/main.css` đã được import

## 🤝 Tích hợp với Backend

Dashboard này được thiết kế để hoạt động với Smart City Platform backend (Spring Boot). Đảm bảo backend đang chạy và có thể truy cập trước khi khởi động dashboard.

Các tính năng backend cần thiết:
- Dynamic Edge Node Discovery (DNS Registry)
- RabbitMQ Data Ingestion
- 3-Tier Storage (Redis + MongoDB)
- REST API endpoint cho stats và data retrieval

## 📄 Giấy phép

MIT License - thoải mái sử dụng project này cho mục đích của bạn.

## 👨‍💻 Development

Được xây dựng với ❤️ cho dự án OLP 2025 Smart City Platform.

Nếu có vấn đề hoặc câu hỏi, vui lòng mở issue trong repository.

---

**Lưu ý**: Đây là monitoring dashboard. Đối với data simulation và backend service, tham khảo các repository Python Data Simulator và Spring Boot Backend tương ứng.
