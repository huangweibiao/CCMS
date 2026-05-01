<template>
  <div class="todo-approval-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2>待办审批</h2>
      <p>处理待您审批的费用申请和报销单</p>
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
        <el-form-item label="紧急程度">
          <el-select v-model="filterForm.urgency" placeholder="选择紧急程度" clearable>
            <el-option label="普通" value="LOW" />
            <el-option label="较急" value="MEDIUM" />
            <el-option label="紧急" value="HIGH" />
          </el-select>
        </el-form-item>
        <el-form-item label="申请时间">
          <el-date-picker
            v-model="filterForm.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
          />
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
        </el-form-item>
      </el-form>
    </div>

    <!-- 待办列表 -->
    <div class="approval-list-section">
      <div class="section-header">
        <h3>待办事项</h3>
        <div class="statistics">
          <span class="stat-item">
            待办总数：<span class="count">{{ pagination.total }}</span>
          </span>
          <span class="stat-item">
            今日新增：<span class="count today">{{ todayCount }}</span>
          </span>
        </div>
      </div>

      <div class="approval-cards">
        <div v-for="item in approvalList" :key="item.id" class="approval-card">
          <div class="card-header">
            <div class="header-left">
              <h4 class="approval-title">
                <el-tag :type="getTypeColor(item.type)" size="small">
                  {{ getTypeText(item.type) }}
                </el-tag>
                {{ item.title }}
              </h4>
              <div class="meta-info">
                <span class="applicant">申请人：{{ item.applicant }}</span>
                <span class="department">部门：{{ item.department }}</span>
                <span class="time">申请时间：{{ formatDateTime(item.applyTime) }}</span>
              </div>
            </div>
            <div class="header-right">
              <div class="urgency-level" :class="item.urgency">
                <el-icon v-if="item.urgency === 'HIGH'"><warning /></el-icon>
                <el-icon v-else-if="item.urgency === 'MEDIUM'"><clock /></el-icon>
                {{ getUrgencyText(item.urgency) }}
              </div>
              <div class="amount">¥{{ formatAmount(item.amount) }}</div>
            </div>
          </div>

          <div class="card-content">
            <p class="reason">{{ item.reason }}</p>
            
            <div class="approval-info">
              <div class="info-row">
                <span class="label">当前节点：</span>
                <span class="value">{{ item.currentNode }}</span>
              </div>
              <div class="info-row">
                <span class="label">到达时间：</span>
                <span class="value">{{ formatDateTime(item.reachTime) }}</span>
              </div>
              <div class="info-row">
                <span class="label">审批时限：</span>
                <span class="value">{{ formatDeadline(item.deadline) }}</span>
              </div>
            </div>
          </div>

          <div class="card-actions">
            <el-button type="primary" size="small" @click="handleApprovalDetail(item)">
              <el-icon><view /></el-icon>
              详情
            </el-button>
            <el-button type="success" size="small" @click="handleApprove(item)">
              <el-icon><check /></el-icon>
              同意
            </el-button>
            <el-button type="danger" size="small" @click="handleReject(item)">
              <el-icon><close /></el-icon>
              驳回
            </el-button>
            <el-button type="warning" size="small" @click="handleTransfer(item)">
              <el-icon><share /></el-icon>
              转审
            </el-button>
            <el-button type="info" size="small" @click="handleRemind(item)">
              <el-icon><bell /></el-icon>
              催办
            </el-button>
          </div>
        </div>

        <div v-if="approvalList.length === 0" class="no-data">
          <el-empty description="暂无待办审批" :image-size="120" />
        </div>
      </div>

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

    <!-- 批量操作 -->
    <div class="batch-action-section" v-if="selectedItems.length > 0">
      <div class="batch-info">
        <span>已选择 {{ selectedItems.length }} 项</span>
      </div>
      <div class="batch-actions">
        <el-button type="success" size="small" @click="handleBatchApprove">
          <el-icon><check /></el-icon>
          批量同意
        </el-button>
        <el-dropdown @command="handleBatchCommand">
          <el-button type="primary" size="small">
            更多操作<el-icon class="el-icon--right"><arrow-down /></el-icon>
          </el-button>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="transfer">批量转审</el-dropdown-item>
              <el-dropdown-item command="remind">批量催办</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { Search, Refresh, View, Check, Close, Share, Bell, Warning, Clock, ArrowDown } from '@element-plus/icons-vue'
import { showSuccess, showError, showConfirm } from '@/utils/message''

// 路由管理
const router = useRouter()

// 筛选表单
const filterForm = reactive({
  approvalType: '',
  urgency: '',
  dateRange: []
})

// 分页信息
const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

// 待办列表数据
const approvalList = ref<any[]>([])

// 选中项
const selectedItems = ref<any[]>([])

// 类型映射
const typeMap = {
  EXPENSE_APPLY: { text: '费用申请', color: 'primary' },
  EXPENSE_REIMBURSE: { text: '费用报销', color: 'success' },
  BUDGET_APPLY: { text: '预算申请', color: 'warning' }
}

// 紧急程度映射
const urgencyMap = {
  LOW: '普通',
  MEDIUM: '较急',
  HIGH: '紧急'
}

// 计算属性
const todayCount = computed(() => {
  const today = new Date().toISOString().split('T')[0]
  return approvalList.value.filter(item => 
    item.applyTime && item.applyTime.startsWith(today)
  ).length
})

// 生命周期
onMounted(() => {
  loadApprovalList()
})

// 方法定义
const loadApprovalList = async () => {
  try {
    // TODO: 调用API获取待办列表
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
        reason: '前往北京参加重要项目会议，包括机票、住宿、交通等费用',
        applyTime: '2024-01-15 10:30:00',
        currentNode: '部门经理审批',
        reachTime: '2024-01-15 14:20:00',
        deadline: '2024-01-17 18:00:00',
        urgency: 'HIGH'
      },
      {
        id: '2',
        type: 'EXPENSE_REIMBURSE',
        title: '业务招待费用报销',
        applicant: '李总监',
        department: '市场部',
        amount: 3200.00,
        reason: '客户招待费用，包括餐厅用餐、礼品购买等',
        applyTime: '2024-01-14 16:45:00',
        currentNode: '财务审批',
        reachTime: '2024-01-15 09:15:00',
        deadline: '2024-01-18 12:00:00',
        urgency: 'MEDIUM'
      },
      {
        id: '3',
        type: 'EXPENSE_REIMBURSE',
        title: '办公用品采购报销',
        applicant: '王助理',
        department: '行政部',
        amount: 1200.50,
        reason: '采购办公文具、打印耗材等日常办公用品',
        applyTime: '2024-01-13 11:20:00',
        currentNode: '部门主管审批',
        reachTime: '2024-01-15 08:30:00',
        deadline: '2024-01-20 18:00:00',
        urgency: 'LOW'
      }
    ]
    
    pagination.total = approvalList.value.length
    
  } catch (error) {
    console.error('加载待办列表失败:', error)
    showError('加载待办列表失败')
  }
}

const handleSearch = () => {
  pagination.current = 1
  loadApprovalList()
}

const handleReset = () => {
  Object.assign(filterForm, {
    approvalType: '',
    urgency: '',
    dateRange: []
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

const handleApprovalDetail = (item: any) => {
  console.log('查看审批详情:', item)
  // TODO: 跳转到详情页面
  if (item.type === 'EXPENSE_APPLY') {
    router.push(`/expense/detail/${item.id}`)
  } else if (item.type === 'EXPENSE_REIMBURSE') {
    router.push(`/reimburse/detail/${item.id}`)
  }
}

const handleApprove = async (item: any) => {
  try {
    await showConfirm(`确定同意"${item.title}"吗？`)
    
    // TODO: 调用API同意审批
    await new Promise(resolve => setTimeout(resolve, 300))
    
    showSuccess('审批已同意')
    
    // 重新加载列表
    loadApprovalList()
    
  } catch (error) {
    // 用户取消操作
  }
}

const handleReject = async (item: any) => {
  // TODO: 打开驳回模态框
  showSuccess('驳回功能开发中，即将上线')
}

const handleTransfer = async (item: any) => {
  // TODO: 打开转审模态框
  showSuccess('转审功能开发中，即将上线')
}

const handleRemind = async (item: any) => {
  try {
    await showConfirm(`确定要向申请人催办"${item.title}"吗？`)
    
    // TODO: 调用催办API
    await new Promise(resolve => setTimeout(resolve, 300))
    
    showSuccess('催办通知已发送')
    
  } catch (error) {
    // 用户取消操作
  }
}

const handleBatchApprove = async () => {
  if (selectedItems.value.length === 0) {
    showError('请选择要审批的项目')
    return
  }

  try {
    await showConfirm(`确定批量同意选中的 ${selectedItems.value.length} 个项目吗？`)
    
    // TODO: 调用批量同意API
    await new Promise(resolve => setTimeout(resolve, 500))
    
    showSuccess('批量审批完成')
    selectedItems.value = []
    loadApprovalList()
    
  } catch (error) {
    // 用户取消操作
  }
}

const handleBatchCommand = (command: string) => {
  if (selectedItems.value.length === 0) {
    showError('请选择要操作的项目')
    return
  }

  switch (command) {
    case 'transfer':
      showSuccess('批量转审功能开发中')
      break
    case 'remind':
      showSuccess('批量催办功能开发中')
      break
  }
}

const getTypeText = (type: string) => {
  return typeMap[type]?.text || '未知类型'
}

const getTypeColor = (type: string) => {
  return typeMap[type]?.color || 'info'
}

const getUrgencyText = (urgency: string) => {
  return urgencyMap[urgency] || '普通'
}

const formatAmount = (amount: number) => {
  return amount?.toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 }) || '0.00'
}

const formatDateTime = (dateTime: string) => {
  if (!dateTime) return '-'
  return dateTime.replace('T', ' ').substring(0, 16)
}

const formatDeadline = (deadline: string) => {
  if (!deadline) return '无限制'
  return formatDateTime(deadline)
}
</script>

<style scoped lang="css">
.todo-approval-container {
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

.approval-list-section {
  background: white;
  border-radius: 8px;
  padding: 20px;
  margin-bottom: 20px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 16px;
  border-bottom: 1px solid #e4e7ed;
}

.section-header h3 {
  margin: 0;
  color: #303133;
  font-size: 16px;
}

.statistics {
  display: flex;
  gap: 20px;
}

.stat-item {
  font-size: 14px;
  color: #606266;
}

.count {
  font-weight: 600;
  color: #409eff;
}

.today {
  color: #67c23a;
}

.approval-cards {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.approval-card {
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  padding: 20px;
  background: white;
  transition: all 0.3s ease;
}

.approval-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  transform: translateY(-2px);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 16px;
}

.header-left {
  flex: 1;
}

.approval-title {
  display: flex;
  align-items: center;
  gap: 12px;
  margin: 0 0 12px 0;
  font-size: 16px;
  color: #303133;
}

.meta-info {
  display: flex;
  gap: 20px;
  font-size: 12px;
  color: #909399;
}

.header-right {
  text-align: right;
  min-width: 120px;
}

.urgency-level {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
  margin-bottom: 8px;
}

.urgency-level.LOW {
  background: #f4f4f5;
  color: #909399;
}

.urgency-level.MEDIUM {
  background: #fdf6ec;
  color: #e6a23c;
}

.urgency-level.HIGH {
  background: #fef0f0;
  color: #f56c6c;
}

.amount {
  font-size: 18px;
  font-weight: 600;
  color: #f56c6c;
}

.card-content {
  margin-bottom: 16px;
}

.reason {
  margin: 0 0 16px 0;
  color: #606266;
  line-height: 1.6;
}

.approval-info {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.info-row {
  display: flex;
  align-items: center;
  font-size: 13px;
}

.info-row .label {
  min-width: 80px;
  color: #909399;
}

.info-row .value {
  color: #303133;
  font-weight: 500;
}

.card-actions {
  display: flex;
  gap: 8px;
  justify-content: flex-end;
}

.no-data {
  padding: 60px 0;
}

.pagination-section {
  display: flex;
  justify-content: center;
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid #e4e7ed;
}

.batch-action-section {
  position: fixed;
  bottom: 0;
  left: 200px; /* 侧边栏宽度 */
  right: 0;
  background: white;
  border-top: 1px solid #e4e7ed;
  padding: 16px 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-shadow: 0 -2px 8px rgba(0, 0, 0, 0.1);
  z-index: 1000;
}

.batch-info {
  color: #606266;
  font-size: 14px;
}

.batch-actions {
  display: flex;
  gap: 12px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .todo-approval-container {
    padding: 10px;
  }
  
  .filter-section,
  .approval-list-section {
    padding: 15px;
  }
  
  .section-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }
  
  .card-header {
    flex-direction: column;
    gap: 12px;
  }
  
  .header-right {
    text-align: left;
  }
  
  .meta-info {
    flex-direction: column;
    gap: 6px;
  }
  
  .card-actions {
    flex-wrap: wrap;
    justify-content: center;
  }
  
  .batch-action-section {
    left: 0;
    flex-direction: column;
    gap: 12px;
  }
}
</style>