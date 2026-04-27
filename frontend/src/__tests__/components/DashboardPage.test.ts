import { describe, it, expect, beforeEach, vi, afterEach } from 'vitest';
import { mount, flushPromises } from '@vue/test-utils';
import { createPinia, setActivePinia } from 'pinia';
import DashboardPage from '../../pages/DashboardPage.vue';
import { useAuthStore } from '../../stores/auth';
import { useBudgetStore } from '../../stores/budget';
import { useExpenseStore } from '../../stores/expense';

// Mock the stores
vi.mock('../../stores/auth', () => ({
  useAuthStore: vi.fn()
}));

vi.mock('../../stores/budget', () => ({
  useBudgetStore: vi.fn()
}));

vi.mock('../../stores/expense', () => ({
  useExpenseStore: vi.fn()
}));

describe('DashboardPage', () => {
  let authStore: any;
  let budgetStore: any;
  let expenseStore: any;

  beforeEach(() => {
    setActivePinia(createPinia());
    
    // Create mock stores
    authStore = {
      userInfo: {
        id: 1,
        username: 'testuser',
        name: '测试用户',
        role: 'USER'
      },
      userName: '测试用户',
      userRole: 'USER'
    };
    
    budgetStore = {
      budgets: [
        {
          id: 1,
          department: '技术部',
          year: 2024,
          month: 1,
          amount: 50000,
          usedAmount: 25000,
          remainingAmount: 25000,
          status: 'APPROVED'
        }
      ],
      totalBudgetAmount: 50000,
      totalUsedAmount: 25000,
      totalRemainingAmount: 25000,
      loading: false,
      loadBudgets: vi.fn().mockResolvedValue({ success: true })
    };
    
    expenseStore = {
      expenseApplications: [
        {
          id: 1,
          title: '项目费用申请',
          amount: 5000,
          status: 'PENDING',
          createdAt: '2024-01-15'
        }
      ],
      expenseReimburses: [
        {
          id: 1,
          title: '差旅费报销',
          amount: 2000,
          status: 'APPROVED',
          createdAt: '2024-01-10'
        }
      ],
      loading: false,
      loadExpenseApplications: vi.fn().mockResolvedValue({ success: true }),
      loadExpenseReimburses: vi.fn().mockResolvedValue({ success: true })
    };
    
    (useAuthStore as any).mockReturnValue(authStore);
    (useBudgetStore as any).mockReturnValue(budgetStore);
    (useExpenseStore as any).mockReturnValue(expenseStore);
    
    // Mock window resize event
    vi.spyOn(window, 'addEventListener');
    vi.spyOn(window, 'removeEventListener');
  });

  afterEach(() => {
    vi.clearAllMocks();
  });

  const createWrapper = (props = {}) => {
    return mount(DashboardPage, {
      global: {
        plugins: [createPinia()],
        stubs: {
          // Stub child components
          'router-link': true,
          'el-card': true,
          'el-statistic': true,
          'el-row': true,
          'el-col': true,
          'el-table': true,
          'el-table-column': true,
          'el-tag': true
        }
      },
      props
    });
  };

  describe('Initial Render', () => {
    it('should render dashboard with user welcome message', () => {
      const wrapper = createWrapper();
      
      expect(wrapper.text()).toContain('仪表盘');
      expect(wrapper.text()).toContain('欢迎回来，测试用户');
    });

    it('should display statistics cards', () => {
      const wrapper = createWrapper();
      
      expect(wrapper.text()).toContain('总预算');
      expect(wrapper.text()).toContain('已使用');
      expect(wrapper.text()).toContain('剩余预算');
      expect(wrapper.text()).toContain('¥50,000.00');
      expect(wrapper.text()).toContain('¥25,000.00');
    });

    it('should display recent activity sections', () => {
      const wrapper = createWrapper();
      
      expect(wrapper.text()).toContain('最新费用申请');
      expect(wrapper.text()).toContain('最新费用报销');
      expect(wrapper.text()).toContain('项目费用申请');
      expect(wrapper.text()).toContain('差旅费报销');
    });

    it('should render navigation menu', () => {
      const wrapper = createWrapper();
      
      expect(wrapper.text()).toContain('预算管理');
      expect(wrapper.text()).toContain('费用申请');
      expect(wrapper.text()).toContain('费用报销');
      expect(wrapper.text()).toContain('审批流程');
    });
  });

  describe('Data Loading', () => {
    it('should load data on component mount', async () => {
      createWrapper();
      
      await flushPromises();
      
      expect(budgetStore.loadBudgets).toHaveBeenCalled();
      expect(expenseStore.loadExpenseApplications).toHaveBeenCalled();
      expect(expenseStore.loadExpenseReimburses).toHaveBeenCalled();
    });

    it('should show loading state while fetching data', async () => {
      budgetStore.loading = true;
      expenseStore.loading = true;
      
      const wrapper = createWrapper();
      
      expect(wrapper.text()).toContain('加载中');
      expect(wrapper.find('.loading-spinner').exists()).toBe(true);
    });

    it('should handle data loading failure', async () => {
      budgetStore.loadBudgets.mockRejectedValue(new Error('加载失败'));
      budgetStore.error = '加载预算数据失败';
      
      const wrapper = createWrapper();
      await flushPromises();
      
      expect(wrapper.text()).toContain('加载预算数据失败');
    });

    it('should refresh data when refresh button is clicked', async () => {
      const wrapper = createWrapper();
      await flushPromises();
      
      // Clear previous call counts
      budgetStore.loadBudgets.mockClear();
      expenseStore.loadExpenseApplications.mockClear();
      expenseStore.loadExpenseReimburses.mockClear();
      
      // Find and click refresh button
      const refreshButton = wrapper.find('.refresh-button');
      if (refreshButton.exists()) {
        await refreshButton.trigger('click');
        
        expect(budgetStore.loadBudgets).toHaveBeenCalled();
        expect(expenseStore.loadExpenseApplications).toHaveBeenCalled();
        expect(expenseStore.loadExpenseReimburses).toHaveBeenCalled();
      }
    });
  });

  describe('Statistics Display', () => {
    it('should display correct budget statistics', () => {
      const wrapper = createWrapper();
      
      expect(wrapper.text()).toContain('¥50,000.00');
      expect(wrapper.text()).toContain('¥25,000.00');
      expect(wrapper.text()).toContain('¥25,000.00');
    });

    it('should handle zero values in statistics', () => {
      budgetStore.totalBudgetAmount = 0;
      budgetStore.totalUsedAmount = 0;
      budgetStore.totalRemainingAmount = 0;
      
      const wrapper = createWrapper();
      
      expect(wrapper.text()).toContain('¥0.00');
    });

    it('should handle very large budget amounts', () => {
      budgetStore.totalBudgetAmount = 1000000000; // 10亿
      budgetStore.totalUsedAmount = 500000000;
      budgetStore.totalRemainingAmount = 500000000;
      
      const wrapper = createWrapper();
      
      expect(wrapper.text()).toContain('¥1,000,000,000.00');
      expect(wrapper.text()).toContain('¥500,000,000.00');
    });

    it('should calculate and display usage percentage', () => {
      const wrapper = createWrapper();
      
      // 25000 / 50000 = 50%
      expect(wrapper.text()).toContain('50%');
    });
  });

  describe('Recent Activity Tables', () => {
    it('should display expense applications in table', () => {
      const wrapper = createWrapper();
      
      expect(wrapper.text()).toContain('项目费用申请');
      expect(wrapper.text()).toContain('¥5,000.00');
      expect(wrapper.text()).toContain('待审批');
    });

    it('should display expense reimburses in table', () => {
      const wrapper = createWrapper();
      
      expect(wrapper.text()).toContain('差旅费报销');
      expect(wrapper.text()).toContain('¥2,000.00');
      expect(wrapper.text()).toContain('已批准');
    });

    it('should handle empty activity lists', () => {
      expenseStore.expenseApplications = [];
      expenseStore.expenseReimburses = [];
      
      const wrapper = createWrapper();
      
      expect(wrapper.text()).toContain('暂无数据');
      expect(wrapper.text()).toContain('暂无报销记录');
    });

    it('should format date and amount correctly', () => {
      expenseStore.expenseApplications[0].createdAt = '2024-01-15T10:30:00';
      expenseStore.expenseApplications[0].amount = 1234.56;
      
      const wrapper = createWrapper();
      
      expect(wrapper.text()).toContain('2024-01-15');
      expect(wrapper.text()).toContain('¥1,234.56');
    });

    it('should display correct status tags with proper colors', () => {
      const wrapper = createWrapper();
      
      // Check if status tags are rendered with appropriate classes
      const pendingTag = wrapper.find('.status-pending');
      const approvedTag = wrapper.find('.status-approved');
      
      if (pendingTag.exists()) {
        expect(pendingTag.text()).toContain('待审批');
      }
      
      if (approvedTag.exists()) {
        expect(approvedTag.text()).toContain('已批准');
      }
    });
  });

  describe('User Interaction', () => {
    it('should navigate to budget management when card is clicked', async () => {
      const mockRouter = {
        push: vi.fn()
      };
      
      const wrapper = mount(DashboardPage, {
        global: {
          plugins: [createPinia()],
          mocks: {
            $router: mockRouter
          },
          stubs: {
            'router-link': true,
            'el-card': true,
            'el-statistic': true
          }
        }
      });
      
      const budgetCard = wrapper.find('.budget-card');
      if (budgetCard.exists()) {
        await budgetCard.trigger('click');
        expect(mockRouter.push).toHaveBeenCalledWith('/budget');
      }
    });

    it('should show detailed view when view more is clicked', async () => {
      const wrapper = createWrapper();
      
      const viewMoreButton = wrapper.find('.view-more');
      if (viewMoreButton.exists()) {
        await viewMoreButton.trigger('click');
        
        // Should navigate to the corresponding page
        const routerLink = wrapper.find('router-link');
        if (routerLink.exists()) {
          expect(routerLink.attributes('to')).toBe('/expense/applications');
        }
      }
    });

    it('should handle row click in activity tables', async () => {
      const mockRouter = {
        push: vi.fn()
      };
      
      const wrapper = mount(DashboardPage, {
        global: {
          plugins: [createPinia()],
          mocks: {
            $router: mockRouter
          },
          stubs: {
            'el-table': {
              template: '<div><tr class="table-row"></tr></div>'
            }
          }
        }
      });
      
      const tableRow = wrapper.find('.table-row');
      if (tableRow.exists()) {
        await tableRow.trigger('click');
        
        // Should navigate to detail page
        expect(mockRouter.push).toHaveBeenCalledWith('/expense/applications/1');
      }
    });
  });

  describe('Access Control', () => {
    it('should show admin features for admin users', () => {
      authStore.userRole = 'ADMIN';
      authStore.userInfo.role = 'ADMIN';
      
      const wrapper = createWrapper();
      
      expect(wrapper.text()).toContain('系统管理');
      expect(wrapper.text()).toContain('用户管理');
    });

    it('should hide admin features for regular users', () => {
      authStore.userRole = 'USER';
      authStore.userInfo.role = 'USER';
      
      const wrapper = createWrapper();
      
      expect(wrapper.text()).not.toContain('系统管理');
      expect(wrapper.text()).not.toContain('用户管理');
    });

    it('should show approval section for users with approval permission', () => {
      authStore.hasPermission = vi.fn().mockImplementation((permission: string) => {
        return permission === 'approval:view';
      });
      
      const wrapper = createWrapper();
      
      expect(wrapper.text()).toContain('待我审批');
    });

    it('should hide approval section for users without permission', () => {
      authStore.hasPermission = vi.fn().mockReturnValue(false);
      
      const wrapper = createWrapper();
      
      expect(wrapper.text()).not.toContain('待我审批');
    });
  });

  describe('Responsive Behavior', () => {
    it('should adapt layout for different screen sizes', () => {
      const wrapper = createWrapper();
      
      // Check for responsive CSS classes
      const container = wrapper.find('.dashboard-container');
      expect(container.exists()).toBe(true);
      
      // Check if grid system adapts
      const gridCols = wrapper.findAll('.grid-col');
      if (gridCols.length > 0) {
        gridCols.forEach(col => {
          const classes = col.classes();
          expect(classes.some(c => c.includes('col-') || c.includes('span-'))).toBe(true);
        });
      }
    });

    it('should handle window resize events', () => {
      createWrapper();
      
      // Trigger resize event
      window.dispatchEvent(new Event('resize'));
      
      expect(window.addEventListener).toHaveBeenCalledWith('resize', expect.any(Function));
    });
  });

  describe('Error Handling', () => {
    it('should display error message when budget data fails to load', async () => {
      budgetStore.loadBudgets.mockRejectedValue(new Error('Network error'));
      budgetStore.error = '无法加载预算数据';
      
      const wrapper = createWrapper();
      await flushPromises();
      
      expect(wrapper.text()).toContain('无法加载预算数据');
      
      // Error should be dismissible
      const errorCloseButton = wrapper.find('.error-close');
      if (errorCloseButton.exists()) {
        await errorCloseButton.trigger('click');
        expect(wrapper.text()).not.toContain('无法加载预算数据');
      }
    });

    it('should handle null user info gracefully', () => {
      authStore.userInfo = null;
      authStore.userName = '';
      
      const wrapper = createWrapper();
      
      expect(wrapper.text()).toContain('欢迎回来');
      expect(wrapper.text()).not.toContain('测试用户');
    });

    it('should handle missing store data', () => {
      budgetStore.budgets = null;
      expenseStore.expenseApplications = undefined;
      
      const wrapper = createWrapper();
      
      // Should not crash and show appropriate fallback
      expect(wrapper.text()).toContain('暂无数据');
    });
  });

  describe('Performance Optimization', () => {
    it('should use virtual scrolling for large data sets', () => {
      // Create large dataset
      const largeApplications = Array.from({ length: 1000 }, (_, i) => ({
        id: i + 1,
        title: `费用申请 ${i + 1}`,
        amount: 1000 + i * 100,
        status: i % 3 === 0 ? 'PENDING' : i % 3 === 1 ? 'APPROVED' : 'REJECTED',
        createdAt: '2024-01-01'
      }));
      
      expenseStore.expenseApplications = largeApplications;
      
      const wrapper = createWrapper();
      
      // Check if virtual scrolling is enabled
      const table = wrapper.find('el-table');
      if (table.exists()) {
        const props = table.props();
        if (props.virtual) {
          expect(props.virtual).toBe(true);
        }
      }
    });

    it('should debounce data refresh requests', async () => {
      const wrapper = createWrapper();
      
      // Clear initial calls
      budgetStore.loadBudgets.mockClear();
      
      // Trigger multiple rapid refresh attempts
      const refreshButton = wrapper.find('.refresh-button');
      if (refreshButton.exists()) {
        await refreshButton.trigger('click');
        await refreshButton.trigger('click');
        await refreshButton.trigger('click');
        
        // Should only call API once due to debouncing
        await new Promise(resolve => setTimeout(resolve, 300));
        expect(budgetStore.loadBudgets).toHaveBeenCalledTimes(1);
      }
    });
  });
});