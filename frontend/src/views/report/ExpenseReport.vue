<template>
  <div class="expense-report-container">
    <h1>费用统计</h1>
    <p>费用统计报表页面 - 功能开发中</p>
    <div class="stats-grid">
      <el-card class="stat-item" shadow="hover">
        <div class="stat-content">
          <div class="stat-icon">📊</div>
          <div class="stat-info">
            <div class="stat-value">¥154,680.50</div>
            <div class="stat-label">总费用金额</div>
          </div>
        </div>
      </el-card>
      
      <el-card class="stat-item" shadow="hover">
        <div class="stat-content">
          <div class="stat-icon">📁</div>
          <div class="stat-info">
            <div class="stat-value">42</div>
            <div class="stat-label">总单据量</div>
          </div>
        </div>
      </el-card>
      
      <el-card class="stat-item" shadow="hover">
        <div class="stat-content">
          <div class="stat-icon">💰</div>
          <div class="stat-info">
            <div class="stat-value">¥3,682.86</div>
            <div class="stat-label">平均费用</div>
          </div>
        </div>
      </el-card>
      
      <el-card class="stat-item" shadow="hover">
        <div class="stat-content">
          <div class="stat-icon">📈</div>
          <div class="stat-info">
            <div class="stat-value">+12.5%</div>
            <div class="stat-label">环比增长率</div>
          </div>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup lang="ts">
// 费用统计报表 - 简化版本（免图表依赖）
</script>

<style scoped lang="css">
.expense-report-container {
  max-width: 1200px;
  margin: 0 auto;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 20px;
  margin-top: 30px;
}

.stat-item {
  border-radius: 8px;
}

.stat-content {
  display: flex;
  align-items: center;
  padding: 20px;
}

.stat-icon {
  font-size: 32px;
  margin-right: 16px;
}

.stat-value {
  font-size: 18px;
  font-weight: bold;
  color: #303133;
  margin-bottom: 4px;
}

.stat-label {
  font-size: 14px;
  color: #606266;
}

@media (max-width: 768px) {
  .stats-grid {
    grid-template-columns: 1fr;
  }
}
</style>