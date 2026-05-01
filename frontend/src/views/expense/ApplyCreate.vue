<template>
  <div class="expense-create-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <h1>新建费用申请</h1>
      <div class="header-actions">
        <el-button @click="handleReset">重置</el-button>
        <el-button type="primary" @click="handleSaveDraft">保存草稿</el-button>
        <el-button type="success" @click="handleSubmit" :loading="submitting">
          {{ submitting ? '提交中...' : '提交申请' }}
        </el-button>
      </div>
    </div>

    <!-- 表单内容 -->
    <div class="form-content">
      <!-- 申请单基本信息 -->
      <el-card class="form-section">
        <template #header>
          <div class="card-header">
            <span class="card-title">申请单信息</span>
            <el-button type="text" @click="copyLastApply">复制最近申请</el-button>
          </div>
        </template>

        <el-form
          ref="baseFormRef"
          :model="applyForm"
          :rules="baseRules"
          label-width="120px"
          class="base-form"
        >
          <el-row :gutter="24">
            <el-col :span="12">
              <el-form-item label="申请事由" prop="reason">
                <el-input
                  v-model="applyForm.reason"
                  type="textarea"
                  :rows="2"
                  placeholder="请输入费用申请的具体事由"
                  maxlength="200"
                  show-word-limit
                />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="申请部门" prop="departmentId">
                <el-select
                  v-model="applyForm.departmentId"
                  placeholder="请选择申请部门"
                  style="width: 100%"
                >
                  <el-option
                    v-for="dept in departmentOptions"
                    :key="dept.id"
                    :label="dept.deptName"
                    :value="dept.id"
                  />
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>

          <el-row :gutter="24">
            <el-col :span="12">
              <el-form-item label="申请金额" prop="estimatedAmount">
                <el-input-number
                  v-model="applyForm.estimatedAmount"
                  :min="0"
                  :precision="2"
                  :step="100"
                  placeholder="请输入预估金额"
                  style="width: 100%"
                >
                  <template #prefix>¥</template>
                </el-input-number>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="申请日期" prop="applyDate">
                <el-date-picker
                  v-model="applyForm.applyDate"
                  type="date"
                  placeholder="选择申请日期"
                  style="width: 100%"
                  value-format="YYYY-MM-DD"
                />
              </el-form-item>
            </el-col>
          </el-row>

          <el-form-item label="费用描述" prop="description">
            <el-input
              v-model="applyForm.description"
              type="textarea"
              :rows="3"
              placeholder="详细描述费用用途和相关说明"
              maxlength="500"
              show-word-limit
            />
          </el-form-item>
        </el-form>
      </el-card>

      <!-- 费用明细列表 -->
      <el-card class="form-section">
        <template #header>
          <div class="card-header">
            <span class="card-title">费用明细</span>
            <el-button type="primary" @click="addExpenseDetail">添加明细</el-button>
          </div>
        </template>

        <div class="expense-list">
          <div 
            v-for="(item, index) in expenseDetails" 
            :key="item.id"
            class="expense-item"
          >
            <el-row :gutter="16" class="item-header">
              <el-col :span="20">
                <span class="item-index">明细 {{ index + 1 }}</span>
              </el-col>
              <el-col :span="4" style="text-align: right;">
                <el-button 
                  type="danger" 
                  link 
                  @click="removeExpenseDetail(index)"
                  :disabled="expenseDetails.length <= 1"
                >
                  删除
                </el-button>
              </el-col>
            </el-row>

            <el-form ref="detailForms" :model="item" class="detail-form">
              <el-row :gutter="24">
                <el-col :span="8">
                  <el-form-item label="费用类型" required>
                    <el-select
                      v-model="item.feeTypeId"
                      placeholder="选择费用类型"
                      style="width: 100%"
                      @change="onFeeTypeChange(index)"
                    >
                      <el-option
                        v-for="feeType in feeTypeOptions"
                        :key="feeType.id"
                        :label="feeType.feeTypeName"
                        :value="feeType.id"
                      />
                    </el-select>
                  </el-form-item>
                </el-col>
                <el-col :span="8">
                  <el-form-item label="金额" required>
                    <el-input-number
                      v-model="item.amount"
                      :min="0"
                      :precision="2"
                      :step="100"
                      placeholder="金额"
                      style="width: 100%"
                      @change="recalculateTotal"
                    >
                      <template #prefix>¥</template>
                    </el-input-number>
                  </el-form-item>
                </el-col>
                <el-col :span="8">
                  <el-form-item label="发生日期">
                    <el-date-picker
                      v-model="item.expenseDate"
                      type="date"
                      placeholder="发生日期"
                      style="width: 100%"
                      value-format="YYYY-MM-DD"
                    />
                  </el-form-item>
                </el-col>
              </el-row>

              <el-row :gutter="24">
                <el-col :span="24">
                  <el-form-item label="费用说明" prop="remark">
                    <el-input
                      v-model="item.remark"
                      type="textarea"
                      :rows="2"
                      placeholder="详细描述此笔费用的具体情况"
                      maxlength="200"
                      show-word-limit
                    />
                  </el-form-item>
                </el-col>
              </el-row>
            </el-form>
          </div>
        </div>

        <!-- 费用汇总 -->
        <div class="expense-summary">
          <el-row :gutter="24">
            <el-col :span="8">
              <div class="summary-item">
                <span class="label">合计金额：</span>
                <span class="value total-amount">¥{{ formatAmount(totalAmount) }}</span>
              </div>
            </el-col>
            <el-col :span="8">
              <div class="summary-item">
                <span class="label">可用预算：</span>
                <span class="value" :class="{ 'budget-warning': budgetStatus === 'warning' }">
                  ¥{{ formatAmount(availableBudget) }}
                </span>
              </div>
            </el-col>
            <el-col :span="8">
              <div class="summary-item">
                <span class="label">预算状态：</span>
                <el-tag :type="budgetStatusTag">{{ budgetStatusText }}</el-tag>
              </div>
            </el-col>
          </el-row>
        </div>
      </el-card>

      <!-- 附件上传 -->
      <el-card class="form-section">
        <template #header>
          <span class="card-title">附件材料</span>
        </template>

        <el-form-item label="证明材料">
          <el-upload
            v-model:file-list="attachmentList"
            action="/api/file/upload"
            multiple
            :limit="5"
            :before-upload="beforeUpload"
            :on-success="handleUploadSuccess"
            :on-error="handleUploadError"
            :on-remove="handleRemove"
            list-type="picture-card"
            accept=".jpg,.jpeg,.png,.pdf,.doc,.docx"
          >
            <el-icon><plus /></el-icon>
          </el-upload>
          <div class="upload-tips">
            支持上传图片、PDF、Word文档，单个文件不超过10MB，最多5个文件
          </div>
        </el-form-item>
      </el-card>
    </div>

    <!-- 提交确认弹窗 -->
    <ExpenseSubmitModal
      v-model="submitModalVisible"
      :expense-data="submitData"
      :total-amount="totalAmount"
      :budget-status="budgetStatus"
      @submit-success="handleSubmitSuccess"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, nextTick } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type UploadFile } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import ExpenseSubmitModal from './components/ExpenseSubmitModal.vue'

// 表单引用
const baseFormRef = ref<FormInstance>()
const detailForms = ref<FormInstance[]>([])

// 表单数据
const applyForm = reactive({
  reason: '',
  departmentId: '',
  estimatedAmount: 0,
  applyDate: new Date().toISOString().split('T')[0],
  description: ''
})

// 费用明细列表
const expenseDetails = ref([
  {
    id: Date.now(),
    feeTypeId: '',
    amount: 0,
    expenseDate: new Date().toISOString().split('T')[0],
    remark: ''
  }
])

// 表单验证规则
const baseRules = {
  reason: [
    { required: true, message: '请输入申请事由', trigger: 'blur' }
  ],
  departmentId: [
    { required: true, message: '请选择申请部门', trigger: 'change' }
  ],
  estimatedAmount: [
    { required: true, message: '请输入预估金额', trigger: 'blur' },
    { type: 'number', min: 0, message: '金额不能为负数', trigger: 'blur' }
  ],
  applyDate: [
    { required: true, message: '请选择申请日期', trigger: 'change' }
  ]
}

// 选项数据
const departmentOptions = ref([
  { id: '1', deptName: '技术部' },
  { id: '2', deptName: '财务部' },
  { id: '3', deptName: '市场部' },
  { id: '4', deptName: '人事部' },
  { id: '5', deptName: '行政部' }
])

const feeTypeOptions = ref([
  { id: '1', feeTypeName: '差旅费' },
  { id: '2', feeTypeName: '办公用品费' },
  { id: '3', feeTypeName: '招待费' },
  { id: '4', feeTypeName: '交通费' },
  { id: '5', feeTypeName: '培训费' },
  { id: '6', feeTypeName: '会议费' },
  { id: '7', feeTypeName: '其他费用' }
])

// 附件列表
const attachmentList = ref<UploadFile[]>([])
const attachmentIds = ref<string[]>([])

// 状态管理
const submitting = ref(false)
const submitModalVisible = ref(false)

// 计算属性
const totalAmount = computed(() => {
  return expenseDetails.value.reduce((total, item) => total + (item.amount || 0), 0)
})

const availableBudget = computed(() => {
  // 模拟可用预算数据
  return 10000
})

const budgetStatus = computed(() => {
  if (totalAmount.value > availableBudget.value) {
    return 'critical'
  } else if (totalAmount.value > availableBudget.value * 0.8) {
    return 'warning'
  } else {
    return 'normal'
  }
})

const budgetStatusTag = computed(() => {
  const statusMap = {
    critical: 'danger',
    warning: 'warning',
    normal: 'success'
  }
  return statusMap[budgetStatus.value]
})

const budgetStatusText = computed(() => {
  const textMap = {
    critical: '预算不足',
    warning: '预算预警',
    normal: '预算充足'
  }
  return textMap[budgetStatus.value]
})

const submitData = computed(() => ({
  id: Date.now().toString(),
  applyCode: 'EA' + Date.now(),
  reason: applyForm.reason,
  departmentId: applyForm.departmentId,
  totalAmount: totalAmount.value,
  estimatedAmount: applyForm.estimatedAmount
}))

// 方法定义
const formatAmount = (amount: number) => {
  return amount?.toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 }) || '0.00'
}

const addExpenseDetail = () => {
  if (expenseDetails.value.length >= 10) {
    ElMessage.warning('最多只能添加10条费用明细')
    return
  }
  
  expenseDetails.value.push({
    id: Date.now() + Math.random(),
    feeTypeId: '',
    amount: 0,
    expenseDate: new Date().toISOString().split('T')[0],
    remark: ''
  })
}

const removeExpenseDetail = (index: number) => {
  if (expenseDetails.value.length <= 1) {
    ElMessage.warning('至少需要保留一条费用明细')
    return
  }
  
  expenseDetails.value.splice(index, 1)
  recalculateTotal()
}

const recalculateTotal = () => {
  // 金额变化时更新预估金额
  if (totalAmount.value > applyForm.estimatedAmount) {
    applyForm.estimatedAmount = totalAmount.value
  }
}

const onFeeTypeChange = (index: number) => {
  // 费用类型变化时的处理
  console.log('费用类型变化:', expenseDetails.value[index].feeTypeId)
}

const handleReset = async () => {
  try {
    await ElMessageBox.confirm(
      '确定要重置表单吗？所有填写的内容将会被清空。',
      '确认重置',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    // 重置表单数据
    Object.assign(applyForm, {
      reason: '',
      departmentId: '',
      estimatedAmount: 0,
      applyDate: new Date().toISOString().split('T')[0],
      description: ''
    })
    
    expenseDetails.value = [{
      id: Date.now(),
      feeTypeId: '',
      amount: 0,
      expenseDate: new Date().toISOString().split('T')[0],
      remark: ''
    }]
    
    attachmentList.value = []
    attachmentIds.value = []
    
    ElMessage.success('表单已重置')
  } catch {
    // 用户取消重置
  }
}

const handleSaveDraft = async () => {
  try {
    // 验证基础表单
    await baseFormRef.value?.validate()
    
    // 验证费用明细
    const invalidDetails = expenseDetails.value.filter(item => !item.feeTypeId || item.amount <= 0)
    if (invalidDetails.length > 0) {
      ElMessage.error('请完善所有费用明细信息')
      return
    }

    const draftData = {
      baseInfo: applyForm,
      details: expenseDetails.value,
      attachments: attachmentIds.value,
      totalAmount: totalAmount.value,
      createTime: new Date().toISOString()
    }

    // TODO: 调用保存草稿API
    console.log('保存草稿数据:', draftData)
    
    ElMessage.success('草稿保存成功')
  } catch (error) {
    ElMessage.error('请完善表单信息')
  }
}

const handleSubmit = async () => {
  try {
    // 验证基础表单
    await baseFormRef.value?.validate()
    
    // 验证费用明细
    const invalidDetails = expenseDetails.value.filter(item => !item.feeTypeId || item.amount <= 0)
    if (invalidDetails.length > 0) {
      ElMessage.error('请完善所有费用明细信息')
      return
    }

    // 检查预算状态
    if (budgetStatus.value === 'critical') {
      await ElMessageBox.confirm(
        '当前申请金额已超过可用预算额度，是否继续提交？',
        '预算不足提醒',
        {
          confirmButtonText: '继续提交',
          cancelButtonText: '取消',
          type: 'warning'
        }
      )
    }

    // 显示提交确认弹窗
    submitModalVisible.value = true

  } catch (error) {
    ElMessage.error('请完善表单信息')
  }
}

const handleSubmitSuccess = () => {
  // 提交成功后的处理
  submitting.value = false
  
  // 跳转到申请列表页面
  setTimeout(() => {
    ElMessage.success('费用申请已成功提交，正在跳转到申请列表...')
    // 这里可以添加路由跳转逻辑
  }, 1000)
}

// 附件相关方法
const beforeUpload = (file: File) => {
  const isLt10M = file.size / 1024 / 1024 < 10
  if (!isLt10M) {
    ElMessage.error('文件大小不能超过 10MB!')
    return false
  }
  return true
}

const handleUploadSuccess = (response: any, file: UploadFile) => {
  ElMessage.success('文件上传成功')
  attachmentIds.value.push(response.data.fileId)
}

const handleUploadError = (error: any) => {
  ElMessage.error('文件上传失败')
}

const handleRemove = (file: UploadFile) => {
  const index = attachmentIds.value.findIndex(id => id === file.uid)
  if (index > -1) {
    attachmentIds.value.splice(index, 1)
  }
}

const copyLastApply = () => {
  ElMessage.info('复制最近申请功能开发中...')
}

// 生命周期
onMounted(() => {
  // 初始化数据
  console.log('费用申请页面初始化完成')
})
</script>

<style scoped lang="css">
.expense-create-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 1px solid #ebeef5;
}

.page-header h1 {
  margin: 0;
  color: #303133;
  font-size: 24px;
  font-weight: 500;
}

.header-actions {
  display: flex;
  gap: 12px;
}

.form-content {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.form-section {
  border-radius: 8px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.base-form {
  max-width: 800px;
}

.expense-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.expense-item {
  border: 1px solid #ebeef5;
  border-radius: 6px;
  padding: 16px;
  background: #fafafa;
}

.item-header {
  margin-bottom: 16px;
}

.item-index {
  font-weight: 600;
  color: #606266;
}

.detail-form {
  margin-left: -8px;
  margin-right: -8px;
}

.expense-summary {
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid #ebeef5;
  background: #f8f9fa;
  border-radius: 6px;
  padding: 16px;
}

.summary-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
}

.summary-item .label {
  font-size: 12px;
  color: #909399;
  margin-bottom: 4px;
}

.summary-item .value {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.total-amount {
  color: #f56c6c;
  font-size: 18px;
}

.budget-warning {
  color: #e6a23c;
}

.upload-tips {
  font-size: 12px;
  color: #909399;
  margin-top: 8px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .expense-create-container {
    padding: 12px;
  }
  
  .page-header {
    flex-direction: column;
    gap: 16px;
    align-items: stretch;
  }
  
  .header-actions {
    justify-content: stretch;
  }
  
  .header-actions .el-button {
    flex: 1;
  }
}
</style>