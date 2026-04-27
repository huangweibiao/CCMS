<template>
  <div class="expense-report">
    <!-- 页面标题和筛选条件 -->
    <div class="page-header">
      <h2 class="page-title">费用统计报表</h2>
      <div class="filter-card">
        <el-row :gutter="20">
          <el-col :span="6">
            <el-form-item label="统计周期">
              <el-select v-model="queryParams.timeRange" placeholder="请选择统计周期" clearable>
                <el-option label="今日" value="today" />
                <el-option label="本周" value="week" />
                <el-option label="本月" value="month" />
                <el-option label="本季度" value="quarter" />
                <el-option label="本年度" value="year" />
                <el-option label="自定义" value="custom" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="6" v-if="queryParams.timeRange === 'custom'">
            <el-form-item label="开始日期">
              <el-date-picker
                v-model="queryParams.startDate"
                type="date"
                placeholder="选择开始日期"
                value-format="YYYY-MM-DD"
                clearable
              />
            </el-form-item>
          </el-col>
          <el-col :span="6" v-if="queryParams.timeRange === 'custom'">
            <el-form-item label="结束日期">
              <el-date-picker
                v-model="queryParams.endDate"
                type="date"
                placeholder="选择结束日期"
                value-format="YYYY-MM-DD"
                clearable
              />
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="部门">
              <el-select v-model="queryParams.deptId" placeholder="请选择部门" clearable filterable>
                <el-option v-for="dept in deptList" :key="dept.id" :label="dept.name" :value="dept.id" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="6">
            <el-form-item label="费用类型">
              <el-select v-model="queryParams.expenseType" placeholder="请选择费用类型" clearable>
                <el-option v-for="type in expenseTypes" :key="type.value" :label="type.label" :value="type.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="统计维度">
              <el-select v-model="queryParams.dimension" placeholder="请选择统计维度" clearable>
                <el-option label="按部门" value="dept" />
                <el-option label="按费用类型" value="type" />
                <el-option label="按时间段" value="time" />
                <el-option label="按项目" value="project" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <div class="filter-actions">
              <el-button type="primary" @click="handleSearch">查询</el-button>
              <el-button @click="handleReset">重置</el-button>
            </div>
          </el-col>
        </el-row>
      </div>
    </div>

    <!-- 统计概览卡片 -->
    <div class="statistics-overview">
      <el-row :gutter="20">
        <el-col :span="6">
          <el-card class="stat-card" shadow="hover">
            <div class="stat-container">
              <div class="stat-icon expense-icon">
                <i class="el-icon-money" />
              </div>
              <div class="stat-content">
                <div class="stat-value">{{ formatAmount(totalAmount) }}</div>
                <div class="stat-label">总费用金额</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stat-card" shadow="hover">
            <div class="stat-container">
              <div class="stat-icon apply-icon">
                <i class="el-icon-document" />
              </div>
              <div class="stat-content">
                <div class="stat-value">{{ totalCount }}</div>
                <div class="stat-label">总单据量</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stat-card" shadow="hover">
            <div class="stat-container">
              <div class="stat-icon avg-icon">
                <i class="el-icon-coin" />
              </div>
              <div class="stat-content">
                <div class="stat-value">{{ formatAmount(avgAmount) }}</div>
                <div class="stat-label">平均费用</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stat-card" shadow="hover">
            <div class="stat-container">
              <div class="stat-icon trend-icon">
                <i class="el-icon-trend-charts" />
              </div>
              <div class="stat-content">
                <div class="stat-value" :class="{ 'positive': trendRate > 0, 'negative': trendRate < 0 }">
                  {{ trendRate > 0 ? '+' : '' }}{{ trendRate }}%
                </div>
                <div class="stat-label">环比增长率</div>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 图表展示区域 -->
    <div class="chart-section">
      <el-card shadow="hover">
        <template #header>
          <div class="chart-header">
            <span class="chart-title">费用分布图表</span>
            <div class="chart-actions">
              <el-button-group>
                <el-button :type="chartType === 'bar' ? 'primary' : ''" @click="chartType = 'bar'">柱状图</el-button>
                <el-button :type="chartType === 'pie' ? 'primary' : ''" @click="chartType = 'pie'">饼图</el-button>
                <el-button :type="chartType === 'line' ? 'primary' : ''" @click="chartType = 'line'">趋势图</el-button>
              </el-button-group>
            </div>
          </div>
        </template>
        <div class="chart-container">
          <div ref="chartRef" style="height: 400px; width: 100%;"></div>
        </div>
      </el-card>
    </div>

    <!-- 数据表格 -->
    <div class="data-table-section">
      <el-card shadow="hover">
        <template #header>
          <div class="table-header">
            <span class="table-title">费用明细数据</span>
            <div class="table-actions">
              <el-button type="primary" @click="handleExport">导出Excel</el-button>
            </div>
          </div>
        </template>
        <el-table :data="tableData" v-loading="loading" border style="width: 100%">
          <el-table-column prop="category" label="分类" align="center" />
          <el-table-column prop="amount" label="金额" align="center">
            <template #default="{ row }">
              {{ formatAmount(row.amount) }}
            </template>
          </el-table-column>
          <el-table-column prop="count" label="单据数" align="center" />
          <el-table-column prop="percentage" label="占比" align="center">
            <template #default="{ row }">
              {{ row.percentage }}%
            </template>
          </el-table-column>
          <el-table-column prop="avgAmount" label="平均金额" align="center">
            <template #default="{ row }">
              {{ formatAmount(row.avgAmount) }}
            </template>
          </el-table-column>
          <el-table-column label="趋势" align="center">
            <template #default="{ row }">
              <el-tag :type="row.trend > 0 ? 'success' : row.trend < 0 ? 'danger' : 'info'">
                {{ row.trend > 0 ? '+' : '' }}{{ row.trend }}%
              </el-tag>
            </template>
          </el-table-column>
        </el-table>
        <div class="pagination-container">
          <el-pagination
            v-model:current-page="pageParams.current"
            v-model:page-size="pageParams.size"
            :total="pageParams.total"
            :page-sizes="[10, 20, 50, 100]"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="handlePageSizeChange"
            @current-change="handlePageChange"
          />
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed, watch } from 'vue'
import * as echarts from 'echarts'

// 接口定义
interface QueryParams {
  timeRange: string
  startDate: string
  endDate: string
  deptId: number
  expenseType: string
  dimension: string
}

interface DeptItem {
  id: number
  name: string
}

interface ExpenseTypeItem {
  value: string
  label: string
}

interface StatisticsData {
  totalAmount: number
  totalCount: number
  avgAmount: number
  trendRate: number
  tableData: any[]
}

// 响应式数据
const queryParams = reactive<QueryParams>({
  timeRange: 'month',
  startDate: '',
  endDate: '',
  deptId: 0,
  expenseType: '',
  dimension: 'dept'
})

const pageParams = reactive({
  current: 1,
  size: 10,
  total: 0
})

const statisticsData = reactive<StatisticsData>({
  totalAmount: 0,
  totalCount: 0,
  avgAmount: 0,
  trendRate: 0,
  tableData: []
})

const loading = ref(false)
const chartRef = ref<HTMLDivElement>()
const chartInstance = ref<echarts.ECharts>()
const chartType = ref<'bar' | 'pie' | 'line'>('bar')

// 模拟数据
const deptList = ref<DeptItem[]>([
  { id: 1, name: '技术部' },
  { id: 2, name: '销售部' },
  { id: 3, name: '市场部' },
  { id: 4, name: '财务部' },
  { id: 5, name: '人事部' }
])

const expenseTypes = ref<ExpenseTypeItem[]>([
  { value: 'travel', label: '差旅费' },
  { value: 'meal', label: '餐饮费' },
  { value: 'transport', label: '交通费' },
  { value: 'office', label: '办公费' },
  { value: 'entertain', label: '招待费' }
])

// 计算属性
const totalAmount = computed(() => statisticsData.totalAmount)
const totalCount = computed(() => statisticsData.totalCount)
const avgAmount = computed(() => statisticsData.avgAmount)
const trendRate = computed(() => statisticsData.trendRate)
const tableData = computed(() => statisticsData.tableData)

// 方法
const formatAmount = (amount: number) => {
  return new Intl.NumberFormat('zh-CN', { style: 'currency', currency: 'CNY' }).format(amount)
}

const handleSearch = () => {
  pageParams.current = 1
  loadStatisticsData()
}

const handleReset = () => {
  Object.assign(queryParams, {
    timeRange: 'month',
    startDate: '',
    endDate: '',
    deptId: 0,
    expenseType: '',
    dimension: 'dept'
  })
  handleSearch()
}

const loadStatisticsData = async () => {
  loading.value = true
  try {
    // 模拟API调用
    await new Promise(resolve => setTimeout(resolve, 1000))
    
    // 模拟数据
    statisticsData.totalAmount = 154680.5
    statisticsData.totalCount = 42
    statisticsData.avgAmount = 3682.86
    statisticsData.trendRate = 12.5
    
    statisticsData.tableData = [
      { category: '技术部', amount: 65432, count: 15, percentage: 42.3, avgAmount: 4362.13, trend: 15.2 },
      { category: '销售部', amount: 43210, count: 10, percentage: 27.9, avgAmount: 4321.0, trend: -8.3 },
      { category: '市场部', amount: 28765, count: 8, percentage: 18.6, avgAmount: 3595.63, trend: 25.7 },
      { category: '财务部', amount: 12345, count: 6, percentage: 8.0, avgAmount: 2057.5, trend: 5.1 },
      { category: '人事部', amount: 4930, count: 3, percentage: 3.2, avgAmount: 1643.33, trend: -3.4 }
    ]
    
    pageParams.total = statisticsData.tableData.length
    
    // 更新图表
    updateChart()
  } catch (error) {
    console.error('加载统计数据失败:', error)
  } finally {
    loading.value = false
  }
}

const updateChart = () => {
  if (!chartRef.value) return

  if (!chartInstance.value) {
    chartInstance.value = echarts.init(chartRef.value)
  }

  const option = getChartOption()
  chartInstance.value?.setOption(option, true)
}

const getChartOption = () => {
  const categories = statisticsData.tableData.map(item => item.category)
  const amounts = statisticsData.tableData.map(item => item.amount)

  switch (chartType.value) {
    case 'bar':
      return {
        tooltip: {
          trigger: 'axis',
          axisPointer: { type: 'shadow' }
        },
        legend: {
          data: ['费用金额']
        },
        grid: {
          left: '3%',
          right: '4%',
          bottom: '3%',
          containLabel: true
        },
        xAxis: {
          type: 'category',
          data: categories,
          axisLabel: {
            rotate: 45
          }
        },
        yAxis: {
          type: 'value'
        },
        series: [{
          name: '费用金额',
          type: 'bar',
          data: amounts
        }]
      }

    case 'pie':
      return {
        tooltip: {
          trigger: 'item'
        },
        legend: {
          orient: 'vertical',
          left: 'left'
        },
        series: [{
          name: '费用占比',
          type: 'pie',
          radius: '50%',
          data: statisticsData.tableData.map(item => ({
            name: item.category,
            value: item.amount
          })),
          emphasis: {
            itemStyle: {
              shadowBlur: 10,
              shadowOffsetX: 0,
              shadowColor: 'rgba(0, 0, 0, 0.5)'
            }
          }
        }]
      }

    case 'line':
      return {
        tooltip: {
          trigger: 'axis'
        },
        legend: {
          data: ['费用趋势']
        },
        grid: {
          left: '3%',
          right: '4%',
          bottom: '3%',
          containLabel: true
        },
        xAxis: {
          type: 'category',
          boundaryGap: false,
          data: ['1月', '2月', '3月', '4月', '5月', '6月']
        },
        yAxis: {
          type: 'value'
        },
        series: [{
          name: '费用趋势',
          type: 'line',
          data: [12000, 23200, 30100, 34500, 45200, 54800],
          smooth: true
        }]
      }
  }
}

const handlePageChange = (page: number) => {
  pageParams.current = page
  loadStatisticsData()
}

const handlePageSizeChange = (size: number) => {
  pageParams.size = size
  pageParams.current = 1
  loadStatisticsData()
}

const handleExport = () => {
  // 导出功能实现
  console.log('导出Excel')
}

// 监听图表类型变化
watch(chartType, () => {
  updateChart()
})

// 生命周期
onMounted(() => {
  loadStatisticsData()
  // 监听窗口大小变化，重新渲染图表
  window.addEventListener('resize', () => {
    chartInstance.value?.resize()
  })
})
</script>

<style scoped>
.expense-report {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: calc(100vh - 60px);
}

.page-header {
  margin-bottom: 20px;
}

.page-title {
  margin: 0 0 20px 0;
  font-size: 24px;
  font-weight: bold;
  color: #303133;
}

.filter-card {
  background: #fff;
  padding: 20px;
  border-radius: 4px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.statistics-overview {
  margin-bottom: 20px;
}

.stat-card {
  border: none;
}

.stat-container {
  display: flex;
  align-items: center;
  padding: 10px 0;
}

.stat-icon {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 20px;
  font-size: 24px;
  color: #fff;
}

.expense-icon { background: linear-gradient(135deg, #409EFF 0%, #67C23A 100%); }
.apply-icon { background: linear-gradient(135deg, #E6A23C 0%, #F56C6C 100%); }
.avg-icon { background: linear-gradient(135deg, #909399 0%, #C0C4CC 100%); }
.trend-icon { background: linear-gradient(135deg, #F56C6C 0%, #E6A23C 100%); }

.stat-value {
  font-size: 24px;
  font-weight: bold;
  color: #303133;
  margin-bottom: 5px;
}

.stat-value.positive {
  color: #67C23A;
}

.stat-value.negative {
  color: #F56C6C;
}

.stat-label {
  font-size: 14px;
  color: #909399;
}

.chart-section {
  margin-bottom: 20px;
}

.chart-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.chart-title {
  font-size: 16px;
  font-weight: bold;
}

.data-table-section {
  margin-bottom: 20px;
}

.table-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.table-title {
  font-size: 16px;
  font-weight: bold;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.filter-actions {
  display: flex;
  gap: 10px;
  margin-top: 10px;
}

@media (max-width: 768px) {
  .expense-report {
    padding: 10px;
  }
  
  .stat-container {
    flex-direction: column;
    text-align: center;
  }
  
  .stat-icon {
    margin-right: 0;
    margin-bottom: 10px;
  }
}
</style>