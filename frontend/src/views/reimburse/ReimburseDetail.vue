<template>
  <div class="reimburse-detail-container">
    <!-- 页面标题和操作 -->
    <div class="page-header">
      <div class="header-left">
        <h2>费用报销单详情</h2>
        <p>报销单号: {{ reimburseData.reimburseCode || '加载中...' }}</p>
      </div>
      <div class="header-actions">
        <el-button @click="handleBack">
          <el-icon><arrow-left /></el-icon>
          返回列表
        </el-button>
        <el-button v-if="canEdit" type="warning" @click="handleEdit">
          <el-icon><edit /></el-icon>
          编辑
        </el-button>
        <el-button v-if="canSubmit" type="success" @click="handleSubmit">
          <el-icon><send /></el-icon>
          提交审批
        </el-button>
        <el-button v-if="canPrint" type="primary" @click="handlePrint">
          <el-icon><printer /></el-icon>
          打印
        </el-button>
        <el-button v-if="canSettle" type="primary" @click="handleSettle">
          <el-icon><check /></el-icon>
          借款核销
        </el-button>
      </div>
    </div>

    <div class="reimburse-detail-content">
      <!-- 基本信息 -->
      <div class="basic-info-section">
        <div class="section-header">
          <h3>基本信息</h3>
          <el-tag :type="getStatusType(reimburseData.status)" effect="light">
            {{ getStatusText(reimburseData.status) }}
          </el-tag>
        </div>
        
        <div class="info-grid">
          <div class="info-item">
            <span class="label">报销单号：</span>
            <span class="value">{{ reimburseData.reimburseCode }}</span>
          </div>
          <div class="info-item">
            <span class="label">报销部门：</span>
            <span class="value">{{ reimburseData.departmentName }}</span>
          </div>
          <div class="info-item">
            <span class="label">报销人：</span>
            <span class="value">{{ reimburseData.reimbursePerson }}</span>
          </div>
          <div class="info-item">
            <span class="label">报销日期：</span>
            <span class="value">{{ formatDate(reimburseData.reimburseDate) }}</span>
          </div>
          <div class="info-item">
            <span class="label">报销类型：</span>
            <span class="value">{{ reimburseData.reimburseTypeName }}</span>
          </div>
          <div class="info-item">
            <span class="label">紧急程度：</span>
            <span class="value">{{ getUrgencyText(reimburseData.urgency) }}</span>
          </div>
          <div class="info-item full-width">
            <span class="label">报销事由：</span>
            <span class="value">{{ reimburseData.reason }}</span>
          </div>
          <div class="info-item">
            <span class="label">关联申请单：</span>
            <span class="value">{{ reimburseData.relateApplyCode || '无' }}</span>
          </div>
          <div class="info-item">
            <span class="label">预借款：</span>
            <span class="value">
              <el-tag v-if="reimburseData.isAdvance" type="warning" size="small">是</el-tag>
              <el-tag v-else type="info" size="small">否</el-tag>
            </span>
          </div>
          <div v-if="reimburseData.isAdvance" class="info-item">
            <span class="label">预借款金额：</span>
            <span class="value amount">¥{{ formatAmount(reimburseData.advanceAmount) }}</span>
          </div>
        </div>
      </div>

      <!-- 费用明细 -->
      <div class="expense-details-section">
        <div class="section-header">
          <h3>费用明细</h3>
        </div>
        
        <div class="details-table">
          <el-table :data="expenseDetails" border style="width: 100%">
            <el-table-column type="index" label="序号" width="60" align="center" />
            <el-table-column prop="itemName" label="费用项目" min-width="150" />
            <el-table-column prop="specification" label="规格说明" min-width="120" />
            <el-table-column prop="quantity" label="数量" width="100" align="center" />
            <el-table-column prop="unitPrice" label="单价(元)" width="120" align="right">
              <template #default="{ row }">
                {{ formatAmount(row.unitPrice) }}
              </template>
            </el-table-column>
            <el-table-column prop="amount" label="金额(元)" width="120" align="right">
              <template #default="{ row }">
                {{ formatAmount(row.amount) }}
              </template>
            </el-table-column>
            <el-table-column prop="remark" label="备注" min-width="120" />
          </el-table>
        </div>
        
        <!-- 费用汇总 -->
        <div class="expense-summary">
          <div class="summary-row">
            <span>合计金额：</span>
            <span class="total-amount">¥{{ formatAmount(reimburseData.totalAmount) }}</span>
          </div>
          <div v-if="reimburseData.isAdvance" class="summary-row">
            <span>预借款金额：</span>
            <span class="advance-amount">¥{{ formatAmount(reimburseData.advanceAmount) }}</span>
          </div>
          <div v-if="reimburseData.isAdvance" class="summary-row">
            <span>应报销金额：</span>
            <span class="final-amount">¥{{ formatAmount(reimburseData.totalAmount - reimburseData.advanceAmount) }}</span>
          </div>
        </div>
      </div>

      <!-- 发票信息 -->
      <div class="invoice-section">
        <div class="section-header">
          <h3>发票信息</h3>
        </div>
        
        <div class="invoice-list">
          <div v-for="invoice in invoiceList" :key="invoice.id" class="invoice-item">
            <div class="invoice-info">
              <div class="invoice-header">
                <span class="invoice-type">{{ getInvoiceTypeText(invoice.type) }}</span>
                <span class="invoice-amount">¥{{ formatAmount(invoice.amount) }}</span>
              </div>
              <div class="invoice-details">
                <span>发票代码：{{ invoice.invoiceCode }}</span>
                <span>开票日期：{{ invoice.issueDate }}</span>
                <span>开票方：{{ invoice.issuer }}</span>
              </div>
              <div class="invoice-actions">
                <el-button type="primary" size="small" @click="handlePreviewInvoice(invoice)">预览</el-button>
                <el-button type="success" size="small" @click="handleDownloadInvoice(invoice)">下载</el-button>
              </div>
            </div>
          </div>
          
          <div v-if="invoiceList.length === 0" class="no-invoice">
            <el-empty description="暂无发票信息" :image-size="80" />
          </div>
        </div>
      </div>

      <!-- 审批流程 -->
      <div class="approval-section">
        <div class="section-header">
          <h3>审批流程</h3>
        </div>
        
        <div class="approval-timeline">
          <el-timeline>
            <el-timeline-item
              v-for="(record, index) in approvalRecords"
              :key="record.id"
              :timestamp="formatDateTime(record.approvalTime)"
              :type="getRecordType(record.status)"
              :color="getRecordColor(record.status)"
            >
              <div class="record-info">
                <div class="record-header">
                  <span class="record-node">{{ record.nodeName }}</span>
                  <el-tag :type="getStatusType(record.status)" size="small">{{ getStatusText(record.status) }}</el-tag>
                </div>
                <div class="record-details">
                  <span>审批人：{{ record.approverName }}</span>
                  <span v-if="record.opinion">审批意见：{{ record.opinion }}</span>
                </div>
                <div v-if="record.attachment" class="record-attachments">
                  <el-button type="primary" size="small" link @click="handleViewAttachment(record.attachment)">
                    查看附件
                  </el-button>
                </div>
              </div>
            </el-timeline-item>
          </el-timeline>
          
          <div v-if="approvalRecords.length === 0" class="no-approval">
            <el-empty description="暂无审批记录" :image-size="80" />
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ArrowLeft, Edit, Send, Printer, Check } from '@element-plus/icons-vue'
import { showSuccess, showError, showConfirm } from '@/utils/message''

// 路由和状态管理
const route = useRoute()
const router = useRouter()

// 报销单数据
const reimburseData = ref<any>({})

// 费用明细数据
const expenseDetails = ref([])

// 发票列表数据
const invoiceList = ref([])

// 审批记录数据
const approvalRecords = ref([])

// 状态类型映射
const statusTypeMap = {
  DRAFT: '',
  PENDING: 'warning',
  PROCESSING: 'primary',
  APPROVED: 'success',
  REJECTED: 'danger',
  COMPLETED: 'info'
}

// 状态文本映射
const statusTextMap = {
  DRAFT: '草稿',
  PENDING: '待审批',
  PROCESSING: '审批中',
  APPROVED: '已批准',
  REJECTED: '已驳回',
  COMPLETED: '已完成'
}

// 紧急程度文本映射
const urgencyTextMap = {
  LOW: '普通',
  MEDIUM: '较急',
  HIGH: '紧急'
}

// 发票类型映射
const invoiceTypeMap = {
  COMMON: '普通发票',
  SPECIAL: '专用发票',
  ELECTRONIC: '电子发票',
  RAILWAY: '火车票',
  AIRLINE: '机票'
}

// 计算属性
const canEdit = computed(() => {
  return reimburseData.value.status === 'DRAFT'
})

const canSubmit = computed(() => {
  return reimburseData.value.status === 'DRAFT'
})

const canPrint = computed(() => {
  return reimburseData.value.status !== 'DRAFT'
})

const canSettle = computed(() => {
  return reimburseData.value.isAdvance && reimburseData.value.status === 'APPROVED'
})

// 生命周期
onMounted(() => {
  loadReimburseData()
})

// 方法定义
const loadReimburseData = async () => {
  const reimburseId = route.params.id
  if (!reimburseId) {
    showError('报销单ID不存在')
    router.push('/reimburse/list')
    return
  }

  try {
    // TODO: 调用API获取数据
    await new Promise(resolve => setTimeout(resolve, 800))
    
    // 模拟数据
    reimburseData.value = {
      id: reimburseId,
      reimburseCode: 'REIM2024010001',
      reimbursePerson: '张经理',
      departmentName: '技术部',
      reimburseDate: '2024-01-15',
      reimburseTypeId: '1',
      reimburseTypeName: '差旅报销',
      totalAmount: 1850.00,
      status: 'APPROVED',
      urgency: 'LOW',
      reason: '出差北京参加技术会议，包括交通、住宿、餐饮费用',
      relateApplyCode: 'EXP2024010001',
      isAdvance: true,
      advanceAmount: 2000.00,
      createTime: '2024-01-15 10:30:00',
      updateTime: '2024-01-16 14:20:00'
    }
    
    expenseDetails.value = [
      {
        id: 1,
        itemName: '高铁票',
        specification: '一等座',
        quantity: 2,
        unitPrice: 553.00,
        amount: 1106.00,
        remark: '往返车票'
      },
      {
        id: 2,
        itemName: '住宿费',
        specification: '标准间',
        quantity: 2,
        unitPrice: 300.00,
        amount: 600.00,
        remark: '两晚住宿'
      },
      {
        id: 3,
        itemName: '餐饮费',
        specification: '日常用餐',
        quantity: 3,
        unitPrice: 48.00,
        amount: 144.00,
        remark: '三顿正餐'
      }
    ]
    
    invoiceList.value = [
      {
        id: '1',
        type: 'RAILWAY',
        amount: 1106.00,
        invoiceCode: '123456789012',
        issueDate: '2024-01-10',
        issuer: '中国铁路'
      },
      {
        id: '2',
        type: 'ELECTRONIC',
        amount: 600.00,
        invoiceCode: '567890123456',
        issueDate: '2024-01-12',
        issuer: '北京大酒店'
      }
    ]
    
    approvalRecords.value = [
      {
        id: '1',
        nodeName: '部门经理审批',
        approverName: '王经理',
        status: 'APPROVED',
        opinion: '符合公司差旅标准，同意报销',
        approvalTime: '2024-01-15 14:30:00'
      },
      {
        id: '2',
        nodeName: '财务审批',
        approverName: '李会计',
        status: 'APPROVED',
        opinion: '发票齐全，金额正确',
        approvalTime: '2024-01-16 09:15:00'
      },
      {
        id: '3',
        nodeName: '总经理审批',
        approverName: '张总',
        status: 'APPROVED',
        opinion: '同意',
        approvalTime: '2024-01-16 14:20:00'
      }
    ]
  } catch (error) {
    console.error('加载报销单数据失败:', error)
    showError('加载报销单数据失败')
    router.push('/reimburse/list')
  }
}

const handleBack = () => {
  router.push('/reimburse/list')
}

const handleEdit = () => {
  router.push(`/reimburse/edit/${reimburseData.value.id}`)
}

const handleSubmit = async () => {
  try {
    await showConfirm('确定要提交此报销单吗？提交后将进入审批流程。')
    
    // TODO: 调用API提交
    await new Promise(resolve => setTimeout(resolve, 500))
    showSuccess('报销单已成功提交审批')
    
    // 重新加载数据
    loadReimburseData()
  } catch (error) {
    // 用户取消操作
  }
}

const handlePrint = () => {
  showSuccess('打印功能开发中，即将上线')
}

const handleSettle = () => {
  showSuccess('借款核销功能开发中，即将上线')
}

const handlePreviewInvoice = (invoice: any) => {
  showSuccess(`预览发票: ${invoice.invoiceCode}`)
}

const handleDownloadInvoice = (invoice: any) => {
  showSuccess(`下载发票: ${invoice.invoiceCode}`)
}

const handleViewAttachment = (attachment: any) => {
  showSuccess('查看附件功能开发中，即将上线')
}

const getStatusType = (status: string) => {
  return statusTypeMap[status] || ''
}

const getStatusText = (status: string) => {
  return statusTextMap[status] || '未知状态'
}

const getUrgencyText = (urgency: string) => {
  return urgencyTextMap[urgency] || '普通'
}

const getInvoiceTypeText = (type: string) => {
  return invoiceTypeMap[type] || '未知类型'
}

const getRecordType = (status: string) => {
  if (status === 'APPROVED') return 'success'
  if (status === 'REJECTED') return 'danger'
  return 'primary'
}

const getRecordColor = (status: string) => {
  if (status === 'APPROVED') return '#0bbd87'
  if (status === 'REJECTED') return '#f56c6c'
  return '#e4e7ed'
}

const formatDate = (date: string) => {
  if (!date) return '-';
  return date;
}

const formatDateTime = (dateTime: string) => {
  if (!dateTime) return '-';
  return dateTime;
}

const formatAmount = (amount: number) => {
  return amount?.toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 }) || '0.00'
}
</script>

<style scoped lang="css">
.reimburse-detail-container {
  padding: 20px;
  background: #f5f7fa;
  min-height: calc(100vh - 60px);
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding: 20px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
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

.header-actions {
  display: flex;
  gap: 12px;
}

.reimburse-detail-content {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.basic-info-section,
.expense-details-section,
.invoice-section,
.approval-section {
  background: white;
  border-radius: 8px;
  padding: 20px;
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

.info-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 16px;
}

.info-item {
  display: flex;
  align-items: flex-start;
}

.info-item.full-width {
  grid-column: 1 / -1;
}

.info-item .label {
  min-width: 100px;
  font-weight: 500;
  color: #606266;
}

.info-item .value {
  flex: 1;
  color: #303133;
}

.info-item .amount {
  font-weight: 600;
  color: #f56c6c;
}

.details-table {
  margin-bottom: 20px;
}

.expense-summary {
  background: #f8f9fa;
  border-radius: 4px;
  padding: 16px;
}

.summary-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.summary-row:last-child {
  margin-bottom: 0;
}

.total-amount {
  font-size: 18px;
  font-weight: 600;
  color: #409eff;
}

.advance-amount {
  font-size: 16px;
  font-weight: 500;
  color: #e6a23c;
}

.final-amount {
  font-size: 18px;
  font-weight: 600;
  color: #67c23a;
}

.invoice-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.invoice-item {
  border: 1px solid #e4e7ed;
  border-radius: 6px;
  padding: 16px;
  background: #f8f9fa;
}

.invoice-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.invoice-header {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.invoice-type {
  font-weight: 600;
  color: #303133;
}

.invoice-amount {
  font-size: 16px;
  font-weight: 600;
  color: #f56c6c;
}

.invoice-details {
  display: flex;
  gap: 16px;
  font-size: 12px;
  color: #909399;
}

.invoice-actions {
  display: flex;
  gap: 8px;
}

.no-invoice,
.no-approval {
  padding: 40px 0;
}

.approval-timeline {
  max-width: 800px;
}

.record-info {
  padding: 12px;
  background: #f8f9fa;
  border-radius: 4px;
}

.record-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.record-node {
  font-weight: 600;
  color: #303133;
}

.record-details {
  display: flex;
  flex-direction: column;
  gap: 4px;
  font-size: 14px;
  color: #606266;
  margin-bottom: 8px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .reimburse-detail-container {
    padding: 10px;
  }
  
  .page-header {
    flex-direction: column;
    gap: 16px;
    align-items: stretch;
    padding: 15px;
  }
  
  .header-actions {
    justify-content: center;
  }
  
  .basic-info-section,
  .expense-details-section,
  .invoice-section,
  .approval-section {
    padding: 15px;
  }
  
  .info-grid {
    grid-template-columns: 1fr;
    gap: 12px;
  }
  
  .invoice-info {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }
  
  .invoice-details {
    flex-direction: column;
    gap: 4px;
  }
}
</style>