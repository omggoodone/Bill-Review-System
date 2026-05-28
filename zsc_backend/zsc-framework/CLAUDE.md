# zsc-framework — 框架核心

Spring Boot 3 的安全与基础设施模块。依赖 `zsc-system`（调用其 Service），被 `zsc-admin` 依赖。

## 核心职责

### 安全链路（最重要）
```
SecurityConfig.java           ← SecurityFilterChain 定义：CSRF 关闭、Stateless 会话、白名单、JWT Filter 插入位置
JwtAuthenticationTokenFilter  ← OncePerRequestFilter，取 Bearer token → Redis 查 LoginUser → 写入 SecurityContext
                                Redis 无数据 + 有 Authorization 头 → 返回 401（强制下线提示）
TokenService.java             ← JWT 创建/解析/刷新/Redis 存储
                                新增 deleteLoginUserByUserId()：按用户 ID 删除 token（停用/删除时强制下线）
                                refreshToken 同步维护 userId→token 映射（login_user_ids:）
PermissionService.java        ← @PreAuthorize("@ss.hasPermi('xxx')") 的实际执行者
UserDetailsServiceImpl.java   ← Spring Security 的 loadUserByUsername 实现
SysLoginService.java          ← 登录业务（验证码校验 + 密码匹配 + Token 签发）
AuthenticationEntryPointImpl  ← 认证失败（401）处理
```

### AOP 切面
```
LogAspect.java          ← @Log 注解 → 自动记录操作日志到 sys_oper_log
DataScopeAspect.java    ← @DataScope 注解 → 数据权限 SQL 注入
RateLimiterAspect.java  ← @RateLimiter 限流
DataSourceAspect.java   ← 多数据源切换
```

### 配置与基础设施
```
DruidConfig.java              ← 连接池 + 多数据源
MybatisPlusConfig.java        ← 分页插件
RedisConfig.java              ← Redis 序列化（FastJson2）
CaptchaConfig.java            ← Kaptcha 验证码
ThreadPoolConfig.java         ← 异步任务线程池
ResourcesConfig.java          ← 静态资源映射
GlobalExceptionHandler.java   ← 全局异常处理
```

### 系统监控
```
web/domain/server/  ← Cpu, Jvm, Mem, Sys, SysFile (OSHI 采集)
```

## 关键约定

- JWT token 30分钟过期，存 Redis（key: `login_tokens:{uuid}`），Header: `Authorization: Bearer <token>`
- userId→token 映射存 Redis（key: `login_user_ids:{userId}`），用于按用户强制下线
- 停用/删除用户时调用 `deleteLoginUserByUserId()` 清除 Redis token，下次请求返回 401
- `@PreAuthorize("@ss.hasPermi('xxx')")` 的 `ss` 是 PermissionService 的 bean name
- `isAdmin()` 判断：检查用户是否持有 `role_key='admin'`（不再硬编码 userId==1）
- 白名单 URL 通过 `@Anonymous` 注解 + `PermitAllUrlProperties` 自动收集
- 数据权限通过 `@DataScope(deptAlias, userAlias)` + AOP 在原 SQL 上拼接 WHERE 条件
