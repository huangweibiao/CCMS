package com.ccms.entity.expense;

import com.ccms.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "expense_detail")
public class ExpenseDetail extends BaseEntity {
    
    @Column(name = "expense_main_id", nullable = false)
    private Long expenseMainId;
    
    @Column(name = "item_no", nullable = false)
    private Integer itemNo;
    
    @Column(name = "expense_type_id")
    private Long expenseTypeId;
    
    @Column(name = "expense_type_name", length = 100)
    private String expenseTypeName;
    
    @Column(name = "description", nullable = false, length = 500)
    private String description;
    
    @Column(name = "amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;
    
    @Column(name = "occur_date", nullable = false)
    private LocalDate occurDate;
    
    @Column(name = "currency", length = 10)
    private String currency = "CNY";
    
    @Column(name = "remark", length = 500)
    private String remark;
    
    @Column(name = "is_approved", columnDefinition = "tinyint(1) default 0")
    private Boolean approved = false;
    
    @Column(name = "budget_item_id")
    private Long budgetItemId;
    
    @Column(name = "budget_item_name", length = 200)
    private String budgetItemName;
}