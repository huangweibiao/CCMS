import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'

/**
 * 静态路由配置 - 登录后可直接访问的基础路由
 */
const routes: RouteRecordRaw[] = [
  // 登录页面
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/Login.vue'),
    meta: { title: '登录', requiresAuth: false }
  },
  
  // 权限不足页面
  {
    path: '/403',
    name: 'AccessDenied',
    component: () => import('@/views/error/AccessDenied.vue'),
    meta: { title: '权限不足', requiresAuth: false }
  },
  
  // 主布局和其子路由
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
        meta: { title: '仪表板', icon: 'House', breadcrumb: true }
      },
      
      // 用户管理
      {
        path: 'users',
        name: 'UserManagement',
        component: () => import('@/views/user/UserList.vue'),
        meta: { 
          title: '用户管理', 
          icon: 'User', 
          breadcrumb: true
        }
      },
      
      // 费用申请
      {
        path: 'expense-apply',
        name: 'ExpenseApply',
        component: () => import('@/views/expense/ApplyList.vue'),
        meta: { 
          title: '费用申请', 
          icon: 'DocumentAdd',
          breadcrumb: true
        }
      },
      
      // 费用报销
      {
        path: 'expense-reimburse',
        name: 'ExpenseReimburse',
        component: () => import('@/views/expense/ReimburseList.vue'),
        meta: { 
          title: '费用报销', 
          icon: 'Money',
          breadcrumb: true
        }
      },
      
      // 审批管理
      {
        path: 'approval/pending',
        name: 'ApprovalPending',
        component: () => import('@/views/approval/PendingList.vue'),
        meta: { 
          title: '待办审批', 
          icon: 'Bell',
          breadcrumb: true
        }
      },
      
      // 预算管理
      {
        path: 'budgets',
        name: 'BudgetManagement',
        component: () => import('@/views/budget/BudgetManagement.vue'),
        meta: { 
          title: '预算管理', 
          icon: 'Money',
          breadcrumb: true
        }
      },
      
      // 报表统计
      {
        path: 'reports/expense',
        name: 'ExpenseReport',
        component: () => import('@/views/report/ExpenseReport.vue'),
        meta: { 
          title: '费用统计', 
          icon: 'DataAnalysis',
          breadcrumb: true
        }
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

// 创建路由器实例
const router = createRouter({
  history: createWebHistory(),
  routes
})

import { useAuthStore } from '@/stores/auth'

router.beforeEach((to) => {
  const auth = useAuthStore()
  if (to.path !== '/login' && !auth.token) {
    return '/login'
  }
  if (to.path === '/login' && auth.token) {
    return '/'
  }
  return true
})

export default router