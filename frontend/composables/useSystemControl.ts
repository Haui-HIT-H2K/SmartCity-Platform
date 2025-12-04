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
