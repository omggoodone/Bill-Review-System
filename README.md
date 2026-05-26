# 票据审核系统 (Bill Review System)

基于 RuoYi-Vue 前后端分离框架开发的在线票据提交与审核系统。支持三角色协作：普通用户提交票据、审核员审核、系统管理员管理用户与注册审批。

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

## 项目结构

```
Bill-Review-System/
├── zsc_backend/                    # 后端 (Spring Boot 多模块)
│   ├── zsc-admin/                  # 应用入口 + Controller
│   ├── zsc-framework/              # 框架核心 (Security / Redis / JWT)
│   ├── zsc-system/                 # 系统管理 (用户 / 角色 / 菜单)
│   ├── zsc-module/                 # 业务模块 (票据管理 / 注册申请)
│   ├── zsc-common/                 # 公共工具
│   └── sql/                        # 数据库脚本 + 迁移
│
├── zsc_frontend/                   # 前端 (Vue 3 SPA)
│   └── src/
│       ├── views/biz/bill/         # 票据管理 (概览/列表/审核)
│       ├── views/biz/admin/        # 管理员页面 (概览/用户/注册审核)
│       ├── views/biz/bizCategory/  # 类别管理
│       ├── api/biz/                # API 封装
│       ├── router/                 # 路由配置
│       └── store/                  # 状态管理
│
├── docs/                           # 设计文档
└── obsidian/                       # Obsidian 知识库
```

## 系统角色

| 角色 | role_key | 职责 |
|------|----------|------|
| 普通用户 | `user` | 提交/管理票据，查看个人仪表盘 |
| 票据审核员 | `reviewer` | 审核票据（通过/退回），查看审核统计 |
| 系统管理员 | `admin_user` | 用户管理、类别管理、注册审批、系统概览 |

## 功能模块

### 普通用户
- **注册申请**: 提交邮箱 + 申请说明 + 角色选择，管理员审批后生成账号
- **票据概览**: 个人统计卡片 + ECharts 分类饼图 + 类别金额柱状图
- **票据管理**: 新增（草稿/直接提交）、编辑草稿、删除草稿、查看详情
- **状态筛选**: 全部 / 草稿 / 待审核 / 已通过 / 已退回

### 票据审核员
- **审核仪表盘**: 待审/今日通过/今日退回/通过率/积压预警（>3天）
- **票据审核**: 通过/退回已提交票据，按提交人/类别/金额筛选
- **积压标记**: 超 3 天未审票据黄色高亮

### 系统管理员
- **管理概览**: 用户/审核员/管理员/票据数统计
- **用户管理**: 按用户名/角色/状态筛选，重置密码、启停、删除
- **注册审批**: 通过→生成账号密码，拒绝→填写意见
- **类别管理**: 业务类别 CRUD

## 快速开始

### 环境要求

- JDK 17+
- MySQL 8.0+
- Redis
- Maven 3.6+
- Node.js 22+

### 后端启动

```bash
# 1. 创建数据库并导入
mysql -u root -p -e "CREATE DATABASE IF NOT EXISTS \`zsc-train\` DEFAULT CHARSET utf8mb4"
mysql -u root -p zsc-train < zsc_backend/sql/zsc.sql

# 2. 修改数据库密码
# 编辑 zsc_backend/zsc-admin/src/main/resources/application-druid.yml

# 3. 启动
cd zsc_backend
mvn spring-boot:run -pl zsc-admin
```

### 前端启动

```bash
cd zsc_frontend
npm install
npm run dev
```

访问: http://localhost

### 默认账号

| 账号 | 密码 | 角色 |
|------|------|------|
| admin | admin123 | 超级管理员（含全部系统权限） |

> 系统管理员（admin_user）需在数据库中通过 sys_role_menu 分配角色。详见 `migrations/14_add_admin_role.sql`。

## 相关资源

- [RuoYi-Vue 框架](https://gitee.com/y_project/RuoYi-Vue)
- [Element Plus 文档](https://element-plus.org/)
- [MyBatis-Plus 文档](https://baomidou.com/)
