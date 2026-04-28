<template>
  <div class="expense-apply-container">
    <div class="header">
      <h1>费用申请</h1>
      <div class="actions">
        <el-button type="primary" @click="handleCreate">新建申请</el-button>
      </div>
    </div>
    
    <el-card class="content-card">
      <el-table :data="applications" border stripe>
        <el-table-column prop="id" label="申请ID" width="100" />
        <el-table-column prop="title" label="申请标题" />
        <el-table-column prop="amount" label="申请金额" />
        <el-table-column prop="status" label="状态" />
        <el-table-column prop="createTime" label="申请时间" />
        <el-table-column label="操作" width="150">
          <template #default="scope">
            <el-button size="small" @click="handleView(scope.row)">查看</el-button>
            <el-button size="small" type="danger" @click="handleDelete(scope.row)">撤销</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'

interface Application {
  id: number
  title: string
  amount: number
  status: string
  createTime: string
}

const applications = ref<Application[]>([])

const handleCreate = () => {
  console.log('新建申请')
}

const handleView = (app: Application) => {
  console.log('查看申请:', app)
}

const handleDelete = (app: Application) => {
  console.log('撤销申请:', app)
}

onMounted(() => {
  applications.value = []
})
</script>

<style scoped lang="css">
.expense-apply-container {
  max-width: 1200px;
  margin: 0 auto;
}
</style>