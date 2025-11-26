<template>
  <Transition name="toast">
    <div v-if="visible" class="toast" :class="`toast-${alert.type}`">
      <div class="toast-icon">{{ getIcon(alert.type) }}</div>
      <div class="toast-content">
        <div class="toast-title">{{ getTitle(alert.type) }}</div>
        <div class="toast-message">{{ alert.description }}</div>
        <div class="toast-location">{{ formatLocation(alert.lat, alert.lon) }}</div>
      </div>
      <button class="toast-close" @click="close" aria-label="Close">×</button>
    </div>
  </Transition>
</template>

<script setup lang="ts">
import type { Alert } from '~/types/alerts';

const props = defineProps<{
  alert: Alert;
  duration?: number;
}>();

const emit = defineEmits<{
  close: [];
}>();

const visible = ref(false);
let timeout: ReturnType<typeof setTimeout>;

const getIcon = (type: string): string => {
  const icons: Record<string, string> = {
    fire: '🔥',
    traffic: '⚠️',
    aqi: '☣️',
  };
  return icons[type] || '📢';
};

const getTitle = (type: string): string => {
  const titles: Record<string, string> = {
    fire: 'Fire Incident',
    traffic: 'Traffic Alert',
    aqi: 'Air Quality Warning',
  };
  return titles[type] || 'New Alert';
};

const formatLocation = (lat: number, lon: number): string => {
  return `${lat.toFixed(4)}°, ${lon.toFixed(4)}°`;
};

const close = () => {
  visible.value = false;
  clearTimeout(timeout);
  setTimeout(() => emit('close'), 300); // Wait for animation
};

onMounted(() => {
  // Show toast
  visible.value = true;
  
  // Auto-dismiss after duration
  const duration = props.duration || 5000;
  timeout = setTimeout(close, duration);
});

onUnmounted(() => {
  clearTimeout(timeout);
});
</script>

<style scoped>
.toast {
  display: flex;
  align-items: flex-start;
  gap: var(--spacing-md);
  min-width: 320px;
  max-width: 400px;
  padding: var(--spacing-md);
  background: var(--color-surface);
  backdrop-filter: blur(12px);
  border: 1px solid var(--color-border);
  border-left: 4px solid var(--toast-color);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-xl);
  margin-bottom: var(--spacing-md);
  cursor: pointer;
  transition: all var(--transition);
}

.toast:hover {
  transform: translateX(-4px);
  box-shadow: 0 20px 30px -5px rgba(0, 0, 0, 0.8);
}

.toast-fire {
  --toast-color: var(--color-fire);
}

.toast-traffic {
  --toast-color: var(--color-traffic);
}

.toast-aqi {
  --toast-color: var(--color-aqi);
}

.toast-icon {
  font-size: 1.5rem;
  flex-shrink: 0;
}

.toast-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: var(--spacing-xs);
}

.toast-title {
  font-weight: 600;
  font-size: 0.9375rem;
  color: var(--color-text);
}

.toast-message {
  font-size: 0.875rem;
  color: var(--color-text-secondary);
  line-height: 1.4;
}

.toast-location {
  font-size: 0.75rem;
  color: var(--color-text-muted);
  font-family: 'Courier New', monospace;
}

.toast-close {
  flex-shrink: 0;
  width: 24px;
  height: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: transparent;
  border: none;
  color: var(--color-text-muted);
  font-size: 1.5rem;
  cursor: pointer;
  border-radius: var(--radius);
  transition: all var(--transition-fast);
  line-height: 1;
  padding: 0;
}

.toast-close:hover {
  background: var(--color-surface-hover);
  color: var(--color-text);
}

/* Toast animations */
.toast-enter-active,
.toast-leave-active {
  transition: all 0.3s cubic-bezier(0.16, 1, 0.3, 1);
}

.toast-enter-from {
  transform: translateX(400px);
  opacity: 0;
}

.toast-leave-to {
  transform: translateX(400px);
  opacity: 0;
}
</style>
