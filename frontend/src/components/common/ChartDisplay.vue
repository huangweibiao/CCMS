<template>
  <div class="chart-display">
    <!-- 自定义图表配置面板 -->
    <div v-if="showConfigPanel" class="config-panel">
      <div class="config-header">
        <h4>图表配置</h4>
        <el-button type="text" @click="showConfigPanel = false">
          <i class="el-icon-close" />
        </el-button>
      </div>
      <div class="config-content">
        <el-form label-width="80px" size="small">
          <el-form-item label="图表类型">
            <el-select v-model="chartConfig.type" @change="onChartTypeChange">
              <el-option 
                v-for="type in chartTypes" 
                :key="type.value" 
                :label="type.label" 
                :value="type.value"
              />
            </el-select>
          </el-form-item>
          
          <el-form-item v-if="chartConfig.type !== 'pie' && chartConfig.type !== 'gauge'" label="X轴标题">
            <el-input v-model="chartConfig.xAxis.name" />
          </el-form-item>
          
          <el-form-item v-if="chartConfig.type !== 'pie' && chartConfig.type !== 'gauge'" label="Y轴标题">
            <el-input v-model="chartConfig.yAxis.name" />
          </el-form-item>
          
          <el-form-item label="显示图例">
            <el-switch v-model="chartConfig.legend.show" />
          </el-form-item>
          
          <el-form-item label="显示网格">
            <el-switch v-model="chartConfig.grid.show" />
          </el-form-item>
          
          <el-form-item v-if="chartConfig.type === 'bar'" label="柱状类型">
            <el-radio-group v-model="chartConfig.barType">
              <el-radio label="default">常规</el-radio>
              <el-radio label="stack">堆叠</el-radio>
              <el-radio label="percent">百分比</el-radio>
            </el-radio-group>
          </el-form-item>
          
          <el-form-item label="平滑曲线">
            <el-switch v-model="chartConfig.smooth" :disabled="chartConfig.type !== 'line'"/>
          </el-form-item>
          
          <el-form-item label="数值标签">
            <el-switch v-model="chartConfig.showLabel"/>
          </el-form-item>
          
          <el-form-item label="主题颜色">
            <el-color-picker v-model="chartConfig.theme" show-alpha />
          </el-form-item>
          
          <el-form-item>
            <el-button type="primary" size="small" @click="applyConfig">应用配置</el-button>
            <el-button size="small" @click="resetConfig">重置</el-button>
          </el-form-item>
        </el-form>
      </div>
    </div>

    <!-- 图表容器 -->
    <div class="chart-container" :style="{ height: height + 'px' }">
      <!-- 图表操作工具栏 -->
      <div class="chart-toolbar" v-if="showToolbar">
        <div class="toolbar-left">
          <span class="chart-title">{{ title }}</span>
        </div>
        <div class="toolbar-right">
          <el-button-group size="small">
            <el-button 
              v-for="type in supportedTypes" 
              :key="type.value"
              :type="chartConfig.type === type.value ? 'primary' : ''"
              @click="changeChartType(type.value)"
            >
              {{ type.label }}
            </el-button>
          </el-button-group>
          
          <el-dropdown trigger="click" @command="handleToolCommand">
            <el-button size="small" type="text">
              <i class="el-icon-setting" /> 更多
            </el-button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="config">图表配置</el-dropdown-item>
                <el-dropdown-item command="download">下载图片</el-dropdown-item>
                <el-dropdown-item command="refresh">刷新数据</el-dropdown-item>
                <el-dropdown-item command="fullscreen">全屏查看</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </div>
      
      <!-- 图表渲染区域 -->
      <div ref="chartRef" class="chart-render-area"></div>
      
      <!-- 数据加载状态 -->
      <div v-if="loading" class="chart-loading">
        <el-icon class="loading-icon"><el-icon-loading /></el-icon>
        <span>图表数据加载中...</span>
      </div>
      
      <!-- 无数据提示 -->
      <div v-else-if="!hasData" class="chart-empty">
        <el-empty description="暂无数据" />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, onUnmounted, watch, nextTick, computed } from 'vue'
import * as echarts from 'echarts'

// 组件属性接口
interface Props {
  title?: string
  height?: number
  chartData: ChartData
  chartType?: string
  loading?: boolean
  showToolbar?: boolean
  customOptions?: any
  responsive?: boolean
}

// 图表数据结构
interface ChartData {
  categories?: string[]
  series: SeriesItem[]
  xAxisName?: string
  yAxisName?: string
}

interface SeriesItem {
  name: string
  type?: string
  data: number[]
  color?: string
  smooth?: boolean
  stack?: string
}

// 默认属性值
const props = withDefaults(defineProps<Props>(), {
  title: '数据图表',
  height: 400,
  chartType: 'line',
  loading: false,
  showToolbar: true,
  responsive: true
})

// 图表类型定义
const chartTypes = [
  { value: 'line', label: '折线图' },
  { value: 'bar', label: '柱状图' },
  { value: 'pie', label: '饼图' },
  { value: 'radar', label: '雷达图' },
  { value: 'scatter', label: '散点图' },
  { value: 'gauge', label: '仪表盘' },
  { value: 'heatmap', label: '热力图' }
]

// 响应式数据
const chartRef = ref<HTMLDivElement>()
const chartInstance = ref<echarts.ECharts>()
const showConfigPanel = ref(false)

// 图表配置
const chartConfig = reactive({
  type: props.chartType,
  xAxis: { name: props.chartData.xAxisName || 'X轴' },
  yAxis: { name: props.chartData.yAxisName || 'Y轴' },
  legend: { show: true },
  grid: { show: true },
  barType: 'default',
  smooth: false,
  showLabel: false,
  theme: '#409EFF'
})

// 计算属性
const hasData = computed(() => {
  return props.chartData.series.length > 0 && 
         props.chartData.series.some(series => series.data.length > 0)
})

const supportedTypes = computed(() => {
  const data = props.chartData
  const types = [...chartTypes]
  
  // 根据数据类型过滤支持的图表类型
  if (data.series.length > 1 && data.categories && data.categories.length > 5) {
    // 多系列大数据量，推荐柱状图或折线图
    return types.filter(type => ['line', 'bar'].includes(type.value))
  }
  if (data.series.length === 1) {
    // 单系列数据，支持所有类型
    return types
  }
  return types
})

// 监听数据变化
watch(() => props.chartData, () => {
  nextTick(() => {
    updateChart()
  })
}, { deep: true })

watch(() => props.loading, (newVal) => {
  if (!newVal) {
    nextTick(() => {
      updateChart()
    })
  }
})

// 方法定义
const initChart = () => {
  if (!chartRef.value) return
  
  chartInstance.value = echarts.init(chartRef.value)
  
  // 监听窗口大小变化
  if (props.responsive) {
    window.addEventListener('resize', handleResize)
  }
  
  updateChart()
}

const updateChart = () => {
  if (!chartInstance.value || !hasData.value) return
  
  const option = generateChartOption()
  chartInstance.value.setOption(option, true)
}

const generateChartOption = () => {
  const { categories, series } = props.chartData
  
  const baseOption = {
    title: {
      text: props.title,
      left: 'center',
      textStyle: {
        fontSize: 16,
        fontWeight: 'bold'
      }
    },
    tooltip: {
      trigger: chartConfig.type === 'pie' ? 'item' : 'axis',
      formatter: chartConfig.type === 'pie' ? 
        '{a} <br/>{b}: {c} ({d}%)' : 
        '{b}<br/>{a}: {c}'
    },
    legend: chartConfig.legend.show ? {
      show: true,
      bottom: 0,
      data: series.map(s => s.name)
    } : { show: false },
    grid: chartConfig.grid.show ? {
      left: '3%',
      right: '4%',
      bottom: chartConfig.legend.show ? '10%' : '3%',
      containLabel: true
    } : { show: false }
  }
  
  let typeSpecificOption = {}
  
  switch (chartConfig.type) {
    case 'line':
      typeSpecificOption = {
        xAxis: {
          type: 'category',
          data: categories || [],
          name: chartConfig.xAxis.name
        },
        yAxis: {
          type: 'value',
          name: chartConfig.yAxis.name
        },
        series: series.map(s => ({
          name: s.name,
          type: 'line',
          data: s.data,
          smooth: chartConfig.smooth,
          lineStyle: { color: s.color },
          showSymbol: series.length === 1
        }))
      }
      break
      
    case 'bar':
      typeSpecificOption = {
        xAxis: {
          type: 'category',
          data: categories || [],
          name: chartConfig.xAxis.name
        },
        yAxis: {
          type: 'value',
          name: chartConfig.yAxis.name
        },
        series: series.map(s => ({
          name: s.name,
          type: 'bar',
          data: s.data,
          itemStyle: { color: s.color },
          stack: chartConfig.barType !== 'default' ? 'total' : undefined,
          label: chartConfig.showLabel ? { show: true, position: 'top' } : undefined
        }))
      }
      break
      
    case 'pie':
      typeSpecificOption = {
        series: [{
          name: props.title,
          type: 'pie',
          radius: '60%',
          data: categories ? categories.map((name, index) => ({
            name,
            value: series[0].data[index] || 0
          })) : [],
          emphasis: {
            itemStyle: {
              shadowBlur: 10,
              shadowOffsetX: 0,
              shadowColor: 'rgba(0, 0, 0, 0.5)'
            }
          }
        }]
      }
      break
      
    case 'radar':
      typeSpecificOption = {
        radar: {
          indicator: categories ? categories.map(name => ({ name, max: Math.max(...series.map(s => Math.max(...s.data))) })) : []
        },
        series: [{
          type: 'radar',
          data: series.map(s => ({
            name: s.name,
            value: s.data
          }))
        }]
      }
      break
      
    case 'gauge':
      typeSpecificOption = {
        series: [{
          type: 'gauge',
          progress: { show: true },
          detail: { valueAnimation: true, fontSize: 20 },
          data: [{ value: series[0]?.data[0] || 0, name: series[0]?.name }]
        }]
      }
      break
  }
  
  return { ...baseOption, ...typeSpecificOption, ...props.customOptions }
}

const handleResize = () => {
  chartInstance.value?.resize()
}

const changeChartType = (type: string) => {
  chartConfig.type = type
  updateChart()
}

const onChartTypeChange = () => {
  // 切换图表类型时的特殊处理
  if (chartConfig.type === 'gauge') {
    chartConfig.showLabel = false
  }
  updateChart()
}

const handleToolCommand = (command: string) => {
  switch (command) {
    case 'config':
      showConfigPanel.value = true
      break
    case 'download':
      downloadChart()
      break
    case 'refresh':
      emit('refresh')
      break
    case 'fullscreen':
      toggleFullscreen()
      break
  }
}

const downloadChart = () => {
  if (!chartInstance.value) return
  
  const url = chartInstance.value.getDataURL({
    type: 'png',
    pixelRatio: 2,
    backgroundColor: '#fff'
  })
  
  const link = document.createElement('a')
  link.href = url
  link.download = `${props.title || 'chart'}.png`
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
}

const toggleFullscreen = () => {
  if (!chartRef.value) return
  
  if (document.fullscreenElement) {
    document.exitFullscreen()
  } else {
    chartRef.value.requestFullscreen()
  }
}

const applyConfig = () => {
  showConfigPanel.value = false
  updateChart()
}

const resetConfig = () => {
  Object.assign(chartConfig, {
    type: props.chartType,
    xAxis: { name: props.chartData.xAxisName || 'X轴' },
    yAxis: { name: props.chartData.yAxisName || 'Y轴' },
    legend: { show: true },
    grid: { show: true },
    barType: 'default',
    smooth: false,
    showLabel: false,
    theme: '#409EFF'
  })
  updateChart()
}

// 事件发射器
const emit = defineEmits<{
  refresh: []
  typeChange: [type: string]
  dataClick: [data: any]
}>()

// 生命周期
onMounted(() => {
  initChart()
})

onUnmounted(() => {
  if (chartInstance.value) {
    chartInstance.value.dispose()
  }
  window.removeEventListener('resize', handleResize)
})

// 暴露方法给父组件
defineExpose({
  getInstance: () => chartInstance.value,
  resize: handleResize,
  download: downloadChart
})
</script>

<style scoped>
.chart-display {
  position: relative;
  width: 100%;
}

.config-panel {
  position: absolute;
  right: 0;
  top: 0;
  width: 300px;
  height: 100%;
  background: #fff;
  border-left: 1px solid #e4e7ed;
  z-index: 10;
  box-shadow: -2px 0 8px rgba(0, 0, 0, 0.1);
}

.config-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px;
  border-bottom: 1px solid #e4e7ed;
}

.config-header h4 {
  margin: 0;
  font-size: 16px;
}

.config-content {
  padding: 15px;
  height: calc(100% - 60px);
  overflow-y: auto;
}

.chart-container {
  position: relative;
  background: #fff;
  border-radius: 4px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  padding: 20px;
}

.chart-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
  padding-bottom: 15px;
  border-bottom: 1px solid #f0f0f0;
}

.chart-title {
  font-size: 16px;
  font-weight: bold;
  color: #303133;
}

.toolbar-right {
  display: flex;
  align-items: center;
  gap: 10px;
}

.chart-render-area {
  width: 100%;
  height: calc(100% - 50px);
}

.chart-loading, .chart-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  color: #909399;
}

.loading-icon {
  font-size: 24px;
  margin-bottom: 10px;
  animation: rotate 2s linear infinite;
}

@keyframes rotate {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

/* 响应式设计 */
@media (max-width: 768px) {
  .chart-toolbar {
    flex-direction: column;
    align-items: stretch;
    gap: 10px;
  }
  
  .toolbar-right {
    justify-content: center;
  }
  
  .config-panel {
    width: 100%;
    height: auto;
    position: fixed;
    bottom: 0;
    left: 0;
    right: 0;
    top: auto;
    max-height: 70vh;
    overflow-y: auto;
  }
}
</style>