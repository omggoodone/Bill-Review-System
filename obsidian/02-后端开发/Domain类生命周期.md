---
tags: [backend, domain]
---

# Domain 类生命周期

一张票据从创建到删除，DTO / Entity / VO 三类对象如何流转。

## 类角色

| 类型 | 方向 | 作用 |
|------|------|------|
| DTO | 入参 | 接收前端请求体，带 `@Valid` 校验 |
| Entity | 内部 | 映射数据库表，ORM 读写 |
| VO | 出参 | 返回前端，含关联数据（名称、嵌套列表） |

## 完整流程

### 1. 创建票据

```
前端表单 → BizBillDto → Controller → Entity → DB
```

```
BizBillDto (入参)              BizBill (Entity)
├─ title: "北京出差报销"        ├─ title
├─ amount: 3800.00             ├─ amount
├─ status: "0"                 ├─ status
└─ categoryId: 1               ├─ categoryId
                               ├─ createBy   ← 从 SecurityContext 取
                               └─ createTime ← new Date()
```

```java
BizBill bill = new BizBill();
BeanUtils.copyProperties(dto, bill);
bill.setCreateBy(SecurityUtils.getUsername());
bill.setCreateTime(new Date());
billMapper.insert(bill);
```

### 2. 列表查询

```
前端请求 → BizBillQueryDto → Service → BizBill (查库) → BizBillVo → 前端
```

```
BizBillQueryDto (入参)         BizBillVo (出参)
├─ status: "0"                 ├─ id, billNo, title, amount, status
├─ keywords: "北京"             ├─ categoryName  ← JOIN biz_category
├─ pageNum: 1, pageSize: 10    ├─ createTime
                               └─ auditBy, auditComment
```

```java
Page<BizBill> page = billMapper.selectPage(
    queryDto.convertToPage(),
    new LambdaQueryWrapper<BizBill>()
        .eq(StringUtils.hasText(dto.getStatus()), BizBill::getStatus, dto.getStatus())
        .eq(BizBill::getCreateBy, getUsername())  // 普通用户只查自己
);
// Entity 列表 → VO 列表
List<BizBillVo> voList = page.getRecords().stream()
    .map(bill -> BeanUtils.copyProperties(bill, BizBillVo.class))
    .collect(Collectors.toList());
```

### 3. 查看详情

```
Controller → Service → BizBill + BizBillFile + BizAuditLog (三表联查) → BizBillDetailVo
```

```
BizBillDetailVo (出参)
├─ id, billNo, title, amount, status
├─ categoryName        ← JOIN biz_category
├─ createBy, auditBy, auditComment
├─ files: [            ← 查 biz_bill_file
│   └─ BizBillFileVo { fileName, filePath, fileSize, fileType }
│  ]
└─ auditLogs: [        ← 查 biz_audit_log
    └─ BizAuditLogVo { action, comment, auditBy, auditTime }
   ]
```

### 4. 编辑票据

```
Controller.update(BizBillDto) → 校验 status IN ("0","3") → 更新 BizBill
```

编辑和创建共用 `BizBillDto`，区别在于 `id` 有值时走更新。更新时不会动 `status` 字段：

```java
BizBill bill = billMapper.selectById(dto.getId());
if (!List.of("0", "3").contains(bill.getStatus())) {
    throw new ServiceException("只能编辑草稿或已退回状态的票据");
}
// 逐字段赋值，status 不参与更新
if (StringUtils.isNotBlank(dto.getAttachment())) {
    bill.setAttachment(dto.getAttachment());
}
bill.setTitle(dto.getTitle());
bill.setCategoryId(dto.getCategoryId());
bill.setAmount(dto.getAmount());
bill.setDescription(dto.getDescription());
bill.setUpdateTime(new Date());
billMapper.updateById(bill);
```

### 5. 提交审核

```
Controller.submit(id) → 校验 status IN ("0","3") → 更新 status=1
```

不需要 DTO，直接操作 Entity：

```java
BizBill bill = billMapper.selectById(id);
bill.setStatus("1");
bill.setBillNo(generateBillNo());  // BILL-20260523-0001
billMapper.updateById(bill);
```

### 6. 审核

```
前端弹窗 → BizBillReviewDto → Controller.review() → 更新 BizBill + 新增 BizAuditLog
```

```
BizBillReviewDto (入参)
├─ billId: 1
├─ action: "1"       "1"=通过  "2"=退回
└─ comment: "同意报销"

       ↓
BizBill 更新:
  status ← "2" (通过) 或 "3" (退回)
  auditBy, auditTime, auditComment  ← 当前用户

BizAuditLog 新增:
  billId, action, comment, auditBy, auditTime
```

```java
// 更新票据
BizBill bill = billMapper.selectById(dto.getBillId());
bill.setStatus(dto.getAction().equals("1") ? "2" : "3");
bill.setAuditBy(SecurityUtils.getUsername());
bill.setAuditTime(new Date());
bill.setAuditComment(dto.getComment());
billMapper.updateById(bill);

// 记录审核历史
BizAuditLog log = BizAuditLog.builder()
    .billId(dto.getBillId())
    .action(dto.getAction())
    .comment(dto.getComment())
    .auditBy(SecurityUtils.getUsername())
    .auditTime(new Date())
    .build();
auditLogMapper.insert(log);
```

### 7. 删除草稿

```
Controller.delete(id) → 校验 status="0" → 删 BizBillFile → 删 BizBill
```

```java
BizBill bill = billMapper.selectById(id);
if (!"0".equals(bill.getStatus())) {
    throw new ServiceException("只能删除草稿状态的票据");
}
// 先删附件
fileMapper.delete(new LambdaQueryWrapper<BizBillFile>()
    .eq(BizBillFile::getBillId, id));
// 再删票据
billMapper.deleteById(id);
```

## 类总览

| 类                | 类型     | 方向  | 使用阶段                 |
| ---------------- | ------ | --- | -------------------- |
| BizBillDto       | DTO    | 入参  | 新增、编辑                |
| BizBillReviewDto | DTO    | 入参  | 审核                   |
| BizBillQueryDto  | DTO    | 入参  | 列表查询                 |
| BizBill          | Entity | 内部  | 全部（ORM 映射）           |
| BizBillFile      | Entity | 内部  | 附件读写                 |
| BizAuditLog      | Entity | 内部  | 审核记录写入               |
| BizBillVo        | VO     | 出参  | 列表项                  |
| BizBillDetailVo  | VO     | 出参  | 详情（含嵌套列表）            |
| BizBillFileVo    | VO     | 出参  | 附件信息（嵌套在 DetailVo 内） |
| BizAuditLogVo    | VO     | 出参  | 审核记录（嵌套在 DetailVo 内） |

## 数据流

```
前端 ──DTO──→ Controller ──Entity──→ Mapper ──→ DB
                                            ←── DB
前端 ←──VO─── Controller ←──Entity── Mapper
```

## 相关笔记

- [[模块说明/zsc-module|zsc-module]]
- [[../01-需求与设计/详细设计|详细设计]]
- [[../01-需求与设计/票据状态流程|票据状态流程]]
