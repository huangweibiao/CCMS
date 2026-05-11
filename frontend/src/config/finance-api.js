// 财务API配置
export const financeApi = {
  // 财务凭证API
  voucherApi: '/api/finance/voucher',
  
  // 财务支付API
  paymentApi: '/api/finance/payment',
  
  // 财务报表API
  reportApi: '/api/finance/report'
};

// 默认分页配置
export const paginationConfig = {
  defaultPageSize: 20,
  pageSizes: [10, 20, 50, 100],
  layout: 'total, sizes, prev, pager, next, jumper'
};

// 业务类型配置
export const businessTypes = {
  EXPENSE: { code: 'EXPENSE', name: '费用报销', description: '员工费用报销' },
  LOAN: { code: 'LOAN', name: '借款申请', description: '员工借款申请' },
  PAYMENT: { code: 'PAYMENT', name: '付款申请', description: '对外付款申请' },
  OTHER: { code: 'OTHER', name: '其他业务', description: '其他业务类型' }
};

// 支付方式配置
export const paymentMethods = {
  BANK_TRANSFER: { code: 1, name: '银行转账', description: '银行账户转账' },
  CHECK: { code: 2, name: '支票支付', description: '现金支票' },
  CASH: { code: 3, name: '现金支付', description: '现金支付' },
  ELECTRONIC: { code: 4, name: '电子支付', description: '电子支付' },
  CREDIT_CARD: { code: 5, name: '银行卡', description: '银行卡支付' },
  ONLINE_BANK: { code: 6, name: '网银', description: '网银支付' },
  THIRD_PARTY: { code: 7, name: '第三方支付', description: '第三方支付' },
  OTHER: { code: 8, name: '其他方式', description: '其他支付方式' }
};

// 凭证状态配置
export const voucherStatus = {
  DRAFT: { code: 0, name: '草稿', description: '凭证草稿状态' },
  GENERATED: { code: 1, name: '已生成', description: '凭证已生成' },
  APPROVED: { code: 2, name: '已审核', description: '凭证已审核' },
  POSTED: { code: 3, name: '已记账', description: '凭证已记账' },
  REJECTED: { code: 4, name: '已驳回', description: '凭证审核驳回' }
};

// 支付状态配置
export const paymentStatus = {
  DRAFT: { code: 0, name: '草稿', description: '支付单草稿' },
  PENDING: { code: 1, name: '待审批', description: '支付单待审批' },
  APPROVED: { code: 2, name: '已审批', description: '支付单已审批' },
  EXECUTED: { code: 3, name: '已支付', description: '支付单已支付' },
  CANCELLED: { code: 4, name: '已取消', description: '支付单已取消' },
  EXECUTION_FAILED: { code: 5, name: '执行失败', description: '支付单执行失败' }
};

// 报表类型配置
export const reportTypes = {
  EXPENSE: { code: 1, name: '费用报表', description: '费用统计报表' },
  PAYMENT: { code: 2, name: '支付报表', description: '支付统计报表' },
  DEPARTMENT: { code: 3, name: '部门报表', description: '部门费用分析报表' },
  ACCOUNT: { code: 4, name: '科目报表', description: '科目余额分析报表' },
  CASH_FLOW: { code: 5, name: '现金流报表', description: '现金流分析报表' },
  PROFIT: { code: 6, name: '利润报表', description: '利润分析报表' },
  BALANCE_SHEET: { code: 7, name: '资产负债表', description: '资产负债分析报表' }
};

// 报表周期配置
export const reportPeriods = {
  DAILY: { code: 1, name: '日报表', description: '每日报表' },
  WEEKLY: { code: 2, name: '周报表', description: '每周报表' },
  MONTHLY: { code: 3, name: '月报表', description: '每月报表' },
  QUARTERLY: { code: 4, name: '季度报表', description: '每季度报表' },
  YEARLY: { code: 5, name: '年报表', description: '每年报表' }
};

// 表单验证配置
export const validationRules = {
  voucherNo: { required: true, pattern: /^[A-Z]{2,}-\d{4}-\d{4}$/, message: '请输入正确的凭证编号' },
  businessNo: { required: true, pattern: /^[A-Z0-9]{2,}-\d{4}-\d{4}$/, message: '请输入正确的业务单号' },
  amount: { required: true, min: 0, type: 'number', message: '请输入有效的金额' },
  paymentMethod: { required: true, message: '请选择支付方式' },
  reportPeriod: { required: true, message: '请选择报表周期' },
  paymentDate: { required: true, type: 'date', message: '请选择支付日期' }
};

// 导出配置
export const exportFormats = {
  EXCEL: { code: 'EXCEL', name: 'Excel格式', description: 'Excel格式导出', extension: '.xlsx', contentType: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' },
  PDF: { code: 'PDF', name: 'PDF格式', description: 'PDF格式导出', extension: '.pdf', contentType: 'application/pdf' }
};

export default {
  financeApi,
  paginationConfig,
  businessTypes,
  paymentMethods,
  voucherStatus,
  paymentStatus,
  reportTypes,
  reportPeriods,
  validationRules,
  exportFormats
};