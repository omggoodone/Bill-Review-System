-- ============================================
-- 迁移: 10_seed_category_menus.sql
-- 说明: 业务类别模块菜单 + 普通用户角色授权
-- ============================================

-- 1. 业务类别菜单
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES
(3007, '业务类别', 3000, 3, 'bizCategory', 'biz/bizCategory/index', '', '', 1, 0, 'C', '0', '0', 'biz:category:list', 'category', 'admin', NOW(), '', NULL, '业务类别管理'),
(3008, '类别查询', 3007, 1, '', '', '', '', 1, 0, 'F', '0', '0', 'biz:category:query', '#', 'admin', NOW(), '', NULL, ''),
(3009, '类别新增', 3007, 2, '', '', '', '', 1, 0, 'F', '0', '0', 'biz:category:add', '#', 'admin', NOW(), '', NULL, ''),
(3010, '类别编辑', 3007, 3, '', '', '', '', 1, 0, 'F', '0', '0', 'biz:category:edit', '#', 'admin', NOW(), '', NULL, ''),
(3011, '类别删除', 3007, 4, '', '', '', '', 1, 0, 'F', '0', '0', 'biz:category:remove', '#', 'admin', NOW(), '', NULL, '')
ON DUPLICATE KEY UPDATE menu_name = VALUES(menu_name);

-- 2. 给普通用户 (role_id=3) 分配类别查询按钮权限（不分配页面菜单3007）
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES
(3, 3008);
