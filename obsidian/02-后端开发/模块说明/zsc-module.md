---
tags: [backend, module]
aliases: [module, 业务模块, BizCategory, biz_bill]
created: "2026-05-23"
---

# zsc-module（自定义业务模块）

**POM**: `zsc_backend/zsc-module/pom.xml`
**描述**: "票据审核系统核心业务模块（票据/审核/注册/类别/邮件）"
**依赖**: zsc-framework, lombok, spring-boot-starter-validation, spring-boot-starter-mail, spring-boot-starter-thymeleaf, springdoc-openapi

自定义业务模块。已从最初的 BizCategory CRUD 扩展为完整的票据审核系统：

- **BizBill** — 票据 CRUD + 审核 + 批量审核 + 统计
- **BizAuditLog** — 审核历史
- **BizBillFile** — 票据附件
- **BizRegisterRequest** — 注册申请 + 审批
- **BizCategory** — 类别管理 + 删除保护（"其他"不可删，票据自动迁移）
- **EmailService** — @Async 邮件通知（创建/停用/删除）

## 架构特点

与系统模块 (`zsc-admin`/`zsc-system`) 采用不同的响应体系：

| 维度 | 系统模块 | zsc-module（本模块） |
|------|----------|---------------------|
| Controller 基类 | 继承 `BaseController` | 直接 `@RestController` |
| 响应包装 | `AjaxResult extends HashMap` | `ResultVo<T>` (Lombok POJO) |
| 分页方式 | `startPage()` + `getDataTable()` | `BasePageReq` → `PageResult` |
| 实体风格 | 继承 `BaseEntity` | Lombok `@Data @Builder`，不继承基类 |
| 入参 | 无 DTO 分离 | 严格 DTO / VO / QueryDto 分离 |
| 参数校验 | `@Validated` 在 Controller | `@Valid` 在 DTO 字段 |
| API 路径 | `/system/*` | `/api/category` |

## Controller

`BizCategoryController` — 路径前缀 `/api/category`，权限前缀 `biz:category:*`

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| GET | `/api/category/{id}` | 按 ID 查询 | `biz:category:query` |
| POST | `/api/category/query` | 分页条件查询 | `biz:category:query` |
| POST | `/api/category` | 新增 | `biz:category:add` |
| PUT | `/api/category` | 修改 | `biz:category:edit` |
| DELETE | `/api/category/{id}` | 删除 | `biz:category:remove` |

所有端点使用 SpringDoc `@Tag` / `@Operation` 注解，输入通过 `@Valid` / `@Validated` 校验。

## 实体与 DTO

### Entity

`BizCategory` — 使用 Lombok + MyBatis-Plus：

| 字段 | 类型 | 注解 |
|------|------|------|
| categoryId | Long | `@TableId(type = IdType.AUTO)` |
| categoryName | String | — |
| sortOrder | Integer | — |
| status | String | — |
| createTime | LocalDateTime | — |
| updateTime | LocalDateTime | — |

### DTO

| 类 | 用途 | 校验 |
|----|------|------|
| `BizCategoryDto` | 创建/更新输入 | `@NotBlank` categoryName, `@NotNull` sortOrder |
| `BizCategoryQueryDto` | 分页条件查询 | 继承 `BasePageReq`，字段: categoryName, status |

### VO

| 类 | 说明 |
|----|------|
| `BizCategoryVo` | 输出 VO（不含内部字段） |

## Mapper

`BizCategoryMapper` — 继承 `BaseMapper<BizCategory>`，纯 MyBatis-Plus，无 XML 文件。

## Service

`BizCategoryServiceImpl` — 继承 `ServiceImpl<BizCategoryMapper, BizCategory>`：
- 使用 `lambdaQuery()` 进行链式条件查询
- `@Transactional` 保证事务
- `BeanUtils.copyProperties(dto, entity)` 进行 DTO → Entity 映射

```java
// 典型查询链
lambdaQuery()
  .eq(StringUtils.hasText(name), BizCategory::getCategoryName, name)
  .eq(StringUtils.hasText(status), BizCategory::getStatus, status)
  .orderByAsc(BizCategory::getSortOrder)
  .page(page);
```

## 模块专属基础设施 (`com.zsc.module.common`)

### 响应

| 类 | 说明 |
|----|------|
| `ResultVo<T>` | 统一响应 `{code, message, data}`，`@JsonInclude(NON_NULL)` |
| `GlobalCodeEnum` | 响应码枚举 (SUCCESS_000, INTERNAL_SERVER_ERROR 等) |

### 分页

| 类 | 说明 |
|----|------|
| `BasePageReq` | 分页请求基类 (`current`, `size`) → `convertToPage()` 转 MyBatis-Plus `Page` |
| `PageResult<T>` | 分页响应 (`currentPage, pageSize, total, list`) → `fromPage(Page)` 工厂方法 |

### 其他

| 类 | 说明 |
|----|------|
| `ServiceException` | 模块专属业务异常 |
| `WebConstants` | Web 常量 |
| `BaseEnum` / `EnumUtil` | 枚举接口与查找工具 |
| `MailConfig` | 邮件配置（spring-boot-starter-mail） |

## 如何用本模块做模板开发新功能

1. 参考 `BizCategory` 的 Entity → DTO → VO → Mapper → Service → Controller 分层
2. 复用 `common/response/ResultVo`、`common/pagination/` 作为响应和分页
3. 新业务放在新的 controller/service/mapper 包下
4. 权限注解: `@PreAuthorize("@ss.hasPermi('biz:xxx:xxx')")`

## 相关笔记

- [[../后端总览|后端总览]]
- [[zsc-framework]]
- [[zsc-generator]] — 可用代码生成器快速生成类似结构
- [[../../03-前端开发/前端总览|前端 bizCategory 页面]]
