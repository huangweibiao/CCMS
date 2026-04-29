<template>
  <div class="loan-list">
    <el-card class="loan-card">
      <template #header>
        <div class="card-header">
          <span class="title">我的借款</span>
          <el-button type="primary" :icon="Plus" @click="goToApply">
            申请借款
          </el-button>
        </div>
      </template>

      <!-- 搜索条件 -->
      <div class="search-wrapper">
        <el-form :model="searchForm" inline>
          <el-form-item label="借款类型">
            <el-select 
              v-model="searchForm.loanType" 
              placeholder="全部类型" 
              clearable>
              <el-option 
                v-for="type in loanTypes" 
                :key="type.value"
                :label="type.label" 
                :value="type.value" />
            </el-select>
          </el-form-item>
          
          <el-form-item label="状态">
            <el-select v-model="searchForm.status" placeholder="全部状态" clearable>
              <el-option 
                v-for="status in loanStatus" 
                :key="status.value"
                :label="status.label" 
                :value="status.value" />
            </el-select>
          </el-form-item>
          
          <el-form-item label="申请时间">
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

      <!-- 借款列表 -->
      <div class="table-wrapper">
        <el-table 
          :data="loanList" 
          v-loading="loading"
          style="width: 100%">
          
          <el-table-column prop="loanNo" label="借款单号" width="180">
            <template #default="{ row }">
              <el-tag type="info" size="small">{{ row.loanNo }}</el-tag>
            </template>
          </el-table-column>
          
          <el-table-column prop="loanType" label="借款类型" width="120">
            <template #default="{ row }">
              <el-tag :type="getLoanTypeTagType(row.loanType)">
                {{ getLoanTypeText(row.loanType) }}
              </el-tag>
            </template>
          </el-table-column>
          
          <el-table-column prop="amount" label="借款金额" width="120" align="right">
            <template #default="{ row }">
              <span class="amount">¥{{ row.amount.toLocaleString() }}</span>
            </template>
          </el-table-column>
          
          <el-table-column prop="status" label="状态" width="120">
            <template #default="{ row }">
              <el-tag :type="getStatusTagType(row.status)">
                {{ getStatusText(row.status) }}
              </el-tag>
            </template>
          </el-table-column>
          
          <el-table-column prop="applyTime" label="申请时间" width="180">
            <template #default="{ row }">
              {{ formatDateTime(row.applyTime) }}
            </template>
          </el-table-column>
          
          <el-table-column prop="expectedRepaymentDate" label="预计还款日期" width="120">
            <template #default="{ row }">
              {{ formatDate(row.expectedRepaymentDate) }}
            </template>
          </el-table-column>
          
          <el-table-column prop="currentApprover" label="当前审批人" width="120">
            <template #default="{ row }">
              <span v-if="row.currentApprover">{{ row.currentApprover }}</span>
              <span v-else class="text-muted">-</span>
            </template>
          </el-table-column>
          
          <el-table-column label="操作" width="200" fixed="right">
            <template #default="{ row }">
              <el-button 
                type="text" 
                size="small" 
                @click="viewDetail(row)"
                :icon="View">
                查看
              </el-button>
              
              <el-button 
                v-if="row.status === 'PENDING'"
                type="text" 
                size="small" 
                @click="handleCancel(row)"
                :icon="Close">
                撤销
              </el-button>
              
              <el-button 
                v-if="row.status === 'APPROVED'"
                type="text" 
                size="small" 
                @click="handleRepay(row)"
                :icon="Money">
                还款
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
    
    <!-- 借款详情对话框 -->
    <loan-detail-dialog 
      v-model="detailVisible" 
      :loan-id="currentLoanId" />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search, Refresh, View, Close, Money } from '@element-plus/icons-vue'
import { useLoanStore } from '@/stores/loan'
import LoanDetailDialog from './components/LoanDetailDialog.vue'

const router = useRouter()
const loanStore = useLoanStore()

// 搜索表单
const searchForm = reactive({
  loanType: '',
  status: '',
  dateRange: []
})

// 分页
const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

const loading = ref(false)
const loanList = ref<any[]>([])
const detailVisible = ref(false)
const currentLoanId = ref('')

// 借款类型选项
const loanTypes = [
  { label: '备用金借款', value: 'RESERVE' },
  { label: '差旅借款', value: 'TRAVEL' },
  { label: '采购借款', value: 'PURCHASE' },
  { label: '业务借款', value: 'BUSINESS' },
  { label: '其他借款', value: 'OTHER' }
]

// 借款状态选项
const loanStatus = [
  { label: '待审批', value: 'PENDING' },
  { label: '审批中', value: 'APPROVING' },
  { label: '已通过', value: 'APPROVED' },
  { label: '已拒绝', value: 'REJECTED' },
  { label: '已撤销', value: 'CANCELLED' },
  { label: '已还款', value: 'REPAID' }
]

// 获取借款类型显示文本
const getLoanTypeText = (type: string) => {
  const typeObj = loanTypes.find(t => t.value === type)
  return typeObj ? typeObj.label : type
}

// 获取借款类型标签样式
const getLoanTypeTagType = (type: string) => {
  const types = {
    'RESERVE': 'success',
    'TRAVEL': 'primary',
    'PURCHASE': 'warning',
    'BUSINESS': 'info',
    'OTHER': 'default'
  }
  return types[type as keyof typeof types] || 'default'
}

// 获取状态显示文本
const getStatusText = (status: string) => {
  const statusObj = loanStatus.find(s => s.value === status)
  return statusObj ? statusObj.label : status
}

// 获取状态标签样式
const getStatusTagType = (status: string) => {
  const types = {
    'PENDING': 'warning',
    'APPROVING': 'primary',
    'APPROVED': 'success',
    'REJECTED': 'danger',
    'CANCELLED': 'info',
    'REPAID': 'default'
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

// 加载借款列表
const loadLoanList = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.current,
      size: pagination.size,
      loanType: searchForm.loanType,
      status: searchForm.status,
      startDate: searchForm.dateRange?.[0],
      endDate: searchForm.dateRange?.[1]
    }
    
    await loanStore.loadLoanList(params)
    loanList.value = loanStore.loanList
    pagination.total = loanStore.totalCount
  } catch (error) {
    ElMessage.error('加载借款列表失败')
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  pagination.current = 1
  loadLoanList()
}

// 重置搜索条件
const handleReset = () => {
  searchForm.loanType = ''
  searchForm.status = ''
  searchForm.dateRange = []
  pagination.current = 1
  loadLoanList()
}

// 跳转到申请页面
const goToApply = () => {
  router.push('/loan/apply')
}

// 查看详情
const viewDetail = (row: any) => {
  currentLoanId.value = row.id
  detailVisible.value = true
}

// 撤销借款申请
const handleCancel = async (row: any) => {
  try {
    await ElMessageBox.confirm(
      `确定要撤销借款单号${row.loanNo}的申请吗？`,
      '确认撤销',
      {
        type: 'warning',
        confirmButtonText: '确定',
        cancelButtonText: '取消'
      }
    )
    
    await loanStore.cancelLoan(row.id)
    ElMessage.success('借款申请已撤销')
    loadLoanList()
  } catch (error) {
    // 用户取消操作
  }
}

// 还款操作
const handleRepay = (row: any) => {
  router.push(`/repayment/apply?loanId=${row.id}`)
}

// 分页事件
const handleSizeChange = (size: number) => {
  pagination.size = size
  pagination.current = 1
  loadLoanList()
}

const handleCurrentChange = (page: number) => {
  pagination.current = page
  loadLoanList()
}

onMounted(() => {
  loadLoanList()
})
</script>

<style scoped>
.loan-list {
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

.text-muted {
  color: #909399;
}

.pagination-wrapper {
  margin-top: 20px;
  text-align: right;
}
</style>