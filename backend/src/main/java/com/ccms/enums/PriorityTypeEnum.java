package com.ccms.enums;

/**
 * 优先级类型枚举
 */
public enum PriorityTypeEnum {
    LOW("LOW", "低"),
    MEDIUM("MEDIUM", "中"),
    HIGH("HIGH", "高"),
    URGENT("URGENT", "紧急");

    private final String code;
    private final String description;

    PriorityTypeEnum(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public static PriorityTypeEnum fromCode(String code) {
        for (PriorityTypeEnum type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        return null;
    }
}