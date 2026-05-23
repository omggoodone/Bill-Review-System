# 数据库脚本管理

## 目录结构

```
sql/
├── README.md              # 本文件
├── zsc.sql                # 基准文件：RuoYi 框架全部建表 + 种子数据 (mysqldump)
└── migrations/            # 增量迁移脚本（按序号执行）
    ├── 01_biz_category.sql
    ├── 02_add_bill_table.sql
    ├── 03_add_bill_file_table.sql
    ├── 04_add_audit_log_table.sql
    └── 05_seed_bill_dict.sql
```

## 两种使用场景

### 场景 A：新组员首次搭建

按顺序执行：

```bash
# 1. 创建数据库
mysql -u root -p -e "CREATE DATABASE IF NOT EXISTS \`zsc-train\` CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;"

# 2. 导入基准文件
mysql -u root -p zsc-train < sql/zsc.sql

# 3. 按序号依次导入所有迁移脚本
mysql -u root -p zsc-train < sql/migrations/01_biz_category.sql
mysql -u root -p zsc-train < sql/migrations/02_add_bill_table.sql
mysql -u root -p zsc-train < sql/migrations/03_add_bill_file_table.sql
mysql -u root -p zsc-train < sql/migrations/04_add_audit_log_table.sql
mysql -u root -p zsc-train < sql/migrations/05_seed_bill_dict.sql
```

### 场景 B：已有数据库，别人新增了表/字段

```bash
# 只执行你没跑过的迁移文件（看序号，找到上次你执行到哪了）
mysql -u root -p zsc-train < sql/migrations/02_xxx.sql
```

---

## 如何新增迁移脚本

当你要**添加表**、**修改字段**、**新增种子数据**时：

### 1. 创建新文件

在 `migrations/` 下新建文件，命名规则：**序号_描述.sql**

```
migrations/
├── 01_biz_category.sql
├── 02_add_bill_table.sql          # 新增票据表
├── 03_add_audit_comment_col.sql   # 给票据表加审核意见字段
└── 04_seed_bill_categories.sql    # 新增类别种子数据
```

### 2. 文件内容规范

```sql
-- ============================================
-- 迁移: 02_add_bill_table.sql
-- 说明: 新增票据表
-- 作者: 张三
-- 日期: 2026-05-23
-- ============================================

-- 建表
DROP TABLE IF EXISTS biz_bill;
CREATE TABLE biz_bill (
    id            bigint(20)    NOT NULL AUTO_INCREMENT COMMENT '主键',
    bill_no       varchar(32)   NOT NULL COMMENT '票据编号（自动生成，唯一）',
    title         varchar(255)  NOT NULL COMMENT '票据标题',
    category_id   bigint(20)    DEFAULT NULL COMMENT '票据类别ID',
    amount        decimal(10,2) NOT NULL COMMENT '金额',
    description   varchar(500)  DEFAULT NULL COMMENT '描述',
    status        char(1)       DEFAULT '0' COMMENT '状态: 0-草稿 1-已提交 2-已通过 3-已退回',
    attachment    varchar(500)  DEFAULT NULL COMMENT '附件路径（单文件，保留兼容）',
    create_by     varchar(64)   DEFAULT NULL COMMENT '创建者',
    create_time   datetime      DEFAULT NULL COMMENT '创建时间',
    update_by     varchar(64)   DEFAULT NULL COMMENT '更新者',
    update_time   datetime      DEFAULT NULL COMMENT '更新时间',
    audit_by      varchar(64)   DEFAULT NULL COMMENT '最近审核人',
    audit_time    datetime      DEFAULT NULL COMMENT '最近审核时间',
    audit_comment varchar(500)  DEFAULT NULL COMMENT '最近审核意见',
    remark        varchar(500)  DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (id),
    UNIQUE KEY uk_bill_no (bill_no),
    KEY idx_create_by (create_by),
    KEY idx_status (status),
    KEY idx_category_id (category_id),
    KEY idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='票据表';
```

如果是**修改已有表**（加字段）：

```sql
-- ============================================
-- 迁移: 03_add_xxx_col.sql
-- 说明: biz_bill 表新增 xxx 字段
-- ============================================

ALTER TABLE biz_bill ADD COLUMN new_field varchar(255) DEFAULT NULL COMMENT '新字段' AFTER amount;
```

### 3. 自己执行验证

```bash
mysql -u root -p zsc-train < sql/migrations/02_add_bill_table.sql
```

确认不报错。

### 4. 提交 + 通知组员

```bash
git add sql/migrations/02_add_bill_table.sql
git commit -m "feat: 新增票据表建表脚本"
git push
```

在群里发一句话：「新增了 `02_add_bill_table.sql`，拉代码后跑一下。」

---

## 注意事项

- **SQL 文件只能追加，禁止修改已提交的脚本** — 否则别人跑过的不一致
- **每个 DDL 语句前加 `DROP TABLE IF EXISTS` 或 `IF NOT EXISTS`** — 避免重复执行报错
- **脚本要求幂等** — 重复执行不会出错（用 `IF NOT EXISTS` / `IF EXISTS`）
- **不要修改 `zsc.sql`** — 那是框架导出的基准文件，自定义表放在 migrations 里
- **改表结构不要直接改旧文件** — 新建一个 migration，用 `ALTER TABLE`
