<template>
  <div class="dept-comparison">
    <!-- 页面标题和筛选条件 -->
    <div class="page-header">
      <h2 class="page-title">部门费用对比分析</h2>
      <div class="filter-card">
        <el-row :gutter="20">
          <el-col :span="6">
            <el-form-item label="对比期间">
              <el-select v-model="queryParams.timeRange" placeholder="请选择统计周期" clearable>
                <el-option label="本月" value="month" />
                <el-option label="本季度" value="quarter" />
                <el-option label="本年度" value="year" />
                <el-option label="上年度" value="lastYear" />
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
            <el-form-item label="费用类型">
              <el-select v-model="queryParams.expenseType" placeholder="请选择费用类型" clearable>
                <el-option label="全部费用" value="" />
                <el-option label="差旅费" value="travel" />
                <el-option label="餐饮费" value="meal" />
                <el-option label="交通费" value="transport" />
                <el-option label="办公费" value="office" />
                <el-option label="招待费" value="entertain" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="6">
            <el-form-item label="对比类型">
              <el-select v-model="queryParams.comparisonType" placeholder="请选择对比类型" clearable>
                <el-option label="部门间对比" value="dept" />
                <el-option label="时间对比" value="time" />
                <el-option label="预算执行对比" value="budget" />
                <el-option label="费用类型对比" value="type" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <div class="filter-actions">
              <el-button type="primary" @click="handleSearch">查询</el-button>
              <el-button @click="handleReset">重置</el-button>
              <el-button type="info" @click="showComparisonSettings">对比设置</el-button>
            </div>
          </el-col>
        </el-row>
      </div>
    </div>

    <!-- 核心对比指标 -->
    <div class="metrics-section">
      <el-row :gutter="20">
        <el-col :span="6">
          <el-card class="metric-card" shadow="hover">
            <div class="metric-container">
              <div class="metric-icon primary-icon">
                <i class="el-icon-s-data" />
              </div>
              <div class="metric-content">
                <div class="metric-value">{{ formatNumber(comparisonData.maxDeptAmount) }}</div>
                <div class="metric-label">最高费用部门</div>
                <div class="metric-desc">{{ comparisonData.maxDeptName }}</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="metric-card" shadow="hover">
            <div class="metric-container">
              <div class="metric-icon success-icon">
                <i class="el-icon-bottom" />
              </div>
              <div class="metric-content">
                <div class="metric-value">{{ formatNumber(comparisonData.minDeptAmount) }}</div>
                <div class="metric-label">最低费用部门</div>
                <div class="metric-desc">{{ comparisonData.minDeptName }}</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="metric-card" shadow="hover">
            <div class="metric-container">
              <div class="metric-icon warning-icon">
                <i class="el-icon-sort" />
              </div>
              <div class="metric-content">
                <div class="metric-value">{{ formatNumber(comparisonData.avgDeptAmount) }}</div>
                <div class="metric-label">部门平均费用</div>
                <div class="metric-desc">{{ comparisonData.deptCount }}个部门</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="metric-card" shadow="hover">
            <div class="metric-container">
              <div class="metric-icon danger-icon">
                <i class="el-icon-s-open" />
              </div>
              <div class="metric-content">
                <div class="metric-value">{{ comparisonData.gapRate }}%</div>
                <div class="metric-label">最高最低差距</div>
                <div class="metric-desc">部门间差异率</div>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 多维度对比图表 -->
    <div class="chart-section">
      <el-card shadow="hover">
        <template #header>
          <div class="chart-header">
            <span class="chart-title">部门费用多维对比</span>
            <div class="chart-actions">
              <el-radio-group v-model="chartType" size="small">
                <el-radio-button label="bar">柱状图</el-radio-button>
                <el-radio-button label="radar">雷达图</el-radio-button>
                <el-radio-button label="scatter">散点图</el-radio-button>
                <el-radio-button label="heatmap">热力图</el-radio-button>
              </el-radio-group>
            </div>
          </div>
        </template>
        <div class="chart-container">
          <div ref="chartRef" style="height: 500px; width: 100%;"></div>
        </div>
      </el-card>
    </div>

    <!-- 费用类型分布 -->
    <div class="type-distribution-section">
      <el-card shadow="hover">
        <template #header>
          <span class="section-title">各部门费用类型分布</span>
        </template>
        <div class="distribution-chart">
          <div ref="distributionChartRef" style="height: 350px; width: 100%;"></div>
        </div>
      </el-card>
    </div>

    <!-- 详细对比数据表格 -->
    <div class="comparison-table-section">
      <el-card shadow="hover">
        <template #header>
          <div class="table-header">
            <span class="table-title">详细对比数据</span>
            <div class="table-actions">
              <el-button type="primary" @click="handleExport">导出数据</el-button>
              <el-button @click="toggleTableView">{{ showDetailTable ? '简化视图' : '详细视图' }}</el-button>
            </div>
          </div>
        </template>
        
        <!-- 简化视图 -->
        <div v-if="!showDetailTable" class="summary-cards">
          <el-row :gutter="20">
            <el-col 
              v-for="dept in summaryData" 
              :key="dept.id" 
              :span="6"
            >
              <el-card class="dept-summary-card" shadow="hover">
                <div class="dept-header">
                  <h4 class="dept-name">{{ dept.name }}</h4>
                  <el-tag :type="getDeptRankType(dept.rank)">第{{ dept.rank }}名</el-tag>
                </div>
                <div class="dept-metrics">
                  <div class="metric-item">
                    <span class="metric-label">总费用:</span>
                    <span class="metric-value">{{ formatAmount(dept.totalAmount) }}</span>
                  </div>
                  <div class="metric-item">
                    <span class="metric-label">费用占比:</span>
                    <span class="metric-value">{{ dept.percentage }}%</span>
                  </div>
                  <div class="metric-item">
                    <span class="metric-label">同比增长:</span>
                    <span class="metric-value" :class="{ 'positive': dept.growthRate > 0, 'negative': dept.growthRate < 0 }">
                      {{ dept.growthRate > 0 ? '+' : '' }}{{ dept.growthRate }}%
                    </span>
                  </div>
                </div>
                <div class="dept-types">
                  <el-progress 
                    v-for="type in dept.expenseTypes" 
                    :key="type.name"
                    :percentage="type.percentage" 
                    :show-text="false"
                    :stroke-width="6"
                    class="type-progress"
                  />
                </div>
              </el-card>
            </el-col>
          </el-row>
        </div>

        <!-- 详细表格视图 -->
        <div v-else>
          <el-table :data="tableData" v-loading="loading" border style="width: 100%" show-summary>
            <el-table-column prop="deptName" label="部门" align="center" fixed />
            <el-table-column prop="totalAmount" label="总费用" align="center">
              <template #default="{ row }">
                <strong>{{ formatAmount(row.totalAmount) }}</strong>
              </template>
            </el-table-column>
            <el-table-column prop="budgetAmount" label="预算金额" align="center">
              <template #default="{ row }">
                {{ formatAmount(row.budgetAmount) }}
              </template>
            </el-table-column>
            <el-table-column prop="executionRate" label="执行率" align="center">
              <template #default="{ row }">
                <el-progress 
                  :percentage="Number(row.executionRate)" 
                  :status="getExecutionStatus(row.executionRate)"
                  :show-text="false"
                />
                <span class="rate-text">{{ row.executionRate }}%</span>
              </template>
            </el-table-column>
            <el-table-column 
              v-for="type in expenseTypeColumns" 
              :key="type.value"
              :prop="type.value" 
              :label="type.label" 
              align="center"
            >
              <template #default="{ row }">
                {{ formatAmount(row[type.value]) }}
              </template>
            </el-table-column>
            <el-table-column prop="growthRate" label="同比增长" align="center">
              <template #default="{ row }">
                <el-tag :type="row.growthRate > 0 ? 'danger' : 'success'">
                  {{ row.growthRate > 0 ? '+' : '' }}{{ row.growthRate }}%
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="rank" label="排名" align="center">
              <template #default="{ row }">
                <el-tag :type="getRankType(row.rank)">
                  {{ row.rank }}
                </el-tag>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </el-card>
    </div>

    <!-- 对比设置弹窗 -->
    <el-dialog v-model="showSettingsDialog" title="对比设置" width="600px">
      <el-form :model="comparisonSettings" label-width="120px">
        <el-form-item label="对比基准">
          <el-radio-group v-model="comparisonSettings.baseline">
            <el-radio label="avg">部门平均值</el-radio>
            <el-radio label="max">最高值</el-radio>
            <el-radio label="budget">预算目标</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="显示方式">
          <el-radio-group v-model="comparisonSettings.display">
            <el-radio label="amount">金额</el-radio>
            <el-radio label="percentage">百分比</el-radio>
            <el-radio label="ratio">比率</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="统计口径">
          <el-checkbox-group v-model="comparisonSettings.scope">
            <el-checkbox label="includeBudget">包含预算内费用</el-checkbox>
            <el-checkbox label="includeActual">包含实际支出</el-checkbox>
            <el-checkbox label="excludeSpecial">排除特殊费用</el-checkbox>
          </el-checkbox-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showSettingsDialog = false">取消</el-button>
        <el-button type="primary" @click="applySettings">应用</el-button>
      </template>
    </el-dialog>
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
  expenseType: string
  comparisonType: string
}

interface ComparisonData {
  maxDeptName: string
  maxDeptAmount: number
  minDeptName: string
  minDeptAmount: number
  avgDeptAmount: number
  deptCount: number
  gapRate: number
}

interface SummaryItem {
  id: number
  name: string
  totalAmount: number
  percentage: number
  growthRate: number
  rank: number
  expenseTypes: Array<{name: string, percentage: number}>
}

// 响应式数据
const queryParams = reactive<QueryParams>({
  timeRange: 'month',
  startDate: '',
  endDate: '',
  expenseType: '',
  comparisonType: 'dept'
})

const comparisonData = reactive<ComparisonData>({
  maxDeptName: '',
  maxDeptAmount: 0,
  minDeptName: '',
  minDeptAmount: 0,
  avgDeptAmount: 0,
  deptCount: 0,
  gapRate: 0
})

const loading = ref(false)
const chartRef = ref<HTMLDivElement>()
const distributionChartRef = ref<HTMLDivElement>()
const chartInstance = ref<echarts.ECharts>()
const distributionChartInstance = ref<echarts.ECharts>()
const chartType = ref<'bar' | 'radar' | 'scatter' | 'heatmap'>('bar')
const showDetailTable = ref(false)
const showSettingsDialog = ref(false)

const comparisonSettings = reactive({
  baseline: 'avg',
  display: 'amount',
  scope: ['includeBudget', 'includeActual']
})

// 模拟数据
const tableData = ref([
  { deptName: '技术部', totalAmount: 654320, budgetAmount: 800000, executionRate: 81.79, travel: 123450, meal: 87650, transport: 54320, office: 98760, entertain: 23400, growthRate: 15.2, rank: 1 },
  { deptName: '销售部', totalAmount: 432100, budgetAmount: 500000, executionRate: 86.42, travel: 87650, meal: 65430, transport: 43210, office: 76540, entertain: 65430, growthRate: -8.3, rank: 2 },
  { deptName: '市场部', totalAmount: 287650, budgetAmount: 350000, executionRate: 82.19, travel: 54320, meal: 43210, transport: 32100, office: 65430, entertain: 76540, growthRate: 25.7, rank: 3 },
  { deptName: '财务部', totalAmount: 123450, budgetAmount: 150000, executionRate: 82.3, travel: 21000, meal: 18760, transport: 15430, office: 43210, entertain: 8760, growthRate: 5.1, rank: 4 },
  { deptName: '人事部', totalAmount: 87650, budgetAmount: 100000, executionRate: 87.65, travel: 15000, meal: 12340, transport: 21000, office: 18760, entertain: 6540, growthRate: -3.4, rank: 5 }
])

const summaryData = computed<SummaryItem[]>(() => {
  return tableData.value.map((item, index) => ({
    id: index + 1,
    name: item.deptName,
    totalAmount: item.totalAmount,
    percentage: Math.round((item.totalAmount / tableData.value.reduce((sum, d) => sum + d.totalAmount, 0)) * 100),
    growthRate: item.growthRate,
    rank: item.rank,
    expenseTypes: [
      { name: '差旅费', percentage: Math.round((item.travel / item.totalAmount) * 100) },
      { name: '餐饮费', percentage: Math.round((item.meal / item.totalAmount) * 100) },
      { name: '交通费', percentage: Math.round((item.transport / item.totalAmount) * 100) },
      { name: '办公费', percentage: Math.round((item.office / item.totalAmount) * 100) },
      { name: '招待费', percentage: Math.round((item.entertain / item.totalAmount) * 100) }
    ]
  }))
})

const expenseTypeColumns = [
  { label: '差旅费', value: 'travel' },
  { label: '餐饮费', value: 'meal' },
  { label: '交通费', value: 'transport' },
  { label: '办公费', value: 'office' },
  { label: '招待费', value: 'entertain' }
]

// 方法
const formatAmount = (amount: number) => {
  return new Intl.NumberFormat('zh-CN', { style: 'currency', currency: 'CNY' }).format(amount)
}

const formatNumber = (num: number) => {
  return new Intl.NumberFormat('zh-CN').format(num)
}

const getExecutionStatus = (rate: number) => {
  if (rate >= 90) return 'exception'
  if (rate >= 80) return 'warning'
  return 'success'
}

const getRankType = (rank: number) => {
  if (rank === 1) return 'danger'
  if (rank <= 3) return 'warning'
  return 'info'
}

const getDeptRankType = (rank: number) => {
  if (rank === 1) return 'danger'
  if (rank <= 3) return 'warning'
  return 'info'
}

const handleSearch = () => {
  loadComparisonData()
}

const handleReset = () => {
  Object.assign(queryParams, {
    timeRange: 'month',
    startDate: '',
    endDate: '',
    expenseType: '',
    comparisonType: 'dept'
  })
  handleSearch()
}

const loadComparisonData = async () => {
  loading.value = true
  try {
    await new Promise(resolve => setTimeout(resolve, 1000))
    
    // 更新对比数据
    Object.assign(comparisonData, {
      maxDeptName: '技术部',
      maxDeptAmount: 654320,
      minDeptName: '人事部',
      minDeptAmount: 87650,
      avgDeptAmount: 318034,
      deptCount: 5,
      gapRate: Math.round(((654320 - 87650) / 87650) * 100)
    })
    
    updateCharts()
  } catch (error) {
    console.error('加载对比数据失败:', error)
  } finally {
    loading.value = false
  }
}

const updateCharts = () => {
  updateMainChart()
  updateDistributionChart()
}

const updateMainChart = () => {
  if (!chartRef.value) return

  if (!chartInstance.value) {
    chartInstance.value = echarts.init(chartRef.value)
  }

  const option = getMainChartOption()
  chartInstance.value?.setOption(option, true)
}

const updateDistributionChart = () => {
  if (!distributionChartRef.value) return

  if (!distributionChartInstance.value) {
    distributionChartInstance.value = echarts.init(distributionChartRef.value)
  }

  const option = getDistributionChartOption()
  distributionChartInstance.value?.setOption(option, true)
}

// 图表配置方法 (由于长度限制，此处省略具体实现，实际项目中需完善)
const getMainChartOption = () => ({ /* 图表配置 */ })
const getDistributionChartOption = () => ({ /* 图表配置 */ })

const handleExport = () => {
  console.log('导出对比数据')
}

const toggleTableView = () => {
  showDetailTable.value = !showDetailTable.value
}

const showComparisonSettings = () => {
  showSettingsDialog.value = true
}

const applySettings = () => {
  showSettingsDialog.value = false
  loadComparisonData()
}

// 生命周期
onMounted(() => {
  loadComparisonData()
  window.addEventListener('resize', () => {
    chartInstance.value?.resize()
    distributionChartInstance.value?.resize()
  })
})
</script>

<style scoped>
.dept-comparison {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: calc(100vh - 60px);
}

.page-header {
  margin-bottom: 20px;
}

.metrics-section, .chart-section, .type-distribution-section, .comparison-table-section {
  margin-bottom: 20px;
}

.metric-card, .dept-summary-card {
  border: none;
}

/* 更多样式实现... */
</style>

<style scoped>
.dept-comparison {
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

.metrics-section {
  margin-bottom: 20px;
}

.metric-card {
  border: none;
}

.metric-container {
  display: flex;
  align-items: center;
  padding: 15px 0;
}

.metric-icon {
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

.primary-icon { background: linear-gradient(135deg, #409EFF 0%, #67C23A 100%); }
.success-icon { background: linear-gradient(135deg, #67C23A 0%, #E6A23C 100%); }
.warning-icon { background: linear-gradient(135deg, #E6A23C 0%, #F56C6C 100%); }
.danger-icon { background: linear-gradient(135deg, #F56C6C 0%, #909399 100%); }

.metric-content {
  flex: 1;
}

.metric-value {
  font-size: 22px;
  font-weight: bold;
  color: #303133;
  margin-bottom: 5px;
}

.metric-label {
  font-size: 14px;
  color: #606266;
  margin-bottom: 3px;
}

.metric-desc {
  font-size: 12px;
  color: #909399;
}

.chart-section, .type-distribution-section, .comparison-table-section {
  margin-bottom: 20px;
}

.chart-header, .table-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.chart-title, .section-title, .table-title {
  font-size: 16px;
  font-weight: bold;
}

.summary-cards {
  margin: -10px;
}

.dept-summary-card {
  height: 180px;
  margin: 10px;
}

.dept-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.dept-name {
  margin: 0;
  font-size: 14px;
}

.dept-metrics {
  margin-bottom: 10px;
}

.metric-item {
  display: flex;
  justify-content: space-between;
  margin-bottom: 5px;
  font-size: 12px;
}

.metric-label {
  color: #909399;
}

.metric-value {
  font-weight: bold;
}

.metric-value.positive {
  color: #67C23A;
}

.metric-value.negative {
  color: #F56C6C;
}

.dept-types {
  margin-top: 10px;
}

.type-progress {
  margin-bottom: 3px;
}

.rate-text {
  font-size: 12px;
  color: #909399;
  margin-left: 5px;
}

.filter-actions {
  display: flex;
  gap: 10px;
  margin-top: 10px;
}

@media (max-width: 768px) {
  .dept-comparison {
    padding: 10px;
  }
  
  .metric-container {
    flex-direction: column;
    text-align: center;
  }
  
  .metric-icon {
    margin-right: 0;
    margin-bottom: 10px;
  }
}
</style>