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
  <div ref="chartContainer" class="w-full" :style="{ height: height }"></div>
</template>

<script setup lang="ts">
import * as echarts from 'echarts'
import type { ECharts } from 'echarts'
import type { RateSnapshot } from '~/stores/system'

interface Props {
  rateHistory: RateSnapshot[]
  height?: string
}

const props = withDefaults(defineProps<Props>(), {
  height: '400px'
})

const chartContainer = ref<HTMLElement | null>(null)
let chartInstance: ECharts | null = null

const initChart = () => {
  if (!chartContainer.value) return

  // Dispose existing chart instance if any
  if (chartInstance) {
    chartInstance.dispose()
  }

  chartInstance = echarts.init(chartContainer.value)
  updateChart()
}

const updateChart = () => {
  if (!chartInstance || !props.rateHistory || props.rateHistory.length === 0) return

  // Prepare data - take last 50 points for better visualization
  const data = props.rateHistory.slice(-50)
  
  const timestamps = data.map(item => {
    const date = new Date(item.timestamp)
    return date.toLocaleTimeString()
  })

  const incomingRates = data.map(item => item.incomingRate)
  const processedRates = data.map(item => item.processedRate)

  const option = {
    backgroundColor: 'transparent',
    tooltip: {
      trigger: 'axis',
      backgroundColor: 'rgba(17, 24, 39, 0.95)',
      borderColor: 'rgba(75, 85, 99, 0.5)',
      textStyle: {
        color: '#e5e7eb'
      },
      formatter: (params: any) => {
        let result = `<div style="font-weight: 600; margin-bottom: 8px;">${params[0].axisValue}</div>`
        params.forEach((param: any) => {
          result += `
            <div style="display: flex; align-items: center; gap: 8px; margin-top: 4px;">
              <span style="display: inline-block; width: 10px; height: 10px; border-radius: 50%; background-color: ${param.color};"></span>
              <span>${param.seriesName}: <strong>${param.value}/s</strong></span>
            </div>
          `
        })
        return result
      }
    },
    legend: {
      data: ['Incoming', 'Processed'],
      textStyle: {
        color: '#9ca3af'
      },
      top: 10
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      top: '60px',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: timestamps,
      axisLine: {
        lineStyle: {
          color: 'rgba(75, 85, 99, 0.3)'
        }
      },
      axisLabel: {
        color: '#6b7280',
        rotate: 45,
        fontSize: 10
      },
      splitLine: {
        show: false
      }
    },
    yAxis: {
      type: 'value',
      name: 'Rate (msg/s)',
      nameTextStyle: {
        color: '#9ca3af',
        fontSize: 12
      },
      axisLine: {
        lineStyle: {
          color: 'rgba(75, 85, 99, 0.3)'
        }
      },
      axisLabel: {
        color: '#6b7280',
        fontSize: 10
      },
      splitLine: {
        lineStyle: {
          color: 'rgba(75, 85, 99, 0.2)',
          type: 'dashed'
        }
      }
    },
    series: [
      {
        name: 'Incoming',
        type: 'line',
        smooth: true,
        symbol: 'circle',
        symbolSize: 6,
        lineStyle: {
          width: 3,
          color: {
            type: 'linear',
            x: 0,
            y: 0,
            x2: 1,
            y2: 0,
            colorStops: [
              { offset: 0, color: '#06b6d4' },
              { offset: 1, color: '#22d3ee' }
            ]
          }
        },
        itemStyle: {
          color: '#06b6d4',
          borderColor: '#06b6d4',
          borderWidth: 2
        },
        areaStyle: {
          color: {
            type: 'linear',
            x: 0,
            y: 0,
            x2: 0,
            y2: 1,
            colorStops: [
              { offset: 0, color: 'rgba(6, 182, 212, 0.3)' },
              { offset: 1, color: 'rgba(6, 182, 212, 0.05)' }
            ]
          }
        },
        data: incomingRates
      },
      {
        name: 'Processed',
        type: 'line',
        smooth: true,
        symbol: 'circle',
        symbolSize: 6,
        lineStyle: {
          width: 3,
          color: {
            type: 'linear',
            x: 0,
            y: 0,
            x2: 1,
            y2: 0,
            colorStops: [
              { offset: 0, color: '#10b981' },
              { offset: 1, color: '#34d399' }
            ]
          }
        },
        itemStyle: {
          color: '#10b981',
          borderColor: '#10b981',
          borderWidth: 2
        },
        areaStyle: {
          color: {
            type: 'linear',
            x: 0,
            y: 0,
            x2: 0,
            y2: 1,
            colorStops: [
              { offset: 0, color: 'rgba(16, 185, 129, 0.3)' },
              { offset: 1, color: 'rgba(16, 185, 129, 0.05)' }
            ]
          }
        },
        data: processedRates
      }
    ]
  }

  chartInstance.setOption(option)
}

// Watch for data changes
watch(() => props.rateHistory, () => {
  updateChart()
}, { deep: true })

// Handle resize
const handleResize = () => {
  if (chartInstance) {
    chartInstance.resize()
  }
}

onMounted(() => {
  initChart()
  window.addEventListener('resize', handleResize)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  if (chartInstance) {
    chartInstance.dispose()
  }
})
</script>

<style scoped>
/* Ensure chart container has proper dimensions */
div {
  min-height: 300px;
}
</style>
