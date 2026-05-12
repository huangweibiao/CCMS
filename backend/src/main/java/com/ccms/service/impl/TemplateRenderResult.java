package com.ccms.service.impl;

import java.util.Objects;

public class TemplateRenderResult {
    private String content;
    private boolean success;
    private String error;
    
    public TemplateRenderResult() {
        this.content = "";
        this.success = true;
        this.error = "";
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TemplateRenderResult that = (TemplateRenderResult) o;
        return success == that.success && 
               Objects.equals(content, that.content) && 
               Objects.equals(error, that.error);
    }

    @Override
    public int hashCode() {
        return Objects.hash(content, success, error);
    }

    @Override
    public String toString() {
        return "TemplateRenderResult{" +
                "content='" + content + '\'' +
                ", success=" + success +
                ", error='" + error + '\'' +
                '}';
    }
}