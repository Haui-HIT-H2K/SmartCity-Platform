<!--

  Copyright 2025 Haui.HIT - H2K

  Licensed under the Apache License, Version 2.0

  http://www.apache.org/licenses/LICENSE-2.0

-->

# Smart City Dashboard - IoT Monitoring Platform

A real-time dashboard for monitoring and managing Smart City IoT infrastructure, built with Nuxt 3 and featuring a cyberpunk-themed dark UI.

![Dashboard Preview](https://img.shields.io/badge/Status-Production%20Ready-brightgreen)
![Nuxt 3](https://img.shields.io/badge/Nuxt-3.x-00DC82)
![TypeScript](https://img.shields.io/badge/TypeScript-5.x-blue)

## 📸 UI Preview

### Dashboard - Real-time Monitoring
![Smart City Dashboard](C:/Users/hoang.nguyenhuy3/.gemini/antigravity/brain/f5944823-a01e-4baf-adbe-66dfbce7b099/dashboard_preview_1764209854404.png)
*Real-time monitoring with edge node status, live charts, and tiered storage statistics*

### Data Explorer
![Data Explorer Page](C:/Users/hoang.nguyenhuy3/.gemini/antigravity/brain/f5944823-a01e-4baf-adbe-66dfbce7b099/data_explorer_1764209886172.png)
*Advanced data filtering and pagination with professional table design*

## 🚀 Features

### Real-time Monitoring
- **Edge Node Status**: Live monitoring of all edge storage nodes with online/offline indicators
- **Data Ingestion Visualization**: Real-time line charts showing incoming and processed data rates
- **Tiered Storage Statistics**: HOT (Redis), WARM (MongoDB), and COLD (MongoDB Archive) data counts
- **Auto-refresh**: Polls backend API every 2 seconds for latest metrics

### Data Explorer
- **Advanced Filtering**: Filter by data type (HOT/WARM/COLD) and sensor ID
- **Paginated Table**: Server-side pagination for efficient data browsing
- **Detailed View**: Click any record to view full details
- **Export Ready**: Structured data display for easy export

### System Control
- **Manual Sync**: Trigger data synchronization from all edge nodes
- **System Reset**: Clear all data for demo purposes (with confirmation)
- **Action History**: Track all manual operations with timestamps
- **Status Monitoring**: Real-time backend connection and node status

## 🛠️ Tech Stack

- **Framework**: Nuxt 3 (SSR Mode)
- **Language**: TypeScript
- **Styling**: TailwindCSS with custom cyberpunk theme
- **Charts**: ECharts for real-time data visualization
- **State Management**: Pinia
- **Icons**: Lucide Vue Next
- **HTTP Client**: Nuxt `useFetch` composable

## 📋 Prerequisites

- Node.js 20.x or higher
- npm or yarn
- Backend API running on `http://localhost:8080` (Spring Boot Smart City Platform)

## 🔧 Installation

1. **Clone the repository**
```bash
git clone <repository-url>
cd FE-MNM
```

2. **Install dependencies**
```bash
npm install
```

3. **Configure environment variables**
```bash
cp .env.example .env
# Edit .env to configure API base URL if needed
```

4. **Start development server**
```bash
npm run dev
```

The dashboard will be available at `http://localhost:3000`

## 📦 Available Scripts

- `npm run dev` - Start development server with hot reload
- `npm run build` - Build for production
- `npm run preview` - Preview production build locally
- `npm run generate` - Generate static site (if needed)

## 🐳 Docker Deployment

### Build Docker Image
```bash
docker build -t smart-city-dashboard .
```

### Run with Docker
```bash
docker run -p 3000:3000 smart-city-dashboard
```

### Using Docker Compose
```bash
docker-compose up -d
```

## 🏗️ Project Structure

```
FE-MNM/
├── assets/
│   └── css/
│       └── main.css              # Global styles and Tailwind config
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
│   └── default.vue               # Default layout with sidebar
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

The dashboard features a **cyberpunk/Smart City aesthetic**:

- **Color Palette**: 
  - Primary: Neon Blue (`#00f0ff`)
  - Accents: Neon Green, Purple, Pink
  - Background: Dark gradients (`#0a0e27` to `#1a1f3a`)
  
- **Effects**:
  - Glassmorphism cards with backdrop blur
  - Neon glow shadows on interactive elements
  - Smooth animations and transitions
  - Pulse animations for online indicators

- **Typography**: Inter font family for clean, modern text

## 📡 API Integration

The dashboard connects to the following backend endpoints:

- `GET /api/stats` - System statistics (HOT/WARM/COLD counts, rates)
- `GET /api/nodes` - Edge node status and information
- `GET /api/data` - Paginated city data records with filters
- `POST /api/sync/trigger` - Trigger manual data synchronization
- `POST /api/system/reset` - Reset system data (demo purposes)

API requests are proxied through Nuxt to avoid CORS issues. Configure the proxy in `nuxt.config.ts`:

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

The dashboard uses polling to achieve real-time updates:

1. **System Stats**: Polls every 2 seconds via `useSystemStats` composable
2. **Chart Updates**: Automatically updates when new data arrives
3. **Auto-cleanup**: Polling stops when component unmounts

## 🌐 Browser Support

- Chrome/Edge (latest)
- Firefox (latest)
- Safari (latest)

## 📝 Environment Variables

Create a `.env` file based on `.env.example`:

```env
# API Configuration
NUXT_PUBLIC_API_BASE=http://localhost:8080

# Application Configuration
NODE_ENV=development
```

## 🚀 Production Deployment

1. Build the application:
```bash
npm run build
```

2. Start the production server:
```bash
node .output/server/index.mjs
```

Or use Docker for containerized deployment (see Docker section above).

## 📚 Implementation Walkthrough

### Component Architecture

#### UI Components

**Card Component** - [components/ui/Card.vue](file:///c:/Users/hoang.nguyenhuy3/FE-MNM/components/ui/Card.vue)
- Base glassmorphism card with variants (default, primary, success, danger)
- Supports title, icon, and custom content slots
- Hover effects with neon glow

**StatCard Component** - [components/ui/StatCard.vue](file:///c:/Users/hoang.nguyenhuy3/FE-MNM/components/ui/StatCard.vue)
- Large number display for KPIs with animated counters
- Color variants for HOT (red), WARM (yellow), COLD (blue) data tiers
- Trend indicators and subtitle support

**NodeCard Component** - [components/ui/NodeCard.vue](file:///c:/Users/hoang.nguyenhuy3/FE-MNM/components/ui/NodeCard.vue)
- Edge node status display with pulse animations
- Shows host:port, status badge, and last ping time
- Differentiates online (green glow) vs offline (dimmed) nodes

**Button Component** - [components/ui/Button.vue](file:///c:/Users/hoang.nguyenhuy3/FE-MNM/components/ui/Button.vue)
- Multiple variants: primary, danger, ghost, secondary
- Size options: small, medium, large
- Loading state with spinner, icon placement support

**RealtimeLineChart** - [components/charts/RealtimeLineChart.vue](file:///c:/Users/hoang.nguyenhuy3/FE-MNM/components/charts/RealtimeLineChart.vue)
- ECharts integration with dark cyberpunk theme
- Dual-line chart (Incoming Rate vs Processed Rate)
- Auto-scrolling time axis with gradient fills
- Maintains rolling window of 30 data points

### State Management (Pinia)

#### System Store - [stores/system.ts](file:///c:/Users/hoang.nguyenhuy3/FE-MNM/stores/system.ts)

**State:**
- `stats`: HOT/WARM/COLD counts, incoming/processed rates
- `edgeNodes`: Array of edge node configurations
- `isLoading`, `error`, `lastUpdate`: UI state

**Actions:**
- `fetchStats()`: Poll system statistics from backend
- `fetchEdgeNodes()`: Get edge node status
- `syncData()`: Trigger manual data synchronization

**Getters:**
- `onlineNodes`: Filters nodes with status 'online'
- `offlineNodes`: Filters nodes with status 'offline'
- `hasActiveNodes`: Boolean check for any active nodes

#### Data Store - [stores/data.ts](file:///c:/Users/hoang.nguyenhuy3/FE-MNM/stores/data.ts)

**State:**
- `data`: Array of CityData records
- `filter`: Type, sensorId, page, pageSize
- `total`, `totalPages`: Pagination metadata

**Actions:**
- `fetchData()`: GET /api/data with filters
- `setFilter()`: Update filter criteria
- `nextPage()`, `prevPage()`, `goToPage()`: Pagination controls

### Composables

**useSystemStats** - [composables/useSystemStats.ts](file:///c:/Users/hoang.nguyenhuy3/FE-MNM/composables/useSystemStats.ts)
- Auto-polling every 2 seconds (configurable)
- Lifecycle management: starts on mount, stops on unmount
- Returns reactive computed properties for stats, nodes, errors

**useSystemControl** - [composables/useSystemControl.ts](file:///c:/Users/hoang.nguyenhuy3/FE-MNM/composables/useSystemControl.ts)
- `syncNow()`: Trigger manual data sync
- `resetSystem()`: Clear all data with confirmation
- Handles success/error states with notifications

### Page Implementations

#### Dashboard (index.vue)
- Grid of edge node status cards
- Real-time line chart for data ingestion
- Three stat cards for HOT/WARM/COLD data
- Auto-refresh every 2 seconds
- Error handling with visual alerts

#### Data Explorer (data-explorer.vue)
- Filter controls (type dropdown, sensor ID input)
- Paginated data table with colored type badges
- Server-side pagination with page controls
- Loading states and empty state handling

#### System Control (system-control.vue)
- Manual sync action with loading indicator
- System reset with confirmation modal
- Action history tracking (last 10 operations)
- System status indicators

### Styling Approach

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
- Pulse effects for online status indicators
- Fade-in animations for stat cards
- Smooth transitions on all interactive elements
- Loading spinners for async operations

### Quick Start Guide

1. **Ensure Backend is Running:**
   ```bash
   # Spring Boot backend should be on port 8080
   curl http://localhost:8080/api/stats
   ```

2. **Start Dashboard:**
   ```bash
   npm run dev
   # Dashboard runs on http://localhost:3000
   ```

3. **Navigate Features:**
   - **Dashboard**: Real-time monitoring homepage
   - **Data Explorer**: Browse and filter IoT data
   - **System Control**: Manual sync and system management

4. **Test Real-time Updates:**
   - Trigger data sync from System Control
   - Watch charts update automatically on Dashboard
   - Filter data in Data Explorer

### Troubleshooting

**Backend Connection Issues:**
- Verify backend is running: `curl http://localhost:8080/api/stats`
- Check proxy configuration in `nuxt.config.ts`
- Review browser console for CORS errors

**Build Errors:**
- Clear `.nuxt` folder: `rm -rf .nuxt`
- Reinstall dependencies: `npm install`
- Check Node.js version: `node --version` (should be 20.x+)

**Styling Issues:**
- Verify Tailwind is configured: check `tailwind.config.js`
- Clear browser cache
- Check that `assets/css/main.css` is imported

## 🤝 Integration with Backend

This dashboard is designed to work with the Smart City Platform backend (Spring Boot). Ensure the backend is running and accessible before starting the dashboard.

Backend features required:
- Dynamic Edge Node Discovery (DNS Registry)
- RabbitMQ Data Ingestion
- 3-Tier Storage (Redis + MongoDB)
- REST API endpoints for stats and data retrieval

## 📄 License

MIT License - feel free to use this project for your own purposes.

## 👨‍💻 Development

Built with ❤️ for the OLP 2025 Smart City Platform project.

For issues or questions, please open an issue in the repository.

---

**Note**: This is a monitoring dashboard. For data simulation and backend services, refer to the corresponding Python Data Simulator and Spring Boot Backend repositories.
