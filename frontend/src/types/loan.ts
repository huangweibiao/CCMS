/**
 * 借款申请请求参数
 */
export interface LoanApplyRequest {
  /** 申请人ID */
  userId: number
  /** 借款金额 */
  loanAmount: number
  /** 借款用途 */
  purpose: string
  /** 预计还款日期 */
  expectedRepayDate: string
  /** 附件ID列表 */
  attachmentIds?: number[]
  /** 关联报销类型ID */
  expenseTypeId?: number
  /** 备注 */
  remark?: string
}

/**
 * 借款信息响应
 */
export interface LoanResponse {
  /** 借款ID */
  id: number
  /** 借款单号 */
  loanNo: string
  /** 申请人ID */
  userId: number
  /** 申请人姓名 */
  userName: string
  /** 申请人部门 */
  departmentName: string
  /** 借款金额 */
  loanAmount: number
  /** 已还金额 */
  repaidAmount: number
  /** 未还金额 */
  outstandingAmount: number
  /** 借款用途 */
  purpose: string
  /** 借款状态 1:草稿 2:审批中 3:已放款 4:已还清 5:已取消 6:已拒绝 */
  status: number
  /** 状态名称 */
  statusName: string
  /** 期望还款日期 */
  expectedRepayDate: string
  /** 实际还款日期 */
  actualRepayDate?: string
  /** 放款日期 */
  paymentDate?: string
  /** 关联审批实例ID */
  approvalInstanceId?: number
  /** 创建时间 */
  createTime: string
  /** 更新时间 */
  updateTime: string
  /** 备注 */
  remark?: string
  /** 还款记录列表 */
  repaymentRecords?: RepaymentResponse[]
}

/**
 * 还款请求参数
 */
export interface RepaymentRequest {
  /** 借款ID */
  loanId: number
  /** 借款人ID */
  userId: number
  /** 还款金额 */
  repayAmount: number
  /** 还款方式 1:现金 2:银行转账 3:报销抵扣 4:工资抵扣 */
  repayType: number
  /** 还款凭证路径 */
  certificatePath?: string
  /** 还款银行名称 */
  bankName?: string
  /** 还款银行账号 */
  bankAccount?: string
  /** 还款单号 */
  repayNo?: string
  /** 还款说明 */
  remark?: string
}

/**
 * 还款记录响应
 */
export interface RepaymentResponse {
  /** 还款ID */
  id: number
  /** 还款单号 */
  repayNo: string
  /** 借款ID */
  loanId: number
  /** 借款单号 */
  loanNo: string
  /** 还款人ID */
  userId: number
  /** 还款人姓名 */
  userName: string
  /** 还款金额 */
  repayAmount: number
  /** 还款方式 */
  repayType: number
  /** 还款方式名称 */
  repayTypeName: string
  /** 还款状态 1:待确认 2:已确认 3:已取消 */
  status: number
  /** 状态名称 */
  statusName: string
  /** 还款银行名称 */
  bankName?: string
  /** 还款银行账号 */
  bankAccount?: string
  /** 还款凭证路径 */
  certificatePath?: string
  /** 还款时间 */
  repayTime: string
  /** 确认时间 */
  confirmTime?: string
  /** 确认人 */
  confirmUser?: string
  /** 还款说明 */
  remark?: string
  /** 创建时间 */
  createTime: string
}

/**
 * 借款统计信息
 */
export interface LoanStats {
  /** 总借款数量 */
  totalLoans: number
  /** 待审批借款数量 */
  pendingLoans: number
  /** 已审批借款数量 */
  approvedLoans: number
  /** 已拒绝借款数量 */
  rejectedLoans: number
  /** 活跃借款数量 */
  activeLoans: number
  /** 待还款总额 */
  totalOutstanding: number
}

/**
 * 分页结果
 */
export interface PageResult<T> {
  /** 数据列表 */
  records: T[]
  /** 总记录数 */
  total: number
  /** 当前页 */
  current: number
  /** 每页大小 */
  size: number
  /** 总页数 */
  pages: number
}

/**
 * 借款状态枚举
 */
export enum LoanStatus {
  DRAFT = 1,
  PENDING_APPROVAL = 2,
  APPROVED = 3,
  REPAID = 4,
  CANCELLED = 5,
  REJECTED = 6
}

/**
 * 还款方式枚举
 */
export enum RepayType {
  CASH = 1,
  BANK_TRANSFER = 2,
  REIMBURSE_DEDUCT = 3,
  SALARY_DEDUCT = 4
}

/**
 * 还款状态枚举
 */
export enum RepaymentStatus {
  PENDING = 1,
  CONFIRMED = 2,
  CANCELLED = 3
}