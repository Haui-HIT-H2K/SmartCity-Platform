<template>
  <Teleport to="body">
    <Transition name="slide">
      <div v-if="isOpen" class="panel-overlay" @click="$emit('close')">
        <div class="panel-container animate-slideInRight" @click.stop>
          <div class="panel-header">
            <h3>🔔 Recent Alerts</h3>
            <button class="close-button" @click="$emit('close')" title="Close">
              <span>✕</span>
            </button>
          </div>

          <div class="panel-content">
            <div v-if="alerts.length === 0" class="empty-state">
              <div class="empty-icon">📭</div>
              <p>No recent alerts</p>
            </div>

            <div v-else class="alerts-list">
              <div
                v-for="alert in alerts"
                :key="alert.id"
                class="alert-item"
                @click="handleAlertClick(alert)"
              >
                <div class="alert-icon-small" :class="`type-${alert.type}`">
                  {{ getAlertIcon(alert.type) }}
                </div>
                <div class="alert-info">
                  <div class="alert-type-name">{{ getAlertTypeName(alert.type) }}</div>
                  <div class="alert-location">{{ alert.source }}</div>
                  <div class="alert-time-small">{{ formatTime(alert.timestamp) }}</div>
                </div>
                <div class="alert-arrow">→</div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<script setup lang="ts">
import type { Alert } from '~/types/alerts';

const props = defineProps<{
  isOpen: boolean;
  alerts: Alert[];
}>();

const emit = defineEmits<{
  close: [];
  alertClick: [alert: Alert];
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
  
  return date.toLocaleDateString();
};

const handleAlertClick = (alert: Alert) => {
  emit('alertClick', alert);
  emit('close');
};

// Close on Escape key
onMounted(() => {
  const handleEscape = (e: KeyboardEvent) => {
    if (e.key === 'Escape' && props.isOpen) {
      emit('close');
    }
  };
  window.addEventListener('keydown', handleEscape);
  
  onUnmounted(() => {
    window.removeEventListener('keydown', handleEscape);
  });
});
</script>

<style scoped>
.panel-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  backdrop-filter: blur(2px);
  z-index: 1000;
}

.panel-container {
  position: fixed;
  top: 0;
  right: 0;
  bottom: 0;
  width: 100%;
  max-width: 400px;
  background: var(--color-bg-secondary);
  border-left: 1px solid var(--color-border);
  box-shadow: var(--shadow-xl);
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.panel-header {
  padding: var(--spacing-lg);
  border-bottom: 1px solid var(--color-border);
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--spacing-md);
  background: var(--color-surface);
}

.panel-header h3 {
  margin: 0;
  font-size: 1.25rem;
  font-weight: 600;
  color: var(--color-text);
}

.close-button {
  width: 32px;
  height: 32px;
  border: none;
  background: var(--color-bg-secondary);
  color: var(--color-text-secondary);
  border-radius: var(--radius);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  transition: all var(--transition-fast);
  flex-shrink: 0;
}

.close-button:hover {
  background: var(--color-surface-hover);
  color: var(--color-text);
  transform: rotate(90deg);
}

.panel-content {
  flex: 1;
  overflow-y: auto;
  padding: var(--spacing-md);
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: var(--spacing-2xl);
  text-align: center;
  color: var(--color-text-muted);
}

.empty-icon {
  font-size: 4rem;
  margin-bottom: var(--spacing-md);
  opacity: 0.5;
}

.alerts-list {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-sm);
}

.alert-item {
  display: flex;
  align-items: center;
  gap: var(--spacing-md);
  padding: var(--spacing-md);
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  cursor: pointer;
  transition: all var(--transition);
}

.alert-item:hover {
  background: var(--color-surface-hover);
  transform: translateX(-4px);
  box-shadow: var(--shadow);
}

.alert-icon-small {
  width: 40px;
  height: 40px;
  border-radius: var(--radius);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  flex-shrink: 0;
}

.alert-icon-small.type-fire {
  background: linear-gradient(135deg, rgba(239, 68, 68, 0.2), rgba(220, 38, 38, 0.3));
}

.alert-icon-small.type-traffic {
  background: linear-gradient(135deg, rgba(245, 158, 11, 0.2), rgba(217, 119, 6, 0.3));
}

.alert-icon-small.type-aqi {
  background: linear-gradient(135deg, rgba(139, 92, 246, 0.2), rgba(124, 58, 237, 0.3));
}

.alert-icon-small.type-other {
  background: linear-gradient(135deg, rgba(59, 130, 246, 0.2), rgba(37, 99, 235, 0.3));
}

.alert-info {
  flex: 1;
  min-width: 0;
}

.alert-type-name {
  font-size: 0.9375rem;
  font-weight: 600;
  color: var(--color-text);
  margin-bottom: 2px;
}

.alert-location {
  font-size: 0.8125rem;
  color: var(--color-text-secondary);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.alert-time-small {
  font-size: 0.75rem;
  color: var(--color-text-muted);
  margin-top: 2px;
}

.alert-arrow {
  color: var(--color-text-muted);
  font-size: 1.25rem;
  flex-shrink: 0;
  transition: transform var(--transition);
}

.alert-item:hover .alert-arrow {
  transform: translateX(4px);
  color: var(--color-primary-light);
}

/* Transitions */
.slide-enter-active,
.slide-leave-active {
  transition: opacity var(--transition);
}

.slide-enter-from,
.slide-leave-to {
  opacity: 0;
}

.slide-enter-active .panel-container,
.slide-leave-active .panel-container {
  transition: transform var(--transition-slow) cubic-bezier(0.16, 1, 0.3, 1);
}

.slide-enter-from .panel-container,
.slide-leave-to .panel-container {
  transform: translateX(100%);
}

@media (max-width: 768px) {
  .panel-container {
    max-width: 100%;
  }
}
</style>
