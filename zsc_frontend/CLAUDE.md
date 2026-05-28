# ZSC 前端 — 票据审核系统 Vue 3 界面

基于 RuoYi-Vue 模板，Vue 3.5 + Vite 5 + Element Plus 2.13 + Pinia 3。

## 核心机制

### 登录 → 系统初始化 → 动态路由注册

```
0. GET /api/init/status → 无用户时显示初始化表单（输邮箱 → 创建超管）
1. POST /login → 拿到 token → Cookie 存储
2. GET /getInfo → 拿到 roles[], permissions[] → user store
3. GET /getRouters → 拿到菜单 JSON 树 [{path, component, meta, children}]
4. permission store.generateRoutes()
   → import.meta.glob('@/views/**/*.vue') 匹配 component 路径字符串
   → filterDynamicRoutes() 按用户权限过滤前端预定义动态路由
   → router.addRoute() 逐一注册
```

### 四角色登录跳转（permission.js）

| 角色 | role_key | 登录后跳转 |
|------|----------|-----------|
| 超管 | admin | `/admin/super` |
| 管理员 | admin_user | `/admin/dashboard` |
| 审核员 | reviewer | `/bill/manage` |
| 普通用户 | user | `/bill/manage` |

### 权限控制（三级）

| 级别 | 机制 | 位置 |
|------|------|------|
| 路由级 | `permissions: ['biz:admin:list']` | `router/index.js` |
| 按钮级 | `v-hasPermi="['system:user:add']"` | 页面 `.vue` |
| 请求级 | 后端 `@PreAuthorize` 拦截 | Controller |

### 请求链路

```
页面 → api/*.js → Axios instance (baseURL: /dev-api)
  → 请求拦截器：注入 Authorization: Bearer <token> + 防重复提交
  → Vite proxy：去掉 /dev-api → http://localhost:8080/*
  → 响应拦截器：code 200 放行 / 401 弹登录过期框 / 500 消息提示
```

## 页面结构

```
views/
  login.vue                 ← 登录页（含系统初始化表单）
  register.vue              ← 注册页
  index.vue                 ← 仪表盘首页（按角色：审核员/普通用户两套视图）
  system/                   ← 用户/角色/菜单/部门/岗位/字典/配置/通知
    user/profile/           ← 个人中心（关闭时跳转角色概览页）
  admin/
    super/index.vue         ← 超管仪表盘（8卡片+5图表+快捷操作）
  biz/
    admin/
      index.vue             ← 管理员仪表盘（7卡片+2图表+注册审批表）
      user/index.vue        ← 用户管理（分页+批量+增删停用重置）
      register/index.vue    ← 注册审核
    bill/
      index.vue             ← 我的票据（Tab+搜索+排序+CRUD+批量操作）
      review/index.vue      ← 票据审核（搜索+排序+批量通过/退回+积压筛选）
      manage/index.vue      ← 票据概览（审核员/用户两套仪表盘）
      components/BillForm   ← 票据表单（新增/编辑/详情）
    bizCategory/index.vue   ← 类别管理（"其他"不可选/删/改）
```

## 关键约定

- 开发代理 `/dev-api` → `http://localhost:8080`
- 图标库：`src/assets/icons/svg/`（stale.svg 等）
- API 层：`src/api/biz/admin.js`（管理端）、`src/api/biz/bill.js`（票据）
- 字典数据：dice-tag 颜色由 `list_class` 字段控制（非 `css_class`）
- 个人中心关闭：所有角色跳转到各自概览页
- 超管动态路由 `/admin/super` 通过 `router/index.js` dynamicRoutes 注册
