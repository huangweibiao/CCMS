<template>
  <div class="repayment-form">
    <el-card class="form-card">
      <template #header>
        <div class="card-header">
          <span class="title">还款申请</span>
          <el-button 
            type="primary" 
            :icon="Money" 
            @click="handleSubmit"
            :loading="submitting">
            提交还款
          </el-button>
        </div>
      </template>

      <!-- 借款信息 -->
      <div class="loan-info" v-if="currentLoan">
        <div class="info-section">
          <div class="info-title">
            <el-icon><InfoFilled /></el-icon>
            借款信息
          </div>
          <el-row :gutter="20">
            <el-col :span="6">
              <div class="info-item">
                <label>借款单号：</label>
                <span>{{ currentLoan.loanNo }}</span>
              </div>
            </el-col>
            <el-col :span="6">
              <div class="info-item">
                <label>借款金额：</label>
                <span class="amount">¥{{ currentLoan.amount?.toLocaleString() }}</span>
              </div>
            </el-col>
            <el-col :span="6">
              <div class="info-item">
                <label>已还金额：</label>
                <span>¥{{ currentLoan.repaidAmount?.toLocaleString() || '0' }}</span>
              </div>
            </el-col>
            <el-col :span="6">
              <div class="info-item">
                <label>待还金额：</label>
                <span class="remain-amount">¥{{ calculateRemainAmount().toLocaleString() }}</span>
              </div>
            </el-col>
          </el-row>
          <el-row :gutter="20">
            <el-col :span="12">
              <div class="info-item">
                <label>借款用途：</label>
                <span>{{ currentLoan.purpose }}</span>
              </div>
            </el-col>
            <el-col :span="12">
              <div class="info-item">
                <label>预计还款日期：</label>
                <span>{{ formatDate(currentLoan.expectedRepaymentDate) }}</span>
              </div>
            </el-col>
          </el-row>
        </div>
      </div>

      <el-alert
        v-else
        title="请选择要还款的借款单"
        type="info"
        :closable="false"
        show-icon>
        <template #default>
          您可以从借款列表中选择一笔已审批通过的借款进行还款。如果没有可还款的借款，请先<a href="/loan/list">申请借款</a>。
        </template>
      </el-alert>

      <!-- 还款表单 -->
      <el-form
        ref="formRef"
        :model="formData"
        :rules="rules"
        label-width="120px"
        class="repayment-form-content"
        v-if="currentLoan">
        
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="还款方式" prop="repaymentMethod">
              <el-select 
                v-model="formData.repaymentMethod" 
                placeholder="请选择还款方式"
                style="width: 100%">
                <el-option 
                  v-for="method in repaymentMethods" 
                  :key="method.value"
                  :label="method.label" 
                  :value="method.value" />
              </el-select>
            </el-form-item>
          </el-col>
          
          <el-col :span="8">
            <el-form-item label="还款金额" prop="repaymentAmount">
              <el-input-number
                v-model="formData.repaymentAmount"
                :min="100"
                :max="calculateRemainAmount()"
                :precision="2"
                placeholder="请输入还款金额"
                style="width: 100%">
                <template #prefix>¥</template>
              </el-input-number>
            </el-form-item>
          </el-col>
          
          <el-col :span="8">
            <el-form-item label="实际还款日期" prop="actualRepaymentDate">
              <el-date-picker
                v-model="formData.actualRepaymentDate"
                type="date"
                placeholder="选择实际还款日期"
                style="width: 100%"
                :disabled-date="disabledRepaymentDate" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="24">
            <el-form-item label="还款备注" prop="remark">
              <el-input
                v-model="formData.remark"
                type="textarea"
                :rows="3"
                placeholder="请填写还款的相关说明（可选）"
                maxlength="500"
                show-word-limit />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="还款凭证" prop="attachments">
          <el-upload
            ref="uploadRef"
            action="#"
            :auto-upload="false"
            :multiple="true"
            :file-list="formData.attachments"
            :before-upload="beforeUpload"
            :on-remove="handleRemove"
            :on-exceed="handleExceed">
            <el-button type="primary" :icon="Upload">上传凭证</el-button>
            <template #tip>
              <div class="el-upload__tip">
                支持JPG、PNG、PDF格式文件，单个文件不超过10MB。可上传银行转账截图、收据等还款凭证。
              </div>
            </template>
          </el-upload>
        </el-form-item>

        <!-- 还款统计信息 -->
        <div class="repayment-summary">
          <el-row :gutter="20">
            <el-col :span="6">
              <div class="summary-item">
                <label>本次还款：</label>
                <span class="amount">¥{{ formData.repaymentAmount?.toLocaleString() || '0' }}</span>
              </div>
            </el-col>
            <el-col :span="6">
              <div class="summary-item">
                <label>还款后待还：</label>
                <span class="remain-amount">¥{{ (calculateRemainAmount() - (formData.repaymentAmount || 0)).toLocaleString() }}</span>
              </div>
            </el-col>
            <el-col :span="6">
              <div class="summary-item">
                <label>还款比例：</label>
                <span>{{ calculateRepaymentPercentage() }}%</span>
              </div>
            </el-col>
            <el-col :span="6">
              <div class="summary-item">
                <label>还款情况：</label>
                <el-tag :type="getRepaymentStatusType()">
                  {{ getRepaymentStatusText() }}
                </el-tag>
              </div>
            </el-col>
          </el-row>
        </div>
      </el-form>
    </el-card>

    <!-- 选择借款对话框 -->
    <el-dialog v-model="selectDialogVisible" title="选择借款" width="800px">
      <loan-select-dialog @select="handleLoanSelect" />
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox, type FormInstance, type UploadInstance } from 'element-plus'
import { Money, Upload, InfoFilled } from '@element-plus/icons-vue'
import type { RepaymentRequest } from '@/types/loan'
import { useLoanStore } from '@/stores/loan'
import LoanSelectDialog from './components/LoanSelectDialog.vue'

const route = useRoute()
const formRef = ref<FormInstance>()
const uploadRef = ref<UploadInstance>()
const loanStore = useLoanStore()

const submitting = ref(false)
const selectDialogVisible = ref(false)
const currentLoan = ref<any>(null)

// 还款方式选项
const repaymentMethods = [
  { label: '银行转账', value: 'BANK_TRANSFER' },
  { label: '现金还款', value: 'CASH' },
  { label: '工资抵扣', value: 'SALARY_DEDUCTION' },
  { label: '费用抵扣', value: 'EXPENSE_OFFSET' },
  { label: '其他方式', value: 'OTHER' }
]

// 表单数据
const formData = reactive<RepaymentRequest>({
  loanId: '',
  repaymentAmount: 0,
  repaymentMethod: 'BANK_TRANSFER',
  actualRepaymentDate: new Date(),
  remark: '',
  attachments: []
})

// 表单验证规则
const rules = {
  repaymentMethod: [
    { required: true, message: '请选择还款方式', trigger: 'change' }
  ],
  repaymentAmount: [
    { required: true, message: '请输入还款金额', trigger: 'blur' },
    { 
      validator: (rule: any, value: number, callback: any) => {
        const remainAmount = calculateRemainAmount()
        if (value < 100) {
          callback(new Error('还款金额不能少于100元'))
        } else if (value > remainAmount) {
          callback(new Error('还款金额不能超过待还金额'))
        } else {
          callback()
        }
      }, 
      trigger: 'blur' 
    }
  ],
  actualRepaymentDate: [
    { required: true, message: '请选择实际还款日期', trigger: 'change' }
  ]
}

// 计算待还金额
const calculateRemainAmount = () => {
  if (!currentLoan.value) return 0
  const totalAmount = currentLoan.value.amount || 0
  const repaidAmount = currentLoan.value.repaidAmount || 0
  return Math.max(totalAmount - repaidAmount, 0)
}

// 计算还款比例
const calculateRepaymentPercentage = () => {
  if (!currentLoan.value) return 0
  const totalAmount = currentLoan.value.amount || 0
  const repaymentAmount = formData.repaymentAmount || 0
  const currentRepaid = currentLoan.value.repaidAmount || 0
  return Math.round(((currentRepaid + repaymentAmount) / totalAmount) * 100)
}

// 获取还款状态类型
const getRepaymentStatusType = () => {
  const percentage = calculateRepaymentPercentage()
  if (percentage >= 100) return 'success'
  if (percentage >= 80) return 'warning'
  return 'info'
}

// 获取还款状态文本
const getRepaymentStatusText = () => {
  const percentage = calculateRepaymentPercentage()
  if (percentage >= 100) return '全部还清'
  if (percentage >= 80) return '基本还清'
  if (percentage >= 50) return '还款过半'
  if (percentage > 0) return '部分还款'
  return '未还款'
}

// 日期格式化
const formatDate = (date: string | Date) => {
  if (!date) return '-';
  return new Date(date).toLocaleDateString('zh-CN')
}

// 禁用日期（不能选择今天之后的日期）
const disabledRepaymentDate = (date: Date) => {
  return date.getTime() > Date.now() + 24 * 60 * 60 * 1000
}

// 文件上传前验证
const beforeUpload = (file: File) => {
  const isJpgOrPng = file.type === 'image/jpeg' || file.type === 'image/png' || file.type === 'application/pdf'
  const isLt10M = file.size / 1024 / 1024 < 10

  if (!isJpgOrPng) {
    ElMessage.error('上传文件只能是 JPG、PNG 或 PDF 格式!')
    return false
  }
  if (!isLt10M) {
    ElMessage.error('上传文件大小不能超过 10MB!')
    return false
  }
  return true
}

// 文件删除处理
const handleRemove = (file: any, fileList: any[]) => {
  formData.attachments = fileList
}

// 文件超出限制
const handleExceed = () => {
  ElMessage.warning('最多只能上传5个文件')
}

// 选择借款
const handleLoanSelect = (loan: any) => {
  currentLoan.value = loan
  formData.loanId = loan.id
  selectDialogVisible.value = false
}

// 提交还款申请
const handleSubmit = async () => {
  if (!formRef.value || !currentLoan.value) {
    selectDialogVisible.value = true
    return
  }

  try {
    await formRef.value.validate()
    
    ElMessageBox.confirm(
      `您确定要还款${formData.repaymentAmount}元吗？此操作不可撤销。`,
      '确认还款',
      {
        confirmButtonText: '确定还款',
        cancelButtonText: '取消',
        type: 'warning'
      }
    ).then(async () => {
      submitting.value = true
      try {
        const success = await loanStore.submitRepayment(formData)
        if (success) {
          ElMessage.success('还款申请提交成功')
          formRef.value?.resetFields()
          formData.attachments = []
          currentLoan.value = null
        }
      } finally {
        submitting.value = false
      }
    })
  } catch (error) {
    ElMessage.error('请检查表单填写是否正确')
  }
}

// 初始化：从URL参数加载借款信息
const initFromQuery = async () => {
  const loanId = route.query.loanId as string
  if (loanId) {
    try {
      await loanStore.loadLoanDetail(loanId)
      currentLoan.value = loanStore.currentLoanDetail
      formData.loanId = loanId
    } catch (error) {
      ElMessage.error('加载借款信息失败')
    }
  }
}

onMounted(() => {
  initFromQuery()
})
</script>

<style scoped>
.repayment-form {
  max-width: 1000px;
  margin: 0 auto;
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.title {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}

.loan-info {
  margin-bottom: 20px;
}

.info-section {
  padding: 15px;
  background: #f8f9fa;
  border-radius: 4px;
  border-left: 4px solid #409eff;
}

.info-title {
  display: flex;
  align-items: center;
  margin-bottom: 15px;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.info-title .el-icon {
  margin-right: 8px;
  color: #409eff;
}

.info-item {
  display: flex;
  align-items: center;
  margin-bottom: 8px;
}

.info-item label {
  font-weight: 600;
  color: #606266;
  min-width: 80px;
  margin-right: 8px;
}

.amount {
  color: #f56c6c;
  font-weight: 600;
}

.remain-amount {
  color: #e6a23c;
  font-weight: 600;
}

.repayment-form-content {
  margin-top: 20px;
}

.repayment-summary {
  margin-top: 20px;
  padding: 15px;
  background: #f8f9fa;
  border-radius: 4px;
}

.summary-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 0;
}

.summary-item label {
  font-weight: 600;
  color: #606266;
}

:deep(.el-upload) {
  width: 100%;
}

:deep(.el-upload .el-button) {
  width: 100%;
}

:deep(.el-upload__tip) {
  margin-top: 8px;
  color: #909399;
  font-size: 12px;
}

:deep(.el-alert a) {
  color: #409eff;
  text-decoration: none;
}

:deep(.el-alert a:hover) {
  text-decoration: underline;
}
</style>