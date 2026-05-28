---
tags: [backend, module]
aliases: [admin, 启动模块, Controller]
created: "2026-05-23"
---

# zsc-admin（应用启动入口）

**POM**: `zsc_backend/zsc-admin/pom.xml`
**依赖**: zsc-framework, zsc-quartz, zsc-generator, zsc-module, mysql-connector-j, spring-boot-devtools, springdoc-openapi

唯一的可执行模块，聚合所有子模块并包含全部 REST Controller。

## 入口点

| 类 | 文件 | 说明 |
|----|------|------|
| `ZscApplication` | `com/zsc/ZscApplication.java` | `@SpringBootApplication`，排除 `DataSourceAutoConfiguration`，打印启动横幅 |
| `ZscServletInitializer` | `com/zsc/ZscServletInitializer.java` | WAR 部署支持 (`SpringBootServletInitializer`) |

## Controller 清单 (13 个，都位于 `com.zsc.web.controller`)

### common 包

| Controller | 路径 | 说明 |
|------------|------|------|
| `CaptchaController` | `/captchaImage` | 验证码图片生成（数学计算型） |
| `CommonController` | `/common/**` | 通用文件上传/下载 |

### monitor 包

| Controller | 路径前缀 | 说明 |
|------------|----------|------|
| `CacheController` | `/monitor/cache` | Redis 缓存监控（查看 key, 清理缓存） |
| `ServerController` | `/monitor/server` | 服务器监控 (OSHI: CPU/JVM/内存/磁盘) |
| `SysLogininforController` | `/monitor/logininfor` | 登录日志 CRUD |
| `SysOperlogController` | `/monitor/operlog` | 操作日志 CRUD |
| `SysUserOnlineController` | `/monitor/online` | 在线用户管理（强退） |

### system 包

| Controller | 路径前缀 | 说明 |
|------------|----------|------|
| `SysConfigController` | `/system/config` | 系统参数配置 |
| `SysDeptController` | `/system/dept` | 部门管理（树形结构） |
| `SysDictDataController` | `/system/dict/data` | 字典数据 CRUD |
| `SysDictTypeController` | `/system/dict/type` | 字典类型 CRUD |
| `SysIndexController` | `/` | 根路径重定向 |
| `SysLoginController` | `/login`, `/getInfo`, `/getRouters` | 登录、获取用户信息、获取前端路由 |
| `SysMenuController` | `/system/menu` | 菜单管理（树形，动态路由来源） |
| `SysNoticeController` | `/system/notice` | 通知公告 CRUD |
| `SysPostController` | `/system/post` | 岗位管理 |
| `SysProfileController` | `/system/user/profile` | 个人资料修改/头像上传 |
| `SysRegisterController` | `/register` | 用户注册（提交邮箱申请） |
| `SysInitController` | `/api/init` | 系统初始化（无用户时创建超管，@Anonymous 免登录） |
| `SysAdminController` | `/api/admin` | 管理端接口（stats/users/workload/create-admin/register-requests） |
| `SysRoleController` | `/system/role` | 角色管理 + 权限分配 |
| `SysUserController` | `/system/user` | 用户 CRUD、密码重置、状态切换 |

### tool 包

| Controller | 路径 | 说明 |
|------------|------|------|
| `TestController` | `/test` | Swagger 测试占位 |

## 配置

| 文件 | 说明 |
|------|------|
| `SwaggerConfig.java` | SpringDoc OpenAPI 配置 |
| `application.yml` | 主配置：端口 8080, Redis, JWT (30min), MyBatis-Plus, 文件上传 10MB |
| `application-druid.yml` | 数据库连接 (MySQL `zsc-train`), Druid 监控 `/druid/*` |
| `logback.xml` | 日志配置 |
| `i18n/messages.properties` | 国际化 |

## JWT 认证流程

```
POST /login (用户名/密码/验证码)
  → SysLoginService.login()
  → 验证码校验 → 密码校验 → TokenService.createToken()
  → 生成 JWT → 存入 Redis (key=login_tokens:<uuid>)
  → 返回 token

后续请求:
  → JwtAuthenticationTokenFilter (OncePerRequestFilter)
  → 提取 Authorization: Bearer <token>
  → 从 Redis 获取 LoginUser → 设置 SecurityContext
```

## 相关笔记

- [[zsc-framework]] — Security + JWT 实现
- [[zsc-system]] — 用户/角色/菜单数据层
- [[../API接口]] — 完整 API 列表
- [[../../03-前端开发/前端总览|前端总览]]
