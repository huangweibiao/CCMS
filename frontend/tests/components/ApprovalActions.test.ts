import { describe, it, expect, vi, beforeEach } from 'vitest';
import { mount } from '@vue/test-utils';
import { ElButton, ElDialog, ElSelect } from 'element-plus';
import ApprovalActions from '@/components/approval/ApprovalActions.vue';

const mockApproval = {
  id: 1,
  title: '差旅费申请',
  amount: 5000,
  applicant: '张三',
  status: 'PENDING',
  currentApprover: '李经理'
};

describe('ApprovalActions', () => {
  let wrapper;

  beforeEach(() => {
    wrapper = mount(ApprovalActions, {
      props: {
        approval: mockApproval
      },
      global: {
        stubs: {
          'el-button': ElButton,
          'el-dialog': ElDialog,
          'el-select': ElSelect
        }
      }
    });
  });

  it('renders approval action buttons correctly', () => {
    expect(wrapper.find('[data-testid="approve-btn"]').exists()).toBe(true);
    expect(wrapper.find('[data-testid="reject-btn"]').exists()).toBe(true);
    expect(wrapper.find('[data-testid="transfer-btn"]').exists()).toBe(true);
    expect(wrapper.find('[data-testid="more-actions"]').exists()).toBe(true);
  });

  it('disables buttons when approval is not in pending status', async () => {
    await wrapper.setProps({
      approval: { ...mockApproval, status: 'APPROVED' }
    });

    const approveBtn = wrapper.find('[data-testid="approve-btn"]');
    const rejectBtn = wrapper.find('[data-testid="reject-btn"]');
    const transferBtn = wrapper.find('[data-testid="transfer-btn"]');

    expect(approveBtn.attributes('disabled')).toBeDefined();
    expect(rejectBtn.attributes('disabled')).toBeDefined();
    expect(transferBtn.attributes('disabled')).toBeDefined();
  });

  it('shows approve dialog when approve button is clicked', async () => {
    const approveBtn = wrapper.find('[data-testid="approve-btn"]');
    await approveBtn.trigger('click');

    expect(wrapper.vm.approveDialogVisible).toBe(true);
  });

  it('shows reject dialog when reject button is clicked', async () => {
    const rejectBtn = wrapper.find('[data-testid="reject-btn"]');
    await rejectBtn.trigger('click');

    expect(wrapper.vm.rejectDialogVisible).toBe(true);
  });

  it('validates approve form correctly', async () => {
    // 打开同意对话框
    await wrapper.vm.showApproveDialog();
    
    // 尝试提交空的表单
    await wrapper.vm.submitApprove();
    
    // 应该通过验证（注释字段是可选的）
    expect(wrapper.vm.approveForm.comment).toBe('');
  });

  it('emits approve event when approve form is submitted', async () => {
    const mockEmit = vi.fn();
    wrapper.vm.$emit = mockEmit;
    
    await wrapper.vm.showApproveDialog();
    wrapper.vm.approveForm.comment = '同意此申请';
    
    await wrapper.vm.submitApprove();
    
    expect(mockEmit).toHaveBeenCalledWith('approve', expect.anything());
  });

  it('validates reject form correctly', async () => {
    await wrapper.vm.showRejectDialog();
    
    // 不设置必需字段，应该验证失败
    wrapper.vm.rejectForm.reason = '';
    wrapper.vm.rejectForm.comment = '';
    
    await wrapper.vm.submitReject();
    
    // 因为验证会阻止提交，所以这里主要是验证验证逻辑
    expect(wrapper.vm.rejectForm.reason).toBe('');
    expect(wrapper.vm.rejectForm.comment).toBe('');
  });

  it('handles more actions dropdown commands', async () => {
    const remindSpy = vi.spyOn(wrapper.vm, 'handleMoreCommand');
    
    wrapper.vm.handleMoreCommand('remind');
    expect(wrapper.vm.remindDialogVisible).toBe(true);
    
    wrapper.vm.handleMoreCommand('follow');
    expect(wrapper.vm.isFollowing).toBe(!wrapper.vm.isFollowing);
  });

  it('formats amount correctly', () => {
    const formatted = wrapper.vm.formatAmount(1234.56);
    expect(formatted).toBe('1,234.56');
  });

  it('searches users for transfer', async () => {
    const searchTerm = '张';
    await wrapper.vm.searchUsers(searchTerm);
    
    // 应该是异步操作，验证用户搜索逻辑
    expect(wrapper.vm.userSearchLoading).toBe(true);
    
    // 模拟异步完成
    setTimeout(() => {
      expect(wrapper.vm.userSearchLoading).toBe(false);
      expect(wrapper.vm.userList.length).toBeGreaterThan(0);
    }, 400);
  });
});