<template>
  <button
    class="icon-button"
    :class="{ 'has-notification': hasNotification }"
    @click="$emit('click')"
    :title="title"
  >
    <slot />
    <span v-if="hasNotification" class="notification-badge"></span>
  </button>
</template>

<script setup lang="ts">
defineProps<{
  title?: string;
  hasNotification?: boolean;
}>();

defineEmits<{
  click: [];
}>();
</script>

<style scoped>
.icon-button {
  position: relative;
  width: 48px;
  height: 48px;
  border: none;
  border-radius: var(--radius-lg);
  background: var(--color-surface);
  backdrop-filter: blur(12px);
  border: 1px solid var(--color-border);
  color: var(--color-text);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  transition: all var(--transition);
  box-shadow: var(--shadow);
}

.icon-button:hover {
  background: var(--color-surface-hover);
  transform: translateY(-2px);
  box-shadow: var(--shadow-lg);
}

.icon-button:active {
  transform: translateY(0);
}

.notification-badge {
  position: absolute;
  top: 4px;
  right: 4px;
  width: 10px;
  height: 10px;
  background: var(--color-fire);
  border-radius: 50%;
  border: 2px solid var(--color-bg);
  animation: pulse 2s cubic-bezier(0.4, 0, 0.6, 1) infinite;
}

.has-notification::before {
  content: '';
  position: absolute;
  top: 4px;
  right: 4px;
  width: 10px;
  height: 10px;
  background: var(--color-fire);
  border-radius: 50%;
  opacity: 0.4;
  animation: ping 1.5s cubic-bezier(0, 0, 0.2, 1) infinite;
}
</style>
