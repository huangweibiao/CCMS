# CCMS 系统部署文档

## 系统架构概述

CCMS采用前后端分离架构：
- **前端**：Vue3 + TypeScript + Element Plus
- **后端**：Spring Boot + MySQL + Redis
- **部署**：Docker容器化部署

## 环境要求

### 服务器配置
| 组件 | 最低配置 | 推荐配置 |
|------|----------|----------|
| CPU | 2核 | 4核 |
| 内存 | 4GB | 8GB |
| 存储 | 50GB | 100GB |
| 系统 | CentOS 7+/Ubuntu 18+ | CentOS 8+/Ubuntu 20+ |

### 软件依赖
- **Java**: JDK 11+
- **Node.js**: 16.17.0+
- **MySQL**: 8.0+
- **Redis**: 6.0+
- **Nginx**: 1.18+
- **Docker**: 20.10+

## 数据库部署

### MySQL配置
```sql
-- 创建数据库和用户
CREATE DATABASE ccms CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'ccms_user'@'%' IDENTIFIED BY 'your_password';
GRANT ALL PRIVILEGES ON ccms.* TO 'ccms_user'@'%';
FLUSH PRIVILEGES;

-- 重要配置项
[mysqld]
character-set-server=utf8mb4
collation-server=utf8mb4_unicode_ci
max_connections=1000
innodb_buffer_pool_size=1G
```

### Redis配置
```conf
# redis.conf
bind 0.0.0.0
port 6379
timeout 300
databases 16
requirepass your_redis_password
maxmemory 512mb
maxmemory-policy allkeys-lru
```

## 后端服务部署

### 环境变量配置
创建 `application-prod.yml`:
```yaml
server:
  port: 8080
  servlet:
    context-path: /api

spring:
  datasource:
    url: jdbc:mysql://mysql-host:3306/ccms?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai
    username: ccms_user
    password: ${DB_PASSWORD}
    druid:
      initial-size: 5
      min-idle: 5
      max-active: 20
  
  redis:
    host: redis-host
    port: 6379
    password: ${REDIS_PASSWORD}
    database: 0
    timeout: 3000ms
    lettuce:
      pool:
        max-active: 20
        max-wait: -1ms
        max-idle: 10
        min-idle: 0

# JWT配置
jwt:
  secret: ${JWT_SECRET}
  expiration: 86400
  header: Authorization

# 文件上传配置
file:
  upload:
    path: /data/ccms/upload
    max-size: 10MB

# 日志配置
logging:
  level:
    com.ccms: DEBUG
  file:
    path: /data/ccms/logs
    name: ccms.log
```

### Docker部署方式
```dockerfile
# Dockerfile
FROM openjdk:11-jre-slim

VOLUME /tmp
ADD target/ccms-backend.jar app.jar

ENV JAVA_OPTS="-Xmx1024m -Xms512m"
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -jar /app.jar"]

EXPOSE 8080
```

Docker Compose配置：
```yaml
version: '3.8'
services:
  mysql:
    image: mysql:8.0
    container_name: ccms-mysql
    environment:
      MYSQL_ROOT_PASSWORD: ${MYSQL_ROOT_PASSWORD}
      MYSQL_DATABASE: ccms
      MYSQL_USER: ccms_user
      MYSQL_PASSWORD: ${MYSQL_PASSWORD}
    volumes:
      - mysql_data:/var/lib/mysql
      - ./init.sql:/docker-entrypoint-initdb.d/init.sql
    ports:
      - "3306:3306"
    networks:
      - ccms-network

  redis:
    image: redis:6.2-alpine
    container_name: ccms-redis
    command: redis-server --requirepass ${REDIS_PASSWORD}
    volumes:
      - redis_data:/data
    ports:
      - "6379:6379"
    networks:
      - ccms-network

  backend:
    image: ccms-backend:latest
    container_name: ccms-backend
    environment:
      - SPRING_PROFILES_ACTIVE=prod
      - DB_PASSWORD=${MYSQL_PASSWORD}
      - REDIS_PASSWORD=${REDIS_PASSWORD}
      - JWT_SECRET=${JWT_SECRET}
    volumes:
      - upload_data:/data/ccms/upload
      - log_data:/data/ccms/logs
    ports:
      - "8080:8080"
    depends_on:
      - mysql
      - redis
    networks:
      - ccms-network

volumes:
  mysql_data:
  redis_data:
  upload_data:
  log_data:

networks:
  ccms-network:
    driver: bridge
```

### 传统部署方式
```bash
# 编译打包
mvn clean package -DskipTests

# 创建部署目录
mkdir -p /opt/ccms/{bin,config,logs,upload}

# 复制文件
cp target/ccms-backend.jar /opt/ccms/bin/
cp src/main/resources/application-prod.yml /opt/ccms/config/

# 创建启动脚本
cat > /opt/ccms/bin/start.sh << 'EOF'
#!/bin/bash
JAVA_OPTS="-Xmx1024m -Xms512m -Dspring.profiles.active=prod"
nohup java $JAVA_OPTS -jar ccms-backend.jar > ../logs/console.log 2>&1 &
echo $! > pid.txt
EOF

chmod +x /opt/ccms/bin/start.sh
```

## 前端部署

### 构建生产版本
```bash
# 安装依赖
npm install

# 构建生产版本
npm run build

# 输出文件在 dist/ 目录
```

### Nginx配置
```nginx
server {
    listen 80;
    server_name ccms.yourdomain.com;
    
    # 前端静态文件
    location / {
        root /usr/share/nginx/html/ccms;
        index index.html;
        try_files $uri $uri/ /index.html;
    }
    
    # API代理
    location /api/ {
        proxy_pass http://backend:8080/api/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
    
    # 文件上传代理
    location /upload/ {
        proxy_pass http://backend:8080/upload/;
        proxy_set_header Host $host;
        client_max_body_size 10M;
    }
    
    # 静态资源缓存
    location ~* \.(js|css|png|jpg|jpeg|gif|ico|svg)$ {
        expires 1y;
        add_header Cache-Control "public, immutable";
    }
}
```

### Docker前端部署
```dockerfile
# 前端Dockerfile
FROM nginx:alpine

COPY dist/ /usr/share/nginx/html/ccms/
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80
```

完整Docker Compose：
```yaml
version: '3.8'
services:
  # ... 之前的mysql, redis, backend服务
  
  frontend:
    image: ccms-frontend:latest
    container_name: ccms-frontend
    ports:
      - "80:80"
    depends_on:
      - backend
    networks:
      - ccms-network
```

## 初始化配置

### 数据库初始化
创建初始化SQL脚本 `init.sql`:
```sql
-- 创建基础数据表
-- 系统会自动创建表结构，这里只需插入基础数据

-- 插入默认管理员用户
INSERT INTO users (username, password, real_name, email, status, create_time) 
VALUES ('admin', '$2a$10$your_hashed_password', '系统管理员', 'admin@company.com', 1, NOW());

-- 插入默认角色
INSERT INTO roles (role_name, role_key, status, create_time) 
VALUES ('系统管理员', 'admin', 1, NOW());

-- 关联用户角色
INSERT INTO user_roles (user_id, role_id) VALUES (1, 1);

-- 插入默认费用类型
INSERT INTO fee_types (name, description, require_invoice, require_budget_control, status) 
VALUES 
('差旅费', '出差相关费用', 1, 1, 1),
('接待费', '客户接待费用', 1, 1, 1),
('办公费', '办公用品采购', 1, 1, 1);
```

### 系统配置检查
部署完成后检查：
```bash
# 检查服务状态
curl http://localhost:8080/api/health

# 检查数据库连接
curl http://localhost:8080/api/db/status

# 检查Redis连接
curl http://localhost:8080/api/redis/status
```

## 安全配置

### SSL证书配置
```nginx
server {
    listen 443 ssl http2;
    server_name ccms.yourdomain.com;
    
    ssl_certificate /path/to/ssl/cert.pem;
    ssl_certificate_key /path/to/ssl/key.pem;
    ssl_protocols TLSv1.2 TLSv1.3;
    ssl_ciphers HIGH:!aNULL:!MD5;
    
    # ... 其他配置
}

# HTTP重定向到HTTPS
server {
    listen 80;
    server_name ccms.yourdomain.com;
    return 301 https://$server_name$request_uri;
}
```

### 防火墙配置
```bash
# 开放必要端口
ufw allow 80/tcp
ufw allow 443/tcp
ufw allow 22/tcp

# 限制数据库访问
ufw allow from 172.17.0.0/16 to any port 3306
ufw allow from 172.17.0.0/16 to any port 6379
```

## 监控与维护

### 日志管理
```bash
# 查看应用日志
tail -f /data/ccms/logs/ccms.log

# 日志轮转配置
cat > /etc/logrotate.d/ccms << 'EOF'
/data/ccms/logs/*.log {
    daily
    rotate 30
    compress
    missingok
    notifempty
    copytruncate
}
EOF
```

### 健康检查脚本
```bash
#!/bin/bash
# health_check.sh

API_URL="http://localhost:8080/api/health"

response=$(curl -s -o /dev/null -w "%{http_code}" $API_URL)

if [ $response -eq 200 ]; then
    echo "Service is healthy"
    exit 0
else
    echo "Service is down"
    # 自动重启服务
    cd /opt/ccms/bin && ./restart.sh
    exit 1
fi
```

### 备份策略
```bash
#!/bin/bash
# backup.sh

# 数据库备份
mysqldump -u ccms_user -p$DB_PASSWORD ccms > /backup/ccms_$(date +%Y%m%d).sql

# 文件备份
tar -czf /backup/upload_$(date +%Y%m%d).tar.gz /data/ccms/upload

# 清理旧备份
find /backup -name "*.sql" -mtime +30 -delete
find /backup -name "*.tar.gz" -mtime +30 -delete
```

## 故障排除

### 常见问题

**问题1：数据库连接失败**
```bash
# 检查MySQL服务状态
systemctl status mysql

# 检查连接参数
mysql -u ccms_user -p -h localhost ccms
```

**问题2：Redis连接失败**
```bash
# 检查Redis服务
redis-cli -a your_password ping

# 检查内存使用
redis-cli -a your_password info memory
```

**问题3：前端静态资源404**
- 检查Nginx配置中的root路径
- 确认dist文件已正确复制

**问题4：文件上传失败**
- 检查upload目录权限
- 检查Nginx client_max_body_size配置

### 性能优化建议

1. **数据库优化**
   - 添加适当索引
   - 定期清理历史数据
   - 监控慢查询

2. **缓存优化**
   - 合理设置缓存过期时间
   - 监控Redis内存使用

3. **前端优化**
   - 启用Gzip压缩
   - 配置CDN加速
   - 优化图片资源

## 升级指南

### 版本升级步骤
1. 备份数据库和配置文件
2. 停止当前服务
3. 部署新版本
4. 执行数据库迁移脚本
5. 启动新服务
6. 验证功能正常

### 回滚方案
1. 恢复备份的数据库
2. 回退到旧版本代码
3. 重启服务

---

*本文档随系统版本更新，请确保使用对应版本的部署说明。*