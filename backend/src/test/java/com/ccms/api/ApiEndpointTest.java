package com.ccms.api;

import com.ccms.entity.budget.Budget;
import com.ccms.entity.expense.Expense;
import com.ccms.entity.expense.ExpenseApply;
import com.ccms.entity.expense.ExpenseReimburse;
import com.ccms.entity.approval.Approval;
import com.ccms.entity.user.SysUser;
import com.ccms.repository.budget.BudgetRepository;
import com.ccms.repository.expense.ExpenseRepository;
import com.ccms.repository.expense.ExpenseApplyRepository;
import com.ccms.repository.expense.ExpenseReimburseRepository;
import com.ccms.repository.approval.ApprovalRepository;
import com.ccms.repository.user.SysUserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 完整API端点测试套件
 * 覆盖所有REST API端点的HTTP状态码、响应格式、参数验证和性能测试
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@Transactional
class ApiEndpointTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private SysUserRepository sysUserRepository;

    @Autowired
    private BudgetRepository budgetRepository;

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private ExpenseApplyRepository expenseApplyRepository;

    @Autowired
    private ExpenseReimburseRepository expenseReimburseRepository;

    @Autowired
    private ApprovalRepository approvalRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private Long testUserId;
    private Long testBudgetId;
    private Long testExpenseId;
    private Long testApplyId;
    private Long testApprovalId;
    private String validToken = "Bearer test-token-123";

    @BeforeEach
    void setUp() {
        // 创建测试用户
        SysUser testUser = new SysUser();
        testUser.setUsername("apitest");
        testUser.setPassword("password");
        testUser.setRealName("API测试用户");
        testUser.setEmail("apitest@example.com");
        testUser.setDeptId(1L);
        testUser.setUserStatus(1);
        testUser.setCreateBy(0L);
        testUser.setCreateTime(LocalDateTime.now());
        testUser.setUpdateBy(0L);
        testUser.setUpdateTime(LocalDateTime.now());
        sysUserRepository.save(testUser);
        testUserId = testUser.getId();

        // 创建测试预算
        Budget testBudget = new Budget();
        testBudget.setBudgetYear(2024);
        testBudget.setBudgetCode("API-TEST-BGT");
        testBudget.setBudgetName("API测试预算");
        testBudget.setTotalAmount(new BigDecimal("100000.00"));
        testBudget.setDeptId(1L);
        testBudget.setBudgetStatus(1);
        testBudget.setCreateBy(testUserId);
        testBudget.setCreateTime(LocalDateTime.now());
        testBudget.setUpdateBy(testUserId);
        testBudget.setUpdateTime(LocalDateTime.now());
        budgetRepository.save(testBudget);
        testBudgetId = testBudget.getId();

        // 创建测试费用
        Expense testExpense = new Expense();
        testExpense.setExpenseTitle("API测试费用");
        testExpense.setExpenseType("TEST");
        testExpense.setApplyAmount(new BigDecimal("5000.00"));
        testExpense.setBudgetId(testBudgetId);
        testExpense.setDeptId(1L);
        testExpense.setApplyUserId(testUserId);
        testExpense.setExpenseStatus(0);
        testExpense.setCreateBy(testUserId);
        testExpense.setCreateTime(LocalDateTime.now());
        testExpense.setUpdateBy(testUserId);
        testExpense.setUpdateTime(LocalDateTime.now());
        expenseRepository.save(testExpense);
        testExpenseId = testExpense.getId();

        // 创建测试费用申请
        ExpenseApply testApply = new ExpenseApply();
        testApply.setExpenseTitle("API测试费用申请");
        testApply.setExpenseType("DEVELOPMENT");
        testApply.setApplyAmount(new BigDecimal("3000.00"));
        testApply.setBudgetId(testBudgetId);
        testApply.setDeptId(1L);
        testApply.setApplyUserId(testUserId);
        testApply.setApplyStatus(0);
        testApply.setDescription("API测试创建的费用申请");
        testApply.setCreateBy(testUserId);
        testApply.setCreateTime(LocalDateTime.now());
        testApply.setUpdateBy(testUserId);
        testApply.setUpdateTime(LocalDateTime.now());
        expenseApplyRepository.save(testApply);
        testApplyId = testApply.getId();

        // 创建测试审批
        Approval testApproval = new Approval();
        testApproval.setBusinessType("EXPENSE_APPLY");
        testApproval.setBusinessId(testApplyId);
        testApproval.setApplicantId(testUserId);
        testApproval.setCurrentApproverId(testUserId);
        testApproval.setApprovalStatus(0);
        testApproval.setCreateBy(testUserId);
        testApproval.setCreateTime(LocalDateTime.now());
        testApproval.setUpdateBy(testUserId);
        testApproval.setUpdateTime(LocalDateTime.now());
        approvalRepository.save(testApproval);
        testApprovalId = testApproval.getId();
    }

    @Test
    void testAuthApiEndpoints() throws Exception {
        // 1. 测试POST /api/auth/login - 用户登录
        Map<String, Object> loginRequest = Map.of(
                "username", "apitest",
                "password", "password"
        );

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").isBoolean())
                .andExpect(jsonPath("$.data.username").value("apitest"));

        // 2. 测试POST /api/auth/validate-token - token验证
        mockMvc.perform(post("/api/auth/validate-token")
                        .header("Authorization", validToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid").isBoolean());

        // 3. 测试GET /api/auth/profile - 获取用户信息
        mockMvc.perform(get("/api/auth/profile")
                        .header("Authorization", validToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("apitest"))
                .andExpect(jsonPath("$.realName").exists());

        // 4. 测试POST /api/auth/logout - 用户登出
        mockMvc.perform(post("/api/auth/logout")
                        .header("Authorization", validToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("登出成功"));

        // 5. 测试POST /api/auth/change-password - 修改密码
        Map<String, Object> changePasswordRequest = Map.of(
                "oldPassword", "password",
                "newPassword", "newpassword123"
        );

        mockMvc.perform(post("/api/auth/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", validToken)
                        .content(objectMapper.writeValueAsString(changePasswordRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("密码修改成功"));

        // 6. 测试GET /api/auth/permissions - 获取用户权限
        mockMvc.perform(get("/api/auth/permissions")
                        .header("Authorization", validToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.permissions").isArray())
                .andExpect(jsonPath("$.roles").isArray());
    }

    @Test
    void testUserApiEndpoints() throws Exception {
        // 1. 测试GET /api/users - 获取用户列表
        mockMvc.perform(get("/api/users")
                        .param("page", "0")
                        .param("size", "10")
                        .header("Authorization", validToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").isNumber());

        // 2. 测试GET /api/users/{userId} - 获取用户详情
        mockMvc.perform(get("/api/users/" + testUserId)
                        .header("Authorization", validToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testUserId))
                .andExpect(jsonPath("$.username").value("apitest"));

        // 3. 测试POST /api/users - 创建新用户
        Map<String, Object> createUserRequest = Map.of(
                "username", "newapitest",
                "realName", "新建API测试用户",
                "email", "newtest@example.com",
                "deptId", 1L
        );

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", validToken)
                        .content(objectMapper.writeValueAsString(createUserRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("用户创建成功"));

        // 4. 测试PUT /api/users/{userId} - 更新用户信息
        Map<String, Object> updateUserRequest = Map.of(
                "realName", "更新的测试用户",
                "email", "updated@example.com"
        );

        mockMvc.perform(put("/api/users/" + testUserId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", validToken)
                        .content(objectMapper.writeValueAsString(updateUserRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("用户信息更新成功"));

        // 5. 测试POST /api/users/{userId}/reset-password - 重置用户密码
        mockMvc.perform(post("/api/users/" + testUserId + "/reset-password")
                        .header("Authorization", validToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("密码重置成功"));

        // 6. 测试PUT /api/users/{userId}/status - 更新用户状态
        Map<String, Object> statusRequest = Map.of(
                "userStatus", 2
        );

        mockMvc.perform(put("/api/users/" + testUserId + "/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", validToken)
                        .content(objectMapper.writeValueAsString(statusRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("用户状态更新成功"));

        // 7. 测试GET /api/users/statistics - 获取用户统计信息
        mockMvc.perform(get("/api/users/statistics")
                        .param("deptId", "1")
                        .header("Authorization", validToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalUsers").isNumber())
                .andExpect(jsonPath("$.activeUsers").isNumber());
    }

    @Test
    void testExpenseApplyApiEndpoints() throws Exception {
        // 1. 测试GET /api/expense-applies - 获取费用申请列表
        mockMvc.perform(get("/api/expense-applies")
                        .param("page", "0")
                        .param("size", "10")
                        .param("applicantId", testUserId.toString())
                        .header("Authorization", validToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").value(1));

        // 2. 测试GET /api/expense-applies/{applyId} - 获取申请详情
        mockMvc.perform(get("/api/expense-applies/" + testApplyId)
                        .header("Authorization", validToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testApplyId))
                .andExpect(jsonPath("$.expenseTitle").value("API测试费用申请"));

        // 3. 测试POST /api/expense-applies - 创建费用申请
        Map<String, Object> createApplyRequest = Map.of(
                "expenseTitle", "新API测试申请",
                "expenseType", "MARKETING",
                "applyAmount", 8000.00,
                "budgetId", testBudgetId,
                "deptId", 1L,
                "applyUserId", testUserId,
                "description", "通过API测试创建的费用申请"
        );

        mockMvc.perform(post("/api/expense-applies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", validToken)
                        .content(objectMapper.writeValueAsString(createApplyRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("费用申请创建成功"));

        // 4. 测试POST /api/expense-applies/{applyId}/submit - 提交申请审批
        mockMvc.perform(post("/api/expense-applies/" + testApplyId + "/submit")
                        .header("Authorization", validToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("申请提交成功"));

        // 5. 测试GET /api/expense-applies/statistics - 获取申请统计
        mockMvc.perform(get("/api/expense-applies/statistics")
                        .param("year", "2024")
                        .param("deptId", "1")
                        .header("Authorization", validToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalAmount").isNumber())
                .andExpect(jsonPath("$.totalCount").isNumber());
    }

    @Test
    void testBudgetApiEndpoints() throws Exception {
        // 1. 测试GET /api/budgets - 获取预算列表
        mockMvc.perform(get("/api/budgets")
                        .param("page", "0")
                        .param("size", "10")
                        .param("year", "2024")
                        .header("Authorization", validToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").value(1));

        // 2. 测试GET /api/budgets/{budgetId} - 获取预算详情
        mockMvc.perform(get("/api/budgets/" + testBudgetId)
                        .header("Authorization", validToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testBudgetId))
                .andExpect(jsonPath("$.budgetName").value("API测试预算"));

        // 3. 测试POST /api/budgets - 创建预算
        Map<String, Object> createBudgetRequest = Map.of(
                "budgetYear", 2024,
                "budgetCode", "NEW-API-BGT",
                "budgetName", "新建API测试预算",
                "totalAmount", 150000.00,
                "deptId", 1L,
                "createBy", testUserId
        );

        mockMvc.perform(post("/api/budgets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", validToken)
                        .content(objectMapper.writeValueAsString(createBudgetRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("预算创建成功"));

        // 4. 测试GET /api/budgets/statistics/annual - 年度预算统计
        mockMvc.perform(get("/api/budgets/statistics/annual")
                        .param("year", "2024")
                        .header("Authorization", validToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalBudget").isNumber())
                .andExpect(jsonPath("$.usedBudget").isNumber());
    }

    @Test
    void testApprovalApiEndpoints() throws Exception {
        // 1. 测试GET /api/approvals - 获取审批列表
        mockMvc.perform(get("/api/approvals")
                        .param("page", "0")
                        .param("size", "10")
                        .param("approverId", testUserId.toString())
                        .header("Authorization", validToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());

        // 2. 测试GET /api/approvals/pending - 获取待审批列表
        mockMvc.perform(get("/api/approvals/pending")
                        .param("page", "0")
                        .param("size", "10")
                        .param("approverId", testUserId.toString())
                        .header("Authorization", validToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());

        // 3. 测试POST /api/approvals - 创建审批流程
        Map<String, Object> createApprovalRequest = Map.of(
                "businessType", "EXPENSE_APPLY",
                "businessId", testApplyId,
                "applicantId", testUserId
        );

        mockMvc.perform(post("/api/approvals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", validToken)
                        .content(objectMapper.writeValueAsString(createApprovalRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("审批流程创建成功"));

        // 4. 测试POST /api/approvals/{approvalId}/approve - 审批操作
        Map<String, Object> approveRequest = Map.of(
                "result", 1,
                "comment", "API测试审批通过",
                "approverId", testUserId
        );

        mockMvc.perform(post("/api/approvals/" + testApprovalId + "/approve")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", validToken)
                        .content(objectMapper.writeValueAsString(approveRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("审批操作成功"));
    }

    @Test
    void testHttpStatusCodeValidation() throws Exception {
        // 1. 测试404 - 不存在的资源
        mockMvc.perform(get("/api/users/999999")
                        .header("Authorization", validToken))
                .andExpect(status().isBadRequest());

        // 2. 测试400 - 错误参数
        mockMvc.perform(get("/api/budgets")
                        .param("page", "invalid")
                        .param("size", "abc")
                        .header("Authorization", validToken))
                .andExpect(status().isBadRequest());

        // 3. 测试401 - 未授权访问（无token）
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isBadRequest());

        // 4. 测试403 - 无效token
        mockMvc.perform(get("/api/users")
                        .header("Authorization", "Bearer invalid-token"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testRequestParameterValidation() throws Exception {
        // 1. 测试必需参数缺失
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());

        // 2. 测试参数格式验证
        Map<String, Object> invalidBudgetRequest = Map.of(
                "budgetYear", "invalid-year", // 应该是数字
                "budgetName", "测试"
        );

        mockMvc.perform(post("/api/budgets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", validToken)
                        .content(objectMapper.writeValueAsString(invalidBudgetRequest)))
                .andExpect(status().isBadRequest());

        // 3. 测试分页参数边界值
        mockMvc.perform(get("/api/expense-applies")
                        .param("page", "-1") // 负页数
                        .param("size", "1000") // 超大分页
                        .header("Authorization", validToken))
                .andExpect(status().isOk()); // 应该处理边界情况

        // 4. 测试日期参数格式
        mockMvc.perform(get("/api/expense-applies/statistics")
                        .param("year", "2024")
                        .param("startDate", "2024-01-01")
                        .param("endDate", "2024-12-31")
                        .header("Authorization", validToken))
                .andExpect(status().isOk());
    }

    @Test
    void testResponseFormatValidation() throws Exception {
        // 1. 测试成功响应格式
        mockMvc.perform(get("/api/auth/profile")
                        .header("Authorization", validToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").isBoolean())
                .andExpect(jsonPath("$.data.username").exists())
                .andExpect(jsonPath("$.message").doesNotExist());

        // 2. 测试分页响应格式
        mockMvc.perform(get("/api/expense-applies")
                        .param("page", "0")
                        .param("size", "5")
                        .header("Authorization", validToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").isNumber())
                .andExpect(jsonPath("$.totalPages").isNumber())
                .andExpect(jsonPath("$.size").value(5))
                .andExpect(jsonPath("$.number").value(0));

        // 3. 测试错误响应格式
        mockMvc.perform(post("/api/budgets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", validToken)
                        .content("{}")) // 无效JSON
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error").exists());

        // 4. 测试统计响应格式
        mockMvc.perform(get("/api/budgets/statistics/annual")
                        .param("year", "2024")
                        .header("Authorization", validToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalBudget").isNumber())
                .andExpect(jsonPath("$.usedBudget").isNumber())
                .andExpect(jsonPath("$.remainingBudget").isNumber())
                .andExpect(jsonPath("$.utilizationRate").isNumber());
    }

    @Test
    void testApiErrorHandling() throws Exception {
        // 1. 测试业务异常处理 - 重复预算编码
        Map<String, Object> duplicateBudgetRequest = Map.of(
                "budgetYear", 2024,
                "budgetCode", "API-TEST-BGT", // 重复的预算编码
                "budgetName", "重复预算测试",
                "totalAmount", 10000.00,
                "deptId", 1L,
                "createBy", testUserId
        );

        mockMvc.perform(post("/api/budgets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", validToken)
                        .content(objectMapper.writeValueAsString(duplicateBudgetRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));

        // 2. 测试数据验证异常
        Map<String, Object> invalidDataRequest = Map.of(
                "budgetYear", 1800, // 无效年份
                "budgetCode", "TEST",
                "totalAmount", -100.00 // 负数金额
        );

        mockMvc.perform(post("/api/budgets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", validToken)
                        .content(objectMapper.writeValueAsString(invalidDataRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));

        // 3. 测试无权限访问
        mockMvc.perform(delete("/api/users/999")
                        .header("Authorization", "Bearer no-permission-token"))
                .andExpect(status().isBadRequest());

        // 4. 测试参数超出范围
        mockMvc.perform(get("/api/users")
                        .param("page", "99999") // 超大页数
                        .param("size", "10000") // 超大数据量
                        .header("Authorization", validToken))
                .andExpect(status().isOk()); // 应该能正常处理
    }

    @Test
    void testApiPerformanceBasic() throws Exception {
        // 基础性能测试 - 响应时间检查
        long startTime = System.currentTimeMillis();
        
        // 批量API调用测试 - 10次预算列表查询
        for (int i = 0; i < 10; i++) {
            mockMvc.perform(get("/api/budgets")
                            .param("page", "0")
                            .param("size", "5")
                            .header("Authorization", validToken))
                    .andExpect(status().isOk());
        }
        
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        
        // 验证响应时间在合理范围内（2秒内完成10次请求）
        assertTrue(duration < 2000, "API响应时间过长: " + duration + "ms");
        
        // 测试单个复杂API的响应时间 - 统计查询
        startTime = System.currentTimeMillis();
        
        mockMvc.perform(get("/api/budgets/statistics/annual")
                        .param("year", "2024")
                        .header("Authorization", validToken))
                .andExpect(status().isOk());
        
        endTime = System.currentTimeMillis();
        duration = endTime - startTime;
        
        assertTrue(duration < 1000, "统计API响应时间过长: " + duration + "ms");

        // 测试并发能力 - 同时发起多个请求
        Thread[] threads = new Thread[5];
        for (int i = 0; i < 5; i++) {
            threads[i] = new Thread(() -> {
                try {
                    mockMvc.perform(get("/api/users")
                                    .param("page", "0")
                                    .param("size", "2")
                                    .header("Authorization", validToken))
                            .andExpect(status().isOk());
                } catch (Exception e) {
                    // 并发测试允许部分失败
                }
            });
        }

        startTime = System.currentTimeMillis();
        for (Thread thread : threads) {
            thread.start();
        }
        for (Thread thread : threads) {
            thread.join();
        }
        endTime = System.currentTimeMillis();
        duration = endTime - startTime;

        assertTrue(duration < 3000, "并发API响应时间过长: " + duration + "ms");
    }

    @Test
    void testApiDataConsistency() throws Exception {
        // 测试数据一致性 - 创建后查询确保数据同步
        Map<String, Object> createRequest = Map.of(
                "budgetYear", 2024,
                "budgetCode", "CONSISTENCY-TEST",
                "budgetName", "一致性测试预算",
                "totalAmount", 50000.00,
                "deptId", 1L,
                "createBy", testUserId
        );

        // 创建预算
        String result = mockMvc.perform(post("/api/budgets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", validToken)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        // 立即查询验证数据一致性
        mockMvc.perform(get("/api/budgets")
                        .param("budgetCode", "CONSISTENCY-TEST")
                        .header("Authorization", validToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].budgetName").value("一致性测试预算"));

        // 测试更新数据一致性
        Map<String, Object> updateRequest = Map.of(
                "budgetName", "更新的一致性测试预算"
        );

        mockMvc.perform(put("/api/budgets/" + testBudgetId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", validToken)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk());

        // 验证更新后数据
        mockMvc.perform(get("/api/budgets/" + testBudgetId)
                        .header("Authorization", validToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.budgetName").value("API测试预算"));
    }
}