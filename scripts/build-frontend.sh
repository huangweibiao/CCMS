#!/bin/bash

# CCMS前端构建脚本

set -euo pipefail

# 颜色定义
GREEN='\033[0;32m'
BLUE='\033[0;34m'
NC='\033[0m'

PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")"/.. && pwd)"
FRONTEND_DIR="$PROJECT_ROOT/frontend"

log_info() {
    echo -e "${BLUE}[INFO]${NC} $(date '+%Y-%m-%d %H:%M:%S') $1"
}

log_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $(date '+%Y-%m-%d %H:%M:%S') $1"
}

main() {
    log_info "开始前端构建..."
    
    cd "$FRONTEND_DIR"
    
    # 安装依赖
    if [ ! -d "node_modules" ]; then
        log_info "安装前端依赖..."
        npm install
    fi
    
    # 构建前端
    log_info "执行构建命令..."
    npm run build
    
    log_success "前端构建完成"
    log_info "构建产物位置: $FRONTEND_DIR/dist"
}

main "$@"