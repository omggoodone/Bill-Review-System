-- 11_bill_dashboard.sql
-- 票据概览仪表盘 + 隐藏首页

-- 调整子菜单排序，为仪表盘让出第1位
UPDATE `sys_menu` SET `order_num` = 2 WHERE `menu_id` = 3001;
UPDATE `sys_menu` SET `order_num` = 3 WHERE `menu_id` = 3002;
UPDATE `sys_menu` SET `order_num` = 4 WHERE `menu_id` = 3007;

-- 票据概览（3000 下第1个子菜单）
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES
(3012, '票据概览', 3000, 1, 'manage', 'biz/bill/manage/index', '', '', 1, 0, 'C', '0', '0', 'biz:bill:list', 'report', 'admin', NOW(), '', NULL, '票据管理仪表盘');

-- 授权：管理员(1/2) 和 普通用户(3)
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (1, 3012);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (2, 3012);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (3, 3012);

