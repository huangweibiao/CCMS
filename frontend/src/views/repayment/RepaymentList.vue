<template>
  <div class="repayment-list">
    <el-card class="repayment-card">
      <template #header>
        <div class="card-header">
          <span class="title">还款记录</span>
          <el-button type="primary" :icon="Plus" @click="goToApply">
            申请还款
          </el-button>
        </div>
      </template>

      <!-- 搜索条件 -->
      <div class="search-wrapper">
        <el-form :model="searchForm" inline>
          <el-form-item label="还款状态">
            <el-select v-model="searchForm.status" placeholder="全部状态" clearable>
              <el-option 
                v-for="status in repaymentStatus" 
                :key="status.value"
                :label="status.label" 
                :value="status.value" />
            </el-select>
          </el-form-item>
          
          <el-form-item label="还款方式">
            <el-select v-model="searchForm.repaymentMethod" placeholder="全部方式" clearable>
              <el-option 
                v-for="method in repaymentMethods" 
                :key="method.value"
                :label="method.label" 
                :value="method.value" />
            </el-select>
          </el-form-item>
          
          <el-form-item label="还款时间">
            <el-date-picker
              v-model="searchForm.dateRange"
              type="daterange"
              range-separator="至"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
              value-format="YYYY-MM-DD" />
          </el-form-item>
          
          <el-form-item>
            <el-button type="primary" :icon="Search" @click="handleSearch">
              搜索
            </el-button>
            <el-button :icon="Refresh" @click="handleReset">
              重置
            </el-button>
          </el-form-item>
        </el-form>
      </div>

      <!-- 还款列表 -->
      <div class="table-wrapper">
        <el-table 
          :data="repaymentList" 
          v-loading="loading"
          style="width: 100%">
          
          <el-table-column prop="repaymentNo" label="还款单号" width="180">
            <template #default="{ row }">
              <el-tag type="info" size="small">{{ row.repaymentNo }}</el-tag>
            </template>
          </el-table-column>
          
          <el-table-column prop="loanNo" label="关联借款单" width="180">
            <template #default="{ row }">
              <el-link type="primary" :underline="false" @click="viewLoanDetail(row.loanId)">
                {{ row.loanNo }}
              </el-link>
            </template>
          </el-table-column>
          
          <el-table-column prop="repaymentAmount" label="还款金额" width="120" align="right">
            <template #default="{ row }">
              <span class="amount">¥{{ row.repaymentAmount?.toLocaleString() }}</span>
            </template>
          </el-table-column>
          
          <el-table-column prop="repaymentMethod" label="还款方式" width="120">
            <template #default="{ row }">
              <el-tag :type="getRepaymentMethodTagType(row.repaymentMethod)">
                {{ getRepaymentMethodText(row.repaymentMethod) }}
              </el-tag>
            </template>
          </el-table-column>
          
          <el-table-column prop="status" label="状态" width="100">
            <template #default="{ row }">
              <el-tag :type="getStatusTagType(row.status)">
                {{ getStatusText(row.status) }}
              </el-tag>
            </template>
          </el-table-column>
          
          <el-table-column prop="actualRepaymentDate" label="实际还款日期" width="120">
            <template #default="{ row }">
              {{ formatDate(row.actualRepaymentDate) }}
            </template>
          </el-table-column>
          
          <el-table-column prop="createTime" label="创建时间" width="180">
            <template #default="{ row }">
              {{ formatDateTime(row.createTime) }}
            </template>
          </el-table-column>
          
          <el-table-column prop="remark" label="备注" min-width="150">
            <template #default="{ row }">
              <span v-if="row.remark" class="remark">{{ row.remark }}</span>
              <span v-else class="text-muted">-</span>
            </template>
          </el-table-column>
          
          <el-table-column label="操作" width="120" fixed="right">
            <template #default="{ row }">
              <el-button 
                type="text" 
                size="small" 
                @click="viewDetail(row)"
                :icon="View">
                详情
              </el-button>
              
              <el-button 
                v-if="row.status === 'PENDING'"
                type="text" 
                size="small" 
                @click="handleCancel(row)"
                :icon="Close">
                撤销
              </el-button>
            </template>
          </el-table-column>
        </el-table>
        
        <!-- 分页 -->
        <div class="pagination-wrapper">
          <el-pagination
            v-model:current-page="pagination.current"
            v-model:page-size="pagination.size"
            :total="pagination.total"
            :page-sizes="[10, 20, 50, 100]"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
            background />
        </div>
      </div>
    </el-card>
    
    <!-- 还款详情对话框 -->
    <repayment-detail-dialog 
      v-model="detailVisible" 
      :repayment-id="currentRepaymentId" />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search, Refresh, View, Close } from '@element-plus/icons-vue'
import { useLoanStore } from '@/stores/loan'
import RepaymentDetailDialog from './components/RepaymentDetailDialog.vue'

const router = useRouter()
const loanStore = useLoanStore()

// 搜索表单
const searchForm = reactive({
  status: '',
  repaymentMethod: '',
  dateRange: []
})

// 分页
const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

const loading = ref(false)
const repaymentList = ref<any[]>([])
const detailVisible = ref(false)
const currentRepaymentId = ref('')

// 还款状态选项
const repaymentStatus = [
  { label: '待审批', value: 'PENDING' },
  { label: '审批中', value: 'APPROVING' },
  { label: '已通过', value: 'APPROVED' },
  { label: '已拒绝', value: 'REJECTED' },
  { label: '已完成', value: 'COMPLETED' },
  { label: '已撤销', value: 'CANCELLED' }
]

// 还款方式选项
const repaymentMethods = [
  { label: '银行转账', value: 'BANK_TRANSFER' },
  { label: '现金还款', value: 'CASH' },
  { label: '工资抵扣', value: 'SALARY_DEDUCTION' },
  { label: '费用抵扣', value: 'EXPENSE_OFFSET' },
  { label: '其他方式', value: 'OTHER' }
]

// 获取还款方式显示文本
const getRepaymentMethodText = (method: string) => {
  const methodObj = repaymentMethods.find(m => m.value === method)
  return methodObj ? methodObj.label : method
}

// 获取还款方式标签样式
const getRepaymentMethodTagType = (method: string) => {
  const types = {
    'BANK_TRANSFER': 'primary',
    'CASH': 'success',
    'SALARY_DEDUCTION': 'warning',
    'EXPENSE_OFFSET': 'info',
    'OTHER': 'default'
  }
  return types[method as keyof typeof types] || 'default'
}

// 获取状态显示文本
const getStatusText = (status: string) => {
  const statusObj = repaymentStatus.find(s => s.value === status)
  return statusObj ? statusObj.label : status
}

// 获取状态标签样式
const getStatusTagType = (status: string) => {
  const types = {
    'PENDING': 'warning',
    'APPROVING': 'primary',
    'APPROVED': 'info',
    'COMPLETED': 'success',
    'REJECTED': 'danger',
    'CANCELLED': 'info'
  }
  return types[status as keyof typeof types] || 'default'
}

// 日期格式化
const formatDate = (date: string | Date) => {
  if (!date) return '-';
  return new Date(date).toLocaleDateString('zh-CN')
}

const formatDateTime = (date: string | Date) => {
  if (!date) return '-';
  return new Date(date).toLocaleString('zh-CN')
}

// 加载还款记录列表
const loadRepaymentList = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.current,
      size: pagination.size,
      status: searchForm.status,
      repaymentMethod: searchForm.repaymentMethod,
      startDate: searchForm.dateRange?.[0],
      endDate: searchForm.dateRange?.[1]
    }
    
    await loanStore.loadRepaymentList(params)
    repaymentList.value = loanStore.repaymentList
    pagination.total = loanStore.repaymentTotalCount
  } catch (error) {
    ElMessage.error('加载还款记录失败')
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  pagination.current = 1
  loadRepaymentList()
}

// 重置搜索条件
const handleReset = () => {
  searchForm.status = ''
  searchForm.repaymentMethod = ''
  searchForm.dateRange = []
  pagination.current = 1
  loadRepaymentList()
}

// 跳转到还款申请页面
const goToApply = () => {
  router.push('/repayment/apply')
}

// 查看还款详情
const viewDetail = (row: any) => {
  currentRepaymentId.value = row.id
  detailVisible.value = true
}

// 查看借款详情
const viewLoanDetail = (loanId: string) => {
  // TODO: 实现跳转到借款详情页面
  ElMessage.info('查看借款详情功能开发中')
}

// 撤销还款申请
const handleCancel = async (row: any) => {
  try {
    await ElMessageBox.confirm(
      `确定要撤销还款单号${row.repaymentNo}的申请吗？`,
      '确认撤销',
      {
        type: 'warning',
        confirmButtonText: '确定',
        cancelButtonText: '取消'
      }
    )
    
    await loanStore.cancelRepayment(row.id)
    ElMessage.success('还款申请已撤销')
    loadRepaymentList()
  } catch (error) {
    // 用户取消操作
  }
}

// 分页事件
const handleSizeChange = (size: number) => {
  pagination.size = size
  pagination.current = 1
  loadRepaymentList()
}

const handleCurrentChange = (page: number) => {
  pagination.current = page
  loadRepaymentList()
}

onMounted(() => {
  loadRepaymentList()
})
</script>

<style scoped>
.repayment-list {
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

.search-wrapper {
  margin-bottom: 20px;
  padding: 20px;
  background: #f8f9fa;
  border-radius: 4px;
}

.table-wrapper {
  margin-top: 20px;
}

.amount {
  font-weight: 600;
  color: #f56c6c;
}

.remark {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.text-muted {
  color: #909399;
}

.pagination-wrapper {
  margin-top: 20px;
  text-align: right;
}
</style>