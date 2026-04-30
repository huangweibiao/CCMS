package com.ccms.service;

/**
 * 发票验真服务状态
 * 
 * @author 系统生成
 */
public class VerifyServiceStatus {
    
    private boolean available;
    private String provider;
    private int remainingQuota;
    private long lastUsedTime;
    
    // 构造函数
    public VerifyServiceStatus() {
    }
    
    public VerifyServiceStatus(boolean available, String provider, int remainingQuota, long lastUsedTime) {
        this.available = available;
        this.provider = provider;
        this.remainingQuota = remainingQuota;
        this.lastUsedTime = lastUsedTime;
    }
    
    // Getter和Setter方法
    public boolean isAvailable() {
        return available;
    }
    
    public void setAvailable(boolean available) {
        this.available = available;
    }
    
    public String getProvider() {
        return provider;
    }
    
    public void setProvider(String provider) {
        this.provider = provider;
    }
    
    public int getRemainingQuota() {
        return remainingQuota;
    }
    
    public void setRemainingQuota(int remainingQuota) {
        this.remainingQuota = remainingQuota;
    }
    
    public long getLastUsedTime() {
        return lastUsedTime;
    }
    
    public void setLastUsedTime(long lastUsedTime) {
        this.lastUsedTime = lastUsedTime;
    }
}