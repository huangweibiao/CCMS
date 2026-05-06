package com.ccms.common.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 统一API响应包装器
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> implements Serializable {
    
    // 手动添加 getter 方法确保 Lombok 生成正确的方法
    public Integer getCode() { return code; }
    public String getMessage() { return message; }
    public T getData() { return data; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public String getPath() { return path; }
    public Long getDuration() { return duration; }

    /**
     * 响应状态码
     */
    private Integer code;

    /**
     * 响应消息
     */
    private String message;

    /**
     * 响应数据
     */
    private T data;

    /**
     * 响应时间
     */
    private LocalDateTime timestamp;

    /**
     * 请求路径
     */
    private String path;

    /**
     * 处理耗时(毫秒)
     */
    private Long duration;

    /**
     * 成功响应
     */
    public static <T> ApiResponse<T> success() {
        return success(null);
    }



    /**
     * 成功响应
     */
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(200, "操作成功", data, LocalDateTime.now(), null, null);
    }

    /**
     * 失败响应
     */
    public static <T> ApiResponse<T> error(String message) {
        return error(500, message);
    }

    /**
     * 失败响应
     */
    public static <T> ApiResponse<T> error(Integer code, String message) {
        return new ApiResponse<>(code, message, null, LocalDateTime.now(), null, null);
    }

    /**
     * 创建响应并设置路径
     */
    public ApiResponse<T> path(String path) {
        this.path = path;
        return this;
    }

    /**
     * 创建响应并设置耗时
     */
    public ApiResponse<T> duration(Long duration) {
        this.duration = duration;
        return this;
    }
}