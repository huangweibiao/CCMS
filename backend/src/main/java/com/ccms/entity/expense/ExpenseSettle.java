package com.ccms.entity.expense;

import com.ccms.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "expense_settle")
public class ExpenseSettle extends BaseEntity {
    
    @Column(name = "settle_no", nullable = false, unique = true, length = 50)
    private String settleNo;
    
    @Column(name = "expense_apply_id", nullable = false)
    private Long expenseApplyId;
    
    @Column(name = "apply_no", length = 50)
    private String applyNo;
    
    @Column(name = "title", nullable = false, length = 200)
    private String title;
    
    @Column(name = "apply_user_id", nullable = false)
    private Long applyUserId;
    
    @Column(name = "apply_user_name", nullable = false, length = 100)
    private String applyUserName;
    
    @Column(name = "dept_id")
    private Long deptId;
    
    @Column(name = "dept_name", length = 100)
    private String deptName;
    
    @Column(name = "settle_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal settleAmount;
    
    @Column(name = "currency", length = 10)
    private String currency = "CNY";
    
    @Column(name = "settle_date", nullable = false)
    private LocalDate settleDate;
    
    @Column(name = "status", nullable = false)
    private Integer status; // 0-草稿 1-已提交 2-结算中 3-已完成 4-已取消
    
    @Column(name = "payment_method", length = 50)
    private String paymentMethod;
    
    @Column(name = "bank_account", length = 100)
    private String bankAccount;
    
    @Column(name = "bank_name", length = 100)
    private String bankName;
    
    @Column(name = "submit_time")
    private LocalDateTime submitTime;
    
    @Column(name = "complete_time")
    private LocalDateTime completeTime;
    
    @Column(name = "cancel_time")
    private LocalDateTime cancelTime;
    
    @Column(name = "cancel_reason", length = 500)
    private String cancelReason;
    
    @Column(name = "handler_id")
    private Long handlerId;
    
    @Column(name = "handler_name", length = 100)
    private String handlerName;
    
    @Column(name = "remark", length = 1000)
    private String remark;
}