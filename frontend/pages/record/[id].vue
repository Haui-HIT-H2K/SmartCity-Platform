<template>
  <div class="min-h-screen p-6 space-y-6">
    <!-- Header -->
    <div class="flex items-center justify-between">
      <div class="flex items-center gap-4">
        <UiButton
          variant="ghost"
          size="sm"
          @click="router.back()"
          class="flex items-center gap-2"
        >
          <div class="flex gap-2 items-center">
            <ArrowLeft class="w-4 h-4" />
            <p>Back</p>
          </div>
        </UiButton>
        <h1 class="text-2xl font-bold text-gray-100">Record Detail</h1>
      </div>

      <div v-if="record" class="flex items-center gap-2">
        <span
          class="px-3 py-1 text-sm font-semibold rounded-full"
          :class="getTypeBadgeClass(record.type)"
        >
          {{ record.type }}
        </span>
        <UiButton
          variant="outline"
          size="sm"
          @click="downloadRecord"
          :disabled="isDownloading"
          class="flex items-center gap-2"
        >
          <Download :class="isDownloading ? 'animate-bounce' : ''" class="w-4 h-4" />
          <span>{{ isDownloading ? 'Downloading...' : 'Download' }}</span>
        </UiButton>
      </div>
    </div>

    <!-- Loading State -->
    <div v-if="pending" class="glass-card p-12 text-center">
      <div
        class="inline-block animate-spin rounded-full h-12 w-12 border-b-2 border-primary"
      ></div>
      <p class="text-gray-500 mt-4">Loading record...</p>
    </div>

    <!-- Error State -->
    <div v-else-if="error" class="glass-card p-12 text-center">
      <AlertCircle class="w-16 h-16 text-red-500 mx-auto mb-4" />
      <h3 class="text-lg font-semibold text-gray-200 mb-2">
        Error Loading Record
      </h3>
      <p class="text-gray-500">
        {{ error.message || "Failed to load record data" }}
      </p>
      <UiButton @click="refresh()" class="mt-4">
        <div class="flex gap-2 items-center">
          <RefreshCw class="w-4 h-4" />
          <p>Retry</p>
        </div>
      </UiButton>
    </div>

    <!-- Record Content -->
    <div v-else-if="record" class="space-y-6">
      <!-- Basic Information -->
      <div class="glass-card p-6">
        <h2
          class="text-lg font-semibold text-gray-200 mb-4 flex items-center gap-2"
        >
          <Info class="w-5 h-5 text-primary" />
          Basic Information
        </h2>
        <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
          <div class="space-y-1">
            <label class="text-sm text-gray-500">Record ID</label>
            <p class="text-gray-200 font-mono text-sm break-all">
              {{ record.id }}
            </p>
          </div>
          <div class="space-y-1">
            <label class="text-sm text-gray-500">Sensor ID</label>
            <p class="text-gray-200 font-medium">{{ record.sensorId }}</p>
          </div>
          <div class="space-y-1">
            <label class="text-sm text-gray-500">Data Type</label>
            <p>
              <span
                class="px-3 py-1 text-xs font-semibold rounded-full inline-block"
                :class="getTypeBadgeClass(record.type)"
              >
                {{ record.type }}
              </span>
            </p>
          </div>
          <div class="space-y-1">
            <label class="text-sm text-gray-500">Timestamp</label>
            <p class="text-gray-200 font-mono text-sm">
              {{ formatTimestamp(record.timestamp) }}
            </p>
          </div>
        </div>
      </div>

      <!-- Payload Data -->
      <div class="glass-card p-6">
        <h2
          class="text-lg font-semibold text-gray-200 mb-4 flex items-center gap-2"
        >
          <Code class="w-5 h-5 text-primary" />
          Payload Data
        </h2>
        <div
          class="bg-dark-lighter rounded-lg p-4 border border-dark-border overflow-x-auto custom-scrollbar"
        >
          <pre class="text-sm text-gray-300 font-mono">{{
            formatJSON(record.payload)
          }}</pre>
        </div>
      </div>

      <!-- Location -->
      <div v-if="record.location" class="glass-card p-6">
        <h2
          class="text-lg font-semibold text-gray-200 mb-4 flex items-center gap-2"
        >
          <MapPin class="w-5 h-5 text-primary" />
          Location
        </h2>
        <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
          <div class="space-y-1">
            <label class="text-sm text-gray-500">Latitude</label>
            <p class="text-gray-200 font-mono">
              {{ record.location.lat.toFixed(6) }}
            </p>
          </div>
          <div class="space-y-1">
            <label class="text-sm text-gray-500">Longitude</label>
            <p class="text-gray-200 font-mono">
              {{ record.location.lng.toFixed(6) }}
            </p>
          </div>
        </div>
      </div>

      <!-- Metadata -->
      <div class="glass-card p-6">
        <h2
          class="text-lg font-semibold text-gray-200 mb-4 flex items-center gap-2"
        >
          <FileText class="w-5 h-5 text-primary" />
          Metadata
        </h2>
        <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
          <div class="space-y-1">
            <label class="text-sm text-gray-500">Value</label>
            <p class="text-gray-200 font-semibold text-lg">
              {{ record.value?.toFixed(2) || "N/A" }}
            </p>
          </div>
          <div class="space-y-1">
            <label class="text-sm text-gray-500">Created At</label>
            <p class="text-gray-200 text-sm">
              {{ formatTimestamp(record.timestamp) }}
            </p>
          </div>
          <div class="space-y-1">
            <label class="text-sm text-gray-500">Source</label>
            <p class="text-gray-200 text-sm">{{ record.sensorId }}</p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import {
  ArrowLeft,
  Info,
  Code,
  MapPin,
  FileText,
  AlertCircle,
  RefreshCw,
  Download,
} from "lucide-vue-next";
import type { DataType } from "~/stores/data";

definePageMeta({
  layout: "default",
});

const route = useRoute();
const router = useRouter();
const id = route.params.id as string;

// Fetch record data
const {
  data: record,
  pending,
  error,
  refresh,
} = await useFetch(`/api/record/${id}`);

// Download state
const isDownloading = ref(false);

// Download function - uses localhost:8080 for browser downloads
const downloadRecord = async () => {
  if (isDownloading.value) return;
  
  try {
    isDownloading.value = true;
    
    // Always use localhost:8080 for browser downloads (not Docker hostname)
    const backendUrl = 'http://localhost:8080';
    const downloadUrl = `${backendUrl}/api/data/${id}/download`;
    
    console.log('Download URL:', downloadUrl);
    
    // Create temporary anchor and trigger download
    const link = document.createElement('a');
    link.href = downloadUrl;
    link.download = `record-${id}.json`;
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
  } catch (error) {
    console.error('Error downloading record:', error);
  } finally {
    isDownloading.value = false;
  }
};

// Utility functions
const getTypeBadgeClass = (type: DataType) => {
  const classes = {
    HOT: "bg-red-500/20 text-red-400 border border-red-500/40",
    WARM: "bg-yellow-500/20 text-yellow-400 border border-yellow-500/40",
    COLD: "bg-blue-500/20 text-blue-400 border border-blue-500/40",
  };
  return (
    classes[type] || "bg-gray-500/20 text-gray-400 border border-gray-500/40"
  );
};

const formatTimestamp = (timestamp: string) => {
  return new Date(timestamp).toLocaleString();
};

const formatJSON = (obj: any) => {
  if (!obj) return "No payload data";
  return JSON.stringify(obj, null, 2);
};
</script>
