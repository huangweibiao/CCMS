# CCMS 打包脚本使用说明

## 脚本列表

| 脚本 | 用途 | 使用场景 |
|------|------|---------|
| `build.ps1` / `build.bat` | 完整打包脚本 | 生产环境部署 |
| `dev-start.ps1` / `dev-start.bat` | 开发环境启动 | 本地开发调试 |

---

## 完整打包脚本 (build.bat / build.ps1)

### 功能
1. 前端打包（npm run build）
2. 将前端资源复制到后端 static 目录
3. 后端打包（Maven package）
4. 生成完整部署包（包含启动脚本和配置文件）

### 使用方法

#### 方式一：双击运行（推荐）
```bash
双击 build.bat
```

#### 方式二：命令行带参数
```powershell
# 基本打包
.\build.ps1

# 指定版本号
.\build.ps1 -Version "1.1.0"

# 指定输出目录
.\build.ps1 -OutputDir "D:\deploy"

# 只打包后端（前端已打包）
.\build.ps1 -SkipFrontend

# 只打包前端
.\build.ps1 -SkipBackend

# 组合参数
.\build.ps1 -Version "1.1.0" -OutputDir "D:\deploy" -SkipTests
```

### 参数说明

| 参数 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `-Version` | string | "1.0.0" | 版本号 |
| `-OutputDir` | string | "./dist" | 输出目录 |
| `-SkipFrontend` | switch | false | 跳过前端打包 |
| `-SkipBackend` | switch | false | 跳过后端打包 |
| `-SkipTests` | switch | true | 跳过测试 |

### 输出结构

```
dist/
└── ccms-1.0.0/
    ├── ccms-backend-1.0.0.jar    # 可执行JAR
    ├── start.bat                  # Windows启动脚本
    ├── start.sh                   # Linux启动脚本
    ├── config/
    │   └── application.yml        # 配置文件
    └── README.md                  # 部署说明

ccms-1.0.0.zip                     # 部署包压缩文件
```

### 部署方法

#### Windows
1. 解压 `ccms-1.0.0.zip`
2. 修改 `config/application.yml` 配置数据库
3. 双击 `start.bat`

#### Linux/Mac
1. 解压 `ccms-1.0.0.zip`
2. 修改 `config/application.yml` 配置数据库
3. 执行：
   ```bash
   chmod +x start.sh
   ./start.sh
   ```

---

## 开发环境启动脚本 (dev-start.bat / dev-start.ps1)

### 功能
同时启动后端和前端开发服务器，方便本地开发调试。

### 使用方法

#### 方式一：双击运行
```bash
双击 dev-start.bat
```

#### 方式二：命令行带参数
```powershell
# 启动前后端
.\dev-start.ps1

# 只启动后端
.\dev-start.ps1 -SkipFrontend

# 只启动前端
.\dev-start.ps1 -SkipBackend

# 先编译再启动后端
.\dev-start.ps1 -BuildFirst
```

### 批处理脚本参数

```bash
# 只启动后端
dev-start.bat --skip-frontend

# 只启动前端
dev-start.bat --skip-backend

# 先编译再启动
dev-start.bat --build
```

### 访问地址

- 前端页面：http://localhost:5173
- 后端API：http://localhost:8080
- API文档：http://localhost:8080/swagger-ui.html

---

## 系统要求

### 打包环境
- Node.js 18+
- npm 8+
- Maven 3.8+
- Java 21+
- PowerShell 5.1+ 或 PowerShell Core

### 运行环境
- Java 21+
- MySQL 8.0+
- Redis 6.0+ (可选)

---

## 常见问题

### Q1: 提示找不到 PowerShell
**解决**：安装 PowerShell 或添加到系统 PATH

### Q2: 前端打包失败
**解决**：
```bash
cd frontend
npm install
npm run build
```

### Q3: 后端打包失败
**解决**：
```bash
cd backend
mvn clean compile
```

### Q4: 如何修改数据库配置
**解决**：编辑 `backend/src/main/resources/application.yml` 或在部署包中编辑 `config/application.yml`

### Q5: 如何修改端口
**解决**：在 `application.yml` 中修改：
```yaml
server:
  port: 8080  # 修改为你需要的端口
```

---

## 手动打包流程

如果不想使用脚本，可以手动执行：

### 1. 前端打包
```bash
cd frontend
npm install
npm run build
```

### 2. 复制到后端
```bash
# 删除旧资源
rmdir /s /q backend\src\main\resources\static

# 复制新资源
xcopy /s /e /i frontend\dist backend\src\main\resources\static
```

### 3. 后端打包
```bash
cd backend
mvn clean package -DskipTests
```

### 4. 运行
```bash
java -jar backend\target\ccms-backend-1.0.0-exec.jar
```

---

## 技术支持

如有问题，请查看项目文档或联系开发团队。
