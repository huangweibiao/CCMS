<template>
  <div class="budget-create-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2>预算编制</h2>
      <p>创建新的年度预算或部门预算</p>
    </div>

    <div class="budget-create-content">
      <!-- 基础信息卡片 -->
      <div class="budget-card">
        <h3 class="card-title">基础信息</h3>
        <el-form :model="budgetForm" :rules="budgetRules" ref="budgetFormRef" label-width="120px">
          <el-row :gutter="20">
            <el-col :xs="24" :sm="12" :md="8">
              <el-form-item label="预算年度" prop="budgetYear">
                <el-date-picker
                  v-model="budgetForm.budgetYear"
                  type="year"
                  placeholder="选择预算年度"
                  format="YYYY"
                  value-format="YYYY"
                  style="width: 100%"
                />
              </el-form-item>
            </el-col>
            <el-col :xs="24" :sm="12" :md="8">
              <el-form-item label="预算类型" prop="budgetType">
                <el-select v-model="budgetForm.budgetType" placeholder="请选择预算类型" style="width: 100%">
                  <el-option label="年度预算" value="ANNUAL" />
                  <el-option label="季度预算" value="QUARTERLY" />
                  <el-option label="月度预算" value="MONTHLY" />
                  <el-option label="项目预算" value="PROJECT" />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :xs="24" :sm="12" :md="8">
              <el-form-item label="责任部门" prop="departmentId">
                <el-select v-model="budgetForm.departmentId" placeholder="请选择部门" style="width: 100%">
                  <el-option 
                    v-for="dept in departmentList" 
                    :key="dept.id" 
                    :label="dept.deptName" 
                    :value="dept.id" 
                  />
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>
          
          <el-row :gutter="20">
            <el-col :xs="24" :sm="12" :md="8">
              <el-form-item label="预算总额" prop="totalAmount">
                <el-input-number
                  v-model="budgetForm.totalAmount"
                  :min="0"
                  :precision="2"
                  :step="1000"
                  placeholder="请输入预算总额"
                  style="width: 100%"
                >
                  <template #prefix>¥</template>
                </el-input-number>
              </el-form-item>
            </el-col>
            <el-col :xs="24" :sm="12" :md="8">
              <el-form-item label="币种" prop="currency">
                <el-select v-model="budgetForm.currency" placeholder="请选择币种" style="width: 100%">
                  <el-option label="人民币" value="CNY" />
                  <el-option label="美元" value="USD" />
                  <el-option label="欧元" value="EUR" />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :xs="24" :sm="24" :md="8">
              <el-form-item label="预算描述" prop="description">
                <el-input
                  v-model="budgetForm.description"
                  placeholder="请输入预算描述"
                  maxlength="200"
                  show-word-limit
                />
              </el-form-item>
            </el-col>
          </el-row>
        </el-form>
      </div>

      <!-- 预算明细卡片 -->
      <div class="budget-card">
        <div class="card-header">
          <h3 class="card-title">预算明细</h3>
          <div class="card-actions">
            <el-button type="primary" @click="handleAddDetail">
              <el-icon><plus /></el-icon>
              添加明细项
            </el-button>
          </div>
        </div>
        
        <div class="budget-details">
          <el-table :data="budgetForm.details" :border="true" size="small">
            <el-table-column type="index" label="序号" width="60" align="center" />
            <el-table-column label="费用类型" prop="expenseType" min-width="120">
              <template #default="{ row }">
                <el-select 
                  v-model="row.expenseType" 
                  placeholder="选择费用类型"
                  style="width: 100%"
                  @change="handleExpenseTypeChange(row)"
                >
                  <el-option 
                    v-for="type in expenseTypeList" 
                    :key="type.id" 
                    :label="type.typeName" 
                    :value="type.id" 
                  />
                </el-select>
              </template>
            </el-table-column>
            <el-table-column label="预算金额" prop="budgetAmount" min-width="120">
              <template #default="{ row }">
                <el-input-number
                  v-model="row.budgetAmount"
                  :min="0"
                  :precision="2"
                  placeholder="金额"
                  style="width: 100%"
                >
                  <template #prefix>¥</template>
                </el-input-number>
              </template>
            </el-table-column>
            <el-table-column label="预算周期" prop="budgetPeriod" min-width="120">
              <template #default="{ row }">
                <el-select v-model="row.budgetPeriod" placeholder="选择周期" style="width: 100%">
                  <el-option label="全年" value="YEAR" />
                  <el-option label="一季度" value="Q1" />
                  <el-option label="二季度" value="Q2" />
                  <el-option label="三季度" value="Q3" />
                  <el-option label="四季度" value="Q4" />
                  <el-option label="月度" value="MONTH" />
                </el-select>
              </template>
            </el-table-column>
            <el-table-column label="说明" prop="remark" min-width="150">
              <template #default="{ row }">
                <el-input v-model="row.remark" placeholder="请输入说明" />
              </template>
            </el-table-column>
            <el-table-column label="操作" width="80" align="center">
              <template #default="{ $index }">
                <el-button 
                  type="danger" 
                  link 
                  size="small" 
                  @click="handleRemoveDetail($index)"
                >
                  删除
                </el-button>
              </template>
            </el-table-column>
          </el-table>
          
          <div class="budget-summary">
            <div class="summary-item">
              <span class="label">明细总额：</span>
              <span class="value">¥{{ detailTotalAmount.toFixed(2) }}</span>
            </div>
            <div class="summary-item">
              <span class="label">剩余额度：</span>
              <span class="value" :class="{ 'negative': remainingAmount < 0 }">
                ¥{{ remainingAmount.toFixed(2) }}
              </span>
            </div>
          </div>
        </div>
      </div>

      <!-- 操作按钮区域 -->
      <div class="action-buttons">
        <el-button type="primary" @click="handleSubmit" :loading="submitting">
          提交预算
        </el-button>
        <el-button @click="handleSaveDraft" :loading="saving">
          保存草稿
        </el-button>
        <el-button @click="handleReset">
          重置
        </el-button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { showSuccess, showError } from '@/components/common/Message.vue'

// 表单引用
const budgetFormRef = ref<FormInstance>()

// 表单数据
const budgetForm = reactive({
  budgetYear: '',
  budgetType: 'ANNUAL',
  departmentId: '',
  totalAmount: 0,
  currency: 'CNY',
  description: '',
  details: [] as Array<{
    expenseType: string
    budgetAmount: number
    budgetPeriod: string
    remark: string
  }>
})

// 表单验证规则
const budgetRules: FormRules = {
  budgetYear: [
    { required: true, message: '请选择预算年度', trigger: 'change' }
  ],
  budgetType: [
    { required: true, message: '请选择预算类型', trigger: 'change' }
  ],
  departmentId: [
    { required: true, message: '请选择责任部门', trigger: 'change' }
  ],
  totalAmount: [
    { required: true, message: '请输入预算总额', trigger: 'blur' },
    { type: 'number', min: 0, message: '预算总额必须大于0', trigger: 'blur' }
  ]
}

// 部门列表
const departmentList = ref([
  { id: '1', deptName: '技术部' },
  { id: '2', deptName: '财务部' },
  { id: '3', deptName: '人事部' },
  { id: '4', deptName: '市场部' },
  { id: '5', deptName: '行政部' }
])

// 费用类型列表
const expenseTypeList = ref([
  { id: 'OFFICE_SUPPLIES', typeName: '办公用品' },
  { id: 'TRAVEL_EXPENSES', typeName: '差旅费用' },
  { id: 'ENTERTAINMENT', typeName: '招待费用' },
  { id: 'MARKETING', typeName: '市场推广' },
  { id: 'TRAINING', typeName: '培训费用' },
  { id: 'EQUIPMENT', typeName: '设备购置' },
  { id: 'SOFTWARE', typeName: '软件采购' },
  { id: 'OTHERS', typeName: '其他费用' }
])

// 计算属性
const detailTotalAmount = computed(() => {
  return budgetForm.details.reduce((sum, item) => sum + (item.budgetAmount || 0), 0)
})

const remainingAmount = computed(() => {
  return (budgetForm.totalAmount || 0) - detailTotalAmount.value
})

// 加载状态
const submitting = ref(false)
const saving = ref(false)

// 生命周期
onMounted(() => {
  // 设置默认预算年度为当前年份
  const currentYear = new Date().getFullYear().toString()
  budgetForm.budgetYear = currentYear
  
  // 加载部门列表和费用类型
  loadDepartmentList()
  loadExpenseTypes()
})

// 方法
const loadDepartmentList = async () => {
  try {
    // TODO: 调用API获取部门列表
    // 使用模拟数据
  } catch (error) {
    console.error('加载部门列表失败:', error)
  }
}

const loadExpenseTypes = async () => {
  try {
    // TODO: 调用API获取费用类型列表
    // 使用模拟数据
  } catch (error) {
    console.error('加载费用类型失败:', error)
  }
}

const handleAddDetail = () => {
  budgetForm.details.push({
    expenseType: '',
    budgetAmount: 0,
    budgetPeriod: 'YEAR',
    remark: ''
  })
}

const handleRemoveDetail = (index: number) => {
  budgetForm.details.splice(index, 1)
}

const handleExpenseTypeChange = (row: any) => {
  const type = expenseTypeList.value.find(t => t.id === row.expenseType)
  if (type && !row.remark) {
    row.remark = `${type.typeName}相关预算`
  }
}

const handleSubmit = async () => {
  if (!budgetFormRef.value) return

  try {
    const valid = await budgetFormRef.value.validate()
    if (!valid) return
    
    // 验证明细数据
    if (budgetForm.details.length === 0) {
      showError('请至少添加一个预算明细项')
      return
    }
    
    // 验证明细金额合计
    if (detailTotalAmount.value > budgetForm.totalAmount) {
      showError('明细金额合计不能超过预算总额')
      return
    }
    
    submitting.value = true
    
    // TODO: 调用提交预算API
    console.log('提交预算:', budgetForm)
    
    showSuccess('预算提交成功')
    handleReset()
  } catch (error) {
    console.error('提交预算失败:', error)
    showError('提交失败，请检查表单数据')
  } finally {
    submitting.value = false
  }
}

const handleSaveDraft = async () => {
  if (!budgetFormRef.value) return

  try {
    const valid = await budgetFormRef.value.validate()
    if (!valid) return
    
    saving.value = true
    
    // TODO: 调用保存草稿API
    console.log('保存草稿:', budgetForm)
    
    showSuccess('草稿保存成功')
  } catch (error) {
    console.error('保存草稿失败:', error)
    showError('保存失败，请检查表单数据')
  } finally {
    saving.value = false
  }
}

const handleReset = () => {
  budgetFormRef.value?.resetFields()
  budgetForm.details = []
  
  // 保留默认年度
  const currentYear = new Date().getFullYear().toString()
  budgetForm.budgetYear = currentYear
}
</script>

<style scoped lang="css">
.budget-create-container {
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

.budget-create-content {
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

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.card-title {
  margin: 0;
  color: #303133;
  font-size: 16px;
}

.card-actions {
  display: flex;
  gap: 10px;
}

.budget-details {
  margin-top: 16px;
}

.budget-summary {
  display: flex;
  justify-content: flex-end;
  gap: 30px;
  margin-top: 16px;
  padding: 12px 0;
  border-top: 1px solid #ebeef5;
}

.summary-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

.summary-item .label {
  color: #606266;
  font-weight: 500;
}

.summary-item .value {
  color: #409eff;
  font-weight: 600;
  font-size: 16px;
}

.summary-item .value.negative {
  color: #f56c6c;
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

/* 响应式设计 */
@media (max-width: 768px) {
  .budget-create-container {
    padding: 10px;
  }
  
  .budget-card {
    padding: 16px;
  }
  
  .card-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }
  
  .action-buttons {
    flex-direction: column;
    align-items: center;
  }
  
  .budget-summary {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }
}
</style>