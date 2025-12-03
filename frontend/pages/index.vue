<template>
  <div class="space-y-6">
    <!-- Section 1: Edge Node Status -->
    <div>
      <h3 class="text-lg font-semibold text-gray-900 dark:text-gray-200 mb-4 flex items-center gap-2">
        <Server class="w-5 h-5 text-primary" />
        Edge Node Status
        <span class="text-sm text-gray-600 dark:text-gray-500 ml-2">({{ onlineNodes.length }}/{{ edgeNodes.length }} online)</span>
      </h3>
      
      <!-- Debug Info -->
      <!-- <div class="mb-4 p-3 bg-yellow-100 dark:bg-yellow-900/20 rounded text-xs">
        <p><strong>Debug Info:</strong></p>
        <p>Edge Nodes Length: {{ edgeNodes.length }}</p>
        <p>Edge Nodes Data: {{ JSON.stringify(edgeNodes) }}</p>
      </div> -->
      
      <div v-if="edgeNodes.length > 0" class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-4">
        <UiNodeCard 
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
        <div class="p-4 space-y-4">
          <!-- Chart Header -->
          <div class="flex items-center justify-between">
            <p class="text-sm text-gray-400">{{ stats.rateHistory?.length || 0 }} data points</p>
            <div class="flex gap-4 text-xs">
              <div class="flex items-center gap-2">
                <div class="w-3 h-3 rounded-full bg-cyan-400"></div>
                <span class="text-gray-400">Incoming</span>
              </div>
              <div class="flex items-center gap-2">
                <div class="w-3 h-3 rounded-full bg-green-400"></div>
                <span class="text-gray-400">Processed</span>
              </div>
            </div>
          </div>

          <!-- Simple Data List (NO CHART - just verify data) -->
          <div v-if="stats.rateHistory && stats.rateHistory.length > 0" class="space-y-2 max-h-64 overflow-y-auto">
            <div 
              v-for="(snapshot, index) in stats.rateHistory.slice(-10)" 
              :key="index"
              class="flex justify-between items-center p-2 bg-gray-100 dark:bg-gray-800 rounded text-sm"
            >
              <span class="text-gray-600 dark:text-gray-400">{{ new Date(snapshot.timestamp).toLocaleTimeString() }}</span>
              <div class="flex gap-4">
                <span class="text-cyan-600 dark:text-cyan-400">↑ {{ snapshot.incomingRate }}/s</span>
                <span class="text-green-600 dark:text-green-400">↓ {{ snapshot.processedRate }}/s</span>
              </div>
            </div>
          </div>

          <!-- No Data -->
          <div v-else class="h-64 bg-gray-100 dark:bg-gray-900/50 rounded-lg flex items-center justify-center">
            <div class="text-center text-gray-500">
              <Activity class="w-12 h-12 mx-auto mb-2 opacity-50" />
              <p>Waiting for rate data...</p>
            </div>
          </div>
        </div>
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
          <p class="text-sm text-gray-600 dark:text-gray-400 mb-1">Total Records</p>
          <p class="text-2xl font-bold text-primary">{{ stats.totalCount.toLocaleString() }}</p>
        </div>
        <div>
          <p class="text-sm text-gray-600 dark:text-gray-400 mb-1">Incoming Rate</p>
          <p class="text-2xl font-bold text-neon-green">{{ stats.incomingRate }}/s</p>
        </div>
        <div>
          <p class="text-sm text-gray-600 dark:text-gray-400 mb-1">Processed Rate</p>
          <p class="text-2xl font-bold text-neon-green">{{ stats.processedRate }}/s</p>
        </div>
        <div>
          <p class="text-sm text-gray-600 dark:text-gray-400 mb-1">System Health</p>
          <div class="flex items-center justify-center gap-2">
            <div class="w-3 h-3 rounded-full bg-neon-green animate-pulse" />
            <p class="text-lg font-semibold text-gray-900 dark:text-gray-100">Healthy</p>
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

// Debug logging
watchEffect(() => {
  console.log('Edge Nodes:', edgeNodes.value)
  console.log('Edge Nodes Length:', edgeNodes.value.length)
  console.log('Online Nodes:', onlineNodes.value)
})

// Calculate bar height for chart
const calculateHeight = (rate: number) => {
  const maxRate = Math.max(
    ...((stats.value.rateHistory || []).flatMap(s => [s.incomingRate, s.processedRate])),
    10
  )
  return (rate / maxRate) * 100
}

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
