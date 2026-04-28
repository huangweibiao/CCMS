package com.ccms.integration;

import com.ccms.entity.budget.Budget;
import com.ccms.entity.expense.Expense;
import com.ccms.repository.budget.BudgetRepository;
import com.ccms.repository.expense.ExpenseRepository;
import com.ccms.service.BudgetService;
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
 * 预算费用集成测试
 * 测试预算管理和费用报销的完整业务流程
 * 包括预算创建、分配、费用报销、预算执行监控等
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@Transactional
class BudgetExpenseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private BudgetService budgetService;

    @Autowired
    private BudgetRepository budgetRepository;

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private Long testUserId = 1L;
    private Long testDeptId = 100L;
    private Long testApproverId = 2L;

    @BeforeEach
    void setUp() {
        // 测试数据会在每个测试方法的事务中自动回滚
    }

    @Test
    void testBudgetCreationAndAllocation() throws Exception {
        // 1. 创建年度预算
        Map<String, Object> budgetRequest = Map.of(
                "budgetYear", 2024,
                "budgetCode", "BGT-2024-001",
                "budgetName", "2024年度营销预算",
                "totalAmount", 100000.00,
                "deptId", testDeptId,
                "createBy", testUserId
        );

        String response = mockMvc.perform(post("/api/budgets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer test-token")
                        .content(objectMapper.writeValueAsString(budgetRequest)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        Map<String, Object> responseMap = objectMapper.readValue(response, Map.class);
        assertEquals("预算创建成功", responseMap.get("message"));

        // 2. 验证预算已创建
        Budget createdBudget = budgetRepository.findByBudgetCode("BGT-2024-001")
                .orElse(null);
        assertNotNull(createdBudget);
        assertEquals(0, createdBudget.getBudgetStatus()); // 草稿状态
        assertEquals(new BigDecimal("100000.00"), createdBudget.getTotalAmount());

        Long budgetId = createdBudget.getId();

        // 3. 获取预算详情
        mockMvc.perform(get("/api/budgets/" + budgetId)
                        .header("Authorization", "Bearer test-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(budgetId));

        // 4. 预算分配
        Map<String, Object> allocationRequest = Map.of(
                "deptId", testDeptId,
                "amount", 50000.00
        );

        mockMvc.perform(post("/api/budgets/" + budgetId + "/allocate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer test-token")
                        .content(objectMapper.writeValueAsString(allocationRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("预算分配成功"));

        // 5. 预算提交审批
        mockMvc.perform(post("/api/budgets/" + budgetId + "/submit")
                        .header("Authorization", "Bearer test-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("预算提交审批成功"));

        // 6. 预算审批
        Map<String, Object> approvalRequest = Map.of(
                "result", 1,
                "comment", "同意预算分配",
                "approverId", testApproverId
        );

        mockMvc.perform(post("/api/budgets/" + budgetId + "/approve")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer test-token")
                        .content(objectMapper.writeValueAsString(approvalRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("预算审批完成"));

        // 7. 验证预算状态更新
        Budget approvedBudget = budgetRepository.findById(budgetId).orElse(null);
        assertNotNull(approvedBudget);
        assertEquals(1, approvedBudget.getBudgetStatus()); // 已审批状态
    }

    @Test
    void testExpenseCreationAndBudgetControl() throws Exception {
        // 1. 创建已审批的预算
        Budget testBudget = new Budget();
        testBudget.setBudgetYear(2024);
        testBudget.setBudgetCode("BGT-2024-002");
        testBudget.setBudgetName("2024年度差旅预算");
        testBudget.setTotalAmount(new BigDecimal("50000.00"));
        testBudget.setDeptId(testDeptId);
        testBudget.setBudgetStatus(1); // 已审批
        testBudget.setCreateBy(testUserId);
        testBudget.setCreateTime(LocalDateTime.now());
        testBudget.setUpdateBy(testUserId);
        testBudget.setUpdateTime(LocalDateTime.now());
        budgetRepository.save(testBudget);

        Long budgetId = testBudget.getId();

        // 2. 创建费用报销申请
        Map<String, Object> expenseRequest = Map.of(
                "expenseTitle", "北京差旅费",
                "expenseType", "TRAVEL",
                "applyAmount", 5000.00,
                "budgetId", budgetId,
                "deptId", testDeptId,
                "applyUserId", testUserId,
                "description", "3月北京出差费用"
        );

        mockMvc.perform(post("/api/expenses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer test-token")
                        .content(objectMapper.writeValueAsString(expenseRequest)))
                .andExpect(status().isOk());

        // 3. 验证费用已创建
        Expense createdExpense = expenseRepository.findByExpenseTitle("北京差旅费")
                .orElse(null);
        assertNotNull(createdExpense);
        assertEquals(new BigDecimal("5000.00"), createdExpense.getApplyAmount());

        Long expenseId = createdExpense.getId();

        // 4. 提交费用报销审批
        mockMvc.perform(post("/api/expenses/" + expenseId + "/submit")
                        .header("Authorization", "Bearer test-token"))
                .andExpect(status().isOk());

        // 5. 费用审批
        Map<String, Object> expenseApprovalRequest = Map.of(
                "result", 1,
                "comment", "差旅费审批通过",
                "approverId", testApproverId
        );

        mockMvc.perform(post("/api/expenses/" + expenseId + "/approve")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer test-token")
                        .content(objectMapper.writeValueAsString(expenseApprovalRequest)))
                .andExpect(status().isOk());

        // 6. 验证费用状态更新
        Expense approvedExpense = expenseRepository.findById(expenseId).orElse(null);
        assertNotNull(approvedExpense);
        assertEquals(1, approvedExpense.getExpenseStatus()); // 已审批

        // 7. 获取预算执行情况
        mockMvc.perform(get("/api/budgets/" + budgetId + "/execution")
                        .header("Authorization", "Bearer test-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalAmount").value(50000))
                .andExpect(jsonPath("$.usedAmount").value(5000));
    }

    @Test
    void testBudgetAdjustmentAndMonitoring() throws Exception {
        // 1. 创建预算
        Budget testBudget = new Budget();
        testBudget.setBudgetYear(2024);
        testBudget.setBudgetCode("BGT-2024-003");
        testBudget.setBudgetName("2024年度运营预算");
        testBudget.setTotalAmount(new BigDecimal("200000.00"));
        testBudget.setDeptId(testDeptId);
        testBudget.setBudgetStatus(1);
        testBudget.setCreateBy(testUserId);
        testBudget.setCreateTime(LocalDateTime.now());
        testBudget.setUpdateBy(testUserId);
        testBudget.setUpdateTime(LocalDateTime.now());
        budgetRepository.save(testBudget);

        Long budgetId = testBudget.getId();

        // 2. 创建费用（超出预算预警设置）
        for (int i = 0; i < 3; i++) {
            Expense expense = new Expense();
            expense.setExpenseTitle("运营费用-" + i);
            expense.setExpenseType("OPERATION");
            expense.setApplyAmount(new BigDecimal("40000.00"));
            expense.setBudgetId(budgetId);
            expense.setDeptId(testDeptId);
            expense.setApplyUserId(testUserId);
            expense.setExpenseStatus(1); // 已审批
            expense.setCreateBy(testUserId);
            expense.setCreateTime(LocalDateTime.now());
            expense.setUpdateBy(testUserId);
            expense.setUpdateTime(LocalDateTime.now());
            expenseRepository.save(expense);
        }

        // 3. 预算超支预警测试
        mockMvc.perform(get("/api/budgets/" + budgetId + "/execution")
                        .header("Authorization", "Bearer test-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalAmount").value(200000))
                .andExpect(jsonPath("$.usedAmount").value(120000));

        // 4. 预算调整
        Map<String, Object> adjustmentRequest = Map.of(
                "newAmount", 300000.00,
                "reason", "业务扩展需要"
        );

        mockMvc.perform(put("/api/budgets/" + budgetId + "/adjust")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer test-token")
                        .content(objectMapper.writeValueAsString(adjustmentRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("预算调整成功"));

        // 5. 验证预算已调整
        Budget adjustedBudget = budgetRepository.findById(budgetId).orElse(null);
        assertNotNull(adjustedBudget);
        assertEquals(new BigDecimal("300000.00"), adjustedBudget.getTotalAmount());

        // 6. 设置预算预警
        Map<String, Object> warningRequest = Map.of(
                "threshold", 80.0,
                "notifyType", "EMAIL"
        );

        mockMvc.perform(put("/api/budgets/" + budgetId + "/warning")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer test-token")
                        .content(objectMapper.writeValueAsString(warningRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("预算预警设置成功"));
    }

    @Test
    void testBudgetStatisticsAndReporting() throws Exception {
        // 1. 创建多个年度预算用于统计
        for (int year = 2022; year <= 2024; year++) {
            Budget budget = new Budget();
            budget.setBudgetYear(year);
            budget.setBudgetCode("BGT-" + year + "-STAT");
            budget.setBudgetName(year + "年度预算");
            budget.setTotalAmount(new BigDecimal(year * 100000.00));
            budget.setDeptId(testDeptId);
            budget.setBudgetStatus(1);
            budget.setCreateBy(testUserId);
            budget.setCreateTime(LocalDateTime.now());
            budget.setUpdateBy(testUserId);
            budget.setUpdateTime(LocalDateTime.now());
            budgetRepository.save(budget);

            // 创建关联的费用
            Expense expense = new Expense();
            expense.setExpenseTitle(year + "年度费用");
            expense.setExpenseType("OPERATION");
            expense.setApplyAmount(new BigDecimal(year * 50000.00));
            expense.setBudgetId(budget.getId());
            expense.setDeptId(testDeptId);
            expense.setApplyUserId(testUserId);
            expense.setExpenseStatus(1);
            expense.setCreateBy(testUserId);
            expense.setCreateTime(LocalDateTime.now());
            expense.setUpdateBy(testUserId);
            expense.setUpdateTime(LocalDateTime.now());
            expenseRepository.save(expense);
        }

        // 2. 测试年度预算统计
        mockMvc.perform(get("/api/budgets/statistics/annual")
                        .param("year", "2024")
                        .header("Authorization", "Bearer test-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.year").value(2024));

        // 3. 测试部门预算统计
        mockMvc.perform(get("/api/budgets/statistics/department")
                        .param("deptId", testDeptId.toString())
                        .param("year", "2024")
                        .header("Authorization", "Bearer test-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.deptId").value(testDeptId));

        // 4. 测试预算列表查询
        mockMvc.perform(get("/api/budgets")
                        .param("year", "2024")
                        .param("deptId", testDeptId.toString())
                        .header("Authorization", "Bearer test-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].budgetYear").value(2024));
    }

    @Test
    void testBudgetApprovalWorkflow() throws Exception {
        // 1. 创建预算
        Map<String, Object> budgetRequest = Map.of(
                "budgetYear", 2024,
                "budgetCode", "BGT-2024-WORKFLOW",
                "budgetName", "审批流程测试预算",
                "totalAmount", 150000.00,
                "deptId", testDeptId,
                "createBy", testUserId
        );

        mockMvc.perform(post("/api/budgets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer test-token")
                        .content(objectMapper.writeValueAsString(budgetRequest)))
                .andExpect(status().isOk());

        // 2. 获取预算ID
        Budget draftBudget = budgetRepository.findByBudgetCode("BGT-2024-WORKFLOW")
                .orElseThrow();
        Long budgetId = draftBudget.getId();

        // 3. 预算更新
        Map<String, Object> updateRequest = Map.of(
                "budgetName", "更新的审批流程测试预算",
                "totalAmount", 180000.00
        );

        mockMvc.perform(put("/api/budgets/" + budgetId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer test-token")
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("预算更新成功"));

        // 4. 提交审批（模拟审批流程）
        mockMvc.perform(post("/api/budgets/" + budgetId + "/submit")
                        .header("Authorization", "Bearer test-token"))
                .andExpect(status().isOk());

        // 5. 审批驳回测试
        Map<String, Object> rejectRequest = Map.of(
                "result", 2,
                "comment", "预算金额过大，请重新评估",
                "approverId", testApproverId
        );

        mockMvc.perform(post("/api/budgets/" + budgetId + "/approve")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer test-token")
                        .content(objectMapper.writeValueAsString(rejectRequest)))
                .andExpect(status().isOk());

        // 6. 验证预算状态为驳回
        Budget rejectedBudget = budgetRepository.findById(budgetId).orElse(null);
        assertNotNull(rejectedBudget);
        assertEquals(2, rejectedBudget.getBudgetStatus()); // 驳回状态

        // 7. 重新调整并再次提交
        Map<String, Object> reAdjustRequest = Map.of(
                "newAmount", 120000.00,
                "reason", "根据审批意见调整预算"
        );

        mockMvc.perform(put("/api/budgets/" + budgetId + "/adjust")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer test-token")
                        .content(objectMapper.writeValueAsString(reAdjustRequest)))
                .andExpect(status().isOk());

        // 8. 再次提交审批
        mockMvc.perform(post("/api/budgets/" + budgetId + "/submit")
                        .header("Authorization", "Bearer test-token"))
                .andExpect(status().isOk());

        // 9. 最终审批通过
        Map<String, Object> finalApprovalRequest = Map.of(
                "result", 1,
                "comment", "预算调整后符合要求，审批通过",
                "approverId", testApproverId
        );

        mockMvc.perform(post("/api/budgets/" + budgetId + "/approve")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer test-token")
                        .content(objectMapper.writeValueAsString(finalApprovalRequest)))
                .andExpect(status().isOk());

        // 10. 验证最终状态
        Budget finalBudget = budgetRepository.findById(budgetId).orElse(null);
        assertNotNull(finalBudget);
        assertEquals(1, finalBudget.getBudgetStatus()); // 最终审批通过
        assertEquals(new BigDecimal("120000.00"), finalBudget.getTotalAmount());
    }

    @Test
    void testBudgetExpenseIntegration() throws Exception {
        // 1. 创建预算并审批通过
        Budget testBudget = new Budget();
        testBudget.setBudgetYear(2024);
        testBudget.setBudgetCode("BGT-2024-INTEGRATION");
        testBudget.setBudgetName("集成测试预算");
        testBudget.setTotalAmount(new BigDecimal("100000.00"));
        testBudget.setDeptId(testDeptId);
        testBudget.setBudgetStatus(1);
        testBudget.setCreateBy(testUserId);
        testBudget.setCreateTime(LocalDateTime.now());
        testBudget.setUpdateBy(testUserId);
        testBudget.setUpdateTime(LocalDateTime.now());
        budgetRepository.save(testBudget);

        Long budgetId = testBudget.getId();

        // 2. 创建多个费用申请
        for (int i = 1; i <= 5; i++) {
            Expense expense = new Expense();
            expense.setExpenseTitle("集成测试费用-" + i);
            expense.setExpenseType("DEVELOPMENT");
            expense.setApplyAmount(new BigDecimal("8000.00"));
            expense.setBudgetId(budgetId);
            expense.setDeptId(testDeptId);
            expense.setApplyUserId(testUserId);
            expense.setExpenseStatus(1);
            expense.setCreateBy(testUserId);
            expense.setCreateTime(LocalDateTime.now());
            expense.setUpdateBy(testUserId);
            expense.setUpdateTime(LocalDateTime.now());
            expenseRepository.save(expense);
        }

        // 3. 测试预算使用率监控
        mockMvc.perform(get("/api/budgets/" + budgetId + "/execution")
                        .header("Authorization", "Bearer test-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.budgetId").value(budgetId))
                .andExpect(jsonPath("$.totalAmount").value(100000))
                .andExpect(jsonPath("$.usedAmount").value(40000));

        // 4. 测试预算预警功能（设置80%预警线）
        Map<String, Object> warningRequest = Map.of(
                "threshold", 50.0,
                "notifyType", "SYSTEM"
        );

        mockMvc.perform(put("/api/budgets/" + budgetId + "/warning")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer test-token")
                        .content(objectMapper.writeValueAsString(warningRequest)))
                .andExpect(status().isOk());

        // 5. 测试费用查询与预算关联
        mockMvc.perform(get("/api/expenses")
                        .param("budgetId", budgetId.toString())
                        .header("Authorization", "Bearer test-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(5));
    }
}