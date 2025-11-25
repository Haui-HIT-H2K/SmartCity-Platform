<template>
  <div ref="mapContainer" class="map-container"></div>
</template>

<script setup lang="ts">
import L from 'leaflet';
import 'leaflet/dist/leaflet.css';
import type { Alert } from '~/types/alerts';

const props = defineProps<{
  alerts: Alert[];
}>();

const emit = defineEmits<{
  alertClick: [alert: Alert];
}>();

const mapContainer = ref<HTMLElement | null>(null);
let map: L.Map | null = null;
const markers = new Map<string, L.Marker>();

// Default center (can be adjusted to your city)
const DEFAULT_CENTER: L.LatLngExpression = [10.8231, 106.6297]; // Ho Chi Minh City
const DEFAULT_ZOOM = 12;

onMounted(() => {
  if (!mapContainer.value) return;

  // Initialize Leaflet map
  map = L.map(mapContainer.value, {
    center: DEFAULT_CENTER,
    zoom: DEFAULT_ZOOM,
    zoomControl: true,
  });

  // Add OpenStreetMap tiles
  L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
    attribution: '© OpenStreetMap contributors',
    maxZoom: 19,
  }).addTo(map);

  // Custom dark tile layer (optional, for better dark mode)
  // Uncomment to use dark tiles instead
  // L.tileLayer('https://tiles.stadiamaps.com/tiles/alidade_smooth_dark/{z}/{x}/{y}{r}.png', {
  //   attribution: '© Stadia Maps © OpenMapTiles © OpenStreetMap',
  //   maxZoom: 20,
  // }).addTo(map);
});

// Create custom icon for each alert type
const createAlertIcon = (type: string): L.DivIcon => {
  const iconConfig: Record<string, { emoji: string; color: string }> = {
    fire: { emoji: '🔥', color: '#ef4444' },
    traffic: { emoji: '⚠️', color: '#f59e0b' },
    aqi: { emoji: '☣️', color: '#8b5cf6' },
    other: { emoji: '📢', color: '#3b82f6' },
  };

  const config = iconConfig[type] || iconConfig.other;

  return L.divIcon({
    className: 'custom-alert-icon',
    html: `
      <div class="alert-marker" style="--marker-color: ${config.color}">
        <div class="marker-pulse"></div>
        <div class="marker-icon">${config.emoji}</div>
      </div>
    `,
    iconSize: [40, 40],
    iconAnchor: [20, 20],
  });
};

// Watch for alert changes and update markers
watch(
  () => props.alerts,
  (newAlerts) => {
    if (!map) return;

    // Get current alert IDs
    const currentIds = new Set(newAlerts.map(a => a.id));

    // Remove markers that no longer exist
    markers.forEach((marker, id) => {
      if (!currentIds.has(id)) {
        map!.removeLayer(marker);
        markers.delete(id);
      }
    });

    // Add or update markers
    newAlerts.forEach(alert => {
      const existingMarker = markers.get(alert.id);

      if (existingMarker) {
        // Update existing marker position if needed
        existingMarker.setLatLng([alert.lat, alert.lon]);
      } else {
        // Create new marker
        const icon = createAlertIcon(alert.type);
        const marker = L.marker([alert.lat, alert.lon], { icon })
          .addTo(map!);

        // Add click event
        marker.on('click', () => {
          emit('alertClick', alert);
        });

        markers.set(alert.id, marker);
      }
    });
  },
  { immediate: true, deep: true }
);

// Expose methods for programmatic control
const panToAlert = (alert: Alert) => {
  if (map) {
    map.setView([alert.lat, alert.lon], 16, {
      animate: true,
      duration: 1,
    });
  }
};

defineExpose({
  panToAlert,
});

onUnmounted(() => {
  if (map) {
    map.remove();
    map = null;
  }
  markers.clear();
});
</script>

<style>
/* Global styles for custom markers (not scoped) */
.custom-alert-icon {
  background: transparent !important;
  border: none !important;
}

.alert-marker {
  position: relative;
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.marker-pulse {
  position: absolute;
  width: 100%;
  height: 100%;
  border-radius: 50%;
  background: var(--marker-color);
  opacity: 0.4;
  animation: ping 1.5s cubic-bezier(0, 0, 0.2, 1) infinite;
}

.marker-icon {
  position: relative;
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: var(--marker-color);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.4);
  border: 2px solid rgba(255, 255, 255, 0.3);
  cursor: pointer;
  transition: transform 0.2s ease;
}

.marker-icon:hover {
  transform: scale(1.2);
}
</style>

<style scoped>
.map-container {
  width: 100%;
  height: 100%;
  position: relative;
}
</style>
