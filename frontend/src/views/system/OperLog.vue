<template>
  <div class="oper-log-container">
    <div class="page-header">
      <div class="page-title">
        <h2>操作日志</h2>
        <p>查看系统操作日志，支持按模块、类型、时间筛选</p>
      </div>
      <div class="page-actions">
        <el-button type="danger" @click="handleClearExpired">
          <el-icon><delete /></el-icon>
          清理过期日志
        </el-button>
      </div>
    </div>

    <div class="search-form">
      <el-form :model="searchForm" inline>
        <el-form-item label="操作模块">
          <el-input v-model="searchForm.operModule" placeholder="请输入操作模块" clearable />
        </el-form-item>
        <el-form-item label="操作类型">
          <el-select v-model="searchForm.operType" placeholder="请选择" clearable>
            <el-option label="新增" value="新增" />
            <el-option label="修改" value="修改" />
            <el-option label="删除" value="删除" />
            <el-option label="查询" value="查询" />
            <el-option label="审批" value="审批" />
          </el-select>
        </el-form-item>
        <el-form-item label="操作时间">
          <el-date-picker
            v-model="searchForm.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
          />
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

    <el-table :data="logList" v-loading="loading" stripe>
      <el-table-column type="index" label="序号" width="60" />
      <el-table-column prop="operModule" label="操作模块" width="120" />
      <el-table-column prop="operType" label="操作类型" width="100">
        <template #default="{ row }">
          <el-tag :type="getOperTypeType(row.operType)">{{ row.operType }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="operName" label="操作人" width="100" />
      <el-table-column prop="operContent" label="操作内容" min-width="200" show-overflow-tooltip />
      <el-table-column prop="operIp" label="IP地址" width="130" />
      <el-table-column prop="status" label="状态" width="80">
        <template #default="{ row }">
          <el-tag :type="row.status === 0 ? 'success' : 'danger'">
            {{ row.status === 0 ? '成功' : '失败' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="costTime" label="耗时(ms)" width="100">
        <template #default="{ row }">
          <span :class="{ 'text-danger': row.costTime > 1000 }">{{ row.costTime }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="operTime" label="操作时间" width="180" />
      <el-table-column label="操作" width="100" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" link size="small" @click="handleViewDetail(row)">
            详情
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

    <el-dialog v-model="detailVisible" title="日志详情" width="600px">
      <el-descriptions :column="1" border v-if="currentLog">
        <el-descriptions-item label="操作模块">{{ currentLog.operModule }}</el-descriptions-item>
        <el-descriptions-item label="操作类型">{{ currentLog.operType }}</el-descriptions-item>
        <el-descriptions-item label="操作人">{{ currentLog.operName }}</el-descriptions-item>
        <el-descriptions-item label="操作内容">{{ currentLog.operContent }}</el-descriptions-item>
        <el-descriptions-item label="请求URL">{{ currentLog.operUrl }}</el-descriptions-item>
        <el-descriptions-item label="请求方法">{{ currentLog.requestMethod }}</el-descriptions-item>
        <el-descriptions-item label="IP地址">{{ currentLog.operIp }}</el-descriptions-item>
        <el-descriptions-item label="操作地点">{{ currentLog.operLocation }}</el-descriptions-item>
        <el-descriptions-item label="请求参数">
          <pre>{{ currentLog.operParam }}</pre>
        </el-descriptions-item>
        <el-descriptions-item label="返回结果">
          <pre>{{ currentLog.jsonResult }}</pre>
        </el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="currentLog.status === 0 ? 'success' : 'danger'">
            {{ currentLog.status === 0 ? '成功' : '失败' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="错误信息" v-if="currentLog.errorMsg">
          <span class="text-danger">{{ currentLog.errorMsg }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="耗时">{{ currentLog.costTime }}ms</el-descriptions-item>
        <el-descriptions-item label="操作时间">{{ currentLog.operTime }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Delete, Search, Refresh } from '@element-plus/icons-vue'
import { logApi } from '@/api/log'
import type { OperLog } from '@/types/system'

const loading = ref(false)
const logList = ref<OperLog[]>([])
const detailVisible = ref(false)
const currentLog = ref<OperLog | null>(null)

const searchForm = reactive({
  operModule: '',
  operType: '',
  dateRange: [] as string[]
})

const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

onMounted(() => {
  loadLogList()
})

const loadLogList = async () => {
  loading.value = true
  try {
    const params: any = {
      page: pagination.page - 1,
      size: pagination.size,
      operModule: searchForm.operModule || undefined,
      operType: searchForm.operType || undefined
    }
    if (searchForm.dateRange?.length === 2) {
      params.startTime = searchForm.dateRange[0] + 'T00:00:00'
      params.endTime = searchForm.dateRange[1] + 'T23:59:59'
    }
    const res = await logApi.getOperLogList(params)
    logList.value = res.data.content
    pagination.total = res.data.totalElements
  } catch (error) {
    ElMessage.error('获取日志列表失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.page = 1
  loadLogList()
}

const handleReset = () => {
  searchForm.operModule = ''
  searchForm.operType = ''
  searchForm.dateRange = []
  handleSearch()
}

const handleViewDetail = (row: OperLog) => {
  currentLog.value = row
  detailVisible.value = true
}

const handleClearExpired = async () => {
  try {
    await ElMessageBox.confirm('确定要清理90天前的过期日志吗？', '提示', { type: 'warning' })
    await logApi.deleteExpiredLogs(90)
    ElMessage.success('清理成功')
    loadLogList()
  } catch (error) {
    // 取消
  }
}

const handleSizeChange = (size: number) => {
  pagination.size = size
  loadLogList()
}

const handlePageChange = (page: number) => {
  pagination.page = page
  loadLogList()
}

const getOperTypeType = (type?: string) => {
  const map: Record<string, string> = {
    '新增': 'success',
    '修改': 'warning',
    '删除': 'danger',
    '查询': 'info',
    '审批': 'primary'
  }
  return map[type ?? ''] || 'info'
}
</script>

<style scoped lang="scss">
.oper-log-container {
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

.text-danger {
  color: #f56c6c;
}

pre {
  margin: 0;
  padding: 10px;
  background: #f5f7fa;
  border-radius: 4px;
  max-height: 200px;
  overflow: auto;
}
</style>
