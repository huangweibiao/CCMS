<template>
  <div class="finance-dashboard">
    <!-- 财务管理仪表盘 -->
    <el-container>
      <el-row :gutter="20">
        <!-- 财务概况 -->
        <el-col :span="6">
          <el-card class="box-card">
            <template #header>
              <div class="card-header">
                <i class="el-icon-s-finance"></i>
                <span>财务概况</span>
                <el-button type="text" size="small">刷新</el-button>
              </div>
            </template>
            <div class="card-body">
              <div class="finance-stats">
                <div class="stat-item">
                  <div class="stat-value">¥ 1,234,567.89</div>
                  <div class="stat-label">本期费用</div>
                </div>
                <div class="stat-item">
                  <div class="stat-value">¥ 2,345,678.90</div>
                  <div class="stat-label">本期支付</div>
                </div>
                <div class="stat-item">
                  <div class="stat-value">-¥ 111,110.99</div>
                  <div class="stat-label">本期结余</div>
                </div>
              </div>
            </div>
          </el-card>
        </el-col>

        <!-- 凭证管理入口 -->
        <el-col :span="6">
          <el-row :gutter="10">
            <el-col :span="8">
              <div class="quick-action" @click="showVoucherManager">
                <el-card shadow="hover">
                  <div class="action-icon">
                    <i class="el-icon-document"></i>
                  </div>
                  <div class="action-content">
                    <div class="action-title">凭证管理</div>
                    <div class="action-desc">财务凭证生成与审核</div>
                  </div>
                </el-card>
              </div>
            </el-col>
            <el-col :span="8">
              <div class="quick-action" @click="showPaymentManager">
                <el-card shadow="hover">
                  <div class="action-icon">
                    <i class="el-icon-wallet"></i>
                  </div>
                  <div class="action-content">
                    <div class="action-title">支付管理</div>
                    <div class="action-desc">支付单据与审批流程</div>
                  </div>
                </el-card>
              </div>
            </el-col>
          </el-row>
        </el-col>

        <!-- 报表中心入口 -->
        <el-col :span="6">
          <el-row :gutter="10">
            <el-col :span="8">
              <div class="quick-action" @click="showReportCenter">
                <el-card shadow="hover">
                  <div class="action-icon">
                    <i class="el-icon-data-analysis"></i>
                  </div>
                  <div class="action-content">
                    <div class="action-title">报表中心</div>
                    <div class="action-desc">财务数据统计与分析</div>
                  </div>
                </el-card>
              </div>
            </el-col>
          </el-row>
        </el-col>
      </el-row>

      <!-- 最近动态 -->
      <el-row :gutter="20">
        <el-col :span="12">
          <el-card>
            <template #header>
              <div class="card-header">
                <i class="el-icon-bell"></i>
                <span>最近动态</span>
                <el-tag type="success" size="small">今日</el-tag>
              </div>
            </template>
            <div class="card-body">
              <el-timeline>
                <el-timeline-item timestamp="2026-05-11 09:30">
                  <div class="timeline-content">
                    <div class="timeline-title">凭证审核通过</div>
                    <div class="timeline-desc">支付单 #PAY2023110001 已审核通过</div>
                  </div>
                </el-timeline-item>
                <el-timeline-item timestamp="2026-05-11 10:15">
                  <div class="timeline-content">
                    <div class="timeline-title">费用报销完成</div>
                    <div class="timeline-desc">报销单 #EXP2023110045 已完成报销</div>
                  </div>
                </el-timeline-item>
                <el-timeline-item timestamp="2026-05-11 08:20">
                  <div class="timeline-content">
                    <div class="timeline-title">新凭证生成</div>
                    <div class="timeline-desc">自动生成凭证 #FIN2023110006</div>
                  </div>
                </el-timeline-item>
              </el-timeline>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </el-container>
  </div>
</template>

<script>
import { ref } from 'vue';
import { ElMessage } from 'element-plus';

export default {
  name: 'FinanceDashboard',
  components: {},
  
  setup() {
    const showVoucherManager = ref(false);
    const showPaymentManager = ref(false);
    const showReportCenter = ref(false);

    return {
      showVoucherManager,
      showPaymentManager,
      showReportCenter
    };
  }
};
</script>

<style scoped lang="scss">
.finance-dashboard {
  padding: 20px;
  background-color: #f5f5f5;
  
  .box-card {
    margin-bottom: 20px;
    border-radius: 8px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
    
    &:hover {
      transform: translateY(-2px);
      box-shadow: 0 4px 16px rgba(0, 0, 0, 0.15);
    }
  }
  
  .card-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 15px 20px;
    border-bottom: 1px solid #e0e0e0;
    background-color: #fafafa;
    
    i {
      font-size: 20px;
      color: #409eff;
      margin-right: 10px;
    }
    
    span {
      font-size: 16px;
      font-weight: 600;
      color: #303133;
    }
  }
  
  .card-body {
    padding: 20px;
    
    .finance-stats {
      display: grid;
      grid-template-columns: repeat(2, 1fr);
      gap: 20px;
    }
    
    .stat-item {
      text-align: center;
      padding: 15px;
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      border-radius: 12px;
      transition: all 0.3s ease;
      
      &:hover {
        transform: scale(1.05);
        box-shadow: 0 8px 16px rgba(0, 0, 0, 0.2);
      }
    }
    
    .stat-value {
      font-size: 24px;
      font-weight: bold;
      color: #ffffff;
      margin-bottom: 5px;
    }
    
    .stat-label {
      font-size: 14px;
      color: #ffffff;
      opacity: 0.9;
    }
  }
  
  .quick-action {
    cursor: pointer;
    transition: all 0.3s ease;
    
    &:hover {
      transform: translateY(-4px);
    }
  }
  
  .action-icon {
    width: 80px;
    height: 80px;
    margin: 0 auto 20px;
    background: linear-gradient(135deg, #409eff 0%, #409eff 100%);
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    
    i {
      font-size: 32px;
      color: #ffffff;
    }
  }
  
  .action-content {
    text-align: center;
  }
  
  .action-title {
    font-size: 16px;
    font-weight: 600;
    color: #303133;
    margin-bottom: 8px;
  }
  
  .action-desc {
    font-size: 14px;
    color: #909399;
    line-height: 1.5;
  }
  
  .timeline-content {
    padding: 15px;
  }
  
  .timeline-title {
    font-size: 14px;
    font-weight: 600;
    color: #303133;
    margin-bottom: 5px;
  }
  
  .timeline-desc {
    font-size: 13px;
    color: #606266;
    line-height: 1.6;
  }
}
</style>