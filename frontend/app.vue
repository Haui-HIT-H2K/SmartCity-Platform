<template>
  <div>
    <NuxtRouteAnnouncer />
    <NuxtLayout>
      <NuxtPage />
    </NuxtLayout>
  </div>
</template>

<script setup>
// Import global CSS
import '~/assets/css/main.css'

// Initialize theme
const { initTheme } = useTheme()

onMounted(() => {
  initTheme()
})
</script>

