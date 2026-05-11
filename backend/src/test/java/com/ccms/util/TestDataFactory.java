package com.ccms.util;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 测试数据工厂类
 * 用于生成各种测试实体对象和通用测试数据
 */
public class TestDataFactory {

    private static Long idCounter = 1L;

    /**
     * 生成唯一ID
     */
    public static Long generateId() {
        return idCounter++;
    }

    /**
     * 生成编码
     */
    public static String generateCode(String prefix) {
        return prefix + System.currentTimeMillis();
    }

    /**
     * 生成随机字符串
     */
    public static String generateRandomString(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < length; i++) {
            int index = (int) (Math.random() * chars.length());
            result.append(chars.charAt(index));
        }
        return result.toString();
    }

    /**
     * 生成随机邮箱
     */
    public static String generateRandomEmail() {
        return "test-" + generateRandomString(8) + "@example.com";
    }

    /**
     * 生成随机手机号
     */
    public static String generateRandomPhone() {
        return "138" + String.format("%08d", (int)(Math.random() * 100000000));
    }

    /**
     * 生成随机金额
     */
    public static BigDecimal generateRandomAmount() {
        return new BigDecimal(String.format("%.2f", Math.random() * 10000));
    }

    /**
     * 生成测试用户数据
     */
    public static Map<String, Object> createUserData() {
        Map<String, Object> user = new HashMap<>();
        user.put("id", generateId());
        user.put("username", "testuser" + generateRandomString(4));
        user.put("name", "测试用户" + generateRandomString(2));
        user.put("email", generateRandomEmail());
        user.put("phone", generateRandomPhone());
        user.put("status", 1);
        user.put("deptId", 1L);
        return user;
    }

    /**
     * 生成费用申请数据
     */
    public static Map<String, Object> createExpenseApplyData() {
        Map<String, Object> expense = new HashMap<>();
        expense.put("applyNo", generateCode("EXP"));
        expense.put("applyType", "TRAVEL");
        expense.put("applyAmount", generateRandomAmount());
        expense.put("applyUserId", 1L);
        expense.put("applyUserName", "测试用户");
        expense.put("applyDeptId", 1L);
        expense.put("applyDate", LocalDate.now());
        expense.put("applyStatus", 0);
        return expense;
    }

    /**
     * 生成借款申请数据
     */
    public static Map<String, Object> createLoanData() {
        Map<String, Object> loan = new HashMap<>();
        loan.put("loanNo", generateCode("LOAN"));
        loan.put("loanType", 1);
        loan.put("loanAmount", generateRandomAmount().multiply(new BigDecimal("5")));
        loan.put("loanUserId", 1L);
        loan.put("loanUserName", "测试用户");
        loan.put("loanDate", LocalDate.now());
        loan.put("loanStatus", 0);
        return loan;
    }

    /**
     * 生成预算数据
     */
    public static Map<String, Object> createBudgetData() {
        Map<String, Object> budget = new HashMap<>();
        budget.put("budgetNo", generateCode("BG"));
        budget.put("budgetYear", LocalDate.now().getYear());
        budget.put("budgetType", "DEPT");
        budget.put("totalAmount", new BigDecimal("100000.00"));
        budget.put("deptId", 1L);
        budget.put("budgetStatus", 0);
        return budget;
    }

    /**
     * 生成审批流配置数据
     */
    public static Map<String, Object> createApprovalFlowData() {
        Map<String, Object> flow = new HashMap<>();
        flow.put("flowCode", generateCode("FLOW"));
        flow.put("flowName", "测试审批流");
        flow.put("businessType", "EXPENSE");
        flow.put("minAmount", new BigDecimal("0"));
        flow.put("maxAmount", new BigDecimal("10000"));
        flow.put("status", 1);
        return flow;
    }

    /**
     * 生成分页参数
     */
    public static Map<String, Object> createPageParams(int page, int size) {
        Map<String, Object> params = new HashMap<>();
        params.put("page", page);
        params.put("size", size);
        return params;
    }

    /**
     * 生成批量测试数据
     */
    public static <T> List<T> createBatchData(java.util.function.Supplier<T> supplier, int count) {
        List<T> list = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            list.add(supplier.get());
        }
        return list;
    }

    /**
     * 重置ID计数器（用于测试隔离）
     */
    public static void resetIdCounter() {
        idCounter = 1L;
    }

    /**
     * 生成测试令牌
     */
    public static String createTestToken() {
        return "Bearer test-token-" + System.currentTimeMillis();
    }

    /**
     * 生成包含特定数据的Map
     */
    public static Map<String, Object> createMap(String... keyValuePairs) {
        if (keyValuePairs.length % 2 != 0) {
            throw new IllegalArgumentException("键值对数量必须为偶数");
        }
        
        Map<String, Object> map = new HashMap<>();
        for (int i = 0; i < keyValuePairs.length; i += 2) {
            map.put(keyValuePairs[i], keyValuePairs[i + 1]);
        }
        return map;
    }

    /**
     * 生成测试用的日期范围
     */
    public static Map<String, LocalDate> createDateRange(int daysAgo) {
        Map<String, LocalDate> range = new HashMap<>();
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(daysAgo);
        range.put("startDate", startDate);
        range.put("endDate", endDate);
        return range;
    }

    /**
     * 生成测试响应数据
     */
    public static Map<String, Object> createResponse(boolean success, String message, Object data) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", success);
        response.put("message", message);
        response.put("data", data);
        return response;
    }

    /**
     * 生成错误响应数据
     */
    public static Map<String, Object> createErrorResponse(String errorMessage) {
        return createResponse(false, errorMessage, null);
    }

    /**
     * 生成成功响应数据
     */
    public static Map<String, Object> createSuccessResponse(Object data) {
        return createResponse(true, "操作成功", data);
    }
}