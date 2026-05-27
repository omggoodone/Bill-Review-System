@echo off
REM 票据审核系统 - Windows 本地构建脚本
REM 只构建，不部署。产物在 zsc_frontend/dist/ 和 zsc_backend/zsc-admin/target/

echo ====================================
echo   票据审核系统 - 本地构建
echo ====================================

echo.
echo [1/2] 构建前端...
cd /d "%~dp0..\zsc_frontend"
call npm run build:prod
if %errorlevel% neq 0 exit /b %errorlevel%

echo.
echo [2/2] 构建后端...
cd /d "%~dp0..\zsc_backend"
call mvn clean package -DskipTests
if %errorlevel% neq 0 exit /b %errorlevel%

echo.
echo ====================================
echo   构建完成！
echo ====================================
echo.
echo   前端: zsc_frontend\dist\
echo   后端: zsc_backend\zsc-admin\target\zsc-admin.jar
echo.
echo   上传到服务器后:
echo     1. dist\* → /opt/bill-review/dist/
echo     2. zsc-admin.jar → /opt/bill-review/
echo     3. 配置 Nginx: cp deploy/nginx.conf → /etc/nginx/conf.d/
echo     4. 启动后端: java -jar zsc-admin.jar
echo     5. 启动 Nginx: nginx -s reload
pause
