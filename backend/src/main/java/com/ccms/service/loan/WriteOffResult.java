package com.ccms.service.loan;

import java.math.BigDecimal;

/**
 * 借款核销结果类
 */
public class WriteOffResult {
    private boolean success;
    private BigDecimal writeOffAmount;
    private BigDecimal remainingBalance;
    private String message;
    
    public WriteOffResult(boolean success, BigDecimal writeOffAmount, BigDecimal remainingBalance, String message) {
        this.success = success;
        this.writeOffAmount = writeOffAmount;
        this.remainingBalance = remainingBalance;
        this.message = message;
    }
    
    // Getters
    public boolean isSuccess() { return success; }
    public BigDecimal getWriteOffAmount() { return writeOffAmount; }
    public BigDecimal getRemainingBalance() { return remainingBalance; }
    public String getMessage() { return message; }
}