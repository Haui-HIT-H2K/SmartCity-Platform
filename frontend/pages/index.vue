<template>
  <div class="space-y-6">
    <!-- Section 1: Edge Node Status -->
    <div>
      <h3 class="text-lg font-semibold text-gray-900 dark:text-gray-200 mb-4 flex items-center gap-2">
        <Server class="w-5 h-5 text-primary" />
        Edge Node Status
        <span class="text-sm text-gray-600 dark:text-gray-500 ml-2">({{ onlineNodes.length }}/{{ edgeNodes.length }} online)</span>
      </h3>
      
      <div v-if="edgeNodes.length > 0" class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-4">
        <NodeCard 
          v-for="node in edgeNodes" 
          :key="node.id" 
          :node="node" 
        />
      </div>
      
      <div v-else class="glass-card p-8 text-center">
        <AlertCircle class="w-12 h-12 text-gray-500 dark:text-gray-600 mx-auto mb-3" />
        <p class="text-gray-600 dark:text-gray-500">No edge nodes configured</p>
      </div>
    </div>

    <!-- Section 2: Data Ingestion Chart -->
    <div>
      <h3 class="text-lg font-semibold text-gray-900 dark:text-gray-200 mb-4 flex items-center gap-2">
        <Activity class="w-5 h-5 text-primary" />
        Data Ingestion Rate
        <span v-if="lastUpdate" class="text-xs text-gray-600 dark:text-gray-600 ml-2">
          Updated {{ formatLastUpdate(lastUpdate) }}
        </span>
      </h3>
      
      <UiCard>
        <ClientOnly>
          <RealtimeLineChart 
            :incoming-rate="stats.incomingRate"
            :processed-rate="stats.processedRate"
            height="400px"
          />
          <template #fallback>
            <div class="flex items-center justify-center h-[400px] text-gray-600 dark:text-gray-500">
              Loading chart...
            </div>
          </template>
        </ClientOnly>
      </UiCard>
    </div>

    <!-- Section 3: Tiered Storage Statistics -->
    <div>
      <h3 class="text-lg font-semibold text-gray-900 dark:text-gray-200 mb-4 flex items-center gap-2">
        <Database class="w-5 h-5 text-primary" />
        Tiered Storage Statistics
      </h3>
      
      <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
        <StatCard
          label="HOT Data"
          :value="stats.hotCount"
          :icon="Flame"
          variant="hot"
          subtitle="In-memory Redis cache"
        />
        
        <StatCard
          label="WARM Data"
          :value="stats.warmCount"
          :icon="Layers"
          variant="warm"
          subtitle="Recent MongoDB records"
        />
        
        <StatCard
          label="COLD Data"
          :value="stats.coldCount"
          :icon="HardDrive"
          variant="cold"
          subtitle="Archived long-term storage"
        />
      </div>
    </div>

    <!-- Quick Stats Summary -->
    <div class="glass-card p-6">
      <div class="grid grid-cols-2 md:grid-cols-4 gap-4 text-center">
        <div>
          <p class="text-sm text-gray-600 dark:text-gray-500 mb-1">Total Records</p>
          <p class="text-2xl font-bold text-primary">{{ stats.totalCount.toLocaleString() }}</p>
        </div>
        <div>
          <p class="text-sm text-gray-500 mb-1">Incoming Rate</p>
          <p class="text-2xl font-bold text-neon-green">{{ stats.incomingRate }}/s</p>
        </div>
        <div>
          <p class="text-sm text-gray-500 mb-1">Processed Rate</p>
          <p class="text-2xl font-bold text-neon-green">{{ stats.processedRate }}/s</p>
        </div>
        <div>
          <p class="text-sm text-gray-500 mb-1">System Health</p>
          <div class="flex items-center justify-center gap-2">
            <div class="w-3 h-3 rounded-full bg-neon-green animate-pulse" />
            <p class="text-lg font-semibold text-gray-900 dark:text-gray-300">Healthy</p>
          </div>
        </div>
      </div>
    </div>

    <!-- Error Display -->
    <div v-if="error" class="glass-card p-4 border-red-500/40 bg-red-500/10 dark:bg-red-500/10">
      <div class="flex items-start gap-3">
        <AlertCircle class="w-5 h-5 text-red-500 flex-shrink-0 mt-0.5" />
        <div>
          <p class="text-red-600 dark:text-red-400 font-medium">Connection Error</p>
          <p class="text-sm text-gray-700 dark:text-gray-400 mt-1">{{ error }}</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { 
  Server, 
  Activity, 
  Database, 
  Flame, 
  Layers, 
  HardDrive, 
  AlertCircle 
} from 'lucide-vue-next'

definePageMeta({
  layout: 'default',
})

// Use the composable to get real-time data
const { 
  stats, 
  edgeNodes, 
  onlineNodes, 
  lastUpdate, 
  error,
  isPolling 
} = useSystemStats(2000)

const formatLastUpdate = (date: Date | null) => {
  if (!date) return 'never'
  
  const now = new Date()
  const diff = now.getTime() - date.getTime()
  
  if (diff < 5000) return 'just now'
  if (diff < 60000) return `${Math.floor(diff / 1000)}s ago`
  return formatTime(date)
}

const formatTime = (date: Date) => {
  return date.toLocaleTimeString()
}
</script>
