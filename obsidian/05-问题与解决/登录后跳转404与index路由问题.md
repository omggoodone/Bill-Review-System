---
tags: [issue, frontend, backend]
created: "2026-05-26"
status: 已解决
---

# 登录后跳转 404 与 /index 路由问题

## 问题描述

1. 用户登录后跳转到 `http://localhost/index` 返回 404
2. 新注册用户登录后无法访问票据概览
3. 注册页点击"使用已有账户登录"跳转到 `/bill/manage` 404

## 原因分析

### `/index` 路由不存在
多处硬编码了 `location.href = '/index'`，但前端路由中没有 `/index`：
- `request.js` 401 处理器：会话过期时跳转 `/index`
- `profile/index.vue` 退出登录：跳转 `/index`
- `404.vue` "返回首页"：链接到 `/index`

### 新注册用户无角色
`SysRegisterService.register()` 只创建用户，不分配角色。登录后 `roles = ['ROLE_DEFAULT']`，无任何菜单权限 → 动态路由为空 → `/bill/manage` 未注册 → 404。

### 统计接口数据未隔离
`getCategoryAmountSummary()` 和 `getMonthlyTrend()` 查询所有用户数据，未按当前用户过滤。

## 解决方案

### 1. `/index` → `/login` 或 `/`
```js
// request.js (401)
location.href = '/login'
// profile/index.vue (退出)
location.href = '/login'
// 404.vue (返回首页)
<router-link to="/">
```

### 2. 注册时自动分配角色
```java
// SysRegisterService.java
userService.insertUserAuth(sysUser.getUserId(), new Long[]{3L});
```

### 3. 统计接口按用户隔离
```java
if (!SecurityUtils.hasPermi("biz:bill:review")) {
    wrapper.eq(BizBill::getCreateBy, SecurityUtils.getUsername());
}
```

### 4. 统计只看已通过票据
```java
.eq(BizBill::getStatus, "2")  // 仅已通过
```

## 相关笔记

- [[票据列表Tab切换数据重复与Vue-DOM错误]]
- [[批量提交票据编号重复冲突]]
