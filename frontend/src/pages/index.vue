<template>
  <div class="dashboard">
    <!-- Loading Screen -->
    <UILoadingScreen v-if="isLoading" />

    <!-- Time Filter -->
    <div v-if="!isLoading" class="time-filter-container">
      <UITimeFilter v-model="selectedTimeFilter" />
    </div>

    <!-- Main Map -->
    <ClientOnly>
      <MapAlertMap
        ref="mapRef"
        :alerts="filteredAlerts"
        @alert-click="handleAlertClick"
      />
    </ClientOnly>

    <!-- Top-Right Controls -->
    <div v-if="!isLoading" class="top-controls">
      <UIIconButton title="Toggle Theme" @click="toggleTheme">
        {{ theme === "dark" ? "☀️" : "🌙" }}
      </UIIconButton>

      <UIIconButton
        title="Recent Alerts"
        :has-notification="recentAlerts.length > 0"
        @click="showRecentPanel = true"
      >
        🔔
      </UIIconButton>
    </div>

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

    <!-- Recent panel -->
    <AlertsRecentPanel
      :is-open="showRecentPanel"
      :alerts="recentAlerts"
      @close="showRecentPanel = false"
      @alert-click="handleRecentAlertClick"
    />

    <!-- Toast Notifications -->
    <div class="toast-container">
      <UIToastNotification
        v-for="toast in toastNotifications"
        :key="toast.id"
        :alert="toast"
        @close="removeToast(toast.id)"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import type { Alert } from "~/types/alerts";

// State management
const { activeAlerts, recentAlerts, addAlert, removeAlert } = useAlerts();
const { connected: wsConnected, connect, onMessage } = useWebSocket();
const { theme, toggleTheme } = useTheme();

// UI state
const isLoading = ref(true);
const showAlertModal = ref(false);
const showRecentPanel = ref(false);
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

// Toast notifications
const toastNotifications = ref<Alert[]>([]);
const MAX_TOASTS = 3;

const addToast = (alert: Alert) => {
  // Add to beginning of array
  toastNotifications.value.unshift(alert);

  // Keep only max 3 toasts
  if (toastNotifications.value.length > MAX_TOASTS) {
    toastNotifications.value = toastNotifications.value.slice(0, MAX_TOASTS);
  }
};

const removeToast = (id: string) => {
  toastNotifications.value = toastNotifications.value.filter(
    (t) => t.id !== id
  );
};

// Handle map marker clicks
const handleAlertClick = (alert: Alert) => {
  selectedAlert.value = alert;
  showAlertModal.value = true;
};

// Handle recent alert list clicks
const handleRecentAlertClick = (alert: Alert) => {
  // Pan map to alert location
  if (mapRef.value) {
    mapRef.value.panToAlert(alert);
  }
  // Show alert details
  selectedAlert.value = alert;
  showAlertModal.value = true;
};

// WebSocket message handler
onMessage.value = (message: WebSocketMessage) => {
  switch (message.type) {
    case "alert":
      const alert = message.data as Alert;
      addAlert(alert);
      // Show toast notification for new alert
      if (!isLoading.value) {
        addToast(alert);
      }
      break;
    case "alert_resolved":
      const resolvedData = message.data as { id: string };
      removeAlert(resolvedData.id);
      break;
    case "metrics":
      updateMetrics(message.data as any);
      break;
  }
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
