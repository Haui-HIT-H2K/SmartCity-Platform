<!--
  Copyright 2025 Haui.HIT - H2K

  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
-->

<template>
  <div class="space-y-6">
    <!-- Header -->
    <div class="glass-card p-6">
      <h3 class="text-lg font-semibold text-gray-900 dark:text-gray-100 mb-2 flex items-center gap-2">
        <Upload class="w-5 h-5 text-primary" />
        Publish Data
      </h3>
      <p class="text-sm text-gray-600 dark:text-gray-400">
        Push sensor data to the Smart City Platform. Choose between single record or batch upload.
      </p>
    </div>

    <!-- Tab Navigation -->
    <div class="glass-card overflow-hidden">
      <div class="flex border-b border-gray-200 dark:border-dark-border">
        <button
          @click="activeTab = 'single'"
          class="flex-1 px-6 py-4 text-sm font-medium transition-all duration-300 flex items-center justify-center gap-2"
          :class="activeTab === 'single' 
            ? 'text-primary border-b-2 border-primary bg-primary/5' 
            : 'text-gray-600 dark:text-gray-400 hover:text-gray-900 dark:hover:text-gray-200 hover:bg-gray-50 dark:hover:bg-dark-lighter'"
        >
          <FileUp class="w-4 h-4" />
          Single Data
        </button>
        <button
          @click="activeTab = 'batch'"
          class="flex-1 px-6 py-4 text-sm font-medium transition-all duration-300 flex items-center justify-center gap-2"
          :class="activeTab === 'batch' 
            ? 'text-primary border-b-2 border-primary bg-primary/5' 
            : 'text-gray-600 dark:text-gray-400 hover:text-gray-900 dark:hover:text-gray-200 hover:bg-gray-50 dark:hover:bg-dark-lighter'"
        >
          <Files class="w-4 h-4" />
          Batch Data
        </button>
      </div>

      <!-- Single Data Tab -->
      <div v-if="activeTab === 'single'" class="p-6">
        <form @submit.prevent="publishSingle" class="space-y-6">
          <!-- Source ID -->
          <div>
            <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
              Source ID / Sensor ID <span class="text-red-500">*</span>
            </label>
            <input
              v-model="singleForm.sourceId"
              type="text"
              required
              placeholder="e.g., sensor-001"
              class="w-full px-4 py-3 bg-white dark:bg-dark-card border border-gray-300 dark:border-dark-border rounded-lg text-gray-900 dark:text-gray-100 placeholder-gray-400 dark:placeholder-gray-500 focus:border-primary focus:outline-none focus:ring-2 focus:ring-primary/20"
            />
          </div>

          <!-- Data Type -->
          <div>
            <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
              Data Type
            </label>
            <select
              v-model="singleForm.dataType"
              class="w-full px-4 py-3 bg-white dark:bg-dark-card border border-gray-300 dark:border-dark-border rounded-lg text-gray-900 dark:text-gray-100 focus:border-primary focus:outline-none focus:ring-2 focus:ring-primary/20"
            >
              <option value="">Auto (System will classify)</option>
              <option value="HOT">HOT - Real-time data</option>
              <option value="WARM">WARM - Recent data</option>
              <option value="COLD">COLD - Historical data</option>
            </select>
          </div>

          <!-- Payload -->
          <div>
            <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
              Payload Data <span class="text-red-500">*</span>
            </label>
            
            <!-- Dynamic Key-Value Fields -->
            <div class="space-y-3">
              <div v-for="(field, index) in payloadFields" :key="index" class="flex gap-3">
                <input
                  v-model="field.key"
                  type="text"
                  placeholder="Key (e.g., temperature)"
                  class="flex-1 px-4 py-2 bg-white dark:bg-dark-card border border-gray-300 dark:border-dark-border rounded-lg text-gray-900 dark:text-gray-100 placeholder-gray-400 dark:placeholder-gray-500 focus:border-primary focus:outline-none focus:ring-2 focus:ring-primary/20"
                />
                <input
                  v-model="field.value"
                  type="text"
                  placeholder="Value (e.g., 25.5)"
                  class="flex-1 px-4 py-2 bg-white dark:bg-dark-card border border-gray-300 dark:border-dark-border rounded-lg text-gray-900 dark:text-gray-100 placeholder-gray-400 dark:placeholder-gray-500 focus:border-primary focus:outline-none focus:ring-2 focus:ring-primary/20"
                />
                <button
                  type="button"
                  @click="removePayloadField(index)"
                  :disabled="payloadFields.length <= 1"
                  class="p-2 text-red-500 hover:bg-red-50 dark:hover:bg-red-900/20 rounded-lg transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
                >
                  <Trash2 class="w-5 h-5" />
                </button>
              </div>
            </div>
            
            <button
              type="button"
              @click="addPayloadField"
              class="mt-3 flex items-center gap-2 text-sm text-primary hover:text-primary/80 transition-colors"
            >
              <Plus class="w-4 h-4" />
              Add field
            </button>
          </div>

          <!-- Submit Button -->
          <button
            type="submit"
            :disabled="publishing"
            class="w-full py-3 px-4 bg-gradient-to-r from-cyan-500 to-blue-500 hover:from-cyan-400 hover:to-blue-400 text-white font-semibold rounded-xl shadow-lg shadow-cyan-500/25 hover:shadow-cyan-500/40 transition-all duration-300 disabled:opacity-50 disabled:cursor-not-allowed flex items-center justify-center gap-2"
          >
            <Loader2 v-if="publishing" class="w-5 h-5 animate-spin" />
            <Upload v-else class="w-5 h-5" />
            Publish Data
          </button>
        </form>
      </div>

      <!-- Batch Data Tab -->
      <div v-if="activeTab === 'batch'" class="p-6">
        <form @submit.prevent="publishBatch" class="space-y-6">
          <!-- JSON Input -->
          <div>
            <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
              JSON Data <span class="text-red-500">*</span>
            </label>
            <p class="text-xs text-gray-500 dark:text-gray-400 mb-2">
              Paste an array of records. Each record must have sourceId and payload.
            </p>
            <textarea
              v-model="batchJson"
              rows="12"
              placeholder='[
  {
    "sourceId": "sensor-001",
    "payload": { "temperature": 25.5, "humidity": 60 }
  },
  {
    "sourceId": "sensor-002", 
    "payload": { "temperature": 22.0, "humidity": 55 }
  }
]'
              class="w-full px-4 py-3 bg-white dark:bg-dark-card border border-gray-300 dark:border-dark-border rounded-lg text-gray-900 dark:text-gray-100 placeholder-gray-400 dark:placeholder-gray-500 focus:border-primary focus:outline-none focus:ring-2 focus:ring-primary/20 font-mono text-sm"
            ></textarea>
          </div>

          <!-- Preview -->
          <div v-if="batchPreview.length > 0" class="p-4 bg-gray-50 dark:bg-dark-lighter rounded-lg">
            <p class="text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
              Preview: {{ batchPreview.length }} records detected
            </p>
            <div class="max-h-40 overflow-y-auto">
              <div v-for="(record, index) in batchPreview.slice(0, 5)" :key="index" class="text-xs text-gray-600 dark:text-gray-400 py-1 border-b border-gray-200 dark:border-dark-border last:border-0">
                <span class="text-primary">{{ record.sourceId }}</span>: {{ JSON.stringify(record.payload).slice(0, 50) }}...
              </div>
              <div v-if="batchPreview.length > 5" class="text-xs text-gray-500 mt-2">
                + {{ batchPreview.length - 5 }} more records...
              </div>
            </div>
          </div>

          <!-- JSON Error -->
          <div v-if="jsonError" class="flex items-center gap-2 p-3 bg-red-500/10 border border-red-500/30 rounded-xl">
            <AlertCircle class="w-5 h-5 text-red-400 flex-shrink-0" />
            <span class="text-red-400 text-sm">{{ jsonError }}</span>
          </div>

          <!-- Submit Button -->
          <button
            type="submit"
            :disabled="publishing || batchPreview.length === 0"
            class="w-full py-3 px-4 bg-gradient-to-r from-purple-500 to-pink-500 hover:from-purple-400 hover:to-pink-400 text-white font-semibold rounded-xl shadow-lg shadow-purple-500/25 hover:shadow-purple-500/40 transition-all duration-300 disabled:opacity-50 disabled:cursor-not-allowed flex items-center justify-center gap-2"
          >
            <Loader2 v-if="publishing" class="w-5 h-5 animate-spin" />
            <Files v-else class="w-5 h-5" />
            Publish {{ batchPreview.length }} Records
          </button>
        </form>
      </div>
    </div>

    <!-- Success Message -->
    <div v-if="successMessage" class="glass-card p-4 border-green-500/40 bg-green-500/10">
      <div class="flex items-start gap-3">
        <CheckCircle class="w-5 h-5 text-green-500 flex-shrink-0 mt-0.5" />
        <div>
          <p class="text-green-600 dark:text-green-400 font-medium">Success!</p>
          <p class="text-sm text-gray-700 dark:text-gray-400 mt-1">{{ successMessage }}</p>
        </div>
      </div>
    </div>

    <!-- Error Message -->
    <div v-if="errorMessage" class="glass-card p-4 border-red-500/40 bg-red-500/10">
      <div class="flex items-start gap-3">
        <AlertCircle class="w-5 h-5 text-red-500 flex-shrink-0 mt-0.5" />
        <div>
          <p class="text-red-600 dark:text-red-400 font-medium">Error</p>
          <p class="text-sm text-gray-700 dark:text-gray-400 mt-1">{{ errorMessage }}</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { 
  Upload, 
  FileUp, 
  Files, 
  Plus, 
  Trash2, 
  Loader2, 
  CheckCircle, 
  AlertCircle 
} from 'lucide-vue-next'

definePageMeta({
  layout: 'default',
  middleware: 'auth'
})

const activeTab = ref<'single' | 'batch'>('single')
const publishing = ref(false)
const successMessage = ref('')
const errorMessage = ref('')

// Single form state
const singleForm = ref({
  sourceId: '',
  dataType: ''
})

const payloadFields = ref([
  { key: '', value: '' }
])

const addPayloadField = () => {
  payloadFields.value.push({ key: '', value: '' })
}

const removePayloadField = (index: number) => {
  if (payloadFields.value.length > 1) {
    payloadFields.value.splice(index, 1)
  }
}

// Batch form state
const batchJson = ref('')
const jsonError = ref('')
const batchPreview = ref<any[]>([])

// Watch batch JSON input
watch(batchJson, (value) => {
  jsonError.value = ''
  batchPreview.value = []
  
  if (!value.trim()) return
  
  try {
    const parsed = JSON.parse(value)
    if (!Array.isArray(parsed)) {
      jsonError.value = 'JSON must be an array of records'
      return
    }
    
    if (parsed.length > 100) {
      jsonError.value = 'Maximum 100 records per batch'
      return
    }
    
    batchPreview.value = parsed
  } catch (e) {
    jsonError.value = 'Invalid JSON format'
  }
})

// Publish single record
const publishSingle = async () => {
  successMessage.value = ''
  errorMessage.value = ''
  
  // Build payload from fields
  const payload: Record<string, any> = {}
  for (const field of payloadFields.value) {
    if (field.key.trim()) {
      // Try to parse as number
      const numValue = parseFloat(field.value)
      payload[field.key.trim()] = isNaN(numValue) ? field.value : numValue
    }
  }
  
  if (Object.keys(payload).length === 0) {
    errorMessage.value = 'At least one payload field is required'
    return
  }
  
  publishing.value = true
  
  try {
    const response: any = await $fetch('/api/publish', {
      method: 'POST',
      body: {
        sourceId: singleForm.value.sourceId,
        payload,
        dataType: singleForm.value.dataType || null
      }
    })
    
    if (response && response.success) {
      successMessage.value = `Data published successfully! ID: ${response.id}, Type: ${response.dataType || 'Auto-classified'}`
      // Reset form
      singleForm.value = { sourceId: '', dataType: '' }
      payloadFields.value = [{ key: '', value: '' }]
    } else {
      throw new Error(response?.message || 'Failed to publish data')
    }
  } catch (err: any) {
    errorMessage.value = err.data?.message || err.message || 'Failed to publish data'
  } finally {
    publishing.value = false
  }
}

// Publish batch records
const publishBatch = async () => {
  successMessage.value = ''
  errorMessage.value = ''
  
  if (batchPreview.value.length === 0) {
    errorMessage.value = 'No valid records to publish'
    return
  }
  
  publishing.value = true
  
  try {
    const response: any = await $fetch('/api/publish/batch', {
      method: 'POST',
      body: batchPreview.value
    })
    
    if (response && response.success) {
      successMessage.value = `${response.totalPublished} out of ${response.totalReceived} records published successfully!`
      if (response.errors && response.errors.length > 0) {
        successMessage.value += ` (${response.errors.length} errors)`
      }
      // Reset form
      batchJson.value = ''
      batchPreview.value = []
    } else {
      throw new Error(response?.message || 'Failed to publish batch data')
    }
  } catch (err: any) {
    errorMessage.value = err.data?.message || err.message || 'Failed to publish batch data'
  } finally {
    publishing.value = false
  }
}
</script>
