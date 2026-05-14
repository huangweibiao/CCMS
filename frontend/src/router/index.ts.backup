import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'
import { RouterGuard } from './guard'
import type { AppRouteRecordRaw } from './types'

/**
 * 静态路由配置
 * 不需要动态权限控制的路由
 */
const staticRoutes: AppRouteRecordRaw[] = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/Login.vue'),
    meta: { title: '登录', requiresAuth: false }
  },
  {
    path: '/403',
    name: 'AccessDenied',
    component: () => import('@/views/error/AccessDenied.vue'),
    meta: { title: '权限不足', requiresAuth: false }
  }
]

/**
 * 认证后基础路由配置
 * 登录后才能访问的基础路由
 */
const baseRoutes: AppRouteRecordRaw[] = [
  {
    path: '/',
    name: 'Layout',
    component: () => import('@/views/layout/Layout.vue'),
    redirect: '/dashboard',
    meta: { title: '首页', requiresAuth: true },
    children: [
      // 仪表板 - 所有认证用户都可以访问
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/layout/Dashboard.vue'),
        meta: { title: '仪表板', icon: 'House', breadcrumb: true }
      }
    ]
  }
]

/**
 * 动态路由配置
 * 基于用户权限动态生成的路由
 */
const dynamicRoutes: AppRouteRecordRaw[] = [
  // 用户管理模块
  {
    path: 'users',
    name: 'UserManagement',
    component: () => import('@/views/user/UserList.vue'),
    meta: { 
      title: '用户管理', 
      icon: 'User', 
      permissionCode: 'user:manage',
      breadcrumb: true
    }
  },
  {
    path: 'departments',
    name: 'DepartmentManagement',
    component: () => import('@/views/user/DepartmentList.vue'),
    meta: { 
      title: '部门管理', 
      icon: 'OfficeBuilding', 
      permissionCode: 'dept:manage',
      breadcrumb: true
    }
  },
  // 系统管理模块
  {
    path: 'system/dict',
    name: 'DataDict',
    component: () => import('@/views/system/DataDict.vue'),
    meta: { 
      title: '数据字典', 
      icon: 'Collection', 
      permissionCode: 'system:dict',
      breadcrumb: true
    }
  },
  {
    path: 'system/config',
    name: 'SystemConfig',
    component: () => import('@/views/system/SystemConfig.vue'),
    meta: { 
      title: '系统配置', 
      icon: 'Setting', 
      permissionCode: 'system:config',
      breadcrumb: true
    }
  },
  {
    path: 'system/attachment',
    name: 'Attachment',
    component: () => import('@/views/system/Attachment.vue'),
    meta: { 
      title: '附件管理', 
      icon: 'Paperclip', 
      permissionCode: 'system:attachment',
      breadcrumb: true
    }
  },
  {
    path: 'system/log',
    name: 'OperLog',
    component: () => import('@/views/system/OperLog.vue'),
    meta: { 
      title: '操作日志', 
      icon: 'Document', 
      permissionCode: 'system:log',
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
      permissionCode: 'budget:manage',
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
      permissionCode: 'expense:apply',
      breadcrumb: true
    }
  },
  {
    path: 'expense-apply/create',
    name: 'ExpenseApplyCreate',
    component: () => import('@/views/expense/ApplyCreate.vue'),
    meta: { 
      title: '新建申请', 
      hidden: true,
      permissionCode: 'expense:create',
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
      permissionCode: 'expense:reimburse',
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
      permissionCode: 'approval:process',
      breadcrumb: true
    }
  },
  {
    path: 'approval/history',
    name: 'ApprovalHistory',
    component: () => import('@/views/approval/HistoryList.vue'),
    meta: { 
      title: '已办审批', 
      icon: 'Finished',
      permissionCode: 'approval:history',
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
      permissionCode: 'report:view',
      breadcrumb: true
    }
  },
  // 借款管理
  {
    path: 'loan/apply',
    name: 'LoanApply',
    component: () => import('@/views/loan/ApplyForm.vue'),
    meta: { 
      title: '借款申请', 
      icon: 'Money', 
      hidden: true,
      permissionCode: 'loan:apply',
      breadcrumb: true
    }
  },
  {
    path: 'loan/list',
    name: 'LoanList',
    component: () => import('@/views/loan/LoanList.vue'),
    meta: { 
      title: '我的借款', 
      icon: 'Coin',
      permissionCode: 'loan:view',
      breadcrumb: true
    }
  },
  // 还款管理
  {
    path: 'repayment/apply',
    name: 'RepaymentApply',
    component: () => import('@/views/repayment/RepaymentForm.vue'),
    meta: { 
      title: '还款申请', 
      icon: 'CreditCard', 
      hidden: true,
      permissionCode: 'repayment:apply',
      breadcrumb: true
    }
  },
  {
    path: 'repayment/list',
    name: 'RepaymentList',
    component: () => import('@/views/repayment/RepaymentList.vue'),
    meta: { 
      title: '还款记录', 
      icon: 'Wallet',
      permissionCode: 'repayment:view',
      breadcrumb: true
    }
  }
]

// 将所有动态路由添加到基础路由的children中
const layoutRoute = baseRoutes.find(route => route.name === 'Layout')
if (layoutRoute && layoutRoute.children) {
  layoutRoute.children.push(...dynamicRoutes)
}

// 最终路由配置 = 静态路由 + 认证后基础路由
const routes: RouteRecordRaw[] = [
  ...staticRoutes,
  ...baseRoutes,
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

// 注册全局路由守卫
router.beforeEach(RouterGuard.beforeEach)

// 导出路由重置方法
export const resetRouter = () => {
  RouterGuard.reset()
}

// 导出动态路由配置，供外部使用
export { dynamicRoutes }

// 路由管理工具类
export class RouterManager {
  /**
   * 添加动态路由
   */
  static addRoutes(routes: AppRouteRecordRaw[]) {
    routes.forEach(route => {
      if (!router.hasRoute(route.name as string)) {
        router.addRoute('Layout', route)
      }
    })
  }

  /**
   * 移除动态路由
   */
  static removeRoutes(routeNames: string[]) {
    routeNames.forEach(name => {
      if (router.hasRoute(name)) {
        router.removeRoute(name)
      }
    })
  }

  /**
   * 获取所有路由
   */
  static getRoutes() {
    return router.getRoutes()
  }

  /**
   * 获取可访问的路由菜单
   */
  static async getAccessibleRoutes() {
    const layoutRoute = router.getRoutes().find(route => route.name === 'Layout')
    if (layoutRoute && layoutRoute.children) {
      // 在实际实现中，这里需要基于权限过滤路由
      return layoutRoute.children.filter(route => !route.meta?.hidden)
    }
    return []
  }
}

export default router