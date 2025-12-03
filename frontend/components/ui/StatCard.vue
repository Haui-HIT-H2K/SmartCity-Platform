<template>
  <div 
    class="glass-card p-6 neon-border transition-all duration-300 hover:scale-105"
    :class="colorClass"
  >
    <div class="flex items-start justify-between mb-4">
      <component 
        :is="icon" 
        class="w-10 h-10"
        :class="iconColor"
      />
      <span v-if="trend" class="text-sm font-medium" :class="trendColor">
        {{ trend }}
      </span>
    </div>
    
    <div class="space-y-2">
      <p class="text-sm text-gray-600 dark:text-gray-400 uppercase tracking-wider">
        {{ label }}
      </p>
      <p class="text-4xl font-bold text-primary dark:text-neon animate-fade-in">
        {{ formattedValue }}
      </p>
      <p v-if="subtitle" class="text-xs text-gray-700 dark:text-gray-500">
        {{ subtitle }}
      </p>
    </div>
  </div>
</template>

<script setup lang="ts">
import type { Component } from 'vue'
import { Database, Layers, HardDrive } from 'lucide-vue-next'

interface Props {
  label: string
  value: number
  icon: Component
  subtitle?: string
  trend?: string
  variant?: 'hot' | 'warm' | 'cold'
}

const props = withDefaults(defineProps<Props>(), {
  variant: 'hot',
})

const formattedValue = computed(() => {
  return props.value.toLocaleString()
})

const colorClass = computed(() => {
  switch (props.variant) {
    case 'hot':
      return 'border-red-500/40'
    case 'warm':
      return 'border-yellow-500/40'
    case 'cold':
      return 'border-blue-500/40'
    default:
      return 'border-primary/40'
  }
})

const iconColor = computed(() => {
  switch (props.variant) {
    case 'hot':
      return 'text-red-500'
    case 'warm':
      return 'text-yellow-500'
    case 'cold':
      return 'text-blue-500'
    default:
      return 'text-primary'
  }
})

const trendColor = computed(() => {
  if (!props.trend) return ''
  return props.trend.startsWith('+') ? 'text-neon-green' : 'text-red-500'
})
</script>
