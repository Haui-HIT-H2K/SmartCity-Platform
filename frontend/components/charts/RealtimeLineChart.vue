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
  <div class="space-y-4">
    <!-- Chart Title -->
    <div class="flex items-center justify-between">
      <p class="text-sm text-gray-400">
        {{ rateHistory.length }} data points (last 10 minutes)
      </p>
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

    <!-- Bar Chart -->
    <div
      v-if="rateHistory.length > 0"
      class="relative h-64 bg-gray-900/50 rounded-lg p-4 overflow-x-auto"
    >
      <div class="flex items-end justify-between h-full gap-1">
        <div
          v-for="(snapshot, index) in displayData"
          :key="index"
          class="flex flex-col items-center justify-end flex-1 min-w-[8px] group"
        >
          <!-- Incoming Rate Bar (Cyan) -->
          <div
            class="w-full bg-gradient-to-t from-cyan-500 to-cyan-300 rounded-t transition-all duration-300 hover:from-cyan-400 hover:to-cyan-200"
            :style="{ height: snapshot.incomingHeight + '%' }"
            :title="`Incoming: ${snapshot.incomingRate}/s at ${snapshot.time}`"
          ></div>

          <!-- Processed Rate Bar (Green) - stacked on top -->
          <div
            class="w-full bg-gradient-to-t from-green-500 to-green-300 mt-0.5 rounded-t transition-all duration-300 hover:from-green-400 hover:to-green-200"
            :style="{ height: snapshot.processedHeight + '%' }"
            :title="`Processed: ${snapshot.processedRate}/s at ${snapshot.time}`"
          ></div>
        </div>
      </div>

      <!-- Y-axis labels -->
      <div
        class="absolute left-0 top-0 h-full flex flex-col justify-between text-xs text-gray-500 pr-2"
      >
        <span>{{ maxRate }}</span>
        <span>{{ Math.floor(maxRate * 0.5) }}</span>
        <span>0</span>
      </div>
    </div>

    <!-- No Data Message -->
    <div
      v-else
      class="h-64 bg-gray-900/50 rounded-lg flex items-center justify-center"
    >
      <div class="text-center text-gray-500">
        <Activity class="w-12 h-12 mx-auto mb-2 opacity-50" />
        <p>Waiting for rate data...</p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { Activity } from "lucide-vue-next";

interface RateSnapshot {
  timestamp: number;
  incomingRate: number;
  processedRate: number;
}

interface Props {
  rateHistory: RateSnapshot[];
}

const props = withDefaults(defineProps<Props>(), {
  rateHistory: () => [],
});

const maxRate = computed(() => {
  if (props.rateHistory.length === 0) return 100;
  return Math.max(
    ...props.rateHistory.flatMap((s) => [s.incomingRate, s.processedRate]),
    10
  );
});

const displayData = computed(() => {
  return props.rateHistory.map((snapshot) => {
    const date = new Date(snapshot.timestamp);
    return {
      time: date.toLocaleTimeString(),
      incomingRate: snapshot.incomingRate,
      processedRate: snapshot.processedRate,
      incomingHeight: (snapshot.incomingRate / maxRate.value) * 100,
      processedHeight: (snapshot.processedRate / maxRate.value) * 100,
    };
  });
});
</script>
