<template>
  <div class="budget-analysis">
    <!-- 页面标题和筛选条件 -->
    <div class="page-header">
      <h2 class="page-title">预算执行分析</h2>
      <div class="filter-card">
        <el-row :gutter="20">
          <el-col :span="6">
            <el-form-item label="预算年份">
              <el-date-picker
                v-model="queryParams.year"
                type="year"
                placeholder="选择年份"
                value-format="YYYY"
                clearable
              />
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="预算期间">
              <el-select v-model="queryParams.period" placeholder="请选择期间" clearable>
                <el-option label="年度" value="year" />
                <el-option label="季度" value="quarter" />
                <el-option label="月度" value="month" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="部门">
              <el-select v-model="queryParams.deptId" placeholder="请选择部门" clearable filterable>
                <el-option v-for="dept in deptList" :key="dept.id" :label="dept.name" :value="dept.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="预算类型">
              <el-select v-model="queryParams.budgetType" placeholder="请选择预算类型" clearable>
                <el-option label="全部" value="" />
                <el-option label="部门预算" value="dept" />
                <el-option label="项目预算" value="project" />
                <el-option label="专项预算" value="special" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <div class="filter-actions">
              <el-button type="primary" @click="handleSearch">查询</el-button>
              <el-button @click="handleReset">重置</el-button>
            </div>
          </el-col>
        </el-row>
      </div>
    </div>

    <!-- 预算执行概况 -->
    <div class="budget-overview">
      <el-row :gutter="20">
        <el-col :span="8">
          <el-card class="budget-card" shadow="hover">
            <div class="budget-container">
              <div class="budget-icon plan-icon">
                <i class="el-icon-document" />
              </div>
              <div class="budget-content">
                <div class="budget-value">{{ formatAmount(planAmount) }}</div>
                <div class="budget-label">预算总额</div>
                <div class="budget-desc">较上年增长 {{ planGrowthRate }}%</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="8">
          <el-card class="budget-card" shadow="hover">
            <div class="budget-container">
              <div class="budget-icon execute-icon">
                <i class="el-icon-coin" />
              </div>
              <div class="budget-content">
                <div class="budget-value">{{ formatAmount(executeAmount) }}</div>
                <div class="budget-label">执行金额</div>
                <div class="budget-desc">执行率 {{ executionRate }}%</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="8">
          <el-card class="budget-card" shadow="hover">
            <div class="budget-container">
              <div class="budget-icon balance-icon">
                <i class="el-icon-refresh" />
              </div>
              <div class="budget-content">
                <div class="budget-value">{{ formatAmount(balanceAmount) }}</div>
                <div class="budget-label">预算余额</div>
                <div class="budget-desc">剩余比例 {{ balanceRate }}%</div>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 预算执行趋势图表 -->
    <div class="chart-section">
      <el-card shadow="hover">
        <template #header>
          <div class="chart-header">
            <span class="chart-title">预算执行趋势分析</span>
            <div class="chart-actions">
              <el-button-group>
                <el-button :type="chartType === 'line' ? 'primary' : ''" @click="chartType = 'line'">趋势图</el-button>
                <el-button :type="chartType === 'bar' ? 'primary' : ''" @click="chartType = 'bar'">对比图</el-button>
                <el-button :type="chartType === 'gauge' ? 'primary' : ''" @click="chartType = 'gauge'">执行率</el-button>
              </el-button-group>
            </div>
          </div>
        </template>
        <div class="chart-container">
          <div ref="chartRef" style="height: 400px; width: 100%;"></div>
        </div>
      </el-card>
    </div>

    <!-- 预算执行情况表格 -->
    <div class="data-table-section">
      <el-card shadow="hover">
        <template #header>
          <div class="table-header">
            <span class="table-title">预算执行明细</span>
            <div class="table-actions">
              <el-button type="primary" @click="handleExport">导出报表</el-button>
            </div>
          </div>
        </template>
        <el-table :data="budgetData" v-loading="loading" border style="width: 100%">
          <el-table-column prop="deptName" label="部门" align="center" />
          <el-table-column prop="budgetCode" label="预算编码" align="center" />
          <el-table-column prop="budgetName" label="预算名称" align="center" />
          <el-table-column prop="planAmount" label="预算金额" align="center">
            <template #default="{ row }">
              {{ formatAmount(row.planAmount) }}
            </template>
          </el-table-column>
          <el-table-column prop="executeAmount" label="执行金额" align="center">
            <template #default="{ row }">
              {{ formatAmount(row.executeAmount) }}
            </template>
          </el-table-column>
          <el-table-column prop="balanceAmount" label="预算余额" align="center">
            <template #default="{ row }">
              {{ formatAmount(row.balanceAmount) }}
            </template>
          </el-table-column>
          <el-table-column prop="executionRate" label="执行率" align="center">
            <template #default="{ row }">
              <el-progress 
                :percentage="Number(row.executionRate)" 
                :status="getExecutionStatus(row.executionRate)"
                :stroke-width="8"
              />
            </template>
          </el-table-column>
          <el-table-column prop="status" label="状态" align="center">
            <template #default="{ row }">
              <el-tag :type="getStatusTagType(row.status)">
                {{ getStatusText(row.status) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" align="center" width="120">
            <template #default="{ row }">
              <el-button type="primary" size="small" @click="handleDetail(row)">详情</el-button>
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

    <!-- 预警信息 -->
    <div class="alert-section">
      <el-alert
        v-if="warningCount > 0"
        title="预算执行预警"
        :description="`当前有 ${warningCount} 个预算项目执行率超过预警阈值`"
        type="warning"
        show-icon
        :closable="false"
      >
        <template #action>
          <el-button type="warning" size="small" @click="viewWarnings">查看详情</el-button>
        </template>
      </el-alert>
    </div>

    <!-- 部门执行率排行 -->
    <div class="ranking-section">
      <el-card shadow="hover">
        <template #header>
          <div class="ranking-header">
            <span class="ranking-title">部门预算执行率排行</span>
          </div>
        </template>
        <div class="ranking-list">
          <div 
            v-for="(item, index) in rankingData" 
            :key="item.deptId"
            class="ranking-item"
            :class="{ 'top-three': index < 3 }"
          >
            <div class="ranking-index">
              <span class="rank-number" :class="getRankClass(index)">{{ index + 1 }}</span>
            </div>
            <div class="ranking-info">
              <div class="dept-name">{{ item.deptName }}</div>
              <div class="budget-info">
                <span class="budget-amount">{{ formatAmount(item.planAmount) }}</span>
                <span class="execute-rate">执行率 {{ item.executionRate }}%</span>
              </div>
            </div>
            <div class="ranking-progress">
              <el-progress 
                :percentage="Number(item.executionRate)" 
                :show-text="false"
                :stroke-width="8"
              />
            </div>
          </div>
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
  year: string
  period: string
  deptId: number
  budgetType: string
}

interface BudgetData {
  deptId: number
  deptName: string
  budgetCode: string
  budgetName: string
  planAmount: number
  executeAmount: number
  balanceAmount: number
  executionRate: number
  status: string
}

interface RankingData {
  deptId: number
  deptName: string
  planAmount: number
  executionRate: number
}

// 响应式数据
const queryParams = reactive<QueryParams>({
  year: new Date().getFullYear().toString(),
  period: 'month',
  deptId: 0,
  budgetType: ''
})

const pageParams = reactive({
  current: 1,
  size: 10,
  total: 0
})

const loading = ref(false)
const chartRef = ref<HTMLDivElement>()
const chartInstance = ref<echarts.ECharts>()
const chartType = ref<'line' | 'bar' | 'gauge'>('line')

const budgetData = ref<BudgetData[]>([])
const rankingData = ref<RankingData[]>([])

// 模拟数据
const deptList = ref([
  { id: 1, name: '技术部' },
  { id: 2, name: '销售部' },
  { id: 3, name: '市场部' },
  { id: 4, name: '财务部' },
  { id: 5, name: '人事部' }
])

// 计算属性
const planAmount = computed(() => budgetData.value.reduce((sum, item) => sum + item.planAmount, 0))
const executeAmount = computed(() => budgetData.value.reduce((sum, item) => sum + item.executeAmount, 0))
const balanceAmount = computed(() => planAmount.value - executeAmount.value)
const executionRate = computed(() => planAmount.value > 0 ? Math.round((executeAmount.value / planAmount.value) * 100) : 0)
const planGrowthRate = computed(() => 8.5) // 模拟数据
const balanceRate = computed(() => 100 - executionRate.value)
const warningCount = computed(() => budgetData.value.filter(item => Number(item.executionRate) > 90).length)

// 方法
const formatAmount = (amount: number) => {
  return new Intl.NumberFormat('zh-CN', { style: 'currency', currency: 'CNY' }).format(amount)
}

const getExecutionStatus = (rate: number) => {
  if (rate >= 90) return 'exception'
  if (rate >= 70) return 'warning'
  return 'success'
}

const getStatusTagType = (status: string) => {
  const statusMap: Record<string, string> = {
    'normal': 'success',
    'warning': 'warning',
    'over': 'danger',
    'complete': 'info'
  }
  return statusMap[status] || 'info'
}

const getStatusText = (status: string) => {
  const statusTextMap: Record<string, string> = {
    'normal': '正常',
    'warning': '预警',
    'over': '超支',
    'complete': '完成'
  }
  return statusTextMap[status] || '未知'
}

const getRankClass = (index: number) => {
  return index < 3 ? `rank-${index + 1}` : 'rank-normal'
}

const handleSearch = () => {
  pageParams.current = 1
  loadBudgetData()
}

const handleReset = () => {
  Object.assign(queryParams, {
    year: new Date().getFullYear().toString(),
    period: 'month',
    deptId: 0,
    budgetType: ''
  })
  handleSearch()
}

const loadBudgetData = async () => {
  loading.value = true
  try {
    // 模拟API调用
    await new Promise(resolve => setTimeout(resolve, 1000))
    
    // 模拟数据
    budgetData.value = [
      { deptId: 1, deptName: '技术部', budgetCode: 'TEC2024', budgetName: '技术研发预算', planAmount: 500000, executeAmount: 320000, balanceAmount: 180000, executionRate: 64, status: 'normal' },
      { deptId: 2, deptName: '销售部', budgetCode: 'SALES2024', budgetName: '市场推广预算', planAmount: 300000, executeAmount: 285000, balanceAmount: 15000, executionRate: 95, status: 'warning' },
      { deptId: 3, deptName: '市场部', budgetCode: 'MKT2024', budgetName: '品牌建设预算', planAmount: 200000, executeAmount: 120000, balanceAmount: 80000, executionRate: 60, status: 'normal' },
      { deptId: 4, deptName: '财务部', budgetCode: 'FIN2024', budgetName: '财务管理预算', planAmount: 150000, executeAmount: 90000, balanceAmount: 60000, executionRate: 60, status: 'normal' },
      { deptId: 5, deptName: '人事部', budgetCode: 'HR2024', budgetName: '人力发展预算', planAmount: 100000, executeAmount: 95000, balanceAmount: 5000, executionRate: 95, status: 'warning' }
    ]
    
    // 排行数据
    rankingData.value = budgetData.value.map(item => ({
      deptId: item.deptId,
      deptName: item.deptName,
      planAmount: item.planAmount,
      executionRate: item.executionRate
    })).sort((a, b) => b.executionRate - a.executionRate)
    
    pageParams.total = budgetData.value.length
    
    // 更新图表
    updateChart()
  } catch (error) {
    console.error('加载预算数据失败:', error)
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
  const months = ['1月', '2月', '3月', '4月', '5月', '6月', '7月', '8月', '9月', '10月', '11月', '12月']
  const planAmounts = [40000, 50000, 60000, 60000, 80000, 85000, 75000, 80000, 90000, 85000, 80000, 75000]
  const executeAmounts = [35000, 45000, 55000, 58000, 72000, 78000, 68000, 75000, 82000, 80000, 75000, 70000]

  switch (chartType.value) {
    case 'line':
      return {
        tooltip: {
          trigger: 'axis'
        },
        legend: {
          data: ['预算金额', '执行金额']
        },
        grid: {
          left: '3%',
          right: '4%',
          bottom: '3%',
          containLabel: true
        },
        xAxis: {
          type: 'category',
          data: months
        },
        yAxis: {
          type: 'value'
        },
        series: [
          {
            name: '预算金额',
            type: 'line',
            data: planAmounts,
            smooth: true,
            lineStyle: { color: '#409EFF' },
            areaStyle: {
              color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                { offset: 0, color: 'rgba(64, 158, 255, 0.3)' },
                { offset: 1, color: 'rgba(64, 158, 255, 0.1)' }
              ])
            }
          },
          {
            name: '执行金额',
            type: 'line',
            data: executeAmounts,
            smooth: true,
            lineStyle: { color: '#67C23A' }
          }
        ]
      }

    case 'bar':
      return {
        tooltip: {
          trigger: 'axis',
          axisPointer: { type: 'shadow' }
        },
        legend: {
          data: ['预算金额', '执行金额']
        },
        grid: {
          left: '3%',
          right: '4%',
          bottom: '3%',
          containLabel: true
        },
        xAxis: {
          type: 'category',
          data: ['技术部', '销售部', '市场部', '财务部', '人事部']
        },
        yAxis: {
          type: 'value'
        },
        series: [
          {
            name: '预算金额',
            type: 'bar',
            data: [500000, 300000, 200000, 150000, 100000],
            itemStyle: { color: '#409EFF' }
          },
          {
            name: '执行金额',
            type: 'bar',
            data: [320000, 285000, 120000, 90000, 95000],
            itemStyle: { color: '#67C23A' }
          }
        ]
      }

    case 'gauge':
      return {
        series: [{
          type: 'gauge',
          center: ['50%', '60%'],
          radius: '75%',
          min: 0,
          max: 100,
          progress: {
            show: true,
            width: 30
          },
          axisLine: {
            lineStyle: {
              width: 30
            }
          },
          axisTick: {
            distance: -45,
            splitNumber: 5,
            lineStyle: {
              width: 2,
              color: '#999'
            }
          },
          axisLabel: {
            distance: -20,
            color: '#999',
            fontSize: 12
          },
          anchor: {
            show: true,
            showAbove: true,
            size: 18,
            itemStyle: {
              borderWidth: 8
            }
          },
          detail: {
            valueAnimation: true,
            fontSize: 40,
            offsetCenter: [0, '15%']
          },
          data: [{
            value: executionRate.value,
            name: '执行率'
          }]
        }]
      }
  }
}

const handlePageChange = (page: number) => {
  pageParams.current = page
  loadBudgetData()
}

const handlePageSizeChange = (size: number) => {
  pageParams.size = size
  pageParams.current = 1
  loadBudgetData()
}

const handleExport = () => {
  console.log('导出预算分析报表')
}

const handleDetail = (row: BudgetData) => {
  console.log('查看预算详情:', row)
}

const viewWarnings = () => {
  const warnings = budgetData.value.filter(item => Number(item.executionRate) > 90)
  console.log('查看预警详情:', warnings)
}

// 生命周期
onMounted(() => {
  loadBudgetData()
  window.addEventListener('resize', () => {
    chartInstance.value?.resize()
  })
})
</script>

<style scoped>
.budget-analysis {
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

.budget-overview {
  margin-bottom: 20px;
}

.budget-card {
  border: none;
}

.budget-container {
  display: flex;
  align-items: center;
  padding: 20px 0;
}

.budget-icon {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 20px;
  font-size: 32px;
  color: #fff;
}

.plan-icon { background: linear-gradient(135deg, #409EFF 0%, #67C23A 100%); }
.execute-icon { background: linear-gradient(135deg, #E6A23C 0%, #F56C6C 100%); }
.balance-icon { background: linear-gradient(135deg, #909399 0%, #C0C4CC 100%); }

.budget-content {
  flex: 1;
}

.budget-value {
  font-size: 28px;
  font-weight: bold;
  color: #303133;
  margin-bottom: 5px;
}

.budget-label {
  font-size: 16px;
  color: #606266;
  margin-bottom: 5px;
}

.budget-desc {
  font-size: 14px;
  color: #909399;
}

.chart-section, .data-table-section, .ranking-section {
  margin-bottom: 20px;
}

.chart-header, .table-header, .ranking-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.chart-title, .table-title, .ranking-title {
  font-size: 16px;
  font-weight: bold;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.alert-section {
  margin-bottom: 20px;
}

.ranking-list {
  padding: 0 10px;
}

.ranking-item {
  display: flex;
  align-items: center;
  padding: 15px 0;
  border-bottom: 1px solid #ebeef5;
}

.ranking-item:last-child {
  border-bottom: none;
}

.ranking-item.top-three {
  background: linear-gradient(90deg, rgba(64, 158, 255, 0.1), rgba(103, 194, 58, 0.1));
  margin: 0 -20px;
  padding: 15px 20px;
  border-radius: 4px;
}

.ranking-index {
  width: 60px;
  text-align: center;
}

.rank-number {
  display: inline-block;
  width: 30px;
  height: 30px;
  border-radius: 50%;
  background: #909399;
  color: #fff;
  line-height: 30px;
  font-weight: bold;
}

.rank-1 { background: #F56C6C; }
.rank-2 { background: #E6A23C; }
.rank-3 { background: #67C23A; }
.rank-normal { background: #909399; }

.ranking-info {
  flex: 1;
}

.dept-name {
  font-weight: bold;
  margin-bottom: 5px;
}

.budget-info {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  color: #909399;
}

.ranking-progress {
  width: 100px;
  margin-left: 20px;
}

.filter-actions {
  display: flex;
  gap: 10px;
  margin-top: 10px;
}

@media (max-width: 768px) {
  .budget-analysis {
    padding: 10px;
  }
  
  .budget-container {
    flex-direction: column;
    text-align: center;
  }
  
  .budget-icon {
    margin-right: 0;
    margin-bottom: 10px;
  }
  
  .ranking-item {
    flex-direction: column;
    align-items: stretch;
  }
  
  .ranking-progress {
    width: 100%;
    margin-left: 0;
    margin-top: 10px;
  }
}
</style>