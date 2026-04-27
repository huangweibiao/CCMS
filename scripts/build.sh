#!/bin/bash

# CCMS系统完整构建脚本
# 支持开发、测试、生产环境构建

set -euo pipefail

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

# 项目路径
PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
BACKEND_DIR="$PROJECT_ROOT/backend"
FRONTEND_DIR="$PROJECT_ROOT/frontend"
TARGET_DIR="$PROJECT_ROOT/target"

# 默认配置
ENV="production"
PROFILE="prod"
CLEAN="true"
SKIP_TESTS="false"
BUILD_FRONTEND="true"
BUILD_BACKEND="true"

# 日志函数
log_error() {
    echo -e "${RED}[ERROR]${NC} $(date '+%Y-%m-%d %H:%M:%S') $1" >&2
}

log_warn() {
    echo -e "${YELLOW}[WARN] ${NC} $(date '+%Y-%m-%d %H:%M:%S') $1"
}

log_info() {
    echo -e "${BLUE}[INFO] ${NC} $(date '+%Y-%m-%d %H:%M:%S') $1"
}

log_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $(date '+%Y-%m-%d %H:%M:%S') $1"
}

# 显示使用说明
show_usage() {
    cat << EOF
CCMS构建脚本 - 企业级费控管理系统完整构建工具

用法: $0 [选项]

选项:
    -e, --env <环境>        构建环境 (dev|test|prod, 默认: prod)
    -p, --profile <profile>  Maven profile (dev|test|prod, 默认: prod)
    -c, --clean <true|false> 是否清理构建缓存 (默认: true)
    -t, --skip-tests         跳过测试 (默认: false)
    --frontend-only         仅构建前端
    --backend-only          仅构建后端
    -h, --help              显示此帮助信息

示例:
    $0                         # 构建生产环境完整版本
    $0 -e dev                  # 构建开发环境
    $0 --frontend-only         # 仅构建前端
    $0 -t -e test              # 跳过测试构建测试环境

EOF
}

# 解析命令行参数
parse_args() {
    while [[ $# -gt 0 ]]; do
        case $1 in
            -e|--env)
                ENV="$2"
                shift 2
                ;;
            -p|--profile)
                PROFILE="$2"
                shift 2
                ;;
            -c|--clean)
                CLEAN="$2"
                shift 2
                ;;
            -t|--skip-tests)
                SKIP_TESTS="true"
                shift
                ;;
            --frontend-only)
                BUILD_BACKEND="false"
                shift
                ;;
            --backend-only)
                BUILD_FRONTEND="false"
                shift
                ;;
            -h|--help)
                show_usage
                exit 0
                ;;
            *)
                log_error "未知参数: $1"
                show_usage
                exit 1
                ;;
        esac
    done
}

# 检查环境要求
check_requirements() {
    log_info "检查构建环境要求..."
    
    # 检查Node.js
    if ! command -v node &> /dev/null; then
        log_error "Node.js未安装"
        exit 1
    fi
    
    # 检查npm
    if ! command -v npm &> /dev/null; then
        log_error "npm未安装"
        exit 1
    fi
    
    # 检查Maven
    if ! command -v mvn &> /dev/null; then
        log_error "Maven未安装"
        exit 1
    fi
    
    # 检查Java
    if ! command -v java &> /dev/null; then
        log_error "Java未安装"
        exit 1
    fi
    
    log_success "环境要求检查通过"
}

# 清理构建缓存
clean_build() {
    if [[ "$CLEAN" == "true" ]]; then
        log_info "清理构建缓存..."
        
        # 清理前端缓存
        if [[ -d "$FRONTEND_DIR/node_modules" ]]; then
            cd "$FRONTEND_DIR"
            rm -rf node_modules package-lock.json dist
            log_success "前端缓存清理完成"
        fi
        
        # 清理后端缓存
        if [[ -d "$BACKEND_DIR/target" ]]; then
            cd "$BACKEND_DIR"
            mvn clean
            log_success "后端缓存清理完成"
        fi
        
        # 清理整个target目录
        if [[ -d "$TARGET_DIR" ]]; then
            rm -rf "$TARGET_DIR"
            log_success "目标目录清理完成"
        fi
        
        mkdir -p "$TARGET_DIR"
    fi
}

# 构建前端
build_frontend() {
    if [[ "$BUILD_FRONTEND" == "true" ]]; then
        log_info "开始构建前端项目..."
        
        cd "$FRONTEND_DIR"
        
        # 安装依赖
        if [[ ! -d "node_modules" ]] || [[ "$CLEAN" == "true" ]]; then
            log_info "安装前端依赖..."
            npm ci --silent
        fi
        
        # 构建前端
        log_info "执行前端构建命令 (环境: $ENV)..."
        npm run build:$ENV
        
        # 验证构建结果
        if [[ ! -d "dist" ]]; then
            log_error "前端构建失败，dist目录不存在"
            exit 1
        fi
        
        log_success "前端构建完成"
        log_info "构建产物位置: $FRONTEND_DIR/dist"
    fi
}

# 构建后端
build_backend() {
    if [[ "$BUILD_BACKEND" == "true" ]]; then
        log_info "开始构建后端项目..."
        
        cd "$BACKEND_DIR"
        
        # 构建命令
        local mvn_cmd="mvn"
        
        if [[ "$SKIP_TESTS" == "true" ]]; then
            mvn_cmd="$mvn_cmd -DskipTests"
        fi
        
        if [[ "$ENV" != "production" ]]; then
            mvn_cmd="$mvn_cmd -P$PROFILE"
        fi
        
        mvn_cmd="$mvn_cmd package"
        
        log_info "执行后端构建命令: $mvn_cmd"
        eval "$mvn_cmd"
        
        # 验证构建结果
        local jar_file="$(find target -name '*.jar' ! -name '*sources.jar' ! -name '*tests.jar' | head -1)"
        if [[ -z "$jar_file" ]]; then
            log_error "后端构建失败，未找到JAR文件"
            exit 1
        fi
        
        log_success "后端构建完成"
        log_info "构建产物位置: $BACKEND_DIR/$jar_file"
    fi
}

# 复制前端文件到后端
copy_frontend_to_backend() {
    if [[ "$BUILD_FRONTEND" == "true" && "$BUILD_BACKEND" == "true" ]]; then
        log_info "复制前端文件到后端静态资源目录..."
        
        local backend_static="$BACKEND_DIR/src/main/resources/static"
        
        # 创建静态资源目录
        mkdir -p "$backend_static"
        
        # 复制前端构建文件
        cp -r "$FRONTEND_DIR/dist/"* "$backend_static/"
        
        log_success "前端文件复制完成"
    fi
}

# 打包最终产物
package_artifacts() {
    log_info "打包最终构建产物..."
    
    # 创建发布目录
    local release_dir="$TARGET_DIR/ccms-$ENV-$(date +%Y%m%d-%H%M%S)"
    mkdir -p "$release_dir"
    
    # 复制后端JAR文件
    if [[ "$BUILD_BACKEND" == "true" ]]; then
        local jar_file="$(find "$BACKEND_DIR/target" -name '*.jar' ! -name '*sources.jar' ! -name '*tests.jar' | head -1)"
        if [[ -n "$jar_file" ]]; then
            cp "$jar_file" "$release_dir/ccms-backend.jar"
            log_success "后端JAR文件已复制"
        fi
    fi
    
    # 复制前端文件
    if [[ "$BUILD_FRONTEND" == "true" ]]; then
        mkdir -p "$release_dir/frontend"
        cp -r "$FRONTEND_DIR/dist/"* "$release_dir/frontend/"
        log_success "前端文件已复制"
    fi
    
    # 复制配置文件
    mkdir -p "$release_dir/config"
    cp "$BACKEND_DIR/src/main/resources/application-$ENV.yml" "$release_dir/config/" 2>/dev/null || true
    cp "$BACKEND_DIR/src/main/resources/application.yml" "$release_dir/config/" 2>/dev/null || true
    
    # 创建启动脚本
    cat > "$release_dir/start.sh" << 'EOF'
#!/bin/bash
# CCMS启动脚本

set -euo pipefail

APP_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
JAR_FILE="$APP_DIR/ccms-backend.jar"

if [[ ! -f "$JAR_FILE" ]]; then
    echo "错误: 未找到后端JAR文件: $JAR_FILE"
    exit 1
fi

# 默认JVM参数
JVM_OPTS="-Xmx512m -Xms256m -Djava.security.egd=file:/dev/./urandom"

# 环境特定配置
if [[ -f "$APP_DIR/config/application-prod.yml" ]]; then
    ENV="prod"
elif [[ -f "$APP_DIR/config/application-test.yml" ]]; then
    ENV="test"
else
    ENV="dev"
fi

echo "启动CCMS系统 (环境: $ENV)..."
java $JVM_OPTS -jar "$JAR_FILE" --spring.profiles.active=$ENV
EOF
    
    chmod +x "$release_dir/start.sh"
    
    # 创建部署说明
    cat > "$release_dir/DEPLOYMENT.md" << EOF
# CCMS部署说明

## 系统信息
- 版本: $ENV-$(date +%Y%m%d-%H%M%S)
- 构建时间: $(date)
- 包含组件: $(if [[ "$BUILD_FRONTEND" == "true" ]]; then echo "前端 "; fi)$(if [[ "$BUILD_BACKEND" == "true" ]]; then echo "后端"; fi)

## 部署步骤

### 1. 准备工作
- 确保已安装Java 21+
- 确保数据库已配置
- 检查端口占用情况

### 2. 修改配置
编辑 config/application.yml 文件，配置数据库连接等信息

### 3. 启动应用
\`\`\`bash
cd $release_dir
./start.sh
\`\`\`

### 4. 验证部署
- 访问 http://localhost:8080 查看应用
- 检查应用日志确认启动成功

## 文件结构
\`\`\`
$(basename "$release_dir")/
├── ccms-backend.jar      # 后端JAR文件
├── frontend/             # 前端静态文件
├── config/               # 配置文件
├── start.sh              # 启动脚本
└── DEPLOYMENT.md         # 部署说明
\`\`\`

## 技术支持
- 问题反馈: 请联系系统管理员
- 日志文件: 查看应用启动日志
EOF
    
    log_success "构建产物打包完成"
    log_info "发布包位置: $release_dir"
    
    # 创建快捷链接
    ln -sfn "$release_dir" "$TARGET_DIR/ccms-latest"
    log_info "最新版本快捷链接: $TARGET_DIR/ccms-latest"
}

# 显示构建摘要
show_build_summary() {
    echo
    echo "==================== 构建摘要 ===================="
    echo "构建环境: $ENV"
    echo "构建时间: $(date)"
    echo "构建结果: ${GREEN}成功${NC}"
    echo "包含组件: $(if [[ "$BUILD_FRONTEND" == "true" ]]; then echo "前端 "; fi)$(if [[ "$BUILD_BACKEND" == "true" ]]; then echo "后端"; fi)"
    echo "测试状态: $(if [[ "$SKIP_TESTS" == "true" ]]; then echo "${YELLOW}跳过${NC}"; else echo "${GREEN}执行${NC}"; fi)"
    echo "清理缓存: $CLEAN"
    echo "发布包位置: $TARGET_DIR/ccms-latest"
    echo "================================================="
    echo
}

# 主函数
main() {
    log_info "开始CCMS系统构建..."
    
    # 解析参数
    parse_args "$@"
    
    # 显示构建配置
    log_info "构建配置 - 环境: $ENV, Profile: $PROFILE, 清理: $CLEAN"
    
    # 检查环境要求
    check_requirements
    
    # 清理构建缓存
    clean_build
    
    # 构建前端
    build_frontend
    
    # 构建后端
    build_backend
    
    # 复制前端文件到后端
    copy_frontend_to_backend
    
    # 打包最终产物
    package_artifacts
    
    # 显示构建摘要
    show_build_summary
    
    log_success "CCMS系统构建完成！"
}

# 执行主函数
main "$@"