import { describe, it, expect, vi, beforeEach } from 'vitest';
import { mount } from '@vue/test-utils';
import { ElTable, ElPagination, ElTabs, ElButton } from 'element-plus';
import TodoApproval from '@/views/approval/TodoApproval.vue';

const mockApprovalList = [
  {
    id: 1,
    title: '差旅费申请',
    applicant: '张三',
    amount: 5000,
    applyTime: '2024-01-15 10:30:00',
    status: 'PENDING',
    currentApprover: '李经理',
    deadline: '2024-01-18 18:00:00'
  },
  {
    id: 2,
    title: '办公用品采购',
    applicant: '李四',
    amount: 3000,
    applyTime: '2024-01-14 14:20:00',
    status: 'PENDING',
    currentApprover: '王总监',
    deadline: '2024-01-17 17:00:00'
  }
];

const mockApprovedList = [
  {
    id: 3,
    title: '项目招待费',
    applicant: '王五',
    amount: 8000,
    applyTime: '2024-01-13 09:15:00',
    status: 'APPROVED',
    approveTime: '2024-01-15 14:30:00',
    approver: '钱总'
  }
];

describe('TodoApproval', () => {
  let wrapper;

  beforeEach(() => {
    wrapper = mount(TodoApproval, {
      global: {
        stubs: {
          'el-table': ElTable,
          'el-pagination': ElPagination,
          'el-tabs': ElTabs,
          'el-button': ElButton
        }
      }
    });
  });

  it('renders todo approval interface correctly', () => {
    expect(wrapper.find('[data-testid="approval-tabs"]').exists()).toBe(true);
    expect(wrapper.find('[data-testid="search-form"]').exists()).toBe(true);
    expect(wrapper.find('[data-testid="approval-table"]').exists()).toBe(true);
    expect(wrapper.find('[data-testid="refresh-btn"]').exists()).toBe(true);
  });

  it('loads approval list on component mount', async () => {
    const loadApprovalsSpy = vi.spyOn(wrapper.vm, 'loadApprovalList');
    
    await wrapper.vm.$nextTick();
    
    expect(loadApprovalsSpy).toHaveBeenCalled();
  });

  it('switches between tab types correctly', async () => {
    // 初始应该在待办标签
    expect(wrapper.vm.activeTab).toBe('pending');
    
    // 切换到已办标签
    wrapper.vm.activeTab = 'approved';
    await wrapper.vm.$nextTick();
    
    expect(wrapper.vm.activeTab).toBe('approved');
    expect(wrapper.vm.loadApprovalList).toHaveBeenCalledWith('APPROVED');
    
    // 切换到全部标签
    wrapper.vm.activeTab = 'all';
    await wrapper.vm.$nextTick();
    
    expect(wrapper.vm.activeTab).toBe('all');
    expect(wrapper.vm.loadApprovalList).toHaveBeenCalledWith('ALL');
  });

  it('searches approvals based on criteria', async () => {
    // 设置搜索条件
    wrapper.vm.searchForm.title = '差旅';
    wrapper.vm.searchForm.applicant = '张三';
    
    await wrapper.vm.handleSearch();
    
    expect(wrapper.vm.loadApprovalList).toHaveBeenCalledWith(
      'PENDING',
      expect.objectContaining({
        title: '差旅',
        applicant: '张三'
      })
    );
  });

  it('resets search criteria and reloads data', async () => {
    // 先设置一些搜索条件
    wrapper.vm.searchForm.title = '测试';
    wrapper.vm.searchForm.applicant = '测试人';
    
    await wrapper.vm.handleReset();
    
    expect(wrapper.vm.searchForm.title).toBe('');
    expect(wrapper.vm.searchForm.applicant).toBe('');
    expect(wrapper.vm.loadApprovalList).toHaveBeenCalledWith('PENDING', {});
  });

  it('navigates to approval detail page', async () => {
    const approval = mockApprovalList[0];
    const routerPushSpy = vi.fn();
    
    wrapper.vm.$router = { push: routerPushSpy };
    
    await wrapper.vm.handleViewDetail(approval);
    
    expect(routerPushSpy).toHaveBeenCalledWith({
      name: 'ApprovalDetail',
      params: { id: approval.id }
    });
  });

  it('approves an application', async () => {
    const approval = mockApprovalList[0];
    const approveSpy = vi.spyOn(wrapper.vm, 'handleApprove');
    
    await wrapper.vm.handleApprove(approval);
    
    expect(approveSpy).toHaveBeenCalledWith(approval);
    expect(wrapper.vm.approveDialogVisible).toBe(true);
    expect(wrapper.vm.currentApproval).toEqual(approval);
  });

  it('rejects an application', async () => {
    const approval = mockApprovalList[0];
    const rejectSpy = vi.spyOn(wrapper.vm, 'handleReject');
    
    await wrapper.vm.handleReject(approval);
    
    expect(rejectSpy).toHaveBeenCalledWith(approval);
    expect(wrapper.vm.rejectDialogVisible).toBe(true);
    expect(wrapper.vm.currentApproval).toEqual(approval);
  });

  it('transfers approval to another user', async () => {
    const approval = mockApprovalList[0];
    const transferSpy = vi.spyOn(wrapper.vm, 'handleTransfer');
    
    await wrapper.vm.handleTransfer(approval);
    
    expect(transferSpy).toHaveBeenCalledWith(approval);
    expect(wrapper.vm.transferDialogVisible).toBe(true);
    expect(wrapper.vm.currentApproval).toEqual(approval);
  });

  it('formats approval status correctly', () => {
    const pendingStatus = wrapper.vm.formatStatus('PENDING');
    const approvedStatus = wrapper.vm.formatStatus('APPROVED');
    const rejectedStatus = wrapper.vm.formatStatus('REJECTED');
    
    expect(pendingStatus).toBe('审核中');
    expect(approvedStatus).toBe('已通过');
    expect(rejectedStatus).toBe('已拒绝');
  });

  it('formats date time correctly', () => {
    const dateTime = '2024-01-15 14:30:00';
    const formatted = wrapper.vm.formatDateTime(dateTime);
    
    expect(formatted).toMatch(/2024-01-15/);
  });

  it('calculates remaining time until deadline', () => {
    const deadline = new Date();
    deadline.setDate(deadline.getDate() + 2); // 2天后
    
    const remaining = wrapper.vm.calculateRemainingTime(deadline.toISOString());
    
    expect(remaining).toContain('2天');
  });

  it('handles pagination correctly', async () => {
    const newPage = 2;
    const newSize = 20;
    
    await wrapper.vm.handlePageChange(newPage);
    expect(wrapper.vm.queryParams.pageNum).toBe(newPage);
    
    await wrapper.vm.handleSizeChange(newSize);
    expect(wrapper.vm.queryParams.pageSize).toBe(newSize);
    
    // 分页变化后应该重新加载数据
    expect(wrapper.vm.loadApprovalList).toHaveBeenCalled();
  });

  it('shows urgent approvals with warning style', () => {
    const urgentApproval = { deadline: new Date(Date.now() + 86400000).toISOString() }; // 1天后
    const normalApproval = { deadline: new Date(Date.now() + 259200000).toISOString() }; // 3天后
    
    const isUrgent1 = wrapper.vm.isUrgent(urgentApproval);
    const isUrgent2 = wrapper.vm.isUrgent(normalApproval);
    
    expect(isUrgent1).toBe(true);
    expect(isUrgent2).toBe(false);
  });

  it('handles bulk approval operations', async () => {
    const selectedApprovals = [mockApprovalList[0], mockApprovalList[1]];
    wrapper.vm.selectedApprovals = selectedApprovals;
    
    const bulkApproveSpy = vi.spyOn(wrapper.vm, 'handleBulkApprove');
    
    await wrapper.vm.handleBulkApprove();
    
    expect(bulkApproveSpy).toHaveBeenCalled();
    expect(wrapper.vm.bulkApproveDialogVisible).toBe(true);
  });

  it('validates bulk operations have selected items', async () => {
    // 没有选中任何项目时
    wrapper.vm.selectedApprovals = [];
    
    const result = wrapper.vm.canPerformBulkOperation();
    
    expect(result).toBe(false);
  });

  it('reloads approval data', async () => {
    const refreshSpy = vi.spyOn(wrapper.vm, 'handleRefresh');
    
    await wrapper.vm.handleRefresh();
    
    expect(refreshSpy).toHaveBeenCalled();
    expect(wrapper.vm.loadApprovalList).toHaveBeenCalled();
  });

  it('exports approval data', async () => {
    const exportSpy = vi.spyOn(wrapper.vm, 'handleExport');
    
    await wrapper.vm.handleExport();
    
    expect(exportSpy).toHaveBeenCalled();
  });

  it('follows/unfollows approval items', async () => {
    const approval = mockApprovalList[0];
    
    // 关注
    await wrapper.vm.handleFollow(approval);
    expect(wrapper.vm.followApproval).toHaveBeenCalledWith(approval.id);
    
    // 取消关注
    await wrapper.vm.handleUnfollow(approval);
    expect(wrapper.vm.unfollowApproval).toHaveBeenCalledWith(approval.id);
  });

  it('sorts approval list by different columns', async () => {
    const sortParams = { prop: 'amount', order: 'descending' };
    
    await wrapper.vm.handleSortChange(sortParams);
    
    expect(wrapper.vm.queryParams.sortBy).toBe('amount');
    expect(wrapper.vm.queryParams.sortOrder).toBe('desc');
    expect(wrapper.vm.loadApprovalList).toHaveBeenCalled();
  });

  it('handles reminder functionality', async () => {
    const approval = mockApprovalList[0];
    
    await wrapper.vm.handleRemind(approval);
    
    expect(wrapper.vm.remindDialogVisible).toBe(true);
    expect(wrapper.vm.currentApproval).toEqual(approval);
  });

  it('searches for transfer users', async () => {
    const searchTerm = '张';
    
    await wrapper.vm.searchTransferUsers(searchTerm);
    
    expect(wrapper.vm.userSearchLoading).toBe(true);
    
    // 模拟异步搜索完成
    setTimeout(() => {
      expect(wrapper.vm.userSearchLoading).toBe(false);
      expect(wrapper.vm.transferUserList.length).toBeGreaterThan(0);
    }, 300);
  });

  it('formats currency amounts correctly', () => {
    const formatted = wrapper.vm.formatAmount(12345.67);
    expect(formatted).toBe('¥12,345.67');
  });

  it('handles approval comment validation', () => {
    const comment = '同意';
    const isValid = wrapper.vm.validateComment(comment);
    
    expect(isValid).toBe(true);
  });
});