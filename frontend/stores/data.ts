SPDX-License-Identifier: Apache-2.0
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

export type DataType = 'HOT' | 'WARM' | 'COLD'

export interface CityData {
    id: string
    sensorId: string
    type: DataType
    value: number
    timestamp: string
    location?: {
        lat: number
        lng: number
    }
    metadata?: Record<string, any>
}

export interface DataFilter {
    type?: DataType
    sensorId?: string
    page: number
    pageSize: number
}

export interface DataResponse {
    data: CityData[]
    total: number
    page: number
    pageSize: number
    totalPages: number
}

export const useDataStore = defineStore('data', {
    state: () => ({
        data: [] as CityData[],
        filter: {
            page: 1,
            pageSize: 20,
        } as DataFilter,
        total: 0,
        totalPages: 0,
        isLoading: false,
        error: null as string | null,
    }),

    actions: {
        async fetchData() {
            try {
                this.isLoading = true
                const params = new URLSearchParams()

                if (this.filter.type) params.append('type', this.filter.type)
                if (this.filter.sensorId) params.append('sensorId', this.filter.sensorId)
                params.append('page', String(this.filter.page - 1)) // Backend uses 0-based indexing
                params.append('size', String(this.filter.pageSize))

                const response = await $fetch<DataResponse>(`/api/data?${params.toString()}`)

                this.data = response.data
                this.total = response.total
                this.totalPages = response.totalPages
                this.error = null
            } catch (err: any) {
                this.error = err?.message || 'Failed to fetch data'
                console.error('Error fetching data:', err)
            } finally {
                this.isLoading = false
            }
        },

        setFilter(filter: Partial<DataFilter>) {
            this.filter = { ...this.filter, ...filter }
            // Reset to page 1 when filter changes
            if ('type' in filter || 'sensorId' in filter) {
                this.filter.page = 1
            }
        },

        nextPage() {
            if (this.filter.page < this.totalPages) {
                this.filter.page++
                this.fetchData()
            }
        },

        prevPage() {
            if (this.filter.page > 1) {
                this.filter.page--
                this.fetchData()
            }
        },

        goToPage(page: number) {
            if (page >= 1 && page <= this.totalPages) {
                this.filter.page = page
                this.fetchData()
            }
        },
    },

    getters: {
        hasMore: (state) => state.filter.page < state.totalPages,
        hasPrevious: (state) => state.filter.page > 1,
    },
})
