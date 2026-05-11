package com.ccms.entity.finance;

/**
 * 财务支付方式枚举
 */
public enum FinancePaymentMethod {

    /**
     * 转账支付
     */
    BANK_TRANSFER("转账", 1),

    /**
     * 支票支付
     */
    CHECK("支票", 2),

    /**
     * 汇票支付
     */
    CASH("现金", 3),

    /**
     * 电子支付
     */
    ELECTRONIC("电子支付", 4),

    /**
     * 银行卡支付
     */
    CREDIT_CARD("银行卡", 5),

    /**
     * 网银支付
     */
    ONLINE_BANK("网银", 6),

    /**
     * 第三方支付
     */
    THIRD_PARTY("第三方支付", 7),

    /**
     * 其他支付方式
     */
    OTHER("其他", 8);

    private final String name;
    private final Integer code;

    FinancePaymentMethod(String name, Integer code) {
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