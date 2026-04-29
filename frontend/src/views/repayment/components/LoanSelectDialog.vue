<template>
  <div class="loan-select-dialog">
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
        
        <el-form-item label="还款状态">
          <el-select 
            v-model="searchForm.repaymentStatus" 
            placeholder="全部状态" 
            clearable>
            <el-option 
              v-for="status in repaymentStatus" 
              :key="status.value"
              :label="status.label" 
              :value="status.value" />
          </el-select>
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

    <div class="table-wrapper">
      <el-table 
        :data="loanList" 
        v-loading="loading"
        style="width: 100%"
        @row-click="handleRowClick">
        
        <el-table-column prop="loanNo" label="借款单号" width="150">
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
            <span class="amount">¥{{ row.amount?.toLocaleString() }}</span>
          </template>
        </el-table-column>
        
        <el-table-column prop="repaidAmount" label="已还金额" width="120" align="right">
          <template #default="{ row }">
            <span>¥{{ row.repaidAmount?.toLocaleString() || '0' }}</span>
          </template>
        </el-table-column>
        
        <el-table-column prop="remainAmount" label="待还金额" width="120" align="right">
          <template #default="{ row }">
            <span class="remain-amount">¥{{ (row.amount - (row.repaidAmount || 0)).toLocaleString() }}</span>
          </template>
        </el-table-column>
        
        <el-table-column prop="repaymentStatus" label="还款状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getRepaymentStatusTagType(row)" size="small">
              {{ getRepaymentStatusText(row) }}
            </el-tag>
          </template>
        </el-table-column>
        
        <el-table-column prop="expectedRepaymentDate" label="预计还款" width="120">
          <template #default="{ row }">
            {{ formatDate(row.expectedRepaymentDate) }}
          </template>
        </el-table-column>
        
        <el-table-column label="操作" width="80" align="center">
          <template #default="{ row }">
            <el-button 
              type="primary" 
              link 
              size="small" 
              @click="handleSelect(row)">
              选择
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="pagination.current"
          v-model:page-size="pagination.size"
          :total="pagination.total"
          :page-sizes="[5, 10, 20]"
          layout="total, sizes, prev, pager, next"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange" />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { Search, Refresh } from '@element-plus/icons-vue'
import { useLoanStore } from '@/stores/loan'

interface Emits {
  (e: 'select', loan: any): void
}

const emit = defineEmits<Emits>()

const loanStore = useLoanStore()

// 搜索表单
const searchForm = reactive({
  loanType: '',
  repaymentStatus: ''
})

// 分页
const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

const loading = ref(false)
const loanList = ref<any[]>([])

// 借款类型选项
const loanTypes = [
  { label: '备用金借款', value: 'RESERVE' },
  { label: '差旅借款', value: 'TRAVEL' },
  { label: '采购借款', value: 'PURCHASE' },
  { label: '业务借款', value: 'BUSINESS' },
  { label: '其他借款', value: 'OTHER' }
]

// 还款状态选项
const repaymentStatus = [
  { label: '全部还清', value: 'FULLY_REPAID' },
  { label: '部分还款', value: 'PARTIALLY_REPAID' },
  { label: '未还款', value: 'NOT_REPAID' }
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

// 获取还款状态显示文本
const getRepaymentStatusText = (row: any) => {
  const repaidAmount = row.repaidAmount || 0
  const totalAmount = row.amount || 0
  
  if (repaidAmount >= totalAmount) return '全部还清'
  if (repaidAmount > 0) return '部分还款'
  return '未还款'
}

// 获取还款状态标签样式
const getRepaymentStatusTagType = (row: any) => {
  const repaidAmount = row.repaidAmount || 0
  const totalAmount = row.amount || 0
  
  if (repaidAmount >= totalAmount) return 'success'
  if (repaidAmount > 0) return 'warning'
  return 'info'
}

// 日期格式化
const formatDate = (date: string | Date) => {
  if (!date) return '-';
  return new Date(date).toLocaleDateString('zh-CN')
}

// 加载可还款的借款列表
const loadRepayableLoans = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.current,
      size: pagination.size,
      loanType: searchForm.loanType,
      status: 'APPROVED', // 只显示已审批通过的借款
      repaymentStatus: searchForm.repaymentStatus
    }
    
    await loanStore.loadLoanList(params)
    loanList.value = loanStore.loanList.filter(loan => {
      const remainAmount = loan.amount - (loan.repaidAmount || 0)
      return remainAmount > 0 // 只显示有欠款的借款
    })
    pagination.total = loanList.value.length
  } catch (error) {
    loanList.value = []
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  pagination.current = 1
  loadRepayableLoans()
}

// 重置搜索条件
const handleReset = () => {
  searchForm.loanType = ''
  searchForm.repaymentStatus = ''
  pagination.current = 1
  loadRepayableLoans()
}

// 选择借款
const handleSelect = (row: any) => {
  emit('select', row)
}

// 点击行选择
const handleRowClick = (row: any) => {
  handleSelect(row)
}

// 分页事件
const handleSizeChange = (size: number) => {
  pagination.size = size
  pagination.current = 1
  loadRepayableLoans()
}

const handleCurrentChange = (page: number) => {
  pagination.current = page
  loadRepayableLoans()
}

onMounted(() => {
  loadRepayableLoans()
})
</script>

<style scoped>
.loan-select-dialog {
  height: 500px;
  display: flex;
  flex-direction: column;
}

.search-wrapper {
  padding: 0 0 20px 0;
  border-bottom: 1px solid #ebeef5;
}

.table-wrapper {
  flex: 1;
  overflow: auto;
}

.amount {
  font-weight: 600;
  color: #f56c6c;
}

.remain-amount {
  font-weight: 600;
  color: #e6a23c;
}

.pagination-wrapper {
  margin-top: 20px;
  text-align: right;
}

:deep(.el-table .el-table__row) {
  cursor: pointer;
}

:deep(.el-table .el-table__row:hover) {
  background-color: #f5f7fa;
}
</style>