-- ============================================
-- 迁移: 09_reset_aa_password.sql
-- 说明: 重置 AA 用户密码为 123456
-- ============================================

UPDATE `sys_user`
SET `password` = '$2b$12$/pagm96wiX/5XyejjpMHOeQzIKA6UXEb/X9apO1dq7von0Ml4K8cq',
    `pwd_update_date` = NOW()
WHERE `user_name` = 'AA';
