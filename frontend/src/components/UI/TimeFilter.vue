<template>
  <div class="time-filter">
    <label class="filter-label">
      <span class="filter-icon">🕐</span>
      <select v-model="selectedTime" class="filter-select">
        <option value="all">All Alerts</option>
        <option value="5">Last 5 minutes</option>
        <option value="15">Last 15 minutes</option>
        <option value="30">Last 30 minutes</option>
        <option value="45">Last 45 minutes</option>
        <option value="60">Last 1 hour</option>
      </select>
    </label>
  </div>
</template>

<script setup lang="ts">
const selectedTime = defineModel<string>({ default: '5' });
</script>

<style scoped>
.time-filter {
  display: inline-flex;
}

.filter-label {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  padding: var(--spacing-sm) var(--spacing-md);
  background: var(--color-surface);
  backdrop-filter: blur(12px);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow);
  cursor: pointer;
  transition: all var(--transition);
}

.filter-label:hover {
  background: var(--color-surface-hover);
  transform: translateY(-1px);
  box-shadow: var(--shadow-lg);
}

.filter-icon {
  font-size: 1.125rem;
  flex-shrink: 0;
}

.filter-select {
  background: transparent;
  border: none;
  color: var(--color-text);
  font-size: 0.875rem;
  font-weight: 500;
  cursor: pointer;
  outline: none;
  padding: 0;
  font-family: inherit;
}

.filter-select option {
  background: var(--color-bg);
  color: var(--color-text);
  padding: var(--spacing-sm);
}
</style>
