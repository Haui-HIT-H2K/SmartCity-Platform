<template>
  <div class="space-y-6">
    <!-- Control Panel -->
    <div class="glass-card p-8">
      <h3 class="text-2xl font-semibold text-gray-200 mb-2 flex items-center gap-2">
        <Settings class="w-6 h-6 text-primary" />
        System Control Panel
      </h3>
      <p class="text-gray-500 mb-8">
        Trigger manual operations and manage system state
      </p>

      <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
        <!-- Sync Data Card -->
        <div class="glass-card p-6 border border-primary/20">
          <div class="flex items-start gap-4">
            <div class="p-3 bg-primary/20 rounded-lg">
              <RefreshCw class="w-8 h-8 text-primary" />
            </div>
            <div class="flex-1">
              <h4 class="text-lg font-semibold text-gray-200 mb-2">
                Sync Data Now
              </h4>
              <p class="text-sm text-gray-400 mb-4">
                Manually trigger data synchronization from all edge nodes. This will pull the latest sensor data and update storage tiers.
              </p>
              <UiButton
                @click="handleSync"
                :loading="isSyncing"
                :disabled="isSyncing"
                size="lg"
                class="w-full"
              >
                <RefreshCw :class="{ 'animate-spin': isSyncing }" class="w-5 h-5" />
                {{ isSyncing ? 'Syncing...' : 'Trigger Sync' }}
              </UiButton>
            </div>
          </div>
        </div>

        <!-- Reset System Card -->
        <div class="glass-card p-6 border border-red-500/20">
          <div class="flex items-start gap-4">
            <div class="p-3 bg-red-500/20 rounded-lg">
              <Trash2 class="w-8 h-8 text-red-500" />
            </div>
            <div class="flex-1">
              <h4 class="text-lg font-semibold text-gray-200 mb-2">
                Reset System
              </h4>
              <p class="text-sm text-gray-400 mb-4">
                Clear all data from Redis and MongoDB. This action cannot be undone. Use for demo reset purposes only.
              </p>
              <UiButton
                @click="showResetConfirm = true"
                :disabled="isResetting"
                variant="danger"
                size="lg"
                class="w-full"
              >
                <AlertTriangle class="w-5 h-5" />
                Reset System
              </UiButton>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- System Information -->
    <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
      <div class="glass-card p-6">
        <div class="flex items-center gap-3 mb-3">
          <Activity class="w-5 h-5 text-neon-green" />
          <h4 class="font-semibold text-gray-300">Backend Status</h4>
        </div>
        <div class="flex items-center gap-2">
          <div class="w-3 h-3 rounded-full bg-neon-green animate-pulse" />
          <span class="text-lg font-medium text-gray-200">Connected</span>
        </div>
        <p class="text-xs text-gray-500 mt-2">http://localhost:8080</p>
      </div>

      <div class="glass-card p-6">
        <div class="flex items-center gap-3 mb-3">
          <Server class="w-5 h-5 text-primary" />
          <h4 class="font-semibold text-gray-300">Edge Nodes</h4>
        </div>
        <div>
          <span class="text-3xl font-bold text-primary">{{ onlineNodes.length }}</span>
          <span class="text-gray-500 text-lg ml-1">/ {{ edgeNodes.length }}</span>
        </div>
        <p class="text-xs text-gray-500 mt-2">Active connections</p>
      </div>

      <div class="glass-card p-6">
        <div class="flex items-center gap-3 mb-3">
          <HardDrive class="w-5 h-5 text-yellow-500" />
          <h4 class="font-semibold text-gray-300">Storage Mode</h4>
        </div>
        <div>
          <span class="text-lg font-medium text-gray-200">3-Tier</span>
        </div>
        <p class="text-xs text-gray-500 mt-2">HOT / WARM / COLD</p>
      </div>
    </div>

    <!-- Action History -->
    <div class="glass-card p-6">
      <h3 class="text-lg font-semibold text-gray-200 mb-4 flex items-center gap-2">
        <Clock class="w-5 h-5 text-primary" />
        Recent Actions
      </h3>
      
      <div v-if="actionHistory.length > 0" class="space-y-3">
        <div
          v-for="(action, index) in actionHistory"
          :key="index"
          class="flex items-start gap-3 p-3 bg-dark-lighter/30 rounded-lg"
        >
          <div 
            class="w-2 h-2 rounded-full mt-2"
            :class="action.success ? 'bg-neon-green' : 'bg-red-500'"
          />
          <div class="flex-1">
            <p class="text-sm font-medium text-gray-300">{{ action.action }}</p>
            <p class="text-xs text-gray-500 mt-1">{{ action.timestamp }}</p>
            <p v-if="action.message" class="text-xs mt-1" :class="action.success ? 'text-neon-green' : 'text-red-400'">
              {{ action.message }}
            </p>
          </div>
        </div>
      </div>
      
      <div v-else class="text-center py-8 text-gray-500">
        No recent actions
      </div>
    </div>

    <!-- Success/Error Messages -->
    <Transition name="slide-fade">
      <div 
        v-if="successMessage"
        class="glass-card p-4 border-neon-green/40 bg-neon-green/10"
      >
        <div class="flex items-start gap-3">
          <CheckCircle class="w-5 h-5 text-neon-green flex-shrink-0 mt-0.5" />
          <div>
            <p class="text-neon-green font-medium">Success</p>
            <p class="text-sm text-gray-300 mt-1">{{ successMessage }}</p>
          </div>
        </div>
      </div>
    </Transition>

    <Transition name="slide-fade">
      <div 
        v-if="errorMessage"
        class="glass-card p-4 border-red-500/40 bg-red-500/10"
      >
        <div class="flex items-start gap-3">
          <AlertCircle class="w-5 h-5 text-red-500 flex-shrink-0 mt-0.5" />
          <div>
            <p class="text-red-400 font-medium">Error</p>
            <p class="text-sm text-gray-300 mt-1">{{ errorMessage }}</p>
          </div>
        </div>
      </div>
    </Transition>

    <!-- Confirmation Modal -->
    <Teleport to="body">
      <Transition name="modal">
        <div 
          v-if="showResetConfirm"
          class="fixed inset-0 bg-black/80 backdrop-blur-sm z-50 flex items-center justify-center p-4"
          @click.self="showResetConfirm = false"
        >
          <div class="glass-card p-6 max-w-md w-full border border-red-500/40">
            <div class="flex items-start gap-4 mb-6">
              <div class="p-3 bg-red-500/20 rounded-lg">
                <AlertTriangle class="w-8 h-8 text-red-500" />
              </div>
              <div>
                <h4 class="text-xl font-semibold text-gray-200 mb-2">
                  Confirm System Reset
                </h4>
                <p class="text-sm text-gray-400">
                  This will permanently delete all data from HOT, WARM, and COLD storage. This action cannot be undone.
                </p>
              </div>
            </div>

            <div class="flex gap-3">
              <UiButton
                variant="ghost"
                @click="showResetConfirm = false"
                class="flex-1"
              >
                Cancel
              </UiButton>
              <UiButton
                variant="danger"
                @click="handleReset"
                :loading="isResetting"
                class="flex-1"
              >
                Yes, Reset System
              </UiButton>
            </div>
          </div>
        </div>
      </Transition>
    </Teleport>
  </div>
</template>

<script setup lang="ts">
import {
  Settings,
  RefreshCw,
  Trash2,
  AlertTriangle,
  Activity,
  Server,
  HardDrive,
  Clock,
  CheckCircle,
  AlertCircle,
} from 'lucide-vue-next'

definePageMeta({
  layout: 'default',
})

const { syncNow, resetSystem, isLoading } = useSystemControl()
const { edgeNodes, onlineNodes } = useSystemStats()

const isSyncing = ref(false)
const isResetting = ref(false)
const showResetConfirm = ref(false)
const successMessage = ref('')
const errorMessage = ref('')

interface ActionHistoryItem {
  action: string
  timestamp: string
  success: boolean
  message?: string
}

const actionHistory = ref<ActionHistoryItem[]>([])

const handleSync = async () => {
  isSyncing.value = true
  successMessage.value = ''
  errorMessage.value = ''

  const result = await syncNow()

  isSyncing.value = false

  if (result.success) {
    successMessage.value = 'Data synchronization completed successfully'
    addToHistory('Manual Sync Triggered', true, 'All edge nodes synced')
    
    setTimeout(() => {
      successMessage.value = ''
    }, 5000)
  } else {
    errorMessage.value = result.error || 'Failed to sync data'
    addToHistory('Manual Sync Failed', false, result.error)
    
    setTimeout(() => {
      errorMessage.value = ''
    }, 5000)
  }
}

const handleReset = async () => {
  isResetting.value = true
  successMessage.value = ''
  errorMessage.value = ''

  const result = await resetSystem()

  isResetting.value = false
  showResetConfirm.value = false

  if (result.success) {
    successMessage.value = 'System reset completed successfully'
    addToHistory('System Reset', true, 'All data cleared')
    
    setTimeout(() => {
      successMessage.value = ''
    }, 5000)
  } else {
    errorMessage.value = result.error || 'Failed to reset system'
    addToHistory('System Reset Failed', false, result.error)
    
    setTimeout(() => {
      errorMessage.value = ''
    }, 5000)
  }
}

const addToHistory = (action: string, success: boolean, message?: string) => {
  actionHistory.value.unshift({
    action,
    timestamp: new Date().toLocaleString(),
    success,
    message,
  })

  // Keep only last 10 items
  if (actionHistory.value.length > 10) {
    actionHistory.value = actionHistory.value.slice(0, 10)
  }
}
</script>

<style scoped>
.slide-fade-enter-active {
  transition: all 0.3s ease;
}

.slide-fade-leave-active {
  transition: all 0.2s ease;
}

.slide-fade-enter-from,
.slide-fade-leave-to {
  transform: translateY(-10px);
  opacity: 0;
}

.modal-enter-active,
.modal-leave-active {
  transition: all 0.3s ease;
}

.modal-enter-from,
.modal-leave-to {
  opacity: 0;
}
</style>
