package com.ccms;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.math.BigDecimal;
import java.time.LocalDate;
import com.ccms.entity.ApprovalFlowConfig;
import com.ccms.entity.ApprovalInstance;
import com.ccms.entity.ApprovalTask;
import com.ccms.entity.budget.BudgetMain;
import com.ccms.entity.budget.BudgetDetail;
import com.ccms.entity.budget.BudgetAdjust;
import com.ccms.entity.User;

/**
 * 测试工具类
 * 提供测试中使用的通用方法和辅助函数
 */
public class TestUtils {
    
    private TestUtils() {
        // 工具类，防止实例化
    }
    
    /**
     * 通过反射设置对象的私有字段值
     */
    public static void setPrivateField(Object target, String fieldName, Object value) {
        try {
            Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException("设置私有字段失败: " + fieldName, e);
        }
    }
    
    /**
     * 通过反射获取对象的私有字段值
     */
    @SuppressWarnings("unchecked")
    public static <T> T getPrivateField(Object target, String fieldName) {
        try {
            Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            return (T) field.get(target);
        } catch (Exception e) {
            throw new RuntimeException("获取私有字段失败: " + fieldName, e);
        }
    }
    
    /**
     * 创建测试时间范围
     */
    public static LocalDateTime[] createTestTimeRange() {
        LocalDateTime startTime = LocalDateTime.of(2024, 1, 1, 0, 0);
        LocalDateTime endTime = LocalDateTime.of(2024, 12, 31, 23, 59);
        return new LocalDateTime[]{startTime, endTime};
    }
    
    /**
     * 创建测试JSON字符串
     */
    public static class JsonBuilder {
        
        public static String createLoginRequest(String username, String password) {
            return String.format("{\"username\": \"%s\", \"password\": \"%s\"}", username, password);
        }
        
        public static String createExpenseApplyRequest(Double amount, String reason) {
            return String.format("{\"amount\": %s, \"reason\": \"%s\"}", amount, reason);
        }
        
        public static String createBudgetRequest(String department, Integer year, Integer month, Double totalAmount) {
            return String.format("{\"department\": \"%s\", \"year\": %d, \"month\": %d, \"totalAmount\": %s}", 
                department, year, month, totalAmount);
        }
    }
    
    /**
     * 常用的测试数据常量
     */
    public static class TestData {
        public static final Long VALID_USER_ID = 1L;
        public static final Long INVALID_USER_ID = 999L;
        public static final String VALID_USERNAME = "testuser";
        public static final String INVALID_USERNAME = "nonexistent";
        public static final String VALID_PASSWORD = "password";
        public static final String INVALID_PASSWORD = "wrongpassword";
        public static final String ADMIN_ROLE = "admin";
        public static final String USER_ROLE = "user";
        public static final Double VALID_AMOUNT = 1000.0;
        public static final Double INVALID_AMOUNT = -1000.0;
        public static final String VALID_DEPARTMENT = "测试部门";
        public static final String INVALID_DEPARTMENT = "不存在的部门";
        
        // 审批状态常量
        public static final Integer STATUS_PENDING = 1;
        public static final Integer STATUS_APPROVED = 2;
        public static final Integer STATUS_REJECTED = 3;
        public static final Integer STATUS_COMPLETED = 4;
        
        // 常见的错误消息
        public static final String ERROR_USER_NOT_FOUND = "用户不存在";
        public static final String ERROR_INVALID_PASSWORD = "密码错误";
        public static final String ERROR_INSUFFICIENT_BUDGET = "预算不足";
        public static final String ERROR_INVALID_STATUS = "状态不合法";
    }
    
    /**
     * 测试注解常量
     */
    public static class TestAnnotations {
        @AutoConfigureMockMvc
        @ExtendWith({SpringExtension.class, MockitoExtension.class})
        public @interface ControllerTestConfig {}
        
        @ExtendWith(MockitoExtension.class)
        public @interface ServiceTestConfig {}
    }
    
    /**
     * 常用的测试验证方法
     */
    public static class Assertions {
        
        public static <T> void assertResponseContains(List<T> items, T expectedItem) {
            if (!items.contains(expectedItem)) {
                throw new AssertionError("期望的元素不在响应列表中: " + expectedItem);
            }
        }
        
        public static void assertValidId(Long id) {
            if (id == null || id <= 0) {
                throw new AssertionError("ID必须大于0: " + id);
            }
        }
        
        public static void assertValidAmount(Double amount) {
            if (amount == null || amount <= 0) {
                throw new AssertionError("金额必须大于0: " + amount);
            }
        }
    }

    /**
     * 服务测试数据工厂 - 统一的测试数据准备模式
     */
    public static class ServiceTestDataFactory {
        
        /**
         * 创建用户测试数据
         */
        public static User createTestUser(Long id, String username, String role) {
            User user = new User();
            user.setId(id);
            user.setUsername(username);
            user.setPassword("testpassword");
            user.setEmail(username + "@test.com");
            user.setRole(role);
            user.setDeptId(101L);
            user.setStatus(1); // 激活状态
            user.setCreateTime(LocalDateTime.now());
            return user;
        }
        
        /**
         * 创建审批流程配置测试数据
         */
        public static ApprovalFlowConfig createApprovalFlowConfig(
                Long id, String flowType, String flowName, Long deptId, boolean autoApproval) {
            ApprovalFlowConfig config = new ApprovalFlowConfig();
            config.setId(id);
            config.setFlowType(flowType);
            config.setFlowName(flowName);
            config.setDeptId(deptId);
            config.setAmountLimitStart(100000L);
            config.setAmountLimitEnd(500000L);
            config.setApprovalFlow("1001,1002,1003");
            config.setAutoApprovalEnabled(autoApproval);
            config.setCreateTime(LocalDateTime.now());
            return config;
        }
        
        /**
         * 创建审批实例测试数据
         */
        public static ApprovalInstance createApprovalInstance(
                Long id, String businessType, Long businessId, Long applicantId, Long deptId, 
                Integer status, Integer currentStep, Integer totalSteps) {
            ApprovalInstance instance = new ApprovalInstance();
            instance.setId(id);
            instance.setBusinessType(businessType);
            instance.setBusinessId(businessId);
            instance.setInstanceNo("AP" + System.currentTimeMillis());
            instance.setApplicantId(applicantId);
            instance.setDeptId(deptId);
            instance.setStatus(status);
            instance.setCurrentStep(currentStep);
            instance.setTotalSteps(totalSteps);
            instance.setCreateTime(LocalDateTime.now());
            return instance;
        }
        
        /**
         * 创建审批任务测试数据
         */
        public static ApprovalTask createApprovalTask(
                Long id, Long instanceId, Integer step, Long approverId, Integer taskStatus) {
            ApprovalTask task = new ApprovalTask();
            task.setId(id);
            task.setInstanceId(instanceId);
            task.setStep(step);
            task.setApproverId(approverId);
            task.setTaskStatus(taskStatus);
            task.setCreateTime(LocalDateTime.now());
            if (taskStatus == 3 || taskStatus == 4) { // 已审批或已拒绝
                task.setApprovalTime(LocalDateTime.now());
                task.setApprovalComment("测试审批意见");
            }
            return task;
        }
        
        /**
         * 创建预算主表测试数据
         */
        public static BudgetMain createBudgetMain(
                Long id, Long deptId, String budgetName, Integer budgetYear, 
                Integer budgetCycle, Integer status, BigDecimal totalAmount) {
            BudgetMain budget = new BudgetMain();
            budget.setId(id);
            budget.setDeptId(deptId);
            budget.setBudgetName(budgetName);
            budget.setBudgetYear(budgetYear);
            budget.setBudgetCycle(budgetCycle);
            budget.setTotalAmount(totalAmount);
            budget.setStatus(status);
            budget.setApprovalStatus(status == 0 ? 0 : 1); // 根据状态设置审批状态
            budget.setBudgetCode("BD" + System.currentTimeMillis());
            budget.setCreateTime(LocalDateTime.now());
            budget.setCreateBy(1L);
            return budget;
        }
        
        /**
         * 创建预算明细测试数据
         */
        public static BudgetDetail createBudgetDetail(
                Long id, Long budgetMainId, Integer expenseType, BigDecimal budgetAmount,
                BigDecimal usedAmount) {
            BudgetDetail detail = new BudgetDetail();
            detail.setId(id);
            detail.setBudgetMainId(budgetMainId);
            detail.setExpenseType(expenseType);
            detail.setBudgetAmount(budgetAmount);
            detail.setUsedAmount(usedAmount);
            detail.setRemainingAmount(budgetAmount.subtract(usedAmount));
            return detail;
        }
        
        /**
         * 创建预算调整测试数据
         */
        public static BudgetAdjust createBudgetAdjust(
                Long id, Long budgetMainId, Integer adjustType, BigDecimal adjustAmount,
                String adjustReason, Integer status) {
            BudgetAdjust adjust = new BudgetAdjust();
            adjust.setId(id);
            adjust.setBudgetMainId(budgetMainId);
            adjust.setAdjustType(adjustType);
            adjust.setAdjustAmount(adjustAmount);
            adjust.setAdjustReason(adjustReason);
            adjust.setStatus(status);
            adjust.setAdjustStatus(status == 0 ? 0 : 1);
            adjust.setAdjustDate(LocalDate.now());
            adjust.setCreateTime(LocalDateTime.now());
            return adjust;
        }
        
        /**
         * 标准的测试数据集合 - 用于服务测试的@BeforeEach方法
         */
        public static class TestDataSets {
            
            public static BudgetMain createDefaultBudgetMain() {
                return createBudgetMain(1L, 101L, "研发部2025年度预算", 2025, 1, 0, 
                        new BigDecimal("1000000.00"));
            }
            
            public static BudgetDetail createDefaultBudgetDetail() {
                return createBudgetDetail(1L, 1L, 1, new BigDecimal("100000.00"), BigDecimal.ZERO);
            }
            
            public static ApprovalFlowConfig createBudgetFlowConfig() {
                return createApprovalFlowConfig(1L, "BUDGET", "预算审批流程", 101L, true);
            }
            
            public static ApprovalInstance createBudgetApprovalInstance() {
                return createApprovalInstance(1L, "BUDGET", 1001L, 2001L, 101L, 1, 1, 3);
            }
            
            public static ApprovalTask createPendingApprovalTask() {
                return createApprovalTask(1L, 1L, 1, 3001L, 1);
            }
            
            public static ApprovalTask createApprovedApprovalTask() {
                return createApprovalTask(2L, 1L, 1, 3001L, 3);
            }
        }
        
        /**
         * Mock数据设置工具
         */
        public static class MockHelper {
            
            /**
             * 设置保存操作的mock响应模式
             */
            public static <T> void setupSaveMock(org.mockito.Mock mockService, T returnObject) {
                when(mockService.save(any())).thenReturn(returnObject);
            }
            
            /**
             * 设置查找操作的mock响应模式
             */
            public static <T> void setupFindByIdMock(org.mockito.Mock mockService, Long id, T returnObject) {
                when(mockService.findById(id)).thenReturn(java.util.Optional.ofNullable(returnObject));
            }
            
            /**
             * 设置空响应的mock模式
             */
            public static <T> void setupEmptyMock(org.mockito.Mock mockService) {
                when(mockService.findById(anyLong())).thenReturn(java.util.Optional.empty());
            }
            
            /**
             * 验证服务方法的调用次数和参数
             */
            public static <T> void verifyMethodCall(org.mockito.Mock mockService, int times, String methodName) {
                try {
                    java.lang.reflect.Method method = mockService.getClass().getDeclaredMethod(methodName, Object.class);
                    verify(mockService, times(times)).save(any());
                } catch (Exception e) {
                    // 方法名可能不匹配，使用通用验证
                    org.mockito.BDDMockito.then(mockService).should(times(times)).save(any());
                }
            }
        }
    }
    
    /**
     * 通用测试数据验证器
     */
    public static class DataValidator {
        
        public static void validateBudgetMain(BudgetMain budget) {
            assertNotNull(budget);
            assertTrue(budget.getId() > 0);
            assertNotNull(budget.getBudgetName());
            assertNotNull(budget.getTotalAmount());
            assertTrue(budget.getTotalAmount().compareTo(BigDecimal.ZERO) > 0);
        }
        
        public static void validateApprovalInstance(ApprovalInstance instance) {
            assertNotNull(instance);
            assertNotNull(instance.getBusinessType());
            assertNotNull(instance.getInstanceNo());
            assertNotNull(instance.getStatus());
            assertNotNull(instance.getCurrentStep());
            assertNotNull(instance.getTotalSteps());
            assertTrue(instance.getCurrentStep() <= instance.getTotalSteps());
        }
        
        public static void validateBudgetDetail(BudgetDetail detail) {
            assertNotNull(detail);
            assertNotNull(detail.getBudgetAmount());
            assertNotNull(detail.getUsedAmount());
            assertNotNull(detail.getRemainingAmount());
            assertTrue(detail.getRemainingAmount().compareTo(BigDecimal.ZERO) >= 0);
            assertTrue(detail.getBudgetAmount().compareTo(detail.getUsedAmount()) >= 0);
        }
    }
}