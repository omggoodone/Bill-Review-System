# zsc-module — 票据审核核心业务

票据审核系统的核心业务模块，包含票据、审核、注册、类别、邮件五大部分。

## 特点（与系统模块的区别）

- **独立的响应体系**：`ResultVo<T>` / `PageResult` / `BasePageReq`（`common/response/` `common/pagination/`）
- **DTO/VO 分离**：`domain/dto/`（入参）+ `domain/vo/`（出参）+ `domain/entity/`（数据库映射）
- **纯 MyBatis-Plus**：无 XML 文件，全用 `lambdaQuery()` 链式查询
- **SpringDoc**：Controller 用 `@Tag` / `@Operation` 注解
- **RESTful**：分页用 `POST /query`，其余 `GET/POST/PUT/DELETE`

## 包结构

```
controller/
  BizBillController       ← 票据 CRUD + 审核 + 批量审核 + 统计（/api/bill）
  BizCategoryController   ← 类别管理（/api/category）
  BizRegisterRequestController ← 注册申请（/api/register-request）
config/
  MailConfig.java         ← 163 SMTP 邮件配置
domain/
  entity/                 ← BizBill, BizBillFile, BizAuditLog, BizCategory, BizRegisterRequest
  dto/                    ← BizBillDto, BizBillReviewDto, BizCategoryDto, BizRegisterRequestDto
  dto/query/              ← BizBillQueryDto（支持排序 sortField/sortOrder）
  vo/                     ← BizBillVo, BizBillDetailVo, BizBillFileVo, BizAuditLogVo,
                             BizCategoryVo, BizRegisterRequestVo,
                             ReviewerWorkloadVo, UserAmountVo, TrendItemVo, ReviewerStatsVo
mapper/                   ← BizBillMapper, BizBillFileMapper, BizAuditLogMapper, BizCategoryMapper, BizRegisterRequestMapper
service/
  BizBillService + impl   ← 票据 CRUD + 提交 + 删除 + 审核 + 统计
  BizCategoryService + impl ← 类别 CRUD + 删除保护（"其他"不可删，票据自动迁移）
  BizRegisterRequestService + impl ← 注册申请 + 审批 + 自动生成用户名密码
  EmailService            ← @Async 邮件发送（创建/停用/删除通知）
common/                   ← ResultVo, PageResult, GlobalCodeEnum, ServiceException, BaseEnum, EnumUtil
```

## 关键约定

- 权限前缀：`biz:bill:*` / `biz:category:*` / `biz:admin:list`
- 票据状态：0-草稿 1-已提交 2-已通过 3-已退回
- 审核意见：1-通过 2-退回
- ServiceImpl 继承 MyBatis-Plus `ServiceImpl<M, T>`
- Dto 用 Lombok `@Data @Builder @NoArgsConstructor @AllArgsConstructor`
- `ResultVo` 和系统的 `AjaxResult` 是两套体系，别混用
- 类别删除：删前将关联票据 category_id 迁移到"其他"
- 数据过滤：超管看全量，审核员按 auditBy，用户按 createBy
