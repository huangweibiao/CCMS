package com.ccms.entity.approval;

import com.ccms.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import jakarta.persistence.*;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "approval_node")
public class ApprovalNode extends BaseEntity {
    
    @Column(name = "process_id", nullable = false)
    private Long processId;
    
    @Column(name = "node_code", nullable = false, length = 50)
    private String nodeCode;
    
    @Column(name = "node_name", nullable = false, length = 200)
    private String nodeName;
    
    @Column(name = "node_type", nullable = false, length = 20)
    private String nodeType; // START, NORMAL, END, CONDITION
    
    @Column(name = "step_number", nullable = false)
    private Integer stepNumber;
    
    @Column(name = "approver_type", length = 20)
    private String approverType; // USER, ROLE, DEPT
    
    @Column(name = "approver_id")
    private Long approverId;
    
    @Column(name = "approver_name", length = 100)
    private String approverName;
    
    @Column(name = "condition_expression", length = 1000)
    private String conditionExpression;
    
    @Column(name = "is_required", columnDefinition = "tinyint(1) default 1")
    private Boolean required = true;
    
    @Column(name = "time_limit")
    private Integer timeLimit; // 小时数
    
    @Column(name = "next_node_id")
    private Long nextNodeId;
    
    @Column(name = "remark", length = 500)
    private String remark;
}