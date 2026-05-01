<template>
  <div class="expense-edit-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2>编辑费用申请单</h2>
      <p>申请单号: {{ expenseData.applyCode || '加载中...' }}</p>
    </div>

    <div class="expense-edit-content">
      <!-- 申请单基本信息 -->
      <div class="basic-info-section">
        <div class="section-header">
          <h3>申请单基本信息</h3>
        </div>
        <el-form 
          ref="basicFormRef" 
          :model="basicForm" 
          :rules="basicFormRules" 
          label-width="120px"
          class="basic-form"
        >
          <el-row :gutter="20">
            <el-col :xs="24" :sm="12" :md="8">
              <el-form-item label="申请部门" prop="departmentId">
                <el-select v-model="basicForm.departmentId" placeholder="选择申请部门" style="width: 100%">
                  <el-option v-for="dept in departmentList" :key="dept.id" :label="dept.deptName" :value="dept.id" />
                </el-select>
              </el-form-item>
            </el-col>
            
            <el-col :xs="24" :sm="12" :md="8">
              <el-form-item label="申请人" prop="applicant">
                <el-input v-model="basicForm.applicant" placeholder="请输入申请人姓名" readonly />
              </el-form-item>
            </el-col>
            
            <el-col :xs="24" :sm="12" :md="8">
              <el-form-item label="申请日期" prop="applyDate">
                <el-date-picker 
                  v-model="basicForm.applyDate" 
                  type="date" 
                  placeholder="选择申请日期" 
                  style="width: 100%"
                  readonly
                />
              </el-form-item>
            </el-col>
          </el-row>
          
          <el-row :gutter="20">
            <el-col :xs="24" :sm="12" :md="8">
              <el-form-item label="预算科目" prop="budgetSubjectId">
                <el-cascader
                  v-model="basicForm.budgetSubjectId"
                  :options="budgetSubjectOptions"
                  :props="{ value: 'id', label: 'name', children: 'children' }"
                  placeholder="选择预算科目"
                  style="width: 100%"
                  clearable
                  @change="handleBudgetSubjectChange"
                />
              </el-form-item>
            </el-col>
            
            <el-col :xs="24" :sm="12" :md="8">
              <el-form-item label="费用类型" prop="expenseTypeId">
                <el-select v-model="basicForm.expenseTypeId" placeholder="选择费用类型" style="width: 100%">
                  <el-option v-for="type in expenseTypeList" :key="type.id" :label="type.name" :value="type.id" />
                </el-select>
              </el-form-item>
            </el-col>
            
            <el-col :xs="24" :sm="12" :md="8">
              <el-form-item label="紧急程度" prop="urgency">
                <el-select v-model="basicForm.urgency" placeholder="选择紧急程度" style="width: 100%">
                  <el-option label="普通" value="LOW" />
                  <el-option label="较急" value="MEDIUM" />
                  <el-option label="紧急" value="HIGH" />
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>
          
          <el-form-item label="申请事由" prop="reason">
            <el-input 
              v-model="basicForm.reason" 
              type="textarea" 
              :rows="3" 
              placeholder="请输入详细的申请事由" 
              maxlength="500"
              show-word-limit
            />
          </el-form-item>
          
          <el-form-item label="附件上传">
            <el-upload
              class="upload-demo"
              action="#"
              :on-preview="handlePreview"
              :on-remove="handleRemove"
              :file-list="fileList"
              :before-upload="beforeUpload"
              multiple
              :limit="5"
              :on-exceed="handleExceed"
            >
              <el-button type="primary">
                <el-icon><upload /></el-icon>
                上传附件
              </el-button>
              <template #tip>
                <div class="el-upload__tip">
                  支持jpg/png/pdf/doc/docx格式文件，单个文件不超过10MB
                </div>
              </template>
            </el-upload>
          </el-form-item>
        </el-form>
      </div>

      <!-- 费用明细 -->
      <div class="expense-details-section">
        <div class="section-header">
          <h3>费用明细</h3>
          <div class="section-actions">
            <el-button type="primary" @click="handleAddDetail">
              <el-icon><plus /></el-icon>
              添加明细
            </el-button>
          </div>
        </div>
        
        <div class="details-table">
          <el-table :data="expenseDetails" border style="width: 100%">
            <el-table-column type="index" label="序号" width="60" align="center" />
            <el-table-column prop="itemName" label="费用项目" min-width="150">
              <template #default="{ row, $index }">
                <el-input 
                  v-model="row.itemName" 
                  placeholder="请输入费用项目名称" 
                  @blur="validateDetail($index)"
                />
              </template>
            </el-table-column>
            <el-table-column prop="specification" label="规格说明" min-width="120">
              <template #default="{ row, $index }">
                <el-input 
                  v-model="row.specification" 
                  placeholder="规格型号"
                  @blur="validateDetail($index)"
                />
              </template>
            </el-table-column>
            <el-table-column prop="quantity" label="数量" width="100" align="center">
              <template #default="{ row, $index }">
                <el-input-number 
                  v-model="row.quantity" 
                  :min="1" 
                  :precision="0" 
                  :controls="false"
                  style="width: 80px"
                  @change="calculateAmount($index)"
                />
              </template>
            </el-table-column>
            <el-table-column prop="unitPrice" label="单价(元)" width="120" align="right">
              <template #default="{ row, $index }">
                <el-input-number 
                  v-model="row.unitPrice" 
                  :min="0" 
                  :precision="2" 
                  :controls="false"
                  style="width: 100px"
                  @change="calculateAmount($index)"
                />
              </template>
            </el-table-column>
            <el-table-column prop="amount" label="金额(元)" width="120" align="right">
              <template #default="{ row }">
                {{ formatAmount(row.amount) }}
              </template>
            </el-table-column>
            <el-table-column prop="remark" label="备注" min-width="120">
              <template #default="{ row }">
                <el-input v-model="row.remark" placeholder="备注信息" />
              </template>
            </el-table-column>
            <el-table-column label="操作" width="80" align="center">
              <template #default="{ $index }">
                <el-button type="danger" size="small" link @click="handleRemoveDetail($index)">
                  <el-icon><delete /></el-icon>
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>
        
        <!-- 费用汇总 -->
        <div class="expense-summary">
          <div class="summary-row">
            <span>合计金额：</span>
            <span class="total-amount">¥{{ formatAmount(totalAmount) }}</span>
          </div>
          <div class="summary-row">
            <span>预算剩余额度：</span>
            <span :class="budgetStatus">¥{{ formatAmount(budgetRemaining) }}</span>
          </div>
        </div>
      </div>

      <!-- 底部操作栏 -->
      <div class="action-bar">
        <div class="action-left">
          <el-button @click="handleBack">
            <el-icon><arrow-left /></el-icon>
            返回
          </el-button>
        </div>
        <div class="action-right">
          <el-button @click="handleReset">
            <el-icon><refresh /></el-icon>
            重置修改
          </el-button>
          <el-button type="warning" @click="handleSaveDraft">
            <el-icon><document /></el-icon>
            保存草稿
          </el-button>
          <el-button type="success" @click="handleSave">
            <el-icon><check /></el-icon>
            保存修改
          </el-button>
          <el-button type="primary" @click="handleSubmit">
            <el-icon><send /></el-icon>
            提交审批
          </el-button>
        </div>
      </div>
    </div>

    <!-- 提交审批确认弹窗 -->
    <ExpenseSubmitModal
      v-model="submitModalVisible"
      :expense-data="submitExpenseData"
      :total-amount="totalAmount"
      :budget-status="budgetStatus"
      @submit-success="handleSubmitSuccess"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ArrowLeft, Plus, Upload, Refresh, Document, Check, Send, Delete } from '@element-plus/icons-vue'
import { showSuccess, showWarning, showError } from '@/utils/message''
import { ElMessageBox } from 'element-plus'
import ExpenseSubmitModal from './components/ExpenseSubmitModal.vue'

// 路由和状态管理
const route = useRoute()
const router = useRouter()

// 表单引用
const basicFormRef = ref()

// 申请单数据
const expenseData = ref<any>({})

// 基本信息表单数据
const basicForm = reactive({
  departmentId: '',
  applicant: '当前用户',
  applyDate: new Date(),
  budgetSubjectId: [],
  expenseTypeId: '',
  urgency: 'LOW',
  reason: ''
})

// 表单验证规则
const basicFormRules = {
  departmentId: [
    { required: true, message: '请选择申请部门', trigger: 'blur' }
  ],
  budgetSubjectId: [
    { required: true, message: '请选择预算科目', trigger: 'blur' }
  ],
  expenseTypeId: [
    { required: true, message: '请选择费用类型', trigger: 'blur' }
  ],
  urgency: [
    { required: true, message: '请选择紧急程度', trigger: 'blur' }
  ],
  reason: [
    { required: true, message: '请输入申请事由', trigger: 'blur' },
    { min: 10, message: '申请事由至少10个字符', trigger: 'blur' }
  ]
}

// 费用明细数据
const expenseDetails = ref([
  {
    id: 1,
    itemName: '',
    specification: '',
    quantity: 1,
    unitPrice: 0,
    amount: 0,
    remark: ''
  }
])

// 附件列表
const fileList = ref([])

// 部门列表
const departmentList = ref([
  { id: '1', deptName: '技术部' },
  { id: '2', deptName: '财务部' },
  { id: '3', deptName: '市场部' },
  { id: '4', deptName: '人事部' },
  { id: '5', deptName: '行政部' }
])

// 预算科目选项
const budgetSubjectOptions = ref([
  {
    id: '1',
    name: '差旅费用',
    children: [
      { id: '101', name: '交通费用' },
      { id: '102', name: '住宿费用' },
      { id: '103', name: '餐饮费用' }
    ]
  },
  {
    id: '2',
    name: '办公费用',
    children: [
      { id: '201', name: '办公用品' },
      { id: '202', name: '通讯费用' }
    ]
  }
])

// 费用类型列表
const expenseTypeList = ref([
  { id: '1', name: '常规报销' },
  { id: '2', name: '采购申请' },
  { id: '3', name: '差旅报销' },
  { id: '4', name: '会议费用' }
])

// 计算总金额
const totalAmount = computed(() => {
  return expenseDetails.value.reduce((sum, detail) => sum + detail.amount, 0)
})

// 预算剩余额度（模拟数据）
const budgetRemaining = computed(() => {
  return 50000 - totalAmount.value
})

// 预算状态
const budgetStatus = computed(() => {
  const remainingRate = budgetRemaining.value / 50000
  if (remainingRate < 0.1) return 'critical'
  if (remainingRate < 0.3) return 'warning'
  return 'normal'
})

// 提交审批相关状态
const submitModalVisible = ref(false)
const submitExpenseData = computed(() => ({
  ...expenseData.value,
  ...basicForm,
  departmentId: basicForm.departmentId,
  reason: basicForm.reason,
  totalAmount: totalAmount.value
}))

// 生命周期
onMounted(() => {
  loadExpenseData()
})

// 方法定义
const loadExpenseData = async () => {
  const expenseId = route.params.id
  if (!expenseId) {
    showError('申请单ID不存在')
    router.push('/expense/list')
    return
  }

  try {
    // TODO: 调用API获取申请单数据
    await new Promise(resolve => setTimeout(resolve, 500))
    
    // 模拟数据
    expenseData.value = {
      id: expenseId,
      applyCode: 'EXP2024010004',
      applicant: '张经理',
      departmentId: '1',
      applyDate: new Date('2024-01-12'),
      budgetSubjectId: ['1', '101'],
      expenseTypeId: '1',
      urgency: 'LOW',
      reason: '采购办公用品，包括打印机、办公桌椅等',
      details: [
        {
          id: 1,
          itemName: '打印机',
          specification: 'HP LaserJet Pro',
          quantity: 1,
          unitPrice: 1500,
          amount: 1500,
          remark: '彩色激光打印机'
        },
        {
          id: 2,
          itemName: '办公桌椅',
          specification: '标准套装',
          quantity: 2,
          unitPrice: 800,
          amount: 1600,
          remark: '员工办公使用'
        }
      ],
      attachments: []
    }
    
    // 填充表单数据
    Object.assign(basicForm, {
      departmentId: expenseData.value.departmentId,
      applicant: expenseData.value.applicant,
      applyDate: expenseData.value.applyDate,
      budgetSubjectId: expenseData.value.budgetSubjectId,
      expenseTypeId: expenseData.value.expenseTypeId,
      urgency: expenseData.value.urgency,
      reason: expenseData.value.reason
    })
    
    expenseDetails.value = [...expenseData.value.details]
  } catch (error) {
    console.error('加载申请单数据失败:', error)
    showError('加载申请单数据失败')
    router.push('/expense/list')
  }
}

const handleBudgetSubjectChange = (value: any) => {
  console.log('选择的预算科目:', value)
}

const handleAddDetail = () => {
  expenseDetails.value.push({
    id: Date.now(),
    itemName: '',
    specification: '',
    quantity: 1,
    unitPrice: 0,
    amount: 0,
    remark: ''
  })
}

const handleRemoveDetail = (index: number) => {
  if (expenseDetails.value.length > 1) {
    expenseDetails.value.splice(index, 1)
  } else {
    showWarning('至少需要保留一项费用明细')
  }
}

const validateDetail = (index: number) => {
  const detail = expenseDetails.value[index]
  if (!detail.itemName.trim()) {
    showError(`第${index + 1}行的费用项目名称不能为空`)
    return false
  }
  return true
}

const calculateAmount = (index: number) => {
  const detail = expenseDetails.value[index]
  detail.amount = detail.quantity * detail.unitPrice
}

const handlePreview = (file: any) => {
  console.log('预览文件:', file)
}

const handleRemove = (file: any, fileList: any) => {
  console.log('移除文件:', file, fileList)
}

const beforeUpload = (file: any) => {
  const isLt10M = file.size / 1024 / 1024 < 10
  if (!isLt10M) {
    showError('上传文件大小不能超过 10MB!')
    return false
  }
  return true
}

const handleExceed = () => {
  showWarning('最多只能上传5个文件')
}

const handleBack = () => {
  router.push('/expense/list')
}

const handleReset = () => {
  // 重置到原始数据
  Object.assign(basicForm, {
    departmentId: expenseData.value.departmentId,
    applicant: expenseData.value.applicant,
    applyDate: expenseData.value.applyDate,
    budgetSubjectId: expenseData.value.budgetSubjectId,
    expenseTypeId: expenseData.value.expenseTypeId,
    urgency: expenseData.value.urgency,
    reason: expenseData.value.reason
  })
  
  expenseDetails.value = [...expenseData.value.details]
  fileList.value = []
}

const handleSaveDraft = async () => {
  try {
    // 验证基本信息表单
    await basicFormRef.value.validate()
    
    // 验证费用明细
    let isValid = true
    for (let i = 0; i < expenseDetails.value.length; i++) {
      if (!validateDetail(i)) {
        isValid = false
        break
      }
    }
    
    if (!isValid) return
    
    // TODO: 调用API保存草稿
    await new Promise(resolve => setTimeout(resolve, 500))
    showSuccess('申请单已保存为草稿')
  } catch (error) {
    showError('表单验证失败，请检查输入')
  }
}

const handleSave = async () => {
  try {
    // 验证基本信息表单
    await basicFormRef.value.validate()
    
    // 验证费用明细
    let isValid = true
    for (let i = 0; i < expenseDetails.value.length; i++) {
      if (!validateDetail(i)) {
        isValid = false
        break
      }
    }
    
    if (!isValid) return
    
    // 检查总金额
    if (totalAmount.value <= 0) {
      showError('费用明细总金额必须大于0')
      return
    }
    
    // TODO: 调用API保存修改
    await new Promise(resolve => setTimeout(resolve, 500))
    showSuccess('申请单修改已保存')
    
    // 更新原始数据
    expenseData.value = {
      ...expenseData.value,
      ...basicForm,
      details: [...expenseDetails.value]
    }
  } catch (error) {
    showError('保存失败，请检查表单输入')
  }
}

// 验证表单，但不提交
const validateSubmitForm = async () => {
  try {
    // 验证基本信息表单
    await basicFormRef.value.validate()
    
    // 验证费用明细
    if (expenseDetails.value.length === 0) {
      showError('至少需要添加一项费用明细')
      return false
    }
    
    let isValid = true
    for (let i = 0; i < expenseDetails.value.length; i++) {
      if (!validateDetail(i)) {
        isValid = false
        break
      }
    }
    
    if (!isValid) return false
    
    // 检查总金额
    if (totalAmount.value <= 0) {
      showError('费用明细总金额必须大于0')
      return false
    }
    
    // 检查预算额度
    if (budgetRemaining.value < 0) {
      showError('申请金额超过预算剩余额度，请调整申请内容')
      return false
    }
    
    return true
  } catch (error) {
    showError('表单验证失败，请检查输入内容')
    return false
  }
}

const handleSubmit = async () => {
  const isValid = await validateSubmitForm()
  if (!isValid) return
  
  // 显示提交审批确认弹窗
  submitModalVisible.value = true
}

const handleSubmitSuccess = () => {
  // 提交成功后跳转到列表页面
  showSuccess('费用申请已成功提交审批')
  router.push('/expense/list')
}

const formatAmount = (amount: number) => {
  return amount?.toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 }) || '0.00'
}
</script>

<style scoped lang="css">
.expense-edit-container {
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

.expense-edit-content {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.basic-info-section,
.expense-details-section {
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

.section-actions {
  display: flex;
  gap: 10px;
}

.basic-form {
  margin-top: 20px;
}

.details-table {
  margin-bottom: 20px;
}

.expense-summary {
  background: #f8f9fa;
  border-radius: 4px;
  padding: 16px;
  margin-top: 20px;
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

.critical {
  color: #f56c6c;
  font-weight: 600;
}

.warning {
  color: #e6a23c;
  font-weight: 600;
}

.normal {
  color: #67c23a;
  font-weight: 600;
}

.action-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: white;
  border-radius: 8px;
  padding: 16px 20px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  position: sticky;
  bottom: 20px;
  z-index: 100;
}

.action-left,
.action-right {
  display: flex;
  gap: 12px;
}

/* 表格样式优化 */
:deep(.el-table) {
  border-radius: 4px;
}

:deep(.el-table th) {
  background: #f5f7fa;
  color: #606266;
  font-weight: 600;
}

:deep(.el-input-number) {
  width: 100%;
}

:deep(.el-input-number .el-input__inner) {
  text-align: center;
}

:deep(.el-input-number .el-input__inner[type="number"]) {
  -moz-appearance: textfield;
}

:deep(.el-input-number .el-input__inner[type="number"]::-webkit-outer-spin-button,
:deep(.el-input-number .el-input__inner[type="number"]::-webkit-inner-spin-button) {
  -webkit-appearance: none;
  margin: 0;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .expense-edit-container {
    padding: 10px;
  }
  
  .basic-info-section,
  .expense-details-section {
    padding: 15px;
  }
  
  .action-bar {
    flex-direction: column;
    gap: 16px;
    align-items: stretch;
  }
  
  .action-left,
  .action-right {
    justify-content: center;
  }
  
  .summary-row {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }
}
</style>