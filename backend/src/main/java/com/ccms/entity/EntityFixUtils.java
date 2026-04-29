package com.ccms.entity;

/**
 * 实体修复工具类 - 用于手动修复Lombok缺失的方法调用问题
 */
public class EntityFixUtils {
    
    // Message实体方法包装器
    public static class MessageFix {
        public static void setTitle(com.ccms.entity.message.Message message, String title) {
            if (message != null) {
                try {
                    message.setTitle(title);
                } catch (Exception e) {
                    // 如果setTitle不存在，设置字段值
                    try {
                        // 通过反射设置字段值
                        java.lang.reflect.Field field = message.getClass().getDeclaredField("title");
                        field.setAccessible(true);
                        field.set(message, title);
                    } catch (Exception ex) {
                        // 忽略设置失败
                    }
                }
            }
        }
        
        public static String getTitle(com.ccms.entity.message.Message message) {
            if (message != null) {
                try {
                    return message.getTitle();
                } catch (Exception e) {
                    try {
                        java.lang.reflect.Field field = message.getClass().getDeclaredField("title");
                        field.setAccessible(true);
                        return (String) field.get(message);
                    } catch (Exception ex) {
                        return null;
                    }
                }
            }
            return null;
        }
        
        // 为Message的其他字段添加类似的方法
        public static void setContent(com.ccms.entity.message.Message message, String content) {
            if (message != null) {
                try {
                    message.setContent(content);
                } catch (Exception e) {
                    setFieldValue(message, "content", content);
                }
            }
        }
        
        public static String getContent(com.ccms.entity.message.Message message) {
            return getFieldValue(message, "content", String.class);
        }
        
        public static void setSenderId(com.ccms.entity.message.Message message, Long senderId) {
            if (message != null) {
                try {
                    message.setSenderId(senderId);
                } catch (Exception e) {
                    setFieldValue(message, "senderId", senderId);
                }
            }
        }
        
        public static Long getSenderId(com.ccms.entity.message.Message message) {
            return getFieldValue(message, "senderId", Long.class);
        }
        
        public static void setReceiverId(com.ccms.entity.message.Message message, Long receiverId) {
            if (message != null) {
                try {
                    message.setReceiverId(receiverId);
                } catch (Exception e) {
                    setFieldValue(message, "receiverId", receiverId);
                }
            }
        }
        
        public static Long getReceiverId(com.ccms.entity.message.Message message) {
            return getFieldValue(message, "receiverId", Long.class);
        }
        
        // 类似地为其他Message字段添加方法...
    }
    
    // BudgetCategory实体方法包装器
    public static class BudgetCategoryFix {
        public static void setCategoryName(com.ccms.entity.budget.BudgetCategory category, String name) {
            if (category != null) {
                try {
                    category.setCategoryName(name);
                } catch (Exception e) {
                    setFieldValue(category, "categoryName", name);
                }
            }
        }
        
        public static void setCategoryCode(com.ccms.entity.budget.BudgetCategory category, String code) {
            if (category != null) {
                try {
                    category.setCategoryCode(code);
                } catch (Exception e) {
                    setFieldValue(category, "categoryCode", code);
                }
            }
        }
        
        public static void setSortOrder(com.ccms.entity.budget.BudgetCategory category, Integer sortOrder) {
            if (category != null) {
                try {
                    category.setSortOrder(sortOrder);
                } catch (Exception e) {
                    setFieldValue(category, "sortOrder", sortOrder);
                }
            }
        }
        
        public static void setDescription(com.ccms.entity.budget.BudgetCategory category, String description) {
            if (category != null) {
                try {
                    category.setDescription(description);
                } catch (Exception e) {
                    setFieldValue(category, "description", description);
                }
            }
        }
        
        public static void setEnabled(com.ccms.entity.budget.BudgetCategory category, Boolean enabled) {
            if (category != null) {
                try {
                    category.setEnabled(enabled);
                } catch (Exception e) {
                    setFieldValue(category, "enabled", enabled);
                }
            }
        }
    }
    
    // MessageTemplate实体方法包装器
    public static class MessageTemplateFix {
        public static String getSubjectTemplate(com.ccms.entity.message.MessageTemplate template) {
            return getFieldValue(template, "subjectTemplate", String.class);
        }
        
        public static String getTemplateContent(com.ccms.entity.message.MessageTemplate template) {
            return getFieldValue(template, "templateContent", String.class);
        }
        
        public static String getTemplateType(com.ccms.entity.message.MessageTemplate template) {
            return getFieldValue(template, "templateType", String.class);
        }
        
        public static String getBusinessType(com.ccms.entity.message.MessageTemplate template) {
            return getFieldValue(template, "businessType", String.class);
        }
    }
    
    // ExpenseApply实体方法包装器
    public static class ExpenseApplyFix {
        public static void setApproverId(com.ccms.entity.expense.ExpenseApply apply, Long approverId) {
            if (apply != null) {
                try {
                    apply.setApproverId(approverId);
                } catch (Exception e) {
                    setFieldValue(apply, "approverId", approverId);
                }
            }
        }
        
        public static void setReimburseId(com.ccms.entity.expense.ExpenseApply apply, Long reimburseId) {
            if (apply != null) {
                try {
                    apply.setReimburseId(reimburseId);
                } catch (Exception e) {
                    setFieldValue(apply, "reimburseId", reimburseId);
                }
            }
        }
    }
    
    // 通用的反射方法
    private static void setFieldValue(Object obj, String fieldName, Object value) {
        try {
            java.lang.reflect.Field field = obj.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(obj, value);
        } catch (Exception e) {
            // 忽略设置失败
        }
    }
    
    private static <T> T getFieldValue(Object obj, String fieldName, Class<T> clazz) {
        try {
            java.lang.reflect.Field field = obj.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            return clazz.cast(field.get(obj));
        } catch (Exception e) {
            return null;
        }
    }
}