<template>
  <el-dialog
    v-model="visible"
    title="提交审批确认"
    width="500px"
    :before-close="handleClose"
  >
    <div class="submit-confirm-content">
      <!-- 申请单信息 -->
      <div class="expense-info">
        <div class="info-row">
          <span class="label">申请单号：</span>
          <span class="value">{{ expenseData.applyCode }}</span>
        </div>
        <div class="info-row">
          <span class="label">申请部门：</span>
          <span class="value">{{ getDepartmentName(expenseData.departmentId) }}</span>
        </div>
        <div class="info-row">
          <span class="label">申请事由：</span>
          <span class="value text-truncate">{{ expenseData.reason }}</span>
        </div>
        <div class="info-row">
          <span class="label">合计金额：</span>
          <span class="value amount">¥{{ formatAmount(expenseData.totalAmount) }}</span>
        </div>
      </div>

      <!-- 审批流程预览 -->
      <div class="approval-preview" v-if="approvalFlow.length > 0">
        <h4>审批流程预览</h4>
        <div class="flow-steps">
          <div 
            v-for="(step, index) in approvalFlow" 
            :key="step.nodeId" 
            class="flow-step"
            :class="{ 'active': index === 0 }"
          >
            <div class="step-icon">
              <el-icon v-if="index === 0"><clock /></el-icon>
              <el-icon v-else><check /></el-icon>
            </div>
            <div class="step-info">
              <div class="step-title">{{ step.nodeName }}</div>
              <div class="step-approver">{{ step.approverName }}</div>
            </div>
            <div class="step-connector" v-if="index < approvalFlow.length - 1"></div>
          </div>
        </div>
      </div>

      <!-- 注意提醒 -->
      <div class="warning-tips">
        <el-alert
          v-if="budgetStatus === 'critical'"
          title="预算额度不足提醒"
          type="error"
          :closable="false"
          description="当前申请金额已超过可用预算额度，提交审批可能被拒绝。"
        />
        <el-alert
          v-else-if="budgetStatus === 'warning'"
          title="预算额度使用提醒"
          type="warning"
          :closable="false"
          description="当前申请金额将使用大部分可用预算额度。"
        />
        <el-alert
          v-else
          title="提交确认"
          type="info"
          :closable="false"
          description="提交后申请单将进入审批流程，不能再修改。"
        />
      </div>

      <!-- 提交选项 -->
      <div class="submit-options" v-if="canSendMessage">
        <el-checkbox v-model="sendMessage" border>
          发送短信通知审批人
        </el-checkbox>
        <el-checkbox v-model="sendEmail" border>
          发送邮件通知审批人
        </el-checkbox>
      </div>
    </div>

    <template #footer>
      <span class="dialog-footer">
        <el-button @click="handleClose">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleConfirmSubmit">
          {{ submitting ? '提交中...' : '确认提交' }}
        </el-button>
      </span>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, reactive, computed, watch } from 'vue'
import { Clock, Check } from '@element-plus/icons-vue'
import { showSuccess, showError } from '@/components/common/Message.vue'

// Props定义
interface Props {
  modelValue: boolean
  expenseData: any
  totalAmount: number
  budgetStatus: string
}

const props = defineProps<Props>()

// Emits定义
const emit = defineEmits<{
  'update:modelValue': [value: boolean]
  'submit-success': []
}>()

// 状态管理
const visible = computed({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value)
})

const submitting = ref(false)
const sendMessage = ref(true)
const sendEmail = ref(true)

// 部门列表（模拟数据）
const departmentList = ref([
  { id: '1', deptName: '技术部' },
  { id: '2', deptName: '财务部' },
  { id: '3', deptName: '市场部' },
  { id: '4', deptName: '人事部' },
  { id: '5', deptName: '行政部' }
])

// 审批流程数据（模拟）
const approvalFlow = ref([
  {
    nodeId: '1',
    nodeName: '部门经理审批',
    approverId: '2',
    approverName: '李经理',
    sortOrder: 1
  },
  {
    nodeId: '2',
    nodeName: '财务审批',
    approverId: '3',
    approverName: '王会计',
    sortOrder: 2
  },
  {
    nodeId: '3',
    nodeName: '总经理审批',
    approverId: '4',
    approverName: '张总',
    sortOrder: 3
  }
])

// 计算属性
const canSendMessage = computed(() => {
  // 检查系统是否支持消息通知功能
  return approvalFlow.value.length > 0
})

// 方法定义
const getDepartmentName = (departmentId: string) => {
  const dept = departmentList.value.find(d => d.id === departmentId)
  return dept ? dept.deptName : '未知部门'
}

const formatAmount = (amount: number) => {
  return amount?.toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 }) || '0.00'
}

const handleClose = () => {
  visible.value = false
}

const handleConfirmSubmit = async () => {
  submitting.value = true
  
  try {
    // 模拟API调用
    await new Promise(resolve => setTimeout(resolve, 2000))
    
    const submitData = {
      expenseId: props.expenseData.id,
      applyCode: props.expenseData.applyCode,
      totalAmount: props.totalAmount,
      approvalFlow: approvalFlow.value,
      notification: {
        sendMessage: sendMessage.value,
        sendEmail: sendEmail.value
      }
    }
    
    // TODO: 调用实际提交审批API
    console.log('提交审批数据:', submitData)
    
    showSuccess('费用申请已成功提交审批')
    
    // 提交成功后关闭弹窗并触发成功事件
    visible.value = false
    emit('submit-success')
    
  } catch (error) {
    console.error('提交审批失败:', error)
    showError('提交审批失败，请重试')
  } finally {
    submitting.value = false
  }
}

// 监听props变化
watch(() => props.modelValue, (newVal) => {
  if (newVal) {
    // 弹窗打开时重置状态
    submitting.value = false
    sendMessage.value = true
    sendEmail.value = true
    
    // TODO: 根据申请单数据重新获取审批流程
    console.log('加载审批流程数据...')
  }
})
</script>

<style scoped lang="css">
.submit-confirm-content {
  padding: 0 10px;
}

.expense-info {
  background: #f8f9fa;
  border-radius: 6px;
  padding: 16px;
  margin-bottom: 20px;
}

.info-row {
  display: flex;
  margin-bottom: 8px;
  align-items: flex-start;
}

.info-row:last-child {
  margin-bottom: 0;
}

.info-row .label {
  min-width: 80px;
  font-weight: 500;
  color: #606266;
}

.info-row .value {
  flex: 1;
  color: #303133;
}

.info-row .amount {
  font-size: 16px;
  font-weight: 600;
  color: #f56c6c;
}

.text-truncate {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  max-width: 200px;
}

.approval-preview {
  margin-bottom: 20px;
}

.approval-preview h4 {
  margin: 0 0 16px 0;
  color: #303133;
  font-size: 14px;
}

.flow-steps {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.flow-step {
  display: flex;
  align-items: center;
  position: relative;
}

.flow-step.active {
  color: #409eff;
}

.flow-step.active .step-title {
  font-weight: 600;
}

.step-icon {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f0f2f5;
  border-radius: 50%;
  margin-right: 12px;
}

.flow-step.active .step-icon {
  background: #409eff;
  color: white;
}

.step-info {
  flex: 1;
}

.step-title {
  font-size: 14px;
  color: #606266;
  margin-bottom: 4px;
}

.step-approver {
  font-size: 12px;
  color: #909399;
}

.step-connector {
  position: absolute;
  left: 16px;
  top: 32px;
  bottom: -14px;
  width: 2px;
  background: #e4e7ed;
}

.flow-step:last-child .step-connector {
  display: none;
}

.warning-tips {
  margin-bottom: 20px;
}

.submit-options {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
}

.submit-options .el-checkbox {
  margin-right: 0;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .info-row {
    flex-direction: column;
    gap: 4px;
  }
  
  .info-row .label {
    min-width: auto;
  }
  
  .submit-options {
    flex-direction: column;
    gap: 8px;
  }
}
</style>