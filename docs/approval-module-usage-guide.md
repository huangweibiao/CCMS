# 审批流程模块使用指南

## 1. 模块概述

审批流程模块为企业级费控管理系统提供了完整的审批流程管理功能，支持多级、多节点、条件化审批流程配置和执行。

### 1.1 核心特性
- **灵活配置**: 支持按业务类型、金额范围、条件表达式配置不同流程
- **多级审批**: 支持无限级数多节点审批，满足复杂审批需求
- **状态管理**: 完整的审批状态机，支持提交、审批、驳回、取消等操作
- **权限控制**: 严格的审批权限验证，确保操作安全性
- **审计追踪**: 完整的操作日志记录，支持全链路追踪

## 2. 快速开始

### 2.1 环境要求
- Java 17+
- Spring Boot 3.0+
- MySQL 8.0+
- Redis 6.0+（可选，用于缓存）

### 2.2 项目集成

#### 方式一：直接依赖（推荐）
```xml
<dependency>
    <groupId>com.ccms</groupId>
    <artifactId>approval-module</artifactId>
    <version>1.0.0</version>
</dependency>
```

#### 方式二：源码集成
1. 复制审批模块源码到项目中
2. 添加依赖配置
3. 配置数据库连接和Redis

### 2.3 基础配置

#### 数据库配置
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/ccms?useSSL=false&serverTimezone=Asia/Shanghai
    username: root
    password: your_password
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
```

#### Redis配置（可选）
```yaml
spring:
  redis:
    host: localhost
    port: 6379
    password: your_redis_password
```

## 3. 核心概念

### 3.1 审批流程配置（ApprovalFlowConfig）
定义审批流程的基本信息，包括业务类型、金额范围、启用条件等。

```java
ApprovalFlowConfig config = new ApprovalFlowConfig();
config.setBusinessType("EXPENSE_REIMBURSE");
config.setFlowName("费用报销标准流程");
config.setMinAmount(new BigDecimal("0"));
config.setMaxAmount(new BigDecimal("5000"));
config.setIsActive(true);
configRepository.save(config);
```

### 3.2 审批节点（ApprovalNode）
定义审批流程中的每个审批节点，包括审批人类型、审批人ID等。

```java
ApprovalNode node = new ApprovalNode();
node.setConfigId(config.getId());
node.setNodeName("部门经理审批");
node.setNodeOrder(1);
node.setApproverType(ApproverTypeEnum.ROLE);
node.setApproverId(2L); // 角色ID
node.setIsRequired(true);
nodeRepository.save(node);
```

### 3.3 审批实例（ApprovalInstance）
代表一次具体的审批流程实例。

```java
ApprovalInstance instance = approvalService.submitApproval(request);
```

## 4. API使用示例

### 4.1 提交审批申请

```java
// 构建审批请求
ApprovalRequest request = new ApprovalRequest();
request.setBusinessType(BusinessTypeEnum.EXPENSE_REIMBURSE);
request.setBusinessId("EXPENSE_20240115001");
request.setApplicantId(1001L);
request.setTitle("差旅费用报销申请");
request.setAmount(new BigDecimal("1500.00"));

// 调用审批服务
ApiResult<ApprovalInstance> result = approvalService.submitApproval(request);

if (result.isSuccess()) {
    ApprovalInstance instance = result.getData();
    System.out.println("审批实例创建成功，ID: " + instance.getId());
    System.out.println("当前状态: " + instance.getStatus().getDesc());
}
```

### 4.2 审批操作

#### 审批通过
```java
ApprovalOperateRequest operateRequest = new ApprovalOperateRequest();
operateRequest.setApproverId(1002L);
operateRequest.setRemarks("申请理由充分，同意报销");

ApiResult<Boolean> result = approvalService.approve(instanceId, operateRequest);
```

#### 审批驳回
```java
ApprovalOperateRequest operateRequest = new ApprovalOperateRequest();
operateRequest.setApproverId(1002L);
operateRequest.setRemarks("报销明细不清晰，请补充");

ApiResult<Boolean> result = approvalService.reject(instanceId, operateRequest);
```

#### 取消申请
```java
ApprovalCancelRequest cancelRequest = new ApprovalCancelRequest();
cancelRequest.setOperatorId(1001L);
cancelRequest.setReason("需要修改申请内容");

ApiResult<Boolean> result = approvalService.cancel(instanceId, cancelRequest);
```

### 4.3 查询操作

#### 查询审批实例
```java
// 根据ID查询
ApiResult<ApprovalInstance> result = approvalService.getInstanceById(instanceId);

// 根据业务ID查询
ApiResult<ApprovalInstance> result = approvalService.getInstanceByBusinessId(
    BusinessTypeEnum.EXPENSE_REIMBURSE, "EXPENSE_001");
```

#### 查询审批历史
```java
// 查询审批记录
ApiResult<List<ApprovalRecord>> result = approvalService.getApprovalHistory(instanceId);

// 查询审计日志
ApiResult<List<ApprovalAuditLog>> result = approvalService.getAuditLogs(instanceId);
```

### 4.4 异步处理和回调

#### 审批状态变更监听
```java
@Component
public class ApprovalStatusListener {
    
    @EventListener
    public void onApprovalStatusChange(ApprovalStatusChangeEvent event) {
        // 业务处理逻辑
        String businessId = event.getBusinessId();
        ApprovalStatus newStatus = event.getNewStatus();
        
        // 更新业务表状态
        businessService.updateApprovalStatus(businessId, newStatus);
        
        // 发送通知
        notificationService.sendApprovalNotification(businessId, newStatus);
    }
}
```

#### 异步审批处理
```java
@Async
public CompletableFuture<ApiResult<Boolean>> asyncApprove(Long instanceId, ApprovalOperateRequest request) {
    return CompletableFuture.supplyAsync(() -> {
        return approvalService.approve(instanceId, request);
    });
}
```

## 5. 高级功能

### 5.1 条件化审批流程

支持根据复杂条件配置审批流程：

```java
// 创建带条件的流程配置
ApprovalFlowConfig conditionalConfig = new ApprovalFlowConfig();
conditionalConfig.setBusinessType("SPECIAL_APPROVAL");
conditionalConfig.setFlowName("专项审批流程");
conditionalConfig.setEnableCondition(true);

// 条件化配置节点
ApprovalNode conditionalNode = new ApprovalNode();
conditionalNode.setConditionExpression("amount > 10000 && departmentId == 1");
conditionalNode.setIsRequired(false); // 满足条件才需要审批
```

### 5.2 多级审批配置

创建多级审批流程示例：

```java
// 一级审批：部门经理
ApprovalNode level1 = new ApprovalNode();
level1.setNodeName("部门审批");
level1.setNodeOrder(1);
level1.setApproverType(ApproverTypeEnum.ROLE);
level1.setApproverId(2L); // 部门经理角色

// 二级审批：财务
ApprovalNode level2 = new ApprovalNode();
level2.setNodeName("财务审批");
level2.setNodeOrder(2);
level2.setApproverType(ApproverTypeEnum.ROLE);
level2.setApproverId(3L); // 财务角色

// 三级审批：总经理
ApprovalNode level3 = new ApprovalNode();
level3.setNodeName("总经理审批");
level3.setNodeOrder(3);
level3.setApproverType(ApproverTypeEnum.ROLE);
level3.setApproverId(4L); // 总经理角色
```

### 5.3 自定义审批人策略

支持多种审批人配置方式：

```java
// 按角色审批
node.setApproverType(ApproverTypeEnum.ROLE);
node.setApproverId(2L); // 角色ID

// 按具体用户审批
node.setApproverType(ApproverTypeEnum.USER);
node.setApproverId(1001L); // 用户ID

// 按部门审批
node.setApproverType(ApproverTypeEnum.DEPARTMENT);
node.setApproverId(1L); // 部门ID

// 按用户组审批
node.setApproverType(ApproverTypeEnum.GROUP);
node.setApproverId(5L); // 用户组ID
```

## 6. 异常处理

### 6.1 统一异常响应

所有审批接口返回统一的响应格式：

```java
// 成功响应示例
{
    "success": true,
    "data": {
        "id": 2001,
        "status": "APPROVING",
        "currentApproverId": 1002
    },
    "errorCode": "1000",
    "errorMessage": "操作成功",
    "timestamp": "2024-01-15T10:30:00"
}

// 错误响应示例
{
    "success": false,
    "data": null,
    "errorCode": "1101",
    "errorMessage": "审批流程配置不存在",
    "timestamp": "2024-01-15T10:30:00"
}
```

### 6.2 常见错误码

| 错误码 | 说明 | 处理建议 |
|--------|------|----------|
| 1100 | 审批流程配置不存在 | 检查业务类型和金额范围 |
| 1101 | 当前状态下不允许此操作 | 检查审批实例状态 |
| 1102 | 审批人不匹配 | 确认当前审批人身份 |
| 1103 | 审批流程实例不存在 | 检查实例ID是否正确 |
| 1104 | 金额范围不匹配 | 调整申请金额或配置流程 |

### 6.3 自定义异常处理

```java
@ControllerAdvice
public class ApprovalExceptionHandler {
    
    @ExceptionHandler(ApprovalException.class)
    @ResponseBody
    public ApiResult<Void> handleApprovalException(ApprovalException e) {
        return ApiResult.error(e.getErrorCode(), e.getMessage());
    }
    
    @ExceptionHandler(StatusTransitionException.class)
    @ResponseBody
    public ApiResult<Void> handleStatusTransitionException(StatusTransitionException e) {
        return ApiResult.error("1101", "状态转换异常: " + e.getMessage());
    }
}
```

## 7. 性能优化建议

### 7.1 缓存配置

```java
@Configuration
@EnableCaching
public class CacheConfig {
    
    @Bean
    public CacheManager cacheManager() {
        RedisCacheManager cacheManager = RedisCacheManager.create(redisConnectionFactory());
        cacheManager.setCacheDefaults(RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofMinutes(30)) // 缓存30分钟
            .disableCachingNullValues());
        return cacheManager;
    }
    
    // 缓存流程配置
    @Cacheable(value = "approvalConfig", key = "#businessType + ':' + #amount")
    public Optional<ApprovalFlowConfig> findMatchingConfig(String businessType, BigDecimal amount) {
        return configRepository.findByBusinessTypeAndAmountRange(businessType, amount);
    }
}
```

### 7.2 数据库优化

```sql
-- 添加索引
CREATE INDEX idx_approval_business ON ccms_approval_instance(business_type, business_id);
CREATE INDEX idx_approval_status ON ccms_approval_instance(status, update_time);
CREATE INDEX idx_approval_applicant ON ccms_approval_instance(applicant_id, create_time);
```

### 7.3 批量操作优化

```java
// 批量查询
@Query("SELECT a FROM ApprovalInstance a WHERE a.applicantId = :applicantId AND a.status IN :statusList")
Page<ApprovalInstance> findByApplicantAndStatus(@Param("applicantId") Long applicantId, 
                                               @Param("statusList") List<ApprovalStatus> statusList,
                                               Pageable pageable);

// 批量更新
@Modifying
@Query("UPDATE ApprovalInstance a SET a.status = :status WHERE a.id IN :instanceIds")
int batchUpdateStatus(@Param("instanceIds") List<Long> instanceIds, 
                     @Param("status") ApprovalStatus status);
```

## 8. 监控和日志

### 8.1 审计日志配置

```java
@Aspect
@Component
public class ApprovalAuditAspect {
    
    @AfterReturning(pointcut = "execution(* com.ccms.service.ApprovalService.*(..))", 
                    returning = "result")
    public void auditApprovalOperation(JoinPoint joinPoint, Object result) {
        // 记录审计日志
        String methodName = joinPoint.getSignature().getName();
        Object[] args = joinPoint.getArgs();
        
        ApprovalAuditLog auditLog = new ApprovalAuditLog();
        auditLog.setActionType(AuditActionType.valueOf(methodName.toUpperCase()));
        auditLog.setDescription("执行审批操作: " + methodName);
        auditLog.setOperatorId(getCurrentUserId());
        auditLog.setIpAddress(getRemoteIp());
        
        auditLogRepository.save(auditLog);
    }
}
```

### 8.2 性能监控

```java
@RestController
public class MonitoringController {
    
    @GetMapping("/metrics/approval")
    public ApprovalMetrics getApprovalMetrics() {
        return approvalService.getMetrics();
    }
    
    @GetMapping("/health/approval")
    public HealthStatus getApprovalHealth() {
        return approvalService.getHealthStatus();
    }
}

// 监控指标类
@Data
public class ApprovalMetrics {
    private long totalInstances;
    private long pendingInstances;
    private long approvedInstances;
    private long rejectedInstances;
    private double averageApprovalTime;
    private Map<String, Long> instancesByBusinessType;
}
```

## 9. 测试指南

### 9.1 单元测试

```java
@SpringBootTest
class ApprovalServiceTest {
    
    @Autowired
    private ApprovalService approvalService;
    
    @Test
    void testSubmitApproval() {
        // 准备测试数据
        ApprovalRequest request = new ApprovalRequest();
        request.setBusinessType(BusinessTypeEnum.EXPENSE_REIMBURSE);
        request.setBusinessId("TEST_001");
        request.setApplicantId(1001L);
        
        // 执行测试
        ApiResult<ApprovalInstance> result = approvalService.submitApproval(request);
        
        // 验证结果
        assertTrue(result.isSuccess());
        assertNotNull(result.getData());
        assertEquals(ApprovalStatus.APPROVING, result.getData().getStatus());
    }
}
```

### 9.2 集成测试

```java
@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ApprovalControllerIntegrationTest {
    
    @Test
    void testFullApprovalWorkflow() {
        // 完整的端到端测试
        // 1. 提交申请
        // 2. 审批操作
        // 3. 验证状态变更
        // 4. 验证审计日志
    }
}
```

### 9.3 性能测试

```java
@Test
void testPerformanceUnderLoad() {
    // 并发测试
    List<CompletableFuture<ApiResult<ApprovalInstance>>> futures = new ArrayList<>();
    
    for (int i = 0; i < 100; i++) {
        ApprovalRequest request = createTestRequest(i);
        CompletableFuture<ApiResult<ApprovalInstance>> future = 
            CompletableFuture.supplyAsync(() -> approvalService.submitApproval(request));
        futures.add(future);
    }
    
    // 等待所有任务完成
    CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
    
    // 验证响应时间和服务可用性
    assertThat(performanceMetrics.getAverageResponseTime(), lessThan(1000L));
}
```

## 10. 常见问题

### 10.1 配置问题

**Q: 如何配置审批流程？**
A: 通过审批流程配置接口或直接在数据库插入配置数据，确保业务类型、金额范围正确匹配。

**Q: 审批流程匹配不到怎么办？**
A: 检查配置的金额范围是否覆盖申请金额，确保流程配置处于启用状态。

### 10.2 操作问题

**Q: 审批操作失败，状态转换不合法？**
A: 检查当前审批实例状态，确保操作符合状态机规则。常见错误：对已完成的申请执行审批操作。

**Q: 审批权限验证失败？**
A: 确认操作用户具有当前节点的审批权限，检查审批人配置是否正确。

### 10.3 性能问题

**Q: 审批查询响应慢？**
A: 添加适当的数据库索引，使用缓存机制，优化查询条件。

**Q: 在高并发场景下如何处理？**
A: 使用乐观锁防止数据竞争，启用异步处理机制，合理配置线程池。

## 11. 扩展开发

### 11.1 自定义审批策略

```java
@Component
public class CustomApprovalStrategy {
    
    @Autowired
    private ApprovalService approvalService;
    
    public ApiResult<ApprovalInstance> customSubmit(ApprovalRequest request) {
        // 自定义审批逻辑
        if (isSpecialCase(request)) {
            // 特殊处理逻辑
            return handleSpecialCase(request);
        }
        
        // 正常审批流程
        return approvalService.submitApproval(request);
    }
}
```

### 11.2 插件化扩展

支持通过插件机制扩展审批功能：

```java
public interface ApprovalPlugin {
    boolean supports(BusinessTypeEnum businessType);
    ApiResult<ApprovalInstance> preProcess(ApprovalRequest request);
    ApiResult<Void> postProcess(ApprovalInstance instance, ApprovalAction action);
}

@Component
public class ExpenseApprovalPlugin implements ApprovalPlugin {
    
    @Override
    public boolean supports(BusinessTypeEnum businessType) {
        return businessType == BusinessTypeEnum.EXPENSE_REIMBURSE;
    }
    
    @Override
    public ApiResult<ApprovalInstance> preProcess(ApprovalRequest request) {
        // 费用报销特殊处理逻辑
        if (request.getAmount().compareTo(new BigDecimal("10000")) > 0) {
            // 大额费用特殊处理
            return handleLargeExpense(request);
        }
        return ApiResult.success(null); // 无特殊处理
    }
}
```

## 12. 最佳实践

1. **配置管理**: 将流程配置抽象为可配置项，支持运行时修改
2. **权限分离**: 审批操作权限与业务操作权限严格分离
3. **状态一致**: 业务状态与审批状态保持同步，避免数据不一致
4. **监控告警**: 设置关键指标监控，及时发现问题
5. **备份恢复**: 定期备份审批数据，制定灾难恢复方案

通过本指南，您可以快速上手审批流程模块的开发和使用。如有更多需求或问题，请查阅API文档或联系开发团队。