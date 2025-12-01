import { useSystemStore } from '~/stores/system'
import type { SystemStats } from '~/stores/system'

export const useSystemStats = (pollingInterval = 2000) => {
    const systemStore = useSystemStore()
    const isPolling = ref(false)
    let intervalId: NodeJS.Timeout | null = null

    const startPolling = () => {
        if (isPolling.value) return

        isPolling.value = true

        // Fetch immediately
        systemStore.fetchStats()
        systemStore.fetchEdgeNodes()

        // Then set up interval
        intervalId = setInterval(() => {
            systemStore.fetchStats()
            systemStore.fetchEdgeNodes()
        }, pollingInterval)
    }

    const stopPolling = () => {
        if (intervalId) {
            clearInterval(intervalId)
            intervalId = null
        }
        isPolling.value = false
    }

    // Auto start/stop based on component lifecycle
    onMounted(() => {
        startPolling()
    })

    onUnmounted(() => {
        stopPolling()
    })

    return {
        stats: computed(() => systemStore.stats),
        edgeNodes: computed(() => systemStore.edgeNodes),
        onlineNodes: computed(() => systemStore.onlineNodes),
        offlineNodes: computed(() => systemStore.offlineNodes),
        hasActiveNodes: computed(() => systemStore.hasActiveNodes),
        lastUpdate: computed(() => systemStore.lastUpdate),
        error: computed(() => systemStore.error),
        isPolling: readonly(isPolling),
        startPolling,
        stopPolling,
    }
}
