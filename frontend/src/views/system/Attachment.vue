<template>
  <div class="attachment-container">
    <div class="page-header">
      <div class="page-title">
        <h2>附件管理</h2>
        <p>管理系统附件文件，支持上传、下载、删除操作</p>
      </div>
      <div class="page-actions">
        <el-upload
          action="#"
          :http-request="handleUpload"
          :show-file-list="false"
          accept="*"
        >
          <el-button type="primary">
            <el-icon><upload /></el-icon>
            上传附件
          </el-button>
        </el-upload>
      </div>
    </div>

    <div class="search-form">
      <el-form :model="searchForm" inline>
        <el-form-item label="业务类型">
          <el-select v-model="searchForm.businessType" placeholder="请选择" clearable>
            <el-option label="系统文件" :value="0" />
            <el-option label="用户头像" :value="1" />
            <el-option label="费用凭证" :value="2" />
            <el-option label="预算文档" :value="3" />
            <el-option label="审批附件" :value="4" />
          </el-select>
        </el-form-item>
        <el-form-item label="业务ID">
          <el-input v-model="searchForm.businessId" placeholder="请输入业务ID" clearable />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">
            <el-icon><search /></el-icon>
            查询
          </el-button>
          <el-button @click="handleReset">
            <el-icon><refresh /></el-icon>
            重置
          </el-button>
        </el-form-item>
      </el-form>
    </div>

    <el-table :data="attachmentList" v-loading="loading" stripe>
      <el-table-column type="index" label="序号" width="60" />
      <el-table-column prop="fileName" label="文件名" min-width="200">
        <template #default="{ row }">
          <el-link type="primary" @click="handleDownload(row)">
            {{ row.fileName }}
          </el-link>
        </template>
      </el-table-column>
      <el-table-column prop="fileSize" label="文件大小" width="120">
        <template #default="{ row }">
          {{ formatFileSize(row.fileSize) }}
        </template>
      </el-table-column>
      <el-table-column prop="fileType" label="文件类型" width="100" />
      <el-table-column prop="businessType" label="业务类型" width="120">
        <template #default="{ row }">
          <el-tag>{{ getBusinessTypeText(row.businessType) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="businessId" label="业务ID" width="100" />
      <el-table-column prop="downloadCount" label="下载次数" width="100" />
      <el-table-column prop="createTime" label="上传时间" width="180" />
      <el-table-column label="操作" width="150" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" link size="small" @click="handleDownload(row)">
            下载
          </el-button>
          <el-button type="danger" link size="small" @click="handleDelete(row)">
            删除
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination
      v-model:current-page="pagination.page"
      v-model:page-size="pagination.size"
      :total="pagination.total"
      layout="total, sizes, prev, pager, next, jumper"
      @size-change="handleSizeChange"
      @current-change="handlePageChange"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Upload, Search, Refresh } from '@element-plus/icons-vue'
import { attachmentApi } from '@/api/attachment'
import type { Attachment } from '@/types/system'

const loading = ref(false)
const attachmentList = ref<Attachment[]>([])

const searchForm = reactive({
  businessType: undefined as number | undefined,
  businessId: undefined as number | undefined
})

const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

onMounted(() => {
  loadAttachmentList()
})

const loadAttachmentList = async () => {
  loading.value = true
  try {
    const res = await attachmentApi.getAttachmentList({
      businessType: searchForm.businessType,
      businessId: searchForm.businessId
    })
    attachmentList.value = res.data
    pagination.total = res.data.length
  } catch (error) {
    ElMessage.error('获取附件列表失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.page = 1
  loadAttachmentList()
}

const handleReset = () => {
  searchForm.businessType = undefined
  searchForm.businessId = undefined
  handleSearch()
}

const handleUpload = async (options: any) => {
  try {
    await attachmentApi.uploadAttachment(options.file, {
      businessType: searchForm.businessType,
      businessId: searchForm.businessId
    })
    ElMessage.success('上传成功')
    loadAttachmentList()
  } catch (error) {
    ElMessage.error('上传失败')
  }
}

const handleDownload = (row: Attachment) => {
  if (row.fileUrl) {
    window.open(row.fileUrl, '_blank')
  } else {
    ElMessage.warning('文件链接不存在')
  }
}

const handleDelete = async (row: Attachment) => {
  try {
    await ElMessageBox.confirm('确定要删除该附件吗？', '提示', { type: 'warning' })
    await attachmentApi.deleteAttachment(row.id!)
    ElMessage.success('删除成功')
    loadAttachmentList()
  } catch (error) {
    // 取消删除
  }
}

const handleSizeChange = (size: number) => {
  pagination.size = size
  loadAttachmentList()
}

const handlePageChange = (page: number) => {
  pagination.page = page
  loadAttachmentList()
}

const formatFileSize = (size?: number) => {
  if (!size) return '-'
  const units = ['B', 'KB', 'MB', 'GB']
  let index = 0
  let fileSize = size
  while (fileSize >= 1024 && index < units.length - 1) {
    fileSize /= 1024
    index++
  }
  return `${fileSize.toFixed(2)} ${units[index]}`
}

const getBusinessTypeText = (type?: number) => {
  const map: Record<number, string> = {
    0: '系统文件',
    1: '用户头像',
    2: '费用凭证',
    3: '预算文档',
    4: '审批附件'
  }
  return map[type ?? 0] || '未知'
}
</script>

<style scoped lang="scss">
.attachment-container {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.search-form {
  margin-bottom: 20px;
  padding: 20px;
  background: #f5f7fa;
  border-radius: 4px;
}
</style>
