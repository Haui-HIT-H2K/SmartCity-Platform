import { useSystemStore } from '~/stores/system'

export const useSystemControl = () => {
    const systemStore = useSystemStore()
    const isLoading = computed(() => systemStore.isLoading)

    const syncNow = async () => {
        try {
            await systemStore.syncData()
            return { success: true }
        } catch (error: any) {
            return { success: false, error: error.message }
        }
    }

    const resetSystem = async () => {
        try {
            await $fetch('/api/system/reset', {
                method: 'POST',
            })
            // Refresh data after reset
            await systemStore.fetchStats()
            await systemStore.fetchEdgeNodes()
            return { success: true }
        } catch (error: any) {
            return { success: false, error: error.message }
        }
    }

    return {
        isLoading,
        syncNow,
        resetSystem,
    }
}
