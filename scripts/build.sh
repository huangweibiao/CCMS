#!/bin/bash

# CCMS企业级费控管理系统完整构建脚本
# 功能：清除历史打包文件 → 构建前端 → 复制前端到后端 → 构建后端包含前端

# 设置脚本执行参数
set -euo pipefail

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # 无颜色

# 全局变量
PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")"/.. && pwd)"
BACKEND_DIR="$PROJECT_ROOT/backend"
FRONTEND_DIR="$PROJECT_ROOT/frontend"
BUILD_DIR="$PROJECT_ROOT/build"

# 日志函数
log_info() {
    echo -e "${BLUE}[INFO]${NC} $(date '+%Y-%m-%d %H:%M:%S') $1"
}

log_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $(date '+%Y-%m-%d %H:%M:%S') $1"
}

log_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $(date '+%Y-%m-%d %H:%M:%S') $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $(date '+%Y-%m-%d %H:%M:%S') $1"
}

# 检查依赖
dependency_check() {
    log_info "检查构建依赖..."
    
    # 检查Node.js
    if ! command -v node &> /dev/null; then
        log_error "Node.js 未安装，请先安装 Node.js 18+"
        exit 1
    fi
    
    # 检查npm
    if ! command -v npm &> /dev/null; then
        log_error "npm 未安装，请先安装 npm"
        exit 1
    fi
    
    # 检查Maven
    if ! command -v mvn &> /dev/null; then
        log_error "Maven 未安装，请先安装 Maven 3.6+"
        exit 1
    fi
    
    # 检查Java
    if ! command -v java &> /dev/null; then
        log_error "Java 未安装，请先安装 Java 21"
        exit 1
    fi
    
    log_success "依赖检查通过"
}

# 清理历史打包文件
clean_build() {
    log_info "清理历史打包文件..."
    
    # 清理前端构建文件
    if [ -d "$FRONTEND_DIR/dist" ]; then
        rm -rf "$FRONTEND_DIR/dist"
        log_info "清理前端dist目录"
    fi
    
    # 清理后端构建文件
    if [ -d "$BACKEND_DIR/target" ]; then
        rm -rf "$BACKEND_DIR/target"
        log_info "清理后端target目录"
    fi
    
    # 清理构建输出目录
    if [ -d "$BUILD_DIR" ]; then
        rm -rf "$BUILD_DIR"
        log_info "清理build目录"
    fi
    
    mkdir -p "$BUILD_DIR"
    
    log_success "清理完成"
}

# 构建前端
build_frontend() {
    log_info "开始构建前端项目..."
    
    cd "$FRONTEND_DIR"
    
    # 检查前端依赖
    if [ ! -d "node_modules" ]; then
        log_info "安装前端依赖..."
        npm install
    fi
    
    # 构建前端
    log_info "执行前端构建..."
    npm run build
    
    if [ $? -ne 0 ]; then
        log_error "前端构建失败"
        exit 1
    fi
    
    log_success "前端构建完成"
    
    cd "$PROJECT_ROOT"
}

# 复制前端构建文件到后端
copy_frontend_to_backend() {
    log_info "复制前端构建文件到后端..."
    
    # 创建后端静态资源目录
    mkdir -p "$BACKEND_DIR/src/main/resources/static"
    
    # 复制前端dist目录内容到后端static目录
    if [ -d "$FRONTEND_DIR/dist" ]; then
        cp -r "$FRONTEND_DIR/dist/"* "$BACKEND_DIR/src/main/resources/static/"
        log_info "前端文件复制完成"
    else
        log_error "前端dist目录不存在，请先构建前端"
        exit 1
    fi
    
    log_success "前端文件复制完成"
}

# 构建后端
build_backend() {
    log_info "开始构建后端项目..."
    
    cd "$BACKEND_DIR"
    
    # 执行Maven构建
    log_info "执行Maven构建..."
    mvn clean package -DskipTests
    
    if [ $? -ne 0 ]; then
        log_error "后端构建失败"
        exit 1
    fi
    
    # 复制最终JAR包到构建目录
    if [ -f "target/ccms-backend-1.0.0.jar" ]; then
        cp "target/ccms-backend-1.0.0.jar" "$BUILD_DIR/ccms-app.jar"
        log_info "应用JAR包已复制到构建目录"
    fi
    
    # 复制配置文件
    if [ -d "src/main/resources" ]; then
        cp -r "src/main/resources/"*.yml "$BUILD_DIR/" 2>/dev/null || true
        log_info "配置文件已复制到构建目录"
    fi
    
    log_success "后端构建完成"
    
    cd "$PROJECT_ROOT"
}

# 生成启动脚本
generate_startup_scripts() {
    log_info "生成启动脚本..."
    
    # 生成Linux启动脚本
    cat > "$BUILD_DIR/start.sh" << 'EOF'
#!/bin/bash
# CCMS应用启动脚本

# 设置JVM参数
JAVA_OPTS="-Xms512m -Xmx1024m -Dfile.encoding=UTF-8 -Duser.timezone=Asia/Shanghai"

# 检查Java环境
if ! command -v java &> /dev/null; then
    echo "错误：Java运行环境未安装"
    exit 1
fi

# 检查JAR文件是否存在
if [ ! -f "ccms-app.jar" ]; then
    echo "错误：应用JAR文件不存在"
    exit 1
fi

# 启动应用
echo "启动CCMS企业级费控管理系统..."
java $JAVA_OPTS -jar ccms-app.jar --spring.profiles.active=prod
EOF
    
    # 生成Windows启动脚本
    cat > "$BUILD_DIR/start.bat" << 'EOF'
@echo off
chcp 65001 >nul
echo 启动CCMS企业级费控管理系统...

REM 设置JVM参数
set JAVA_OPTS=-Xms512m -Xmx1024m -Dfile.encoding=UTF-8 -Duser.timezone=Asia/Shanghai

REM 检查Java环境
java -version >nul 2>&1
if errorlevel 1 (
    echo 错误：Java运行环境未安装
    pause
    exit /b 1
)

REM 检查JAR文件是否存在
if not exist "ccms-app.jar" (
    echo 错误：应用JAR文件不存在
    pause
    exit /b 1
)

REM 启动应用
java %JAVA_OPTS% -jar ccms-app.jar --spring.profiles.active=prod
EOF
    
    # 设置执行权限
    chmod +x "$BUILD_DIR/start.sh"
    
    log_success "启动脚本生成完成"
}

# 生成构建报告
generate_build_report() {
    local build_time=$(date '+%Y-%m-%d %H:%M:%S')
    local jar_size=$(stat -f%z "$BUILD_DIR/ccms-app.jar" 2>/dev/null || stat -c%s "$BUILD_DIR/ccms-app.jar" 2>/dev/null || echo "未知")
    
    cat > "$BUILD_DIR/build-info.txt" << EOF
=== CCMS企业级费控管理系统构建报告 ===
构建时间: $build_time
应用版本: 1.0.0
JAR包大小: $jar_size 字节
前端技术: Vue 3 + TypeScript + Element Plus
后端技术: Spring Boot 3.5.11 + Java 21 + MySQL 8

启动说明:
1. 确保已安装Java 21运行环境
2. 确保MySQL 8数据库服务已启动
3. Linux/macOS系统执行: ./start.sh
4. Windows系统执行: start.bat
5. 默认访问地址: http://localhost:8080

数据库配置:
- 开发环境: 使用application-dev.yml配置
- 生产环境: 使用环境变量或application-prod.yml

EOF
    
    log_success "构建报告生成完成"
}

# 主执行函数
main() {
    log_info "开始CCMS企业级费控管理系统构建流程..."
    
    # 执行构建步骤
    dependency_check
    clean_build
    build_frontend
    copy_frontend_to_backend
    build_backend
    generate_startup_scripts
    generate_build_report
    
    log_success "CCMS企业级费控管理系统构建完成！"
    log_info "构建产物位置: $BUILD_DIR"
    log_info "启动命令: cd $BUILD_DIR && ./start.sh (Linux/macOS) 或 start.bat (Windows)"
}

# 脚本入口
main "$@"