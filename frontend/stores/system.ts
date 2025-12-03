import { defineStore } from 'pinia'

export interface EdgeNode {
    id: string
    name: string
    host: string
    port: number
    status: 'online' | 'offline'
    lastPing?: string
}

export interface RateSnapshot {
    timestamp: number
    incomingRate: number
    processedRate: number
}

export interface SystemStats {
    hotCount: number
    warmCount: number
    coldCount: number
    totalCount: number
    incomingRate: number
    processedRate: number
    rateHistory: RateSnapshot[]
}

export const useSystemStore = defineStore('system', {
    state: () => ({
        stats: {
            hotCount: 0,
            warmCount: 0,
            coldCount: 0,
            totalCount: 0,
            incomingRate: 0,
            processedRate: 0,
            rateHistory: [],
        } as SystemStats,
        edgeNodes: [] as EdgeNode[],
        isLoading: false,
        error: null as string | null,
        lastUpdate: null as Date | null,
    }),

    actions: {
        async fetchStats() {
            try {
                const data = await $fetch<SystemStats>('/api/stats')
                this.stats = data
                this.lastUpdate = new Date()
                this.error = null
            } catch (err: any) {
                this.error = err?.message || 'Failed to fetch stats'
                console.error('Error fetching stats:', err)
            }
        },

        async fetchEdgeNodes() {
            try {
                console.log('Fetching edge nodes from /api/nodes...')
                const data = await $fetch<EdgeNode[]>('/api/nodes')
                console.log('Received edge nodes data:', data)
                console.log('Edge nodes count:', data?.length || 0)

                if (data && Array.isArray(data)) {
                    this.edgeNodes = data
                    console.log('Successfully set edge nodes:', this.edgeNodes)
                } else {
                    console.warn('Invalid edge nodes data received:', data)
                    this.edgeNodes = []
                }
                this.error = null
            } catch (err: any) {
                this.error = err?.message || 'Failed to fetch edge nodes'
                console.error('Error fetching edge nodes:', err)
                console.error('Error details:', err)
            }
        },

        async syncData() {
            try {
                this.isLoading = true
                await $fetch('/api/sync/trigger', {
                    method: 'POST',
                })
                // Refresh stats after sync
                await this.fetchStats()
                this.error = null
            } catch (err: any) {
                this.error = err?.message || 'Failed to sync data'
                console.error('Error syncing data:', err)
                throw err
            } finally {
                this.isLoading = false
            }
        },
    },

    getters: {
        onlineNodes: (state) => state.edgeNodes.filter(node => node.status === 'online'),
        offlineNodes: (state) => state.edgeNodes.filter(node => node.status === 'offline'),
        hasActiveNodes: (state) => state.edgeNodes.some(node => node.status === 'online'),
    },
})
