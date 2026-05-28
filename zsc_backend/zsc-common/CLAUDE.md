# zsc-common — 公共基础设施

**被所有模块依赖**的最底层模块，提供注解、基类、工具类、统一响应、枚举。无外部业务依赖。

## 核心内容

```
annotation/    ← @Log, @RateLimiter, @RepeatSubmit, @DataScope, @Sensitive, @Anonymous, @Excel
core/
  controller/BaseController.java   ← 所有 Controller 的基类（startPage/getDataTable/success/error）
  domain/AjaxResult.java           ← 系统模块统一响应 {code, msg, data}
  domain/BaseEntity.java           ← 实体基类（createBy/createTime/updateBy/updateTime/remark）
  domain/TreeEntity.java           ← 树形实体基类（含 parentId/ancestors）
  domain/entity/                   ← 核心系统实体：SysUser, SysRole, SysDept, SysMenu, SysDict*
  domain/model/LoginUser.java      ← 登录用户模型（存 Redis，含 roles + permissions 集合）
  page/TableDataInfo.java          ← 分页响应 {code, msg, rows, total}
  page/TableSupport.java           ← 分页入参构建工具
enums/        ← BusinessType, UserStatus, HttpMethod, LimitType, OperatorType...
utils/        ← SecurityUtils, StringUtils, ExcelUtil(poi), 通用工具类包
exception/    ← 业务异常体系（用户/文件/任务/服务/DemoMode/全局异常）
filter/       ← XSS 过滤, RepeatableRequest 等 Servlet Filter
```

## 关键约定

- `SysUser.isAdmin()`：按角色 `role_key='admin'` 判断（兼容旧 user_id==1 逻辑）
- 统一响应：系统模块用 `AjaxResult`，业务模块(zsc-module)用自己的一套 `ResultVo`
- 实体继承：普通表实体 extends `BaseEntity`，树形表 extends `TreeEntity`
- 脱敏：`@Sensitive` 注解 + `SensitiveJsonSerializer` Jackson 序列化
- Excel：`@Excel` 注解 + `ExcelUtil.exportExcel()/importExcel()`
