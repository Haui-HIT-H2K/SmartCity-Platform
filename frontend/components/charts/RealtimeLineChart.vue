<template>
  <div ref="chartRef" class="w-full" :style="{ height: height }" />
</template>

<script setup lang="ts">
import * as echarts from 'echarts/core'
import { LineChart } from 'echarts/charts'
import {
  GridComponent,
  TooltipComponent,
  LegendComponent,
  DataZoomComponent,
} from 'echarts/components'
import { CanvasRenderer } from 'echarts/renderers'
import type { EChartsOption } from 'echarts'

// Register ECharts components
echarts.use([
  LineChart,
  GridComponent,
  TooltipComponent,
  LegendComponent,
  DataZoomComponent,
  CanvasRenderer,
])

interface Props {
  incomingRate: number
  processedRate: number
  height?: string
}

const props = withDefaults(defineProps<Props>(), {
  height: '300px',
})

const chartRef = ref<HTMLElement>()
let chart: echarts.ECharts | null = null
const timeData = ref<string[]>([])
const incomingData = ref<number[]>([])
const processedData = ref<number[]>([])
const maxDataPoints = 30

const initChart = () => {
  if (!chartRef.value) return

  chart = echarts.init(chartRef.value, 'dark')
  updateChart()
}

const updateChart = () => {
  if (!chart) return

  const now = new Date()
  const timeStr = now.toLocaleTimeString()

  // Add new data point
  timeData.value.push(timeStr)
  incomingData.value.push(props.incomingRate)
  processedData.value.push(props.processedRate)

  // Keep only last N data points
  if (timeData.value.length > maxDataPoints) {
    timeData.value.shift()
    incomingData.value.shift()
    processedData.value.shift()
  }

  const option: EChartsOption = {
    backgroundColor: 'transparent',
    tooltip: {
      trigger: 'axis',
      backgroundColor: 'rgba(26, 31, 58, 0.95)',
      borderColor: '#00f0ff',
      borderWidth: 1,
      textStyle: {
        color: '#e5e7eb',
      },
    },
    legend: {
      data: ['Incoming Rate', 'Processed Rate'],
      textStyle: {
        color: '#9ca3af',
      },
      top: 10,
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '10%',
      top: '15%',
      containLabel: true,
    },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: timeData.value,
      axisLine: {
        lineStyle: {
          color: '#2a2f4a',
        },
      },
      axisLabel: {
        color: '#6b7280',
        rotate: 45,
      },
    },
    yAxis: {
      type: 'value',
      name: 'Requests/s',
      nameTextStyle: {
        color: '#9ca3af',
      },
      axisLine: {
        lineStyle: {
          color: '#2a2f4a',
        },
      },
      axisLabel: {
        color: '#6b7280',
      },
      splitLine: {
        lineStyle: {
          color: '#1a1f3a',
        },
      },
    },
    series: [
      {
        name: 'Incoming Rate',
        type: 'line',
        smooth: true,
        data: incomingData.value,
        lineStyle: {
          color: '#00f0ff',
          width: 2,
          shadowColor: 'rgba(0, 240, 255, 0.5)',
          shadowBlur: 10,
        },
        itemStyle: {
          color: '#00f0ff',
        },
        areaStyle: {
          color: {
            type: 'linear',
            x: 0,
            y: 0,
            x2: 0,
            y2: 1,
            colorStops: [
              { offset: 0, color: 'rgba(0, 240, 255, 0.4)' },
              { offset: 1, color: 'rgba(0, 240, 255, 0.0)' },
            ],
          },
        },
      },
      {
        name: 'Processed Rate',
        type: 'line',
        smooth: true,
        data: processedData.value,
        lineStyle: {
          color: '#00ff41',
          width: 2,
          shadowColor: 'rgba(0, 255, 65, 0.5)',
          shadowBlur: 10,
        },
        itemStyle: {
          color: '#00ff41',
        },
        areaStyle: {
          color: {
            type: 'linear',
            x: 0,
            y: 0,
            x2: 0,
            y2: 1,
            colorStops: [
              { offset: 0, color: 'rgba(0, 255, 65, 0.3)' },
              { offset: 1, color: 'rgba(0, 255, 65, 0.0)' },
            ],
          },
        },
      },
    ],
  }

  chart.setOption(option)
}

// Watch for prop changes and update chart
watch(
  () => [props.incomingRate, props.processedRate],
  () => {
    updateChart()
  }
)

// Handle window resize
const handleResize = () => {
  chart?.resize()
}

onMounted(() => {
  initChart()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  chart?.dispose()
})
</script>
