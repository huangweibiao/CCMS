<template>
  <el-dialog
    v-model="visible"
    title="借款详情"
    width="800px"
    :before-close="handleClose">
    
    <div class="loan-detail" v-loading="loading">
      <!-- 基本信息 -->
      <el-row :gutter="20" class="info-section">
        <el-col :span="8">
          <div class="info-item">
            <label>借款单号：</label>
            <span class="value">{{ detail.loanNo }}</span>
          </div>
        </el-col>
        <el-col :span="8">
          <div class="info-item">
            <label>借款类型：</label>
            <el-tag :type="getLoanTypeTagType(detail.loanType)">
              {{ getLoanTypeText(detail.loanType) }}
            </el-tag>
          </div>
        </el-col>
        <el-col :span="8">
          <div class="info-item">
            <label>申请时间：</label>
            <span class="value">{{ formatDateTime(detail.applyTime) }}</span>
          </div>
        </el-col>
      </el-row>

      <el-row :gutter="20" class="info-section">
        <el-col :span="8">
          <div class="info-item">
            <label>借款金额：</label>
            <span class="value amount">¥{{ detail.amount?.toLocaleString() }}</span>
          </div>
        </el-col>
        <el-col :span="8">
          <div class="info-item">
            <label>申请状态：</label>
            <el-tag :type="getStatusTagType(detail.status)">
              {{ getStatusText(detail.status) }}
            </el-tag>
          </div>
        </el-col>
        <el-col :span="8">
          <div class="info-item">
            <label>预计还款：</label>
            <span class="value">{{ formatDate(detail.expectedRepaymentDate) }}</span>
          </div>
        </el-col>
      </el-row>

      <!-- 用途说明 -->
      <div class="section-title">用途说明</div>
      <el-row class="info-section">
        <el-col :span="24">
          <div class="info-item">
            <label>用途：</label>
            <span class="value">{{ detail.purpose }}</span>
          </div>
        </el-col>
      </el-row>
      
      <el-row class="info-section">
        <el-col :span="24">
          <div class="info-item vertical">
            <label>详细说明：</label>
            <div class="value content">{{ detail.description || '无' }}</div>
          </div>
        </el-col>
      </el-row>

      <!-- 审批信息 -->
      <div class="section-title" v-if="detail.approvalRecords && detail.approvalRecords.length > 0">
        审批流程
      </div>
      <div class="approval-timeline" v-if="detail.approvalRecords && detail.approvalRecords.length > 0">
        <el-timeline>
          <el-timeline-item
            v-for="record in detail.approvalRecords"
            :key="record.id"
            :timestamp="formatDateTime(record.approvalTime)"
            :type="getTimelineType(record.approvalResult)"
            placement="top">
            <div class="approval-item">
              <div class="approval-header">
                <span class="approver">{{ record.approverName }}</span>
                <el-tag :type="getApprovalResultTagType(record.approvalResult)" size="small">
                  {{ getApprovalResultText(record.approvalResult) }}
                </el-tag>
              </div>
              <div class="approval-comment" v-if="record.approvalComment">
                <span class="comment-label">审批意见：</span>
                {{ record.approvalComment }}
              </div>
            </div>
          </el-timeline-item>
        </el-timeline>
      </div>

      <!-- 还款信息 -->
      <div class="section-title" v-if="detail.repaymentRecords && detail.repaymentRecords.length > 0">
        还款记录
      </div>
      <el-table 
        :data="detail.repaymentRecords" 
        size="small"
        v-if="detail.repaymentRecords && detail.repaymentRecords.length > 0">
        <el-table-column prop="repaymentNo" label="还款单号" width="150" />
        <el-table-column prop="repaymentAmount" label="还款金额" width="120" align="right">
          <template #default="{ row }">
            ¥{{ row.repaymentAmount?.toLocaleString() }}
          </template>
        </el-table-column>
        <el-table-column prop="actualRepaymentDate" label="实际还款日期" width="120">
          <template #default="{ row }">
            {{ formatDate(row.actualRepaymentDate) }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getRepaymentStatusTagType(row.status)" size="small">
              {{ getRepaymentStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" />
      </el-table>

      <!-- 附件信息 -->
      <div class="section-title" v-if="detail.attachments && detail.attachments.length > 0">
        附件列表
      </div>
      <div class="attachment-list" v-if="detail.attachments && detail.attachments.length > 0">
        <div 
          v-for="attachment in detail.attachments" 
          :key="attachment.id"
          class="attachment-item">
          <el-link 
            type="primary" 
            :underline="false"
            @click="previewAttachment(attachment)">
            <el-icon><Document /></el-icon>
            {{ attachment.fileName }}
          </el-link>
        </div>
      </div>
    </div>

    <template #footer>
      <el-button @click="handleClose">关闭</el-button>
      <el-button 
        v-if="detail.status === 'PENDING'"
        type="warning" 
        @click="handleCancel">
        撤销申请
      </el-button>
      <el-button 
        v-if="detail.status === 'APPROVED'"
        type="primary" 
        @click="handleRepay">
        立即还款
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Document } from '@element-plus/icons-vue'
import { useLoanStore } from '@/stores/loan'

interface Props {
  modelValue: boolean
  loanId?: string
}

interface Emits {
  (e: 'update:modelValue', value: boolean): void
}

const props = withDefaults(defineProps<Props>(), {
  modelValue: false,
  loanId: ''
})

const emit = defineEmits<Emits>()

const router = useRouter()
const loanStore = useLoanStore()

const loading = ref(false)
const detail = ref<any>({})

const visible = ref(props.modelValue)

// 借款类型映射
const loanTypes = [
  { label: '备用金借款', value: 'RESERVE' },
  { label: '差旅借款', value: 'TRAVEL' },
  { label: '采购借款', value: 'PURCHASE' },
  { label: '业务借款', value: 'BUSINESS' },
  { label: '其他借款', value: 'OTHER' }
]

// 借款状态映射
const loanStatus = [
  { label: '待审批', value: 'PENDING' },
  { label: '审批中', value: 'APPROVING' },
  { label: '已通过', value: 'APPROVED' },
  { label: '已拒绝', value: 'REJECTED' },
  { label: '已撤销', value: 'CANCELLED' },
  { label: '已还款', value: 'REPAID' }
]

// 审批结果映射
const approvalResults = [
  { label: '通过', value: 'APPROVED' },
  { label: '拒绝', value: 'REJECTED' },
  { label: '待处理', value: 'PENDING' }
]

// 还款状态映射
const repaymentStatus = [
  { label: '待处理', value: 'PENDING' },
  { label: '已完成', value: 'COMPLETED' },
  { label: '已撤销', value: 'CANCELLED' }
]

// 获取显示文本的辅助函数
const getLoanTypeText = (type: string) => {
  const typeObj = loanTypes.find(t => t.value === type)
  return typeObj ? typeObj.label : type
}

const getStatusText = (status: string) => {
  const statusObj = loanStatus.find(s => s.value === status)
  return statusObj ? statusObj.label : status
}

const getApprovalResultText = (result: string) => {
  const resultObj = approvalResults.find(r => r.value === result)
  return resultObj ? resultObj.label : result
}

const getRepaymentStatusText = (status: string) => {
  const statusObj = repaymentStatus.find(s => s.value === status)
  return statusObj ? statusObj.label : status
}

// 获取标签类型的辅助函数
const getLoanTypeTagType = (type: string) => {
  const types = {
    'RESERVE': 'success',
    'TRAVEL': 'primary',
    'PURCHASE': 'warning',
    'BUSINESS': 'info',
    'OTHER': 'default'
  }
  return types[type as keyof typeof types] || 'default'
}

const getStatusTagType = (status: string) => {
  const types = {
    'PENDING': 'warning',
    'APPROVING': 'primary',
    'APPROVED': 'success',
    'REJECTED': 'danger',
    'CANCELLED': 'info',
    'REPAID': 'default'
  }
  return types[status as keyof typeof types] || 'default'
}

const getApprovalResultTagType = (result: string) => {
  const types = {
    'APPROVED': 'success',
    'REJECTED': 'danger',
    'PENDING': 'warning'
  }
  return types[result as keyof typeof types] || 'default'
}

const getRepaymentStatusTagType = (status: string) => {
  const types = {
    'PENDING': 'warning',
    'COMPLETED': 'success',
    'CANCELLED': 'info'
  }
  return types[status as keyof typeof types] || 'default'
}

const getTimelineType = (result: string) => {
  const types = {
    'APPROVED': 'success',
    'REJECTED': 'danger',
    'PENDING': 'warning'
  }
  return types[result as keyof typeof types] || 'primary'
}

// 日期格式化
const formatDate = (date: string | Date) => {
  if (!date) return '-';
  return new Date(date).toLocaleDateString('zh-CN')
}

const formatDateTime = (date: string | Date) => {
  if (!date) return '-';
  return new Date(date).toLocaleString('zh-CN')
}

// 加载借款详情
const loadDetail = async () => {
  if (!props.loanId) return
  
  loading.value = true
  try {
    await loanStore.loadLoanDetail(props.loanId)
    detail.value = loanStore.currentLoanDetail
  } catch (error) {
    ElMessage.error('加载借款详情失败')
  } finally {
    loading.value = false
  }
}

// 预览附件
const previewAttachment = (attachment: any) => {
  // TODO: 实现附件预览逻辑
  ElMessage.info('附件预览功能开发中')
}

// 撤销申请
const handleCancel = async () => {
  try {
    await ElMessageBox.confirm(
      '确定要撤销这笔借款申请吗？此操作不可恢复。',
      '确认撤销',
      {
        type: 'warning',
        confirmButtonText: '确定',
        cancelButtonText: '取消'
      }
    )
    
    await loanStore.cancelLoan(props.loanId)
    ElMessage.success('借款申请已撤销')
    emit('update:modelValue', false)
    location.reload() // 刷新页面更新列表
  } catch (error) {
    // 用户取消操作
  }
}

// 立即还款
const handleRepay = () => {
  router.push(`/repayment/apply?loanId=${props.loanId}`)
  emit('update:modelValue', false)
}

// 关闭对话框
const handleClose = () => {
  emit('update:modelValue', false)
}

// 监听visible变化
watch(() => props.modelValue, (val) => {
  visible.value = val
  if (val && props.loanId) {
    loadDetail()
  }
})

// 监听visible内部变化
watch(visible, (val) => {
  emit('update:modelValue', val)
})
</script>

<style scoped>
.loan-detail {
  max-height: 60vh;
  overflow-y: auto;
  padding: 10px;
}

.info-section {
  margin-bottom: 20px;
}

.info-item {
  display: flex;
  align-items: center;
  margin-bottom: 10px;
}

.info-item.vertical {
  flex-direction: column;
  align-items: flex-start;
}

.info-item label {
  font-weight: 600;
  color: #606266;
  min-width: 80px;
  margin-right: 10px;
}

.info-item .value {
  color: #303133;
}

.info-item .value.content {
  margin-top: 5px;
  line-height: 1.6;
}

.amount {
  color: #f56c6c;
  font-weight: 600;
}

.section-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  margin: 20px 0 15px 0;
  padding-bottom: 8px;
  border-bottom: 1px solid #ebeef5;
}

.approval-timeline {
  margin: 15px 0;
}

.approval-item {
  padding: 8px 0;
}

.approval-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 5px;
}

.approver {
  font-weight: 600;
  color: #303133;
}

.approval-comment {
  color: #606266;
  font-size: 14px;
}

.comment-label {
  font-weight: 600;
  color: #909399;
}

.attachment-list {
  margin: 15px 0;
}

.attachment-item {
  margin: 8px 0;
}

:deep(.el-timeline-item__timestamp) {
  font-size: 12px;
  color: #909399;
}
</style>