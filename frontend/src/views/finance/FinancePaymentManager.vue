<template>
  <div class="finance-payment-manager">
    <!-- 页面头部 -->
    <div class="page-header">
      <h2 class="page-title">财务支付管理</h2>
      <div class="page-actions">
        <el-button type="primary" size="small" @click="createPayment">创建支付</el-button>
      </div>
    </div>

    <!-- 搜索筛选 -->
    <div class="search-section">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="支付单据编号">
          <el-input v-model="searchForm.paymentNo" placeholder="请输入支付单据编号" clearable></el-input>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="searchPayments" size="small">查询</el-button>
          <el-button @click="resetSearch" size="small">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 支付单据列表 -->
    <div class="payment-list">
      <el-table :data="paymentList" border stripe style="width: 100%" v-loading="loading">
        <el-table-column prop="paymentNo" label="支付单据编号" width="150"></el-table-column>
        <el-table-column prop="amount" label="支付金额" width="120" align="right">
          <template #default="{ row }">
            <span class="amount-value">¥ {{ row.amount }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="paymentStatus" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.paymentStatus)" size="small">
              {{ getStatusName(row.paymentStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="160"></el-table-column>
        <el-table-column label="操作" width="150" fixed="right" align="center">
          <template #default="{ row }">
            <el-button size="small" type="text" @click="viewPayment(row)">查看</el-button>
            <el-button size="small" type="primary" @click="approvePayment(row)">审核</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :total="total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange">
        </el-pagination>
      </div>
    </div>
  </div>
</template>

<script>
import { ref } from 'vue';
import { ElMessage } from 'element-plus';

export default {
  name: 'FinancePaymentManager',
  
  data() {
    return {
      searchForm: {
        paymentNo: ''
      },
      paymentList: [],
      currentPage: 1,
      pageSize: 20,
      total: 0,
      loading: false,
      paymentDetailVisible: false,
      paymentDetail: {},
      approveVisible: false,
      approveForm: {
        paymentId: null,
        approve: null,
        comment: ''
      }
    };
  },
  
  methods: {
    async searchPayments() {
      this.loading = true;
      try {
        // 模拟API调用，暂时返回模拟数据
        setTimeout(() => {
          this.loading = false;
          this.paymentList = this.getMockPaymentData();
          this.total = 50;
          ElMessage.success('查询成功');
        }, 1000);
      } catch (error) {
        ElMessage.error('查询失败: ' + error.message);
      }
    },
    
    resetSearch() {
      this.searchForm = {
        paymentNo: ''
      };
      this.searchPayments();
    },
    
    handleSizeChange(val) {
      this.pageSize = val;
      this.searchPayments();
    },
    
    handleCurrentChange(val) {
      this.currentPage = val;
      this.searchPayments();
    },
    
    viewPayment(payment) {
      this.paymentDetail = { ...payment };
      this.paymentDetailVisible = true;
    },
    
    async approvePayment(payment) {
      this.approveForm.paymentId = payment.id;
      this.approveVisible = true;
    },
    
    closePaymentDetail() {
      this.paymentDetailVisible = false;
      this.paymentDetail = {};
    },
    
    closeApprove() {
      this.approveVisible = false;
      this.approveForm = {
        paymentId: null,
        approve: null,
        comment: ''
      };
    },
    
    async submitApprove() {
      try {
        // 模拟API调用
        setTimeout(() => {
          ElMessage.success('支付审批成功');
          this.closeApprove();
          this.searchPayments();
        }, 500);
      } catch (error) {
        ElMessage.error('支付审批失败: ' + error.message);
      }
    },
    
    getMockPaymentData() {
      const mockData = [];
      for (let i = 0; i < 20; i++) {
        mockData.push({
          paymentId: i + 1,
          paymentNo: 'PAY202311' + String(i + 1).padStart(4, '0'),
          amount: (Math.random() * 10000 + 500).toFixed(2),
          businessType: ['费用报销', '借款申请', '付款申请', '其他'][Math.floor(Math.random() * 4)],
          paymentMethod: Math.floor(Math.random() * 8) + 1,
          paymentDate: new Date().toISOString().split('T')[0],
          paymentStatus: Math.floor(Math.random() * 3),
          payeeName: '测试收款人' + i,
          applyEmployeeName: '测试申请人' + i,
          createTime: new Date().toISOString().split('T')[0]
        });
      }
      return mockData;
    },
    
    getStatusType(status) {
      if (status === 1) return 'info';
      if (status === 2) return 'success';
      if (status === 0) return 'warning';
      return 'primary';
    },
    
    getStatusName(status) {
      if (status === 0) return '草稿';
      if (status === 1) return '待审批';
      if (status === 2) return '已审批';
      if (status === 3) return '已支付';
      if (status === 4) return '已取消';
      return '未知';
    }
  }
};
</script>

<style scoped lang="scss">
.finance-payment-manager {
  padding: 20px;
  background-color: #f5f7fa;
  
  .page-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 20px;
    padding: 20px 24px;
    background: #ffffff;
    border-radius: 8px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  }
  
  .page-title {
    font-size: 20px;
    font-weight: 600;
    color: #303133;
    margin: 0;
  }
  
  .page-actions {
    display: flex;
    gap: 10px;
  }
  
  .search-section {
    background: #ffffff;
    padding: 20px;
    border-radius: 8px;
    margin-bottom: 20px;
    box-shadow: 0 1px 6px rgba(0, 0, 0, 0.05);
  }
  
  .search-form {
    margin: 0;
  }
  
  .payment-list {
    background: #ffffff;
    padding: 20px;
    border-radius: 8px;
    box-shadow: 0 1px 6px rgba(0, 0, 0, 0.05);
    min-height: 400px;
  }
  
  .pagination-container {
    margin-top: 20px;
    display: flex;
    justify-content: center;
    padding: 20px;
    background: #f9fafb;
    border-radius: 8px;
  }
  
  .amount-value {
    font-weight: 600;
    color: #f56c6c;
    font-size: 16px;
  }
}
</style>