import request from '@/utils/request';

// 财务报表相关的API
interface ReportQueryParams {
  page?: number;
  size?: number;
  reportType?: string;
  startDate?: string;
  endDate?: string;
  departmentId?: number;
  status?: string;
}

export interface ReportData {
  reportId: number;
  reportName: string;
  reportType: number;
  reportPeriod: number;
  startDate: string;
  endDate: string;
  status: number;
  totalAmount: number;
  departmentId?: number;
  departmentName?: string;
  downloadCount: number;
  createdAt: string;
  chartData: any;
}

export interface GenerateReportParams {
  reportType: string;
  period: number;
  startDate?: string;
  endDate?: string;
  departmentId?: number;
}

// 财务报表API
export const financeReportApi = {
  // 获取报表列表
  getReportList(queryParams: ReportQueryParams) {
    return request.get('/api/finance/report/list', { params: queryParams });
  },

  // 生成费用报表
  generateExpenseReport(params: GenerateReportParams) {
    return request.post('/api/finance/report/expense', params);
  },

  // 生成支付报表
  generatePaymentReport(params: GenerateReportParams) {
    return request.post('/api/finance/report/payment', params);
  },

  // 生成部门费用报表
  generateDepartmentExpenseReport(params: GenerateReportParams) {
    return request.post('/api/finance/report/department', params);
  },

  // 生成科目报表
  generateAccountReport(date?: Date) {
    const params = { date: date ? date.toISOString().split('T')[0] : undefined };
    return request.post('/api/finance/report/account', params);
  },

  // 生成现金流报表
  generateCashFlowReport(params: GenerateReportParams) {
    return request.post('/api/finance/report/cash-flow', params);
  },

  // 生成利润报表
  generateProfitReport(params: GenerateReportParams) {
    return request.post('/api/finance/report/profit', params);
  },

  // 审核报表
  approveReport(reportId: number, status: number, reason: string) {
    return request.post(`/api/finance/report/${reportId}/approve`, { status, reason });
  },

  // 导出单个报表
  exportReport(reportId: number, format: string) {
    return request.get(`/api/finance/report/${reportId}/export`, { 
      params: { format },
      responseType: 'blob'
    });
  },

  // 更新报表下载次数
  updateReportDownloadCount(reportId: number) {
    return request.post(`/api/finance/report/${reportId}/download-count`);
  },

  // 批量导出报表
  batchExportReports(params: {
    exportType: string;
    exportTypes: string[];
    startDate: string;
    endDate: string;
  }) {
    return request.post('/api/finance/report/batch-export', params, {
      responseType: 'blob'
    });
  }
};

// 财务凭证相关的API
export const financeVoucherApi = {
  // 获取凭证列表
  getVoucherList(queryParams: any) {
    return request.get('/api/finance/voucher/list', { params: queryParams });
  },

  // 创建凭证
  createVoucher(voucherData: any) {
    return request.post('/api/finance/voucher/create', voucherData);
  },

  // 更新凭证
  updateVoucher(voucherId: number, voucherData: any) {
    return request.put(`/api/finance/voucher/${voucherId}`, voucherData);
  },

  // 删除凭证
  deleteVoucher(voucherId: number) {
    return request.delete(`/api/finance/voucher/${voucherId}`);
  },

  // 审核凭证
  approveVoucher(voucherId: number, status: number, reason: string) {
    return request.post(`/api/finance/voucher/${voucherId}/approve`, { status, reason });
  }
};

// 财务支付相关的API
export const financePaymentApi = {
  // 获取支付列表
  getPaymentList(queryParams: any) {
    return request.get('/api/finance/payment/list', { params: queryParams });
  },

  // 创建支付
  createPayment(paymentData: any) {
    return request.post('/api/finance/payment/create', paymentData);
  },

  // 更新支付
  updatePayment(paymentId: number, paymentData: any) {
    return request.put(`/api/finance/payment/${paymentId}`, paymentData);
  },

  // 删除支付
  deletePayment(paymentId: number) {
    return request.delete(`/api/finance/payment/${paymentId}`);
  },

  // 执行支付
  executePayment(paymentId: number) {
    return request.post(`/api/finance/payment/${paymentId}/execute`);
  }
};

export default {
  financeReportApi,
  financeVoucherApi,
  financePaymentApi
};