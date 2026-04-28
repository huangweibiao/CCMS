<template>
  <div class="department-list-container">
    <div class="header">
      <h1>部门管理</h1>
      <div class="actions">
        <el-button type="primary" @click="handleCreate">新建部门</el-button>
      </div>
    </div>
    
    <el-card class="content-card">
      <el-table :data="departments" border stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="部门名称" />
        <el-table-column prop="code" label="部门代码" />
        <el-table-column prop="manager" label="部门负责人" />
        <el-table-column prop="memberCount" label="成员数量" />
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

interface Department {
  id: number
  name: string
  code: string
  manager: string
  memberCount: number
}

const departments = ref<Department[]>([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

const handleCreate = () => {
  // TODO: 实现新建部门逻辑
  console.log('新建部门')
}

const handleEdit = (department: Department) => {
  // TODO: 实现编辑部门逻辑
  console.log('编辑部门:', department)
}

const handleDelete = (department: Department) => {
  // TODO: 实现删除部门逻辑
  console.log('删除部门:', department)
}

onMounted(() => {
  // 模拟数据
  departments.value = [
    { id: 1, name: '技术部', code: 'TECH', manager: '张三', memberCount: 15 },
    { id: 2, name: '市场部', code: 'MARKET', manager: '李四', memberCount: 8 },
    { id: 3, name: '财务部', code: 'FINANCE', manager: '王五', memberCount: 6 }
  ]
  total.value = departments.value.length
})
</script>

<style scoped lang="css">
.department-list-container {
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