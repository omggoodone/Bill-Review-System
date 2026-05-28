# 数据库脚本

## 当前方案

单文件初始化：`init-system.sql` — 包含全部建表语句（35张表）+ 种子数据（角色/字典/菜单/票据类别/系统配置），不含用户数据。

## 使用方式

```bash
mysql -u root -p -e "CREATE DATABASE IF NOT EXISTS \`zsc-train\` DEFAULT CHARSET utf8mb4"
mysql -u root -p zsc-train < init-system.sql
```

导入后访问系统 → Web 初始化表单创建超管 → 开始使用。

## 种子数据内容

| 类型 | 数量 | 说明 |
|------|------|------|
| 角色 | 4 | admin / user / reviewer / admin_user |
| 字典 | 9 | 性别 / 系统开关 / 票据状态 |
| 菜单 | 22 | 票据管理 + 系统管理 + 操作按钮 |
| 角色菜单 | 28 | 各角色对应权限 |
| 类别 | 4 | 办公用品 / 差旅交通 / 招待餐饮 / 其他 |
| 配置 | 6 | 验证码 / 注册开关 / 皮肤 / 初始密码 |

## 注意事项

- 重新导入会 DROP + CREATE 所有表，数据丢失
- 备份：`mysqldump -u root -p zsc-train > backup.sql`
- 邮件配置在 `application-druid.yml` 的 `mail.*` 下
