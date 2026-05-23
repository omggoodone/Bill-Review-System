---
tags: [backend, api]
---

# API 接口

## 全局规范

| 项目 | 说明 |
|------|------|
| 认证方式 | `Authorization: Bearer <token>` (JWT) |
| 内容类型 | `application/json` |
| 跨域 | CORS 全开（开发阶段） |
| 文档 | Swagger: http://localhost:8080/swagger-ui/index.html |

## 通用响应格式

### 系统模块 (AjaxResult)

```json
// 成功
{"code": 200, "msg": "操作成功", "data": {}}

// 分页
{"code": 200, "msg": "查询成功", "rows": [], "total": 100}

// 失败
{"code": 500, "msg": "操作失败"}

// 警告
{"code": 301, "msg": "警告信息"}
```

### 业务模块 (ResultVo<T>)

```json
// 成功
{"code": "SUCCESS_000", "message": "操作成功", "data": {}}

// 分页
{"code": "SUCCESS_000", "message": "查询成功", "data": {
  "currentPage": 1, "pageSize": 10, "total": 100, "list": []
}}
```

## 认证接口

| 方法 | 路径 | 说明 | 匿名 |
|------|------|------|------|
| POST | `/login` | 登录 (username, password, code, uuid) | 是 |
| POST | `/register` | 注册 | 是 |
| GET | `/captchaImage` | 获取验证码图片 | 是 |
| GET | `/getInfo` | 获取当前用户信息 (roles, permissions) | 否 |
| GET | `/getRouters` | 获取前端动态路由树 | 否 |
| POST | `/logout` | 退出登录 | 否 |

## 系统管理接口 (路径前缀 `/system`)

### 用户管理 `/system/user`

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| GET | `/list` | 用户列表 (分页, 支持部门过滤) | `system:user:list` |
| GET | `/{userId}` | 用户详情 | `system:user:query` |
| POST | — | 新增用户 | `system:user:add` |
| PUT | — | 修改用户 | `system:user:edit` |
| DELETE | `/{userIds}` | 删除用户 | `system:user:remove` |
| PUT | `/resetPwd` | 重置密码 | `system:user:resetPwd` |
| PUT | `/changeStatus` | 状态切换 (正常/停用) | `system:user:edit` |

### 角色管理 `/system/role`

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/list` | 角色列表 |
| GET | `/{roleId}` | 角色详情 |
| POST | — | 新增角色 |
| PUT | — | 修改角色 |
| PUT | `/dataScope` | 设置数据权限 |
| DELETE | `/{roleIds}` | 删除角色 |

### 菜单管理 `/system/menu`

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/list` | 菜单列表 (树形) |
| GET | `/treeselect` | 菜单下拉树 (选择父菜单用) |
| GET | `/roleMenuTreeselect/{roleId}` | 角色菜单树 (角色授权用) |
| POST | — | 新增菜单 |
| PUT | — | 修改菜单 |
| DELETE | `/{menuId}` | 删除菜单 |

### 部门管理 `/system/dept`

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/list` | 部门列表 (树形) |
| GET | `/list/exclude/{deptId}` | 排除指定部门的树 |
| GET | `/{deptId}` | 部门详情 |
| POST | — | 新增部门 |
| PUT | — | 修改部门 |
| DELETE | `/{deptId}` | 删除部门 |

### 其他系统接口

| 模块 | 路径 | 主要方法 |
|------|------|----------|
| 岗位 | `/system/post` | list, get, add, edit, delete |
| 字典类型 | `/system/dict/type` | listType, getType, add, edit, delete, refreshCache, optionselect |
| 字典数据 | `/system/dict/data` | list, get, getDicts/{dictType}, add, edit, delete |
| 参数配置 | `/system/config` | list, get, getConfigKey/{key}, add, edit, delete, refreshCache |
| 通知公告 | `/system/notice` | list, get, add, edit, delete |
| 个人中心 | `/system/user/profile` | 查看/修改资料, 上传头像, 修改密码 |

## 监控接口 (路径前缀 `/monitor`)

| 模块 | 路径 | 主要方法 |
|------|------|----------|
| 在线用户 | `/monitor/online` | list, forceLogout |
| 定时任务 | `/monitor/job` | listJob, getJob, addJob, updateJob, changeJobStatus, runJob, delJob |
| 任务日志 | `/monitor/jobLog` | listJobLog, delJobLog, cleanJobLog |
| 操作日志 | `/monitor/operlog` | list, delOperlog, cleanOperlog |
| 登录日志 | `/monitor/logininfor` | list, delLogininfor, unlockLogininfor, cleanLogininfor |
| 缓存监控 | `/monitor/cache` | getCache, listCacheName, listCacheKey, getCacheValue, clear* |
| 服务监控 | `/monitor/server` | getServer (CPU/JVM/内存/磁盘) |

## 业务接口 `/api/category`

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| GET | `/{id}` | 按 ID 查询 | `biz:category:query` |
| POST | `/query` | 分页条件查询 | `biz:category:query` |
| POST | — | 新增类别 | `biz:category:add` |
| PUT | — | 修改类别 | `biz:category:edit` |
| DELETE | `/{id}` | 删除类别 | `biz:category:remove` |

## 业务接口 `/api/bill`

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| POST | `/query` | 分页查询票据列表（普通用户只看自己） | `biz:bill:list` |
| GET | `/{id}` | 票据详情（含附件列表 + 审核历史） | `biz:bill:query` |
| POST | — | 新增票据（status=0 草稿 / status=1 提交） | `biz:bill:add` |
| PUT | — | 修改票据（仅草稿或已退回状态） | `biz:bill:add` |
| POST | `/submit/{id}` | 提交草稿进入审核流程 | `biz:bill:add` |
| DELETE | `/{id}` | 删除草稿票据及其附件 | `biz:bill:remove` |
| POST | `/review` | 审核票据（通过/退回） | `biz:bill:review` |

## 代码生成器 `/tool/gen`

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/list` | 已导入表列表 |
| GET | `/db/list` | 数据库表列表 |
| GET | `/{tableId}` | 表生成配置 |
| PUT | — | 修改生成配置 |
| POST | `/importTable` | 导入数据库表 |
| POST | `/createTable` | 在线创建表 |
| GET | `/preview/{tableId}` | 预览生成代码 |
| DELETE | `/{tableIds}` | 删除表配置 |
| GET | `/genCode/{tableName}` | 下载生成的代码 ZIP |
| GET | `/synchDb/{tableName}` | 同步数据库表结构 |

## 相关笔记

- [[后端总览]]
- [[模块说明/zsc-admin|zsc-admin]] — Controller 实现
- [[模块说明/zsc-module|zsc-module]] — 业务接口实现
- [[../03-前端开发/前端总览|前端 API 封装]]
