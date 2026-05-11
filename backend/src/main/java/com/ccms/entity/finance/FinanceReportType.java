package com.ccms.entity.finance;

/**
 * 财务报表类型枚举
 */
public enum FinanceReportType {

    /**
     * 费用报表
     */
    EXPENSE_REPORT("费用报表", 1),

    /**
     * 支付报表
     */
    PAYMENT_REPORT("支付报表", 2),

    /**
     * 部门报表
     */
    DEPARTMENT_REPORT("部门报表", 3),

    /**
     * 科目报表
     */
    ACCOUNT_REPORT("科目报表", 4),

    /**
     * 现金流报表
     */
    CASH_FLOW_REPORT("现金流报表", 5),

    /**
     * 利润报表
     */
    PROFIT_REPORT("利润报表", 6),

    /**
     * 资产负债报表
     */
    BALANCE_SHEET_REPORT("资产负债报表", 7);

    private final String name;
    private final Integer code;

    FinanceReportType(String name, Integer code) {
        this.name = name;
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public Integer getCode() {
        return code;
    }
}