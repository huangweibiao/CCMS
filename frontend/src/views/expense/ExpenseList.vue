<template>
  <div class="expense-list-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2>费用申请单列表</h2>
      <p>查看和管理费用申请单</p>
    </div>

    <div class="expense-list-content">
      <!-- 筛选条件 -->
      <div class="filter-section">
        <el-form :model="filterParams" inline class="filter-form">
          <el-form-item label="申请单号">
            <el-input v-model="filterParams.applyCode" placeholder="输入申请单号" clearable style="width: 150px" />
          </el-form-item>
          
          <el-form-item label="申请人">
            <el-input v-model="filterParams.applicant" placeholder="输入申请人" clearable style="width: 120px" />
          </el-form-item>
          
          <el-form-item label="申请部门">
            <el-select v-model="filterParams.departmentId" placeholder="选择部门" clearable style="width: 120px">
              <el-option v-for="dept in departmentList" :key="dept.id" :label="dept.deptName" :value="dept.id" />
            </el-select>
          </el-form-item>
          
          <el-form-item label="费用类型">
            <el-select v-model="filterParams.expenseType" placeholder="费用类型" clearable style="width: 120px">
              <el-option label="差旅报销" value="TRAVEL" />
              <el-option label="采购申请" value="PURCHASE" />
              <el-option label="常规报销" value="GENERAL" />
              <el-option label="会议费用" value="MEETING" />
            </el-select>
          </el-form-item>
          
          <el-form-item label="申请状态">
            <el-select v-model="filterParams.status" placeholder="申请状态" clearable style="width: 120px">
              <el-option label="草稿" value="DRAFT" />
              <el-option label="审批中" value="PENDING" />
              <el-option label="已批准" value="APPROVED" />
              <el-option label="已驳回" value="REJECTED" />
              <el-option label="已撤销" value="CANCELLED" />
            </el-select>
          </el-form-item>
          
          <el-form-item label="申请日期">
            <el-date-picker
              v-model="filterParams.dateRange"
              type="daterange"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
              style="width: 240px"
              clearable
            />
          </el-form-item>
          
          <el-form-item>
            <el-button type="primary" @click="handleSearch">
              <el-icon><search /></el-icon>
              查询
            </el-button>
            <el-button @click="handleReset">
              <el-icon><refresh /></el-icon>
              重置
            </el-button>
            <el-button type="success" @click="handleExport">
              <el-icon><download /></el-icon>
              导出
            </el-button>
          </el-form-item>
        </el-form>
      </div>

      <!-- 申请单表格 -->
      <div class="list-table">
        <DataTable
          :columns="columns"
          :data="tableData"
          :loading="loading"
          :pagination="pagination"
          @pagination-change="handlePageChange"
        >
          <!-- 申请单号列 -->
          <template #applyCode="{ row }">
            <el-button type="primary" link @click="handleViewDetail(row)">{{ row.applyCode }}</el-button>
          </template>
          
          <!-- 状态列 -->
          <template #status="{ row }">
            <el-tag :type="getStatusType(row.status)" size="small">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
          
          <!-- 金额列 -->
          <template #totalAmount="{ row }">
            <span class="amount-text">¥{{ formatAmount(row.totalAmount) }}</span>
          </template>
          
          <!-- 紧急程度列 -->
          <template #urgency="{ row }">
            <el-tag :type="getUrgencyType(row.urgency)" size="small">
              {{ getUrgencyText(row.urgency) }}
            </el-tag>
          </template>
          
          <!-- 操作列 -->
          <template #actions="{ row }">
            <div class="action-buttons">
              <el-button type="primary" link size="small" @click="handleViewDetail(row)">
                查看
              </el-button>
              <el-button v-if="row.status === 'DRAFT'" type="success" link size="small" @click="handleEdit(row)">
                编辑
              </el-button>
              <el-button 
                v-if="row.status === 'DRAFT'" 
                type="warning" 
                link size="small" 
                @click="handleSubmit(row)"
              >
                提交
              </el-button>
              <el-button 
                v-if="row.status === 'DRAFT' || row.status === 'PENDING'" 
                type="danger" 
                link size="small" 
                @click="handleCancel(row)"
              >
                撤销
              </el-button>
              <el-button type="info" link size="small" @click="handleTrack(row)">
                跟踪
              </el-button>
            </div>
          </template>
        </DataTable>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { Search, Refresh, Download } from '@element-plus/icons-vue'
import DataTable from '@/components/common/DataTable.vue'
import { showSuccess, showWarning, showError } from '@/components/common/Message.vue'

// 响应式数据
const loading = ref(false)

// 筛选参数
const filterParams = reactive({
  applyCode: '',
  applicant: '',
  departmentId: '',
  expenseType: '',
  status: '',
  dateRange: []
})

// 部门列表
const departmentList = ref([
  { id: '1', deptName: '技术部' },
  { id: '2', deptName: '财务部' },
  { id: '3', deptName: '市场部' },
  { id: '4', deptName: '人事部' },
  { id: '5', deptName: '行政部' }
])

// 表格数据
const tableData = ref([
  {
    id: '1',
    applyCode: 'EXP2024010001',
    applicant: '张经理',
    departmentName: '技术部',
    expenseType: 'TRAVEL',
    totalAmount: 3850.50,
    applyDate: '2024-01-15 10:30:25',
    status: 'PENDING',
    urgency: 'HIGH',
    currentApprover: '李总',
    approveLevel: '2/3'
  },
  {
    id: '2',
    applyCode: 'EXP2024010002',
    applicant: '王会计',
    departmentName: '财务部',
    expenseType: 'GENERAL',
    totalAmount: 1200.00,
    applyDate: '2024-01-14 14:20:18',
    status: 'APPROVED',
    urgency: 'LOW',
    currentApprover: '-',
    approveLevel: '3/3'
  },
  {
    id: '3',
    applyCode: 'EXP2024010003',
    applicant: '赵主管',
    departmentName: '市场部',
    expenseType: 'PURCHASE',
    totalAmount: 8500.00,
    applyDate: '2024-01-13 09:15:30',
    status: 'REJECTED',
    urgency: 'MEDIUM',
    currentApprover: '-',
    approveLevel: '1/3'
  },
  {
    id: '4',
    applyCode: 'EXP2024010004',
    applicant: '陈助理',
    departmentName: '行政部',
    expenseType: 'MEETING',
    totalAmount: 3000.00,
    applyDate: '2024-01-12 16:45:22',
    status: 'DRAFT',
    urgency: 'LOW',
    currentApprover: '-',
    approveLevel: '0/3'
  },
  {
    id: '5',
    applyCode: 'EXP2024010005',
    applicant: '刘工程师',
    departmentName: '技术部',
    expenseType: 'TRAVEL',
    totalAmount: 2500.00,
    applyDate: '2024-01-11 11:30:15',
    status: 'CANCELLED',
    urgency: 'MEDIUM',
    currentApprover: '-',
    approveLevel: '2/3'
  }
])

// 表格列配置
const columns = [
  { prop: 'applyCode', label: '申请单号', width: 150, slot: 'applyCode' },
  { prop: 'applicant', label: '申请人', width: 100 },
  { prop: 'departmentName', label: '申请部门', width: 100 },
  { prop: 'expenseType', label: '费用类型', width: 100, slot: 'expenseType' },
  { prop: 'totalAmount', label: '申请金额', width: 120, slot: 'totalAmount', sortable: true },
  { prop: 'applyDate', label: '申请日期', width: 180, sortable: true },
  { prop: 'status', label: '状态', width: 100, slot: 'status' },
  { prop: 'urgency', label: '紧急程度', width: 100, slot: 'urgency' },
  { prop: 'currentApprover', label: '当前审批人', width: 100 },
  { prop: 'approveLevel', label: '审批进度', width: 100 },
  { prop: 'actions', label: '操作', width: 200, slot: 'actions' }
]

// 分页配置
const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: tableData.value.length
})

// 生命周期
onMounted(() => {
  loadExpenseList()
})

// 方法定义
const loadExpenseList = async () => {
  loading.value = true
  try {
    // TODO: 调用API获取申请单列表
    await new Promise(resolve => setTimeout(resolve, 500))
    // 使用模拟数据
    console.log('加载费用申请单列表')
  } catch (error) {
    console.error('加载申请单列表失败:', error)
    showError('加载申请单列表失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.current = 1
  loadExpenseList()
}

const handleReset = () => {
  Object.assign(filterParams, {
    applyCode: '',
    applicant: '',
    departmentId: '',
    expenseType: '',
    status: '',
    dateRange: []
  })
  pagination.current = 1
  loadExpenseList()
}

const handleExport = async () => {
  try {
    // TODO: 调用导出API
    await new Promise(resolve => setTimeout(resolve, 500))
    showSuccess('导出成功')
  } catch (error) {
    showError('导出失败')
  }
}

const handlePageChange = (page: number, pageSize: number) => {
  pagination.current = page
  pagination.pageSize = pageSize
  loadExpenseList()
}

const handleViewDetail = (row: any) => {
  // 跳转到详情页面或打开详情弹窗
  showSuccess(`查看申请单详情: ${row.applyCode}`)
}

const handleEdit = (row: any) => {
  // 跳转到编辑页面
  showSuccess(`编辑申请单: ${row.applyCode}`)
}

const handleSubmit = async (row: any) => {
  try {
    await ElMessageBox.confirm(
      `确定要提交申请单"${row.applyCode}"吗？提交后将进入审批流程。`,
      '提交确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    // TODO: 调用提交API
    await new Promise(resolve => setTimeout(resolve, 500))
    showSuccess('申请单已提交审批')
    loadExpenseList()
  } catch (error) {
    // 用户取消操作
  }
}

const handleCancel = async (row: any) => {
  try {
    await ElMessageBox.confirm(
      `确定要撤销申请单"${row.applyCode}"吗？撤销后审批流程将终止。`,
      '撤销确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    // TODO: 调用撤销API
    await new Promise(resolve => setTimeout(resolve, 500))
    showSuccess('申请单已撤销')
    loadExpenseList()
  } catch (error) {
    // 用户取消操作
  }
}

const handleTrack = (row: any) => {
  // 打开审批流程跟踪窗口
  showSuccess(`跟踪审批流程: ${row.applyCode}`)
}

// 工具函数
const getStatusType = (status: string) => {
  const statusMap: Record<string, string> = {
    'DRAFT': 'info',
    'PENDING': 'warning',
    'APPROVED': 'success',
    'REJECTED': 'danger',
    'CANCELLED': 'info'
  }
  return statusMap[status] || 'info'
}

const getStatusText = (status: string) => {
  const statusTextMap: Record<string, string> = {
    'DRAFT': '草稿',
    'PENDING': '审批中',
    'APPROVED': '已批准',
    'REJECTED': '已驳回',
    'CANCELLED': '已撤销'
  }
  return statusTextMap[status] || status
}

const getUrgencyType = (urgency: string) => {
  const urgencyMap: Record<string, string> = {
    'LOW': 'info',
    'MEDIUM': 'warning',
    'HIGH': 'danger'
  }
  return urgencyMap[urgency] || 'info'
}

const getUrgencyText = (urgency: string) => {
  const urgencyTextMap: Record<string, string> = {
    'LOW': '普通',
    'MEDIUM': '较急',
    'HIGH': '紧急'
  }
  return urgencyTextMap[urgency] || '未知'
}

const formatAmount = (amount: number) => {
  return amount?.toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 }) || '0.00'
}
</script>

<style scoped lang="css">
.expense-list-container {
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

.expense-list-content {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.filter-section {
  background: white;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.filter-form {
  margin-bottom: 0;
}

.filter-form :deep(.el-form-item) {
  margin-bottom: 0;
}

.list-table {
  background: white;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.amount-text {
  font-weight: 600;
  color: #409eff;
}

.action-buttons {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .filter-form {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
    gap: 16px;
  }
  
  .action-buttons {
    flex-direction: column;
  }
}

@media (max-width: 768px) {
  .expense-list-container {
    padding: 10px;
  }
  
  .filter-section {
    padding: 15px;
  }
  
  .filter-form {
    grid-template-columns: 1fr;
  }
}
</style>