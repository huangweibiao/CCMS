# 审批流程模块使用指南

## 概述

企业级费控管理系统（CCMS）的审批流程模块提供了一个完整、灵活且可配置的审批解决方案。本指南详细介绍模块的使用方法、配置步骤和最佳实践。

## 快速开始

### 1. 环境准备

确保系统已正确配置审批模块依赖：

```yaml
# application.yml 配置示例
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/ccms
    username: ccms_user
    password: your_password
  
  redis:
    host: localhost
    port: 6379
    
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true

# 审批模块配置
ccms:
  approval:
    async-enabled: true
    audit-enabled: true
    cache-enabled: true
```

### 2. 数据表初始化

系统启动时会自动创建必要的数据库表：
- `ccms_approval_flow_config` - 审批流程配置表
- `ccms_approval_instance` - 审批实例表
- `ccms_approval_node` - 审批节点表
- `ccms_approval_record` - 审批记录表
- `ccms_approval_audit_log` - 审计日志表

## 核心功能使用

### 1. 审批流程配置

#### 1.1 创建审批流程配置

```java
// 创建小额报销审批流程
ApprovalFlowConfigRequest request = ApprovalFlowConfigRequest.builder()
    .flowName("小额报销审批流程")
    .businessType("EXPENSE_APPROVAL")
    .approvalType(ApprovalType.SEQUENTIAL)
    .category("报销")
    .minAmount(BigDecimal.ZERO)
    .maxAmount(new BigDecimal("500"))
    .description("小额日常报销审批流程")
    .build();

// 调用API创建配置
ResponseEntity<ApiResponse<ApprovalFlowConfig>> response = 
    restTemplate.postForEntity("/api/approval/configs", request, ApiResponse.class);
```

#### 1.2 配置审批节点

```java
// 为主管审批添加节点
ApprovalNodeRequest nodeRequest = ApprovalNodeRequest.builder()
    .flowConfigId(configId)
    .nodeName("主管审批")
    .nodeType("APPROVAL")
    .approverIds(Arrays.asList(managerId1, managerId2))
    .approvalStrategy("OR")  // 任一审批人通过即可
    .nodeOrder(1)
    .conditions("amount >= 100 && amount < 5000")
    .build();
```

### 2. 审批实例管理

#### 2.1 创建审批申请

```java
// 创建报销申请
ExpenseApprovalDto expenseDto = ExpenseApprovalDto.builder()
    .businessType("EXPENSE_APPROVAL")
    .businessTitle("交通费报销申请")
    .applicantId(1001L)
    .departmentId(101L)
    .amount(new BigDecimal("150.50"))
    .description("本月交通费用报销")
    .expenseDate(LocalDateTime.now())
    .build();

// 调用审批实例创建接口
ResponseEntity<ApiResponse<ApprovalInstance>> response = 
    restTemplate.postForEntity("/api/approval/instances", expenseDto, ApiResponse.class);
```

#### 2.2 提交审批

```java
// 提交审批申请
ResponseEntity<ApiResponse<ApprovalInstance>> response = 
    restTemplate.postForEntity("/api/approval/instances/{instanceId}/submit", 
        null, ApiResponse.class, instanceId);
```

### 3. 审批操作

#### 3.1 审批通过

```java
ApprovalActionDto approveAction = ApprovalActionDto.builder()
    .actionType(ApprovalAction.APPROVE)
    .comment("单据符合规范，同意报销")
    .approverId(approverId)
    .build();

ResponseEntity<ApiResponse<ApprovalInstance>> response = 
    restTemplate.postForEntity("/api/approval/instances/{instanceId}/approve", 
        approveAction, ApiResponse.class, instanceId);
```

#### 3.2 审批驳回

```java
ApprovalActionDto rejectAction = ApprovalActionDto.builder()
    .actionType(ApprovalAction.REJECT)
    .comment("单据填写不规范，请补充说明")
    .approverId(approverId)
    .build();
```

#### 3.3 审批转审

```java
ApprovalActionDto transferAction = ApprovalActionDto.builder()
    .actionType(ApprovalAction.TRANSFER)
    .comment("此申请涉及专业领域，转相关部门审批")
    .approverId(currentApproverId)
    .nextApproverId(nextApproverId)
    .build();
```

## 业务集成示例

### 1. 报销业务集成

```java
@Service
public class ExpenseReimburseService {
    
    @Autowired
    private ApprovalService approvalService;
    
    /**
     * 创建报销申请并启动审批流程
     */
    public void createExpenseWithApproval(ExpenseApplyMain expenseApply) {
        // 1. 创建审批实例
        ApprovalInstance approvalInstance = approvalService.createApprovalInstance(
            expenseApply.toApprovalDto());
        
        // 2. 关联业务数据
        expenseApply.setApprovalInstanceId(approvalInstance.getId());
        expenseApply.setApprovalStatus(ApprovalStatus.DRAFT);
        
        // 3. 保存业务数据
        expenseApplyMainRepository.save(expenseApply);
    }
    
    /**
     * 提交报销审批
     */
    public void submitExpenseApproval(Long expenseId) {
        ExpenseApplyMain expenseApply = expenseApplyMainRepository.findById(expenseId)
            .orElseThrow(() -> new BusinessException("报销申请不存在"));
        
        // 提交审批
        approvalService.submitApproval(expenseApply.getApprovalInstanceId());
        
        // 更新业务状态
        expenseApply.setApprovalStatus(ApprovalStatus.APPROVING);
        expenseApplyMainRepository.save(expenseApply);
    }
}
```

### 2. 借款业务集成

```java
@Service
public class LoanService {
    
    @Autowired
    private ApprovalService approvalService;
    
    /**
     * 借款审批完成回调
     */
    @Async("approvalTaskExecutor")
    public void onApprovalCompleted(Long instanceId, ApprovalStatus status) {
        // 根据审批实例ID找到对应的借款申请
        LoanApply loanApply = loanApplyRepository.findByApprovalInstanceId(instanceId)
            .orElseThrow(() -> new BusinessException("借款申请不存在"));
        
        // 更新借款申请状态
        loanApply.setApprovalStatus(status);
        loanApply.setApprovalCompleteTime(LocalDateTime.now());
        
        if (status == ApprovalStatus.COMPLETED) {
            // 审批通过，执行放款操作
            executeLoanDisbursement(loanApply);
        } else if (status == ApprovalStatus.REJECTED) {
            // 审批驳回，发送通知
            sendRejectionNotification(loanApply);
        }
        
        loanApplyRepository.save(loanApply);
    }
}
```

## 高级配置

### 1. 自定义审批策略

```java
@Component
public class CustomApprovalStrategy implements ApprovalStrategy {
    
    @Override
    public ApprovalResult execute(ApprovalContext context) {
        // 自定义审批逻辑
        if (isUrgentBusiness(context)) {
            // 紧急业务加速审批
            return accelerateApproval(context);
        } else if (isHighRiskBusiness(context)) {
            // 高风险业务多级审批
            return multiLevelApproval(context);
        }
        
        // 默认策略
        return defaultApproval(context);
    }
    
    private boolean isUrgentBusiness(ApprovalContext context) {
        // 判断是否为紧急业务
        return "URGENT".equals(context.getBusinessPriority());
    }
}
```

### 2. 审批条件配置

#### 2.1 金额条件

```sql
-- 审批节点条件示例
amount >= 100 AND amount < 5000  -- 小额审批
amount >= 5000 AND amount < 20000  -- 中额审批
amount >= 20000  -- 大额审批
```

#### 2.2 业务条件

```sql
-- 报销类型条件
departmentId IN (101, 102, 103)  -- 特定部门
businessSubType = 'TRAVEL'  -- 差旅报销
expenseDate >= '2024-01-01'  -- 时间范围
```

### 3. 审批人配置策略

#### 3.1 固定审批人

```java
// 配置固定的审批人列表
ApprovalNodeRequest nodeRequest = ApprovalNodeRequest.builder()
    .approverIds(Arrays.asList(1001L, 1002L, 1003L))  // 固定的审批人ID
    .approvalStrategy("OR")  // 任一审批人通过即可
    .build();
```

#### 3.2 动态审批人

```java
// 根据业务规则动态确定审批人
public class DynamicApproverService {
    
    public List<Long> determineApprovers(ApprovalContext context) {
        List<Long> approvers = new ArrayList<>();
        
        // 根据部门确定主管
        approvers.add(getDepartmentManager(context.getDepartmentId()));
        
        // 根据金额确定是否需要财务审批
        if (context.getAmount().compareTo(new BigDecimal("5000")) >= 0) {
            approvers.add(getFinanceManager());
        }
        
        // 根据业务类型确定专业审批人
        if ("SPECIAL_PROJECT".equals(context.getBusinessType())) {
            approvers.add(getProjectDirector());
        }
        
        return approvers;
    }
}
```

## 监控和运维

### 1. 性能监控

```java
@Component
public class ApprovalPerformanceMonitor {
    
    private final MeterRegistry meterRegistry;
    
    public ApprovalPerformanceMonitor(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }
    
    /**
     * 记录审批操作性能指标
     */
    public void recordApprovalOperation(String operation, Duration duration, boolean success) {
        Timer.builder("approval.operation.duration")
            .tag("operation", operation)
            .tag("success", String.valueOf(success))
            .register(meterRegistry)
            .record(duration);
        
        Counter.builder("approval.operation.count")
            .tag("operation", operation)
            .tag("success", String.valueOf(success))
            .register(meterRegistry)
            .increment();
    }
    
    /**
     * 监控审批队列状态
     */
    public void monitorApprovalQueue() {
        long pendingCount = approvalInstanceRepository.countByStatus(ApprovalStatus.APPROVING);
        Gauge.builder("approval.queue.pending", () -> pendingCount)
            .register(meterRegistry);
    }
}
```

### 2. 审计日志配置

```yaml
# 审计日志配置
logging:
  level:
    com.ccms.approval: DEBUG
  
  file:
    name: logs/approval-audit.log
    
  logback:
    rollingpolicy:
      max-file-size: 100MB
      max-history: 30

# 审计日志格式配置
ccms:
  audit:
    enabled: true
    level: INFO
    include-request-body: true
    include-response-body: false
    max-payload-length: 10000
```

### 3. 异常处理配置

```java
@Configuration
public class ApprovalExceptionConfig {
    
    @Bean
    public ApprovalErrorHandler approvalErrorHandler() {
        return new ApprovalErrorHandler();
    }
    
    @Bean
    public ApprovalRetryPolicy approvalRetryPolicy() {
        return ApprovalRetryPolicy.builder()
            .maxAttempts(3)
            .backoffDelay(1000)
            .maxDelay(10000)
            .build();
    }
}

@Component
public class ApprovalErrorHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(ApprovalErrorHandler.class);
    
    /**
     * 处理审批操作异常
     */
    public void handleApprovalError(ApprovalException exception, ApprovalContext context) {
        logger.error("审批操作异常 - 实例ID: {}, 操作: {}, 错误: {}", 
            context.getInstanceId(), context.getAction(), exception.getMessage(), exception);
        
        // 记录错误审计日志
        auditLogService.logError(context, exception);
        
        // 发送异常通知
        notificationService.sendErrorNotification(context, exception);
        
        // 根据错误类型执行恢复操作
        if (exception instanceof TransientApprovalException) {
            handleTransientError(context, exception);
        } else if (exception instanceof BusinessApprovalException) {
            handleBusinessError(context, exception);
        }
    }
}
```

## 最佳实践

### 1. 审批流程设计原则

#### 1.1 流程简洁性
- 避免过度复杂的审批流程
- 每个审批节点应有明确的责任人
- 流程层级不宜超过5级

#### 1.2 审批时效性
- 设置合理的审批时限
- 实现超时自动处理机制
- 提供审批提醒功能

#### 1.3 权限分离
- 申请人与审批人角色分离
- 审批人与配置管理员角色分离
- 审计人员独立权限

### 2. 性能优化建议

#### 2.1 数据库优化
```sql
-- 关键索引配置
CREATE INDEX idx_approval_instance_status ON ccms_approval_instance(status);
CREATE INDEX idx_approval_instance_applicant ON ccms_approval_instance(applicant_id);
CREATE INDEX idx_approval_record_instance ON ccms_approval_record(instance_id);
CREATE INDEX idx_approval_audit_log_time ON ccms_approval_audit_log(log_time);
```

#### 2.2 缓存策略
```java
@Configuration
@EnableCaching
public class ApprovalCacheConfig {
    
    @Bean
    public CacheManager approvalCacheManager() {
        return new RedisCacheManager(redisTemplate());
    }
    
    @Bean
    public Cache approvalFlowConfigCache() {
        return new RedisCache("approval-flow-config", redisTemplate());
    }
    
    @Bean
    public Cache approvalInstanceCache() {
        return new RedisCache("approval-instance", redisTemplate());
    }
}
```

### 3. 安全配置

#### 3.1 权限验证
```java
@Aspect
@Component
public class ApprovalSecurityAspect {
    
    @Before("@annotation(RequiresApprovalPermission)")
    public void checkApprovalPermission(JoinPoint joinPoint) {
        // 验证用户是否有审批权限
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Long userId = getUserIdFromAuthentication(authentication);
        
        Object[] args = joinPoint.getArgs();
        Long instanceId = (Long) args[0];
        
        if (!approvalPermissionService.hasPermission(userId, instanceId)) {
            throw new AccessDeniedException("无审批操作权限");
        }
    }
}
```

#### 3.2 数据脱敏
```java
@Component
public class ApprovalDataMaskingService {
    
    /**
     * 审批记录数据脱敏
     */
    public ApprovalRecord maskSensitiveData(ApprovalRecord record) {
        ApprovalRecord maskedRecord = record.clone();
        
        // 银行卡号脱敏
        if (record.getBankAccount() != null) {
            maskedRecord.setBankAccount(maskBankAccount(record.getBankAccount()));
        }
        
        // 身份证号脱敏
        if (record.getIdCard() != null) {
            maskedRecord.setIdCard(maskIdCard(record.getIdCard()));
        }
        
        // 手机号脱敏
        if (record.getPhoneNumber() != null) {
            maskedRecord.setPhoneNumber(maskPhoneNumber(record.getPhoneNumber()));
        }
        
        return maskedRecord;
    }
}
```

## 故障排查

### 1. 常见问题

#### 1.1 审批流程无法匹配
**症状**: 创建审批实例时报"未找到匹配的审批流程"
**排查步骤**:
1. 检查审批流程配置是否激活
2. 验证业务类型是否正确
3. 检查金额范围是否匹配
4. 确认审批节点条件配置

#### 1.2 审批操作失败
**症状**: 审批操作返回状态转换错误
**排查步骤**:
1. 检查当前审批状态
2. 验证操作是否符合状态流转规则
3. 确认审批人权限
4. 查看审批记录和审计日志

#### 1.3 性能问题
**症状**: 审批操作响应缓慢
**排查步骤**:
1. 检查数据库连接池状态
2. 分析慢查询日志
3. 验证缓存命中率
4. 检查系统资源使用情况

### 2. 日志分析

审批模块的关键日志文件：
- `logs/approval-service.log` - 服务层日志
- `logs/approval-audit.log` - 审计日志
- `logs/approval-performance.log` - 性能监控日志

### 3. 监控指标

关键监控指标：
- 审批操作成功率
- 平均审批时长
- 待审批队列长度
- 系统资源使用率

## 扩展开发

### 1. 自定义审批节点类型

```java
@Component
public class CustomApprovalNodeHandler implements ApprovalNodeHandler {
    
    @Override
    public String getNodeType() {
        return "CUSTOM_APPROVAL";
    }
    
    @Override
    public ApprovalResult handle(ApprovalNode node, ApprovalContext context) {
        // 自定义审批节点处理逻辑
        return executeCustomApprovalLogic(node, context);
    }
}
```

### 2. 集成外部审批系统

```java
@Component
public class ExternalApprovalIntegration {
    
    /**
     * 同步外部审批系统状态
     */
    @Scheduled(fixedRate = 300000) // 每5分钟同步一次
    public void syncExternalApprovalStatus() {
        List<ApprovalInstance> externalInstances = 
            approvalInstanceRepository.findByExternalSystemNotNull();
        
        for (ApprovalInstance instance : externalInstances) {
            ExternalApprovalStatus externalStatus = 
                externalApprovalService.getStatus(instance.getExternalReferenceId());
            
            if (externalStatus != null && !externalStatus.equals(instance.getStatus())) {
                // 同步状态更新
                approvalService.syncExternalStatus(instance.getId(), externalStatus);
            }
        }
    }
}
```

---

本使用指南涵盖了审批流程模块的核心功能和高级用法。实际使用时，请根据具体业务需求进行调整和扩展。如有问题，请参考API文档或联系技术支持。