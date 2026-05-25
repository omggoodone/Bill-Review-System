---
tags: [issue, frontend]
created: "2026-05-26"
status: 已解决
---

# 票据列表 Tab 切换数据重复与 Vue DOM 错误

## 问题描述

1. 我的票据页面快速切换状态 Tab（草稿/已提交/…）时，表格出现其他状态的数据
2. 连续点击刷新按钮弹出"数据正在处理，请勿重复提交"
3. 控制台大量报错：`Cannot read properties of null (reading 'insertBefore')`

## 错误信息

```
Uncaught (in promise) TypeError: Cannot read properties of null (reading 'insertBefore')
    at insert (runtime-dom.esm-bundler.js:28:12)
    at processCommentNode (runtime-core.esm-bundler.js:5474:7)
```
```
Uncaught (in promise) TypeError: Cannot read properties of null (reading 'exposed')
    at getComponentPublicInstance (runtime-core.esm-bundler.js:8246:16)
```

## 复现步骤

1. 打开我的票据页面
2. 快速连续点击"草稿"→"已提交"→"草稿" Tab
3. 观察表格数据：草稿 Tab 下出现了已提交状态的票据
4. 查看控制台：大量 Vue DOM 错误

## 原因分析

### 数据重复
1. **Vue 响应式代理序列化问题**：`queryParams` 是 `ref({})`，传给 axios 时被 `JSON.stringify` 序列化，响应式代理对象可能丢失字段
2. **请求竞态**：Tab A → 请求A 发出 → Tab B → 请求B 发出 → 请求A 响应（慢）覆盖请求B 响应 → 显示错误数据

### Vue DOM 错误
1. **`v-if` 注释锚点丢失**：`el-dialog`（append-to-body teleport）+ 内部 `v-if`，dialog 关闭时 teleport 内容被销毁，Vue 的 `v-if` 注释锚点引用失效
2. **表格 `v-if` 频繁切换**：操作列按钮 `v-if="canEdit(row)"` 在数据快速切换时反复创建/销毁 DOM 节点

### 防重复提交误触发
查询 API（`listBill`/`listBizCategory`）使用 `method: 'post'`，触发了 `request.js` 拦截器的防重复提交逻辑

## 解决方案

### 1. AbortController 取消旧请求 (`src/views/biz/bill/index.vue`)

```js
let abortController = null
function getList() {
  if (abortController) abortController.abort()
  abortController = new AbortController()
  const params = { /* 显式构建普通对象 */ }
  listBill(params, abortController.signal).then(...)
}
```

### 2. API 支持 signal (`src/api/biz/bill.js`)

```js
export function listBill(query, signal) {
  return request({ url: '/api/bill/query', method: 'post', data: query, signal })
}
```

### 3. 表格按钮 v-if → v-show (`src/views/biz/bill/index.vue`)

```html
<!-- 改前 -->
<el-button v-if="canEdit(scope.row)" ... />
<!-- 改后 -->
<el-button v-show="canEdit(scope.row)" ... />
```

### 4. 查询 API 跳过防重复检查

```js
headers: { repeatSubmit: false }
```

### 5. el-dialog 加 destroy-on-close，内部 v-if → v-show

## 相关笔记

- [[../03-前端开发/页面说明|前端页面说明]]
- [[../03-前端开发/路由设计|路由设计]]
