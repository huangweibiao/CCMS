<template>
  <div class="loan-apply-form">
    <el-card class="form-card">
      <template #header>
        <div class="card-header">
          <span class="title">借款申请</span>
          <el-button 
            type="primary" 
            :icon="DocumentAdd" 
            @click="handleSubmit">
            提交申请
          </el-button>
        </div>
      </template>

      <el-form
        ref="formRef"
        :model="formData"
        :rules="rules"
        label-width="120px"
        class="loan-form">
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="借款类型" prop="loanType">
              <el-select 
                v-model="formData.loanType" 
                placeholder="请选择借款类型"
                style="width: 100%">
                <el-option 
                  v-for="type in loanTypes" 
                  :key="type.value"
                  :label="type.label" 
                  :value="type.value" />
              </el-select>
            </el-form-item>
          </el-col>
          
          <el-col :span="12">
            <el-form-item label="借款金额" prop="amount">
              <el-input-number
                v-model="formData.amount"
                :min="100"
                :max="100000"
                :precision="2"
                placeholder="请输入借款金额"
                style="width: 100%">
                <template #prefix>¥</template>
              </el-input-number>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="预计还款日期" prop="expectedRepaymentDate">
              <el-date-picker
                v-model="formData.expectedRepaymentDate"
                type="date"
                placeholder="选择预计还款日期"
                style="width: 100%"
                :disabled-date="disabledRepaymentDate" />
            </el-form-item>
          </el-col>
          
          <el-col :span="12">
            <el-form-item label="用途说明" prop="purpose">
              <el-input
                v-model="formData.purpose"
                placeholder="请输入借款用途说明"
                maxlength="200"
                show-word-limit />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="详细说明" prop="description">
          <el-input
            v-model="formData.description"
            type="textarea"
            :rows="4"
            placeholder="请详细描述借款用途、具体支出等内容"
            maxlength="500"
            show-word-limit />
        </el-form-item>

        <el-form-item label="附件上传" prop="attachments">
          <el-upload
            ref="uploadRef"
            action="#"
            :auto-upload="false"
            :multiple="true"
            :file-list="formData.attachments"
            :before-upload="beforeUpload"
            :on-remove="handleRemove"
            :on-exceed="handleExceed">
            <el-button type="primary" :icon="Upload">选择文件</el-button>
            <template #tip>
              <div class="el-upload__tip">支持JPG、PNG、PDF格式文件，单个文件不超过10MB</div>
            </template>
          </el-upload>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type UploadInstance } from 'element-plus'
import { DocumentAdd, Upload } from '@element-plus/icons-vue'
import type { LoanApplyRequest } from '@/types/loan'
import { useLoanStore } from '@/stores/loan'

const formRef = ref<FormInstance>()
const uploadRef = ref<UploadInstance>()
const loanStore = useLoanStore()

// 借款类型选项
const loanTypes = [
  { label: '备用金借款', value: 'RESERVE' },
  { label: '差旅借款', value: 'TRAVEL' },
  { label: '采购借款', value: 'PURCHASE' },
  { label: '业务借款', value: 'BUSINESS' },
  { label: '其他借款', value: 'OTHER' }
]

// 表单数据
const formData = reactive<LoanApplyRequest>({
  loanType: 'RESERVE',
  amount: 0,
  purpose: '',
  description: '',
  expectedRepaymentDate: new Date(Date.now() + 30 * 24 * 60 * 60 * 1000),
  attachments: []
})

// 表单验证规则
const rules = {
  loanType: [
    { required: true, message: '请选择借款类型', trigger: 'change' }
  ],
  amount: [
    { required: true, message: '请输入借款金额', trigger: 'blur' },
    { 
      validator: (rule: any, value: number, callback: any) => {
        if (value < 100) {
          callback(new Error('借款金额不能少于100元'))
        } else if (value > 100000) {
          callback(new Error('单笔借款金额不能超过10万元'))
        } else {
          callback()
        }
      }, 
      trigger: 'blur' 
    }
  ],
  expectedRepaymentDate: [
    { required: true, message: '请选择预计还款日期', trigger: 'change' }
  ],
  purpose: [
    { required: true, message: '请输入用途说明', trigger: 'blur' },
    { min: 5, max: 200, message: '用途说明长度在5-200个字符', trigger: 'blur' }
  ]
}

// 禁用日期（不能选择今天之前的日期）
const disabledRepaymentDate = (date: Date) => {
  return date.getTime() < Date.now() - 24 * 60 * 60 * 1000
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

// 提交借款申请
const handleSubmit = async () => {
  if (!formRef.value) return

  try {
    await formRef.value.validate()
    
    ElMessageBox.confirm(
      `您确定要提交这笔${formData.amount}元的借款申请吗？`,
      '确认提交',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    ).then(async () => {
      const success = await loanStore.applyLoan(formData)
      if (success) {
        ElMessage.success('借款申请提交成功，等待审批')
        formRef.value?.resetFields()
        formData.attachments = []
      }
    })
  } catch (error) {
    ElMessage.error('请检查表单填写是否正确')
  }
}
</script>

<style scoped>
.loan-apply-form {
  max-width: 900px;
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

.loan-form {
  margin-top: 20px;
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
</style>