<template>
  <div class="expense-detail-container">
    <!-- 页面标题和操作栏 -->
    <div class="page-header">
      <div class="header-left">
        <h2>费用申请单详情</h2>
        <p>申请单号: {{ expenseData.applyCode || '加载中...' }}</p>
      </div>
      <div class="header-right">
        <el-button @click="handleBack">
          <el-icon><arrow-left /></el-icon>
          返回列表
        </el-button>
        <el-button v-if="expenseData.status === 'DRAFT'" type="primary" @click="handleEdit">
          <el-icon><edit /></el-icon>
          编辑
        </el-button>
        <el-button v-if="expenseData.status === 'DRAFT'" type="success" @click="handleSubmit">
          <el-icon><check /></el-icon>
          提交审批
        </el-button>
        <el-button v-if="expenseData.status === 'DRAFT' || expenseData.status === 'PENDING'" type="danger" @click="handleCancel">
          <el-icon><close /></el-icon>
          撤销申请
        </el-button>
        <el-button type="info" @click="handlePrint">
          <el-icon><printer /></el-icon>
          打印
        </el-button>
      </div>
    </div>

    <div class="expense-detail-content">
      <!-- 申请单基本信息卡片 -->
      <div class="basic-info-card">
        <div class="card-header">
          <h3>申请单基本信息</h3>
          <el-tag :type="getStatusType(expenseData.status)" size="large">
            {{ getStatusText(expenseData.status) }}
          </el-tag>
        </div>
        
        <el-descriptions :column="3" border>
          <el-descriptions-item label="申请单号">{{ expenseData.applyCode }}</el-descriptions-item>
          <el-descriptions-item label="申请人">{{ expenseData.applicant }}</el-descriptions-item>
          <el-descriptions-item label="申请部门">{{ expenseData.departmentName }}</el-descriptions-item>
          <el-descriptions-item label="费用类型">{{ getExpenseTypeText(expenseData.expenseType) }}</el-descriptions-item>
          <el-descriptions-item label="申请日期">{{ expenseData.applyDate }}</el-descriptions-item>
          <el-descriptions-item label="紧急程度">
            <el-tag :type="getUrgencyType(expenseData.urgency)">{{ getUrgencyText(expenseData.urgency) }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="申请金额" :span="2">
            <span class="amount-text">¥{{ formatAmount(expenseData.totalAmount) }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="申请事由" :span="3">{{ expenseData.reason }}</el-descriptions-item>
        </el-descriptions>
      </div>

      <!-- 费用明细卡片 -->
      <div class="details-card">
        <div class="card-header">
          <h3>费用明细</h3>
          <span>共计 {{ expenseData.details?.length || 0 }} 项费用</span>
        </div>
        
        <el-table :data="expenseData.details" border style="width: 100%">
          <el-table-column type="index" label="序号" width="60" align="center" />
          <el-table-column prop="itemName" label="费用项目" min-width="150" />
          <el-table-column prop="specification" label="规格说明" min-width="120" />
          <el-table-column prop="quantity" label="数量" width="100" align="center" />
          <el-table-column prop="unitPrice" label="单价(元)" width="120" align="right">
            <template #default="{ row }">{{ formatAmount(row.unitPrice) }}</template>
          </el-table-column>
          <el-table-column prop="amount" label="金额(元)" width="120" align="right">
            <template #default="{ row }">
              <span class="detail-amount">{{ formatAmount(row.amount) }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="remark" label="备注" min-width="120" />
        </el-table>
        
        <div class="detail-summary">
          <div class="summary-row">
            <span>合计金额：</span>
            <span class="total-amount">¥{{ formatAmount(expenseData.totalAmount) }}</span>
          </div>
        </div>
      </div>

      <!-- 附件信息卡片 -->
      <div v-if="expenseData.attachments && expenseData.attachments.length > 0" class="attachments-card">
        <div class="card-header">
          <h3>附件信息</h3>
          <span>共计 {{ expenseData.attachments?.length || 0 }} 个附件</span>
        </div>
        
        <div class="attachments-list">
          <div v-for="attachment in expenseData.attachments" :key="attachment.id" class="attachment-item">
            <div class="attachment-info">
              <el-icon class="file-icon">
                <component :is="getFileIcon(attachment.fileType)" />
              </el-icon>
              <div class="file-details">
                <div class="file-name">{{ attachment.fileName }}</div>
                <div class="file-size">{{ formatFileSize(attachment.fileSize) }}</div>
              </div>
            </div>
            <div class="attachment-actions">
              <el-button type="primary" link size="small" @click="handlePreview(attachment)">预览</el-button>
              <el-button type="success" link size="small" @click="handleDownload(attachment)">下载</el-button>
            </div>
          </div>
        </div>
      </div>

      <!-- 审批流程卡片 -->
      <div class="approval-card">
        <div class="card-header">
          <h3>审批流程</h3>
          <div class="approval-status">
            <span>审批进度：</span>
            <span class="approval-level">{{ expenseData.approveLevel || '0/0' }}</span>
          </div>
        </div>
        
        <el-timeline>
          <el-timeline-item
            v-for="(step, index) in approvalSteps"
            :key="index"
            :type="getTimelineType(step.status)"
            :timestamp="step.timestamp"
          >
            <div class="timeline-content">
              <div class="step-header">
                <span class="step-title">{{ step.title }}</span>
                <el-tag :type="getStepStatusType(step.status)" size="small">{{ step.statusText }}</el-tag>
              </div>
              <div class="step-details">
                <span v-if="step.approver">审批人: {{ step.approver }}</span>
                <span v-if="step.comment">审批意见: {{ step.comment }}</span>
                <span v-if="step.current">当前步骤</span>
              </div>
            </div>
          </el-timeline-item>
        </el-timeline>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ArrowLeft, Edit, Check, Close, Printer, Document, Picture, VideoCamera, File } from '@element-plus/icons-vue'
import { showSuccess, showWarning, showError } from '@/utils/message''

// 路由和状态管理
const route = useRoute()
const router = useRouter()

// 申请单数据
const expenseData = ref<any>({
  id: '',
  applyCode: '',
  applicant: '',
  departmentName: '',
  expenseType: '',
  totalAmount: 0,
  applyDate: '',
  status: '',
  urgency: '',
  reason: '',
  details: [],
  attachments: [],
  approveLevel: '',
  currentApprover: ''
})

// 审批步骤数据
const approvalSteps = ref([
  {
    title: '部门经理审批',
    status: 'COMPLETED',
    statusText: '已批准',
    approver: '张经理',
    comment: '符合规定，同意申请',
    timestamp: '2024-01-15 11:30:25'
  },
  {
    title: '财务部审批',
    status: 'PENDING',
    statusText: '审批中',
    approver: '李总监',
    comment: '',
    timestamp: '当前步骤',
    current: true
  },
  {
    title: '最终审批',
    status: 'WAITING',
    statusText: '待审批',
    approver: '王总',
    comment: '',
    timestamp: '等待中'
  }
])

// 生命周期
onMounted(() => {
  loadExpenseDetail()
})

// 方法定义
const loadExpenseDetail = async () => {
  const expenseId = route.params.id
  if (!expenseId) {
    showError('申请单ID不存在')
    router.push('/expense/list')
    return
  }

  try {
    // TODO: 调用API获取申请单详情
    await new Promise(resolve => setTimeout(resolve, 500))
    
    // 模拟数据
    expenseData.value = {
      id: expenseId,
      applyCode: 'EXP2024010001',
      applicant: '张经理',
      departmentName: '技术部',
      expenseType: 'TRAVEL',
      totalAmount: 3850.50,
      applyDate: '2024-01-15 10:30:25',
      status: 'PENDING',
      urgency: 'HIGH',
      reason: '赴北京参加技术峰会，包括交通、住宿、会议注册等费用',
      details: [
        {
          itemName: '往返机票',
          specification: '经济舱',
          quantity: 1,
          unitPrice: 1500,
          amount: 1500,
          remark: '北京-上海往返'
        },
        {
          itemName: '酒店住宿',
          specification: '商务标间',
          quantity: 3,
          unitPrice: 450,
          amount: 1350,
          remark: '3晚住宿'
        },
        {
          itemName: '会议注册费',
          specification: '标准注册',
          quantity: 1,
          unitPrice: 1000,
          amount: 1000,
          remark: '技术峰会注册费'
        }
      ],
      attachments: [
        {
          id: '1',
          fileName: '会议邀请函.pdf',
          fileType: 'pdf',
          fileSize: 245760
        },
        {
          id: '2',
          fileName: '行程安排.docx',
          fileType: 'docx',
          fileSize: 15360
        }
      ],
      approveLevel: '1/3',
      currentApprover: '李总监'
    }
  } catch (error) {
    console.error('加载申请单详情失败:', error)
    showError('加载申请单详情失败')
    router.push('/expense/list')
  }
}

const handleBack = () => {
  router.push('/expense/list')
}

const handleEdit = () => {
  router.push(`/expense/edit/${expenseData.value.id}`)
}

const handleSubmit = async () => {
  try {
    await ElMessageBox.confirm(
      '确定要提交此申请单吗？提交后将进入审批流程。',
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
    loadExpenseDetail() // 重新加载详情
  } catch (error) {
    // 用户取消操作
  }
}

const handleCancel = async () => {
  try {
    await ElMessageBox.confirm(
      '确定要撤销此申请单吗？撤销后审批流程将终止。',
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
    loadExpenseDetail() // 重新加载详情
  } catch (error) {
    // 用户取消操作
  }
}

const handlePrint = () => {
  showSuccess('打印功能待实现')
}

const handlePreview = (attachment: any) => {
  showSuccess(`预览附件: ${attachment.fileName}`)
}

const handleDownload = (attachment: any) => {
  showSuccess(`下载附件: ${attachment.fileName}`)
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

const getExpenseTypeText = (type: string) => {
  const typeMap: Record<string, string> = {
    'TRAVEL': '差旅报销',
    'PURCHASE': '采购申请',
    'GENERAL': '常规报销',
    'MEETING': '会议费用'
  }
  return typeMap[type] || '未知'
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

const getFileIcon = (fileType: string) => {
  const iconMap: Record<string, any> = {
    'pdf': Document,
    'doc': Document,
    'docx': Document,
    'jpg': Picture,
    'png': Picture,
    'gif': Picture,
    'mp4': VideoCamera,
    'avi': VideoCamera
  }
  return iconMap[fileType] || File
}

const formatFileSize = (bytes: number) => {
  if (bytes === 0) return '0 Bytes'
  const k = 1024
  const sizes = ['Bytes', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
}

const getTimelineType = (status: string) => {
  const typeMap: Record<string, string> = {
    'COMPLETED': 'success',
    'PENDING': 'warning',
    'WAITING': 'info'
  }
  return typeMap[status] || 'info'
}

const getStepStatusType = (status: string) => {
  const typeMap: Record<string, string> = {
    'COMPLETED': 'success',
    'PENDING': 'warning',
    'WAITING': 'info'
  }
  return typeMap[status] || 'info'
}
</script>

<style scoped lang="css">
.expense-detail-container {
  padding: 20px;
  background: #f5f7fa;
  min-height: calc(100vh - 60px);
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding: 16px 20px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.header-left h2 {
  margin: 0 0 4px 0;
  color: #303133;
  font-size: 20px;
}

.header-left p {
  margin: 0;
  color: #909399;
  font-size: 14px;
}

.header-right {
  display: flex;
  gap: 12px;
}

.expense-detail-content {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.basic-info-card,
.details-card,
.attachments-card,
.approval-card {
  background: white;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 16px;
  border-bottom: 1px solid #e4e7ed;
}

.card-header h3 {
  margin: 0;
  color: #303133;
  font-size: 16px;
}

.amount-text {
  font-size: 18px;
  font-weight: 600;
  color: #409eff;
}

.detail-amount {
  font-weight: 600;
  color: #67c23a;
}

.detail-summary {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
  padding: 16px;
  background: #f8f9fa;
  border-radius: 4px;
}

.summary-row {
  display: flex;
  align-items: center;
  gap: 16px;
}

.total-amount {
  font-size: 20px;
  font-weight: 600;
  color: #409eff;
}

.attachments-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.attachment-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  transition: border-color 0.3s;
}

.attachment-item:hover {
  border-color: #409eff;
}

.attachment-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.file-icon {
  font-size: 24px;
  color: #409eff;
}

.file-details {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.file-name {
  font-weight: 500;
  color: #303133;
}

.file-size {
  font-size: 12px;
  color: #909399;
}

.attachment-actions {
  display: flex;
  gap: 8px;
}

.approval-status {
  display: flex;
  align-items: center;
  gap: 8px;
}

.approval-level {
  font-weight: 600;
  color: #409eff;
}

.timeline-content {
  padding: 8px 0;
}

.step-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.step-title {
  font-weight: 500;
  color: #303133;
}

.step-details {
  display: flex;
  flex-direction: column;
  gap: 4px;
  color: #606266;
  font-size: 14px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .expense-detail-container {
    padding: 10px;
  }
  
  .page-header {
    flex-direction: column;
    gap: 16px;
    align-items: stretch;
  }
  
  .header-right {
    justify-content: center;
    flex-wrap: wrap;
  }
  
  .card-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }
  
  .attachment-item {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }
  
  .attachment-actions {
    align-self: flex-end;
  }
}
</style>