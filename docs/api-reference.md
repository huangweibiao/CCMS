# CCMS API 接口文档

## 概述

企业级费控管理系统(CCMS)提供完整的RESTful API接口，支持费用申请、预算控制、审批流程等核心业务功能。

## 认证与授权

### 登录认证
```http
POST /api/auth/login
```

**请求体：**
```json
{
  "username": "admin",
  "password": "password123"
}
```

**响应：**
```json
{
  "code": 200,
  "message": "登录成功",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "user": {
      "id": 1,
      "username": "admin",
      "realName": "管理员",
      "department": "信息部"
    }
  }
}
```

## 预算管理接口

### 1. 获取预算列表
```http
GET /api/budgets
```

**查询参数：**
- `pageNum`: 页码（默认1）
- `pageSize`: 每页大小（默认10）
- `name`: 预算名称模糊查询
- `status`: 预算状态（ACTIVE/INACTIVE）

**响应：**
```json
{
  "code": 200,
  "message": "成功",
  "data": {
    "list": [
      {
        "id": 1,
        "name": "差旅费预算",
        "totalAmount": 50000.0,
        "usedAmount": 12000.0,
        "balance": 38000.0,
        "status": "ACTIVE",
        "startDate": "2024-01-01",
        "endDate": "2024-12-31"
      }
    ],
    "total": 15,
    "pageNum": 1,
    "pageSize": 10
  }
}
```

### 2. 创建预算
```http
POST /api/budgets
```

**请求体：**
```json
{
  "name": "办公费预算",
  "totalAmount": 20000.0,
  "startDate": "2024-01-01",
  "endDate": "2024-12-31",
  "description": "办公用品采购预算",
  "budgetDetails": [
    {
      "feeTypeId": 1,
      "amount": 10000.0
    }
  ]
}
```

### 3. 预算检查接口
```http
POST /api/budgets/check
```

**请求体：**
```json
{
  "budgetId": 1,
  "amount": 5000.0,
  "feeTypeId": 1
}
```

**响应：**
```json
{
  "code": 200,
  "message": "预算检查通过",
  "data": {
    "available": true,
    "balance": 15000.0,
    "exceeded": false
  }
}
```

## 费用申请接口

### 1. 创建费用申请
```http
POST /api/expense/apply
```

**请求体：**
```json
{
  "title": "北京出差费用申请",
  "budgetId": 1,
  "feeTypeId": 1,
  "totalAmount": 5000.0,
  "description": "参加技术研讨会费用",
  "expenseDetails": [
    {
      "itemName": "交通费",
      "amount": 2000.0,
      "description": "高铁票"
    },
    {
      "itemName": "住宿费", 
      "amount": 3000.0,
      "description": "酒店住宿"
    }
  ]
}
```

### 2. 获取申请列表
```http
GET /api/expense/apply
```

**查询参数：**
- `status`: 申请状态（PENDING/APPROVED/REJECTED）
- `applicantId`: 申请人ID
- `startDate`: 开始日期
- `endDate`: 结束日期

### 3. 获取申请详情
```http
GET /api/expense/apply/{id}
```

**响应：**
```json
{
  "code": 200,
  "message": "成功",
  "data": {
    "id": 1,
    "title": "北京出差费用申请",
    "status": "PENDING",
    "totalAmount": 5000.0,
    "applicant": {
      "id": 1,
      "realName": "张三"
    },
    "expenseDetails": [...],
    "approvalHistory": [...]
  }
}
```

## 审批流程接口

### 1. 获取待办任务
```http
GET /api/approval/todo
```

**响应：**
```json
{
  "code": 200,
  "message": "成功",
  "data": [
    {
      "id": 1,
      "title": "北京出差费用申请",
      "applicant": "张三",
      "amount": 5000.0,
      "status": "PENDING",
      "currentApprover": "李经理",
      "applyTime": "2024-01-15 10:30:00",
      "deadline": "2024-01-18 18:00:00"
    }
  ]
}
```

### 2. 审批操作
```http
POST /api/approval/{id}/approve
```

**请求体：**
```json
{
  "action": "APPROVE",
  "comment": "同意，符合公司差旅标准"
}
```

其他审批动作：
- `action: "REJECT"` - 驳回
- `action: "TRANSFER"` - 转审

### 3. 获取审批历史
```http
GET /api/approval/{id}/history
```

## 费用类型接口

### 1. 获取费用类型列表
```http
GET /api/fee-types
```

**响应：**
```json
{
  "code": 200,
  "message": "成功",
  "data": [
    {
      "id": 1,
      "name": "差旅费",
      "description": "出差相关费用",
      "requireInvoice": true,
      "requireBudgetControl": true
    }
  ]
}
```

## 发票管理接口

### 1. 上传发票
```http
POST /api/invoices/upload
Content-Type: multipart/form-data
```

**表单数据：**
- `file`: 发票图片文件
- `expenseApplyId`: 费用申请ID
- `amount`: 发票金额

## 消息通知接口

### 1. 获取通知列表
```http
GET /api/notifications
```

**响应：**
```json
{
  "code": 200,
  "message": "成功",
  "data": [
    {
      "id": 1,
      "title": "新的待办审批",
      "content": "您有1个新的费用申请需要审批",
      "type": "APPROVAL",
      "read": false,
      "createTime": "2024-01-15 14:30:00"
    }
  ]
}
```

## 系统管理接口

### 1. 获取用户列表
```http
GET /api/users
```

### 2. 获取部门列表
```http
GET /api/departments
```

## 错误码说明

| 错误码 | 说明 | 建议操作 |
|--------|------|----------|
| 200 | 成功 | - |
| 400 | 请求参数错误 | 检查请求参数格式 |
| 401 | 未授权 | 检查token是否有效 |
| 403 | 权限不足 | 确认用户权限 |
| 404 | 资源不存在 | 检查资源ID |
| 500 | 服务器内部错误 | 联系系统管理员 |

## 数据格式说明

### 时间格式
所有时间字段使用ISO 8601格式：`YYYY-MM-DDTHH:mm:ssZ`

### 金额格式
所有金额字段使用Decimal类型，精确到2位小数

### 分页格式
所有分页查询返回统一的分页格式

## 安全要求

1. 所有API请求必须包含有效的认证token
2. 敏感操作（如审批、金额修改）需要权限验证
3. 数据删除操作必须是软删除
4. 重要操作需要记录操作日志

## 限频策略

- 普通接口：60次/分钟
- 敏感接口：10次/分钟
- 上传接口：5次/分钟