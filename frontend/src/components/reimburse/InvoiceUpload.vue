<template>
  <div class="invoice-upload-container">
    <!-- 发票上传区域 -->
    <div class="upload-area">
      <el-upload
        ref="uploadRef"
        :action="uploadUrl"
        :headers="uploadHeaders"
        :data="uploadData"
        :file-list="fileList"
        :limit="limit"
        :on-exceed="handleExceed"
        :before-upload="beforeUpload"
        :on-success="handleUploadSuccess"
        :on-error="handleUploadError"
        :on-remove="handleRemove"
        :on-preview="handlePreview"
        :accept="acceptTypes"
        :multiple="true"
        list-type="picture-card"
        class="invoice-upload"
      >
        <el-icon><plus /></el-icon>
        <div class="upload-text">
          <div>点击上传发票</div>
          <div class="upload-tips">支持图片、PDF格式，单张不超过5MB</div>
        </div>
      </el-upload>
    </div>

    <!-- 发票信息表格 -->
    <div class="invoice-table-section">
      <div class="section-header">
        <h4>发票信息</h4>
        <el-button type="primary" size="small" @click="handleBatchRecognition">
          <el-icon><magic-stick /></el-icon>
          批量识别
        </el-button>
      </div>
      
      <el-table :data="invoiceList" border style="width: 100%" empty-text="暂无发票信息">
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column prop="fileName" label="发票名称" min-width="150" />
        <el-table-column prop="invoiceCode" label="发票代码" width="140" align="center">
          <template #default="{ row }">
            <div v-if="row.invoiceCode">{{ row.invoiceCode }}</div>
            <el-button v-else type="primary" size="small" link @click="handleRecognition(row)">
              识别发票
            </el-button>
          </template>
        </el-table-column>
        <el-table-column prop="type" label="发票类型" width="120" align="center">
          <template #default="{ row }">
            <el-select v-model="row.type" placeholder="选择类型" size="small" @change="handleTypeChange(row)">
              <el-option label="普通发票" value="COMMON" />
              <el-option label="专用发票" value="SPECIAL" />
              <el-option label="电子发票" value="ELECTRONIC" />
              <el-option label="火车票" value="RAILWAY" />
              <el-option label="机票" value="AIRLINE" />
            </el-select>
          </template>
        </el-table-column>
        <el-table-column prop="amount" label="金额(元)" width="120" align="right">
          <template #default="{ row }">
            <el-input-number
              v-model="row.amount"
              :min="0"
              :precision="2"
              :controls="false"
              size="small"
              placeholder="金额"
              :class="{ 'error-input': row.amount === null }"
            />
          </template>
        </el-table-column>
        <el-table-column prop="issueDate" label="开票日期" width="120" align="center">
          <template #default="{ row }">
            <el-date-picker
              v-model="row.issueDate"
              type="date"
              placeholder="开票日期"
              size="small"
              format="YYYY-MM-DD"
              value-format="YYYY-MM-DD"
            />
          </template>
        </el-table-column>
        <el-table-column prop="issuer" label="开票方" min-width="120">
          <template #default="{ row }">
            <el-input v-model="row.issuer" placeholder="开票方名称" size="small" />
          </template>
        </el-table-column>
        <el-table-column prop="taxNumber" label="税号" min-width="140">
          <template #default="{ row }">
            <el-input v-model="row.taxNumber" placeholder="纳税人识别号" size="small" />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right" align="center">
          <template #default="{ row, $index }">
            <el-button type="primary" size="small" link @click="handlePreview(row)">预览</el-button>
            <el-button type="warning" size="small" link @click="handleExtract(row)">提取</el-button>
            <el-button type="danger" size="small" link @click="handleDelete($index)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 发票信息汇总 -->
    <div class="invoice-summary">
      <div class="summary-item">
        <span>发票数量：</span>
        <span class="count">{{ invoiceList.length }} 张</span>
      </div>
      <div class="summary-item">
        <span>发票总金额：</span>
        <span class="total-amount">¥{{ formatAmount(totalAmount) }}</span>
      </div>
      <div class="summary-item">
        <span>已识别数量：</span>
        <span class="recognized-count">{{ recognizedCount }} 张</span>
      </div>
    </div>

    <!-- 发票预览模态框 -->
    <el-dialog v-model="previewVisible" :title="previewTitle" width="70%" top="5vh" append-to-body>
      <div class="preview-content">
        <img v-if="previewFile.type === 'image'" :src="previewFile.url" style="max-width: 100%; max-height: 70vh;" />
        <embed v-else-if="previewFile.type === 'pdf'" :src="previewFile.url" width="100%" height="600" type="application/pdf" />
        <div v-else class="unsupported-preview">
          <el-empty description="不支持预览此格式文件" :image-size="100" />
        </div>
      </div>
    </el-dialog>

    <!-- OCR识别进度模态框 -->
    <el-dialog v-model="ocrProgressVisible" title="发票识别中" width="400px" :close-on-click-modal="false" :show-close="false">
      <div class="ocr-progress">
        <div class="progress-info">
          <el-progress :percentage="ocrProgress" :status="ocrStatus" :show-text="false" />
          <div class="progress-text">{{ progressText }}</div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, watch } from 'vue'
import { Plus, MagicStick } from '@element-plus/icons-vue'
import type { UploadInstance, UploadRawFile, UploadFile, UploadUserFile } from 'element-plus'
import { showSuccess, showError, showConfirm } from '@/components/common/Message.vue'

// 上传组件实例
const uploadRef = ref<UploadInstance>()

// 可接受的文件类型
const acceptTypes = '.jpg,.jpeg,.png,.pdf,.bmp,.gif'

// 上传配置
const uploadUrl = ref('/api/invoice/upload') // 实际项目中替换为真实API地址
const uploadHeaders = ref({
  Authorization: 'Bearer ' + localStorage.getItem('token')
})

const uploadData = ref({
  reimburseId: '', // 报销单ID
  userId: localStorage.getItem('userId')
})

// 上传限制
const limit = ref(10) // 最多10张发票

// 文件列表
const fileList = ref<UploadUserFile[]>([])

// 发票数据列表
const invoiceList = ref<any[]>([])

// 预览相关状态
const previewVisible = ref(false)
const previewFile = ref<any>({})

// OCR识别进度
const ocrProgressVisible = ref(false)
const ocrProgress = ref(0)
const ocrStatus = ref<'success' | 'exception' | 'warning' | undefined>()

// 属性定义
interface Props {
  reimburseId?: string
  maxAmount?: number
}

const props = withDefaults(defineProps<Props>(), {
  reimburseId: '',
  maxAmount: 0
})

// 事件定义
const emit = defineEmits<{
  (e: 'update:invoiceData', data: any[]): void
  (e: 'amountChange', totalAmount: number): void
  (e: 'invoiceChange', count: number): void
}>()

// 计算属性
const totalAmount = computed(() => {
  return invoiceList.value.reduce((sum, invoice) => {
    return sum + (invoice.amount || 0)
  }, 0)
})

const recognizedCount = computed(() => {
  return invoiceList.value.filter(invoice => invoice.invoiceCode).length
})

const previewTitle = computed(() => {
  return `发票预览 - ${previewFile.value.fileName || '未知文件'}`
})

const progressText = computed(() => {
  if (ocrStatus.value === 'success') return '识别完成'
  if (ocrStatus.value === 'exception') return '识别失败'
  return `正在识别... ${ocrProgress.value}%`
})

// 监听器
watch(
  () => invoiceList.value,
  () => {
    emit('update:invoiceData', invoiceList.value)
    emit('amountChange', totalAmount.value)
    emit('invoiceChange', invoiceList.value.length)
  },
  { deep: true }
)

// 操作方法
const handleExceed = (files: File[]) => {
  showError(`最多只能上传 ${limit.value} 个文件，当前已选择 ${files.length} 个文件`)
}

const beforeUpload = (file: UploadRawFile) => {
  // 文件大小检查
  const isLt5M = file.size / 1024 / 1024 < 5
  if (!isLt5M) {
    showError('发票文件大小不能超过5MB')
    return false
  }

  // 文件类型检查
  const isAcceptType = acceptTypes.split(',').some(type => {
    const ext = type.replace('.', '').toLowerCase()
    return file.type.includes(ext) || file.name.toLowerCase().endsWith(type.toLowerCase())
  })

  if (!isAcceptType) {
    showError('只支持上传图片和PDF格式文件')
    return false
  }

  return true
}

const handleUploadSuccess = (response: any, file: UploadFile) => {
  try {
    // 假设响应结构
    const fileInfo = {
      id: response.data.id,
      fileName: file.name,
      fileUrl: response.data.url,
      fileSize: file.size,
      uploadTime: new Date().toISOString(),
      status: 'UPLOADED'
    }

    // 创建对应的发票数据
    const invoiceData = {
      ...fileInfo,
      type: '',
      invoiceCode: '',
      amount: null,
      issueDate: '',
      issuer: '',
      taxNumber: '',
      isRecognized: false
    }

    invoiceList.value.push(invoiceData)
    showSuccess(`文件 "${file.name}" 上传成功`)

    // 自动触发OCR识别
    setTimeout(() => {
      handleAutoRecognition(invoiceData)
    }, 500)

  } catch (error) {
    console.error('文件上传处理失败:', error)
    showError('文件上传处理失败')
  }
}

const handleUploadError = (error: Error, file: UploadFile) => {
  console.error('文件上传失败:', error)
  showError(`文件 "${file.name}" 上传失败`)
}

const handleRemove = (file: UploadFile) => {
  const index = invoiceList.value.findIndex(invoice => invoice.fileName === file.name)
  if (index > -1) {
    invoiceList.value.splice(index, 1)
  }
}

const handlePreview = (file: UploadFile | any) => {
  if (file.url) {
    previewFile.value = {
      type: getFileType(file.fileName || file.name),
      url: file.url || file.fileUrl,
      fileName: file.fileName || file.name
    }
    previewVisible.value = true
  } else {
    showError('文件预览链接不存在')
  }
}

const handleRecognition = async (invoice: any) => {
  try {
    ocrProgressVisible.value = true
    ocrProgress.value = 0
    ocrStatus.value = undefined

    // 模拟OCR识别过程
    const interval = setInterval(() => {
      ocrProgress.value += 10
      if (ocrProgress.value >= 100) {
        clearInterval(interval)
        
        // 模拟识别结果
        setTimeout(() => {
          const mockResult = generateMockInvoiceData(invoice.fileName)
          Object.assign(invoice, mockResult)
          invoice.isRecognized = true
          
          ocrStatus.value = 'success'
          setTimeout(() => {
            ocrProgressVisible.value = false
            showSuccess('发票识别成功')
          }, 1000)
        }, 300)
      }
    }, 200)

  } catch (error) {
    ocrStatus.value = 'exception'
    console.error('发票识别失败:', error)
    showError('发票识别失败')
  }
}

const handleAutoRecognition = async (invoice: any) => {
  // 对小文件进行自动识别
  if (invoice.fileSize < 2 * 1024 * 1024) { // 小于2MB的文件自动识别
    await handleRecognition(invoice)
  }
}

const handleBatchRecognition = async () => {
  const unrecoginzedInvoices = invoiceList.value.filter(invoice => !invoice.isRecognized)
  
  if (unrecoginzedInvoices.length === 0) {
    showSuccess('所有发票已识别完成')
    return
  }

  try {
    await showConfirm(`确定要对 ${unrecoginzedInvoices.length} 张未识别发票进行批量识别吗？`)
    
    ocrProgressVisible.value = true
    ocrProgress.value = 0
    ocrStatus.value = undefined

    // 批量识别处理
    for (let i = 0; i < unrecoginzedInvoices.length; i++) {
      const invoice = unrecoginzedInvoices[i]
      await new Promise(resolve => {
        setTimeout(() => {
          const mockResult = generateMockInvoiceData(invoice.fileName)
          Object.assign(invoice, mockResult)
          invoice.isRecognized = true
          
          ocrProgress.value = Math.round(((i + 1) / unrecoginzedInvoices.length) * 100)
          resolve(null)
        }, 500)
      })
    }

    ocrStatus.value = 'success'
    setTimeout(() => {
      ocrProgressVisible.value = false
      showSuccess(`批量识别完成，共识别 ${unrecoginzedInvoices.length} 张发票`)
    }, 1000)

  } catch (error) {
    // 用户取消操作
    ocrProgressVisible.value = false
  }
}

const handleExtract = (invoice: any) => {
  // 发票信息提取逻辑
  if (invoice.isRecognized) {
    showSuccess('发票信息已提取')
  } else {
    showError('请先识别发票信息')
  }
}

const handleDelete = (index: number) => {
  invoiceList.value.splice(index, 1)
  // 同时从上传列表中删除对应文件
  const fileName = invoiceList.value[index]?.fileName
  if (fileName) {
    const uploadIndex = fileList.value.findIndex(file => file.name === fileName)
    if (uploadIndex > -1) {
      fileList.value.splice(uploadIndex, 1)
    }
  }
  showSuccess('发票信息已删除')
}

const handleTypeChange = (invoice: any) => {
  // 类型变化时的处理逻辑
  console.log('发票类型变更:', invoice)
}

// 工具方法
const getFileType = (fileName: string) => {
  const ext = fileName.toLowerCase().split('.').pop()
  if (['jpg', 'jpeg', 'png', 'bmp', 'gif'].includes(ext || '')) {
    return 'image'
  } else if (ext === 'pdf') {
    return 'pdf'
  }
  return 'unknown'
}

const generateMockInvoiceData = (fileName: string) => {
  const types = ['COMMON', 'SPECIAL', 'ELECTRONIC', 'RAILWAY', 'AIRLINE']
  const issuers = ['北京商贸有限公司', '上海餐饮连锁', '广州科技发展有限公司', '深圳信息科技', '成都餐饮管理']
  
  return {
    invoiceCode: Math.random().toString(36).substr(2, 12).toUpperCase(),
    type: types[Math.floor(Math.random() * types.length)],
    amount: Math.round(Math.random() * 1000 * 100) / 100, // 0-1000之间的随机金额
    issueDate: formatDate(new Date(Date.now() - Math.random() * 30 * 24 * 60 * 60 * 1000)),
    issuer: issuers[Math.floor(Math.random() * issuers.length)],
    taxNumber: '91310101MA1' + Math.floor(Math.random() * 10000000).toString().padStart(7, '0')
  }
}

const formatDate = (date: Date) => {
  return date.toISOString().split('T')[0]
}

const formatAmount = (amount: number) => {
  return amount?.toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 }) || '0.00'
}

// 导出方法
const validateInvoices = () => {
  const invalidInvoices = invoiceList.value.filter(invoice => !invoice.amount)
  
  if (invalidInvoices.length > 0) {
    showError(`有 ${invalidInvoices.length} 张发票金额未填写`)
    return false
  }
  
  return true
}

const getInvoiceData = () => {
  return invoiceList.value
}

const clearInvoices = () => {
  invoiceList.value = []
  fileList.value = []
}

// 暴露方法给父组件
defineExpose({
  validateInvoices,
  getInvoiceData,
  clearInvoices
})
</script>

<style scoped lang="css">
.invoice-upload-container {
  background: white;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.upload-area {
  margin-bottom: 20px;
}

.invoice-upload {
  width: 100%;
}

.upload-text {
  margin-top: 8px;
  text-align: center;
  color: #606266;
}

.upload-tips {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.section-header h4 {
  margin: 0;
  color: #303133;
}

.invoice-table-section {
  margin-bottom: 20px;
}

.error-input {
  :deep(.el-input__wrapper) {
    box-shadow: 0 0 0 1px var(--el-color-danger) inset;
  }
}

.invoice-summary {
  display: flex;
  justify-content: flex-end;
  gap: 20px;
  padding: 16px;
  background: #f8f9fa;
  border-radius: 4px;
  border: 1px solid #e4e7ed;
}

.summary-item {
  display: flex;
  align-items: center;
  font-size: 14px;
}

.summary-item .count,
.summary-item .recognized-count {
  font-weight: 600;
  color: #409eff;
}

.summary-item .total-amount {
  font-size: 16px;
  font-weight: 600;
  color: #f56c6c;
}

.preview-content {
  text-align: center;
}

.unsupported-preview {
  padding: 40px 0;
}

.ocr-progress {
  text-align: center;
}

.progress-info {
  margin-bottom: 16px;
}

.progress-text {
  margin-top: 8px;
  font-size: 14px;
  color: #606266;
}

/* 单元格样式优化 */
:deep(.el-table .cell) {
  padding: 4px 8px;
}

:deep(.el-input-number) {
  width: 100px;
}

:deep(.el-input) {
  width: 120px;
}

:deep(.el-select) {
  width: 100px;
}

:deep(.el-date-editor) {
  width: 120px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .invoice-upload-container {
    padding: 15px;
  }
  
  .invoice-summary {
    flex-direction: column;
    gap: 8px;
    align-items: flex-start;
  }
  
  :deep(.el-table) {
    font-size: 12px;
  }
  
  :deep(.el-input-number),
  :deep(.el-input),
  :deep(.el-select),
  :deep(.el-date-editor) {
    width: 100%;
    min-width: 80px;
  }
}
</style>