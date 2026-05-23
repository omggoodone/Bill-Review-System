-- ============================================
-- 迁移: 05_seed_bill_dict.sql
-- 说明: 票据模块完整种子数据（角色 + 菜单 + 字典）
-- ============================================

-- 1. 票据状态字典类型
INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, update_by, update_time, remark)
VALUES ('票据状态', 'biz_bill_status', '0', 'admin', NOW(), 'admin', NOW(), '票据状态字典')
ON DUPLICATE KEY UPDATE update_time = NOW();

-- 2. 票据状态字典数据
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES
(0, '草稿',   '0', 'biz_bill_status', 'info',    'info',    'N', '0', 'admin', NOW(), 'admin', NOW(), '票据状态: 草稿'),
(1, '已提交', '1', 'biz_bill_status', 'warning',  'warning',  'N', '0', 'admin', NOW(), 'admin', NOW(), '票据状态: 已提交, 待审核'),
(2, '已通过', '2', 'biz_bill_status', 'success',  'success',  'N', '0', 'admin', NOW(), 'admin', NOW(), '票据状态: 审核通过'),
(3, '已退回', '3', 'biz_bill_status', 'danger',   'danger',   'N', '0', 'admin', NOW(), 'admin', NOW(), '票据状态: 审核退回')
ON DUPLICATE KEY UPDATE update_time = NOW();

-- 3. 普通用户角色
INSERT INTO `sys_role` (`role_id`, `role_name`, `role_key`, `role_sort`, `data_scope`, `menu_check_strictly`, `dept_check_strictly`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`)
VALUES (3, '普通用户', 'user', 3, '5', 1, 1, '0', '0', 'admin', NOW(), '', NULL, '票据系统普通用户')
ON DUPLICATE KEY UPDATE role_name = '普通用户';

-- 4. 票据菜单
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES
(3000, '票据管理', 0, 4, 'bill', NULL, '', '', 1, 0, 'M', '0', '0', '', 'bill', 'admin', NOW(), '', NULL, '票据管理目录'),
(3001, '我的票据', 3000, 1, 'myBill', 'biz/bill/index', '', '', 1, 0, 'C', '0', '0', 'biz:bill:list', 'documentation', 'admin', NOW(), '', NULL, '我的票据菜单'),
(3002, '票据审核', 3000, 2, 'review', 'biz/bill/review/index', '', '', 1, 0, 'C', '0', '0', 'biz:bill:list', 'edit', 'admin', NOW(), '', NULL, '票据审核菜单'),
(3003, '票据查询', 3001, 1, '', '', '', '', 1, 0, 'F', '0', '0', 'biz:bill:query', '#', 'admin', NOW(), '', NULL, ''),
(3004, '票据新增', 3001, 2, '', '', '', '', 1, 0, 'F', '0', '0', 'biz:bill:add', '#', 'admin', NOW(), '', NULL, ''),
(3005, '票据删除', 3001, 3, '', '', '', '', 1, 0, 'F', '0', '0', 'biz:bill:remove', '#', 'admin', NOW(), '', NULL, ''),
(3006, '票据审核', 3002, 1, '', '', '', '', 1, 0, 'F', '0', '0', 'biz:bill:review', '#', 'admin', NOW(), '', NULL, '')
ON DUPLICATE KEY UPDATE menu_name = VALUES(menu_name);

-- 5. 角色-菜单分配: 普通用户 (role_id=3) 只能查看、新增、删除，不能审核
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES
(3, 3000), (3, 3001), (3, 3003), (3, 3004), (3, 3005);
-- 管理员 (role_id=1) 框架自动放行所有权限，无需显式分配
