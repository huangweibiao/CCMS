package com.ccms.entity.finance;

import com.ccms.entity.BaseEntity;
import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * 财务凭证模板实体类
 * 对应表名：finance_voucher_template
 */
@Entity
@Table(name = "finance_voucher_template")
public class VoucherTemplate extends BaseEntity {

    /**
     * 主键ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    /**
     * 模板编码
     */
    @Column(name = "template_code", length = 32, nullable = false, unique = true)
    private String templateCode;

    /**
     * 模板名称
     */
    @Column(name = "template_name", length = 64, nullable = false)
    private String templateName;

    /**
     * 凭证类型：VOUCHER/RECEIPT/TRANSFER/PAYMENT
     */
    @Column(name = "voucher_type", length = 16, nullable = false)
    private String voucherType;

    /**
     * 业务类型：EXPENSE/REIMBURSE/PAYMENT/OTHER
     */
    @Column(name = "business_type", length = 32, nullable = false)
    private String businessType;

    /**
     * 模板内容（JSON格式）
     */
    @Lob
    @Column(name = "template_content", nullable = false, columnDefinition = "text")
    private String templateContent;

    /**
     * 状态：0-禁用 1-启用
     */
    @Column(name = "status", nullable = false)
    private Integer status;

    /**
     * 备注
     */
    @Column(name = "remark", length = 512)
    private String remark;

    /**
     * 创建人ID
     */
    @Column(name = "create_by", nullable = false)
    private Long createBy;

    /**
     * 创建时间
     */
    @Column(name = "create_time", nullable = false, updatable = false)
    private LocalDateTime createTime;

    /**
     * 更新人ID
     */
    @Column(name = "update_by")
    private Long updateBy;

    /**
     * 更新时间
     */
    @Column(name = "update_time")
    private LocalDateTime updateTime;

    /**
     * 乐观锁版本号
     */
    @Version
    @Column(name = "version", nullable = false)
    private Integer version;

    // getter和setter方法
    public Long getId() {
        return id;
    }

    public String getTemplateCode() {
        return templateCode;
    }

    public String getTemplateName() {
        return templateName;
    }

    public String getVoucherType() {
        return voucherType;
    }

    public String getBusinessType() {
        return businessType;
    }

    public String getTemplateContent() {
        return templateContent;
    }

    public Integer getStatus() {
        return status;
    }

    public String getRemark() {
        return remark;
    }

    public Long getCreateBy() {
        return createBy;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public Long getUpdateBy() {
        return updateBy;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public Integer getVersion() {
        return version;
    }

    public void setTemplateCode(String templateCode) {
        this.templateCode = templateCode;
    }

    public void setTemplateName(String templateName) {
        this.templateName = templateName;
    }

    public void setVoucherType(String voucherType) {
        this.voucherType = voucherType;
    }

    public void setBusinessType(String businessType) {
        this.businessType = businessType;
    }

    public void setTemplateContent(String templateContent) {
        this.templateContent = templateContent;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public void setCreateBy(Long createBy) {
        this.createBy = createBy;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public void setUpdateBy(Long updateBy) {
        this.updateBy = updateBy;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }
}