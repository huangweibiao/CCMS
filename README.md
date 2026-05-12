# CCMS - 企业级费控管理系统

一个功能完整的企业级费用控制系统，覆盖费用申请、审批流转、预算控制、财务入账全流程管理。

## 🌟 项目介绍

CCMS（Corporate Cost Management System）是一款专门为企业设计的费用管控系统，帮助企业管理从费用申请到财务报销的全生命周期流程。系统支持灵活的审批流配置、智能预算控制、发票验真等企业级功能。

### 核心特性

- **📊 预算管理** - 完整的预算编制、调整、控制与执行跟踪
- **📝 费用申请** - 事前申请、借款申请、申请单跟踪管理
- **💰 费用报销** - 报销单填报、发票管理、费用分摊、借款核销
- **✅ 审批管理** - 灵活的审批流配置、待办/已办处理、审批记录
- **🧾 发票管理** - 发票识别（OCR）、验真、信息管理
- **📈 报表分析** - 费用分析、预算执行、部门统计、BI分析
- **🔐 权限控制** - 多级权限管理、数据隔离、操作审计

## 🏗️ 系统架构

### 技术栈

**后端技术栈**
- Java 21 + Spring Boot 3.5.11
- Spring Data JPA + MySQL
- Spring Security + JWT
- Redis缓存 + Email通知
- Apache POI（Excel导出）

**前端技术栈**
- Vue 3 + TypeScript
- Element Plus UI组件库
- Pinia状态管理
- Vue Router路由管理
- ECharts图表库

### 系统结构

```
CCMS/
├── backend/           # Spring Boot后端服务
├── frontend/          # Vue3前端项目
├── docs/             # 项目文档
├── ui-tests/         # UI自动化测试
├── scripts/          # 构建脚本
└── README.md         # 项目说明
```

## 🚀 快速开始

### 环境要求

- Java 21+
- MySQL 8.0+
- Redis 6.0+
- Node.js 18+
- Maven 3.6+

### 后端启动

1. **配置数据库**
   ```sql
   CREATE DATABASE ccms DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
   ```

2. **配置应用参数**
   修改 `backend/src/main/resources/application.yml` 中的数据库连接信息

3. **构建并启动**
   ```bash
   cd backend
   mvn clean package
   java -jar target/ccms-backend-1.0.0.jar
   ```

### 前端启动

1. **安装依赖**
   ```bash
   cd frontend
   npm install
   ```

2. **配置API地址**
   修改 `frontend/src/config/env.ts` 中的后端API地址

3. **启动开发服务器**
   ```bash
   npm run dev
   ```

### 一键启动（Windows）

```bash
# 执行PowerShell脚本
.\dev-start.bat

# 或执行批处理文件
.\build.bat
```

## 📋 功能模块

### 1. 用户与组织管理
- 用户管理（工号、姓名、部门、岗位）
- 部门管理（部门结构、负责人、启用状态）
- 角色权限（角色分配、权限控制）

### 2. 预算管理系统
- 预算编制（年度/季度/月度预算）
- 预算调整（追加/调减/转移）
- 预算控制（实时余额监控）
- 执行跟踪（已用/冻结/剩余金额）

### 3. 费用申请模块
- 普通申请（事前费用申请）
- 借款申请（员工借款）
- 申请单状态跟踪
- 预算自动校验

### 4. 费用报销模块
- 报销单填报
- 发票信息管理
- 费用分摊明细
- 借款自动核销

### 5. 审批流程系统
- 审批流配置（按金额、部门、费用类型）
- 多级审批（固定流程、条件分支）
- 待办提醒（消息通知）
- 审批记录追溯

### 6. 发票管理系统
- 发票信息录入
- 发票验真（≥1000元自动验真）
- OCR识别（计划中）
- 发票合规性校验

### 7. 财务对接模块
- 凭证生成
- 付款管理
- 财务系统对接
- 数据导出

## 🔧 核心业务规则

### 预算控制规则
- 预算扣减采用乐观锁保证并发安全
- 超标单据无法提交，需先申请预算调整
- 预算执行实时监控与预警

### 审批流程规则
- 根据金额、费用类型、部门自动匹配审批流
- 支持固定流程、条件分支、动态审批人
- 审批超时自动转审或提醒

### 费用合规规则
- 餐补不超过100元/天
- 交通费用需匹配行程时间
- 无发票费用需特殊审批
- 发票金额≥1000元需自动验真

## 📊 数据库设计

系统包含完整的表结构设计，关键表包括：

- **用户组织相关**：`sys_user`, `sys_dept`, `sys_role`, `sys_permission`
- **预算管理相关**：`budget_main`, `budget_detail`, `budget_adjust`, `budget_log`
- **费用申请相关**：`expense_apply_main`, `expense_apply_detail`
- **费用报销相关**：`expense_reimburse_main`, `expense_reimburse_detail`
- **审批流程相关**：`approval_flow_config`, `approval_instance`, `approval_record`

详细表结构请参考[详细设计文档](./企业级费控管理系统详细设计文档.md)

## 🧪 测试与质量

### 测试覆盖
- **后端测试**：单元测试 + 集成测试，使用JUnit + Mockito + TestContainers
- **前端测试**：单元测试 + UI自动化测试，使用Playwright
- **代码质量**：配置了JaCoCo测试覆盖率检查（≥10%）

### 构建脚本
- `build.bat` - Windows环境一键构建
- `build.ps1` - PowerShell构建脚本
- `scripts/` - 自定义构建和部署脚本

## 📝 开发文档

### 已有文档
- [详细设计文档](./企业级费控管理系统详细设计文档.md) - 完整系统设计方案
- [API参考文档](./docs/api-reference.md) - 后端接口说明
- [用户指南](./docs/user-guide.md) - 系统使用说明
- [部署指南](./docs/deployment-guide.md) - 部署配置说明

### 开发指南
- [前端启动指南](./frontend/STARTUP_GUIDE.md) - 前端开发环境配置
- [测试覆盖说明](./ui-tests/TEST_COVERAGE.md) - 测试工程说明

## 🤝 贡献指南

欢迎贡献代码！请遵循以下流程：

1. Fork 项目
2. 创建功能分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 创建 Pull Request

## 📄 开源协议

本项目采用 MIT 协议 - 查看 [LICENSE](./LICENSE) 文件了解详情。

## 🆘 常见问题

### Q: 如何配置审批流？
A: 参考 [审批模块使用指南](./docs/approval-module-usage-guide.md)

### Q: 预算控制如何生效？
A: 系统在费用申请和报销时会自动校验预算余额，不满足条件将无法提交

### Q: 支持哪些发票类型？
A: 支持增值税专用发票、普通发票、电子发票等主流发票类型

### Q: 如何扩展新的费用类型？
A: 在 `fee_type` 表中添加新的费用类型，并配置相应的预算控制和审批规则

## 📞 联系我们

如有问题或建议，请通过以下方式联系：

- 项目Issue：创建GitHub Issue反馈问题
- 开发团队：企业信息化开发团队

---

**CCMS - 让企业费用管理更简单、更高效！**
