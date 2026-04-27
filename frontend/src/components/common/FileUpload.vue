<template>
  <div class="file-upload">
    <!-- 文件上传区域 -->
    <el-upload
      ref="uploadRef"
      :action="action"
      :headers="headers"
      :data="data"
      :name="name"
      :multiple="multiple"
      :accept="accept"
      :limit="limit"
      :file-list="fileList"
      :before-upload="beforeUpload"
      :before-remove="beforeRemove"
      :on-progress="onProgress"
      :on-success="onSuccess"
      :on-error="onError"
      :on-change="onChange"
      :on-remove="onRemove"
      :on-exceed="onExceed"
      :disabled="disabled"
      :show-file-list="showFileList"
      :drag="drag"
      :list-type="listType"
      :auto-upload="autoUpload"
      :class="{ 'upload-disabled': disabled }"
    >
      <template v-if="listType === 'picture-card'">
        <el-icon v-if="!disabled"><plus /></el-icon>
        <div v-else class="upload-disabled-text">
          <el-icon><picture /></el-icon>
          <div>已禁用</div>
        </div>
      </template>
      
      <template v-else-if="drag">
        <el-icon class="el-icon--upload"><upload-filled /></el-icon>
        <div class="el-upload__text">
          将文件拖到此处，或<em>点击上传</em>
        </div>
        <div v-if="tip" class="el-upload__tip">
          {{ tip }}
        </div>
      </template>
      
      <template v-else>
        <el-button :type="buttonType" :size="buttonSize" :disabled="disabled">
          <el-icon><upload /></el-icon>
          {{ buttonText }}
        </el-button>
        <div v-if="tip" class="el-upload__tip">
          {{ tip }}
        </div>
      </template>
    </el-upload>

    <!-- 文件预览 -->
    <div v-if="showPreview && fileList.length > 0" class="file-preview">
      <div class="preview-title">已上传文件：</div>
      <div class="file-list">
        <div 
          v-for="file in fileList" 
          :key="file.uid" 
          class="file-item"
          @click="handlePreview(file)"
        >
          <el-icon class="file-icon"><document /></el-icon>
          <div class="file-info">
            <div class="file-name">{{ file.name }}</div>
            <div class="file-size">{{ formatFileSize(file.size) }}</div>
          </div>
          <div class="file-actions">
            <el-button 
              v-if="showDownload" 
              type="text" 
              size="small" 
              @click.stop="handleDownload(file)"
            >
              下载
            </el-button>
            <el-button 
              v-if="!disabled" 
              type="text" 
              size="small" 
              @click.stop="handleRemove(file)"
            >
              删除
            </el-button>
          </div>
        </div>
      </div>
    </div>

    <!-- 图片预览对话框 -->
    <el-dialog v-model="previewVisible" title="图片预览" width="60%">
      <img :src="previewImageUrl" alt="预览图片" class="preview-image" />
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Plus,
  Picture,
  UploadFilled,
  Upload,
  Document
} from '@element-plus/icons-vue'

// Props
interface FileItem {
  uid: string
  name: string
  url?: string
  status?: 'uploading' | 'success' | 'error'
  size?: number
  percentage?: number
  raw?: File
  response?: any
}

interface Props {
  modelValue?: FileItem[]
  action?: string
  headers?: Record<string, string>
  data?: Record<string, any>
  name?: string
  multiple?: boolean
  accept?: string
  limit?: number
  disabled?: boolean
  showFileList?: boolean
  drag?: boolean
  listType?: 'text' | 'picture' | 'picture-card'
  autoUpload?: boolean
  buttonType?: 'primary' | 'success' | 'warning' | 'danger' | 'info'
  buttonSize?: 'large' | 'default' | 'small'
  buttonText?: string
  tip?: string
  showPreview?: boolean
  showDownload?: boolean
  maxSize?: number // 最大文件大小（MB）
  fileTypes?: string[] // 允许的文件类型
}

const props = withDefaults(defineProps<Props>(), {
  modelValue: () => [],
  action: '/api/upload',
  name: 'file',
  multiple: true,
  limit: 10,
  showFileList: true,
  drag: false,
  listType: 'text',
  autoUpload: true,
  buttonType: 'primary',
  buttonSize: 'default',
  buttonText: '点击上传',
  showPreview: true,
  showDownload: true,
  maxSize: 10,
  fileTypes: () => ['jpg', 'jpeg', 'png', 'gif', 'pdf', 'doc', 'docx', 'xls', 'xlsx']
})

// Emits
const emit = defineEmits<{
  'update:modelValue': [files: FileItem[]]
  'change': [file: FileItem, fileList: FileItem[]]
  'remove': [file: FileItem, fileList: FileItem[]]
  'success': [response: any, file: FileItem, fileList: FileItem[]]
  'error': [error: Error, file: FileItem, fileList: FileItem[]]
  'progress': [event: any, file: FileItem, fileList: FileItem[]]
  'exceed': [files: File[], fileList: FileItem[]]
}>()

// 响应式数据
const uploadRef = ref()
const fileList = ref<FileItem[]>(props.modelValue)
const previewVisible = ref(false)
const previewImageUrl = ref('')

// 监听modelValue变化
watch(() => props.modelValue, (newValue) => {
  fileList.value = newValue
}, { deep: true })

// 监听fileList变化
watch(fileList, (newValue) => {
  emit('update:modelValue', newValue)
}, { deep: true })

// 计算属性
const totalFileSize = computed(() => {
  return fileList.value.reduce((total, file) => total + (file.size || 0), 0)
})

// 方法
const beforeUpload = (file: File) => {
  // 检查文件类型
  const fileExtension = file.name.split('.').pop()?.toLowerCase() || ''
  if (props.fileTypes.length > 0 && !props.fileTypes.includes(fileExtension)) {
    ElMessage.error(`仅支持上传 ${props.fileTypes.join(', ')} 格式的文件`)
    return false
  }

  // 检查文件大小
  const isLtMaxSize = file.size / 1024 / 1024 < props.maxSize
  if (!isLtMaxSize) {
    ElMessage.error(`文件大小不能超过 ${props.maxSize}MB`)
    return false
  }

  return true
}

const beforeRemove = async (file: FileItem) => {
  try {
    await ElMessageBox.confirm('确定要删除这个文件吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    return true
  } catch (error) {
    return false
  }
}

const onProgress = (event: any, file: FileItem, fileList: FileItem[]) => {
  emit('progress', event, file, fileList)
}

const onSuccess = (response: any, file: FileItem, fileList: FileItem[]) => {
  ElMessage.success('文件上传成功')
  emit('success', response, file, fileList)
}

const onError = (error: Error, file: FileItem, fileList: FileItem[]) => {
  ElMessage.error('文件上传失败')
  emit('error', error, file, fileList)
}

const onChange = (file: FileItem, fileList: FileItem[]) => {
  emit('change', file, fileList)
}

const onRemove = (file: FileItem, fileList: FileItem[]) => {
  emit('remove', file, fileList)
}

const onExceed = (files: File[], fileList: FileItem[]) => {
  ElMessage.warning(`最多只能上传 ${props.limit} 个文件`)
  emit('exceed', files, fileList)
}

const handlePreview = (file: FileItem) => {
  if (file.url && isImageFile(file.name)) {
    previewImageUrl.value = file.url
    previewVisible.value = true
  } else {
    ElMessage.info('该文件不支持在线预览')
  }
}

const handleDownload = (file: FileItem) => {
  if (file.url) {
    const link = document.createElement('a')
    link.href = file.url
    link.download = file.name
    link.click()
  } else {
    ElMessage.warning('文件下载链接不存在')
  }
}

const handleRemove = (file: FileItem) => {
  const index = fileList.value.findIndex(f => f.uid === file.uid)
  if (index !== -1) {
    fileList.value.splice(index, 1)
  }
}

// 检查是否是图片文件
const isImageFile = (fileName: string): boolean => {
  const imageExtensions = ['jpg', 'jpeg', 'png', 'gif', 'bmp', 'webp']
  const extension = fileName.split('.').pop()?.toLowerCase() || ''
  return imageExtensions.includes(extension)
}

// 格式化文件大小
const formatFileSize = (size: number): string => {
  if (size === 0) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(size) / Math.log(k))
  return parseFloat((size / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
}

// 手动上传
const submit = () => {
  uploadRef.value?.submit()
}

// 清空文件列表
const clearFiles = () => {
  uploadRef.value?.clearFiles()
  fileList.value = []
}

// 暴露方法给父组件
defineExpose({
  submit,
  clearFiles,
  fileList
})
</script>

<style scoped lang="css">
.file-upload {
  width: 100%;
}

.file-preview {
  margin-top: 20px;
  border: 1px solid #ebeef5;
  border-radius: 4px;
  padding: 15px;
}

.preview-title {
  font-weight: 600;
  margin-bottom: 10px;
  color: #606266;
}

.file-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.file-item {
  display: flex;
  align-items: center;
  padding: 8px 12px;
  border: 1px solid #ebeef5;
  border-radius: 4px;
  cursor: pointer;
  transition: background-color 0.3s;
}

.file-item:hover {
  background-color: #f5f7fa;
}

.file-icon {
  font-size: 20px;
  color: #409eff;
  margin-right: 12px;
}

.file-info {
  flex: 1;
}

.file-name {
  font-weight: 500;
  color: #303133;
  margin-bottom: 2px;
}

.file-size {
  font-size: 12px;
  color: #909399;
}

.file-actions {
  display: flex;
  gap: 8px;
}

.preview-image {
  width: 100%;
  height: auto;
  display: block;
}

.upload-disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.upload-disabled-text {
  display: flex;
  flex-direction: column;
  align-items: center;
  color: #909399;
}

@media (max-width: 768px) {
  .file-item {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }
  
  .file-actions {
    align-self: flex-end;
  }
}
</style>