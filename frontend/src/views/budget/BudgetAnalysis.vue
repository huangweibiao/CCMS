<template>
  <div class="budget-analysis-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2>预算执行分析</h2>
      <p>分析预算执行情况，监控预算使用状况</p>
    </div>

    <div class="budget-analysis-content">
      <!-- 筛选条件行 -->
      <div class="filter-row">
        <div class="filter-group">
          <el-select v-model="filterParams.budgetYear" placeholder="预算年度" style="width: 120px">
            <el-option label="2024年" value="2024" />
            <el-option label="2023年" value="2023" />
            <el-option label="2022年" value="2022" />
          </el-select>
          
          <el-select v-model="filterParams.departmentId" placeholder="选择部门" style="width: 150px">
            <el-option label="全部部门" value="" />
            <el-option v-for="dept in departmentList" :key="dept.id" :label="dept.deptName" :value="dept.id" />
          </el-select>
          
          <el-select v-model="filterParams.budgetType" placeholder="预算类型" style="width: 120px">
            <el-option label="全部类型" value="" />
            <el-option label="年度预算" value="ANNUAL" />
            <el-option label="季度预算" value="QUARTERLY" />
            <el-option label="月度预算" value="MONTHLY" />
          </el-select>
          
          <el-button type="primary" @click="handleFilter">
            <el-icon><search /></el-icon>
            查询
          </el-button>
          
          <el-button @click="handleReset">
            <el-icon><refresh /></el-icon>
            重置
          </el-button>
        </div>
      </div>

      <!-- 总体概览卡片 -->
      <div class="overview-cards">
        <el-row :gutter="20">
          <el-col :xs="24" :sm="6" :md="6">
            <div class="stat-card">
              <div class="stat-info">
                <div class="stat-value" style="color: #409eff;">¥{{ formatAmount(overviewData.totalBudget) }}</div>
                <div class="stat-label">预算总额</div>
              </div>
              <el-icon class="stat-icon"><trend-charts /></el-icon>
            </div>
          </el-col>
          
          <el-col :xs="24" :sm="6" :md="6">
            <div class="stat-card">
              <div class="stat-info">
                <div class="stat-value" style="color: #67c23a;">¥{{ formatAmount(overviewData.usedAmount) }}</div>
                <div class="stat-label">已使用金额</div>
              </div>
              <el-icon class="stat-icon"><money /></el-icon>
            </div>
          </el-col>
          
          <el-col :xs="24" :sm="6" :md="6">
            <div class="stat-card">
              <div class="stat-info">
                <div class="stat-value" style="color: #e6a23c;">¥{{ formatAmount(overviewData.remainingAmount) }}</div>
                <div class="stat-label">剩余额度</div>
              </div>
              <el-icon class="stat-icon"><coin /></el-icon>
            </div>
          </el-col>
          
          <el-col :xs="24" :sm="6" :md="6">
            <div class="stat-card">
              <div class="stat-info">
                <div class="stat-value" :style="{ color: getUsageRateColor(overviewData.usageRate) }">
                  {{ overviewData.usageRate?.toFixed(1) }}%
                </div>
                <div class="stat-label">预算使用率</div>
              </div>
              <el-icon class="stat-icon"><data-analysis /></el-icon>
            </div>
          </el-col>
        </el-row>
      </div>

      <!-- 图表分析区 -->
      <div class="chart-section">
        <el-row :gutter="20">
          <el-col :xs="24" :sm="12" :md="12">
            <div class="chart-card">
              <h3 class="chart-title">预算使用情况</h3>
              <div class="chart-container">
                <div ref="pieChartRef" style="height: 300px; width: 100%;"></div>
              </div>
            </div>
          </el-col>
          
          <el-col :xs="24" :sm="12" :md="12">
            <div class="chart-card">
              <h3 class="chart-title">月度支出趋势</h3>
              <div class="chart-container">
                <div ref="lineChartRef" style="height: 300px; width: 100%;"></div>
              </div>
            </div>
          </el-col>
        </el-row>
        
        <el-row :gutter="20" style="margin-top: 20px;">
          <el-col :xs="24" :sm="24" :md="24">
            <div class="chart-card">
              <h3 class="chart-title">部门预算执行对比</h3>
              <div class="chart-container">
                <div ref="barChartRef" style="height: 350px; width: 100%;"></div>
              </div>
            </div>
          </el-col>
        </el-row>
      </div>

      <!-- 详细分析表格 -->
      <div class="analysis-table">
        <div class="table-header">
          <h3>预算明细分析</h3>
          <div class="table-actions">
            <el-button type="primary" size="small">
              <el-icon><download /></el-icon>
              导出报表
            </el-button>
          </div>
        </div>
        
        <DataTable
          :columns="analysisColumns"
          :data="analysisData"
          :loading="loading"
          :pagination="pagination"
          @pagination-change="handlePageChange"
        >
          <!-- 使用率列 -->
          <template #usageRate="{ row }">
            <div class="usage-rate-container">
              <el-progress 
                :percentage="Math.round(row.usageRate || 0)" 
                :show-text="false" 
                :stroke-width="8"
                :color="getProgressColor(row.usageRate)"
              />
              <span class="rate-text">{{ Math.round(row.usageRate || 0) }}%</span>
            </div>
          </template>
          
          <!-- 操作列 -->
          <template #actions="{ row }">
            <el-button type="primary" link size="small" @click="handleViewDetail(row)">
              查看详情
            </el-button>
            <el-button type="success" link size="small" @click="handleExportDetail(row)">
              导出报告
            </el-button>
          </template>
        </DataTable>
      </div>

      <!-- 预警信息 -->
      <div class="alert-section" v-if="alertsData.length > 0">
        <h3>预算预警信息</h3>
        <div class="alert-list">
          <div v-for="alert in alertsData" :key="alert.id" class="alert-item" :class="alert.level">
            <div class="alert-icon">
              <el-icon v-if="alert.level === 'critical'"><warning-filled /></el-icon>
              <el-icon v-else-if="alert.level === 'warning'"><warning /></el-icon>
              <el-icon v-else><info-filled /></el-icon>
            </div>
            <div class="alert-content">
              <div class="alert-title">{{ alert.title }}</div>
              <div class="alert-desc">{{ alert.description }}</div>
            </div>
            <div class="alert-time">{{ alert.time }}</div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, onUnmounted, nextTick } from 'vue'
import { Search, Refresh, Download, TrendCharts, Money, Coin, DataAnalysis, WarningFilled, Warning, InfoFilled } from '@element-plus/icons-vue'
import * as echarts from 'echarts'
import DataTable from '@/components/common/DataTable.vue'
import { showSuccess } from '@/components/common/Message.vue'

// ECharts实例引用
const pieChartRef = ref<HTMLElement>()
const lineChartRef = ref<HTMLElement>()
const barChartRef = ref<HTMLElement>()
let pieChart: echarts.ECharts
let lineChart: echarts.ECharts
let barChart: echarts.ECharts

// 响应式数据
const loading = ref(false)

// 筛选参数
const filterParams = reactive({
  budgetYear: '2024',
  departmentId: '',
  budgetType: ''
})

// 部门列表
const departmentList = ref([
  { id: '1', deptName: '技术部' },
  { id: '2', deptName: '财务部' },
  { id: '3', deptName: '人事部' },
  { id: '4', deptName: '市场部' },
  { id: '5', deptName: '行政部' }
])

// 概览数据
const overviewData = reactive({
  totalBudget: 1500000,
  usedAmount: 850000,
  remainingAmount: 650000,
  usageRate: 56.7
})

// 表格数据
const analysisData = ref([
  {
    id: '1',
    budgetCode: 'BGT202401001',
    budgetYear: '2024',
    departmentName: '技术部',
    totalAmount: 500000,
    usedAmount: 250000,
    usageRate: 50,
    status: 'EXECUTING'
  },
  {
    id: '2',
    budgetCode: 'BGT202401002',
    budgetYear: '2024',
    departmentName: '市场部',
    totalAmount: 300000,
    usedAmount: 240000,
    usageRate: 80,
    status: 'EXECUTING'
  },
  {
    id: '3',
    budgetCode: 'BGT202401003',
    budgetYear: '2024',
    departmentName: '行政部',
    totalAmount: 200000,
    usedAmount: 100000,
    usageRate: 50,
    status: 'EXECUTING'
  },
  {
    id: '4',
    budgetCode: 'BGT202401004',
    budgetYear: '2024',
    departmentName: '财务部',
    totalAmount: 400000,
    usedAmount: 200000,
    usageRate: 50,
    status: 'EXECUTING'
  },
  {
    id: '5',
    budgetCode: 'BGT202401005',
    budgetYear: '2024',
    departmentName: '人事部',
    totalAmount: 100000,
    usedAmount: 60000,
    usageRate: 60,
    status: 'EXECUTING'
  }
])

// 预警数据
const alertsData = ref([
  {
    id: '1',
    title: '预算使用率过高',
    description: '市场部预算使用率达到80%，请注意控制支出',
    level: 'warning',
    time: '2024-01-15 14:30:25'
  },
  {
    id: '2',
    title: '预算即将用尽',
    description: '技术部第一季度预算剩余额度不足20%',
    level: 'critical',
    time: '2024-01-14 10:15:18'
  }
])

// 表格列配置
const analysisColumns = [
  { prop: 'budgetCode', label: '预算编号', width: 120 },
  { prop: 'budgetYear', label: '预算年度', width: 100 },
  { prop: 'departmentName', label: '责任部门', width: 120 },
  { prop: 'totalAmount', label: '预算总额', width: 120, sortable: true },
  { prop: 'usedAmount', label: '已使用金额', width: 120, sortable: true },
  { prop: 'usageRate', label: '使用率', width: 150, slot: 'usageRate', sortable: true },
  { prop: 'status', label: '状态', width: 100, slot: 'status' },
  { prop: 'actions', label: '操作', width: 150, slot: 'actions' }
]

// 分页配置
const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: analysisData.value.length
})

// 生命周期
onMounted(() => {
  initCharts()
  loadAnalysisData()
})

onUnmounted(() => {
  // 清理图表实例
  if (pieChart) pieChart.dispose()
  if (lineChart) lineChart.dispose()
  if (barChart) barChart.dispose()
})

// 方法
const initCharts = () => {
  nextTick(() => {
    // 饼图 - 预算使用情况
    if (pieChartRef.value) {
      pieChart = echarts.init(pieChartRef.value)
      const pieOption = {
        tooltip: { trigger: 'item' },
        legend: { orient: 'vertical', right: 10, top: 'center' },
        series: [{
          name: '预算使用情况',
          type: 'pie',
          radius: ['40%', '70%'],
          center: ['40%', '50%'],
          data: [
            { value: overviewData.usedAmount, name: '已使用' },
            { value: overviewData.remainingAmount, name: '剩余额度' }
          ],
          emphasis: { itemStyle: { shadowBlur: 10, shadowOffsetX: 0, shadowColor: 'rgba(0, 0, 0, 0.5)' } }
        }]
      }
      pieChart.setOption(pieOption)
    }

    // 折线图 - 月度支出趋势
    if (lineChartRef.value) {
      lineChart = echarts.init(lineChartRef.value)
      const lineOption = {
        tooltip: { trigger: 'axis' },
        legend: { data: ['支出金额', '预算金额'] },
        xAxis: { type: 'category', data: ['1月', '2月', '3月', '4月', '5月', '6月', '7月', '8月', '9月', '10月', '11月', '12月'] },
        yAxis: { type: 'value' },
        series: [
          { name: '支出金额', type: 'line', data: [120000, 80000, 90000, 110000, 95000, 85000, 100000, 105000, 98000, 92000, 88000, 85000] },
          { name: '预算金额', type: 'line', data: [125000, 125000, 125000, 125000, 125000, 125000, 125000, 125000, 125000, 125000, 125000, 125000] }
        ]
      }
      lineChart.setOption(lineOption)
    }

    // 柱状图 - 部门预算执行对比
    if (barChartRef.value) {
      barChart = echarts.init(barChartRef.value)
      const barOption = {
        tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
        legend: { data: ['预算总额', '已使用', '剩余额度'] },
        xAxis: { type: 'category', data: ['技术部', '市场部', '行政部', '财务部', '人事部'] },
        yAxis: { type: 'value' },
        series: [
          { name: '预算总额', type: 'bar', data: [500000, 300000, 200000, 400000, 100000] },
          { name: '已使用', type: 'bar', data: [250000, 240000, 100000, 200000, 60000] },
          { name: '剩余额度', type: 'bar', data: [250000, 60000, 100000, 200000, 40000] }
        ]
      }
      barChart.setOption(barOption)
    }
  })
}

const loadAnalysisData = async () => {
  loading.value = true
  try {
    // TODO: 调用API获取分析数据
    // 使用模拟数据
    await new Promise(resolve => setTimeout(resolve, 500))
  } catch (error) {
    console.error('加载分析数据失败:', error)
  } finally {
    loading.value = false
  }
}

const handleFilter = () => {
  loadAnalysisData()
  // 重新渲染图表
  initCharts()
}

const handleReset = () => {
  Object.assign(filterParams, {
    budgetYear: '2024',
    departmentId: '',
    budgetType: ''
  })
  loadAnalysisData()
  initCharts()
}

const handlePageChange = (page: number, pageSize: number) => {
  pagination.current = page
  pagination.pageSize = pageSize
  loadAnalysisData()
}

const handleViewDetail = (row: any) => {
  showSuccess(`查看预算详情: ${row.budgetCode}`)
}

const handleExportDetail = (row: any) => {
  showSuccess(`导出预算报告: ${row.budgetCode}`)
}

// 工具函数
const formatAmount = (amount: number) => {
  return amount?.toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 }) || '0.00'
}

const getUsageRateColor = (rate: number) => {
  if (rate >= 80) return '#f56c6c'
  if (rate >= 60) return '#e6a23c'
  return '#67c23a'
}

const getProgressColor = (rate: number) => {
  if (rate >= 80) return '#f56c6c'
  if (rate >= 60) return '#e6a23c'
  return '#409eff'
}
</script>

<style scoped lang="css">
.budget-analysis-container {
  padding: 20px;
  background: #f5f7fa;
  min-height: calc(100vh - 60px);
}

.page-header {
  margin-bottom: 20px;
}

.page-header h2 {
  margin: 0 0 8px 0;
  color: #303133;
  font-size: 20px;
}

.page-header p {
  margin: 0;
  color: #909399;
  font-size: 14px;
}

.budget-analysis-content {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.filter-row {
  background: white;
  border-radius: 8px;
  padding: 16px 20px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.filter-group {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.overview-cards {
  margin-bottom: 0;
}

.stat-card {
  background: white;
  border-radius: 8px;
  padding: 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  transition: transform 0.3s;
}

.stat-card:hover {
  transform: translateY(-2px);
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 24px;
  font-weight: 600;
  margin-bottom: 8px;
}

.stat-label {
  color: #909399;
  font-size: 14px;
}

.stat-icon {
  font-size: 40px;
  opacity: 0.2;
}

.chart-section {
  margin-top: 0;
}

.chart-card {
  background: white;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.chart-title {
  margin: 0 0 20px 0;
  color: #303133;
  font-size: 16px;
}

.chart-container {
  width: 100%;
}

.analysis-table {
  background: white;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.table-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 20px 0 20px;
}

.table-header h3 {
  margin: 0;
  color: #303133;
  font-size: 16px;
}

.table-actions {
  display: flex;
  gap: 10px;
}

.usage-rate-container {
  display: flex;
  align-items: center;
  gap: 12px;
}

.rate-text {
  min-width: 40px;
  text-align: right;
  font-size: 12px;
}

.alert-section {
  background: white;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.alert-section h3 {
  margin: 0 0 16px 0;
  color: #303133;
  font-size: 16px;
}

.alert-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.alert-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  border-radius: 4px;
  border-left: 4px solid #ccc;
}

.alert-item.critical {
  background: #fef0f0;
  border-left-color: #f56c6c;
  color: #f56c6c;
}

.alert-item.warning {
  background: #fdf6ec;
  border-left-color: #e6a23c;
  color: #e6a23c;
}

.alert-item.info {
  background: #f4f4f5;
  border-left-color: #909399;
  color: #606266;
}

.alert-icon {
  font-size: 20px;
}

.alert-content {
  flex: 1;
}

.alert-title {
  font-weight: 500;
  margin-bottom: 4px;
}

.alert-desc {
  font-size: 12px;
  opacity: 0.8;
}

.alert-time {
  font-size: 12px;
  color: #909399;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .budget-analysis-container {
    padding: 10px;
  }
  
  .filter-group {
    flex-direction: column;
    align-items: stretch;
  }
  
  .stat-card {
    margin-bottom: 10px;
  }
  
  .table-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }
  
  .alert-item {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }
  
  .alert-time {
    align-self: flex-end;
  }
}
</style>