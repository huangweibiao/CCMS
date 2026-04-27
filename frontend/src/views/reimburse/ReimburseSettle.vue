<template>
  <div class="reimburse-settle-container">
    <!-- 页面标题和操作 -->
    <div class="page-header">
      <div class="header-left">
        <h2>借款核销</h2>
        <p>对已审批通过的报销单进行借款核销处理</p>
      </div>
      <div class="header-actions">
        <el-button @click="handleBack">
          <el-icon><arrow-left /></el-icon>
          返回列表
        </el-button>
      </div>
    </div>

    <div class="settle-content">
      <!-- 核销信息 -->
      <div class="settle-info-section">
        <div class="section-header">
          <h3>核销信息</h3>
        </div>
        
        <div class="settle-form">
          <el-scrollbar max-height="500px">
            <div class="form-section">
              <h4>基本信息</h4>
              <el-descriptions :column="2" border>
                <el-descriptions-item label="报销单号">{{ settleInfo.reimburseCode }}</el-descriptions-item>
                <el-descriptions-item label="报销金额">
                  <span class="amount">¥{{ formatAmount(settleInfo.totalAmount) }}</span>
                </el-descriptions-item>
                <el-descriptions-item label="预借款金额">
                  <span class="advance-amount">¥{{ formatAmount(settleInfo.advanceAmount) }}</span>
                </el-descriptions-item>
                <el-descriptions-item label="应报销金额">
                  <span class="final-amount">¥{{ formatAmount(settleInfo.totalAmount - settleInfo.advanceAmount) }}</span>
                </el-descriptions-item>
                <el-descriptions-item label="报销人">{{ settleInfo.reimbursePerson }}</el-descriptions-item>
                <el-descriptions-item label="报销部门">{{ settleInfo.departmentName }}</el-descriptions-item>
                <el-descriptions-item label="审批时间">{{ formatDateTime(settleInfo.approvalTime) }}</el-descriptions-item>
              </el-descriptions>
            </div>
            
            <div class="form-section">
              <h4>核销确认</h4>
              <el-form ref="settleFormRef" :model="settleForm" :rules="settleRules" label-width="120px">
                <el-form-item label="核销结算方式" prop="settleType">
                  <el-radio-group v-model="settleForm.settleType">
                    <el-radio label="BANK_TRANSFER">银行转账</el-radio>
                    <el-radio label="CASH">现金支付</el-radio>
                    <el-radio label="OFFSET">全额核销</el-radio>
                  </el-radio-group>
                </el-form-item>
                
                <el-form-item v-if="settleForm.settleType === 'BANK_TRANSFER'" label="收款银行" prop="bankName">
                  <el-input v-model="settleForm.bankName" placeholder="请输入收款银行名称" />
                </el-form-item>
                
                <el-form-item v-if="settleForm.settleType === 'BANK_TRANSFER'" label="收款账户" prop="accountNumber">
                  <el-input v-model="settleForm.accountNumber" placeholder="请输入银行账户号码" />
                </el-form-item>
                
                <el-form-item v-if="settleForm.settleType === 'BANK_TRANSFER'" label="账户户名" prop="accountName">
                  <el-input v-model="settleForm.accountName" placeholder="请输入账户持有人姓名" />
                </el-form-item>
                
                <el-form-item label="核销金额" prop="settleAmount">
                  <el-input-number
                    v-model="settleForm.settleAmount"
                    :min="0"
                    :max="settleInfo.totalAmount - settleInfo.advanceAmount"
                    :precision="2"
                    :controls="false"
                    placeholder="核销金额"
                  />
                  <span class="hint-text">
                    可核销金额：<span class="available-amount">¥{{ formatAmount(finalReimburseAmount) }}</span>
                  </span>
                </el-form-item>
                
                <el-form-item v-if="finalReimburseAmount > 0 && settleForm.settleAmount !== finalReimburseAmount" label="核销备注" prop="remark">
                  <el-input
                    v-model="settleForm.remark"
                    type="textarea"
                    :rows="3"
                    placeholder="请输入核销备注（如：部分核销、差旅补贴等）"
                    maxlength="200"
                    show-word-limit
                  />
                </el-form-item>
                
                <el-form-item label="核销说明" prop="description">
                  <el-input
                    v-model="settleForm.description"
                    type="textarea"
                    :rows="3"
                    placeholder="请输入核销说明"
                    maxlength="500"
                    show-word-limit
                  />
                </el-form-item>
              </el-form>
            </div>
            
            <div class="form-section">
              <h4>费用明细</h4>
              <el-table :data="expenseDetails" border style="width: 100%">
                <el-table-column type="index" label="序号" width="60" align="center" />
                <el-table-column prop="itemName" label="费用项目" min-width="150" />
                <el-table-column prop="specification" label="规格说明" min-width="120" />
                <el-table-column prop="quantity" label="数量" width="80" align="center" />
                <el-table-column prop="unitPrice" label="单价(元)" width="100" align="right">
                  <template #default="{ row }">
                    {{ formatAmount(row.unitPrice) }}
                  </template>
                </el-table-column>
                <el-table-column prop="amount" label="金额(元)" width="100" align="right">
                  <template #default="{ row }">
                    {{ formatAmount(row.amount) }}
                  </template>
                </el-table-column>
              </el-table>
            </div>
            
            <div class="form-section">
              <h4>发票信息</h4>
              <div class="invoice-summary">
                <div class="invoice-grid">
                  <div v-for="invoice in invoiceList" :key="invoice.id" class="invoice-card">
                    <div class="invoice-header">
                      <span class="invoice-type">{{ getInvoiceTypeText(invoice.type) }}</span>
                      <span class="invoice-amount">¥{{ formatAmount(invoice.amount) }}</span>
                    </div>
                    <div class="invoice-info">
                      <div class="info-row">
                        <span class="label">发票代码：</span>
                        <span class="value">{{ invoice.invoiceCode }}</span>
                      </div>
                      <div class="info-row">
                        <span class="label">开票日期：</span>
                        <span class="value">{{ invoice.issueDate }}</span>
                      </div>
                      <div class="info-row">
                        <span class="label">开票方：</span>
                        <span class="value">{{ invoice.issuer }}</span>
                      </div>
                    </div>
                  </div>
                </div>
                <div v-if="invoiceList.length === 0" class="no-invoice">
                  <el-empty description="该报销单没有发票信息" :image-size="80" />
                </div>
              </div>
            </div>
          </el-scrollbar>
        </div>
      </div>

      <!-- 核销总结 -->
      <div class="settle-summary-section">
        <div class="section-header">
          <h3>核销总结</h3>
        </div>
        
        <div class="summary-content">
          <div class="summary-item">
            <span class="label">报销单总金额：</span>
            <span class="value">¥{{ formatAmount(settleInfo.totalAmount) }}</span>
          </div>
          <div class="summary-item">
            <span class="label">预借款金额：</span>
            <span class="value advance">¥{{ formatAmount(settleInfo.advanceAmount) }}</span>
          </div>
          <div class="summary-item">
            <span class="label">应报销金额：</span>
            <span class="value final">¥{{ formatAmount(finalReimburseAmount) }}</span>
          </div>
          <div class="summary-item">
            <span class="label">本次核销金额：</span>
            <span class="value settle">¥{{ formatAmount(settleForm.settleAmount || 0) }}</span>
          </div>
          <div class="summary-item">
            <span class="label">剩余核销金额：</span>
            <span class="value remaining">¥{{ formatAmount(Math.max(finalReimburseAmount - (settleForm.settleAmount || 0), 0)) }}</span>
          </div>
        </div>
      </div>

      <!-- 底部操作栏 -->
      <div class="action-bar">
        <div class="action-left">
          <el-button @click="handleBack">
            <el-icon><arrow-left /></el-icon>
            取消
          </el-button>
        </div>
        <div class="action-right">
          <el-button type="warning" @click="handleSaveDraft">
            <el-icon><document /></el-icon>
            保存草稿
          </el-button>
          <el-button type="primary" @click="handleSubmit">
            <el-icon><check /></el-icon>
            确认核销
          </el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ArrowLeft, Document, Check } from '@element-plus/icons-vue'
import { showSuccess, showError, showConfirm } from '@/components/common/Message.vue'

// 路由和状态管理
const route = useRoute()
const router = useRouter()

// 表单引用
const settleFormRef = ref()

// 核销信息数据
const settleInfo = ref<any>({})

// 费用明细数据
const expenseDetails = ref([])

// 发票列表数据
const invoiceList = ref([])

// 核销表单数据
const settleForm = reactive({
  settleType: 'BANK_TRANSFER',
  settleAmount: 0,
  bankName: '',
  accountNumber: '',
  accountName: '',
  remark: '',
  description: ''
})

// 表单验证规则
const settleRules = {
  settleType: [
    { required: true, message: '请选择核销结算方式', trigger: 'change' }
  ],
  bankName: [
    { required: false, message: '请输入收款银行名称', trigger: 'blur' }
  ],
  accountNumber: [
    { required: false, message: '请输入银行账户号码', trigger: 'blur' }
  ],
  accountName: [
    { required: false, message: '请输入账户持有人姓名', trigger: 'blur' }
  ],
  settleAmount: [
    { required: true, message: '请输入核销金额', trigger: 'blur' },
    { 
      validator: (rule: any, value: number) => {
        if (value <= 0) {
          return new Error('核销金额必须大于0')
        }
        if (value > finalReimburseAmount.value) {
          return new Error('核销金额不能大于应报销金额')
        }
        return true
      },
      trigger: 'blur' 
    }
  ],
  remark: [
    { required: false, message: '请输入核销备注', trigger: 'blur' }
  ],
  description: [
    { required: true, message: '请输入核销说明', trigger: 'blur' },
    { min: 10, message: '核销说明至少10个字符', trigger: 'blur' }
  ]
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
const finalReimburseAmount = computed(() => {
  if (settleInfo.value.totalAmount && settleInfo.value.advanceAmount) {
    return settleInfo.value.totalAmount - settleInfo.value.advanceAmount
  }
  return 0
})

// 生命周期
onMounted(() => {
  loadReimburseData()
  
  // 自动设置核销金额为应报销金额
  setTimeout(() => {
    if (finalReimburseAmount.value > 0) {
      settleForm.settleAmount = finalReimburseAmount.value
    }
  }, 100)
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
    // TODO: 调用API获取报销单详情
    await new Promise(resolve => setTimeout(resolve, 800))
    
    // 模拟数据
    settleInfo.value = {
      id: reimburseId,
      reimburseCode: 'REIM2024010001',
      reimbursePerson: '张经理',
      departmentName: '技术部',
      totalAmount: 1850.00,
      advanceAmount: 2000.00,
      approvalTime: '2024-01-16 14:20:00',
      status: 'APPROVED'
    }
    
    expenseDetails.value = [
      {
        id: 1,
        itemName: '高铁票',
        specification: '一等座',
        quantity: 2,
        unitPrice: 553.00,
        amount: 1106.00
      },
      {
        id: 2,
        itemName: '住宿费',
        specification: '标准间',
        quantity: 2,
        unitPrice: 300.00,
        amount: 600.00
      },
      {
        id: 3,
        itemName: '餐饮费',
        specification: '日常用餐',
        quantity: 3,
        unitPrice: 48.00,
        amount: 144.00
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
    
  } catch (error) {
    console.error('加载报销单数据失败:', error)
    showError('加载报销单数据失败')
    router.push('/reimburse/list')
  }
}

const handleBack = () => {
  router.push('/reimburse/list')
}

const handleSaveDraft = async () => {
  try {
    // 验证表单
    await settleFormRef.value.validate()
    
    // TODO: 调用API保存核销草稿
    await new Promise(resolve => setTimeout(resolve, 500))
    showSuccess('核销信息已保存为草稿')
    
  } catch (error) {
    showError('表单验证失败，请检查输入')
  }
}

const handleSubmit = async () => {
  try {
    // 验证表单
    await settleFormRef.value.validate()
    
    // 确认核销
    const confirmMessage = settleForm.settleAmount === finalReimburseAmount.value 
      ? '确认进行全额核销吗？系统将自动冲销对应预借款。' 
      : `确认进行部分核销吗？核销金额为 ¥${formatAmount(settleForm.settleAmount)}，剩余 ¥${formatAmount(finalReimburseAmount.value - settleForm.settleAmount)} 仍需处理。`
    
    await showConfirm(confirmMessage)
    
    // TODO: 调用API进行核销
    await new Promise(resolve => setTimeout(resolve, 800))
    
    showSuccess('借款核销成功')
    
    // 返回列表页面
    setTimeout(() => {
      router.push('/reimburse/list')
    }, 1500)
    
  } catch (error) {
    // 用户取消操作
    console.error('核销提交失败:', error)
  }
}

const getInvoiceTypeText = (type: string) => {
  return invoiceTypeMap[type] || '未知类型'
}

const formatAmount = (amount: number) => {
  return amount?.toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 }) || '0.00'
}

const formatDateTime = (dateTime: string) => {
  if (!dateTime) return '-';
  return dateTime;
}

// 表单验证辅助方法
const validateBankTransfer = () => {
  if (settleForm.settleType === 'BANK_TRANSFER') {
    if (!settleForm.bankName) {
      showError('银行转账需要填写收款银行名称')
      return false
    }
    if (!settleForm.accountNumber) {
      showError('银行转账需要填写银行账户号码')
      return false
    }
    if (!settleForm.accountName) {
      showError('银行转账需要填写账户持有人姓名')
      return false
    }
  }
  return true
}
</script>

<style scoped lang="css">
.reimburse-settle-container {
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

.settle-content {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.settle-info-section,
.settle-summary-section {
  background: white;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.section-header {
  margin-bottom: 20px;
  padding-bottom: 16px;
  border-bottom: 1px solid #e4e7ed;
}

.section-header h3 {
  margin: 0;
  color: #303133;
  font-size: 16px;
}

.settle-form {
  max-height: 600px;
}

.form-section {
  margin-bottom: 30px;
}

.form-section h4 {
  margin: 0 0 16px 0;
  color: #606266;
  font-size: 14px;
  font-weight: 600;
}

.hint-text {
  margin-left: 12px;
  font-size: 12px;
  color: #909399;
}

.available-amount {
  color: #409eff;
  font-weight: 600;
}

.invoice-summary {
  margin-top: 16px;
}

.invoice-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 16px;
}

.invoice-card {
  border: 1px solid #e4e7ed;
  border-radius: 6px;
  padding: 16px;
  background: #f8f9fa;
}

.invoice-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
  padding-bottom: 8px;
  border-bottom: 1px solid #e4e7ed;
}

.invoice-type {
  font-weight: 600;
  color: #303133;
  font-size: 14px;
}

.invoice-amount {
  font-size: 16px;
  font-weight: 600;
  color: #f56c6c;
}

.invoice-info {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.info-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 12px;
}

.info-row .label {
  color: #606266;
}

.info-row .value {
  color: #303133;
  font-weight: 500;
}

.no-invoice {
  padding: 40px 0;
}

.settle-summary-section {
  border-left: 4px solid #409eff;
}

.summary-content {
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding: 16px;
  background: #f8f9fa;
  border-radius: 4px;
}

.summary-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 0;
  border-bottom: 1px solid #e4e7ed;
}

.summary-item:last-child {
  border-bottom: none;
}

.summary-item .label {
  font-weight: 500;
  color: #606266;
}

.summary-item .value {
  font-weight: 600;
  color: #303133;
}

.summary-item .advance {
  color: #e6a23c;
}

.summary-item .final {
  color: #67c23a;
}

.summary-item .settle {
  color: #409eff;
  font-size: 16px;
}

.summary-item .remaining {
  color: #f56c6c;
  font-size: 16px;
}

.action-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: white;
  border-radius: 8px;
  padding: 16px 20px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.action-left,
.action-right {
  display: flex;
  gap: 12px;
}

.amount {
  font-weight: 600;
  color: #f56c6c;
}

.advance-amount {
  font-weight: 600;
  color: #e6a23c;
}

.final-amount {
  font-weight: 600;
  color: #67c23a;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .reimburse-settle-container {
    padding: 10px;
  }
  
  .page-header {
    flex-direction: column;
    gap: 16px;
    align-items: stretch;
    padding: 15px;
  }
  
  .settle-info-section,
  .settle-summary-section {
    padding: 15px;
  }
  
  .invoice-grid {
    grid-template-columns: 1fr;
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
  
  .summary-item {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }
}
</style>