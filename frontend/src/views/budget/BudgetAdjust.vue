<template>
  <div class="budget-adjust-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2>预算调整</h2>
      <p>对已批准的预算进行额度调整</p>
    </div>

    <div class="budget-adjust-content">
      <!-- 原始预算信息 -->
      <div class="budget-card">
        <h3 class="card-title">原始预算信息</h3>
        <div class="original-budget">
          <div class="budget-info">
            <div class="info-row">
              <div class="info-item">
                <label>预算编号：</label>
                <span>{{ originalBudget.budgetCode }}</span>
              </div>
              <div class="info-item">
                <label>预算年度：</label>
                <span>{{ originalBudget.budgetYear }}</span>
              </div>
              <div class="info-item">
                <label>责任部门：</label>
                <span>{{ originalBudget.departmentName }}</span>
              </div>
            </div>
            <div class="info-row">
              <div class="info-item">
                <label>原始总额：</label>
                <span class="amount">¥{{ originalBudget.totalAmount?.toFixed(2) }}</span>
              </div>
              <div class="info-item">
                <label>已使用：</label>
                <span class="amount">¥{{ originalBudget.usedAmount?.toFixed(2) }}</span>
              </div>
              <div class="info-item">
                <label>剩余额度：</label>
                <span class="amount">¥{{ remainingAmount?.toFixed(2) }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 调整信息卡片 -->
      <div class="budget-card">
        <h3 class="card-title">调整信息</h3>
        <el-form :model="adjustForm" :rules="adjustRules" ref="adjustFormRef" label-width="120px">
          <el-row :gutter="20">
            <el-col :xs="24" :sm="12" :md="8">
              <el-form-item label="调整类型" prop="adjustType">
                <el-select v-model="adjustForm.adjustType" placeholder="请选择调整类型" style="width: 100%">
                  <el-option label="增加预算" value="INCREASE" />
                  <el-option label="减少预算" value="DECREASE" />
                  <el-option label="项目间调整" value="TRANSFER" />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :xs="24" :sm="12" :md="8">
              <el-form-item label="调整金额" prop="adjustAmount">
                <el-input-number
                  v-model="adjustForm.adjustAmount"
                  :min="0"
                  :precision="2"
                  :step="1000"
                  placeholder="请输入调整金额"
                  style="width: 100%"
                >
                  <template #prefix>¥</template>
                </el-input-number>
              </el-form-item>
            </el-col>
            <el-col :xs="24" :sm="12" :md="8">
              <el-form-item label="币种" prop="currency">
                <el-select v-model="adjustForm.currency" placeholder="请选择币种" style="width: 100%">
                  <el-option label="人民币" value="CNY" />
                  <el-option label="美元" value="USD" />
                  <el-option label="欧元" value="EUR" />
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>
          
          <el-row :gutter="20">
            <el-col :xs="24" :sm="12" :md="12">
              <el-form-item label="调整原因" prop="adjustReason">
                <el-select v-model="adjustForm.adjustReason" placeholder="请选择调整原因" style="width: 100%">
                  <el-option label="业务发展需求" value="BUSINESS_GROWTH" />
                  <el-option label="项目变更" value="PROJECT_CHANGE" />
                  <el-option label="成本控制需要" value="COST_CONTROL" />
                  <el-option label="紧急情况" value="EMERGENCY" />
                  <el-option label="其他" value="OTHER" />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :xs="24" :sm="12" :md="12">
              <el-form-item label="调整日期" prop="adjustDate">
                <el-date-picker
                  v-model="adjustForm.adjustDate"
                  type="date"
                  placeholder="选择调整日期"
                  style="width: 100%"
                />
              </el-form-item>
            </el-col>
          </el-row>
          
          <el-form-item label="详细说明" prop="description">
            <el-input
              v-model="adjustForm.description"
              type="textarea"
              :rows="3"
              placeholder="请输入调整的详细说明"
              maxlength="500"
              show-word-limit
            />
          </el-form-item>
        </el-form>
      </div>

      <!-- 调整后预算信息 -->
      <div class="budget-card">
        <h3 class="card-title">调整后预算信息</h3>
        <div class="adjusted-budget">
          <div class="budget-summary">
            <div class="summary-item">
              <span class="label">调整前总额：</span>
              <span class="value">¥{{ originalBudget.totalAmount?.toFixed(2) }}</span>
            </div>
            <div class="summary-item">
              <span class="label">调整金额：</span>
              <span class="value" :class="{ 'positive': adjustAmount > 0, 'negative': adjustAmount < 0 }">
                {{ adjustAmount > 0 ? '+' : '' }}¥{{ adjustAmount.toFixed(2) }}
              </span>
            </div>
            <div class="summary-item">
              <span class="label">调整后总额：</span>
              <span class="value final-amount">¥{{ adjustedTotalAmount.toFixed(2) }}</span>
            </div>
            <div class="summary-item">
              <span class="label">调整幅度：</span>
              <span class="value" :class="{ 'positive': adjustPercentage > 0, 'negative': adjustPercentage < 0 }">
                {{ adjustPercentage > 0 ? '+' : '' }}{{ adjustPercentage.toFixed(2) }}%
              </span>
            </div>
          </div>
          
          <!-- 调整影响分析 -->
          <div class="impact-analysis" v-if="adjustAmount !== 0">
            <h4>调整影响分析</h4>
            <div class="impact-summary">
              <div v-if="adjustAmount > 0" class="impact-item positive">
                <el-icon><circle-check /></el-icon>
                <span>预算额度增加，有利于支持业务发展</span>
              </div>
              <div v-if="adjustAmount < 0" class="impact-item negative">
                <el-icon><warning /></el-icon>
                <span>预算额度减少，有助于成本控制</span>
              </div>
              <div v-if="adjustedRemainingAmount >= 0" class="impact-item">
                <el-icon><info-filled /></el-icon>
                <span>调整后剩余额度：¥{{ adjustedRemainingAmount.toFixed(2) }}</span>
              </div>
              <div v-if="adjustedRemainingAmount < 0" class="impact-item warning">
                <el-icon><warning-filled /></el-icon>
                <span>调整后预算不足，需谨慎处理</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 操作按钮区域 -->
      <div class="action-buttons">
        <el-button type="primary" @click="handleSubmit" :loading="submitting">
          提交调整申请
        </el-button>
        <el-button @click="handlePreview" :loading="previewing">
          预览调整单
        </el-button>
        <el-button @click="handleReset">
          重置
        </el-button>
        <el-button @click="handleCancel">
          取消
        </el-button>
      </div>
    </div>

    <!-- 调整单预览对话框 -->
    <CustomModal
      v-model="previewModalVisible"
      title="预算调整单预览"
      width="700px"
    >
      <div class="preview-content">
        <div class="preview-header">
          <h3>预算调整申请单</h3>
          <p>调整申请编号：{{ previewData.adjustCode || '待生成' }}</p>
        </div>
        
        <div class="preview-section">
          <h4>基本信息</h4>
          <div class="preview-row">
            <div class="preview-item">
              <label>预算编号：</label>
              <span>{{ previewData.budgetCode }}</span>
            </div>
            <div class="preview-item">
              <label>调整类型：</label>
              <span>{{ getAdjustTypeText(previewData.adjustType) }}</span>
            </div>
            <div class="preview-item">
              <label>调整金额：</label>
              <span class="amount">{{ previewData.adjustAmount > 0 ? '+' : '' }}¥{{ previewData.adjustAmount?.toFixed(2) }}</span>
            </div>
          </div>
          <div class="preview-row">
            <div class="preview-item">
              <label>调整原因：</label>
              <span>{{ getAdjustReasonText(previewData.adjustReason) }}</span>
            </div>
            <div class="preview-item">
              <label>调整日期：</label>
              <span>{{ previewData.adjustDate }}</span>
            </div>
            <div class="preview-item">
              <label>调整幅度：</label>
              <span>{{ previewAdjustPercentage > 0 ? '+' : '' }}{{ previewAdjustPercentage.toFixed(2) }}%</span>
            </div>
          </div>
        </div>
        
        <div class="preview-section">
          <h4>详细说明</h4>
          <p class="preview-description">{{ previewData.description || '暂无说明' }}</p>
        </div>
      </div>
      
      <template #footer>
        <el-button @click="previewModalVisible = false">关闭</el-button>
        <el-button type="primary" @click="handleConfirmSubmit">确认提交</el-button>
      </template>
    </CustomModal>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { CircleCheck, Warning, InfoFilled, WarningFilled } from '@element-plus/icons-vue'
import CustomModal from '@/components/common/CustomModal.vue'
import { showSuccess, showConfirm, showError } from '@/components/common/Message.vue'

// 表单引用
const adjustFormRef = ref<FormInstance>()

// 原始预算数据
const originalBudget = reactive({
  id: '1',
  budgetCode: 'BGT202401001',
  budgetYear: '2024',
  budgetType: 'ANNUAL',
  departmentName: '技术部',
  totalAmount: 500000,
  usedAmount: 125000,
  status: 'APPROVED'
})

// 调整表单数据
const adjustForm = reactive({
  adjustType: 'INCREASE',
  adjustAmount: 0,
  currency: 'CNY',
  adjustReason: '',
  adjustDate: new Date(),
  description: ''
})

// 表单验证规则
const adjustRules: FormRules = {
  adjustType: [
    { required: true, message: '请选择调整类型', trigger: 'change' }
  ],
  adjustAmount: [
    { required: true, message: '请输入调整金额', trigger: 'blur' },
    { type: 'number', min: 0, message: '调整金额必须大于0', trigger: 'blur' }
  ],
  adjustReason: [
    { required: true, message: '请选择调整原因', trigger: 'change' }
  ],
  adjustDate: [
    { required: true, message: '请选择调整日期', trigger: 'change' }
  ]
}

// 计算属性
const remainingAmount = computed(() => {
  return (originalBudget.totalAmount || 0) - (originalBudget.usedAmount || 0)
})

const adjustAmount = computed(() => {
  const amount = adjustForm.adjustAmount || 0
  return adjustForm.adjustType === 'DECREASE' ? -amount : amount
})

const adjustedTotalAmount = computed(() => {
  return (originalBudget.totalAmount || 0) + adjustAmount.value
})

const adjustPercentage = computed(() => {
  if (originalBudget.totalAmount === 0) return 0
  return (adjustAmount.value / originalBudget.totalAmount) * 100
})

const adjustedRemainingAmount = computed(() => {
  return (adjustedTotalAmount.value || 0) - (originalBudget.usedAmount || 0)
})

// 预览数据
const previewModalVisible = ref(false)
const previewData = reactive({})

const previewAdjustPercentage = computed(() => {
  if (originalBudget.totalAmount === 0) return 0
  const amount = adjustForm.adjustAmount || 0
  const finalAmount = adjustForm.adjustType === 'DECREASE' ? -amount : amount
  return (finalAmount / originalBudget.totalAmount) * 100
})

// 加载状态
const submitting = ref(false)
const previewing = ref(false)

// 生命周期
onMounted(() => {
  loadOriginalBudget()
})

// 方法
const loadOriginalBudget = async () => {
  try {
    // TODO: 根据传入的预算ID加载原始预算数据
    // 使用模拟数据
  } catch (error) {
    console.error('加载原始预算数据失败:', error)
  }
}

const handleSubmit = async () => {
  if (!adjustFormRef.value) return

  try {
    const valid = await adjustFormRef.value.validate()
    if (!valid) return
    
    // 验证调整金额
    if (adjustForm.adjustAmount <= 0) {
      showError('调整金额必须大于0')
      return
    }
    
    // 验证调整后的预算总额
    if (adjustedTotalAmount.value < 0) {
      showError('调整后预算总额不能为负数')
      return
    }
    
    // 如果是减少预算，验证剩余额度是否充足
    if (adjustForm.adjustType === 'DECREASE' && adjustForm.adjustAmount > remainingAmount.value) {
      showError('调整金额不能超过剩余额度')
      return
    }
    
    submitting.value = true
    
    // TODO: 调用提交调整申请API
    console.log('提交预算调整:', { originalBudget, adjustForm, adjustedTotalAmount: adjustedTotalAmount.value })
    
    showSuccess('预算调整申请提交成功')
    handleReset()
  } catch (error) {
    console.error('提交预算调整失败:', error)
    showError('提交失败，请检查表单数据')
  } finally {
    submitting.value = false
  }
}

const handlePreview = async () => {
  if (!adjustFormRef.value) return

  try {
    const valid = await adjustFormRef.value.validate()
    if (!valid) return
    
    previewing.value = true
    
    // 生成预览数据
    Object.assign(previewData, {
      budgetCode: originalBudget.budgetCode,
      adjustType: adjustForm.adjustType,
      adjustAmount: adjustAmount.value,
      adjustReason: adjustForm.adjustReason,
      adjustDate: typeof adjustForm.adjustDate === 'string' ? adjustForm.adjustDate : adjustForm.adjustDate?.toLocaleDateString(),
      description: adjustForm.description
    })
    
    previewModalVisible.value = true
  } catch (error) {
    console.error('预览失败:', error)
    showError('预览失败，请检查表单数据')
  } finally {
    previewing.value = false
  }
}

const handleConfirmSubmit = async () => {
  previewModalVisible.value = false
  await handleSubmit()
}

const handleReset = () => {
  adjustFormRef.value?.resetFields()
  
  // 重置表单数据
  Object.assign(adjustForm, {
    adjustType: 'INCREASE',
    adjustAmount: 0,
    currency: 'CNY',
    adjustReason: '',
    adjustDate: new Date(),
    description: ''
  })
}

const handleCancel = () => {
  showConfirm('确定要取消预算调整吗？所有输入数据将会丢失。').then((confirmed) => {
    if (confirmed) {
      // 返回预算列表页面
      showSuccess('已取消预算调整')
    }
  })
}

// 文本映射函数
const getAdjustTypeText = (type: string) => {
  const typeMap = {
    INCREASE: '增加预算',
    DECREASE: '减少预算',
    TRANSFER: '项目间调整'
  }
  return typeMap[type] || type
}

const getAdjustReasonText = (reason: string) => {
  const reasonMap = {
    BUSINESS_GROWTH: '业务发展需求',
    PROJECT_CHANGE: '项目变更',
    COST_CONTROL: '成本控制需要',
    EMERGENCY: '紧急情况',
    OTHER: '其他'
  }
  return reasonMap[reason] || reason
}
</script>

<style scoped lang="css">
.budget-adjust-container {
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

.budget-adjust-content {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.budget-card {
  background: white;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.card-title {
  margin: 0 0 20px 0;
  color: #303133;
  font-size: 16px;
}

.original-budget {
  padding: 16px;
  background: #f8f9fa;
  border-radius: 4px;
}

.info-row {
  display: flex;
  gap: 30px;
  margin-bottom: 12px;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

.info-item label {
  min-width: 80px;
  color: #606266;
  font-weight: 500;
}

.info-item .amount {
  color: #f56c6c;
  font-weight: 600;
  font-size: 16px;
}

.adjusted-budget {
  padding-top: 10px;
}

.budget-summary {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
  margin-bottom: 20px;
  padding: 16px;
  background: #f8f9fa;
  border-radius: 4px;
}

.summary-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.summary-item .label {
  color: #606266;
  font-weight: 500;
}

.summary-item .value {
  font-weight: 600;
  font-size: 16px;
}

.summary-item .value.positive {
  color: #67c23a;
}

.summary-item .value.negative {
  color: #f56c6c;
}

.summary-item .value.final-amount {
  color: #409eff;
}

.impact-analysis {
  padding: 16px;
  background: #ecf5ff;
  border-radius: 4px;
}

.impact-analysis h4 {
  margin: 0 0 12px 0;
  color: #303133;
  font-size: 14px;
}

.impact-summary {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.impact-item {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #606266;
}

.impact-item.positive {
  color: #67c23a;
}

.impact-item.negative {
  color: #f56c6c;
}

.impact-item.warning {
  color: #e6a23c;
}

.action-buttons {
  display: flex;
  justify-content: center;
  gap: 20px;
  margin-top: 30px;
  padding: 20px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

/* 预览样式 */
.preview-content {
  padding: 10px 0;
}

.preview-header {
  text-align: center;
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 2px solid #ebeef5;
}

.preview-header h3 {
  margin: 0 0 8px 0;
  color: #303133;
}

.preview-section {
  margin-bottom: 20px;
}

.preview-section h4 {
  margin: 0 0 12px 0;
  color: #303133;
  font-size: 14px;
  padding-left: 8px;
  border-left: 3px solid #409eff;
}

.preview-row {
  display: flex;
  gap: 20px;
  margin-bottom: 12px;
}

.preview-item {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 8px;
}

.preview-item label {
  min-width: 80px;
  color: #606266;
  font-weight: 500;
}

.preview-item .amount {
  color: #409eff;
  font-weight: 600;
}

.preview-description {
  padding: 12px;
  background: #f8f9fa;
  border-radius: 4px;
  margin: 0;
  line-height: 1.6;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .budget-adjust-container {
    padding: 10px;
  }
  
  .budget-card {
    padding: 16px;
  }
  
  .info-row {
    flex-direction: column;
    gap: 12px;
  }
  
  .budget-summary {
    grid-template-columns: 1fr;
    gap: 12px;
  }
  
  .action-buttons {
    flex-direction: column;
    align-items: center;
  }
  
  .preview-row {
    flex-direction: column;
    gap: 12px;
  }
}
</style>