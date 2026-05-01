# 企业级费控管理系统部署文档

## 系统概述

CCMS（企业级费控管理系统）是一款基于Spring Boot和Vue.js的企业级费用控制和审批管理系统。系统采用前后端分离架构，支持多环境部署。

### 技术栈
- **后端**: Spring Boot 3.5.11 + Java 21 + MySQL + Redis
- **前端**: Vue 3 + TypeScript + Element Plus + Vite
- **构建工具**: Maven + Vite
- **部署方式**: 单机部署

## 系统要求

### 硬件要求
- **CPU**: 2核心或以上
- **内存**: 4GB或以上
- **存储**: 100GB可用空间（根据文件存储需求）

### 软件要求
- **操作系统**: Linux/Windows/macOS
- **Java**: OpenJDK 25或Oracle JDK 25
- **MySQL**: 8.0或以上版本
- **Redis**: 6.0或以上版本（可选，用于缓存）
- **Node.js**: 18.0或以上版本（仅构建时需要）
- **Git**: 用于代码管理

## 快速开始

### 1. 环境准备

```bash
# 克隆代码库
git clone <repository-url>
cd CCMS

# 检查环境要求
./scripts/build.sh -h
```

### 2. 数据库初始化

```sql
-- 创建数据库
CREATE DATABASE ccms CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 创建用户（可选）
CREATE USER 'ccms_user'@'%' IDENTIFIED BY 'secure_password';
GRANT ALL PRIVILEGES ON ccms.* TO 'ccms_user'@'%';
FLUSH PRIVILEGES;
```

### 3. 一键构建

```bash
# 构建生产环境版本
./scripts/build.sh -e prod

# 构建开发环境版本
./scripts/build.sh -e dev --skip-tests

# 仅构建前端
./scripts/build.sh --frontend-only
```

### 4. 启动应用

```bash
# 进入发布目录
cd target/ccms-latest

# 修改配置文件（如果需要）
vi config/application.yml

# 启动应用
./start.sh
```

## 详细部署指南

### 开发环境部署

#### 环境配置
```bash
# 设置环境变量
export DB_HOST=localhost
export DB_PORT=3306
export DB_NAME=ccms_dev
export DB_USERNAME=root
export DB_PASSWORD=root
```

#### 构建和部署
```bash
# 清理并构建开发环境
./scripts/build.sh -e dev -p dev --clean true

# 启动开发环境
cd target/ccms-latest
java -jar ccms-backend.jar

# 或使用提供的启动脚本
./start.sh
```

### 生产环境部署

#### 环境配置
创建生产环境配置文件 `/etc/ccms/env.conf`：

```bash
# 数据库配置
export DB_HOST=production-mysql-server
export DB_PORT=3306
export DB_NAME=ccms_prod
export DB_USERNAME=ccms_prod_user
export DB_PASSWORD=your_secure_password

# 应用配置
export SERVER_PORT=8080
export JWT_SECRET=your-jwt-secret-key-prod
export JWT_EXPIRATION=3600
export FILE_UPLOAD_PATH=/data/ccms/uploads

# CORS配置
export CORS_ORIGINS=https://your-domain.com
```

#### 构建生产版本
```bash
# 构建生产版本（包含测试）
./scripts/build.sh -e prod -p prod

# 检查构建结果
ls -la target/ccms-latest/
```

#### 部署到服务器
```bash
# 传输部署包到服务器
scp -r target/ccms-latest/ user@server:/opt/ccms/

# 在服务器上启动
ssh user@server
cd /opt/ccms
source /etc/ccms/env.conf
./start.sh
```

#### 使用systemd管理服务
创建服务文件 `/etc/systemd/system/ccms.service`:

```ini
[Unit]
Description=CCMS Enterprise Cost Management System
After=network.target mysql.service redis.service

[Service]
Type=simple
User=ccms
Group=ccms
WorkingDirectory=/opt/ccms
EnvironmentFile=/etc/ccms/env.conf
ExecStart=/opt/ccms/start.sh
ExecStop=/bin/kill -15 $MAINPID
Restart=on-failure
RestartSec=10

[Install]
WantedBy=multi-user.target
```

启动服务：
```bash
sudo systemctl daemon-reload
sudo systemctl enable ccms
sudo systemctl start ccms
sudo systemctl status ccms
```

### Docker部署（可选）

#### 构建Docker镜像
```dockerfile
FROM openjdk:21-jdk-slim

WORKDIR /app
COPY target/ccms-latest/ ./

EXPOSE 8080

CMD ["java", "-jar", "ccms-backend.jar"]
```

构建和运行：
```bash
# 构建镜像
docker build -t ccms:latest .

# 运行容器
docker run -d \
  --name ccms \
  -p 8080:8080 \
  -v /data/ccms/uploads:/app/uploads \
  -v /data/ccms/logs:/app/logs \
  ccms:latest
```

## 环境配置说明

### 前端环境变量

环境变量通过 `.env` 文件管理：

- `.env.development` - 开发环境
- `.env.production` - 生产环境

关键配置项：
```
VITE_API_BASE_URL=http://localhost:8080    # API基础URL
VITE_ENABLE_MOCK=true                       # 是否启用Mock数据
VITE_ENABLE_DEBUG=true                      # 是否启用调试模式
VITE_LOG_LEVEL=debug                        # 日志级别
```

### 后端环境配置

通过 Spring Profile 管理不同环境：

- `application-dev.yml` - 开发环境
- `application-test.yml` - 测试环境  
- `application-prod.yml` - 生产环境

关键配置项：
```yaml
# 数据库配置
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/ccms
    username: root
    password: root

# JWT配置
jwt:
  secret: your-jwt-secret
  expiration: 86400

# 文件上传
file:
  upload-path: ./uploads/
  max-size: 10485760
```

## 数据库初始化

### 自动初始化
系统启动时会自动创建必要的数据库表结构。确保数据库用户具有创建表的权限。

### 手动初始化（可选）
如果需要手动初始化，可以执行以下DDL语句：

```sql
-- 创建数据库
export MYSQL_PWD=your_password
mysql -u root -e "CREATE DATABASE IF NOT EXISTS ccms CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"

-- 导入基础数据（如果存在）
mysql -u root ccms < database/init_data.sql
```

## 监控和维护

### 日志管理
- **日志位置**: `/data/ccms/logs/ccms.log`（生产环境）
- **日志级别**: 可在配置文件中调整
- **日志轮转**: 生产环境自动配置日志轮转

### 健康检查
```bash
# 应用健康检查
curl http://localhost:8080/actuator/health

# 数据库连接检查
curl http://localhost:8080/api/health/db

# 系统信息
curl http://localhost:8080/api/system/info
```

### 备份策略

#### 数据库备份
```bash
# MySQL备份
mysqldump -u root -p ccms > backup/ccms_$(date +%Y%m%d).sql

# 定期备份（crontab）
0 2 * * * /usr/bin/mysqldump -u root -pPASSWORD ccms > /backup/ccms_$(date +\\%Y\\%m\\%d).sql
```

#### 文件备份
```bash
# 上传文件备份
tar -czf backup/uploads_$(date +%Y%m%d).tar.gz /data/ccms/uploads/

# 日志文件备份
find /data/ccms/logs -name "*.log.*" -mtime +30 -exec rm {} \;
```

## 故障排除

### 常见问题

#### 1. 端口冲突
```bash
# 检查端口占用
netstat -tlnp | grep 8080

# 修改端口
vi config/application.yml
# 修改 server.port 配置
```

#### 2. 数据库连接失败
```bash
# 检查数据库服务
systemctl status mysql

# 测试数据库连接
mysql -h localhost -u root -p -e "SHOW DATABASES;"
```

#### 3. 文件权限问题
```bash
# 检查文件权限
ls -la /data/ccms/uploads/

# 修复权限
chown -R ccms:ccms /data/ccms/
chmod -R 755 /data/ccms/uploads/
```

#### 4. 内存不足
```bash
# 检查内存使用
free -h

# 增加JVM内存
java -Xmx1g -Xms512m -jar ccms-backend.jar
```

### 日志分析
```bash
# 查看实时日志
tail -f /data/ccms/logs/ccms.log

# 搜索错误日志
grep -i "error" /data/ccms/logs/ccms.log

# 按时间查看日志
journalctl -u ccms --since "2025-01-01" --until "2025-01-02"
```

## 安全配置

### 基础安全
- 使用强密码策略
- 定期更新JWT密钥
- 配置防火墙规则
- 使用HTTPS协议

### 数据库安全
```sql
-- 创建专用数据库用户
CREATE USER 'ccms_app'@'localhost' IDENTIFIED BY 'strong_password';
GRANT SELECT, INSERT, UPDATE, DELETE ON ccms.* TO 'ccms_app'@'localhost';
FLUSH PRIVILEGES;
```

### 应用安全
- 定期更新依赖包
- 启用SQL注入防护
- 配置CORS策略
- 实现API访问限流

## 性能优化

### JVM优化
```bash
# 生产环境JVM参数
java -server -Xmx2g -Xms1g -XX:+UseG1GC -jar ccms-backend.jar
```

### 数据库优化
```sql
-- 创建索引
CREATE INDEX idx_user_dept ON sys_user(dept_id);
CREATE INDEX idx_expense_status ON expense_apply_main(status);

-- 定期优化表
OPTIMIZE TABLE expense_apply_main;
```

### 缓存优化
- 启用Redis缓存
- 配置合理的缓存过期时间
- 使用多级缓存策略

## 升级指南

### 版本升级步骤
1. 备份当前版本数据和配置文件
2. 停止当前服务
3. 部署新版本
4. 执行数据库迁移（如有）
5. 启动新版本服务
6. 验证功能正常

### 回滚流程
1. 停止当前服务
2. 恢复备份数据
3. 启动旧版本服务
4. 验证功能正常

## 支持与联系

### 文档资源
- [用户手册](./docs/user-guide.md)
- [API文档](./docs/api-documentation.md)
- [技术架构](./docs/architecture.md)

### 技术支持
- 问题反馈：创建GitHub Issue
- 技术支持邮箱：tech-support@example.com
- 紧急联系方式：+86-XXX-XXXX-XXXX

### 版本信息
- 当前版本：v1.0.0
- 发布日期：2025-01-01
- 更新日志：[CHANGELOG.md](./CHANGELOG.md)

---

**注意**: 本文档会根据系统版本更新，请确保使用最新版本的文档。如有问题，请及时联系技术支持团队。