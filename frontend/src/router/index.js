import { createRouter, createWebHistory } from 'vue-router';
import FinanceDashboard from '../views/finance/FinanceDashboard.vue';
import FinanceVoucherManager from '../views/finance/FinanceVoucherManager.vue';
import FinancePaymentManager from '../views/finance/FinancePaymentManager.vue';
import FinanceReportCenter from '../views/finance/FinanceReportCenter.vue';

const routes = [
  {
    path: '/',
    redirect: '/dashboard'
  },
  {
    path: '/dashboard',
    name: 'Dashboard',
    component: FinanceDashboard,
    meta: { title: '仪表盘', icon: 'Odometer', requiresAuth: true }
  },
  {
    path: '/finance/voucher',
    name: 'FinanceVoucher',
    component: FinanceVoucherManager,
    meta: { title: '凭证管理', icon: 'Document', requiresAuth: true, roles: ['admin', 'accountant'] }
  },
  {
    path: '/finance/payment',
    name: 'FinancePayment',
    component: FinancePaymentManager,
    meta: { title: '支付管理', icon: 'Wallet', requiresAuth: true, roles: ['admin', 'accountant'] }
  },
  {
    path: '/finance/report',
    name: 'FinanceReport',
    component: FinanceReportCenter,
    meta: { title: '报表中心', icon: 'DataAnalysis', requiresAuth: true, roles: ['admin', 'accountant', 'manager'] }
  }
];

const router = createRouter({
  history: createWebHistory(),
  routes
});

export default router;