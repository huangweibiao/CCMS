<template>
  <div class="done-approval-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2>已办审批</h2>
      <p>查看您已审批的费用申请和报销单记录</p>
    </div>

    <!-- 筛选条件 -->
    <div class="filter-section">
      <el-form ref="filterForm" :model="filterForm" inline>
        <el-form-item label="审批类型">
          <el-select v-model="filterForm.approvalType" placeholder="选择审批类型" clearable>
            <el-option label="费用申请" value="EXPENSE_APPLY" />
            <el-option label="费用报销" value="EXPENSE_REIMBURSE" />
            <el-option label="预算申请" value="BUDGET_APPLY" />
          </el-select>
        </el-form-item>
        <el-form-item label="审批结果">
          <el-select v-model="filterForm.approvalResult" placeholder="选择审批结果" clearable>
            <el-option label="同意" value="APPROVED" />
            <el-option label="驳回" value="REJECTED" />
            <el-option label="转审" value="TRANSFERRED" />
          </el-select>
        </el-form-item>
        <el-form-item label="审批时间">
          <el-date-picker
            v-model="filterForm.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>
        <el-form-item label="关键词搜索">
          <el-input
            v-model="filterForm.keyword"
            placeholder="输入申请标题、申请人或事由"
            clearable
            style="width: 250px"
          >
            <template #append>
              <el-button @click="handleSearch">
                <el-icon><search /></el-icon>
              </el-button>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">
            <el-icon><search /></el-icon>
            搜索
          </el-button>
          <el-button @click="handleReset">
            <el-icon><refresh /></el-icon>
            重置
          </el-button>
          <el-button type="info" @click="exportData">
            <el-icon><download /></el-icon>
            导出
          </el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 统计卡片 -->
    <div class="statistics-section">
      <el-row :gutter="20">
        <el-col :span="6">
          <div class="stat-card total">
            <div class="stat-icon"><el-icon><document /></el-icon></div>
            <div class="stat-content">
              <div class="stat-number">{{ statistics.totalCount }}</div>
              <div class="stat-label">总审批数</div>
            </div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-card approved">
            <div class="stat-icon"><el-icon><check /></el-icon></div>
            <div class="stat-content">
              <div class="stat-number">{{ statistics.approvedCount }}</div>
              <div class="stat-label">同意数量</div>
            </div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-card rejected">
            <div class="stat-icon"><el-icon><close /></el-icon></div>
            <div class="stat-content">
              <div class="stat-number">{{ statistics.rejectedCount }}</div>
              <div class="stat-label">驳回数量</div>
            </div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-card average">
            <div class="stat-icon"><el-icon><timer /></el-icon></div>
            <div class="stat-content">
              <div class="stat-number">{{ statistics.avgTime }}</div>
              <div class="stat-label">平均审批时长</div>
            </div>
          </div>
        </el-col>
      </el-row>
    </div>

    <!-- 已办审批列表 -->
    <div class="approval-list-section">
      <el-table
        :data="approvalList"
        v-loading="loading"
        style="width: 100%"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column label="审批类型" width="120" prop="type">
          <template #default="{ row }">
            <el-tag :type="getTypeColor(row.type)" size="small">
              {{ getTypeText(row.type) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="申请标题" min-width="200" prop="title">
          <template #default="{ row }">
            <div class="title-cell">
              <span class="title-text">{{ row.title }}</span>
              <el-tag v-if="row.urgency === 'HIGH'" size="small" type="danger">紧急</el-tag>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="申请人" width="120" prop="applicant" />
        <el-table-column label="部门" width="120" prop="department" />
        <el-table-column label="金额" width="120" prop="amount">
          <template #default="{ row }">
            <span class="amount">¥{{ formatAmount(row.amount) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="申请时间" width="160" prop="applyTime">
          <template #default="{ row }">
            {{ formatDateTime(row.applyTime) }}
          </template>
        </el-table-column>
        <el-table-column label="审批时间" width="160" prop="approvalTime">
          <template #default="{ row }">
            {{ formatDateTime(row.approvalTime) }}
          </template>
        </el-table-column>
        <el-table-column label="审批结果" width="100" prop="result">
          <template #default="{ row }">
            <el-tag 
              :type="row.result === 'APPROVED' ? 'success' : row.result === 'REJECTED' ? 'danger' : 'warning'"
              size="small"
            >
              {{ getResultText(row.result) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="处理时长" width="120" prop="processTime">
          <template #default="{ row }">
            {{ formatProcessTime(row.processTime) }}
          </template>
        </el-table-column>
        <el-table-column label="当前状态" width="120" prop="status">
          <template #default="{ row }">
            <el-tag :type="getStatusColor(row.status)" size="small">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button 
              type="primary" 
              link 
              size="small" 
              @click="viewDetail(row)"
            >
              <el-icon><view /></el-icon>
              详情
            </el-button>
            <el-button 
              type="info" 
              link 
              size="small" 
              @click="viewProcess(row)"
            >
              <el-icon><sort /></el-icon>
              流程
            </el-button>
            <el-button 
              type="warning" 
              link 
              size="small" 
              @click="approveAgain(row)"
              v-if="row.result === 'REJECTED'"
            >
              <el-icon><refresh /></el-icon>
              重审
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-section">
        <el-pagination
          v-model:current-page="pagination.current"
          v-model:page-size="pagination.size"
          :total="pagination.total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { Search, Refresh, Download, Document, Check, Close, Timer, View, Sort } from '@element-plus/icons-vue'
import { showSuccess, showError, showConfirm } from '@/components/common/Message.vue'

// 路由管理
const router = useRouter()

// 筛选表单
const filterForm = reactive({
  approvalType: '',
  approvalResult: '',
  dateRange: [],
  keyword: ''
})

// 分页信息
const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

// 统计信息
const statistics = reactive({
  totalCount: 0,
  approvedCount: 0,
  rejectedCount: 0,
  avgTime: '0小时'
})

// 表格数据
const approvalList = ref<any[]>([])
const selectedItems = ref<any[]>([])
const loading = ref(false)

// 类型映射
const typeMap = {
  EXPENSE_APPLY: { text: '费用申请', color: 'primary' },
  EXPENSE_REIMBURSE: { text: '费用报销', color: 'success' },
  BUDGET_APPLY: { text: '预算申请', color: 'warning' }
}

// 结果映射
const resultMap = {
  APPROVED: '同意',
  REJECTED: '驳回',
  TRANSFERRED: '转审'
}

// 状态映射
const statusMap = {
  PENDING: '审批中',
  APPROVED: '已通过',
  REJECTED: '已驳回',
  COMPLETED: '已完成',
  CANCELLED: '已取消'
}

// 状态颜色映射
const statusColorMap = {
  PENDING: 'warning',
  APPROVED: 'success',
  REJECTED: 'danger',
  COMPLETED: 'info',
  CANCELLED: 'info'
}

// 生命周期
onMounted(() => {
  loadApprovalList()
  loadStatistics()
})

// 方法定义
const loadApprovalList = async () => {
  loading.value = true
  try {
    // TODO: 调用API获取已办列表
    await new Promise(resolve => setTimeout(resolve, 500))
    
    // 模拟数据
    approvalList.value = [
      {
        id: '1',
        type: 'EXPENSE_APPLY',
        title: '差旅费用申请 - 北京项目会议',
        applicant: '张经理',
        department: '技术部',
        amount: 5000.00,
        applyTime: '2024-01-15 10:30:00',
        approvalTime: '2024-01-15 16:45:00',
        result: 'APPROVED',
        processTime: 6.25, // 小时
        status: 'COMPLETED',
        urgency: 'HIGH'
      },
      {
        id: '2',
        type: 'EXPENSE_REIMBURSE',
        title: '业务招待费用报销',
        applicant: '李总监',
        department: '市场部',
        amount: 3200.00,
        applyTime: '2024-01-14 16:45:00',
        approvalTime: '2024-01-16 09:20:00',
        result: 'APPROVED',
        processTime: 40.58, // 小时
        status: 'COMPLETED',
        urgency: 'MEDIUM'
      },
      {
        id: '3',
        type: 'EXPENSE_REIMBURSE',
        title: '办公用品采购报销',
        applicant: '王助理',
        department: '行政部',
        amount: 1200.50,
        applyTime: '2024-01-13 11:20:00',
        approvalTime: '2024-01-17 10:15:00',
        result: 'REJECTED',
        processTime: 94.92, // 小时
        status: 'REJECTED',
        urgency: 'LOW'
      },
      {
        id: '4',
        type: 'EXPENSE_APPLY',
        title: '设备采购申请',
        applicant: '赵工程师',
        department: '研发部',
        amount: 8000.00,
        applyTime: '2024-01-16 14:30:00',
        approvalTime: '2024-01-16 16:00:00',
        result: 'TRANSFERRED',
        processTime: 1.5,
        status: 'PENDING',
        urgency: 'MEDIUM'
      },
      {
        id: '5',
        type: 'BUDGET_APPLY',
        title: '第一季度部门预算申请',
        applicant: '钱主管',
        department: '财务部',
        amount: 50000.00,
        applyTime: '2024-01-10 09:00:00',
        approvalTime: '2024-01-12 15:30:00',
        result: 'APPROVED',
        processTime: 54.5,
        status: 'COMPLETED',
        urgency: 'MEDIUM'
      }
    ]
    
    pagination.total = approvalList.value.length
    
  } catch (error) {
    console.error('加载已办列表失败:', error)
    showError('加载已办列表失败')
  } finally {
    loading.value = false
  }
}

const loadStatistics = async () => {
  try {
    // TODO: 调用API获取统计数据
    await new Promise(resolve => setTimeout(resolve, 300))
    
    Object.assign(statistics, {
      totalCount: 156,
      approvedCount: 128,
      rejectedCount: 15,
      avgTime: '2.3小时'
    })
    
  } catch (error) {
    console.error('加载统计数据失败:', error)
  }
}

const handleSearch = () => {
  pagination.current = 1
  loadApprovalList()
}

const handleReset = () => {
  Object.assign(filterForm, {
    approvalType: '',
    approvalResult: '',
    dateRange: [],
    keyword: ''
  })
  pagination.current = 1
  loadApprovalList()
}

const handleSizeChange = (size: number) => {
  pagination.size = size
  pagination.current = 1
  loadApprovalList()
}

const handleCurrentChange = (current: number) => {
  pagination.current = current
  loadApprovalList()
}

const handleSelectionChange = (selection: any[]) => {
  selectedItems.value = selection
}

const viewDetail = (row: any) => {
  console.log('查看审批详情:', row)
  // TODO: 跳转到详情页面
  if (row.type === 'EXPENSE_APPLY') {
    router.push(`/expense/detail/${row.id}`)
  } else if (row.type === 'EXPENSE_REIMBURSE') {
    router.push(`/reimburse/detail/${row.id}`)
  }
}

const viewProcess = (row: any) => {
  console.log('查看审批流程:', row)
  // TODO: 打开审批流程图
  showSuccess('审批流程查看功能开发中')
}

const approveAgain = async (row: any) => {
  try {
    await showConfirm(`确定要重新审批"${row.title}"吗？`)
    
    // TODO: 调用重新审批API
    await new Promise(resolve => setTimeout(resolve, 300))
    
    showSuccess('已提交重新审批')
    loadApprovalList()
    
  } catch (error) {
    // 用户取消操作
  }
}

const exportData = async () => {
  try {
    loading.value = true
    
    // TODO: 调用导出API
    await new Promise(resolve => setTimeout(resolve, 1000))
    
    showSuccess('数据导出成功')
    
  } catch (error) {
    console.error('导出失败:', error)
    showError('数据导出失败')
  } finally {
    loading.value = false
  }
}

const getTypeText = (type: string) => {
  return typeMap[type]?.text || '未知类型'
}

const getTypeColor = (type: string) => {
  return typeMap[type]?.color || 'info'
}

const getResultText = (result: string) => {
  return resultMap[result] || '未知'
}

const getStatusText = (status: string) => {
  return statusMap[status] || '未知'
}

const getStatusColor = (status: string) => {
  return statusColorMap[status] || 'info'
}

const formatAmount = (amount: number) => {
  return amount?.toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 }) || '0.00'
}

const formatDateTime = (dateTime: string) => {
  if (!dateTime) return '-'
  return dateTime.replace('T', ' ').substring(0, 16)
}

const formatProcessTime = (hours: number) => {
  if (hours < 1) {
    const minutes = Math.round(hours * 60)
    return `${minutes}分钟`
  } else if (hours < 24) {
    return `${hours.toFixed(1)}小时`
  } else {
    const days = Math.floor(hours / 24)
    const remainHours = hours % 24
    return `${days}天${remainHours.toFixed(0)}小时`
  }
}
</script>

<style scoped lang="css">
.done-approval-container {
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

.filter-section {
  background: white;
  border-radius: 8px;
  padding: 20px;
  margin-bottom: 20px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.statistics-section {
  margin-bottom: 20px;
}

.stat-card {
  background: white;
  border-radius: 8px;
  padding: 20px;
  display: flex;
  align-items: center;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  height: 100px;
}

.stat-card .stat-icon {
  width: 60px;
  height: 60px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 16px;
  font-size: 28px;
}

.stat-card.total .stat-icon {
  background: #e6f7ff;
  color: #1890ff;
}

.stat-card.approved .stat-icon {
  background: #f6ffed;
  color: #52c41a;
}

.stat-card.rejected .stat-icon {
  background: #fff2f0;
  color: #ff4d4f;
}

.stat-card.average .stat-icon {
  background: #fff7e6;
  color: #fa8c16;
}

.stat-content .stat-number {
  font-size: 24px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 4px;
}

.stat-content .stat-label {
  font-size: 14px;
  color: #909399;
}

.approval-list-section {
  background: white;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.title-cell {
  display: flex;
  align-items: center;
  gap: 8px;
}

.title-text {
  flex: 1;
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.amount {
  color: #f56c6c;
  font-weight: 600;
}

.pagination-section {
  display: flex;
  justify-content: center;
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid #e4e7ed;
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .statistics-section .el-col {
    margin-bottom: 16px;
  }
}

@media (max-width: 768px) {
  .done-approval-container {
    padding: 10px;
  }
  
  .filter-section,
  .approval-list-section {
    padding: 15px;
  }
  
  .stat-card {
    flex-direction: column;
    text-align: center;
    height: auto;
  }
  
  .stat-card .stat-icon {
    margin-right: 0;
    margin-bottom: 12px;
  }
  
  :deep(.el-table) {
    font-size: 12px;
  }
  
  :deep(.el-table .el-button) {
    font-size: 12px;
    padding: 4px 8px;
  }
}

@media (max-width: 480px) {
  .filter-section {
    padding: 12px;
  }
  
  .filter-section .el-form-item {
    margin-bottom: 12px;
    width: 100%;
  }
  
  .filter-section .el-form-item__content {
    width: 100%;
  }
  
  .statistics-section .el-col {
    width: 50%;
    margin-bottom: 12px;
  }
  
  .approval-list-section {
    padding: 12px;
  }
}
</style>