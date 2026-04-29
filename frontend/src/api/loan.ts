import request from '@/utils/request'
import type { 
  LoanApplyRequest, 
  LoanResponse, 
  RepaymentRequest,
  RepaymentResponse,
  PageResult
} from '@/types/loan'

/**
 * 借款申请API
 */
export const loanApi = {
  /**
   * 提交借款申请
   */
  apply: (data: LoanApplyRequest) => {
    return request.post<LoanResponse>('/loan/apply', data)
  },

  /**
   * 获取借款列表
   */
  getLoans: (params: {
    page?: number
    size?: number
    status?: number
    userId?: number
    keyword?: string
  }) => {
    return request.get<PageResult<LoanResponse>>('/loan/list', { params })
  },

  /**
   * 获取借款详情
   */
  getLoanDetail: (loanId: number) => {
    return request.get<LoanResponse>(`/loan/${loanId}`)
  },

  /**
   * 取消借款申请
   */
  cancelLoan: (loanId: number, reason?: string) => {
    return request.post(`/loan/${loanId}/cancel`, { reason })
  },

  /**
   * 提交还款申请
   */
  repay: (data: RepaymentRequest) => {
    return request.post<RepaymentResponse>('/repayment/repay', data)
  },

  /**
   * 获取还款记录列表
   */
  getRepayments: (params: {
    page?: number
    size?: number
    loanId?: number
    userId?: number
  }) => {
    return request.get<PageResult<RepaymentResponse>>('/repayment/list', { params })
  },

  /**
   * 获取用户借款统计
   */
  getUserLoanStats: (userId: number) => {
    return request.get<{
      totalLoans: number
      pendingLoans: number
      approvedLoans: number
      rejectedLoans: number
      activeLoans: number
      totalOutstanding: number
    }>(`/loan/user/${userId}/stats`)
  },

  /**
   * 获取待还款借款列表
   */
  getPendingRepayments: (userId: number) => {
    return request.get<LoanResponse[]>('/loan/pending-repayments', { 
      params: { userId } 
    })
  }
}