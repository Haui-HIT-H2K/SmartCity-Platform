<!--
  Copyright 2025 Haui.HIT - H2K

  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
-->

<template>
  <Teleport to="body">
    <div
      v-if="show"
      class="fixed inset-0 z-50 flex items-center justify-center p-4"
    >
      <!-- Backdrop -->
      <div
        class="absolute inset-0 bg-black/60 backdrop-blur-sm"
        @click="close"
      ></div>

      <!-- Modal Content -->
      <div
        class="relative bg-white dark:bg-dark-card rounded-2xl shadow-2xl w-full max-w-4xl max-h-[90vh] overflow-hidden"
      >
        <!-- Header -->
        <div
          class="flex items-center justify-between p-4 border-b border-gray-200 dark:border-dark-border"
        >
          <div class="flex items-center gap-3">
            <div class="p-2 bg-primary/10 rounded-lg">
              <MapPin class="w-5 h-5 text-primary" />
            </div>
            <div>
              <h3 class="text-lg font-semibold text-gray-900 dark:text-gray-100">
                Vị Trí Sensor
              </h3>
              <p class="text-sm text-gray-600 dark:text-gray-400">
                {{ sensorId }}
              </p>
            </div>
          </div>
          <button
            @click="close"
            class="p-2 hover:bg-gray-100 dark:hover:bg-dark-lighter rounded-lg transition-colors"
          >
            <X class="w-5 h-5 text-gray-600 dark:text-gray-400" />
          </button>
        </div>

        <!-- Map Container -->
        <div class="relative h-[500px]">
          <div ref="mapContainer" class="w-full h-full"></div>
          
          <!-- Legend -->
          <div
            class="absolute bottom-4 left-4 bg-white dark:bg-dark-card p-3 rounded-lg shadow-lg text-sm"
          >
            <div class="font-semibold text-gray-900 dark:text-gray-100 mb-2">
              Chú thích
            </div>
            <div class="flex items-center gap-2 mb-1">
              <div class="w-3 h-3 bg-primary rounded-full"></div>
              <span class="text-gray-700 dark:text-gray-300">Vị trí Sensor</span>
            </div>
            <div class="flex items-center gap-2 mb-1">
              <div class="w-3 h-3 bg-red-500 rounded-full"></div>
              <span class="text-gray-700 dark:text-gray-300">Hoàng Sa (Việt Nam)</span>
            </div>
            <div class="flex items-center gap-2">
              <div class="w-3 h-3 bg-red-500 rounded-full"></div>
              <span class="text-gray-700 dark:text-gray-300">Trường Sa (Việt Nam)</span>
            </div>
          </div>
        </div>

        <!-- Footer -->
        <div
          class="p-4 border-t border-gray-200 dark:border-dark-border bg-gray-50 dark:bg-dark-lighter/50"
        >
          <div class="flex items-center justify-between text-sm">
            <div class="text-gray-600 dark:text-gray-400">
              Tọa độ: <span class="font-mono">{{ location.lat.toFixed(4) }}°N, {{ location.lng.toFixed(4) }}°E</span>
            </div>
            <div class="text-gray-500 dark:text-gray-500 italic">
              Biển Đông (East Sea) - Chủ quyền Việt Nam
            </div>
          </div>
        </div>
      </div>
    </div>
  </Teleport>
</template>

<script setup lang="ts">
import { MapPin, X } from 'lucide-vue-next'
import type L from 'leaflet'

interface Location {
  lat: number
  lng: number
}

const props = defineProps<{
  show: boolean
  sensorId: string
  location: Location
}>()

const emit = defineEmits<{
  (e: 'close'): void
}>()

const mapContainer = ref<HTMLElement | null>(null)
let map: L.Map | null = null
let leaflet: typeof L | null = null

const close = () => {
  emit('close')
}

// Vietnamese sovereignty locations
const HOANG_SA = { lat: 16.5, lng: 112.0 }
const TRUONG_SA = { lat: 10.0, lng: 114.0 }
const EAST_SEA_LABEL = { lat: 14.0, lng: 113.0 }

const initMap = async () => {
  if (!mapContainer.value || !process.client) return

  // Dynamic import Leaflet (client-side only)
  leaflet = await import('leaflet')
  const L = leaflet.default || leaflet

  // Fix Leaflet default icon issue
  delete (L.Icon.Default.prototype as any)._getIconUrl
  L.Icon.Default.mergeOptions({
    iconRetinaUrl: 'https://unpkg.com/leaflet@1.9.4/dist/images/marker-icon-2x.png',
    iconUrl: 'https://unpkg.com/leaflet@1.9.4/dist/images/marker-icon.png',
    shadowUrl: 'https://unpkg.com/leaflet@1.9.4/dist/images/marker-shadow.png',
  })

  // Create map centered on location with higher zoom for city view
  map = L.map(mapContainer.value).setView([props.location.lat, props.location.lng], 14)

  // Add CartoDB Voyager tile layer (no Chinese labels)
  L.tileLayer('https://{s}.basemaps.cartocdn.com/rastertiles/voyager/{z}/{x}/{y}{r}.png', {
    attribution: '© <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors © <a href="https://carto.com/attributions">CARTO</a>',
    subdomains: 'abcd',
    maxZoom: 20
  }).addTo(map)

  // Create custom icons
  const sensorIcon = L.divIcon({
    className: 'custom-sensor-icon',
    html: `<div class="w-8 h-8 bg-primary rounded-full border-4 border-white shadow-lg flex items-center justify-center">
             <div class="w-3 h-3 bg-white rounded-full"></div>
           </div>`,
    iconSize: [32, 32],
    iconAnchor: [16, 16]
  })

  const vietnamIcon = L.divIcon({
    className: 'custom-vietnam-icon',
    html: `<div class="w-6 h-6 bg-red-500 rounded-full border-2 border-yellow-400 shadow-lg flex items-center justify-center">
             <span class="text-yellow-400 text-xs font-bold">★</span>
           </div>`,
    iconSize: [24, 24],
    iconAnchor: [12, 12]
  })

  // Add sensor location marker
  L.marker([props.location.lat, props.location.lng], { icon: sensorIcon })
    .addTo(map)
    .bindPopup(`<b>${props.sensorId}</b><br/>Vị trí: ${props.location.lat.toFixed(4)}°N, ${props.location.lng.toFixed(4)}°E`)
    .openPopup()

  // Add Hoang Sa marker (Vietnamese sovereignty)
  L.marker([HOANG_SA.lat, HOANG_SA.lng], { icon: vietnamIcon })
    .addTo(map)
    .bindTooltip('Quần đảo Hoàng Sa<br/><b>Việt Nam</b>', { 
      permanent: true, 
      direction: 'top',
      className: 'vietnam-tooltip'
    })

  // Add Truong Sa marker (Vietnamese sovereignty)
  L.marker([TRUONG_SA.lat, TRUONG_SA.lng], { icon: vietnamIcon })
    .addTo(map)
    .bindTooltip('Quần đảo Trường Sa<br/><b>Việt Nam</b>', { 
      permanent: true, 
      direction: 'top',
      className: 'vietnam-tooltip'
    })

  // Add East Sea label
  const eastSeaIcon = L.divIcon({
    className: 'east-sea-label',
    html: `<div class="text-blue-600 font-semibold text-lg bg-white/80 px-3 py-1 rounded shadow-sm whitespace-nowrap">
             Biển Đông<br/><span class="text-sm">(East Sea)</span>
           </div>`,
    iconSize: [120, 50],
    iconAnchor: [60, 25]
  })
  
  L.marker([EAST_SEA_LABEL.lat, EAST_SEA_LABEL.lng], { icon: eastSeaIcon, interactive: false })
    .addTo(map)
}

const destroyMap = () => {
  if (map) {
    map.remove()
    map = null
  }
}

watch(() => props.show, async (newVal) => {
  if (newVal) {
    await nextTick()
    setTimeout(() => {
      initMap()
    }, 100)
  } else {
    destroyMap()
  }
})

onUnmounted(() => {
  destroyMap()
})
</script>

<style>
/* Custom styles for Leaflet components */
.custom-sensor-icon,
.custom-vietnam-icon,
.east-sea-label {
  background: transparent !important;
  border: none !important;
}

.vietnam-tooltip {
  background: rgba(255, 255, 255, 0.95) !important;
  border: 2px solid #dc2626 !important;
  border-radius: 8px !important;
  padding: 4px 8px !important;
  font-size: 12px !important;
  text-align: center !important;
}

.vietnam-tooltip::before {
  border-top-color: #dc2626 !important;
}

/* Ensure Leaflet map inherits dark mode */
.leaflet-container {
  font-family: inherit;
}
</style>
