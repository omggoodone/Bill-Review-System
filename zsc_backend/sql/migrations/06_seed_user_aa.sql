-- ============================================
-- 迁移: 06_seed_user_aa.sql
-- 说明: 创建票据测试用户 AA，分配普通用户角色 (role_id=3)
-- ============================================

INSERT INTO `sys_user` (`user_id`, `dept_id`, `user_name`, `nick_name`, `user_type`, `email`, `phonenumber`, `sex`, `avatar`, `password`, `status`, `del_flag`, `login_ip`, `login_date`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`)
VALUES (3, 103, 'AA', 'AA', '00', '', '', '0', '', '$2b$12$7ya9JHt8NAFZ39fEeEWP0uiXoUhaaX5z3T/P3vgywmZOoJl0VZb.6', '0', '0', '', NULL, 'admin', NOW(), '', NULL, '票据测试用户');

INSERT INTO `sys_user_role` (`user_id`, `role_id`) VALUES (3, 3);
