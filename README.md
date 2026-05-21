# 票据审核系统 (Bill Review System)

基于 RuoYi-Vue 前后端分离框架开发的在线票据提交与审核系统。支持员工提交报销/差旅/办公等票据，管理员在线审核。

## 技术栈

| 层级 | 技术 |
|------|------|
| 后端框架 | Spring Boot 3.5.8 |
| ORM | MyBatis-Plus 3.5.9 |
| 安全框架 | Spring Security + JWT |
| 数据库 | MySQL 8.0 |
| 缓存 | Redis |
| 前端框架 | Vue 3.5 + Vite 5 |
| UI 组件库 | Element Plus 2.13 |
| 状态管理 | Pinia 3 |
| 构建工具 | Maven + npm |

## 项目结构

```
Bill-Review-System/
├── zsc_backend/                    # 后端 (Spring Boot 多模块)
│   ├── zsc-admin/                  # 应用入口 + Controller
│   ├── zsc-framework/              # 框架核心 (Security / Redis / JWT)
│   ├── zsc-system/                 # 系统管理 (用户 / 角色 / 菜单)
│   ├── zsc-module/                 # 业务模块 (票据管理)
│   ├── zsc-common/                 # 公共工具
│   ├── zsc-quartz/                 # 定时任务
│   ├── zsc-generator/             # 代码生成器
│   └── sql/                        # 数据库脚本
│
├── zsc_frontend/                   # 前端 (Vue 3 SPA)
│   └── src/
│       ├── views/biz/bill/         # 票据管理页面
│       ├── api/biz/                # API 封装
│       ├── router/                 # 路由配置
│       └── store/                  # 状态管理
│
├── docs/                           # 设计文档
│   ├── 需求规格说明书.md / .docx
│   ├── 可行性分析报告.md / .docx
│   ├── 概要设计说明书.md / .docx
│   └── 详细设计说明书.md / .docx
│
└── GIT_GUIDE.md                    # 小组 Git 协作指南
```

## 功能模块

### 普通用户
- 票据管理：新增（草稿/提交）、编辑草稿、删除草稿、查看详情
- 状态查看：按状态筛选（全部/草稿/待审核/已通过/已退回）
- 票据查询：按关键词、类型、日期范围搜索
- 文件上传：支持上传票据附件
- 用户注册

### 管理员
- 票据审核：审核已提交票据（通过/退回，填写审核意见）
- 用户管理：用户 CRUD、密码重置、账号冻结/解冻
- 系统管理：角色管理、菜单管理、日志查看等

## 快速开始

### 环境要求

- JDK 17+
- MySQL 8.0+
- Redis
- Maven 3.6+
- Node.js 22+

### 后端启动

```bash
# 1. 创建数据库并导入表结构
mysql -u root -p < zsc_backend/sql/zsc.sql
mysql -u root -p < zsc_backend/sql/biz_bill.sql

# 2. 修改数据库配置
# 编辑 zsc_backend/zsc-admin/src/main/resources/application-druid.yml
# 填入你的 MySQL 用户名和密码

# 3. 编译并启动
cd zsc_backend
mvn clean package -DskipTests
java -jar zsc-admin/target/zsc-admin.jar
```

后端启动后访问：
- API 接口: http://localhost:8080
- Swagger 文档: http://localhost:8080/swagger-ui/index.html

### 前端启动

```bash
cd zsc_frontend
npm install
npm run dev
```

前端启动后访问：http://localhost:80

### 默认账号

| 账号 | 密码 | 角色 |
|------|------|------|
| admin | admin123 | 管理员 |

## 版本标签

| Tag | 说明 |
|-----|------|
| [v0.1-docs](https://github.com/cupcoff1/Bill-Review-System/releases/tag/v0.1-docs) | 全部设计文档完成 |

## 相关资源

- [RuoYi-Vue 框架](https://gitee.com/y_project/RuoYi-Vue)
- [Element Plus 文档](https://element-plus.org/)
- [MyBatis-Plus 文档](https://baomidou.com/)
