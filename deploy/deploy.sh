#!/bin/bash
# 票据审核系统 - 一键构建部署脚本
# 用法: bash deploy.sh

set -e

DEPLOY_DIR="/opt/bill-review"
PROJECT_DIR="$(cd "$(dirname "$0")/.." && pwd)"

echo "===================================="
echo "  票据审核系统 - 生产构建部署"
echo "===================================="
echo ""

# 1. 构建前端
echo "[1/4] 构建前端..."
cd "$PROJECT_DIR/zsc_frontend"
npm install --silent
npm run build:prod
echo "      前端构建完成 → zsc_frontend/dist/"

# 2. 构建后端
echo "[2/4] 构建后端..."
cd "$PROJECT_DIR/zsc_backend"
mvn clean package -DskipTests -q
echo "      后端构建完成 → zsc-admin/target/zsc-admin.jar"

# 3. 创建部署目录
echo "[3/4] 准备部署目录..."
sudo mkdir -p "$DEPLOY_DIR"/{dist,logs,uploads}

# 复制前端 dist
sudo cp -r "$PROJECT_DIR/zsc_frontend/dist/"* "$DEPLOY_DIR/dist/"

# 复制后端 JAR
cp "$PROJECT_DIR/zsc_backend/zsc-admin/target/zsc-admin.jar" "$DEPLOY_DIR/"

# 复制 Nginx 配置
sudo cp "$PROJECT_DIR/deploy/nginx.conf" /etc/nginx/conf.d/bill.conf

echo "      文件已复制到 $DEPLOY_DIR"

# 4. 提示
echo "[4/4] 部署完成！"
echo ""
echo "===================================="
echo "  后续步骤（手动执行）"
echo "===================================="
echo ""
echo "1. 启动后端："
echo "   cd $DEPLOY_DIR"
echo "   nohup java -jar zsc-admin.jar --spring.profiles.active=prod > logs/app.log 2>&1 &"
echo ""
echo "2. 启动 Nginx："
echo "   sudo nginx -t          # 检查配置"
echo "   sudo nginx -s reload   # 重载配置"
echo ""
echo "3. 验证："
echo "   打开 http://服务器IP"
echo "   API 测试: curl http://127.0.0.1:8080/"
echo ""
echo "4. 查看日志："
echo "   后端: tail -f $DEPLOY_DIR/logs/app.log"
echo "   Nginx: tail -f $DEPLOY_DIR/logs/nginx-access.log"
