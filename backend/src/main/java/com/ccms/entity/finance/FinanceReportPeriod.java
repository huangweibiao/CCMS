package com.ccms.entity.finance;

/**
 * 财务报表周期枚举
 */
public enum FinanceReportPeriod {

    /**
     * 日报表
     */
    DAILY("日", 1),

    /**
     * 周报表
     */
    WEEKLY("周", 2),

    /**
     * 月报表
     */
    MONTHLY("月", 3),

    /**
     * 季度报表
     */
    QUARTERLY("季度", 4),

    /**
     * 年报表
     */
    YEARLY("年", 5);

    private final String name;
    private final Integer code;

    FinanceReportPeriod(String name, Integer code) {
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