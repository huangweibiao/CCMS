import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'
import type { RouteRecordRaw } from 'vue-router'

/**
 * 路由配置
 * 定义系统的所有路由及其权限要求
 */
const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/Login.vue'),
    meta: { title: '登录', requiresAuth: false }
  },
  {
    path: '/',
    name: 'Layout',
    component: () => import('@/views/layout/Layout.vue'),
    redirect: '/dashboard',
    meta: { title: '首页', requiresAuth: true },
    children: [
      // 仪表板
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/layout/Dashboard.vue'),
        meta: { title: '仪表板', icon: 'House' }
      },
      // 用户管理
      {
        path: 'users',
        name: 'UserManagement',
        component: () => import('@/views/user/UserList.vue'),
        meta: { title: '用户管理', icon: 'User', requiresAdmin: true }
      },
      {
        path: 'departments',
        name: 'DepartmentManagement',
        component: () => import('@/views/user/DepartmentList.vue'),
        meta: { title: '部门管理', icon: 'OfficeBuilding', requiresAdmin: true }
      },
      // 预算管理
      {
        path: 'budgets',
        name: 'BudgetManagement',
        component: () => import('@/views/budget/BudgetManagement.vue'),
        meta: { title: '预算管理', icon: 'Money' }
      },
      // 费用申请
      {
        path: 'expense-apply',
        name: 'ExpenseApply',
        component: () => import('@/views/expense/ApplyList.vue'),
        meta: { title: '费用申请', icon: 'DocumentAdd' }
      },
      {
        path: 'expense-apply/create',
        name: 'ExpenseApplyCreate',
        component: () => import('@/views/expense/ApplyCreate.vue'),
        meta: { title: '新建申请', hidden: true }
      },
      // 费用报销
      {
        path: 'expense-reimburse',
        name: 'ExpenseReimburse',
        component: () => import('@/views/expense/ReimburseList.vue'),
        meta: { title: '费用报销', icon: 'Money' }
      },
      // 审批管理
      {
        path: 'approval/pending',
        name: 'ApprovalPending',
        component: () => import('@/views/approval/PendingList.vue'),
        meta: { title: '待办审批', icon: 'Bell' }
      },
      {
        path: 'approval/history',
        name: 'ApprovalHistory',
        component: () => import('@/views/approval/HistoryList.vue'),
        meta: { title: '已办审批', icon: 'Finished' }
      },
      // 报表统计
      {
        path: 'reports/expense',
        name: 'ExpenseReport',
        component: () => import('@/views/report/ExpenseReport.vue'),
        meta: { title: '费用统计', icon: 'DataAnalysis' }
      },
      // 借款管理
      {
        path: 'loan/apply',
        name: 'LoanApply',
        component: () => import('@/views/loan/ApplyForm.vue'),
        meta: { title: '借款申请', icon: 'Money', hidden: true }
      },
      {
        path: 'loan/list',
        name: 'LoanList',
        component: () => import('@/views/loan/LoanList.vue'),
        meta: { title: '我的借款', icon: 'Coin' }
      },
      // 还款管理
      {
        path: 'repayment/apply',
        name: 'RepaymentApply',
        component: () => import('@/views/repayment/RepaymentForm.vue'),
        meta: { title: '还款申请', icon: 'CreditCard', hidden: true }
      },
      {
        path: 'repayment/list',
        name: 'RepaymentList',
        component: () => import('@/views/repayment/RepaymentList.vue'),
        meta: { title: '还款记录', icon: 'Wallet' }
      }
    ]
  },
  // 404页面
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/views/layout/NotFound.vue'),
    meta: { title: '页面未找到' }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫 - 权限控制
router.beforeEach((to, from, next) => {
  const userStore = useUserStore()
  
  // 设置页面标题
  if (to.meta.title) {
    document.title = `${to.meta.title} - CCMS`
  }
  
  // 检查是否需要登录
  if (to.meta.requiresAuth && !userStore.isAuthenticated) {
    next('/login')
    return
  }
  
  // 检查管理员权限
  if (to.meta.requiresAdmin && !userStore.isAdmin) {
    next('/dashboard')
    return
  }
  
  // 已登录用户访问登录页，重定向到首页
  if (to.path === '/login' && userStore.isAuthenticated) {
    next('/dashboard')
    return
  }
  
  next()
})

export default router