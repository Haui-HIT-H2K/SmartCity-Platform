<template>
  <UIModal :isOpen="isOpen" :title="getAlertTitle" @close="$emit('close')">
    <div v-if="alert" class="alert-details">
      <div class="alert-header">
        <div class="alert-icon" :class="`alert-icon-${alert.type}`">
          {{ getAlertIcon(alert.type) }}
        </div>
        <div class="alert-meta">
          <div class="alert-type">{{ getAlertTypeName(alert.type) }}</div>
          <div class="alert-time">{{ formatTime(alert.timestamp) }}</div>
        </div>
      </div>

      <div class="detail-grid">
        <div class="detail-item">
          <div class="detail-label">📍 Location</div>
          <div class="detail-value">
            {{ alert.lat.toFixed(6) }}, {{ alert.lon.toFixed(6) }}
          </div>
        </div>

        <div class="detail-item">
          <div class="detail-label">🔍 Source</div>
          <div class="detail-value">{{ alert.source }}</div>
        </div>

        <div class="detail-item full-width">
          <div class="detail-label">📝 Description</div>
          <div class="detail-value">{{ alert.description }}</div>
        </div>

        <div class="detail-item full-width">
          <div class="detail-label">🆔 Alert ID</div>
          <div class="detail-value code">{{ alert.id }}</div>
        </div>
      </div>
    </div>
  </UIModal>
</template>

<script setup lang="ts">
import type { Alert } from '~/types/alerts';

const props = defineProps<{
  isOpen: boolean;
  alert: Alert | null;
}>();

defineEmits<{
  close: [];
}>();

const getAlertIcon = (type: string) => {
  const icons: Record<string, string> = {
    fire: '🔥',
    traffic: '⚠️',
    aqi: '☣️',
    other: '📢',
  };
  return icons[type] || icons.other;
};

const getAlertTypeName = (type: string) => {
  const names: Record<string, string> = {
    fire: 'Fire Detection',
    traffic: 'Traffic Incident',
    aqi: 'Hazardous Air Quality',
    other: 'General Alert',
  };
  return names[type] || names.other;
};

const getAlertTitle = computed(() => {
  if (!props.alert) return 'Alert Details';
  return `${getAlertTypeName(props.alert.type)} Alert`;
});

const formatTime = (timestamp: string) => {
  const date = new Date(timestamp);
  const now = new Date();
  const diff = now.getTime() - date.getTime();
  const seconds = Math.floor(diff / 1000);
  const minutes = Math.floor(seconds / 60);
  const hours = Math.floor(minutes / 60);

  if (seconds < 60) return `${seconds}s ago`;
  if (minutes < 60) return `${minutes}m ago`;
  if (hours < 24) return `${hours}h ago`;
  
  return date.toLocaleString();
};
</script>

<style scoped>
.alert-details {
  min-width: 500px;
}

.alert-header {
  display: flex;
  align-items: center;
  gap: var(--spacing-md);
  margin-bottom: var(--spacing-xl);
  padding: var(--spacing-lg);
  background: var(--color-surface);
  border-radius: var(--radius-lg);
}

.alert-icon {
  width: 64px;
  height: 64px;
  border-radius: var(--radius-lg);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 32px;
  flex-shrink: 0;
}

.alert-icon-fire {
  background: linear-gradient(135deg, rgba(239, 68, 68, 0.2), rgba(220, 38, 38, 0.3));
  box-shadow: 0 0 30px rgba(239, 68, 68, 0.3);
}

.alert-icon-traffic {
  background: linear-gradient(135deg, rgba(245, 158, 11, 0.2), rgba(217, 119, 6, 0.3));
  box-shadow: 0 0 30px rgba(245, 158, 11, 0.3);
}

.alert-icon-aqi {
  background: linear-gradient(135deg, rgba(139, 92, 246, 0.2), rgba(124, 58, 237, 0.3));
  box-shadow: 0 0 30px rgba(139, 92, 246, 0.3);
}

.alert-icon-other {
  background: linear-gradient(135deg, rgba(59, 130, 246, 0.2), rgba(37, 99, 235, 0.3));
  box-shadow: 0 0 30px rgba(59, 130, 246, 0.3);
}

.alert-meta {
  flex: 1;
}

.alert-type {
  font-size: 1.25rem;
  font-weight: 600;
  color: var(--color-text);
  margin-bottom: var(--spacing-xs);
}

.alert-time {
  font-size: 0.875rem;
  color: var(--color-text-muted);
}

.detail-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: var(--spacing-lg);
}

.detail-item {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-xs);
}

.detail-item.full-width {
  grid-column: 1 / -1;
}

.detail-label {
  font-size: 0.875rem;
  font-weight: 500;
  color: var(--color-text-secondary);
}

.detail-value {
  font-size: 1rem;
  color: var(--color-text);
  padding: var(--spacing-sm) var(--spacing-md);
  background: var(--color-surface);
  border-radius: var(--radius);
  border: 1px solid var(--color-border);
}

.detail-value.code {
  font-family: 'Courier New', monospace;
  font-size: 0.875rem;
  word-break: break-all;
}

@media (max-width: 768px) {
  .alert-details {
    min-width: auto;
  }

  .detail-grid {
    grid-template-columns: 1fr;
  }
}
</style>
