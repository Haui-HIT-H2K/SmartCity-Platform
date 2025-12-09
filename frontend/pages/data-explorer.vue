<template>
  <div class="space-y-6">
    <!-- Header with Filters -->
    <div class="glass-card p-6">
      <h3
        class="text-lg font-semibold text-gray-900 dark:text-gray-100 mb-4 flex items-center gap-2"
      >
        <Filter class="w-5 h-5 text-primary" />
        Filters
      </h3>

      <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
        <!-- Type Filter -->
        <div>
          <label
            class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2"
          >
            Data Type
          </label>
          <select
            v-model="selectedType"
            class="w-full px-4 py-2 bg-white dark:bg-dark-card border border-gray-300 dark:border-dark-border rounded-lg text-gray-900 dark:text-gray-100 focus:border-primary focus:outline-none focus:ring-2 focus:ring-primary/20"
            @change="handleFilterChange"
          >
            <option value="">All Types</option>
            <option value="HOT">HOT</option>
            <option value="WARM">WARM</option>
            <option value="COLD">COLD</option>
          </select>
        </div>

        <!-- Sensor ID Filter -->
        <div>
          <label
            class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2"
          >
            Sensor ID
          </label>
          <input
            v-model="sensorIdInput"
            type="text"
            placeholder="Enter sensor ID..."
            class="w-full px-4 py-2 bg-white dark:bg-dark-card border border-gray-300 dark:border-dark-border rounded-lg text-gray-900 dark:text-gray-100 placeholder-gray-400 dark:placeholder-gray-500 focus:border-primary focus:outline-none focus:ring-2 focus:ring-primary/20"
            @keyup.enter="handleFilterChange"
          />
        </div>

        <!-- Apply Button -->
        <div class="flex items-end">
          <UiButton
            @click="handleFilterChange"
            :loading="isLoading"
            class="w-full"
          >
            <div class="flex gap-2 items-center">
              <Search class="w-4 h-4" />
              <p>Apply Filters</p>
            </div>
          </UiButton>
        </div>
      </div>
    </div>

    <!-- Data Table -->
    <div class="glass-card overflow-hidden">
      <div
        class="p-4 border-b border-gray-200 dark:border-dark-border flex items-center justify-between"
      >
        <div>
          <h3
            class="text-lg font-semibold text-gray-900 dark:text-gray-100 flex items-center gap-2"
          >
            <Database class="w-5 h-5 text-primary" />
            City Data Records
          </h3>
          <p class="text-sm text-gray-600 dark:text-gray-400 mt-1">
            Showing {{ data.length }} of {{ total }} records
          </p>
        </div>

        <div class="text-sm text-gray-600 dark:text-gray-400">
          Page {{ filter.page }} of {{ totalPages }}
        </div>
      </div>

      <!-- Table -->
      <div
        v-if="!isLoading && data.length > 0"
        class="overflow-x-auto custom-scrollbar"
      >
        <table class="w-full">
          <thead>
            <tr
              class="bg-gray-50 dark:bg-dark-lighter/50 border-b border-gray-200 dark:border-dark-border"
            >
              <th
                class="px-6 py-3 text-left text-xs font-medium text-gray-700 dark:text-gray-300 uppercase tracking-wider"
              >
                ID
              </th>
              <th
                class="px-6 py-3 text-left text-xs font-medium text-gray-700 dark:text-gray-300 uppercase tracking-wider"
              >
                Sensor ID
              </th>
              <th
                class="px-6 py-3 text-left text-xs font-medium text-gray-700 dark:text-gray-300 uppercase tracking-wider"
              >
                Type
              </th>
              <th
                class="px-6 py-3 text-left text-xs font-medium text-gray-700 dark:text-gray-300 uppercase tracking-wider"
              >
                Value
              </th>
              <th
                class="px-6 py-3 text-left text-xs font-medium text-gray-700 dark:text-gray-300 uppercase tracking-wider"
              >
                Timestamp
              </th>
              <th
                class="px-6 py-3 text-left text-xs font-medium text-gray-700 dark:text-gray-300 uppercase tracking-wider"
              >
                Location
              </th>
              <th
                class="px-6 py-3 text-center text-xs font-medium text-gray-700 dark:text-gray-300 uppercase tracking-wider"
              >
                Actions
              </th>
            </tr>
          </thead>
          <tbody class="divide-y divide-gray-200 dark:divide-dark-border/30">
            <tr
              v-for="item in data"
              :key="item.id"
              class="hover:bg-gray-50 dark:hover:bg-dark-lighter/30 transition-colors cursor-pointer"
              @click="selectRecord(item)"
            >
              <td
                class="px-6 py-4 whitespace-nowrap text-sm font-mono text-gray-600 dark:text-gray-400"
              >
                {{ item.id.substring(0, 8) }}...
              </td>
              <td
                class="px-6 py-4 whitespace-nowrap text-sm font-medium text-primary"
              >
                {{ item.sensorId }}
              </td>
              <td class="px-6 py-4 whitespace-nowrap">
                <span
                  class="px-3 py-1 inline-flex text-xs leading-5 font-semibold rounded-full"
                  :class="getTypeBadgeClass(item.type)"
                >
                  {{ item.type }}
                </span>
              </td>
              <td
                class="px-6 py-4 whitespace-nowrap text-sm text-gray-900 dark:text-gray-100"
              >
                {{ item.value.toFixed(2) }}
              </td>
              <td
                class="px-6 py-4 whitespace-nowrap text-sm text-gray-600 dark:text-gray-400 font-mono"
              >
                {{ formatTimestamp(item.timestamp) }}
              </td>
              <td
                class="px-6 py-4 whitespace-nowrap text-sm text-gray-600 dark:text-gray-400"
              >
                <span v-if="item.location">
                  {{ item.location.lat.toFixed(4) }},
                  {{ item.location.lng.toFixed(4) }}
                </span>
                <span v-else class="text-gray-500 dark:text-gray-600">N/A</span>
              </td>
              <td class="px-6 py-4 whitespace-nowrap text-center">
                <button
                  @click.stop="viewRecordDetail(item.id)"
                  class="inline-flex items-center justify-center p-2 rounded-lg bg-white dark:bg-dark-card border border-gray-300 dark:border-dark-border hover:bg-primary/10 hover:border-primary transition-all group"
                  title="View details"
                >
                  <Eye
                    class="w-4 h-4 text-gray-600 dark:text-gray-400 group-hover:text-primary transition-colors"
                  />
                </button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- Loading State -->
      <div v-else-if="isLoading" class="p-12 text-center">
        <div
          class="inline-block animate-spin rounded-full h-12 w-12 border-b-2 border-primary"
        ></div>
        <p class="text-gray-600 dark:text-gray-400 mt-4">Loading data...</p>
      </div>

      <!-- Empty State -->
      <div v-else class="p-12 text-center">
        <Database
          class="w-16 h-16 text-gray-400 dark:text-gray-600 mx-auto mb-4"
        />
        <p class="text-gray-600 dark:text-gray-400">No data found</p>
        <p class="text-sm text-gray-600 mt-2">Try adjusting your filters</p>
      </div>

      <!-- Pagination -->
      <div
        v-if="totalPages > 1"
        class="p-4 border-t border-gray-200 dark:border-dark-border flex items-center justify-between"
      >
        <UiButton
          variant="ghost"
          size="sm"
          :disabled="!hasPrevious"
          @click="goToPrevPage"
        >
          <div class="flex gap-2 items-center">
            <ChevronLeft class="w-4 h-4" />
            <p>Previous</p>
          </div>
        </UiButton>

        <div class="flex items-center gap-2">
          <button
            v-for="page in visiblePages"
            :key="page"
            @click="goToPage(page)"
            class="px-3 py-1 rounded text-sm transition-colors"
            :class="
              page === filter.page
                ? 'bg-primary text-white font-semibold'
                : 'text-gray-600 dark:text-gray-400 hover:text-gray-900 dark:hover:text-gray-100 hover:bg-gray-100 dark:hover:bg-dark-lighter'
            "
          >
            {{ page == -1 ? "..." : page }}
          </button>
        </div>

        <UiButton
          variant="ghost"
          size="sm"
          :disabled="!hasMore"
          @click="goToNextPage"
        >
          <div class="flex gap-2 items-center">
            <p>Next</p>
            <ChevronRight class="w-4 h-4" />
          </div>
        </UiButton>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import {
  Filter,
  Search,
  Database,
  ChevronLeft,
  ChevronRight,
  Eye,
} from "lucide-vue-next";
import { useDataStore } from "~/stores/data";
import type { CityData, DataType } from "~/stores/data";

definePageMeta({
  layout: "default",
  middleware: 'auth'
});

const dataStore = useDataStore();
const router = useRouter();

// Local filter state
const selectedType = ref<DataType | "">("");
const sensorIdInput = ref("");

// Computed from store
const data = computed(() => dataStore.data);
const filter = computed(() => dataStore.filter);
const total = computed(() => dataStore.total);
const totalPages = computed(() => dataStore.totalPages);
const isLoading = computed(() => dataStore.isLoading);
const hasMore = computed(() => dataStore.hasMore);
const hasPrevious = computed(() => dataStore.hasPrevious);

// Handle filter change
const handleFilterChange = () => {
  dataStore.setFilter({
    type: selectedType.value || undefined,
    sensorId: sensorIdInput.value || undefined,
  });
  dataStore.fetchData();
};

// Pagination handlers
const goToNextPage = () => {
  dataStore.nextPage();
};

const goToPrevPage = () => {
  dataStore.prevPage();
};

const goToPage = (page: number) => {
  dataStore.goToPage(page);
};

// Calculate visible page numbers
const visiblePages = computed(() => {
  const current = filter.value.page;
  const total = totalPages.value;
  const delta = 2;
  const range: number[] = [];

  for (
    let i = Math.max(2, current - delta);
    i <= Math.min(total - 1, current + delta);
    i++
  ) {
    range.push(i);
  }

  if (current - delta > 2) {
    range.unshift(-1); // Ellipsis
  }
  if (current + delta < total - 1) {
    range.push(-1); // Ellipsis
  }

  range.unshift(1);
  if (total > 1) range.push(total);

  return range.filter((v, i, a) => a.indexOf(v) === i); // Remove duplicates
});

// Utility functions
const getTypeBadgeClass = (type: DataType) => {
  const classes = {
    HOT: "bg-red-500/20 text-red-400 border border-red-500/40",
    WARM: "bg-yellow-500/20 text-yellow-400 border border-yellow-500/40",
    COLD: "bg-blue-500/20 text-blue-400 border border-blue-500/40",
  };
  return classes[type];
};

const formatTimestamp = (timestamp: string) => {
  return new Date(timestamp).toLocaleString();
};

const viewRecordDetail = (id: string) => {
  router.push(`/record/${id}`);
};

const selectRecord = (record: CityData) => {
  // Row click - could be used for selection if needed
  console.log("Selected record:", record);
};

// Fetch data on mount
onMounted(() => {
  dataStore.fetchData();
});
</script>
