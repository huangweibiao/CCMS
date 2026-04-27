<template>
  <div class="data-export">
    <!-- 导出设置弹窗 -->
    <el-dialog 
      v-model="showExportDialog" 
      :title="exportTitle" 
      width="500px"
      :close-on-click-modal="false"
    >
      <el-form :model="exportForm" label-width="80px" size="small">
        <el-form-item label="导出格式">
          <el-radio-group v-model="exportForm.format">
            <el-radio label="excel">Excel (.xlsx)</el-radio>
            <el-radio label="csv">CSV (.csv)</el-radio>
            <el-radio label="pdf">PDF (.pdf)</el-radio>
            <el-radio label="json">JSON (.json)</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item v-if="exportForm.format === 'excel'" label="Excel选项">
          <el-checkbox-group v-model="exportForm.excelOptions">
            <el-checkbox label="autoWidth">自动列宽</el-checkbox>
            <el-checkbox label="headerBold">表头加粗</el-checkbox>
            <el-checkbox label="freezeHeader">冻结表头</el-checkbox>
            <el-checkbox label="addFilter">添加筛选器</el-checkbox>
          </el-checkbox-group>
        </el-form-item>

        <el-form-item label="导出范围">
          <el-radio-group v-model="exportForm.range">
            <el-radio label="current">当前页数据</el-radio>
            <el-radio label="all">全部数据</el-radio>
            <el-radio label="selected">选中数据</el-radio>
            <el-radio label="custom">自定义</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item v-if="exportForm.range === 'custom'" label="数据范围">
          <el-input-number 
            v-model="exportForm.customRange.start" 
            placeholder="起始行" 
            :min="1" 
            :max="totalRows"
          />
          <span class="range-separator">至</span>
          <el-input-number 
            v-model="exportForm.customRange.end" 
            placeholder="结束行" 
            :min="exportForm.customRange.start" 
            :max="totalRows"
          />
          <span class="range-info">共 {{ totalRows }} 行</span>
        </el-form-item>

        <el-form-item label="文件名称">
          <el-input v-model="exportForm.filename" placeholder="请输入文件名">
            <template #append>
              .{{ getFileExtension(exportForm.format) }}
            </template>
          </el-input>
        </el-form-item>

        <el-form-item label="包含列">
          <el-checkbox-group v-model="exportForm.columns" size="small">
            <el-checkbox 
              v-for="column in availableColumns" 
              :key="column.prop" 
              :label="column.prop"
            >
              {{ column.label }}
            </el-checkbox>
          </el-checkbox-group>
          <div class="column-actions">
            <el-button type="text" size="mini" @click="selectAllColumns">全选</el-button>
            <el-button type="text" size="mini" @click="clearColumns">清空</el-button>
            <el-button type="text" size="mini" @click="resetColumns">重置</el-button>
          </div>
        </el-form-item>

        <el-form-item label="数据筛选">
          <el-switch v-model="exportForm.includeFilters" />
          <span class="filter-desc">包含当前筛选条件</span>
        </el-form-item>

        <el-form-item v-if="exportForm.format === 'excel'" label="工作表">
          <el-input 
            v-model="exportForm.sheetName" 
            placeholder="工作表名称" 
            maxlength="31"
            show-word-limit
          />
        </el-form-item>

        <el-form-item v-if="exportForm.format === 'excel' || exportForm.format === 'pdf'" label="日期格式">
          <el-select v-model="exportForm.dateFormat">
            <el-option label="YYYY-MM-DD" value="YYYY-MM-DD" />
            <el-option label="YYYY/MM/DD" value="YYYY/MM/DD" />
            <el-option label="DD/MM/YYYY" value="DD/MM/YYYY" />
            <el-option label="MM-DD-YYYY" value="MM-DD-YYYY" />
          </el-select>
        </el-form-item>

        <el-form-item label="导出模板">
          <el-select v-model="exportForm.template" placeholder="选择导出模板">
            <el-option label="标准模板" value="standard" />
            <el-option label="财务专用" value="finance" />
            <el-option label="统计报表" value="report" />
            <el-option label="自定义模板" value="custom" />
          </el-select>
        </el-form-item>
      </el-form>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="showExportDialog = false">取消</el-button>
          <el-button type="primary" :loading="exporting" @click="handleExport">
            {{ exporting ? '导出中...' : '开始导出' }}
          </el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 导出进度提示 -->
    <el-dialog 
      v-model="showProgressDialog" 
      title="导出进度" 
      width="400px"
      :show-close="false"
      :close-on-click-modal="false"
      :close-on-press-escape="false"
    >
      <div class="progress-content">
        <el-progress 
          :percentage="progressPercentage" 
          :status="progressStatus"
          :stroke-width="8"
        />
        <div class="progress-info">
          <span>{{ progressMessage }}</span>
          <span v-if="progressPercentage < 100">{{ progressPercentage }}%</span>
        </div>
      </div>
      <template #footer>
        <el-button 
          v-if="progressStatus === 'success'" 
          type="primary" 
          @click="handleDownloadSuccess"
        >
          打开文件位置
        </el-button>
        <el-button 
          v-if="progressStatus !== 'success' && progressStatus !== 'exception'" 
          @click="cancelExport"
        >
          取消导出
        </el-button>
        <el-button 
          v-if="progressStatus === 'success' || progressStatus === 'exception'" 
          @click="showProgressDialog = false"
        >
          关闭
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'

// 组件属性接口
interface Props {
  exportData: any[]
  columns: ColumnItem[]
  filename?: string
  totalRows?: number
  selectedRows?: any[]
  currentPage?: number
  pageSize?: number
  showExport?: boolean
}

interface ColumnItem {
  prop: string
  label: string
  width?: number
  type?: string
  formatter?: Function
}

interface ExportForm {
  format: string
  range: string
  filename: string
  columns: string[]
  includeFilters: boolean
  excelOptions: string[]
  sheetName: string
  dateFormat: string
  template: string
  customRange: {
    start: number
    end: number
  }
}

// 默认属性值
const props = withDefaults(defineProps<Props>(), {
  filename: 'export_data',
  totalRows: 0,
  selectedRows: () => [],
  currentPage: 1,
  pageSize: 20,
  showExport: false
})

// 响应式数据
const showExportDialog = ref(false)
const showProgressDialog = ref(false)
const exporting = ref(false)
const progressPercentage = ref(0)
const progressStatus = ref<'success' | 'exception' | ''>('')
const progressMessage = ref('')

// 导出表单数据
const exportForm = reactive<ExportForm>({
  format: 'excel',
  range: 'current',
  filename: props.filename,
  columns: [],
  includeFilters: true,
  excelOptions: ['autoWidth', 'headerBold'],
  sheetName: 'Sheet1',
  dateFormat: 'YYYY-MM-DD',
  template: 'standard',
  customRange: {
    start: 1,
    end: props.totalRows
  }
})

// 计算属性
const exportTitle = computed(() => {
  return `导出数据 - ${getFormatLabel(exportForm.format)}`
})

const availableColumns = computed(() => {
  return props.columns.filter(column => 
    column.prop && column.label && column.prop !== 'operation'
  )
})

// 监听显示状态
watch(() => props.showExport, (newVal) => {
  if (newVal) {
    showExportDialog.value = true
    initializeExportForm()
  }
})

// 方法定义
const getFormatLabel = (format: string) => {
  const formatMap: Record<string, string> = {
    'excel': 'Excel',
    'csv': 'CSV',
    'pdf': 'PDF',
    'json': 'JSON'
  }
  return formatMap[format] || format
}

const getFileExtension = (format: string) => {
  const extensionMap: Record<string, string> = {
    'excel': 'xlsx',
    'csv': 'csv',
    'pdf': 'pdf',
    'json': 'json'
  }
  return extensionMap[format] || format
}

const initializeExportForm = () => {
  // 初始化列选择
  exportForm.columns = availableColumns.value.map(col => col.prop)
  
  // 初始化文件名
  if (!exportForm.filename || exportForm.filename === 'export_data') {
    const timestamp = new Date().toISOString().slice(0, 10)
    exportForm.filename = `${props.filename}_${timestamp}`
  }
  
  // 初始化自定义范围
  exportForm.customRange.end = props.totalRows
}

const selectAllColumns = () => {
  exportForm.columns = availableColumns.value.map(col => col.prop)
}

const clearColumns = () => {
  exportForm.columns = []
}

const resetColumns = () => {
  exportForm.columns = availableColumns.value.map(col => col.prop)
}

const getExportData = () => {
  let data = [...props.exportData]
  
  // 根据范围筛选数据
  switch (exportForm.range) {
    case 'current':
      // 当前页数据
      const startIndex = (props.currentPage - 1) * props.pageSize
      const endIndex = Math.min(startIndex + props.pageSize, data.length)
      data = data.slice(startIndex, endIndex)
      break
      
    case 'selected':
      data = props.selectedRows.length > 0 ? props.selectedRows : data
      break
      
    case 'custom':
      const start = Math.max(0, exportForm.customRange.start - 1)
      const end = Math.min(exportForm.customRange.end, data.length)
      data = data.slice(start, end)
      break
      
    case 'all':
    default:
      // 全部数据
      break
  }
  
  return data
}

const handleExport = async () => {
  if (exportForm.columns.length === 0) {
    ElMessage.warning('请至少选择一列进行导出')
    return
  }
  
  if (exportForm.range === 'custom' && 
      (exportForm.customRange.start > exportForm.customRange.end || exportForm.customRange.start < 1)) {
    ElMessage.warning('请输入有效的自定义范围')
    return
  }
  
  exporting.value = true
  showProgressDialog.value = true
  progressPercentage.value = 0
  progressStatus.value = ''
  progressMessage.value = '准备数据中...'
  
  try {
    // 模拟数据准备过程
    await simulateProgress('数据处理', 30)
    
    const exportData = getExportData()
    
    if (exportData.length === 0) {
      throw new Error('没有可导出的数据')
    }
    
    await simulateProgress('生成文件', 70)
    
    // 根据格式生成文件
    const blob = await generateFileBlob(exportData)
    
    await simulateProgress('下载文件', 90)
    
    // 下载文件
    downloadFile(blob)
    
    progressPercentage.value = 100
    progressStatus.value = 'success'
    progressMessage.value = `导出成功！共导出 ${exportData.length} 条数据`
    
  } catch (error) {
    console.error('导出失败:', error)
    progressStatus.value = 'exception'
    progressMessage.value = error instanceof Error ? error.message : '导出失败'
  } finally {
    exporting.value = false
    showExportDialog.value = false
  }
}

const simulateProgress = (message: string, targetPercentage: number) => {
  return new Promise<void>((resolve) => {
    progressMessage.value = message
    
    const interval = setInterval(() => {
      if (progressPercentage.value < targetPercentage) {
        progressPercentage.value += 5
      } else {
        clearInterval(interval)
        resolve()
      }
    }, 100)
  })
}

const generateFileBlob = async (data: any[]) => {
  // 实际项目中需要实现具体的文件生成逻辑
  // 这里使用模拟实现
  
  const selectedColumns = availableColumns.value.filter(col => 
    exportForm.columns.includes(col.prop)
  )
  
  let content = ''
  
  switch (exportForm.format) {
    case 'csv':
      // CSV格式
      const headers = selectedColumns.map(col => col.label).join(',')
      const rows = data.map(item => 
        selectedColumns.map(col => {
          let value = item[col.prop]
          if (col.formatter) {
            value = col.formatter(item[col.prop], item)
          }
          // 处理特殊字符
          return `"${String(value || '').replace(/"/g, '""')}"`
        }).join(',')
      ).join('\n')
      content = headers + '\n' + rows
      break
      
    case 'json':
      // JSON格式
      const jsonData = data.map(item => {
        const obj: any = {}
        selectedColumns.forEach(col => {
          obj[col.label] = item[col.prop]
        })
        return obj
      })
      content = JSON.stringify(jsonData, null, 2)
      break
      
    case 'excel':
    case 'pdf':
    default:
      // Excel和PDF需要第三方库支持
      content = '模拟文件内容'
      break
  }
  
  return new Blob([content], { 
    type: getMimeType(exportForm.format) 
  })
}

const getMimeType = (format: string) => {
  const mimeMap: Record<string, string> = {
    'excel': 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
    'csv': 'text/csv',
    'pdf': 'application/pdf',
    'json': 'application/json'
  }
  return mimeMap[format] || 'application/octet-stream'
}

const downloadFile = (blob: Blob) => {
  const url = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = `${exportForm.filename}.${getFileExtension(exportForm.format)}`
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
  URL.revokeObjectURL(url)
}

const cancelExport = () => {
  exporting.value = false
  showProgressDialog.value = false
  progressPercentage.value = 0
  ElMessage.info('导出已取消')
}

const handleDownloadSuccess = () => {
  // 实际项目中可能需要打开文件所在位置
  ElMessage.success('文件下载成功')
  showProgressDialog.value = false
}

// 暴露方法给父组件
defineExpose({
  openExportDialog: () => {
    showExportDialog.value = true
    initializeExportForm()
  },
  closeExportDialog: () => {
    showExportDialog.value = false
  }
})
</script>

<style scoped>
.data-export {
  display: inline-block;
}

.range-separator {
  margin: 0 10px;
  color: #909399;
}

.range-info {
  margin-left: 10px;
  font-size: 12px;
  color: #909399;
}

.column-actions {
  margin-top: 5px;
}

.filter-desc {
  margin-left: 8px;
  font-size: 12px;
  color: #909399;
}

.progress-content {
  text-align: center;
}

.progress-info {
  display: flex;
  justify-content: space-between;
  margin-top: 10px;
  font-size: 14px;
  color: #606266;
}

.dialog-footer {
  text-align: right;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .el-form-item {
    margin-bottom: 16px;
  }
  
  .el-radio-group {
    display: flex;
    flex-direction: column;
    gap: 8px;
  }
  
  .el-checkbox-group {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 8px;
  }
}
</style>