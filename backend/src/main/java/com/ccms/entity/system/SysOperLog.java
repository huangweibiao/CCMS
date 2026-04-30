package com.ccms.entity.system;

import com.ccms.entity.BaseEntity;
import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 操作日志表实体类
 * 对应表名：ccms_sys_oper_log
 * 
 * @author 系统生成
 */
@Entity
@Table(name = "ccms_sys_oper_log")
public class SysOperLog extends BaseEntity {

    /**
     * 模块标题
     */
    @Column(name = "title", length = 256, nullable = false)
    private String title;
    
    /**
     * 业务类型：0=新增,1=修改,2=删除,3=查询,4=导入,5=导出,9=其他
     */
    @Column(name = "business_type", nullable = false)
    private Integer businessType;
    
    /**
     * 方法名称
     */
    @Column(name = "method", length = 128)
    private String method;
    
    /**
     * 请求方式
     */
    @Column(name = "request_method", length = 16)
    private String requestMethod;
    
    /**
     * 操作类别：0=后台用户,1=前台用户
     */
    @Column(name = "operator_type")
    private Integer operatorType;
    
    /**
     * 操作人ID
     */
    @Column(name = "oper_user_id", nullable = false)
    private String operUserId;
    
    /**
     * 操作人名称
     */
    @Column(name = "oper_name", length = 64)
    private String operName;
    
    /**
     * 部门名称
     */
    @Column(name = "dept_name", length = 64)
    private String deptName;
    
    /**
     * 操作模块
     */
    @Column(name = "oper_module", length = 64, nullable = false)
    private String operModule;
    
    /**
     * 操作类型：新增/修改/删除/查询/审批
     */
    @Column(name = "oper_type", length = 32, nullable = false)
    private String operType;

    /**
     * 操作内容
     */
    @Column(name = "oper_content", length = 512)
    private String operContent;
    
    /**
     * 请求URL
     */
    @Column(name = "oper_url", length = 512)
    private String operUrl;
    
    /**
     * 操作IP
     */
    @Column(name = "oper_ip", length = 64)
    private String operIp;
    
    /**
     * 操作地点
     */
    @Column(name = "oper_location", length = 128)
    private String operLocation;
    
    /**
     * 请求参数
     */
    @Column(name = "oper_param", length = 2048)
    private String operParam;
    
    /**
     * 返回参数
     */
    @Column(name = "json_result", length = 2048)
    private String jsonResult;
    
    /**
     * 操作状态：0=正常,1=异常
     */
    @Column(name = "status", nullable = false)
    private Integer status;
    
    /**
     * 错误消息
     */
    @Column(name = "error_msg", length = 2048)
    private String errorMsg;
    
    /**
     * 执行耗时（毫秒）
     */
    @Column(name = "cost_time")
    private Long costTime;
    
    /**
     * 业务模块
     */
    @Column(name = "business_module", length = 64)
    private String businessModule;
    
    /**
     * 业务ID
     */
    @Column(name = "business_id", length = 64)
    private String businessId;
    
    /**
     * 操作时间
     */
    @Column(name = "oper_time", nullable = false)
    private LocalDateTime operTime;
    
    /**
     * 操作设备信息
     */
    @Column(name = "device_info", length = 256)
    private String deviceInfo;
    
    /**
     * 用户代理
     */
    @Column(name = "user_agent", length = 512)
    private String userAgent;

    // Getters and Setters
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Integer getBusinessType() {
        return businessType;
    }

    public void setBusinessType(Integer businessType) {
        this.businessType = businessType;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getRequestMethod() {
        return requestMethod;
    }

    public void setRequestMethod(String requestMethod) {
        this.requestMethod = requestMethod;
    }

    public Integer getOperatorType() {
        return operatorType;
    }

    public void setOperatorType(Integer operatorType) {
        this.operatorType = operatorType;
    }

    public String getOperUserId() {
        return operUserId;
    }

    public void setOperUserId(String operUserId) {
        this.operUserId = operUserId;
    }

    public String getOperName() {
        return operName;
    }

    public void setOperName(String operName) {
        this.operName = operName;
    }

    public String getDeptName() {
        return deptName;
    }

    public void setDeptName(String deptName) {
        this.deptName = deptName;
    }

    public String getOperModule() {
        return operModule;
    }

    public void setOperModule(String operModule) {
        this.operModule = operModule;
    }

    public String getOperType() {
        return operType;
    }

    public void setOperType(String operType) {
        this.operType = operType;
    }

    public String getOperUrl() {
        return operUrl;
    }

    public void setOperUrl(String operUrl) {
        this.operUrl = operUrl;
    }

    public String getOperIp() {
        return operIp;
    }

    public void setOperIp(String operIp) {
        this.operIp = operIp;
    }

    public String getOperLocation() {
        return operLocation;
    }

    public void setOperLocation(String operLocation) {
        this.operLocation = operLocation;
    }

    public String getOperParam() {
        return operParam;
    }

    public void setOperParam(String operParam) {
        this.operParam = operParam;
    }

    public String getJsonResult() {
        return jsonResult;
    }

    public void setJsonResult(String jsonResult) {
        this.jsonResult = jsonResult;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }

    public Long getCostTime() {
        return costTime;
    }

    public void setCostTime(Long costTime) {
        this.costTime = costTime;
    }

    public String getBusinessModule() {
        return businessModule;
    }

    public void setBusinessModule(String businessModule) {
        this.businessModule = businessModule;
    }

    public String getBusinessId() {
        return businessId;
    }

    public void setBusinessId(String businessId) {
        this.businessId = businessId;
    }

    public LocalDateTime getOperTime() {
        return operTime;
    }

    public void setOperTime(LocalDateTime operTime) {
        this.operTime = operTime;
    }

    public String getDeviceInfo() {
        return deviceInfo;
    }

    public void setDeviceInfo(String deviceInfo) {
        this.deviceInfo = deviceInfo;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public String getOperContent() {
        return operContent;
    }

    public void setOperContent(String operContent) {
        this.operContent = operContent;
    }

    @Override
    public String toString() {
        return "SysOperLog{" +
                "id=" + getId() +
                ", title='" + title + '\'' +
                ", businessType=" + businessType +
                ", operUserId='" + operUserId + '\'' +
                ", operName='" + operName + '\'' +
                ", operModule='" + operModule + '\'' +
                ", operType='" + operType + '\'' +
                ", operContent='" + operContent + '\'' +
                ", businessModule='" + businessModule + '\'' +
                ", businessId='" + businessId + '\'' +
                ", operIp='" + operIp + '\'' +
                ", status=" + status +
                ", costTime=" + costTime +
                ", operTime=" + operTime +
                ", createTime=" + getCreateTime() +
                ", updateTime=" + getUpdateTime() +
                ", version=" + getVersion() +
                '}';
    }
}