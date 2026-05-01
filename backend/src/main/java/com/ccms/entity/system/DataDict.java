package com.ccms.entity.system;

import com.ccms.entity.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 数据字典实体类
 */
@Entity
@Table(name = "sys_data_dict")
public class DataDict extends BaseEntity {

    @NotBlank
    @Size(max = 50)
    private String dictType;

    @NotBlank
    @Size(max = 50)
    private String dictCode;

    @NotBlank
    @Size(max = 100)
    private String dictName;

    private String dictValue;

    private Integer status = 1;

    private Integer sortOrder = 0;

    private String remark;

    private boolean builtIn = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private DataDict parent;

    // 构造器
    public DataDict() {}

    public DataDict(String dictType, String dictCode, String dictName) {
        this.dictType = dictType;
        this.dictCode = dictCode;
        this.dictName = dictName;
    }

    // Getters and Setters
    public String getDictType() { return dictType; }
    public void setDictType(String dictType) { this.dictType = dictType; }

    public String getDictCode() { return dictCode; }
    public void setDictCode(String dictCode) { this.dictCode = dictCode; }

    public String getDictName() { return dictName; }
    public void setDictName(String dictName) { this.dictName = dictName; }

    public String getDictValue() { return dictValue; }
    public void setDictValue(String dictValue) { this.dictValue = dictValue; }

    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }

    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }

    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }

    public boolean isBuiltIn() { return builtIn; }
    public void setBuiltIn(boolean builtIn) { this.builtIn = builtIn; }

    public DataDict getParent() { return parent; }
    public void setParent(DataDict parent) { this.parent = parent; }

    /**
     * 检查是否为启用状态
     */
    @Transient
    public boolean isEnabled() {
        return status != null && status == 1;
    }

    /**
     * 检查是否为顶层字典项
     */
    @Transient
    public boolean isTopLevel() {
        return parent == null;
    }

    /**
     * 获取完整的字典类型路径
     */
    @Transient
    public String getFullDictType() {
        if (parent != null) {
            return parent.getFullDictType() + "/" + dictType;
        }
        return dictType;
    }
}