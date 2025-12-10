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
  <div
    class="glass-card p-6 transition-all duration-300 hover:shadow-neon"
    :class="variantClasses"
  >
    <div v-if="title || icon" class="flex items-center gap-3 mb-4">
      <component v-if="icon" :is="icon" class="w-6 h-6" :class="iconColor" />
      <h3
        v-if="title"
        class="text-lg font-semibold text-gray-900 dark:text-gray-100"
      >
        {{ title }}
      </h3>
    </div>

    <div class="text-gray-700 dark:text-gray-300">
      <slot />
    </div>
  </div>
</template>

<script setup lang="ts">
import type { Component } from "vue";

interface Props {
  title?: string;
  icon?: Component;
  variant?: "default" | "primary" | "success" | "danger";
}

const props = withDefaults(defineProps<Props>(), {
  variant: "default",
});

const variantClasses = computed(() => {
  switch (props.variant) {
    case "primary":
      return "border-primary/30";
    case "success":
      return "border-neon-green/30";
    case "danger":
      return "border-red-500/30";
    default:
      return "";
  }
});

const iconColor = computed(() => {
  switch (props.variant) {
    case "primary":
      return "text-primary";
    case "success":
      return "text-neon-green";
    case "danger":
      return "text-red-500";
    default:
      return "text-gray-600 dark:text-gray-400";
  }
});
</script>
