<template>
  <div class="dashboard-container">
    <div class="dashboard-header">
      <h1>仪表板</h1>
      <p class="welcome-text">欢迎使用企业费控管理系统</p>
    </div>
    
    <div class="dashboard-content">
      <div class="stats-grid">
        <el-card class="stat-item" shadow="hover">
          <div class="stat-content">
            <div class="stat-icon">📊</div>
            <div class="stat-info">
              <div class="stat-value">0</div>
              <div class="stat-label">待处理申请</div>
            </div>
          </div>
        </el-card>
        
        <el-card class="stat-item" shadow="hover">
          <div class="stat-content">
            <div class="stat-icon">📝</div>
            <div class="stat-info">
              <div class="stat-value">0</div>
              <div class="stat-label">待审批事项</div>
            </div>
          </div>
        </el-card>
        
        <el-card class="stat-item" shadow="hover">
          <div class="stat-content">
            <div class="stat-icon">💰</div>
            <div class="stat-info">
              <div class="stat-value">¥0</div>
              <div class="stat-label">本月预算余额</div>
            </div>
          </div>
        </el-card>
        
        <el-card class="stat-item" shadow="hover">
          <div class="stat-content">
            <div class="stat-icon">✅</div>
            <div class="stat-info">
              <div class="stat-value">0</div>
              <div class="stat-label">已完成报销</div>
            </div>
          </div>
        </el-card>
      </div>
      
      <div class="quick-actions">
        <h3>快速操作</h3>
        <div class="action-buttons">
          <el-button type="primary" size="large">新建费用申请</el-button>
          <el-button size="large">报销管理</el-button>
          <el-button size="large">预算查询</el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
// 仪表板页面 - 显示系统概览和快捷操作
</script>

<style scoped lang="css">
.dashboard-container {
  max-width: 1200px;
  margin: 0 auto;
}

.dashboard-header {
  margin-bottom: 30px;
}

.dashboard-header h1 {
  font-size: 28px;
  margin-bottom: 8px;
  color: #303133;
}

.welcome-text {
  font-size: 16px;
  color: #606266;
  margin: 0;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 20px;
  margin-bottom: 30px;
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
  font-size: 24px;
  font-weight: bold;
  color: #303133;
  margin-bottom: 4px;
}

.stat-label {
  font-size: 14px;
  color: #606266;
}

.quick-actions {
  background: white;
  padding: 20px;
  border-radius: 8px;
}

.quick-actions h3 {
  margin-bottom: 16px;
  font-size: 18px;
  color: #303133;
}

.action-buttons {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

@media (max-width: 768px) {
  .stats-grid {
    grid-template-columns: 1fr;
  }
  
  .action-buttons {
    flex-direction: column;
  }
  
  .action-buttons .el-button {
    width: 100%;
  }
}
</style>