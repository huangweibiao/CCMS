<template>
  <div class="finance-report-center">
    <!-- 页面头部 -->
    <div class="page-header">
      <h2 class="page-title">财务报表中心</h2>
      <div class="page-actions">
        <el-button type="primary" size="small" @click="createReport">生成报表</el-button>
        <el-button type="success" size="small" @click="showBatchExport">批量导出</el-button>
      </div>
    </div>

    <!-- 报表类型选择 -->
    <div class="report-types">
      <div class="report-title">报表类型</div>
      <div class="type-cards">
        <div class="type-card" :class="{ active: selectedReportType === 'expense' }" @click="selectReportType('expense')">
          <div class="type-icon">
            <i class="el-icon-document"></i>
          </div>
          <div class="type-info">
            <div class="type-name">费用报表</div>
            <div class="type-desc">日常费用、月度费用统计与分析</div>
          </div>
        </div>
        <div class="type-card" :class="{ active: selectedReportType === 'payment' }" @click="selectReportType('payment')">
          <div class="type-icon">
            <i class="el-icon-wallet"></i>
          </div>
          <div class="type-info">
            <div class="type-name">支付报表</div>
            <div class="type-desc">支付方式、支付时间、收款人统计</div>
          </div>
        </div>
        <div class="type-card" :class="{ active: selectedReportType === 'department' }" @click="selectReportType('department')">
          <div class="type-icon">
            <i class="el-icon-office-building"></i>
          </div>
          <div class="type-info">
            <div class="type-name">部门报表</div>
            <div class="type-desc">部门费用统计、预算执行情况</div>
          </div>
        </div>
        <div class="type-card" :class="{ active: selectedReportType === 'account' }" @click="selectReportType('account')">
          <div class="type-icon">
            <i class="el-icon-bank-card"></i>
          </div>
          <div class="type-info">
            <div class="type-name">科目报表</div>
            <div class="type-desc">科目余额、发生额、借贷平衡分析</div>
          </div>
        </div>
        <div class="type-card" :class="{ active: selectedReportType === 'cashflow' }" @click="selectReportType('cashflow')">
          <div class="type-icon">
            <i class="el-icon-tickets"></i>
          </div>
          <div class="type-info">
            <div class="type-name">现金流报表</div>
            <div class="type-desc">现金流入、流出、净流量分析</div>
          </div>
        </div>
        <div class="type-card" :class="{ active: selectedReportType === 'profit' }" @click="selectReportType('profit')">
          <div class="type-icon">
            <i class="el-icon-trend-charts"></i>
          </div>
          <div class="type-info">
            <div class="type-name">利润报表</div>
            <div class="type-desc">收入支出、利润计算、利润率分析</div>
          </div>
        </div>
      </div>
    </div>

    <!-- 报表参数设置 -->
    <div class="report-params">
      <div class="report-title">报表参数</div>
      <el-form :inline="true" :model="reportForm" class="param-form">
        <el-form-item label="报表周期">
          <el-radio-group v-model="reportForm.period">
            <el-radio-button label="日报表" :value="1"></el-radio-button>
            <el-radio-button label="周报表" :value="2"></el-radio-button>
            <el-radio-button label="月报表" :value="3"></el-radio-button>
            <el-radio-button label="季度报表" :value="4"></el-radio-button>
            <el-radio-button label="年度报表" :value="5"></el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="日期范围">
          <el-date-picker v-model="reportForm.startDate" type="date" placeholder="开始日期" clearable></el-date-picker>
          <el-date-picker v-model="reportForm.endDate" type="date" placeholder="结束日期" clearable></el-date-picker>
        </el-form-item>
        <el-form-item label="统计部门">
          <el-select v-model="reportForm.departmentId" placeholder="请选择部门" clearable>
            <el-option label="全部部门" :value="0"></el-option>
            <el-option v-for="dept in departments" :key="dept.id" :label="dept.name" :value="dept.id"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="generateReport" :loading="generating">生成报表</el-button>
          <el-button @click="resetParams">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 已生成的报表列表 -->
    <div class="report-list">
      <div class="report-title">已生成的报表</div>
      <el-table :data="reportList" border stripe style="width: 100%" v-loading="loading">
        <el-table-column prop="reportName" label="报表名称" width="200"></el-table-column>
        <el-table-column prop="reportType" label="报表类型" width="120">
          <template #default="{ row }">
            <span>{{ getReportTypeName(row.reportType) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="reportPeriod" label="报表周期" width="100">
          <template #default="{ row }">
            <span>{{ getPeriodName(row.reportPeriod) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="startDate" label="开始日期" width="120"></el-table-column>
        <el-table-column prop="endDate" label="结束日期" width="120"></el-table-column>
        <el-table-column prop="approvalStatus" label="审核状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.approvalStatus)" size="small">
              {{ getStatusName(row.approvalStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="generationStatus" label="生成状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getGenerationStatusType(row.generationStatus)" size="small">
              {{ getGenerationStatusName(row.generationStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="160"></el-table-column>
        <el-table-column prop="downloadCount" label="下载次数" width="100" align="center"></el-table-column>
        <el-table-column label="操作" width="200" fixed="right" align="center">
          <template #default="{ row }">
            <el-button size="small" type="text" @click="viewReport(row)">查看</el-button>
            <el-button size="small" type="primary" @click="approveReport(row)" :disabled="!canApprove(row.approvalStatus)">审核</el-button>
            <el-button size="small" type="success" @click="downloadReport(row)">下载</el-button>
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

    <!-- 报表详情对话框 -->
    <el-dialog v-model="reportDetailVisible" title="报表详情" width="900px" @close="closeReportDetail">
      <div class="report-detail-content">
        <div class="detail-header">
          <div class="detail-title">{{ currentReport.reportName }}</div>
          <div class="detail-info">
            <span class="info-label">报表类型：</span>
            <span class="info-value">{{ getReportTypeName(currentReport.reportType) }}</span>
            <span class="info-label">报表周期：</span>
            <span class="info-value">{{ getPeriodName(currentReport.reportPeriod) }}</span>
            <span class="info-label">日期范围：</span>
            <span class="info-value">{{ currentReport.startDate }} 至 {{ currentReport.endDate }}</span>
          </div>
        </div>
        
        <div class="detail-charts">
          <div class="chart-container">
            <div class="chart-title">费用趋势分析</div>
            <div ref="expenseTrendChart" style="width: 100%; height: 300px;"></div>
          </div>
          <div class="chart-container">
            <div class="chart-title">费用类别分布</div>
            <div ref="expenseCategoryChart" style="width: 100%; height: 300px;"></div>
          </div>
        </div>
      </div>
      </div>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="closeReportDetail">关闭</el-button>
          <el-button type="primary" @click="exportCurrentReport">导出报表</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 批量导出对话框 -->
    <el-dialog v-model="batchExportVisible" title="批量导出报表" width="600px" @close="closeBatchExport">
      <el-form :model="batchExportForm" label-width="120px">
        <el-form-item label="导出格式">
          <el-radio-group v-model="batchExportForm.exportType">
            <el-radio label="Excel" value="EXCEL"></el-radio>
            <el-radio label="PDF" value="PDF"></el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="导出类型">
          <el-checkbox-group v-model="batchExportForm.exportTypes">
            <el-checkbox label="全部报表" value="ALL"></el-checkbox>
            <el-checkbox label="费用报表" value="EXPENSE"></el-checkbox>
            <el-checkbox label="支付报表" value="PAYMENT"></el-checkbox>
            <el-checkbox label="部门报表" value="DEPARTMENT"></el-checkbox>
            <el-checkbox label="科目报表" value="ACCOUNT"></el-checkbox>
            <el-checkbox label="现金流报表" value="CASH_FLOW"></el-checkbox>
            <el-checkbox label="利润报表" value="PROFIT"></el-checkbox>
          </el-checkbox-group>
        </el-form-item>
        <el-form-item label="日期范围">
          <el-date-picker v-model="batchExportForm.startDate" type="date" placeholder="开始日期"></el-date-picker>
          <el-date-picker v-model="batchExportForm.endDate" type="date" placeholder="结束日期"></el-date-picker>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="closeBatchExport">取消</el-button>
          <el-button type="primary" @click="confirmBatchExport" :loading="exporting">确认导出</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { ref } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import * as echarts from 'echarts';
import { financeReportApi } from '@/api/finance';

export default {
  name: 'FinanceReportCenter',
  
  data() {
    return {
      selectedReportType: 'expense',
      reportForm: {
        period: 3,
        startDate: null,
        endDate: null,
        departmentId: null
      },
      reportList: [],
      currentPage: 1,
      pageSize: 20,
      total: 0,
      loading: false,
      generating: false,
      reportDetailVisible: false,
      currentReport: {},
      batchExportVisible: false,
      batchExportForm: {
        exportType: 'EXCEL',
        exportTypes: [],
        startDate: null,
        endDate: null
      },
      exporting: false,
      departments: [
        { id: 1, name: '财务部' },
        { id: 2, name: '市场部' },
        { id: 3, name: '技术部' },
        { id: 4, name: '行政部' }
      ]
    };
  },
  
  created() {
    this.loadReports();
    this.initCharts();
  },
  
  methods: {
    selectReportType(type) {
      this.selectedReportType = type;
    },
    
    async loadReports() {
      this.loading = true;
      try {
        const response = await financeReportApi.getReportList(
                null, null, null, null, null, 1, 20
        );
        
        if (response.success) {
          this.reportList = response.data.reports;
          this.total = response.data.total;
          ElMessage.success('报表列表加载成功');
        } else {
          ElMessage.error('报表列表加载失败: ' + response.message);
        }
      } catch (error) {
        ElMessage.error('报表列表加载失败: ' + error.message);
      } finally {
        this.loading = false;
      }
    },
    
    resetParams() {
      this.reportForm = {
        period: 3,
        startDate: null,
        endDate: null,
        departmentId: null
      };
    },
    
    async generateReport() {
      this.generating = true;
      try {
        let response;
        
        switch(this.selectedReportType) {
          case 'expense':
            response = await financeReportApi.generateExpenseReport(
                    this.reportForm.startDate, this.reportForm.endDate, this.reportForm.departmentId);
            break;
          case 'payment':
            response = await financeReportApi.generatePaymentReport(
                    this.reportForm.startDate, this.reportForm.endDate);
            break;
          case 'department':
            response = await financeReportApi.generateDepartmentExpenseReport(
                    this.reportForm.startDate, this.reportForm.endDate, this.reportForm.departmentId);
            break;
          case 'account':
            response = await financeReportApi.generateAccountReport(new Date());
            break;
          case 'cashflow':
            response = await financeReportApi.generateCashFlowReport(
                    this.reportForm.startDate, this.reportForm.endDate);
            break;
          case 'profit':
            response = await financeReportApi.generateProfitReport(
                    this.reportForm.startDate, this.reportForm.endDate);
            break;
          default:
            ElMessage.warning('请先选择报表类型');
            return;
        }
        
        if (response.success) {
          ElMessage.success('报表生成成功');
          this.loadReports();
          // 生成报表后显示详情
          if (response.data.reportId) {
            this.viewReport(response.data);
          }
        } else {
          ElMessage.error('报表生成失败: ' + response.message);
        }
      } catch (error) {
        ElMessage.error('报表生成失败: ' + error.message);
      } finally {
        this.generating = false;
      }
    },
    
    viewReport(report) {
      this.currentReport = { ...report };
      this.reportDetailVisible = true;
      
      // 初始化图表数据
      this.$nextTick(() => {
        this.initReportCharts(report);
      });
    },
    
    canApprove(status) {
      return status === 0; // 只有待审核才能审核
    },
    
    async approveReport(report) {
      try {
        const response = await financeReportApi.approveReport(report.reportId, 1, '审核通过');
        
        if (response.success) {
          ElMessage.success('报表审核成功');
          this.loadReports();
          this.reportDetailVisible = false;
        } else {
          ElMessage.error('报表审核失败: ' + response.message);
        }
      } catch (error) {
        ElMessage.error('报表审核失败: ' + error.message);
      }
    },
    
    async downloadReport(report) {
      try {
        const response = await financeReportApi.exportReport(report.reportId, 'EXCEL');
        
        if (response.success) {
          ElMessage.success('报表下载成功');
          window.open(response.data.reportFilePath);
          
          // 更新下载次数
          report.downloadCount++;
          await financeReportApi.updateReportDownloadCount(report.reportId);
        } else {
          ElMessage.error('报表下载失败: ' + response.message);
        }
      } catch (error) {
        ElMessage.error('报表下载失败: ' + error.message);
      }
    },
    
    exportCurrentReport() {
      this.downloadReport(this.currentReport);
    },
    
    closeReportDetail() {
      this.reportDetailVisible = false;
      this.currentReport = {};
      this.$nextTick(() => {
        this.initCharts();
      });
    },
    
    showBatchExport() {
      this.batchExportVisible = true;
    },
    
    closeBatchExport() {
      this.batchExportVisible = false;
    },
    
    async confirmBatchExport() {
      this.exporting = true;
      try {
        const response = await financeReportApi.batchExportReports({
          exportType: this.batchExportForm.exportType,
          exportTypes: this.batchExportForm.exportTypes,
          startDate: this.batchExportForm.startDate,
          endDate: this.batchExportForm.endDate
        });
        
        if (response.success) {
          ElMessage.success('批量导出完成');
          this.closeBatchExport();
        } else {
          ElMessage.error('批量导出失败: ' + response.message);
        }
      } catch (error) {
        ElMessage.error('批量导出失败: ' + error.message);
      } finally {
        this.exporting = false;
      }
    },
    
    handleSizeChange(val) {
      this.pageSize = val;
      this.loadReports();
    },
    
    handleCurrentChange(val) {
      this.currentPage = val;
      this.loadReports();
    },
    
    getReportTypeName(type) {
      const typeMap = {
        1: '费用报表',
        2: '支付报表',
        3: '部门报表',
        4: '科目报表',
        5: '现金流报表',
        6: '利润报表',
        7: '资产负债报表'
      };
      return typeMap[type] || '未知报表';
    },
    
    getPeriodName(period) {
      const periodMap = {
        1: '日报表',
        2: '周报表',
        3: '月报表',
        4: '季度报表',
        5: '年度报表'
      };
      return periodMap[period] || '未知周期';
    },
    
    getStatusType(status) {
      if (status === 1) return 'success';
      if (status === 2) return 'info';
      return 'default';
    },
    
    getStatusName(status) {
      if (status === 0) return '待审核';
      if (status === 1) return '已审核';
      if (status === 2) return '已拒绝';
      return '未知';
    },
    
    getGenerationStatusType(status) {
      if (status === 1) return 'success';
      if (status === 2) return 'danger';
      return 'info';
    },
    
    getGenerationStatusName(status) {
      if (status === 0) return '待生成';
      if (status === 1) return '已生成';
      if (status === 2) return '生成失败';
      return '未知';
    },
    
    initCharts() {
      // 费用趋势图表
      if (this.$refs.expenseTrendChart) {
        const expenseChart = echarts.init(this.$refs.expenseTrendChart);
        const expenseOption = {
          title: { text: '费用趋势分析' },
          tooltip: { trigger: 'axis' },
          legend: { data: ['本期费用', '上期费用'] },
          xAxis: { type: 'category', data: ['一月', '二月', '三月', '四月', '五月', '六月'] },
          yAxis: { type: 'value' },
          series: [
            {
              name: '本期费用',
              type: 'line',
              data: [10000, 15000, 12000, 18000, 16000],
              smooth: true,
              itemStyle: { color: '#409eff' }
            },
            {
              name: '上期费用',
              type: 'line',
              data: [8000, 12000, 10000, 14000, 13000],
              smooth: true,
              itemStyle: { color: '#91cc75' }
            }
          ]
        };
        expenseChart.setOption(expenseOption);
      }
      
      // 费用类别饼图
      if (this.$refs.expenseCategoryChart) {
        const categoryChart = echarts.init(this.$refs.expenseCategoryChart);
        const categoryOption = {
          title: { text: '费用类别分布' },
          tooltip: { trigger: 'item', formatter: '{a} <br/>{b}: ({d}%)' },
          legend: { orient: 'vertical', left: 'left' },
          series: [
            {
              name: '费用类别',
              type: 'pie',
              radius: ['40%', '70%'],
              data: [
                { value: 35, name: '差旅费' },
                { value: 25, name: '办公费' },
                { value: 20, name: '采购费' },
                { value: 15, name: '其他费用' },
                { value: 5, name: '人力费用' }
              ],
              emphasis: {
                itemStyle: {
                  borderRadius: 10,
                  borderColor: '#fff',
                  borderWidth: 2
                }
              }
            }
          ]
        };
        categoryChart.setOption(categoryOption);
      }
    },
    
    initReportCharts(report) {
      // TODO: 根据报表数据初始化图表
      if (this.$refs.expenseTrendChart) {
        const expenseChart = echarts.init(this.$refs.expenseTrendChart);
        // 根据报表数据更新图表配置
        const expenseOption = {
          title: { text: report.reportName + ' - 趋势分析' },
          tooltip: { trigger: 'axis' },
          xAxis: { type: 'category' },
          yAxis: { type: 'value' },
          series: [
            {
              name: '费用金额',
              type: 'bar',
              data: [10000, 15000, 12000, 18000, 16000, 14000]
            }
          ]
        };
        expenseChart.setOption(expenseOption);
      }
      
      if (this.$refs.expenseCategoryChart) {
        const categoryChart = echarts.init(this.$refs.expenseCategoryChart);
        // 根据报表数据更新饼图配置
        const categoryOption = {
          title: { text: report.reportName + ' - 类别分布' },
          series: [
            {
              type: 'pie',
              data: [
                { value: 35, name: '差旅费' },
                { value: 25, name: '办公费' },
                { value: 20, name: '采购费' }
              ]
            }
          ]
        };
        categoryChart.setOption(categoryOption);
      }
    }
  }
};
</script>

<style scoped lang="scss">
.finance-report-center {
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
  
  .report-types {
    margin-bottom: 20px;
  }
  
  .report-title {
    font-size: 16px;
    font-weight: 600;
    color: #606266;
    margin-bottom: 15px;
    padding-left: 10px;
    border-left: 4px solid #409eff;
  }
  
  .type-cards {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
    gap: 15px;
  }
  
  .type-card {
    background: #ffffff;
    border-radius: 8px;
    padding: 20px;
    cursor: pointer;
    transition: all 0.3s ease;
    border: 2px solid #e0e0e0;
    
    &:hover {
      transform: translateY(-4px);
      box-shadow: 0 6px 16px rgba(0, 0, 0, 0.15);
      border-color: #409eff;
    }
    
    &.active {
      background: linear-gradient(135deg, #409eff 0%, #36a3f 100%);
      border-color: #36a3f;
    }
  }
  
  .type-icon {
    width: 60px;
    height: 60px;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    margin: 0 auto 15px;
    background: linear-gradient(135deg, #409eff 0%, #36a3f 100%);
    color: #ffffff;
  }
  
  .type-info {
    flex: 1;
  }
  
  .type-name {
    font-size: 16px;
    font-weight: 600;
    color: #303133;
    margin-bottom: 5px;
  }
  
  .type-desc {
    font-size: 14px;
    color: #909399;
    line-height: 1.4;
  }
  
  .report-params {
    background: #ffffff;
    padding: 20px;
    border-radius: 8px;
    margin-bottom: 20px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  }
  
  .param-form {
    margin: 0;
  }
  
  .report-list {
    background: #ffffff;
    padding: 20px;
    border-radius: 8px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
    min-height: 600px;
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
  
  .detail-content {
    padding: 20px;
  }
  
  .detail-header {
    border-bottom: 1px solid #e0e0e0;
    padding-bottom: 15px;
    margin-bottom: 20px;
  }
  
  .detail-title {
    font-size: 18px;
    font-weight: 600;
    color: #303133;
    margin-bottom: 10px;
  }
  
  .detail-info {
    display: flex;
    flex-wrap: wrap;
    gap: 15px;
  }
  
  .info-label {
    font-size: 14px;
    color: #909399;
    font-weight: 500;
  }
  
  .info-value {
    font-size: 14px;
    color: #303133;
    font-weight: 600;
  }
  
  .detail-charts {
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: 20px;
    margin-top: 20px;
  }
  
  .chart-container {
    background: #f9fafb;
    padding: 20px;
    border-radius: 8px;
    border: 1px solid #e0e0e0;
  }
  
  .chart-title {
    font-size: 16px;
    font-weight: 600;
    color: #606266;
    margin-bottom: 15px;
    padding-left: 10px;
    border-left: 4px solid #409eff;
  }
}
</style>