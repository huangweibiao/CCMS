<template>
  <el-dialog
    v-model="visible"
    title="还款详情"
    width="700px"
    :before-close="handleClose">
    
    <div class="repayment-detail" v-loading="loading">
      <!-- 基本信息 -->
      <el-row :gutter="20" class="info-section">
        <el-col :span="12">
          <div class="info-item">
            <label>还款单号：</label>
            <span class="value">{{ detail.repaymentNo }}</span>
          </div>
        </el-col>
        <el-col :span="12">
          <div class="info-item">
            <label>还款状态：</label>
            <el-tag :type="getStatusTagType(detail.status)">
              {{ getStatusText(detail.status) }}
            </el-tag>
          </div>
        </el-col>
      </el-row>

      <el-row :gutter="20" class="info-section">
        <el-col :span="12">
          <div class="info-item">
            <label>关联借款：</label>
            <el-link type="primary" :underline="false" @click="viewLoanDetail">
              {{ detail.loanNo }}
            </el-link>
          </div>
        </el-col>
        <el-col :span="12">
          <div class="info-item">
            <label>还款方式：</label>
            <el-tag :type="getRepaymentMethodTagType(detail.repaymentMethod)">
              {{ getRepaymentMethodText(detail.repaymentMethod) }}
            </el-tag>
          </div>
        </el-col>
      </el-row>

      <el-row :gutter="20" class="info-section">
        <el-col :span="12">
          <div class="info-item">
            <label>还款金额：</label>
            <span class="value amount">¥{{ detail.repaymentAmount?.toLocaleString() }}</span>
          </div>
        </el-col>
        <el-col :span="12">
          <div class="info-item">
            <label>实际还款日期：</label>
            <span class="value">{{ formatDate(detail.actualRepaymentDate) }}</span>
          </div>
        </el-col>
      </el-row>

      <el-row :gutter="20" class="info-section">
        <el-col :span="12">
          <div class="info-item">
            <label>创建时间：</label>
            <span class="value">{{ formatDateTime(detail.createTime) }}</span>
          </div>
        </el-col>
        <el-col :span="12">
          <div class="info-item">
            <label>更新时间：</label>
            <span class="value">{{ formatDateTime(detail.updateTime) }}</span>
          </div>
        </el-col>
      </el-row>

      <!-- 备注信息 -->
      <div class="section-title">备注信息</div>
      <el-row class="info-section">
        <el-col :span="24">
          <div class="info-item vertical">
            <label>还款备注：</label>
            <div class="value content">{{ detail.remark || '无' }}</div>
          </div>
        </el-col>
      </el-row>

      <!-- 凭证信息 -->
      <div class="section-title" v-if="detail.attachments && detail.attachments.length > 0">
        还款凭证
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
    </div>

    <template #footer>
      <el-button @click="handleClose">关闭</el-button>
      <el-button 
        v-if="detail.status === 'PENDING'"
        type="warning" 
        @click="handleCancel">
        撤销申请
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
  repaymentId?: string
}

interface Emits {
  (e: 'update:modelValue', value: boolean): void
}

const props = withDefaults(defineProps<Props>(), {
  modelValue: false,
  repaymentId: ''
})

const emit = defineEmits<Emits>()

const router = useRouter()
const loanStore = useLoanStore()

const loading = ref(false)
const detail = ref<any>({})

const visible = ref(props.modelValue)

// 还款状态映射
const repaymentStatus = [
  { label: '待审批', value: 'PENDING' },
  { label: '审批中', value: 'APPROVING' },
  { label: '已通过', value: 'APPROVED' },
  { label: '已完成', value: 'COMPLETED' },
  { label: '已拒绝', value: 'REJECTED' },
  { label: '已撤销', value: 'CANCELLED' }
]

// 还款方式映射
const repaymentMethods = [
  { label: '银行转账', value: 'BANK_TRANSFER' },
  { label: '现金还款', value: 'CASH' },
  { label: '工资抵扣', value: 'SALARY_DEDUCTION' },
  { label: '费用抵扣', value: 'EXPENSE_OFFSET' },
  { label: '其他方式', value: 'OTHER' }
]

// 审批结果映射
const approvalResults = [
  { label: '通过', value: 'APPROVED' },
  { label: '拒绝', value: 'REJECTED' },
  { label: '待处理', value: 'PENDING' }
]

// 获取显示文本的辅助函数
const getStatusText = (status: string) => {
  const statusObj = repaymentStatus.find(s => s.value === status)
  return statusObj ? statusObj.label : status
}

const getRepaymentMethodText = (method: string) => {
  const methodObj = repaymentMethods.find(m => m.value === method)
  return methodObj ? methodObj.label : method
}

const getApprovalResultText = (result: string) => {
  const resultObj = approvalResults.find(r => r.value === result)
  return resultObj ? resultObj.label : result
}

// 获取标签类型的辅助函数
const getStatusTagType = (status: string) => {
  const types = {
    'PENDING': 'warning',
    'APPROVING': 'primary',
    'APPROVED': 'info',
    'COMPLETED': 'success',
    'REJECTED': 'danger',
    'CANCELLED': 'info'
  }
  return types[status as keyof typeof types] || 'default'
}

const getRepaymentMethodTagType = (method: string) => {
  const types = {
    'BANK_TRANSFER': 'primary',
    'CASH': 'success',
    'SALARY_DEDUCTION': 'warning',
    'EXPENSE_OFFSET': 'info',
    'OTHER': 'default'
  }
  return types[method as keyof typeof types] || 'default'
}

const getApprovalResultTagType = (result: string) => {
  const types = {
    'APPROVED': 'success',
    'REJECTED': 'danger',
    'PENDING': 'warning'
  }
  return types[result as keyof typeof types] || 'default'
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

// 加载还款详情
const loadDetail = async () => {
  if (!props.repaymentId) return
  
  loading.value = true
  try {
    // TODO: 实现还款详情加载逻辑
    // 临时模拟数据
    detail.value = {
      repaymentNo: `RR${Date.now()}`,
      status: 'COMPLETED',
      loanNo: 'LN2024123456',
      loanId: '123',
      repaymentMethod: 'BANK_TRANSFER',
      repaymentAmount: 5000,
      actualRepaymentDate: new Date(),
      createTime: new Date(),
      updateTime: new Date(),
      remark: '银行转账还款',
      attachments: [],
      approvalRecords: []
    }
  } catch (error) {
    ElMessage.error('加载还款详情失败')
  } finally {
    loading.value = false
  }
}

// 预览附件
const previewAttachment = (attachment: any) => {
  // TODO: 实现附件预览逻辑
  ElMessage.info('附件预览功能开发中')
}

// 查看借款详情
const viewLoanDetail = () => {
  // TODO: 实现跳转到借款详情逻辑
  ElMessage.info('查看借款详情功能开发中')
}

// 撤销还款申请
const handleCancel = async () => {
  try {
    await ElMessageBox.confirm(
      '确定要撤销这笔还款申请吗？此操作不可恢复。',
      '确认撤销',
      {
        type: 'warning',
        confirmButtonText: '确定',
        cancelButtonText: '取消'
      }
    )
    
    // TODO: 实现还款撤销逻辑
    ElMessage.success('还款申请已撤销')
    emit('update:modelValue', false)
    location.reload() // 刷新页面更新列表
  } catch (error) {
    // 用户取消操作
  }
}

// 关闭对话框
const handleClose = () => {
  emit('update:modelValue', false)
}

// 监听visible变化
watch(() => props.modelValue, (val) => {
  visible.value = val
  if (val && props.repaymentId) {
    loadDetail()
  }
})

// 监听visible内部变化
watch(visible, (val) => {
  emit('update:modelValue', val)
})
</script>

<style scoped>
.repayment-detail {
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
  min-width: 100px;
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

.attachment-list {
  margin: 15px 0;
}

.attachment-item {
  margin: 8px 0;
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

:deep(.el-timeline-item__timestamp) {
  font-size: 12px;
  color: #909399;
}
</style>