<template>
  <div class="finance-voucher-manager">
    <!-- 页面头部 -->
    <div class="page-header">
      <h2 class="page-title">财务凭证管理</h2>
      <div class="page-actions">
        <el-button type="primary" size="small" @click="createVoucher">生成凭证</el-button>
        <el-button type="success" size="small" @click="batchApprove">批量审核</el-button>
        <el-button type="warning" size="small" @click="exportVouchers">导出凭证</el-button>
      </div>
    </div>

    <!-- 搜索筛选 -->
    <div class="search-section">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="凭证编号">
          <el-input v-model="searchForm.voucherNo" placeholder="请输入凭证编号" clearable></el-input>
        </el-form-item>
        <el-form-item label="业务类型">
          <el-select v-model="searchForm.businessType" placeholder="请选择业务类型" clearable>
            <el-option label="费用报销" value="EXPENSE"></el-option>
            <el-option label="借款申请" value="LOAN"></el-option>
            <el-option label="付款申请" value="PAYMENT"></el-option>
            <el-option label="其他" value="OTHER"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="凭证状态">
          <el-select v-model="searchForm.status" placeholder="请选择凭证状态" clearable>
            <el-option label="草稿" :value="0"></el-option>
            <el-option label="已生成" :value="1"></el-option>
            <el-option label="已审核" :value="2"></el-option>
            <el-option label="已记账" :value="3"></el-option>
            <el-option label="已驳回" :value="4"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="日期范围">
          <el-date-picker v-model="searchForm.startDate" type="date" placeholder="开始日期" clearable></el-date-picker>
          <el-date-picker v-model="searchForm.endDate" type="date" placeholder="结束日期" clearable></el-date-picker>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="searchVouchers" size="small">查询</el-button>
          <el-button @click="resetSearch" size="small">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 凭证列表 -->
    <div class="voucher-list">
      <el-table :data="voucherList" border stripe style="width: 100%" v-loading="loading">
        <el-table-column prop="voucherNo" label="凭证编号" width="150"></el-table-column>
        <el-table-column prop="businessType" label="业务类型" width="120">
          <template #default="{ row }">
            <span>{{ getBusinessTypeName(row.businessType) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="businessNo" label="业务单号" width="150"></el-table-column>
        <el-table-column prop="amount" label="金额" width="120" align="right">
          <template #default="{ row }">
            <span class="amount-value">¥ {{ row.amount }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="voucherStatus" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.voucherStatus)" size="small">
              {{ getStatusName(row.voucherStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="accountingDate" label="会计日期" width="120"></el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="160"></el-table-column>
        <el-table-column label="操作" width="180" fixed="right" align="center">
          <template #default="{ row }">
            <el-button size="small" type="text" @click="viewVoucher(row)">查看</el-button>
            <el-button size="small" type="primary" @click="approveVoucher(row)" :disabled="!canApprove(row.voucherStatus)">审核</el-button>
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

    <!-- 凭证详情对话框 -->
    <el-dialog v-model="voucherDetailVisible" title="凭证详情" width="800px" @close="closeVoucherDetail">
      <el-form :model="voucherDetail" label-width="100px">
        <el-form-item label="凭证编号">
          <el-input v-model="voucherDetail.voucherNo" disabled></el-input>
        </el-form-item>
        <el-form-item label="业务类型">
          <el-input v-model="voucherDetail.businessType" disabled></el-input>
        </el-form-item>
        <el-form-item label="业务单号">
          <el-input v-model="voucherDetail.businessNo" disabled></el-input>
        </el-form-item>
        <el-form-item label="金额">
          <el-input v-model="voucherDetail.amount" disabled>
            <template #prefix>
              <span>¥</span>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item label="借方科目">
          <el-input v-model="voucherDetail.debitAccount" disabled></el-input>
        </el-form-item>
        <el-form-item label="贷方科目">
          <el-input v-model="voucherDetail.creditAccount" disabled></el-input>
        </el-form-item>
        <el-form-item label="会计日期">
          <el-date-picker v-model="voucherDetail.accountingDate" disabled></el-date-picker>
        </el-form-item>
        <el-form-item label="凭证状态">
          <el-tag :type="getStatusType(voucherDetail.voucherStatus)" size="large">
            {{ getStatusName(voucherDetail.voucherStatus) }}
          </el-tag>
        </el-form-item>
        <el-form-item label="创建人">
          <el-input v-model="voucherDetail.createBy" disabled></el-input>
        </el-form-item>
        <el-form-item label="创建时间">
          <el-input v-model="voucherDetail.createTime" disabled></el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="closeVoucherDetail">关闭</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 凭证审核对话框 -->
    <el-dialog v-model="approveVisible" title="凭证审核" width="500px" @close="closeApprove">
      <el-form :model="approveForm" label-width="100px">
        <el-form-item label="审核结果">
          <el-radio-group v-model="approveForm.approve">
            <el-radio :label="1" border>同意</el-radio>
            <el-radio :label="2" border>驳回</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="审核意见">
          <el-input v-model="approveForm.comment" type="textarea" :rows="4" placeholder="请输入审核意见"></el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="closeApprove">取消</el-button>
          <el-button type="primary" @click="submitApprove">确定</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { ref } from 'vue';
import { ElMessage } from 'element-plus';
import { financeVoucherApi } from '@/api/finance';

export default {
  name: 'FinanceVoucherManager',
  
  data() {
    return {
      searchForm: {
        voucherNo: '',
        businessType: '',
        status: null,
        startDate: null,
        endDate: null
      },
      voucherList: [],
      currentPage: 1,
      pageSize: 20,
      total: 0,
      loading: false,
      voucherDetailVisible: false,
      voucherDetail: {
        voucherId: null,
        voucherNo: '',
        businessType: '',
        businessNo: '',
        amount: '',
        debitAccount: '',
        creditAccount: '',
        accountingDate: '',
        voucherStatus: 0,
        createBy: '',
        createTime: ''
      },
      approveVisible: false,
      approveForm: {
        voucherId: null,
        approve: null,
        comment: ''
      }
    };
  },
  
  created() {
    this.searchVouchers();
  },
  
  methods: {
    async searchVouchers() {
      this.loading = true;
      try {
        const response = await financeVoucherApi.getVoucherList(
          this.searchForm.businessType,
          this.searchForm.businessNo,
          this.searchForm.status,
          this.searchForm.startDate,
          this.searchForm.endDate,
          this.currentPage,
          this.pageSize
        );
        
        if (response.success) {
          this.voucherList = response.data.payments;
          this.total = response.data.total;
          ElMessage.success('查询成功');
        } else {
          ElMessage.error('查询失败: ' + response.message);
        }
      } catch (error) {
        ElMessage.error('查询失败: ' + error.message);
      } finally {
        this.loading = false;
      }
    },
    
    resetSearch() {
      this.searchForm = {
        voucherNo: '',
        businessType: '',
        status: null,
        startDate: null,
        endDate: null
      };
      this.searchVouchers();
    },
    
    async createVoucher() {
      try {
        const response = await financeVoucherApi.generateVoucher(
          'EXPENSE',
          null,
          null,
          null
        );
        
        if (response.success) {
          ElMessage.success('凭证生成成功');
          this.searchVouchers();
        } else {
          ElMessage.error('凭证生成失败: ' + response.message);
        }
      } catch (error) {
        ElMessage.error('凭证生成失败: ' + error.message);
      }
    },
    
    async batchApprove() {
      try {
        const voucherIds = this.voucherList
          .filter(v => v.voucherStatus === 1)
          .map(v => v.voucherId);
        
        if (voucherIds.length === 0) {
          ElMessage.warning('请先选择待审核的凭证');
          return;
        }
        
        const response = await financeVoucherApi.batchApprove(voucherIds, 1, '');
        
        if (response.success) {
          ElMessage.success(`批量审核完成：成功${response.data.successCount}个，失败${response.data.failCount}个`);
          this.searchVouchers();
        } else {
          ElMessage.error('批量审核失败: ' + response.message);
        }
      } catch (error) {
        ElMessage.error('批量审核失败: ' + error.message);
      }
    },
    
    async exportVouchers() {
      try {
        const voucherIds = this.voucherList.map(v => v.voucherId);
        
        if (voucherIds.length === 0) {
          ElMessage.warning('请先选择要导出的凭证');
          return;
        }
        
        const response = await financeVoucherApi.exportVouchers({
          voucherIds: voucherIds,
          exportType: 'EXCEL'
        });
        
        if (response.success) {
          ElMessage.success('凭证导出成功');
        } else {
          ElMessage.error('凭证导出失败: ' + response.message);
        }
      } catch (error) {
        ElMessage.error('凭证导出失败: ' + error.message);
      }
    },
    
    viewVoucher(voucher) {
      this.voucherDetail = { ...voucher };
      this.voucherDetailVisible = true;
    },
    
    canApprove(status) {
      return status === 1; // 只有待审核状态才能审核
    },
    
    async approveVoucher(voucher) {
      this.approveForm.voucherId = voucher.voucherId;
      this.approveVisible = true;
    },
    
    closeVoucherDetail() {
      this.voucherDetailVisible = false;
      this.voucherDetail = {};
    },
    
    async submitApprove() {
      try {
        const response = await financeVoucherApi.approveVoucher(
          this.approveForm.voucherId,
          this.approveForm.approve,
          this.approveForm.comment
        );
        
        if (response.success) {
          ElMessage.success('凭证审核成功');
          this.closeApprove();
          this.searchVouchers();
        } else {
          ElMessage.error('凭证审核失败: ' + response.message);
        }
      } catch (error) {
        ElMessage.error('凭证审核失败: ' + error.message);
      }
    },
    
    closeApprove() {
      this.approveVisible = false;
      this.approveForm = {
        voucherId: null,
        approve: null,
        comment: ''
      };
    },
    
    handleSizeChange(val) {
      this.pageSize = val;
      this.searchVouchers();
    },
    
    handleCurrentChange(val) {
      this.currentPage = val;
      this.searchVouchers();
    },
    
    getBusinessTypeName(type) {
      const typeMap = {
        'EXPENSE': '费用报销',
        'LOAN': '借款申请',
        'PAYMENT': '付款申请',
        'OTHER': '其他'
      };
      return typeMap[type] || type;
    },
    
    getStatusType(status) {
      if (status === 3) return 'success';
      if (status === 4) return 'info';
      if (status === 0) return 'warning';
      return 'primary';
    },
    
    getStatusName(status) {
      const statusMap = {
        0: '草稿',
        1: '已生成',
        2: '已审核',
        3: '已记账',
        4: '已驳回'
      };
      return statusMap[status] || '未知';
    }
  }
};
</script>

<style scoped lang="scss">
.finance-voucher-manager {
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
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  }
  
  .search-form {
    margin: 0;
  }
  
  .voucher-list {
    background: #ffffff;
    padding: 20px;
    border-radius: 8px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
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
  }
  
  .dialog-footer {
    display: flex;
    justify-content: flex-end;
    gap: 10px;
    padding-top: 20px;
  }
}
</style>