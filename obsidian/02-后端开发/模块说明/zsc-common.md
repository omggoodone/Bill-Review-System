---
tags: [backend, module]
aliases: [common, 公共工具, 基础设施]
created: "2026-05-23"
---

# zsc-common（公共基础设施）

**POM**: `zsc_backend/zsc-common/pom.xml`
**依赖**: spring-context-support, spring-web, spring-boot-starter-security, spring-boot-starter-validation, commons-lang3, jackson-databind, fastjson2, commons-io, poi-ooxml, jjwt, jaxb-api, spring-boot-starter-data-redis, commons-pool2, yauaa, jakarta.servlet-api, mybatis-plus-spring-boot3-starter

最底层的公共模块，99 个源文件。提供注解、工具类、基类、枚举、异常、XSS 防御等基础设施。

## 注解 (`com.zsc.common.annotation`)

| 注解 | 用途 | 使用场景 |
|------|------|----------|
| `@Anonymous` | 标记 URL 允许匿名访问 | 登录、注册、验证码接口 |
| `@DataScope` | 数据权限过滤 | Service 方法上，注入部门/用户范围 SQL |
| `@DataSource` | 多数据源切换 | 指定主库/从库 |
| `@Excel` | Excel 导入导出字段标注 | 实体字段上 |
| `@Excels` | 多 `@Excel` 容器 | 嵌套实体导出 |
| `@Log` | 操作日志记录 | Controller 方法上 |
| `@RateLimiter` | 接口限流 | 高并发接口 |
| `@RepeatSubmit` | 防重复提交 | 表单提交接口 |
| `@Sensitive` | 数据脱敏 | 返回字段（手机号、身份证等） |

## 核心基类 (`com.zsc.common.core`)

### 实体基类

| 类 | 字段 | 说明 |
|----|------|------|
| `BaseEntity` | createBy, createTime, updateBy, updateTime, remark, params | 所有业务实体基类 |
| `TreeEntity` | 继承 BaseEntity + parentId, ancestors, children, orderNum | 树形实体基类（部门、菜单） |

### 响应基类

| 类               | 继承                                | 关键方法                             | 说明       |
| --------------- | --------------------------------- | -------------------------------- | -------- |
| `AjaxResult`    | `extends HashMap<String, Object>` | `success()`, `error()`, `warn()` | 系统模块统一响应 |
| `R<T>`          | —                                 | `ok()`, `fail()`                 | 另一通用响应包装 |
| `TableDataInfo` | —                                 | code, msg, rows, total           | 分页查询响应   |

### 分页工具

| 类 | 说明 |
|----|------|
| `PageDomain` | 分页请求参数 (pageNum, pageSize, orderByColumn, isAsc) |
| `TableSupport` | 从 HttpServletRequest 构建 PageDomain |

### Controller 基类

`BaseController` (`com.zsc.common.core.controller`)：
- `startPage()` — 启动 MyBatis-Plus 分页
- `getDataTable()` — 包装分页结果为 `TableDataInfo`
- `success()` / `error()` / `toAjax()` — 快捷返回 `AjaxResult`
- `getLoginUser()` / `getUserId()` / `getUsername()` — 获取当前用户

### Redis 工具

`RedisCache` (`com.zsc.common.core.redis`)：
- `setCacheObject(key, value, timeout, unit)`
- `getCacheObject(key)`
- `deleteObject(key)`
- `setCacheList()` / `getCacheList()`
- `keys(pattern)` — 按模式匹配 key

## 常量 (`com.zsc.common.constant`)

| 类 | 内容 |
|----|------|
| `Constants` | Token 前缀, JWT claims key, 角色分隔符, 通用状态 |
| `CacheConstants` | Redis key 前缀（login_tokens, sys_config, sys_dict） |
| `UserConstants` | 用户状态: 正常/停用/删除, 唯一性校验 |
| `HttpStatus` | SUCCESS=200, ERROR=500, WARN=301 |
| `GenConstants` | 代码生成器常量（Java 类型映射, 模板名称） |
| `ScheduleConstants` | Quartz 任务常量 |

## 枚举 (`com.zsc.common.enums`)

| 枚举 | 值 |
|------|-----|
| `BusinessType` | INSERT, UPDATE, DELETE, EXPORT, IMPORT, OTHER |
| `BusinessStatus` | SUCCESS, FAIL |
| `DataSourceType` | MASTER, SLAVE |
| `HttpMethod` | GET, POST, PUT, DELETE |
| `LimitType` | DEFAULT, IP |
| `OperatorType` | MANAGE, MOBILE, OTHER |
| `UserStatus` | OK, DISABLE, DELETED |
| `DesensitizedType` | PHONE, EMAIL, ID_CARD, BANK_CARD 等 |

## 异常体系 (`com.zsc.common.exception`)

```
BaseException (base/)
  ├── GlobalException
  ├── ServiceException
  ├── UtilException
  ├── DemoModeException
  ├── file/ (FileException, FileUploadException, ...)
  ├── job/TaskException
  └── user/ (UserException, CaptchaException, UserPasswordNotMatchException,
             UserPasswordRetryLimitExceedException, BlackListException, ...)
```

每个用户异常对应具体的登录失败场景，返回不同错误提示。

## 工具类 (`com.zsc.common.utils`，25+ 个类)

| 类别      | 工具类                                                                                               |
| ------- | ------------------------------------------------------------------------------------------------- |
| 通用      | `StringUtils`, `DateUtils`, `Arith`(精确计算), `LogUtils`, `ExceptionUtil`, `MessageUtils`, `Threads` |
| Bean    | `BeanUtils`, `BeanValidators`                                                                     |
| 文件      | `FileUtils`, `FileUploadUtils`, `FileTypeUtils`, `ImageUtils`, `MimeTypeUtils`                    |
| HTTP    | `HttpUtils`, `HttpHelper`, `UserAgentUtils` (yauaa 解析 UA)                                         |
| IP      | `IpUtils`, `AddressUtils` (IP 归属地)                                                                |
| Excel   | `ExcelUtil` (POI 读写)                                                                              |
| 加密      | `Base64`, `Md5Utils`                                                                              |
| UUID    | `IdUtils`, `Seq`, `UUID`                                                                          |
| SQL     | `SqlUtil` (防注入转义)                                                                                 |
| Spring  | `SpringUtils` (获取 Bean)                                                                           |
| Servlet | `ServletUtils`                                                                                    |
| 安全      | `SecurityUtils` (获取当前 LoginUser)                                                                  |
| 字典      | `DictUtils`                                                                                       |

## XSS / 过滤器 (`com.zsc.common.filter`)

| 类 | 说明 |
|----|------|
| `XssFilter` | XSS 过滤过滤器 |
| `XssHttpServletRequestWrapper` | Request 包装器，转义 HTML 特殊字符 |
| `RepeatableFilter` | 使 request body 可重复读取 |
| `RepeatedlyRequestWrapper` | Body 缓存包装 |
| `RefererFilter` | Referer 校验 |

## 配置

| 类 | 说明 |
|----|------|
| `RuoYiConfig` | 应用配置: name, version, copyright, profile, captchaType |

## 相关笔记

- [[zsc-system]] — 使用方
- [[zsc-framework]] — 使用方
- [[../后端总览|后端总览]]
