<template>
  <div class="budget-management-container">
    <div class="header">
      <h1>预算管理</h1>
      <div class="actions">
        <el-button type="primary" @click="handleCreate">新建预算</el-button>
      </div>
    </div>
    
    <el-card class="content-card">
      <el-table :data="budgets" border stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="预算名称" />
        <el-table-column prop="department" label="部门" />
        <el-table-column prop="year" label="年度" />
        <el-table-column prop="totalAmount" label="总预算" />
        <el-table-column prop="usedAmount" label="已用金额" />
        <el-table-column prop="remainingAmount" label="剩余金额" />
        <el-table-column label="操作" width="200">
          <template #default="scope">
            <el-button size="small" @click="handleEdit(scope.row)">编辑</el-button>
            <el-button size="small" type="danger" @click="handleDelete(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <div class="pagination">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :total="total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'

interface Budget {
  id: number
  name: string
  department: string
  year: number
  totalAmount: number
  usedAmount: number
  remainingAmount: number
}

const budgets = ref<Budget[]>([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

const handleCreate = () => {
  // TODO: 实现新建预算逻辑
  console.log('新建预算')
}

const handleEdit = (budget: Budget) => {
  // TODO: 实现编辑预算逻辑
  console.log('编辑预算:', budget)
}

const handleDelete = (budget: Budget) => {
  // TODO: 实现删除预算逻辑
  console.log('删除预算:', budget)
}

onMounted(() => {
  // 模拟数据
  budgets.value = [
    { id: 1, name: '技术部年度预算', department: '技术部', year: 2026, totalAmount: 500000, usedAmount: 120000, remainingAmount: 380000 },
    { id: 2, name: '市场部年度预算', department: '市场部', year: 2026, totalAmount: 300000, usedAmount: 80000, remainingAmount: 220000 },
    { id: 3, name: '财务部年度预算', department: '财务部', year: 2026, totalAmount: 200000, usedAmount: 50000, remainingAmount: 150000 }
  ]
  total.value = budgets.value.length
})
</script>

<style scoped lang="css">
.budget-management-container {
  max-width: 1200px;
  margin: 0 auto;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.header h1 {
  font-size: 24px;
  margin: 0;
  color: #303133;
}

.content-card {
  border-radius: 8px;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>