<template>
  <div class="approval-detail-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <el-page-header @back="goBack">
        <template #content>
          <div class="header-content">
            <h2>审批详情</h2>
            <div class="header-subtitle">
              <el-tag :type="getTypeColor(detailData.type)" size="small">
                {{ getTypeText(detailData.type) }}
              </el-tag>
              <span class="application-id">申请编号：{{ detailData.id || '-' }}</span>
            </div>
          </div>
        </template>
      </el-page-header>
      
      <div class="header-actions" v-if="!isReadonly">
        <ApprovalActions 
          :approval="detailData" 
          @approve="handleApprove"
          @reject="handleReject"
          @transfer="handleTransfer"
        />
      </div>
    </div>

    <!-- 基本信息 -->
    <div class="section">
      <div class="section-header">
        <h3>基本信息</h3>
      </div>
      <div class="section-content">
        <el-descriptions :column="3" border>
          <el-descriptions-item label="申请标题">{{ detailData.title || '-' }}</el-descriptions-item>
          <el-descriptions-item label="申请人">{{ detailData.applicant || '-' }}</el-descriptions-item>
          <el-descriptions-item label="部门">{{ detailData.department || '-' }}</el-descriptions-item>
          <el-descriptions-item label="申请金额">
            <span class="amount">¥{{ formatAmount(detailData.amount) }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="紧急程度">
            <el-tag :type="getUrgencyColor(detailData.urgency)" size="small">
              {{ getUrgencyText(detailData.urgency) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="申请时间">{{ formatDateTime(detailData.applyTime) }}</el-descriptions-item>
          <el-descriptions-item label="申请事由" :span="3">
            <div class="reason-text">{{ detailData.reason || '-' }}</div>
          </el-descriptions-item>
          <el-descriptions-item label="备注信息" :span="3">
            <div class="remark-text">{{ detailData.remark || '无' }}</div>
          </el-descriptions-item>
        </el-descriptions>
      </div>
    </div>

    <!-- 费用明细 -->
    <div class="section" v-if="detailData.expenseItems && detailData.expenseItems.length > 0">
      <div class="section-header">
        <h3>费用明细</h3>
        <span class="total-amount">总计：¥{{ formatAmount(detailData.amount) }}</span>
      </div>
      <div class="section-content">
        <el-table :data="detailData.expenseItems" border>
          <el-table-column label="费用类型" prop="type" width="120">
            <template #default="{ row }">
              <span>{{ getExpenseTypeText(row.type) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="费用说明" prop="description" min-width="200" />
          <el-table-column label="金额" prop="amount" width="120" align="right">
            <template #default="{ row }">
              <span class="item-amount">¥{{ formatAmount(row.amount) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="发生时间" prop="occurTime" width="150">
            <template #default="{ row }">
              {{ formatDate(row.occurTime) }}
            </template>
          </el-table-column>
          <el-table-column label="发票" prop="hasInvoice" width="80" align="center">
            <template #default="{ row }">
              <el-tag v-if="row.hasInvoice" type="success" size="small">有</el-tag>
              <el-tag v-else type="info" size="small">无</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="备注" prop="remark" />
        </el-table>
      </div>
    </div>

    <!-- 附件信息 -->
    <div class="section" v-if="detailData.attachments && detailData.attachments.length > 0">
      <div class="section-header">
        <h3>附件信息</h3>
        <span class="attachment-count">{{ detailData.attachments.length }} 个文件</span>
      </div>
      <div class="section-content">
        <div class="attachment-list">
          <div 
            v-for="attachment in detailData.attachments" 
            :key="attachment.id" 
            class="attachment-item"
          >
            <div class="attachment-icon">
              <el-icon v-if="attachment.type === 'image'"><picture /></el-icon>
              <el-icon v-else-if="attachment.type === 'pdf'"><document /></el-icon>
              <el-icon v-else><document-copy /></el-icon>
            </div>
            <div class="attachment-info">
              <div class="file-name">{{ attachment.name }}</div>
              <div class="file-meta">
                <span class="file-size">{{ formatFileSize(attachment.size) }}</span>
                <span class="upload-time">{{ formatDateTime(attachment.uploadTime) }}</span>
              </div>
            </div>
            <div class="attachment-actions">
              <el-button type="primary" link size="small" @click="previewAttachment(attachment)">
                预览
              </el-button>
              <el-button type="success" link size="small" @click="downloadAttachment(attachment)">
                下载
              </el-button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 审批流程 -->
    <div class="section">
      <div class="section-header">
        <h3>审批流程</h3>
        <div class="process-status">
          <el-tag :type="getStatusColor(detailData.status)" size="large">
            {{ getStatusText(detailData.status) }}
          </el-tag>
          <span class="current-node" v-if="detailData.currentNode">
            当前节点：{{ detailData.currentNode }}
          </span>
        </div>
      </div>
      <div class="section-content">
        <div class="approval-process">
          <el-timeline>
            <el-timeline-item
              v-for="(process, index) in detailData.processes"
              :key="index"
              :timestamp="formatDateTime(process.time)"
              :type="getProcessType(process)"
              :size="process.isCurrent ? 'large' : 'default'"
            >
              <div class="process-item" :class="{ 'current': process.isCurrent }">
                <div class="process-header">
                  <span class="process-name">{{ process.nodeName }}</span>
                  <el-tag 
                    :type="getProcessStatusColor(process.status)" 
                    size="small"
                  >
                    {{ getProcessStatusText(process.status) }}
                  </el-tag>
                </div>
                <div class="process-content">
                  <div class="process-info">
                    <span class="approver">审批人：{{ process.approver || '-' }}</span>
                    <span class="approval-time" v-if="process.approvalTime">
                      审批时间：{{ formatDateTime(process.approvalTime) }}
                    </span>
                  </div>
                  <div class="process-comment" v-if="process.comment">
                    <strong>审批意见：</strong>{{ process.comment }}
                  </div>
                  <div class="process-attachments" v-if="process.attachments && process.attachments.length > 0">
                    <strong>附件：</strong>
                    <el-tag 
                      v-for="attach in process.attachments" 
                      :key="attach.id" 
                      size="small" 
                      type="info"
                      style="margin-right: 8px;"
                    >
                      {{ attach.name }}
                    </el-tag>
                  </div>
                </div>
              </div>
            </el-timeline-item>
          </el-timeline>
        </div>
      </div>
    </div>

    <!-- 审批历史 -->
    <div class="section" v-if="detailData.histories && detailData.histories.length > 0">
      <div class="section-header">
        <h3>审批历史记录</h3>
        <span class="history-count">{{ detailData.histories.length }} 条记录</span>
      </div>
      <div class="section-content">
        <el-table :data="detailData.histories" border>
          <el-table-column label="操作时间" prop="time" width="160">
            <template #default="{ row }">
              {{ formatDateTime(row.time) }}
            </template>
          </el-table-column>
          <el-table-column label="操作人" prop="operator" width="120" />
          <el-table-column label="操作类型" prop="action" width="100">
            <template #default="{ row }">
              <el-tag :type="getActionColor(row.action)" size="small">
                {{ getActionText(row.action) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作内容" prop="content" min-width="200" />
          <el-table-column label="备注" prop="remark" />
        </el-table>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { Picture, Document, DocumentCopy } from '@element-plus/icons-vue'
import { showSuccess, showError, showConfirm } from '@/utils/message''
import ApprovalActions from '@/components/approval/ApprovalActions.vue'

// 路由管理
const router = useRouter()
const route = useRoute()

// 页面数据
const detailData = ref<any>({})
const isReadonly = ref(false)

// 类型映射
const typeMap = {
  EXPENSE_APPLY: { text: '费用申请', color: 'primary' },
  EXPENSE_REIMBURSE: { text: '费用报销', color: 'success' }
}

const urgencyMap = {
  LOW: { text: '普通', color: 'info' },
  MEDIUM: { text: '较急', color: 'warning' },
  HIGH: { text: '紧急', color: 'danger' }
}

const statusMap = {
  PENDING: { text: '审批中', color: 'warning' },
  APPROVED: { text: '已通过', color: 'success' },
  REJECTED: { text: '已驳回', color: 'danger' },
  COMPLETED: { text: '已完成', color: 'info' },
  CANCELLED: { text: '已取消', color: 'info' }
}

const processStatusMap = {
  PENDING: { text: '待处理', color: 'warning' },
  APPROVED: { text: '已同意', color: 'success' },
  REJECTED: { text: '已驳回', color: 'danger' },
  TRANSFERRED: { text: '已转审', color: 'info' }
}

const actionMap = {
  SUBMIT: { text: '提交', color: 'primary' },
  APPROVE: { text: '同意', color: 'success' },
  REJECT: { text: '驳回', color: 'danger' },
  TRANSFER: { text: '转审', color: 'info' },
  CANCEL: { text: '取消', color: 'info' }
}

// 生命周期
onMounted(() => {
  loadApprovalDetail()
})

// 方法定义
const loadApprovalDetail = async () => {
  const id = route.params.id
  const type = route.query.type
  
  try {
    // TODO: 调用API获取审批详情
    await new Promise(resolve => setTimeout(resolve, 500))
    
    // 模拟数据
    detailData.value = {
      id: id || 'AP20240115001',
      type: type || 'EXPENSE_APPLY',
      title: '差旅费用申请 - 北京项目会议',
      applicant: '张经理',
      department: '技术部',
      amount: 5000.00,
      reason: '前往北京参加重要项目会议，与客户进行技术交流和项目汇报。费用包括机票、住宿、交通、餐饮等。会议持续3天，需要提前一天到达进行准备工作。',
      remark: '本次会议对公司项目推进非常重要，希望尽快审批通过。',
      applyTime: '2024-01-15 10:30:00',
      urgency: 'HIGH',
      status: 'PENDING',
      currentNode: '财务审批',
      
      expenseItems: [
        { id: '1', type: 'TRANSPORT', description: '往返机票（北京-上海）', amount: 1200.00, occurTime: '2024-01-20', hasInvoice: true, remark: '经济舱' },
        { id: '2', type: 'ACCOMMODATION', description: '酒店住宿（3晚）', amount: 1500.00, occurTime: '2024-01-20', hasInvoice: true, remark: '协议酒店' },
        { id: '3', type: 'TRANSPORT', description: '市内交通费', amount: 300.00, occurTime: '2024-01-20', hasInvoice: false, remark: '预估费用' },
        { id: '4', type: 'MEAL', description: '餐饮费用', amount: 800.00, occurTime: '2024-01-20', hasInvoice: false, remark: '预估费用' },
        { id: '5', type: 'OTHER', description: '其他费用', amount: 1200.00, occurTime: '2024-01-20', hasInvoice: false, remark: '会议材料打印等' }
      ],
      
      attachments: [
        { id: '1', name: '会议邀请函.pdf', type: 'pdf', size: 2048000, uploadTime: '2024-01-15 10:35:00' },
        { id: '2', name: '项目简报.docx', type: 'doc', size: 512000, uploadTime: '2024-01-15 10:36:00' }
      ],
      
      processes: [
        { nodeName: '提交申请', status: 'APPROVED', approver: '系统', time: '2024-01-15 10:30:00', approvalTime: '2024-01-15 10:30:00' },
        { nodeName: '部门主管审批', status: 'APPROVED', approver: '李主管', time: '2024-01-15 11:20:00', approvalTime: '2024-01-15 14:30:00', comment: '会议安排合理，同意申请' },
        { nodeName: '财务审批', status: 'PENDING', approver: '待分配', time: '2024-01-15 14:30:00', isCurrent: true },
        { nodeName: '总经理审批', status: 'PENDING', approver: '待分配', time: '2024-01-15 14:30:00' }
      ],
      
      histories: [
        { time: '2024-01-15 10:30:00', operator: '张经理', action: 'SUBMIT', content: '提交申请', remark: '新建申请单' },
        { time: '2024-01-15 11:20:00', operator: '系统', action: 'TRANSFER', content: '流转至部门主管审批', remark: '流程节点流转' },
        { time: '2024-01-15 14:30:00', operator: '李主管', action: 'APPROVE', content: '审批通过', remark: '部门主管审批完成' },
        { time: '2024-01-15 14:30:00', operator: '系统', action: 'TRANSFER', content: '流转至财务审批', remark: '流程节点流转' }
      ]
    }
    
    // 判断是否为只读模式（历史记录或已审批）
    isReadonly.value = detailData.value.status !== 'PENDING'
    
  } catch (error) {
    console.error('加载审批详情失败:', error)
    showError('加载审批详情失败')
  }
}

const goBack = () => {
  router.back()
}

const handleApprove = async (data: any) => {
  try {
    await showConfirm(`确定同意"${data.title}"吗？`)
    
    // TODO: 调用API同意审批
    await new Promise(resolve => setTimeout(resolve, 300))
    
    showSuccess('审批已同意')
    loadApprovalDetail() // 重新加载页面
    
  } catch (error) {
    // 用户取消操作
  }
}

const handleReject = async (data: any, reason: string) => {
  try {
    await showConfirm(`确定驳回"${data.title}"吗？`)
    
    // TODO: 调用API驳回审批
    await new Promise(resolve => setTimeout(resolve, 300))
    
    showSuccess('审批已驳回')
    loadApprovalDetail()
    
  } catch (error) {
    // 用户取消操作
  }
}

const handleTransfer = async (data: any, target: string, reason: string) => {
  // TODO: 实现转审逻辑
  showSuccess('转审功能开发中')
}

const previewAttachment = (attachment: any) => {
  console.log('预览附件:', attachment)
  // TODO: 实现附件预览
  showSuccess('附件预览功能开发中')
}

const downloadAttachment = (attachment: any) => {
  console.log('下载附件:', attachment)
  // TODO: 实现附件下载
  showSuccess('附件下载功能开发中')
}

// 辅助方法
const getTypeText = (type: string) => {
  return typeMap[type]?.text || '未知类型'
}

const getTypeColor = (type: string) => {
  return typeMap[type]?.color || 'info'
}

const getUrgencyText = (urgency: string) => {
  return urgencyMap[urgency]?.text || '普通'
}

const getUrgencyColor = (urgency: string) => {
  return urgencyMap[urgency]?.color || 'info'
}

const getStatusText = (status: string) => {
  return statusMap[status]?.text || '未知'
}

const getStatusColor = (status: string) => {
  return statusMap[status]?.color || 'info'
}

const getProcessStatusText = (status: string) => {
  return processStatusMap[status]?.text || '待处理'
}

const getProcessStatusColor = (status: string) => {
  return processStatusMap[status]?.color || 'info'
}

const getActionText = (action: string) => {
  return actionMap[action]?.text || '操作'
}

const getActionColor = (action: string) => {
  return actionMap[action]?.color || 'info'
}

const getExpenseTypeText = (type: string) => {
  const typeMap = {
    TRANSPORT: '交通费',
    ACCOMMODATION: '住宿费',
    MEAL: '餐饮费',
    ENTERTAINMENT: '招待费',
    OFFICE: '办公费',
    OTHER: '其他费用'
  }
  return typeMap[type] || type
}

const getProcessType = (process: any) => {
  if (process.isCurrent) return 'primary'
  switch (process.status) {
    case 'APPROVED': return 'success'
    case 'REJECTED': return 'danger'
    case 'TRANSFERRED': return 'info'
    default: return ''
  }
}

const formatAmount = (amount: number) => {
  return amount?.toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 }) || '0.00'
}

const formatDateTime = (dateTime: string) => {
  if (!dateTime) return '-'
  return dateTime.replace('T', ' ').substring(0, 16)
}

const formatDate = (date: string) => {
  if (!date) return '-'
  return date.substring(0, 10)
}

const formatFileSize = (size: number) => {
  if (!size) return '0B'
  const units = ['B', 'KB', 'MB', 'GB']
  let unitIndex = 0
  while (size >= 1024 && unitIndex < units.length - 1) {
    size /= 1024
    unitIndex++
  }
  return `${size.toFixed(1)}${units[unitIndex]}`
}
</script>

<style scoped lang="css">
.approval-detail-container {
  padding: 20px;
  background: #f5f7fa;
  min-height: calc(100vh - 60px);
}

.page-header {
  margin-bottom: 20px;
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
}

.header-content h2 {
  margin: 0 0 8px 0;
  color: #303133;
  font-size: 20px;
}

.header-subtitle {
  display: flex;
  align-items: center;
  gap: 12px;
}

.application-id {
  color: #909399;
  font-size: 14px;
}

.section {
  background: white;
  border-radius: 8px;
  margin-bottom: 20px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.section-header {
  padding: 16px 20px;
  border-bottom: 1px solid #e4e7ed;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.section-header h3 {
  margin: 0;
  color: #303133;
  font-size: 16px;
}

.process-status {
  display: flex;
  align-items: center;
  gap: 16px;
}

.current-node {
  color: #606266;
  font-size: 14px;
}

.section-content {
  padding: 20px;
}

.amount {
  color: #f56c6c;
  font-weight: 600;
  font-size: 14px;
}

.item-amount {
  color: #f56c6c;
  font-weight: 500;
}

.total-amount {
  color: #f56c6c;
  font-size: 16px;
  font-weight: 600;
}

.reason-text,
.remark-text {
  line-height: 1.6;
  color: #606266;
}

.attachment-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.attachment-item {
  display: flex;
  align-items: center;
  padding: 12px;
  border: 1px solid #e4e7ed;
  border-radius: 6px;
  background: #fafafa;
}

.attachment-icon {
  width: 40px;
  height: 40px;
  background: #409eff;
  border-radius: 6px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 20px;
  margin-right: 12px;
}

.attachment-info {
  flex: 1;
}

.file-name {
  font-weight: 500;
  color: #303133;
  margin-bottom: 4px;
}

.file-meta {
  font-size: 12px;
  color: #909399;
  display: flex;
  gap: 16px;
}

.attachment-actions {
  display: flex;
  gap: 8px;
}

.process-item {
  border: 1px solid #e4e7ed;
  border-radius: 6px;
  padding: 12px;
  background: white;
}

.process-item.current {
  border-color: #409eff;
  background: #f0f9ff;
}

.process-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.process-name {
  font-weight: 600;
  color: #303133;
}

.process-content {
  font-size: 14px;
  color: #606266;
}

.process-info {
  display: flex;
  gap: 16px;
  margin-bottom: 8px;
}

.process-comment,
.process-attachments {
  margin-top: 8px;
  padding-top: 8px;
  border-top: 1px solid #f0f0f0;
}

.history-count {
  color: #909399;
  font-size: 14px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .approval-detail-container {
    padding: 10px;
  }
  
  .page-header {
    flex-direction: column;
    gap: 12px;
  }
  
  .section-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }
  
  .process-status {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }
  
  .section-content {
    padding: 15px;
  }
  
  .attachment-item {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }
  
  .attachment-actions {
    width: 100%;
    justify-content: flex-end;
  }
  
  .process-info {
    flex-direction: column;
    gap: 8px;
  }
}

@media (max-width: 480px) {
  .header-subtitle {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }
  
  :deep(.el-descriptions) {
    font-size: 12px;
  }
  
  :deep(.el-table) {
    font-size: 12px;
  }
}
</style>