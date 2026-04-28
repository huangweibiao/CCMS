<template>
  <div class="reimburse-list-container">
    <div class="header">
      <h1>费用报销</h1>
      <div class="actions">
        <el-button type="primary" @click="handleCreate">新建报销</el-button>
      </div>
    </div>
    
    <el-card class="content-card">
      <p>报销列表页面</p>
    </el-card>
  </div>
</template>

<script setup lang="ts">
const handleCreate = () => {
  console.log('新建报销')
}
</script>

<style scoped lang="css">
.reimburse-list-container {
  max-width: 1200px;
  margin: 0 auto;
}
</style>