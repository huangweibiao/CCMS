import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { loanApi } from '@/api/loan'
import type { 
  LoanApplyRequest, 
  LoanResponse, 
  RepaymentRequest,
  RepaymentResponse,
  LoanStats,
  PageResult 
} from '@/types/loan'

export const useLoanStore = defineStore('loan', () => {
  // 状态
  const loanList = ref<LoanResponse[]>([])
  const currentLoan = ref<LoanResponse | null>(null)
  const repaymentList = ref<RepaymentResponse[]>([])
  const loanStats = ref<LoanStats | null>(null)
  const pendingRepayments = ref<LoanResponse[]>([])
  
  // 分页信息
  const pagination = ref({
    current: 1,
    size: 20,
    total: 0,
    pages: 0
  })

  // getters
  const pendingLoans = computed(() => 
    loanList.value.filter(loan => loan.status === 2)
  )

  const activeLoans = computed(() => 
    loanList.value.filter(loan => loan.status === 3)
  )

  const totalOutstanding = computed(() => 
    loanList.value.reduce((sum, loan) => sum + loan.outstandingAmount, 0)
  )

  // actions
  const submitLoanApply = async (data: LoanApplyRequest) => {
    try {
      const response = await loanApi.apply(data)
      // 重新加载借款列表
      await loadLoanList()
      return response.data
    } catch (error) {
      throw error
    }
  }

  const loadLoanList = async (params: {
    page?: number
    size?: number
    status?: number
    keyword?: string
  } = {}) => {
    try {
      const response = await loanApi.getLoans({
        page: params.page || pagination.value.current,
        size: params.size || pagination.value.size,
        status: params.status,
        keyword: params.keyword
      })
      
      loanList.value = response.data.records
      pagination.value = {
        current: response.data.current,
        size: response.data.size,
        total: response.data.total,
        pages: response.data.pages
      }
    } catch (error) {
      console.error('加载借款列表失败:', error)
      throw error
    }
  }

  const loadLoanDetail = async (loanId: number) => {
    try {
      const response = await loanApi.getLoanDetail(loanId)
      currentLoan.value = response.data
      return response.data
    } catch (error) {
      console.error('加载借款详情失败:', error)
      throw error
    }
  }

  const submitRepayment = async (data: RepaymentRequest) => {
    try {
      const response = await loanApi.repay(data)
      // 重新加载借款详情和还款列表
      if (data.loanId) {
        await loadLoanDetail(data.loanId)
        await loadRepaymentList({ loanId: data.loanId })
      }
      return response.data
    } catch (error) {
      throw error
    }
  }

  const loadRepaymentList = async (params: {
    page?: number
    size?: number
    loanId?: number
  } = {}) => {
    try {
      const response = await loanApi.getRepayments(params)
      repaymentList.value = response.data.records
      return response.data.records
    } catch (error) {
      console.error('加载还款记录失败:', error)
      throw error
    }
  }

  const loadLoanStats = async (userId: number) => {
    try {
      const response = await loanApi.getUserLoanStats(userId)
      loanStats.value = response.data
      return response.data
    } catch (error) {
      console.error('加载借款统计失败:', error)
      throw error
    }
  }

  const loadPendingRepayments = async (userId: number) => {
    try {
      const response = await loanApi.getPendingRepayments(userId)
      pendingRepayments.value = response.data
      return response.data
    } catch (error) {
      console.error('加载待还款列表失败:', error)
      throw error
    }
  }

  const cancelLoan = async (loanId: number, reason?: string) => {
    try {
      await loanApi.cancelLoan(loanId, reason)
      // 重新加载借款列表
      await loadLoanList()
      if (currentLoan.value?.id === loanId) {
        await loadLoanDetail(loanId)
      }
    } catch (error) {
      throw error
    }
  }

  const updatePagination = (current: number, size: number) => {
    pagination.value.current = current
    pagination.value.size = size
  }

  // 重置状态
  const reset = () => {
    loanList.value = []
    currentLoan.value = null
    repaymentList.value = []
    loanStats.value = null
    pendingRepayments.value = []
    pagination.value = {
      current: 1,
      size: 20,
      total: 0,
      pages: 0
    }
  }

  return {
    // state
    loanList,
    currentLoan,
    repaymentList,
    loanStats,
    pendingRepayments,
    pagination,
    
    // getters
    pendingLoans,
    activeLoans,
    totalOutstanding,
    
    // actions
    submitLoanApply,
    loadLoanList,
    loadLoanDetail,
    submitRepayment,
    loadRepaymentList,
    loadLoanStats,
    loadPendingRepayments,
    cancelLoan,
    updatePagination,
    reset
  }
})