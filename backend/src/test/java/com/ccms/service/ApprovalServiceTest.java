package com.ccms.service;

import com.ccms.BaseTest;
import com.ccms.entity.approval.ApprovalConfig;
import com.ccms.entity.approval.ApprovalInstance;
import com.ccms.entity.approval.ApprovalNode;
import com.ccms.repository.ApprovalConfigRepository;
import com.ccms.repository.ApprovalInstanceRepository;
import com.ccms.repository.ApprovalNodeRepository;
import com.ccms.service.impl.ApprovalServiceImpl;
import com.ccms.service.MessageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * 审批服务测试类
 */
@DisplayName("审批服务测试")
class ApprovalServiceTest extends BaseTest {

    @Mock
    private ApprovalConfigRepository approvalConfigRepository;
    
    @Mock
    private ApprovalInstanceRepository approvalInstanceRepository;
    
    @Mock
    private ApprovalNodeRepository approvalNodeRepository;
    
    @Mock
    private MessageService messageService;

    @InjectMocks
    private ApprovalServiceImpl approvalService;

    private ApprovalConfig testApprovalConfig;
    private ApprovalInstance testApprovalInstance;
    private ApprovalNode testApprovalNode;

    @BeforeEach
    public void setUp() {
        super.setUp();
        
        // 创建测试审批配置数据
        testApprovalConfig = new ApprovalConfig();
        testApprovalConfig.setId(1L);
        testApprovalConfig.setName("标准费用申请审批流程");
        testApprovalConfig.setApprovalType("EXPENSE_APPLY");
        testApprovalConfig.setStatus("ENABLED");
        testApprovalConfig.setIsDefault(true);
        testApprovalConfig.setNodeCount(3);
        testApprovalConfig.setCreateTime(LocalDateTime.now());
        testApprovalConfig.setUpdateTime(LocalDateTime.now());

        // 创建测试审批实例数据
        testApprovalInstance = new ApprovalInstance();
        testApprovalInstance.setId(1L);
        testApprovalInstance.setApprovalConfigId(1L);
        testApprovalInstance.setBusinessId("EXP2024010001");
        testApprovalInstance.setBusinessType("EXPENSE_APPLY");
        testApprovalInstance.setStatus("PENDING");
        testApprovalInstance.setCurrentNode(1);
        testApprovalInstance.setApplicant("张三");
        testApprovalInstance.setCreateTime(LocalDateTime.now());

        // 创建测试审批节点数据
        testApprovalNode = new ApprovalNode();
        testApprovalNode.setId(1L);
        testApprovalNode.setApprovalInstanceId(1L);
        testApprovalNode.setNodeOrder(1);
        testApprovalNode.setApproverRole("部门主管");
        testApprovalNode.setStatus("PENDING");
        testApprovalNode.setCreateTime(LocalDateTime.now());
    }

    @Test
    @DisplayName("根据审批类型获取配置成功")
    void getApprovalConfigByType_success() {
        // 设置Mock行为
        when(approvalConfigRepository.findByApprovalType("EXPENSE_APPLY"))
            .thenReturn(Arrays.asList(testApprovalConfig));

        // 执行测试
        List<ApprovalConfig> result = approvalService.getApprovalConfigByType("EXPENSE_APPLY");

        // 验证结果
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getApprovalType()).isEqualTo("EXPENSE_APPLY");
        
        // 验证Mock调用
        verify(approvalConfigRepository, times(1)).findByApprovalType("EXPENSE_APPLY");
    }

    @Test
    @DisplayName("创建审批实例成功")
    void createApprovalInstance_success() {
        // 设置Mock行为
        when(approvalConfigRepository.findByApprovalType("EXPENSE_APPLY"))
            .thenReturn(Arrays.asList(testApprovalConfig));
        when(approvalInstanceRepository.save(any(ApprovalInstance.class)))
            .thenReturn(testApprovalInstance);
        when(approvalNodeRepository.save(any(ApprovalNode.class)))
            .thenReturn(testApprovalNode);

        // 执行测试
        ApprovalInstance result = approvalService.createApprovalInstance(
            "EXP2024010001", 
            "EXPENSE_APPLY", 
            "张三"
        );

        // 验证结果
        assertThat(result).isNotNull();
        assertThat(result.getBusinessType()).isEqualTo("EXPENSE_APPLY");
        
        // 验证Mock调用
        verify(approvalInstanceRepository, times(1)).save(any(ApprovalInstance.class));
    }

    @Test
    @DisplayName("获取待办审批列表")
    void getPendingApprovals_success() {
        // 创建测试数据
        List<ApprovalInstance> instances = Arrays.asList(testApprovalInstance);
        
        // 设置Mock行为
        when(approvalInstanceRepository.findByStatusAndApprover("PENDING", "李四"))
            .thenReturn(instances);

        // 执行测试
        List<ApprovalInstance> result = approvalService.getPendingApprovals("李四");

        // 验证结果
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStatus()).isEqualTo("PENDING");
        
        // 验证Mock调用
        verify(approvalInstanceRepository, times(1)).findByStatusAndApprover("PENDING", "李四");
    }

    @Test
    @DisplayName("处理审批 - 同意")
    void approve_success() {
        // 设置Mock行为
        when(approvalInstanceRepository.findById(1L)).thenReturn(Optional.of(testApprovalInstance));
        when(approvalNodeRepository.findByApprovalInstanceIdAndNodeOrder(1L, 1))
            .thenReturn(Optional.of(testApprovalNode));
        when(approvalInstanceRepository.save(any(ApprovalInstance.class)))
            .thenReturn(testApprovalInstance);
        when(approvalNodeRepository.save(any(ApprovalNode.class)))
            .thenReturn(testApprovalNode);

        // 执行测试
        boolean result = approvalService.approve(1L, 1L, "李四", "同意");

        // 验证结果
        assertThat(result).isTrue();
        
        // 验证Mock调用
        verify(approvalInstanceRepository, times(1)).save(any(ApprovalInstance.class));
        verify(messageService, times(1)).sendApprovalNotification(anyString(), anyString());
    }

    @Test
    @DisplayName("处理审批 - 驳回")
    void reject_success() {
        // 设置Mock行为
        when(approvalInstanceRepository.findById(1L)).thenReturn(Optional.of(testApprovalInstance));
        when(approvalNodeRepository.findByApprovalInstanceIdAndNodeOrder(1L, 1))
            .thenReturn(Optional.of(testApprovalNode));
        when(approvalInstanceRepository.save(any(ApprovalInstance.class)))
            .thenReturn(testApprovalInstance);
        when(approvalNodeRepository.save(any(ApprovalNode.class)))
            .thenReturn(testApprovalNode);

        // 执行测试
        boolean result = approvalService.reject(1L, 1L, "李四", "信息不完整");

        // 验证结果
        assertThat(result).isTrue();
        
        // 验证Mock调用
        verify(approvalInstanceRepository, times(1)).save(any(ApprovalInstance.class));
    }

    @Test
    @DisplayName("获取审批历史")
    void getApprovalHistory_success() {
        // 修改测试实例状态为完成
        testApprovalInstance.setStatus("COMPLETED");
        
        // 创建测试数据
        List<ApprovalInstance> instances = Arrays.asList(testApprovalInstance);
        
        // 设置Mock行为
        when(approvalInstanceRepository.findByBusinessId("EXP2024010001"))
            .thenReturn(instances);

        // 执行测试
        List<ApprovalInstance> result = approvalService.getApprovalHistory("EXP2024010001");

        // 验证结果
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getBusinessId()).isEqualTo("EXP2024010001");
        
        // 验证Mock调用
        verify(approvalInstanceRepository, times(1)).findByBusinessId("EXP2024010001");
    }

    @Test
    @DisplayName("获取审批节点信息")
    void getApprovalNodes_success() {
        // 创建测试数据
        List<ApprovalNode> nodes = Arrays.asList(testApprovalNode);
        
        // 设置Mock行为
        when(approvalNodeRepository.findByApprovalInstanceId(1L))
            .thenReturn(nodes);

        // 执行测试
        List<ApprovalNode> result = approvalService.getApprovalNodes(1L);

        // 验证结果
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getApproverRole()).isEqualTo("部门主管");
        
        // 验证Mock调用
        verify(approvalNodeRepository, times(1)).findByApprovalInstanceId(1L);
    }

    @Test
    @DisplayName("催办审批")
    void remindApproval_success() {
        // 设置Mock行为
        when(approvalInstanceRepository.findById(1L)).thenReturn(Optional.of(testApprovalInstance));

        // 执行测试
        boolean result = approvalService.remindApproval(1L, "请尽快处理");

        // 验证结果
        assertThat(result).isTrue();
        
        // 验证Mock调用
        verify(messageService, times(1)).sendRemindNotification(anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("转审处理")
    void transferApproval_success() {
        // 设置Mock行为
        when(approvalInstanceRepository.findById(1L)).thenReturn(Optional.of(testApprovalInstance));
        when(approvalNodeRepository.findByApprovalInstanceIdAndNodeOrder(1L, 1))
            .thenReturn(Optional.of(testApprovalNode));
        when(approvalNodeRepository.save(any(ApprovalNode.class)))
            .thenReturn(testApprovalNode);

        // 执行测试
        boolean result = approvalService.transferApproval(1L, 1L, "李四", "已出差，转李四处理");

        // 验证结果
        assertThat(result).isTrue();
        
        // 验证Mock调用
        verify(approvalNodeRepository, times(1)).save(any(ApprovalNode.class));
    }

    @Test
    @DisplayName("获取审批进度")
    void getApprovalProgress_success() {
        // 创建测试节点数据
        testApprovalNode.setStatus("APPROVED");
        List<ApprovalNode> nodes = Arrays.asList(testApprovalNode);
        
        // 设置Mock行为
        when(approvalInstanceRepository.findById(1L)).thenReturn(Optional.of(testApprovalInstance));
        when(approvalNodeRepository.findByApprovalInstanceId(1L)).thenReturn(nodes);

        // 执行测试
        String progress = approvalService.getApprovalProgress(1L);

        // 验证结果
        assertThat(progress).isNotNull();
        // 这里可以实现具体的进度计算逻辑验证
    }

    @Test
    @DisplayName("取消审批实例")
    void cancelApprovalInstance_success() {
        // 设置Mock行为
        when(approvalInstanceRepository.findById(1L)).thenReturn(Optional.of(testApprovalInstance));
        when(approvalInstanceRepository.save(any(ApprovalInstance.class)))
            .thenReturn(testApprovalInstance);

        // 执行测试
        boolean result = approvalService.cancelApprovalInstance(1L, "申请人取消");

        // 验证结果
        assertThat(result).isTrue();
        
        // 验证Mock调用
        verify(approvalInstanceRepository, times(1)).save(any(ApprovalInstance.class));
    }
}