<template>
  <div class="reimburse-list-container">
    <!-- 页面标题和操作 -->
    <div class="page-header">
      <div class="header-left">
        <h2>费用报销单列表</h2>
        <p>查看和管理所有费用报销单</p>
      </div>
      <div class="header-actions">
        <el-button type="primary" @click="handleCreate">
          <el-icon><plus /></el-icon>
          新建报销单
        </el-button>
        <el-button @click="handleExport">
          <el-icon><download /></el-icon>
          导出数据
        </el-button>
      </div>
    </div>

    <!-- 查询条件 -->
    <div class="query-section">
      <el-form :model="queryParams" inline label-width="80px">
        <el-form-item label="报销单号">
          <el-input v-model="queryParams.reimburseCode" placeholder="请输入报销单号" clearable />
        </el-form-item>
        
        <el-form-item label="报销人">
          <el-input v-model="queryParams.reimbursePerson" placeholder="请输入报销人" clearable />
        </el-form-item>
        
        <el-form-item label="报销日期">
          <el-date-picker
            v-model="queryParams.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>
        
        <el-form-item label="报销类型">
          <el-select v-model="queryParams.reimburseTypeId" placeholder="选择报销类型" clearable>
            <el-option v-for="type in reimburseTypeList" :key="type.id" :label="type.name" :value="type.id" />
          </el-select>
        </el-form-item>
        
        <el-form-item label="状态">
          <el-select v-model="queryParams.status" placeholder="选择状态" clearable>
            <el-option label="草稿" value="DRAFT" />
            <el-option label="待审批" value="PENDING" />
            <el-option label="审批中" value="PROCESSING" />
            <el-option label="已批准" value="APPROVED" />
            <el-option label="已驳回" value="REJECTED" />
            <el-option label="已完成" value="COMPLETED" />
          </el-select>
        </el-form-item>
        
        <el-form-item>
          <el-button type="primary" @click="handleQuery">
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

    <!-- 数据表格 -->
    <div class="table-section">
      <el-table :data="tableData" border v-loading="loading" style="width: 100%">
        <el-table-column type="selection" width="55" align="center" />
        <el-table-column type="index" label="序号" width="60" align="center" />
        
        <el-table-column prop="reimburseCode" label="报销单号" min-width="150" show-overflow-tooltip>
          <template #default="{ row }">
            <el-button type="primary" link @click="handleView(row)">{{ row.reimburseCode }}</el-button>
          </template>
        </el-table-column>
        
        <el-table-column prop="reimbursePerson" label="报销人" width="100" align="center" />
        
        <el-table-column prop="departmentName" label="报销部门" width="120" show-overflow-tooltip />
        
        <el-table-column prop="reimburseDate" label="报销日期" width="120" align="center">
          <template #default="{ row }">
            {{ formatDate(row.reimburseDate) }}
          </template>
        </el-table-column>
        
        <el-table-column prop="reimburseTypeName" label="报销类型" width="120" />
        
        <el-table-column prop="totalAmount" label="报销金额" width="120" align="right">
          <template #default="{ row }">
            ¥{{ formatAmount(row.totalAmount) }}
          </template>
        </el-table-column>
        
        <el-table-column prop="status" label="状态" width="120" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" effect="light">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        
        <el-table-column prop="isAdvance" label="预借款" width="80" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.isAdvance" type="warning" effect="plain">是</el-tag>
            <el-tag v-else type="info" effect="plain">否</el-tag>
          </template>
        </el-table-column>
        
        <el-table-column prop="advanceAmount" label="预借金额" width="120" align="right">
          <template #default="{ row }">
            <span v-if="row.isAdvance">¥{{ formatAmount(row.advanceAmount) }}</span>
            <span v-else>-</span>
          </template>
        </el-table-column>
        
        <el-table-column prop="currentApprover" label="当前审批人" width="120" show-overflow-tooltip />
        
        <el-table-column prop="createTime" label="创建时间" width="160" align="center">
          <template #default="{ row }">
            {{ formatDateTime(row.createTime) }}
          </template>
        </el-table-column>
        
        <el-table-column label="操作" width="200" align="center" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" link @click="handleView(row)">
              查看
            </el-button>
            
            <el-button 
              v-if="row.status === 'DRAFT'" 
              type="warning" 
              size="small" 
              link 
              @click="handleEdit(row)"
            >
              编辑
            </el-button>
            
            <el-button 
              v-if="row.status === 'DRAFT'" 
              type="success" 
              size="small" 
              link 
              @click="handleSubmit(row)"
            >
              提交
            </el-button>
            
            <el-button 
              v-if="row.status === 'DRAFT'" 
              type="danger" 
              size="small" 
              link 
              @click="handleDelete(row)"
            >
              删除
            </el-button>
            
            <el-button 
              v-if="row.status === 'APPROVED' && row.isAdvance" 
              type="primary" 
              size="small" 
              link 
              @click="handleSettlement(row)"
            >
              核销
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 分页 -->
    <div class="pagination-section">
      <el-pagination
        v-model:current-page="queryParams.pageNum"
        v-model:page-size="queryParams.pageSize"
        :page-sizes="[10, 20, 50, 100]"
        :total="total"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Plus, Download, Search, Refresh } from '@element-plus/icons-vue'
import { showSuccess, showError, showConfirm } from '@/utils/message''

// 路由和状态管理
const router = useRouter()

// 查询参数
const queryParams = reactive({
  reimburseCode: '',
  reimbursePerson: '',
  dateRange: [],
  reimburseTypeId: '',
  status: '',
  pageNum: 1,
  pageSize: 20
})

// 表格数据
const tableData = ref([])
const loading = ref(false)
const total = ref(0)

// 报销类型列表
const reimburseTypeList = ref([
  { id: '1', name: '差旅报销' },
  { id: '2', name: '业务招待' },
  { id: '3', name: '交通费' },
  { id: '4', name: '通讯费' },
  { id: '5', name: '办公用品' }
])

// 状态类型映射
const statusTypeMap = {
  DRAFT: '',
  PENDING: 'warning',
  PROCESSING: 'primary',
  APPROVED: 'success',
  REJECTED: 'danger',
  COMPLETED: 'info'
}

// 状态文本映射
const statusTextMap = {
  DRAFT: '草稿',
  PENDING: '待审批',
  PROCESSING: '审批中',
  APPROVED: '已批准',
  REJECTED: '已驳回',
  COMPLETED: '已完成'
}

// 生命周期
onMounted(() => {
  loadTableData()
})

// 方法定义
const loadTableData = async () => {
  loading.value = true
  
  try {
    // TODO: 调用API获取数据
    await new Promise(resolve => setTimeout(resolve, 1000))
    
    // 模拟数据
    tableData.value = [
      {
        id: '1',
        reimburseCode: 'REIM2024010001',
        reimbursePerson: '张经理',
        departmentName: '技术部',
        reimburseDate: '2024-01-15',
        reimburseTypeId: '1',
        reimburseTypeName: '差旅报销',
        totalAmount: 1850.00,
        status: 'APPROVED',
        isAdvance: true,
        advanceAmount: 2000.00,
        currentApprover: '李财务',
        createTime: '2024-01-15 10:30:00'
      },
      {
        id: '2',
        reimburseCode: 'REIM2024010002',
        reimbursePerson: '王会计',
        departmentName: '财务部',
        reimburseDate: '2024-01-16',
        reimburseTypeId: '5',
        reimburseTypeName: '办公用品',
        totalAmount: 650.00,
        status: 'PROCESSING',
        isAdvance: false,
        advanceAmount: 0,
        currentApprover: '张经理',
        createTime: '2024-01-16 14:20:00'
      },
      {
        id: '3',
        reimburseCode: 'REIM2024010003',
        reimbursePerson: '李销售',
        departmentName: '市场部',
        reimburseDate: '2024-01-17',
        reimburseTypeId: '2',
        reimburseTypeName: '业务招待',
        totalAmount: 1200.00,
        status: 'DRAFT',
        isAdvance: false,
        advanceAmount: 0,
        currentApprover: '',
        createTime: '2024-01-17 09:15:00'
      }
    ]
    
    total.value = 35
  } catch (error) {
    console.error('加载数据失败:', error)
    showError('加载数据失败')
  } finally {
    loading.value = false
  }
}

const handleQuery = () => {
  queryParams.pageNum = 1
  loadTableData()
}

const handleReset = () => {
  Object.assign(queryParams, {
    reimburseCode: '',
    reimbursePerson: '',
    dateRange: [],
    reimburseTypeId: '',
    status: '',
    pageNum: 1
  })
  loadTableData()
}

const handleSizeChange = (size: number) => {
  queryParams.pageSize = size
  loadTableData()
}

const handleCurrentChange = (page: number) => {
  queryParams.pageNum = page
  loadTableData()
}

const handleCreate = () => {
  router.push('/reimburse/create')
}

const handleExport = () => {
  showSuccess('导出功能开发中，即将上线')
}

const handleView = (row: any) => {
  router.push(`/reimburse/detail/${row.id}`)
}

const handleEdit = (row: any) => {
  router.push(`/reimburse/edit/${row.id}`)
}

const handleSubmit = async (row: any) => {
  try {
    await showConfirm('确定要提交此报销单吗？提交后将进入审批流程。')
    
    // TODO: 调用API提交
    await new Promise(resolve => setTimeout(resolve, 500))
    showSuccess('报销单已成功提交')
    
    // 重新加载数据
    loadTableData()
  } catch (error) {
    // 用户取消操作
  }
}

const handleDelete = async (row: any) => {
  try {
    await showConfirm('确定要删除此报销单吗？删除后无法恢复。')
    
    // TODO: 调用API删除
    await new Promise(resolve => setTimeout(resolve, 500))
    showSuccess('报销单已删除')
    
    // 重新加载数据
    loadTableData()
  } catch (error) {
    // 用户取消操作
  }
}

const handleSettlement = (row: any) => {
  showSuccess('借款核销功能开发中，即将上线')
}

const getStatusType = (status: string) => {
  return statusTypeMap[status] || ''
}

const getStatusText = (status: string) => {
  return statusTextMap[status] || '未知状态'
}

const formatDate = (date: string) => {
  if (!date) return '-';
  return date;
}

const formatDateTime = (dateTime: string) => {
  if (!dateTime) return '-';
  return dateTime;
}

const formatAmount = (amount: number) => {
  return amount?.toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 }) || '0.00'
}
</script>

<style scoped lang="css">
.reimburse-list-container {
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

.header-left h2 {
  margin: 0 0 8px 0;
  color: #303133;
  font-size: 20px;
}

.header-left p {
  margin: 0;
  color: #909399;
  font-size: 14px;
}

.header-actions {
  display: flex;
  gap: 12px;
}

.query-section {
  background: white;
  border-radius: 8px;
  padding: 20px;
  margin-bottom: 20px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.query-section .el-form {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
}

.query-section .el-form-item {
  margin-bottom: 0;
}

.table-section {
  background: white;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  margin-bottom: 20px;
}

.pagination-section {
  display: flex;
  justify-content: center;
  background: white;
  border-radius: 8px;
  padding: 16px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

/* 响应式设计 */
@media (max-width: 768px) {
  .reimburse-list-container {
    padding: 10px;
  }
  
  .page-header {
    flex-direction: column;
    gap: 16px;
    align-items: stretch;
    padding: 15px;
  }
  
  .header-actions {
    justify-content: center;
  }
  
  .query-section {
    padding: 15px;
  }
  
  .query-section .el-form {
    flex-direction: column;
    gap: 12px;
  }
}
</style>