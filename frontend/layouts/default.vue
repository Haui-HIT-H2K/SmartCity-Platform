<template>
  <div class="min-h-screen flex bg-gradient-cyber dark:bg-gradient-cyber">
    <!-- Sidebar -->
    <aside class="w-64 glass-card m-4 rounded-xl p-6 flex flex-col border border-primary/20">
      <!-- Logo/Brand -->
      <div class="mb-8">
        <h1 class="text-2xl font-bold text-primary dark:text-neon flex items-center gap-2">
          <Zap class="w-7 h-7" />
          <span>Smart City</span>
        </h1>
        <p class="text-xs text-gray-600 dark:text-gray-500 mt-1 ml-9">IoT Dashboard</p>
      </div>

      <!-- Navigation -->
      <nav class="flex-1 space-y-2">
        <NuxtLink
          to="/"
          class="flex items-center gap-3 px-4 py-3 rounded-lg transition-all duration-300 group"
          active-class="bg-primary/20 text-primary border border-primary/40"
          inactive-class="text-gray-600 dark:text-gray-400 hover:bg-light-lighter dark:hover:bg-dark-lighter hover:text-gray-900 dark:hover:text-gray-200"
        >
          <LayoutDashboard class="w-5 h-5" />
          <span class="font-medium">Dashboard</span>
        </NuxtLink>

        <NuxtLink
          to="/data-explorer"
          class="flex items-center gap-3 px-4 py-3 rounded-lg transition-all duration-300"
          active-class="bg-primary/20 text-primary border border-primary/40"
          inactive-class="text-gray-600 dark:text-gray-400 hover:bg-light-lighter dark:hover:bg-dark-lighter hover:text-gray-900 dark:hover:text-gray-200"
        >
          <Database class="w-5 h-5" />
          <span class="font-medium">Data Explorer</span>
        </NuxtLink>

        <NuxtLink
          to="/system-control"
          class="flex items-center gap-3 px-4 py-3 rounded-lg transition-all duration-300"
          active-class="bg-primary/20 text-primary border border-primary/40"
          inactive-class="text-gray-600 dark:text-gray-400 hover:bg-light-lighter dark:hover:bg-dark-lighter hover:text-gray-900 dark:hover:text-gray-200"
        >
          <Settings class="w-5 h-5" />
          <span class="font-medium">System Control</span>
        </NuxtLink>
      </nav>

      <!-- Footer -->
      <div class="mt-auto pt-6 border-t border-light-border dark:border-dark-border">
        <div class="flex items-center gap-3 px-4 py-2">
          <div class="w-8 h-8 rounded-full bg-primary/20 flex items-center justify-center">
            <User class="w-4 h-4 text-primary" />
          </div>
          <div>
            <p class="text-sm font-medium text-gray-900 dark:text-gray-100">Admin</p>
            <p class="text-xs text-gray-600 dark:text-gray-400">System Operator</p>
          </div>
        </div>
      </div>
    </aside>

    <!-- Main Content -->
    <div class="flex-1 flex flex-col min-h-screen">
      <!-- Header -->
      <header class="glass-card m-4 mb-0 rounded-xl p-4 flex items-center justify-between border border-primary/10">
        <div>
          <h2 class="text-xl font-semibold text-gray-900 dark:text-gray-100">
            {{ pageTitle }}
          </h2>
          <p class="text-sm text-gray-600 dark:text-gray-500">Real-time monitoring & analytics</p>
        </div>

        <div class="flex items-center gap-6">
          <!-- Theme Toggle -->
          <button
            @click="toggleTheme"
            class="p-2 rounded-lg transition-all duration-300 hover:bg-light-lighter dark:hover:bg-dark-lighter border border-light-border dark:border-dark-border"
            :aria-label="isDark ? 'Switch to light mode' : 'Switch to dark mode'"
          >
            <Sun v-if="isDark" class="w-5 h-5 text-yellow-500" />
            <Moon v-else class="w-5 h-5 text-primary" />
          </button>

          <!-- Connection Status -->
          <div class="flex items-center gap-2">
            <div class="relative w-2 h-2 rounded-full bg-neon-green">
              <div class="absolute inset-0 rounded-full bg-neon-green animate-ping opacity-75" />
            </div>
            <span class="text-sm text-gray-600 dark:text-gray-400">Connected</span>
          </div>

          <!-- Current Time -->
          <div class="flex items-center gap-2 text-gray-600 dark:text-gray-400">
            <Clock class="w-4 h-4" />
            <span class="text-sm font-mono">{{ currentTime }}</span>
          </div>
        </div>
      </header>

      <!-- Page Content -->
      <main class="flex-1 p-4 pt-4">
        <slot />
      </main>
    </div>
  </div>
</template>

<script setup lang="ts">
import { 
  LayoutDashboard, 
  Database, 
  Settings, 
  User, 
  Clock,
  Zap,
  Sun,
  Moon,
} from 'lucide-vue-next'

const route = useRoute()
const currentTime = ref('')
const { theme, toggleTheme, isDark } = useTheme()

const pageTitle = computed(() => {
  const titles: Record<string, string> = {
    '/': 'Real-time Monitoring',
    '/data-explorer': 'Data Explorer',
    '/system-control': 'System Control',
  }
  return titles[route.path] || 'Dashboard'
})

// Update time every second
const updateTime = () => {
  const now = new Date()
  currentTime.value = now.toLocaleTimeString('en-US', { hour12: false })
}

onMounted(() => {
  updateTime()
  setInterval(updateTime, 1000)
})
</script>
