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
  <div class="min-h-screen p-6 space-y-6">
    <!-- Header -->
    <div class="flex items-center justify-between">
      <div>
        <h1 class="text-2xl font-bold text-gray-100">Edge Node Management</h1>
        <p class="text-gray-500 mt-1">Manage edge storage nodes</p>
      </div>
      <UiButton @click="showAddDialog = true" class="flex items-center gap-2">
        <Plus class="w-4 h-4" />
        <span>Add Node</span>
      </UiButton>
    </div>

    <!-- Loading State -->
    <div v-if="loading" class="glass-card p-12 text-center">
      <div class="inline-block animate-spin rounded-full h-12 w-12 border-b-2 border-primary"></div>
      <p class="text-gray-500 mt-4">Loading nodes...</p>
    </div>

    <!-- Nodes Table -->
    <div v-else class="glass-card overflow-hidden">
      <table class="w-full">
        <thead class="border-b border-dark-border">
          <tr class="text-left">
            <th class="p-4 text-gray-400 font-semibold">Name</th>
            <th class="p-4 text-gray-400 font-semibold">Host</th>
            <th class="p-4 text-gray-400 font-semibold">Port</th>
            <th class="p-4 text-gray-400 font-semibold">Status</th>
            <th class="p-4 text-gray-400 font-semibold">Enabled</th>
            <th class="p-4 text-gray-400 font-semibold text-right">Actions</th>
          </tr>
        </thead>
        <tbody>
          <tr
            v-for="node in nodes"
            :key="node.id"
            class="border-b border-dark-border hover:bg-dark-lighter/50 transition-colors"
          >
            <td class="p-4 text-gray-200 font-medium">{{ node.name }}</td>
            <td class="p-4 text-gray-400 font-mono text-sm">{{ node.host }}</td>
            <td class="p-4 text-gray-400">{{ node.port }}</td>
            <td class="p-4">
              <span
                class="px-3 py-1 text-xs font-semibold rounded-full"
                :class="getStatusClass(node.enabled)"
              >
                {{ node.enabled ? 'Online' : 'Offline' }}
              </span>
            </td>
            <td class="p-4">
              <button
                @click="toggleNode(node.name)"
                class="relative inline-flex h-6 w-11 items-center rounded-full transition-colors"
                :class="node.enabled ? 'bg-primary' : 'bg-gray-600'"
              >
                <span
                  class="inline-block h-4 w-4 transform rounded-full bg-white transition-transform"
                  :class="node.enabled ? 'translate-x-6' : 'translate-x-1'"
                ></span>
              </button>
            </td>
            <td class="p-4 text-right">
              <UiButton
                variant="ghost"
                size="sm"
                @click="confirmDelete(node.name)"
                class="text-red-400 hover:text-red-300"
              >
                <Trash2 class="w-4 h-4" />
              </UiButton>
            </td>
          </tr>
        </tbody>
      </table>

      <!-- Empty State -->
      <div v-if="nodes.length === 0" class="p-12 text-center">
        <Database class="w-16 h-16 text-gray-600 mx-auto mb-4" />
        <h3 class="text-lg font-semibold text-gray-400 mb-2">No Edge Nodes</h3>
        <p class="text-gray-500">Add your first edge node to get started</p>
      </div>
    </div>

    <!-- Add Node Dialog -->
    <div v-if="showAddDialog" class="fixed inset-0 bg-black/50 flex items-center justify-center z-50">
      <div class="glass-card p-6 max-w-md w-full mx-4">
        <h2 class="text-xl font-bold text-gray-100 mb-4">Add New Edge Node</h2>
        
        <form @submit.prevent="addNode" class="space-y-4">
          <div>
            <label class="block text-sm font-medium text-gray-400 mb-1">Name</label>
            <input
              v-model="newNode.name"
              type="text"
              required
              class="w-full px-4 py-2 bg-dark-lighter border border-dark-border rounded-lg text-gray-200 focus:outline-none focus:border-primary"
              placeholder="Subnet-CauGiay"
            />
          </div>
          
          <div>
            <label class="block text-sm font-medium text-gray-400 mb-1">Host</label>
            <input
              v-model="newNode.host"
              type="text"
              required
              class="w-full px-4 py-2 bg-dark-lighter border border-dark-border rounded-lg text-gray-200 focus:outline-none focus:border-primary"
              placeholder="rabbit-edge-1"
            />
          </div>
          
          <div>
            <label class="block text-sm font-medium text-gray-400 mb-1">Port</label>
            <input
              v-model.number="newNode.port"
              type="number"
              required
              class="w-full px-4 py-2 bg-dark-lighter border border-dark-border rounded-lg text-gray-200 focus:outline-none focus:border-primary"
              placeholder="5672"
            />
          </div>
          
          <div>
            <label class="block text-sm font-medium text-gray-400 mb-1">Queue Name</label>
            <input
              v-model="newNode.queueName"
              type="text"
              class="w-full px-4 py-2 bg-dark-lighter border border-dark-border rounded-lg text-gray-200 focus:outline-none focus:border-primary"
              placeholder="city-data-queue-1 (optional)"
            />
          </div>
          
          <div>
            <label class="block text-sm font-medium text-gray-400 mb-1">Username</label>
            <input
              v-model="newNode.username"
              type="text"
              class="w-full px-4 py-2 bg-dark-lighter border border-dark-border rounded-lg text-gray-200 focus:outline-none focus:border-primary"
              placeholder="edge_user (optional)"
            />
          </div>
          
          <div>
            <label class="block text-sm font-medium text-gray-400 mb-1">Password</label>
            <input
              v-model="newNode.password"
              type="password"
              class="w-full px-4 py-2 bg-dark-lighter border border-dark-border rounded-lg text-gray-200 focus:outline-none focus:border-primary"
              placeholder="edge_pass (optional)"
            />
          </div>
          
          <div class="flex gap-3 pt-4">
            <UiButton type="submit" class="flex-1">
              <span>Add Node</span>
            </UiButton>
            <UiButton type="button" variant="ghost" @click="showAddDialog = false" class="flex-1">
              <span>Cancel</span>
            </UiButton>
          </div>
        </form>
      </div>
    </div>

    <!-- Delete Confirmation Dialog -->
    <div v-if="deleteConfirm" class="fixed inset-0 bg-black/50 flex items-center justify-center z-50">
      <div class="glass-card p-6 max-w-sm w-full mx-4">
        <h2 class="text-xl font-bold text-gray-100 mb-4">Delete Node</h2>
        <p class="text-gray-400 mb-6">
          Are you sure you want to delete <span class="text-gray-200 font-semibold">{{ deleteConfirm }}</span>?
          This action cannot be undone.
        </p>
        
        <div class="flex gap-3">
          <UiButton @click="deleteNode" class="flex-1 bg-red-600 hover:bg-red-700">
            <span>Delete</span>
          </UiButton>
          <UiButton variant="ghost" @click="deleteConfirm = null" class="flex-1">
            <span>Cancel</span>
          </UiButton>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { Plus, Trash2, Database } from 'lucide-vue-next';

definePageMeta({
  layout: 'default',
  middleware: 'auth'
});

// State
const nodes = ref<any[]>([]);
const loading = ref(true);
const showAddDialog = ref(false);
const deleteConfirm = ref<string | null>(null);
const newNode = ref({
  name: '',
  host: '',
  port: 5672,
  queueName: '',
  username: '',
  password: '',
});

// Fetch nodes
const fetchNodes = async () => {
  try {
    loading.value = true;
    const data = await $fetch('/api/nodes');
    nodes.value = data;
  } catch (error) {
    console.error('Error fetching nodes:', error);
  } finally {
    loading.value = false;
  }
};

// Add node
const addNode = async () => {
  try {
    await $fetch('/api/nodes', {
      method: 'POST',
      body: newNode.value,
    });
    
    // Reset form and close dialog
    newNode.value = { name: '', host: '', port: 5672, queueName: '', username: '', password: '' };
    showAddDialog.value = false;
    
    // Refresh nodes list
    await fetchNodes();
  } catch (error: any) {
    console.error('Error adding node:', error);
    alert(error.data?.error || 'Failed to add node');
  }
};

// Toggle node status
const toggleNode = async (name: string) => {
  try {
    await $fetch(`/api/nodes/${name}/toggle`, {
      method: 'PUT',
    });
    
    // Refresh nodes list
    await fetchNodes();
  } catch (error) {
    console.error('Error toggling node:', error);
  }
};

// Confirm delete
const confirmDelete = (name: string) => {
  deleteConfirm.value = name;
};

// Delete node
const deleteNode = async () => {
  if (!deleteConfirm.value) return;
  
  try {
    await $fetch(`/api/nodes/${deleteConfirm.value}`, {
      method: 'DELETE',
    });
    
    deleteConfirm.value = null;
    
    // Refresh nodes list
    await fetchNodes();
  } catch (error) {
    console.error('Error deleting node:', error);
  }
};

// Get status badge class
const getStatusClass = (enabled: boolean) => {
  return enabled
    ? 'bg-green-500/20 text-green-400 border border-green-500/40'
    : 'bg-gray-500/20 text-gray-400 border border-gray-500/40';
};

// Load nodes on mount
onMounted(() => {
  fetchNodes();
});
</script>
