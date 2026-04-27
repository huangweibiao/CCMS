<template>
  <div class="budget-list-container">
    <!-- 页面标题和操作区域 -->
    <div class="page-header">
      <div class="page-title">
        <h2>预算查询</h2>
        <p>查看和管理部门预算信息</p>
      </div>
      <div class="page-actions">
        <el-button type="primary" @click="handleCreate">
          <el-icon><plus /></el-icon>
          新建预算
        </el-button>
        <el-button @click="handleExport">
          <el-icon><download /></el-icon>
          导出数据
        </el-button>
        <el-button @click="handleRefresh">
          <el-icon><refresh /></el-icon>
          刷新
        </el-button>
      </div>
    </div>

    <!-- 查询条件 -->
    <div class="search-form">
      <el-form :model="searchForm" size="default">
        <el-row :gutter="20">
          <el-col :xs="24" :sm="12" :md="8" :lg="6">
            <el-form-item label="预算年度">
              <el-date-picker
                v-model="searchForm.budgetYear"
                type="year"
                placeholder="选择年度"
                format="YYYY"
                value-format="YYYY"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12" :md="8" :lg="6">
            <el-form-item label="预算类型">
              <el-select v-model="searchForm.budgetType" placeholder="请选择类型" clearable style="width: 100%">
                <el-option label="年度预算" value="ANNUAL" />
                <el-option label="季度预算" value="QUARTERLY" />
                <el-option label="月度预算" value="MONTHLY" />
                <el-option label="项目预算" value="PROJECT" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12" :md="8" :lg="6">
            <el-form-item label="责任部门">
              <el-select v-model="searchForm.departmentId" placeholder="请选择部门" clearable style="width: 100%">
                <el-option 
                  v-for="dept in departmentList" 
                  :key="dept.id" 
                  :label="dept.deptName" 
                  :value="dept.id" 
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12" :md="8" :lg="6">
            <el-form-item label="预算状态">
              <el-select v-model="searchForm.status" placeholder="请选择状态" clearable style="width: 100%">
                <el-option label="草稿" value="DRAFT" />
                <el-option label="待审批" value="PENDING" />
                <el-option label="已批准" value="APPROVED" />
                <el-option label="已驳回" value="REJECTED" />
                <el-option label="执行中" value="EXECUTING" />
                <el-option label="已完成" value="COMPLETED" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row :gutter="20">
          <el-col :span="24">
            <div class="search-actions">
              <el-button type="primary" @click="handleSearch">
                <el-icon><search /></el-icon>
                查询
              </el-button>
              <el-button @click="handleReset">
                <el-icon><refresh-right /></el-icon>
                重置
              </el-button>
            </div>
          </el-col>
        </el-row>
      </el-form>
    </div>

    <!-- 数据表格 -->
    <div class="data-table">
      <DataTable
        ref="dataTableRef"
        :columns="tableColumns"
        :data="tableData"
        :loading="loading"
        :pagination="pagination"
        @pagination-change="handlePageChange"
        @sort-change="handleSortChange"
      >
        <!-- 预算状态列 -->
        <template #status="{ row }">
          <el-tag 
            :type="getStatusType(row.status)"
            size="small"
          >
            {{ getStatusText(row.status) }}
          </el-tag>
        </template>

        <!-- 操作列 -->
        <template #actions="{ row }">
          <el-button type="primary" link size="small" @click="handleView(row)">
            查看
          </el-button>
          <el-button type="primary" link size="small" @click="handleEdit(row)" 
            v-if="row.status === 'DRAFT' || row.status === 'REJECTED'">
            编辑
          </el-button>
          <el-button type="warning" link size="small" @click="handleAdjust(row)" 
            v-if="row.status === 'APPROVED' || row.status === 'EXECUTING'">
            调整
          </el-button>
          <el-button type="success" link size="small" @click="handleAnalysis(row)">
            分析
          </el-button>
          <el-button type="danger" link size="small" @click="handleDelete(row)" 
            v-if="row.status === 'DRAFT'">
            删除
          </el-button>
        </template>
      </DataTable>
    </div>

    <!-- 预算详情对话框 -->
    <CustomModal
      v-model="detailModalVisible"
      title="预算详情"
      width="800px"
      @closed="handleDetailModalClosed"
    >
      <div v-if="selectedBudget" class="budget-detail">
        <div class="detail-row">
          <div class="detail-item">
            <label>预算编号：</label>
            <span>{{ selectedBudget.budgetCode }}</span>
          </div>
          <div class="detail-item">
            <label>预算年度：</label>
            <span>{{ selectedBudget.budgetYear }}</span>
          </div>
        </div>
        <div class="detail-row">
          <div class="detail-item">
            <label>预算类型：</label>
            <span>{{ getBudgetTypeText(selectedBudget.budgetType) }}</span>
          </div>
          <div class="detail-item">
            <label>责任部门：</label>
            <span>{{ selectedBudget.departmentName }}</span>
          </div>
        </div>
        <div class="detail-row">
          <div class="detail-item">
            <label>预算总额：</label>
            <span class="amount">¥{{ selectedBudget.totalAmount?.toFixed(2) }}</span>
          </div>
          <div class="detail-item">
            <label>预算状态：</label>
            <el-tag :type="getStatusType(selectedBudget.status)" size="small">
              {{ getStatusText(selectedBudget.status) }}
            </el-tag>
          </div>
        </div>
        <div class="detail-row">
          <div class="detail-item">
            <label>创建人：</label>
            <span>{{ selectedBudget.createUser }}</span>
          </div>
          <div class="detail-item">
            <label>创建时间：</label>
            <span>{{ selectedBudget.createTime }}</span>
          </div>
        </div>
        
        <!-- 预算明细表格 -->
        <div class="detail-section">
          <h4>预算明细</h4>
          <el-table :data="selectedBudget.details" :border="true" size="small">
            <el-table-column label="费用类型" prop="expenseTypeName" min-width="120" />
            <el-table-column label="预算金额" prop="budgetAmount" min-width="120">
              <template #default="{ row }">
                ¥{{ row.budgetAmount?.toFixed(2) }}
              </template>
            </el-table-column>
            <el-table-column label="预算周期" prop="budgetPeriod" min-width="100">
              <template #default="{ row }">
                {{ getPeriodText(row.budgetPeriod) }}
              </template>
            </el-table-column>
            <el-table-column label="已使用金额" prop="usedAmount" min-width="120">
              <template #default="{ row }">
                ¥{{ row.usedAmount?.toFixed(2) }}
              </template>
            </el-table-column>
            <el-table-column label="使用率" prop="usageRate" min-width="80" align="center">
              <template #default="{ row }">
                <el-progress 
                  :percentage="Math.round(row.usageRate || 0)" 
                  :show-text="false" 
                  size="small" 
                />
                <span style="font-size: 12px;">{{ Math.round(row.usageRate || 0) }}%</span>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </div>
      
      <template #footer>
        <el-button @click="detailModalVisible = false">关闭</el-button>
      </template>
    </CustomModal>
  </div>
</template>

<script setup lang="ts">
/**
 * 预算查询列表组件
 * 负责预算信息的查询、展示和管理操作
 * 
 * @file BudgetList.vue
 * @description 预算管理模块 - 预算列表查看页面
 * @author CCMS开发团队
 * @version 1.0.0
 */

import { ref, reactive, onMounted } from 'vue'
import { Plus, Search, RefreshRight, Download, Refresh } from '@element-plus/icons-vue'

// 引入通用组件
import DataTable from '@/components/common/DataTable.vue'
import CustomModal from '@/components/common/CustomModal.vue'
import { showSuccess, showConfirm } from '@/components/common/Message.vue'

/**
 * 数据表格组件引用
 * 用于控制数据表格的分页、排序等操作
 */
const dataTableRef = ref()

/**
 * 数据加载状态
 * 控制表格数据的加载动画显示
 */
const loading = ref(false)

/**
 * 预算详情弹窗显示状态
 * 控制预算详情对话框的显示/隐藏
 */
const detailModalVisible = ref(false)

/**
 * 选中的预算信息
 * 存储当前查看详情的预算对象
 */
const selectedBudget = ref(null)

/**
 * 部门列表数据
 * 存储所有部门信息，用于责任部门筛选下拉框
 */
const departmentList = ref([
  { id: '1', deptName: '技术部' },
  { id: '2', deptName: '财务部' },
  { id: '3', deptName: '人事部' },
  { id: '4', deptName: '市场部' },
  { id: '5', deptName: '行政部' }
])

// 查询表单
const searchForm = reactive({
  budgetYear: '',
  budgetType: '',
  departmentId: '',
  status: ''
})

// 分页配置
const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0
})

// 表格列配置
const tableColumns = [
  { prop: 'budgetCode', label: '预算编号', width: 120, sortable: true },
  { prop: 'budgetYear', label: '预算年度', width: 100, sortable: true },
  { prop: 'budgetType', label: '预算类型', width: 100, slot: 'budgetType' },
  { prop: 'departmentName', label: '责任部门', width: 120 },
  { prop: 'totalAmount', label: '预算总额', width: 120, sortable: true },
  { prop: 'usedAmount', label: '已使用', width: 120, sortable: true },
  { prop: 'usageRate', label: '使用率', width: 100, sortable: true },
  { prop: 'status', label: '状态', width: 90, slot: 'status' },
  { prop: 'createUser', label: '创建人', width: 100 },
  { prop: 'createTime', label: '创建时间', width: 150, sortable: true },
  { prop: 'actions', label: '操作', width: 220, slot: 'actions', fixed: 'right' }
]

// 表格数据
const tableData = ref([])

// 生命周期
onMounted(() => {
  loadBudgetList()
})

// 方法
const loadBudgetList = async () => {
  loading.value = true
  try {
    // TODO: 调用API获取预算列表
    // 使用模拟数据
    tableData.value = [
      {
        id: '1',
        budgetCode: 'BGT202401001',
        budgetYear: '2024',
        budgetType: 'ANNUAL',
        departmentName: '技术部',
        totalAmount: 500000,
        usedAmount: 125000,
        usageRate: 25,
        status: 'EXECUTING',
        createUser: '张经理',
        createTime: '2024-01-15 10:30:25',
        details: [
          { expenseTypeName: '办公用品', budgetAmount: 50000, usedAmount: 12500, usageRate: 25, budgetPeriod: 'YEAR' },
          { expenseTypeName: '差旅费用', budgetAmount: 200000, usedAmount: 50000, usageRate: 25, budgetPeriod: 'YEAR' },
          { expenseTypeName: '设备购置', budgetAmount: 250000, usedAmount: 62500, usageRate: 25, budgetPeriod: 'YEAR' }
        ]
      },
      {
        id: '2',
        budgetCode: 'BGT202401002',
        budgetYear: '2024',
        budgetType: 'QUARTERLY',
        departmentName: '市场部',
        totalAmount: 200000,
        usedAmount: 160000,
        usageRate: 80,
        status: 'EXECUTING',
        createUser: '李总监',
        createTime: '2024-01-10 14:20:15',
        details: [
          { expenseTypeName: '市场推广', budgetAmount: 150000, usedAmount: 120000, usageRate: 80, budgetPeriod: 'Q1' },
          { expenseTypeName: '招待费用', budgetAmount: 50000, usedAmount: 40000, usageRate: 80, budgetPeriod: 'Q1' }
        ]
      },
      {
        id: '3',
        budgetCode: 'BGT202401003',
        budgetYear: '2024',
        budgetType: 'PROJECT',
        departmentName: '行政部',
        totalAmount: 100000,
        usedAmount: 25000,
        usageRate: 25,
        status: 'APPROVED',
        createUser: '王主管',
        createTime: '2024-01-18 09:15:08',
        details: [
          { expenseTypeName: '办公用品', budgetAmount: 100000, usedAmount: 25000, usageRate: 25, budgetPeriod: 'YEAR' }
        ]
      }
    ]
    pagination.total = tableData.value.length
  } catch (error) {
    console.error('加载预算列表失败:', error)
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.current = 1
  loadBudgetList()
}

const handleReset = () => {
  Object.keys(searchForm).forEach(key => {
    searchForm[key] = ''
  })
  pagination.current = 1
  loadBudgetList()
}

const handlePageChange = (page: number, pageSize: number) => {
  pagination.current = page
  pagination.pageSize = pageSize
  loadBudgetList()
}

const handleSortChange = (sort: any) => {
  console.log('排序变化:', sort)
  loadBudgetList()
}

const handleCreate = () => {
  // 跳转到预算编制页面
  showSuccess('跳转到新建预算页面')
}

const handleExport = () => {
  showSuccess('导出功能开发中...')
}

const handleRefresh = () => {
  loadBudgetList()
  showSuccess('数据已刷新')
}

const handleView = (row: any) => {
  selectedBudget.value = row
  detailModalVisible.value = true
}

const handleEdit = (row: any) => {
  showSuccess(`编辑预算: ${row.budgetCode}`)
}

const handleAdjust = (row: any) => {
  showSuccess(`调整预算: ${row.budgetCode}`)
}

const handleAnalysis = (row: any) => {
  showSuccess(`分析预算: ${row.budgetCode}`)
}

const handleDelete = async (row: any) => {
  const confirmed = await showConfirm(`确定要删除预算 ${row.budgetCode} 吗？`)
  if (confirmed) {
    showSuccess('删除成功')
    loadBudgetList()
  }
}

const handleDetailModalClosed = () => {
  selectedBudget.value = null
}

// 状态类型映射
const getStatusType = (status: string) => {
  const statusMap = {
    DRAFT: 'info',
    PENDING: 'warning',
    APPROVED: 'success',
    REJECTED: 'danger',
    EXECUTING: 'primary',
    COMPLETED: ''
  }
  return statusMap[status] || 'info'
}

const getStatusText = (status: string) => {
  const textMap = {
    DRAFT: '草稿',
    PENDING: '待审批',
    APPROVED: '已批准',
    REJECTED: '已驳回',
    EXECUTING: '执行中',
    COMPLETED: '已完成'
  }
  return textMap[status] || status
}

const getBudgetTypeText = (type: string) => {
  const typeMap = {
    ANNUAL: '年度预算',
    QUARTERLY: '季度预算',
    MONTHLY: '月度预算',
    PROJECT: '项目预算'
  }
  return typeMap[type] || type
}

const getPeriodText = (period: string) => {
  const periodMap = {
    YEAR: '全年',
    Q1: '一季度',
    Q2: '二季度',
    Q3: '三季度',
    Q4: '四季度',
    MONTH: '月度'
  }
  return periodMap[period] || period
}
</script>

<style scoped lang="css">
.budget-list-container {
  padding: 20px;
  background: #f5f7fa;
  min-height: calc(100vh - 60px);
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 20px;
}

.page-title h2 {
  margin: 0 0 8px 0;
  color: #303133;
  font-size: 20px;
}

.page-title p {
  margin: 0;
  color: #909399;
  font-size: 14px;
}

.page-actions {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.search-form {
  background: white;
  border-radius: 8px;
  padding: 20px;
  margin-bottom: 20px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.search-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  padding-top: 10px;
}

.data-table {
  background: white;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.budget-detail {
  padding: 10px 0;
}

.detail-row {
  display: flex;
  margin-bottom: 16px;
  gap: 20px;
}

.detail-item {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 8px;
}

.detail-item label {
  min-width: 80px;
  color: #606266;
  font-weight: 500;
}

.detail-item .amount {
  color: #f56c6c;
  font-weight: 600;
  font-size: 16px;
}

.detail-section {
  margin-top: 24px;
  padding-top: 16px;
  border-top: 1px solid #ebeef5;
}

.detail-section h4 {
  margin: 0 0 16px 0;
  color: #303133;
  font-size: 16px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .budget-list-container {
    padding: 10px;
  }
  
  .page-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }
  
  .search-form {
    padding: 16px;
  }
  
  .detail-row {
    flex-direction: column;
    gap: 12px;
  }
  
  .search-actions {
    justify-content: center;
  }
}
</style>