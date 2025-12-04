/** 

 * Copyright 2025 Haui.HIT - H2K

 *

 * Licensed under the Apache License, Version 2.0 (the "License");

 * http://www.apache.org/licenses/LICENSE-2.0

 * Unless required by applicable law or agreed to in writing, software

 * distributed under the License is distributed on an "AS IS" BASIS,

 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.

 */

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
        systemStore.fetchHealth()

        // Then set up interval
        intervalId = setInterval(() => {
            systemStore.fetchStats()
            systemStore.fetchEdgeNodes()
            systemStore.fetchHealth()
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
        backendHealth: computed(() => systemStore.backendHealth),
        healthStatus: computed(() => systemStore.healthStatus),
        healthColor: computed(() => systemStore.healthColor),
        lastUpdate: computed(() => systemStore.lastUpdate),
        error: computed(() => systemStore.error),
        isPolling: readonly(isPolling),
        startPolling,
        stopPolling,
    }
}
