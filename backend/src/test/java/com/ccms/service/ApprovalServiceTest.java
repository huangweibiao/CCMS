package com.ccms.service;

import com.ccms.entity.ApprovalFlowConfig;
import com.ccms.entity.ApprovalInstance;
import com.ccms.entity.ApprovalTask;
import com.ccms.repository.ApprovalFlowConfigRepository;
import com.ccms.repository.ApprovalInstanceRepository;
import com.ccms.repository.ApprovalTaskRepository;
import com.ccms.service.impl.ApprovalServiceImpl;
import com.ccms.service.impl.BudgetServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * 审批服务单元测试
 */
@ExtendWith(MockitoExtension.class)
class ApprovalServiceTest {

    @Mock
    private ApprovalFlowConfigRepository approvalFlowConfigRepository;

    @Mock
    private ApprovalInstanceRepository approvalInstanceRepository;

    @Mock
    private ApprovalTaskRepository approvalTaskRepository;

    @Mock
    private BudgetServiceImpl budgetService;

    @InjectMocks
    private ApprovalServiceImpl approvalService;

    private ApprovalFlowConfig budgetFlowConfig;
    private ApprovalFlowConfig expenseFlowConfig;
    private ApprovalInstance submittedInstance;
    private ApprovalInstance approvedInstance;
    private ApprovalTask pendingTask;
    private ApprovalTask completedTask;

    @BeforeEach
    void setUp() {
        // 创建预算审批流程配置
        budgetFlowConfig = new ApprovalFlowConfig();
        budgetFlowConfig.setId(1L);
        budgetFlowConfig.setFlowType("BUDGET");
        budgetFlowConfig.setFlowName("预算审批流程");
        budgetFlowConfig.setDeptId(101L);
        budgetFlowConfig.setAmountLimitStart(100000L);
        budgetFlowConfig.setAmountLimitEnd(500000L);
        budgetFlowConfig.setApprovalFlow("1,2,3"); // 三级审批
        budgetFlowConfig.setAutoApprovalEnabled(true);

        // 创建费用审批流程配置
        expenseFlowConfig = new ApprovalFlowConfig();
        expenseFlowConfig.setId(2L);
        expenseFlowConfig.setFlowType("EXPENSE");
        expenseFlowConfig.setFlowName("费用报销审批流程");
        expenseFlowConfig.setAutoApprovalEnabled(false);

        // 创建已提交的审批实例
        submittedInstance = new ApprovalInstance();
        submittedInstance.setId(1L);
        submittedInstance.setBusinessType("BUDGET");
        submittedInstance.setBusinessId(1001L);
        submittedInstance.setInstanceNo("AP20250001");
        submittedInstance.setApplicantId(2001L);
        submittedInstance.setDeptId(101L);
        submittedInstance.setCurrentStep(1);
        submittedInstance.setTotalSteps(3);
        submittedInstance.setStatus(1); // 审批中
        submittedInstance.setCreateTime(LocalDateTime.now());

        // 创建已批准的审批实例
        approvedInstance = new ApprovalInstance();
        approvedInstance.setId(2L);
        approvedInstance.setBusinessType("EXPENSE");
        approvedInstance.setBusinessId(1002L);
        approvedInstance.setStatus(3); // 已批准
        approvedInstance.setCurrentStep(3);
        approvedInstance.setTotalSteps(3);

        // 创建待办审批任务
        pendingTask = new ApprovalTask();
        pendingTask.setId(1L);
        pendingTask.setInstanceId(1L);
        pendingTask.setStep(1);
        pendingTask.setApproverId(3001L);
        pendingTask.setTaskStatus(1); // 待审批
        pendingTask.setCreateTime(LocalDateTime.now());

        // 创建已完成的审批任务
        completedTask = new ApprovalTask();
        completedTask.setId(2L);
        completedTask.setInstanceId(1L);
        completedTask.setStep(1);
        completedTask.setTaskStatus(3); // 已批准
        completedTask.setApprovalComment("同意");
        completedTask.setApprovalTime(LocalDateTime.now());
    }

    @Test
    void testCreateApprovalFlow_Success() {
        // 模拟Repository保存
        when(approvalFlowConfigRepository.save(any(ApprovalFlowConfig.class))).thenReturn(budgetFlowConfig);

        // 执行创建审批流程
        ApprovalFlowConfig result = approvalService.createApprovalFlow(budgetFlowConfig);

        // 验证结果
        assertNotNull(result);
        assertEquals("预算审批流程", result.getFlowName());
        assertEquals("BUDGET", result.getFlowType());
        
        verify(approvalFlowConfigRepository, times(1)).save(any(ApprovalFlowConfig.class));
    }

    @Test
    void testStartApprovalProcess_Success() {
        // 模拟Repository查找和保存
        when(approvalFlowConfigRepository.findByFlowTypeAndDeptId("BUDGET", 101L))
                .thenReturn(Optional.of(budgetFlowConfig));
        when(approvalInstanceRepository.save(any(ApprovalInstance.class))).thenReturn(submittedInstance);
        when(approvalTaskRepository.save(any(ApprovalTask.class))).thenReturn(pendingTask);

        // 执行启动审批流程
        ApprovalInstance result = approvalService.startApprovalProcess("BUDGET", 1001L, 2001L, 101L);

        // 验证结果
        assertNotNull(result);
        assertEquals("BUDGET", result.getBusinessType());
        assertEquals(1, result.getCurrentStep().intValue());
        assertEquals(3, result.getTotalSteps().intValue());
        
        verify(approvalFlowConfigRepository, times(1)).findByFlowTypeAndDeptId("BUDGET", 101L);
        verify(approvalInstanceRepository, times(1)).save(any(ApprovalInstance.class));
        verify(approvalTaskRepository, times(1)).save(any(ApprovalTask.class));
    }

    @Test
    void testStartApprovalProcess_FlowConfigNotFound() {
        // 模拟Repository返回空
        when(approvalFlowConfigRepository.findByFlowTypeAndDeptId("BUDGET", 999L))
                .thenReturn(Optional.empty());

        // 验证异常
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            approvalService.startApprovalProcess("BUDGET", 1001L, 2001L, 999L);
        });

        assertEquals("未找到对应的审批流程配置", exception.getMessage());
        verify(approvalFlowConfigRepository, times(1)).findByFlowTypeAndDeptId("BUDGET", 999L);
        verify(approvalInstanceRepository, never()).save(any(ApprovalInstance.class));
    }

    @Test
    void testHandleAutoApproval_WhenEnabled() {
        // 设置自动审批为true
        budgetFlowConfig.setAutoApprovalEnabled(true);
        
        // 模拟Repository查找和保存
        when(approvalInstanceRepository.findByBusinessTypeAndBusinessId("BUDGET", 1001L))
                .thenReturn(Optional.of(submittedInstance));
        when(approvalTaskRepository.findByInstanceIdAndStep(1L, 1)).thenReturn(pendingTask);
        when(approvalTaskRepository.save(any(ApprovalTask.class))).thenReturn(pendingTask);
        when(approvalInstanceRepository.save(any(ApprovalInstance.class))).thenReturn(submittedInstance);

        // 执行自动审批处理
        boolean result = approvalService.handleAutoApproval("BUDGET", 1001L, 100000L);

        // 验证结果
        assertTrue(result);
        verify(approvalInstanceRepository, times(1)).findByBusinessTypeAndBusinessId("BUDGET", 1001L);
        verify(approvalTaskRepository, times(1)).findByInstanceIdAndStep(1L, 1);
        verify(approvalTaskRepository, times(1)).save(any(ApprovalTask.class));
    }

    @Test
    void testHandleAutoApproval_WhenDisabled() {
        // 设置自动审批为false
        budgetFlowConfig.setAutoApprovalEnabled(false);
        
        // 模拟Repository查找
        when(approvalInstanceRepository.findByBusinessTypeAndBusinessId("BUDGET", 1001L))
                .thenReturn(Optional.of(submittedInstance));

        // 执行自动审批处理
        boolean result = approvalService.handleAutoApproval("BUDGET", 1001L, 100000L);

        // 验证结果
        assertFalse(result);
        verify(approvalInstanceRepository, times(1)).findByBusinessTypeAndBusinessId("BUDGET", 1001L);
        verify(approvalTaskRepository, never()).findByInstanceIdAndStep(anyLong(), anyInt());
    }

    @Test
    void testApproveTask_Success() {
        // 模拟Repository查找和保存
        when(approvalTaskRepository.findById(1L)).thenReturn(Optional.of(pendingTask));
        when(approvalInstanceRepository.findById(1L)).thenReturn(Optional.of(submittedInstance));
        when(approvalTaskRepository.save(any(ApprovalTask.class))).thenReturn(pendingTask);
        when(approvalInstanceRepository.save(any(ApprovalInstance.class))).thenReturn(submittedInstance);

        // 执行审批任务
        boolean result = approvalService.approveTask(1L, true, "同意", 3001L);

        // 验证结果
        assertTrue(result);
        assertEquals(3, pendingTask.getTaskStatus().intValue()); // 已批准
        assertEquals("同意", pendingTask.getApprovalComment());
        assertNotNull(pendingTask.getApprovalTime());
        
        verify(approvalTaskRepository, times(1)).findById(1L);
        verify(approvalInstanceRepository, times(1)).findById(1L);
        verify(approvalTaskRepository, times(1)).save(any(ApprovalTask.class));
    }

    @Test
    void testApproveTask_TaskNotFound() {
        // 模拟Repository返回空
        when(approvalTaskRepository.findById(999L)).thenReturn(Optional.empty());

        // 验证异常
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            approvalService.approveTask(999L, true, "同意", 3001L);
        });

        assertEquals("审批任务不存在", exception.getMessage());
        verify(approvalTaskRepository, times(1)).findById(999L);
        verify(approvalInstanceRepository, never()).findById(anyLong());
    }

    @Test
    void testApproveTask_InstanceNotFound() {
        // 模拟Repository查找任务成功，审批实例失败
        when(approvalTaskRepository.findById(1L)).thenReturn(Optional.of(pendingTask));
        when(approvalInstanceRepository.findById(1L)).thenReturn(Optional.empty());

        // 验证异常
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            approvalService.approveTask(1L, true, "同意", 3001L);
        });

        assertEquals("审批实例不存在", exception.getMessage());
        verify(approvalTaskRepository, times(1)).findById(1L);
        verify(approvalInstanceRepository, times(1)).findById(1L);
    }

    @Test
    void testCreateNextApprovalTask_Success() {
        // 模拟Repository查找和保存
        when(approvalInstanceRepository.findById(1L)).thenReturn(Optional.of(submittedInstance));
        when(approvalTaskRepository.save(any(ApprovalTask.class))).thenReturn(pendingTask);

        // 执行创建下一个审批任务
        ApprovalTask result = approvalService.createNextApprovalTask(1L, 2, 3002L);

        // 验证结果
        assertNotNull(result);
        assertEquals(1L, result.getInstanceId());
        assertEquals(2, result.getStep().intValue());
        assertEquals(3002L, result.getApproverId().longValue());
        
        verify(approvalInstanceRepository, times(1)).findById(1L);
        verify(approvalTaskRepository, times(1)).save(any(ApprovalTask.class));
    }

    @Test
    void testCompleteApprovalProcess_Success() {
        // 模拟Repository保存
        when(approvalInstanceRepository.save(any(ApprovalInstance.class))).thenReturn(submittedInstance);

        // 执行完成审批流程
        boolean result = approvalService.completeApprovalProcess(1L, 3); // 已批准

        // 验证结果
        assertTrue(result);
        assertEquals(3, submittedInstance.getStatus().intValue());
        assertNotNull(submittedInstance.getCompleteTime());
        
        verify(approvalInstanceRepository, times(1)).save(any(ApprovalInstance.class));
    }

    @Test
    void testGetPendingApprovals_Success() {
        // 模拟Repository返回任务列表
        when(approvalTaskRepository.findByApproverIdAndTaskStatus(3001L, 1))
                .thenReturn(Arrays.asList(pendingTask));

        // 执行获取待办审批
        List<ApprovalTask> result = approvalService.getPendingApprovals(3001L);

        // 验证结果
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getId().longValue());
        
        verify(approvalTaskRepository, times(1)).findByApproverIdAndTaskStatus(3001L, 1);
    }

    @Test
    void testGetApprovalHistory_Success() {
        // 模拟Repository返回历史列表
        when(approvalTaskRepository.findByInstanceIdOrderByCreateTimeDesc(1L))
                .thenReturn(Arrays.asList(completedTask, pendingTask));

        // 执行获取审批历史
        List<ApprovalTask> result = approvalService.getApprovalHistory(1L);

        // 验证结果
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(2L, result.get(0).getId().longValue()); // 按时间倒序排列
        
        verify(approvalTaskRepository, times(1)).findByInstanceIdOrderByCreateTimeDesc(1L);
    }

    @Test
    void testGetApprovalInstanceByBusinessId_Found() {
        // 模拟Repository返回实例
        when(approvalInstanceRepository.findByBusinessTypeAndBusinessId("BUDGET", 1001L))
                .thenReturn(Optional.of(submittedInstance));

        // 执行查询
        ApprovalInstance result = approvalService.getApprovalInstanceByBusinessId("BUDGET", 1001L);

        // 验证结果
        assertNotNull(result);
        assertEquals(1L, result.getId().longValue());
        assertEquals("BUDGET", result.getBusinessType());
        
        verify(approvalInstanceRepository, times(1)).findByBusinessTypeAndBusinessId("BUDGET", 1001L);
    }

    @Test
    void testGetApprovalInstanceByBusinessId_NotFound() {
        // 模拟Repository返回空
        when(approvalInstanceRepository.findByBusinessTypeAndBusinessId("BUDGET", 999L))
                .thenReturn(Optional.empty());

        // 执行查询
        ApprovalInstance result = approvalService.getApprovalInstanceByBusinessId("BUDGET", 999L);

        // 验证结果
        assertNull(result);
        verify(approvalInstanceRepository, times(1)).findByBusinessTypeAndBusinessId("BUDGET", 999L);
    }

    @Test
    void testCheckApprovalStatus_WhenCompleted() {
        // 模拟Repository查找流程
        when(approvalInstanceRepository.findByBusinessTypeAndBusinessId("BUDGET", 1001L))
                .thenReturn(Optional.of(approvedInstance));

        // 执行检查审批状态
        int result = approvalService.checkApprovalStatus("BUDGET", 1001L);

        // 验证结果
        assertEquals(3, result); // 已批准
        verify(approvalInstanceRepository, times(1)).findByBusinessTypeAndBusinessId("BUDGET", 1001L);
    }

    @Test
    void testCheckApprovalStatus_WhenNoInstance() {
        // 模拟Repository返回空
        when(approvalInstanceRepository.findByBusinessTypeAndBusinessId("BUDGET", 999L))
                .thenReturn(Optional.empty());

        // 执行检查审批状态
        int result = approvalService.checkApprovalStatus("BUDGET", 999L);

        // 验证结果
        assertEquals(0, result); // 未提交审批
        verify(approvalInstanceRepository, times(1)).findByBusinessTypeAndBusinessId("BUDGET", 999L);
    }

    @Test
    void testGetApprovalStatistics_Success() {
        // 模拟Repository返回统计信息
        when(approvalTaskRepository.countByApproverIdAndTaskStatus(3001L, 1)).thenReturn(5L);
        when(approvalTaskRepository.countByApproverIdAndTaskStatus(3001L, 3)).thenReturn(20L);
        when(approvalTaskRepository.countByApproverIdAndTaskStatus(3001L, 4)).thenReturn(2L);

        // 执行获取审批统计
        var result = approvalService.getApprovalStatistics(3001L);

        // 验证结果
        assertNotNull(result);
        assertEquals(5L, result.getPendingCount().longValue());
        assertEquals(20L, result.getApprovedCount().longValue());
        assertEquals(2L, result.getRejectedCount().longValue());
        
        verify(approvalTaskRepository, times(1)).countByApproverIdAndTaskStatus(3001L, 1);
        verify(approvalTaskRepository, times(1)).countByApproverIdAndTaskStatus(3001L, 3);
        verify(approvalTaskRepository, times(1)).countByApproverIdAndTaskStatus(3001L, 4);
    }

    @Test
    void testCancelApprovalProcess_Success() {
        // 模拟Repository查找和保存
        when(approvalInstanceRepository.findById(1L)).thenReturn(Optional.of(submittedInstance));
        when(approvalTaskRepository.save(any(ApprovalTask.class))).thenReturn(pendingTask);
        when(approvalInstanceRepository.save(any(ApprovalInstance.class))).thenReturn(submittedInstance);

        // 执行取消审批流程
        boolean result = approvalService.cancelApprovalProcess(1L, "申请人撤销");

        // 验证结果
        assertTrue(result);
        assertEquals(6, submittedInstance.getStatus().intValue()); // 已撤销
        assertEquals("申请人撤销", submittedInstance.getCancelReason());
        
        verify(approvalInstanceRepository, times(1)).findById(1L);
        verify(approvalTaskRepository, times(1)).save(any(ApprovalTask.class));
        verify(approvalInstanceRepository, times(1)).save(any(ApprovalInstance.class));
    }

    @Test
    void testValidateApprovalFlowConfig_Valid() {
        // 创建有效的审批流程配置
        ApprovalFlowConfig validConfig = new ApprovalFlowConfig();
        validConfig.setFlowType("BUDGET");
        validConfig.setFlowName("预算审批");
        validConfig.setApprovalFlow("1001,1002,1003");

        // 执行验证
        boolean result = approvalService.validateApprovalFlowConfig(validConfig);

        // 验证结果
        assertTrue(result);
    }

    @Test
    void testValidateApprovalFlowConfig_InvalidFlowType() {
        // 创建无效的审批流程配置（缺少流程类型）
        ApprovalFlowConfig invalidConfig = new ApprovalFlowConfig();
        invalidConfig.setFlowName("预算审批");
        invalidConfig.setApprovalFlow("1001,1002,1003");

        // 执行验证
        boolean result = approvalService.validateApprovalFlowConfig(invalidConfig);

        // 验证结果
        assertFalse(result);
    }

    @Test
    void testValidateApprovalFlowConfig_InvalidApprovalFlow() {
        // 创建无效的审批流程配置（审批流程为空）
        ApprovalFlowConfig invalidConfig = new ApprovalFlowConfig();
        invalidConfig.setFlowType("BUDGET");
        invalidConfig.setFlowName("预算审批");
        invalidConfig.setApprovalFlow("");

        // 执行验证
        boolean result = approvalService.validateApprovalFlowConfig(invalidConfig);

        // 验证结果
        assertFalse(result);
    }
}