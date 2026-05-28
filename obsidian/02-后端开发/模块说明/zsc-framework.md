---
tags: [backend, module]
aliases: [framework, 框架核心, Security, AOP]
created: "2026-05-23"
---

# zsc-framework（框架核心）

**POM**: `zsc_backend/zsc-framework/pom.xml`
**依赖**: spring-boot-starter-web, spring-boot-starter-aop, druid-spring-boot-3-starter, mybatis-plus-spring-boot3-starter, kaptcha, oshi-core, zsc-system

全项目框架胶水层，负责 Security、AOP、数据源路由、异步任务、全局异常处理。

## AOP 切面 (`com.zsc.framework.aspectj`)

| 类 | 注解 | 功能 |
|----|------|------|
| `LogAspect` | `@Log` | 拦截标注方法，自动记录操作日志到 `sys_oper_log` |
| `DataScopeAspect` | `@DataScope` | 数据权限过滤：将部门/用户范围的 SQL 条件注入查询 |
| `RateLimiterAspect` | `@RateLimiter` | 基于令牌桶的接口限流 |
| `DataSourceAspect` | `@DataSource` | 多数据源切换（主/从） |

## 安全认证 (`com.zsc.framework.security`)

### 上下文

| 类 | 说明 |
|----|------|
| `AuthenticationContextHolder` | 线程局部变量，持有当前认证信息 |
| `PermissionContextHolder` | 线程局部变量，持有权限上下文 |

### 过滤器

| 类 | 说明 |
|----|------|
| `JwtAuthenticationTokenFilter` | `OncePerRequestFilter`，从 `Authorization: Bearer <token>` 解析 JWT → Redis 获取 LoginUser → 设置 SecurityContext |

### 处理器

| 类 | 说明 |
|----|------|
| `AuthenticationEntryPointImpl` | 401 未授权 → JSON 响应 |
| `LogoutSuccessHandlerImpl` | 注销成功 → 从 Redis 删除 token |

## 配置类 (`com.zsc.framework.config`)

| 类 | 关键配置 |
|----|----------|
| `ApplicationConfig` | `@MapperScan("com.zsc.**.mapper")`，Jackson 时区 |
| `SecurityConfig` | 禁用 CSRF，无状态会话，JWT Filter 在 `UsernamePasswordAuthenticationFilter` 前，BCrypt 密码编码器，CORS 全开，白名单 URL 通过 `@Anonymous` 收集 |
| `DruidConfig` | 多数据源（主/从），`DynamicDataSource` 路由 |
| `MybatisPlusConfig` | 注册 `PaginationInnerInterceptor` (MySQL 方言) |
| `RedisConfig` | `FastJson2JsonRedisSerializer` 作为默认序列化器 |
| `ResourcesConfig` | `WebMvcConfigurer` — 静态资源映射，文件上传路径，Swagger UI，`RepeatSubmitInterceptor` 注册 |
| `ThreadPoolConfig` | `AsyncManager` 的异步线程池 |
| `CaptchaConfig` | Kaptcha 验证码生成器配置 |

### 属性类

| 类 | 说明 |
|----|------|
| `DruidProperties` | Druid 连接池参数 |
| `PermitAllUrlProperties` | 收集所有 `@Anonymous` 注解的 URL 作为白名单 |

## Web 服务层 (`com.zsc.framework.web.service`)

| 类 | Bean 名 | 职责 |
|----|---------|------|
| `TokenService` | — | JWT 创建/解析/刷新/Redis 存储。键: `login_tokens:{uuid}` + `login_user_ids:{userId}`（强制下线映射）。停用/删除用户时调 `deleteLoginUserByUserId()` 清除 token，下次请求 401 |
| `PermissionService` | `"ss"` | `@PreAuthorize("@ss.hasPermi('xxx')")` / `@ss.hasRole()` / `@ss.hasAnyRoles()` 的权限判断 |
| `SysLoginService` | — | 登录业务: 验证码校验 → 密码校验 (带重试锁定) → 创建令牌 |
| `SysRegisterService` | — | 注册业务 |
| `SysPasswordService` | — | BCrypt 密码编码/匹配 |
| `SysPermissionService` | — | 从数据库加载角色和权限集合 |
| `UserDetailsServiceImpl` | — | `UserDetailsService` 实现，按用户名加载用户 |

## 数据源管理 (`com.zsc.framework.datasource`)

| 类 | 说明 |
|----|------|
| `DynamicDataSource` | 继承 `AbstractRoutingDataSource`，运行时切换数据源 |
| `DynamicDataSourceContextHolder` | 线程局部变量持有当前数据源 key |

## 拦截器 (`com.zsc.framework.interceptor`)

| 类 | 说明 |
|----|------|
| `RepeatSubmitInterceptor` | 防重复提交拦截器接口 |
| `SameUrlDataInterceptor` | 实现：相同 URL + 请求体 → 拒绝重复提交 |

## 异步管理 (`com.zsc.framework.manager`)

| 类 | 说明 |
|----|------|
| `AsyncManager` | 异步任务管理器（使用 `ThreadPoolConfig` 线程池） |
| `ShutdownManager` | 优雅关闭 |
| `AsyncFactory` | 异步任务工厂（记录登录日志、操作日志） |

## 系统监控 (`com.zsc.framework.web.domain`)

| 类 | 说明 |
|----|------|
| `Server` | 服务器信息聚合 |
| `server/Cpu` | CPU 信息 (OSHI) |
| `server/Jvm` | JVM 内存/版本/启动时间 |
| `server/Mem` | 物理内存 |
| `server/Sys` | 操作系统信息 |
| `server/SysFile` | 磁盘/文件系统信息 |

## 全局异常处理

`GlobalExceptionHandler` — `@RestControllerAdvice`，统一处理所有异常类型。

## 相关笔记

- [[zsc-admin]]
- [[zsc-system]]
- [[../技术笔记/SpringSecurity|Spring Security 笔记]]
- [[../技术笔记/JWT|JWT 笔记]]
- [[../技术笔记/Redis|Redis 笔记]]
