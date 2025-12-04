/** 

 * Copyright 2025 Haui.HIT - H2K

 *

 * Licensed under the Apache License, Version 2.0 (the "License");

 * http://www.apache.org/licenses/LICENSE-2.0

 * Unless required by applicable law or agreed to in writing, software

 * distributed under the License is distributed on an "AS IS" BASIS,

 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.

 */

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

export interface BackendHealth {
    status: 'healthy' | 'degraded' | 'offline'
    message?: string
    responseTime?: number
    lastCheck?: Date
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
        backendHealth: {
            status: 'offline',
            message: 'Checking...',
            responseTime: 0,
            lastCheck: undefined,
        } as BackendHealth,
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
                const data = await $fetch<EdgeNode[]>('/api/nodes')

                if (data && Array.isArray(data)) {
                    this.edgeNodes = data
                } else {
                    console.warn('Invalid edge nodes data received:', data)
                    this.edgeNodes = []
                }
                this.error = null
            } catch (err: any) {
                this.error = err?.message || 'Failed to fetch edge nodes'
                console.error('Error fetching edge nodes:', err)
            }
        },

        async fetchHealth() {
            const startTime = performance.now()

            try {
                // Try to fetch stats as a health check
                await $fetch('/api/stats')
                const endTime = performance.now()
                const responseTime = Math.round(endTime - startTime)

                // Determine status based on response time
                let status: 'healthy' | 'degraded' | 'offline' = 'healthy'
                let message = 'All systems operational'

                if (responseTime > 2000) {
                    status = 'degraded'
                    message = 'Slow response time'
                } else if (responseTime > 1000) {
                    status = 'degraded'
                    message = 'Elevated response time'
                }

                this.backendHealth = {
                    status,
                    message,
                    responseTime,
                    lastCheck: new Date(),
                }

                // Clear error if health check succeeds
                if (this.error?.includes('Backend')) {
                    this.error = null
                }
            } catch (err: any) {
                const endTime = performance.now()
                const responseTime = Math.round(endTime - startTime)

                this.backendHealth = {
                    status: 'offline',
                    message: err?.message || 'Backend unreachable',
                    responseTime,
                    lastCheck: new Date(),
                }

                // Only set error if it's not already set
                if (!this.error) {
                    this.error = 'Backend service unavailable'
                }

                console.error('Backend health check failed:', err)
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

        healthStatus: (state) => {
            const { status } = state.backendHealth
            return status === 'healthy' ? 'Healthy' :
                status === 'degraded' ? 'Degraded' : 'Offline'
        },

        healthColor: (state) => {
            const { status } = state.backendHealth
            return status === 'healthy' ? 'bg-neon-green' :
                status === 'degraded' ? 'bg-yellow-500' : 'bg-red-500'
        },
    },
})
