<template>
  <div 
    class="glass-card p-5 transition-all duration-300"
    :class="statusClass"
  >
    <div class="flex items-start justify-between mb-3">
      <div class="flex-1">
        <div class="flex items-center gap-2 mb-2">
          <h4 class="font-semibold text-gray-900 dark:text-gray-100">{{ node.name }}</h4>
          <div 
            class="relative w-2 h-2 rounded-full"
            :class="node.status === 'online' ? 'bg-neon-green' : 'bg-red-500'"
          >
            <div 
              v-if="node.status === 'online'"
              class="absolute inset-0 rounded-full bg-neon-green animate-ping opacity-75"
            />
          </div>
        </div>
        
        <div class="space-y-1 text-sm">
          <p class="text-gray-600 dark:text-gray-400">
            <span class="text-gray-700 dark:text-gray-500">Host:</span>
            <span class="ml-2 font-mono text-primary">{{ node.host }}:{{ node.port }}</span>
          </p>
          <p v-if="node.lastPing" class="text-gray-700 dark:text-gray-500 text-xs">
            Last ping: {{ formatTime(node.lastPing) }}
          </p>
        </div>
      </div>
    </div>
    
    <div class="flex items-center justify-between pt-3 border-t border-light-border/50 dark:border-dark-border/50">
      <span 
        class="text-xs px-3 py-1 rounded-full font-medium"
        :class="node.status === 'online' ? 'status-online' : 'status-offline'"
      >
        {{ node.status.toUpperCase() }}
      </span>
      
      <Server class="w-5 h-5 text-gray-500 dark:text-gray-600" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { Server } from 'lucide-vue-next'
import type { EdgeNode } from '~/stores/system'

interface Props {
  node: EdgeNode
}

const props = defineProps<Props>()

const statusClass = computed(() => {
  return props.node.status === 'online' 
    ? 'border-neon-green/30 hover:shadow-[0_0_20px_rgba(0,255,65,0.3)]' 
    : 'border-red-500/30 opacity-70'
})

const formatTime = (timestamp: string) => {
  const date = new Date(timestamp)
  const now = new Date()
  const diff = now.getTime() - date.getTime()
  
  if (diff < 60000) return 'Just now'
  if (diff < 3600000) return `${Math.floor(diff / 60000)}m ago`
  return date.toLocaleTimeString()
}
</script>
