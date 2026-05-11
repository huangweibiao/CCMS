<template>
  <div class="finance-payment-manager">
    <!-- 页面头部 -->
    <div class="page-header">
      <h2 class="page-title">财务支付管理</h2>
      <div class="page-actions">
        <el-button type="primary" size="small" @click="createPayment">创建支付</el-button>
        <el-button type="success" size="small" @click="batchCreatePayments">批量创建</el-button>
      </div>
    </div>

    <!-- 搜索筛选 -->
    <div class="search-section">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="支付单据编号">
          <el-input v-model="searchForm.paymentNo" placeholder="请输入支付单据编号" clearable></el-input>
        </el-form-item>
        <el-form-item label="业务类型">
          <el-select v-model="searchForm.businessType" placeholder="请选择业务类型" clearable>
            <el-option label="费用报销" value="EXPENSE"></el-option>
            <el-option label="借款申请" value="LOAN"></el-option>
            <el-option label="付款申请" value="PAYMENT"></el-option>
            <el-option label="其他" value="OTHER"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="支付状态">
          <el-select v-model="searchForm.paymentStatus" placeholder="请选择支付状态" clearable>
            <el-option label="全部" :value="null"></el-option>
            <el-option label="草稿" :value="0"></el-option>
            <el-option label="待审批" :value="1"></el-option>
            <el-option label="已审批" :value="2"></el-option>
            <el-option label="已支付" :value="3"></el-option>
            <el-option label="已取消" :value="4"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="申请部门">
          <el-input v-model="searchForm.departmentId" placeholder="请输入部门ID" clearable></el-input>
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
        <el-table-column prop="businessType" label="业务类型" width="120">
          <template #default="{ row }">
            <span>{{ getBusinessTypeName(row.businessType) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="businessNo" label="业务单号" width="150"></el-table-column>
        <el-table-column prop="amount" label="支付金额" width="120" align="right">
          <template #default="{ row }">
            <span class="amount-value">¥{{ row.amount }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="paymentMethod" label="支付方式" width="100" align="center">
          <template #default="{ row }">
            <span>{{ getPaymentMethodName(row.paymentMethod) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="paymentDate" label="支付日期" width="120"></el-table-column>
        <el-table-column prop="paymentStatus" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.paymentStatus)" size="small">
              {{ getStatusName(row.paymentStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="payeeName" label="收款人" width="120"></el-table-column>
        <el-table-column prop="applyEmployeeName" label="申请人" width="120"></el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="160"></el-table-column>
        <el-table-column label="操作" width="200" fixed="right" align="center">
          <template #default="{ row }">
            <el-button size="small" type="text" @click="viewPayment(row)">查看</el-button>
            <el-button size="small" type="primary" @click="approvePayment(row)" :disabled="!canApprove(row.paymentStatus)">审核</el-button>
            <el-button size="small" type="success" @click="executePayment(row)" :disabled="!canExecute(row.paymentStatus)">执行</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          v-model="currentPage"
          v-model:page-size="pageSize"
          :total="total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange">
        </el-pagination>
      </div>
    </div>

    <!-- 支付详情对话框 -->
    <el-dialog v-model="paymentDetailVisible" title="支付详情" width="800px" @close="closePaymentDetail">
      <el-form :model="paymentDetail" label-width="100px">
        <el-form-item label="支付单据编号">
          <el-input v-model="paymentDetail.paymentNo" disabled></el-input>
        </el-form-item>
        <el-form-item label="业务类型">
          <el-input v-model="paymentDetail.businessType" disabled></el-input>
        </el-form-item>
        <el-form-item label="业务单号">
          <el-input v-model="paymentDetail.businessNo" disabled></el-input>
        </el-form-item>
        <el-form-item label="申请部门">
          <el-input v-model="paymentDetail.applyDepartmentName" disabled></el-input>
        </el-form-item>
        <el-form-item label="申请人工">
          <el-input v-model="paymentDetail.applyEmployeeName" disabled></el-input>
        </el-form-item>
        <el-form-item label="支付金额">
          <el-input v-model="paymentDetail.amount" disabled>
            <template #prefix>
              <span>¥</span>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item label="支付方式">
          <el-input v-model="paymentDetail.paymentMethod" disabled></el-input>
        </el-form-item>
        <el-form-item label="收款人">
          <el-input v-model="paymentDetail.payeeName" disabled></el-input>
        </el-form-item>
        <el-form-item label="支付日期">
          <el-date-picker v-model="paymentDetail.paymentDate" disabled></el-date-picker>
        </el-form-item>
        <el-form-item label="支付状态">
          <el-tag :type="getStatusType(paymentDetail.paymentStatus)" size="large">
            {{ getStatusName(paymentDetail.paymentStatus) }}
          </el-tag>
        </el-form-item>
        <el-form-item label="申请人">
          <el-input v-model="paymentDetail.applyEmployeeName" disabled></el-input>
        </el-form-item>
        <el-form-item label="申请时间">
          <el-input v-model="paymentDetail.createTime" disabled></el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="closePaymentDetail">关闭</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 支付审批对话框 -->
    <el-dialog v-model="approveVisible" title="支付审批" width="600px" @close="closeApprove">
      <el-form :model="approveForm" label-width="100px">
        <el-alert type="info" title="支付审批" :closable="false" description="请确认是否通过此支付申请，审核通过后财务将开始付款处理。">
        </el-alert>
        <el-form-item label="支付单据信息">
          <div class="payment-info">
            <div class="info-row">
              <span class="info-label">支付单据编号：</span>
              <span class="info-value">{{ approvePayment.paymentNo }}</span>
            </div>
            <div class="info-row">
              <span class="info-label">支付金额：</span>
              <span class="info-value amount-value">¥{{ approvePayment.amount }}</span>
            </div>
            <div class="info-row">
              <span class="info-label">收款人：</span>
              <span class="info-value">{{ approvePayment.payeeName }}</span>
            </div>
            <div class="info-row">
              <span class="info-label">申请部门：</span>
              <span class="info-value">{{ approvePayment.applyDepartmentName }}</span>
            </div>
          </div>
        </el-form-item>
        <el-form-item label="审批结果">
          <el-radio-group v-model="approveForm.approve">
            <el-radio :label="1" border>同意支付</el-radio>
            <el-radio :label="2" border>驳回支付</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="审批意见">
          <el-input v-model="approveForm.comment" type="textarea" :rows="4" placeholder="请输入审批意见"></el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="closeApprove">取消</el-button>
          <el-button type="primary" @click="submitApprove">确定</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 支付执行对话框 -->
    <el-dialog v-model="executeVisible" title="支付执行" width="600px" @close="closeExecute">
      <el-alert type="warning" title="支付执行确认" :closable="false" description="请确认执行此支付，执行后将从财务账户扣除相应金额。">
        </el-alert>
        <el-form-item label="支付信息">
          <div class="payment-info">
            <div class="info-row">
              <span class="info-label">支付单据编号：</span>
              <span class="info-value">{{ executePayment.paymentNo }}</span>
            </div>
            <div class="info-row">
              <span class="info-label">支付金额：</span>
              <span class="info-value amount-value">¥{{ executePayment.amount }}</span>
            </div>
            <div class="info-row">
              <span class="info-label">收款账户：</span>
              <span class="info-value">{{ executePayment.payeeAccount }}</span>
            </div>
            <div class="info-row">
              <span class="info-label">收款银行：</span>
              <span class="info-value">{{ executePayment.payeeBank }}</span>
            </div>
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="closeExecute">取消</el-button>
          <el-button type="warning" @click="confirmExecute">确定执行</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { ref } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { financePaymentApi } from '@/api/finance';

export default {
  name: 'FinancePaymentManager',
  
  data() {
    return {
      searchForm: {
        paymentNo: '',
        businessType: '',
        paymentStatus: null,
        departmentId: ''
      },
      paymentList: [],
      currentPage: 1,
      pageSize: 20,
      total: 0,
      loading: false,
      paymentDetailVisible: false,
      paymentDetail: {
        paymentId: null,
        paymentNo: '',
        businessType: '',
        businessNo: '',
        amount: '',
        paymentMethod: '',
        paymentDate: '',
        paymentStatus: 0,
        applyEmployeeName: '',
        createTime: ''
      },
      approvePayment: {},
      approveVisible: false,
      approveForm: {
        paymentId: null,
        approve: null,
        comment: ''
      },
      executePayment: {},
      executeVisible: false
    };
  },
  
  created() {
    this.searchPayments();
  },
  
  methods: {
    async searchPayments() {
      this.loading = true;
      try {
        const response = await financePaymentApi.getPaymentList(
                this.searchForm.businessType,
                this.searchForm.paymentNo,
                this.searchForm.paymentStatus,
                null, null, null, null,
                this.currentPage,
                this.pageSize
        );
        
        if (response.success) {
          this.paymentList = response.data.payments;
          this.total = response.data.total;
          ElMessage.success('查询成功');
        } else {
          ElMessage.error(response.message);
        }
      } catch (error) {
        ElMessage.error('查询失败: ' + error.message);
      } finally {
        this.loading = false;
      }
    },
    
    resetSearch() {
      this.searchForm = {
        paymentNo: '',
        businessType: '',
        paymentStatus: null,
        departmentId: ''
      };
      this.searchPayments();
    },
    
    async createPayment() {
      try {
        const response = await financePaymentApi.createPayment(
                'PAYMENT',
                null,
                null,
                10000.00,
                'BANK_TRANSFER'
        );
        
        if (response.success) {
          ElMessage.success('支付单创建成功');
          this.searchPayments();
        } else {
          ElMessage.error(response.message);
        }
      } catch (error) {
        ElMessage.error('支付单创建失败: ' + error.message);
      }
    },
    
    async batchCreatePayments() {
      ElMessageBox.confirm('批量创建支付单据', '确认批量创建10个测试支付单据？', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'info'
      }).then(async () => {
        try {
          const response = await financePaymentApi.batchApprovePayments([], 1, '');
          
          if (response.success) {
            ElMessage.success(`批量创建完成：成功${response.data.successCount}个`);
            this.searchPayments();
          } else {
            ElMessage.error(response.message);
          }
        } catch (error) {
          ElMessage.error('批量创建失败: ' + error.message);
        }
      });
    },
    
    viewPayment(payment) {
      this.paymentDetail = { ...payment };
      this.paymentDetailVisible = true;
    },
    
    canApprove(status) {
      return status === 1; // 只有待审批状态才能审核
    },
    
    canExecute(status) {
      return status === 2; // 只有已审批状态才能执行
    },
    
    async approvePayment(payment) {
      this.approvePayment = payment;
      this.approveVisible = true;
    },
    
    async executePayment(payment) {
      this.executePayment = payment;
      this.executeVisible = true;
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
        const response = await financePaymentApi.approvePayment(
                this.approveForm.paymentId,
                this.approveForm.approve,
                this.approveForm.comment
        );
        
        if (response.success) {
          ElMessage.success('支付审批成功');
          this.closeApprove();
          this.searchPayments();
        } else {
          ElMessage.error(response.message);
        }
      } catch (error) {
        ElMessage.error('支付审批失败: ' + error.message);
      }
    },
    
    closeExecute() {
      this.executeVisible = false;
    },
    
    async confirmExecute() {
      try {
        const response = await financePaymentApi.executePayment(this.executePayment.paymentId);
        
        if (response.success) {
          ElMessage.success('支付执行成功');
          this.closeExecute();
          this.searchPayments();
        } else {
          ElMessage.error(response.message);
        }
      } catch (error) {
        ElMessage.error('支付执行失败: ' + error.message);
      }
    },
    
    handleSizeChange(val) {
      this.pageSize = val;
      this.searchPayments();
    },
    
    handleCurrentChange(val) {
      this.currentPage = val;
      this.searchPayments();
    },
    
    getBusinessTypeName(type) {
      const typeMap = {
        'EXPENSE': '费用报销',
        'LOAN': '借款申请',
        'PAYMENT': '付款申请',
        'OTHER': '其他'
      };
      return typeMap[type] || '其他';
    },
    
    getPaymentMethodName(method) {
      const methodMap = {
        1: '转账',
        2: '支票',
        3: '现金',
        4: '电子支付',
        5: '银行卡',
        6: '网银',
        7: '第三方支付',
        8: '其他'
      };
      return methodMap[method] || '其他';
    },
    
    getStatusType(status) {
      if (status === 1) return 'info';
      if (status === 2) return 'success';
      if (status === 3) return 'warning';
      if (status === 4) return 'info';
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
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
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
  
  .dialog-footer {
    display: flex;
    justify-content: flex-end;
    gap: 10px;
    padding-top: 20px;
  }
  
  .amount-value {
    font-weight: 600;
    color: #f56c6c;
    font-size: 16px;
  }
  
  .payment-info {
    background: #f5f7fa;
    padding: 15px;
    border-radius: 8px;
    margin-bottom: 15px;
  }
  
  .info-row {
    display: flex;
    justify-content: space-between;
    padding: 8px 0;
    border-bottom: 1px solid #e0e0e0;
  }
  
  .info-row:last-child {
    border-bottom: none;
  }
  
  .info-label {
    font-size: 14px;
    color: #606266;
    font-weight: 500;
  }
  
  .info-value {
    font-size: 14px;
    color: #303133;
    font-weight: 600;
  }
}
</style>