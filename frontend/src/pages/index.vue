<template>
  <div class="dashboard">
    <!-- Main Map -->
    <ClientOnly>
      <MapAlertMap
        ref="mapRef"
        :alerts="filteredAlerts"
        @alert-click="handleAlertClick"
      />
    </ClientOnly>

    <!-- Connection Status -->
    <div
      v-if="!isLoading"
      class="connection-status"
      :class="{ connected: wsConnected }"
    >
      <span class="status-dot"></span>
      <span class="status-text">
        {{ wsConnected ? "Connected" : "Connecting..." }}
      </span>
    </div>

    <!-- Modals and Panels -->
    <MapAlertModal
      :is-open="showAlertModal"
      :alert="selectedAlert"
      @close="showAlertModal = false"
    />
  </div>
</template>

<script setup lang="ts">
import type { Alert } from "~/types/alerts";

// State management
const { activeAlerts } = useAlerts();
const { connected: wsConnected, connect } = useWebSocket();

// UI state
const isLoading = ref(true);
const showAlertModal = ref(false);
const selectedAlert = ref<Alert | null>(null);
const mapRef = ref<any>(null);

// Time filter
const selectedTimeFilter = ref<string>("all");

// Filter alerts based on selected time
const filteredAlerts = computed(() => {
  if (selectedTimeFilter.value === "all") {
    return activeAlerts.value;
  }

  const minutes = parseInt(selectedTimeFilter.value);
  const cutoffTime = Date.now() - minutes * 60 * 1000;

  return activeAlerts.value.filter((alert) => {
    const alertTime = new Date(alert.timestamp).getTime();
    return alertTime >= cutoffTime;
  });
});

// Handle map marker clicks
const handleAlertClick = (alert: Alert) => {
  selectedAlert.value = alert;
  showAlertModal.value = true;
};

// Connect to WebSocket on mount
onMounted(() => {
  const wsUrl =
    (useRuntimeConfig().public.wsUrl as string) || "ws://localhost:9090";
  connect(wsUrl);

  // Hide loading screen after initial setup
  setTimeout(() => {
    isLoading.value = false;
  }, 1500);
});

// Set page metadata
useHead({
  title: "Smart City Dashboard",
  meta: [
    {
      name: "description",
      content: "Real-time Smart City Data Platform Dashboard",
    },
  ],
  link: [{ rel: "icon", type: "image/x-icon", href: "/favicon.ico" }],
});
</script>

<style scoped>
.dashboard {
  width: 100%;
  height: 100vh;
  position: relative;
  overflow: hidden;
}

.time-filter-container {
  position: absolute;
  top: var(--spacing-lg);
  left: 50%;
  transform: translateX(-50%);
  z-index: 500;
}

.top-controls {
  position: absolute;
  top: var(--spacing-lg);
  right: var(--spacing-lg);
  display: flex;
  flex-direction: column;
  gap: var(--spacing-md);
  z-index: 500;
}

.connection-status {
  position: absolute;
  bottom: var(--spacing-lg);
  left: var(--spacing-lg);
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  padding: var(--spacing-sm) var(--spacing-md);
  background: var(--color-surface);
  backdrop-filter: blur(12px);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow);
  z-index: 500;
  font-size: 0.875rem;
}

.status-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: var(--color-text-muted);
  transition: background var(--transition);
}

.connection-status.connected .status-dot {
  background: var(--color-success);
  box-shadow: 0 0 8px var(--color-success);
  animation: pulse 2s cubic-bezier(0.4, 0, 0.6, 1) infinite;
}

.status-text {
  color: var(--color-text-secondary);
  font-weight: 500;
}

.connection-status.connected .status-text {
  color: var(--color-text);
}

.toast-container {
  position: fixed;
  bottom: var(--spacing-lg);
  right: var(--spacing-lg);
  z-index: 1000;
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  pointer-events: none;
}

.toast-container > * {
  pointer-events: auto;
}
</style>
