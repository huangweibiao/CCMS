-- 费用类型管理模块初始化数据
-- 系统预设费用类型数据

-- 清空现有费用类型数据（仅在测试环境使用，生产环境需谨慎）
-- DELETE FROM fee_type;

-- 插入系统预设费用类型数据
-- 1. 费用大类（category=1）- 第一级分类
INSERT INTO fee_type (id, type_code, type_name, type_desc, category, budget_control_flag, invoice_require_flag, sort_no, status, is_system_preset, created_by, created_time, updated_by, updated_time, version) VALUES
(1, 'ADMIN_EXPENSE', '行政费用', '公司行政管理相关费用', 1, 1, 1, 1, 1, 1, 'system', NOW(), 'system', NOW(), 0),
(2, 'BUSINESS_EXPENSE', '业务费用', '公司业务运营相关费用', 1, 1, 1, 2, 1, 1, 'system', NOW(), 'system', NOW(), 0),
(3, 'TRAVEL_EXPENSE', '差旅费用', '员工出差相关费用', 1, 1, 1, 3, 1, 1, 'system', NOW(), 'system', NOW(), 0),
(4, 'EQUIPMENT_EXPENSE', '设备费用', '办公设备和资产相关费用', 1, 1, 1, 4, 1, 1, 'system', NOW(), 'system', NOW(), 0),
(5, 'OTHER_EXPENSE', '其他费用', '其他未分类的费用项目', 1, 0, 0, 99, 1, 1, 'system', NOW(), 'system', NOW(), 0);

-- 2. 具体费用类型（category=2）- 第二级分类
-- 行政费用子分类
INSERT INTO fee_type (id, type_code, type_name, type_desc, category, parent_id, budget_control_flag, invoice_require_flag, sort_no, status, is_system_preset, created_by, created_time, updated_by, updated_time, version) VALUES
(101, 'OFFICE_SUPPLIES', '办公用品', '文具、打印纸、墨盒等办公用品', 2, 1, 1, 1, 1, 1, 1, 'system', NOW(), 'system', NOW(), 0),
(102, 'COMMUNICATION', '通讯费用', '电话费、网络费、邮寄费等', 2, 1, 1, 1, 2, 1, 1, 'system', NOW(), 'system', NOW(), 0),
(103, 'UTILITIES', '水电费', '办公场所水电费', 2, 1, 1, 1, 3, 1, 1, 'system', NOW(), 'system', NOW(), 0),
(104, 'RENT', '房租', '办公场地租金', 2, 1, 1, 1, 4, 1, 1, 'system', NOW(), 'system', NOW(), 0),
(105, 'MAINTENANCE', '维修费', '办公设施维护维修费用', 2, 1, 1, 1, 5, 1, 1, 'system', NOW(), 'system', NOW(), 0);

-- 业务费用子分类
INSERT INTO fee_type (id, type_code, type_name, type_desc, category, parent_id, budget_control_flag, invoice_require_flag, sort_no, status, is_system_preset, created_by, created_time, updated_by, updated_time, version) VALUES
(201, 'MARKETING', '市场推广', '广告、宣传、营销活动费用', 2, 2, 1, 1, 1, 1, 1, 'system', NOW(), 'system', NOW(), 0),
(202, 'ENTERTAINMENT', '业务招待', '客户接待、商务宴请费用', 2, 2, 1, 1, 2, 1, 1, 'system', NOW(), 'system', NOW(), 0),
(203, 'TRAINING', '培训费', '员工培训、进修费用', 2, 2, 1, 1, 3, 1, 1, 'system', NOW(), 'system', NOW(), 0),
(204, 'CONFERENCE', '会议费', '外部会议、研讨会费用', 2, 2, 1, 1, 4, 1, 1, 'system', NOW(), 'system', NOW(), 0),
(205, 'SUBSCRIPTION', '订阅费', '专业期刊、软件订阅费用', 2, 2, 1, 1, 5, 1, 1, 'system', NOW(), 'system', NOW(), 0);

-- 差旅费用子分类
INSERT INTO fee_type (id, type_code, type_name, type_desc, category, parent_id, budget_control_flag, invoice_require_flag, sort_no, status, is_system_preset, created_by, created_time, updated_by, updated_time, version) VALUES
(301, 'TRANSPORTATION', '交通费', '机票、火车票、出租车等交通费用', 2, 3, 1, 1, 1, 1, 1, 'system', NOW(), 'system', NOW(), 0),
(302, 'ACCOMMODATION', '住宿费', '酒店住宿费用', 2, 3, 1, 1, 2, 1, 1, 'system', NOW(), 'system', NOW(), 0),
(303, 'MEAL_ALLOWANCE', '伙食补助', '出差期间的伙食补贴', 2, 3, 1, 0, 3, 1, 1, 'system', NOW(), 'system', NOW(), 0),
(304, 'TRAVEL_INSURANCE', '差旅保险', '差旅期间意外保险费用', 2, 3, 1, 1, 4, 1, 1, 'system', NOW(), 'system', NOW(), 0),
(305, 'OTHER_TRAVEL', '其他差旅费', '签证费、机场建设费等', 2, 3, 1, 1, 5, 1, 1, 'system', NOW(), 'system', NOW(), 0);

-- 设备费用子分类
INSERT INTO fee_type (id, type_code, type_name, type_desc, category, parent_id, budget_control_flag, invoice_require_flag, sort_no, status, is_system_preset, created_by, created_time, updated_by, updated_time, version) VALUES
(401, 'COMPUTER_EQUIP', '电脑设备', '电脑、服务器、网络设备', 2, 4, 1, 1, 1, 1, 1, 'system', NOW(), 'system', NOW(), 0),
(402, 'OFFICE_FURNITURE', '办公家具', '桌椅、文件柜等办公家具', 2, 4, 1, 1, 2, 1, 1, 'system', NOW(), 'system', NOW(), 0),
(403, 'SOFTWARE', '软件费用', '正版软件购买、许可费用', 2, 4, 1, 1, 3, 1, 1, 'system', NOW(), 'system', NOW(), 0),
(404, 'EQUIP_MAINTENANCE', '设备维护', '设备维修保养费用', 2, 4, 1, 1, 4, 1, 1, 'system', NOW(), 'system', NOW(), 0),
(405, 'EQUIPMENT_RENTAL', '设备租赁', '复印机、打印机等设备租赁', 2, 4, 1, 1, 5, 1, 1, 'system', NOW(), 'system', NOW(), 0);

-- 其他费用子分类
INSERT INTO fee_type (id, type_code, type_name, type_desc, category, parent_id, budget_control_flag, invoice_require_flag, sort_no, status, is_system_preset, created_by, created_time, updated_by, updated_time, version) VALUES
(501, 'DONATION', '捐赠支出', '慈善捐款、公益捐赠', 2, 5, 0, 1, 1, 1, 1, 'system', NOW(), 'system', NOW(), 0),
(502, 'LOSS_WRITEOFF', '损失核销', '固定资产损失核销', 2, 5, 0, 0, 2, 1, 1, 'system', NOW(), 'system', NOW(), 0),
(503, 'FINES', '罚款支出', '政府罚款、违约赔偿', 2, 5, 0, 0, 3, 1, 1, 'system', NOW(), 'system', NOW(), 0),
(504, 'MISC_EXPENSE', '杂项支出', '临时性、小额支出', 2, 5, 0, 0, 4, 1, 1, 'system', NOW(), 'system', NOW(), 0);

-- ====================================================================
-- 权限系统初始化数据 - RBAC权限管理模块
-- 创建系统角色、菜单权限、初始用户账号
-- ====================================================================

-- 1. 系统角色初始化数据
-- 清空现有角色数据（仅在测试/开发环境使用，生产环境需谨慎）
-- DELETE FROM roles;

-- 插入系统预设角色数据
INSERT INTO roles (id, role_code, role_name, role_desc, role_type, status, is_system_preset, created_by, created_time, updated_by, updated_time, version) VALUES
(1, 'SUPER_ADMIN', '超级管理员', '系统超级管理员，拥有全部权限', 1, 1, 1, 'system', NOW(), 'system', NOW(), 0),
(2, 'SYSTEM_ADMIN', '系统管理员', '系统管理员，拥有系统管理权限', 1, 1, 1, 'system', NOW(), 'system', NOW(), 0),
(3, 'FINANCE_ADMIN', '财务管理员', '财务管理权限，处理财务审核业务', 2, 1, 1, 'system', NOW(), 'system', NOW(), 0),
(4, 'DEPARTMENT_MANAGER', '部门经理', '部门管理权限，审核本部门费用', 2, 1, 1, 'system', NOW(), 'system', NOW(), 0),
(5, 'NORMAL_USER', '普通用户', '普通员工权限，提交费用报销', 3, 1, 1, 'system', NOW(), 'system', NOW(), 0);

-- 2. 系统菜单权限初始化数据
-- 清空现有菜单数据（仅在测试/开发环境使用，生产环境需谨慎）
-- DELETE FROM menus;

-- 插入系统预设菜单数据
INSERT INTO menus (id, menu_code, menu_name, icon, path, component, menu_type, parent_id, sort_no, status, is_visible, is_cache, perms, remark, created_by, created_time, updated_by, updated_time, version) VALUES
-- 一级菜单 - 系统管理
(1, 'SYSTEM_MANAGEMENT', '系统管理', 'System', '/system', 'Layout', 1, 0, 1, 1, 1, 0, 'system:management', '系统管理模块', 'system', NOW(), 'system', NOW(), 0),

-- 二级菜单 - 用户管理
(2, 'USER_MANAGEMENT', '用户管理', 'User', 'user', 'system/user/index', 2, 1, 1, 1, 1, 0, 'system:user:list', '用户管理功能', 'system', NOW(), 'system', NOW(), 0),

-- 二级菜单 - 角色管理
(3, 'ROLE_MANAGEMENT', '角色管理', 'Lock', 'role', 'system/role/index', 2, 1, 2, 1, 1, 0, 'system:role:list', '角色管理功能', 'system', NOW(), 'system', NOW(), 0),

-- 二级菜单 - 菜单管理
(4, 'MENU_MANAGEMENT', '菜单管理', 'Menu', 'menu', 'system/menu/index', 2, 1, 3, 1, 1, 0, 'system:menu:list', '菜单管理功能', 'system', NOW(), 'system', NOW(), 0),

-- 一级菜单 - 费用管理
(5, 'EXPENSE_MANAGEMENT', '费用管理', 'Money', '/expense', 'Layout', 1, 0, 2, 1, 1, 0, 'expense:management', '费用管理模块', 'system', NOW(), 'system', NOW(), 0),

-- 二级菜单 - 费用报销
(6, 'EXPENSE_APPLICATION', '费用报销', 'Document', 'application', 'expense/application/index', 2, 5, 1, 1, 1, 0, 'expense:application:list', '费用报销功能', 'system', NOW(), 'system', NOW(), 0),

-- 二级菜单 - 报销审核
(7, 'EXPENSE_APPROVAL', '报销审核', 'Checked', 'approval', 'expense/approval/index', 2, 5, 2, 1, 1, 0, 'expense:approval:list', '报销审核功能', 'system', NOW(), 'system', NOW(), 0),

-- 二级菜单 - 费用统计
(8, 'EXPENSE_STATISTICS', '费用统计', 'DataAnalysis', 'statistics', 'expense/statistics/index', 2, 5, 3, 1, 1, 0, 'expense:statistics:list', '费用统计功能', 'system', NOW(), 'system', NOW(), 0),

-- 一级菜单 - 审批中心
(9, 'APPROVAL_CENTER', '审批中心', 'DocumentChecked', '/approval', 'Layout', 1, 0, 3, 1, 1, 0, 'approval:center', '审批中心模块', 'system', NOW(), 'system', NOW(), 0),

-- 二级菜单 - 待我审批
(10, 'PENDING_APPROVAL', '待我审批', 'Clock', 'pending', 'approval/pending/index', 2, 9, 1, 1, 1, 0, 'approval:pending:list', '待审批列表', 'system', NOW(), 'system', NOW(), 0),

-- 二级菜单 - 已审批记录
(11, 'APPROVED_RECORDS', '已审批记录', 'Finished', 'approved', 'approval/approved/index', 2, 9, 2, 1, 1, 0, 'approval:approved:list', '已审批记录', 'system', NOW(), 'system', NOW(), 0);

-- 3. 角色菜单关联数据
-- 清空现有角色菜单关联数据
-- DELETE FROM role_menus;

-- 超级管理员拥有所有菜单权限
INSERT INTO role_menus (role_id, menu_id, created_by, created_time, updated_by, updated_time, version) VALUES
(1, 1, 'system', NOW(), 'system', NOW(), 0), (1, 2, 'system', NOW(), 'system', NOW(), 0), (1, 3, 'system', NOW(), 'system', NOW(), 0), (1, 4, 'system', NOW(), 'system', NOW(), 0),
(1, 5, 'system', NOW(), 'system', NOW(), 0), (1, 6, 'system', NOW(), 'system', NOW(), 0), (1, 7, 'system', NOW(), 'system', NOW(), 0), (1, 8, 'system', NOW(), 'system', NOW(), 0),
(1, 9, 'system', NOW(), 'system', NOW(), 0), (1, 10, 'system', NOW(), 'system', NOW(), 0), (1, 11, 'system', NOW(), 'system', NOW(), 0);

-- 系统管理员拥有系统管理和审批相关菜单权限
INSERT INTO role_menus (role_id, menu_id, created_by, created_time, updated_by, updated_time, version) VALUES
(2, 1, 'system', NOW(), 'system', NOW(), 0), (2, 2, 'system', NOW(), 'system', NOW(), 0), (2, 3, 'system', NOW(), 'system', NOW(), 0), (2, 4, 'system', NOW(), 'system', NOW(), 0),
(2, 9, 'system', NOW(), 'system', NOW(), 0), (2, 10, 'system', NOW(), 'system', NOW(), 0), (2, 11, 'system', NOW(), 'system', NOW(), 0);

-- 财务管理员拥有费用管理和审批相关菜单权限
INSERT INTO role_menus (role_id, menu_id, created_by, created_time, updated_by, updated_time, version) VALUES
(3, 5, 'system', NOW(), 'system', NOW(), 0), (3, 6, 'system', NOW(), 'system', NOW(), 0), (3, 7, 'system', NOW(), 'system', NOW(), 0), (3, 8, 'system', NOW(), 'system', NOW(), 0),
(3, 9, 'system', NOW(), 'system', NOW(), 0), (3, 10, 'system', NOW(), 'system', NOW(), 0), (3, 11, 'system', NOW(), 'system', NOW(), 0);

-- 部门经理拥有费用管理和部门审批相关菜单权限
INSERT INTO role_menus (role_id, menu_id, created_by, created_time, updated_by, updated_time, version) VALUES
(4, 5, 'system', NOW(), 'system', NOW(), 0), (4, 6, 'system', NOW(), 'system', NOW(), 0), (4, 7, 'system', NOW(), 'system', NOW(), 0),
(4, 9, 'system', NOW(), 'system', NOW(), 0), (4, 10, 'system', NOW(), 'system', NOW(), 0), (4, 11, 'system', NOW(), 'system', NOW(), 0);

-- 普通用户拥有费用报销相关菜单权限
INSERT INTO role_menus (role_id, menu_id, created_by, created_time, updated_by, updated_time, version) VALUES
(5, 6, 'system', NOW(), 'system', NOW(), 0), (5, 11, 'system', NOW(), 'system', NOW(), 0);

-- 4. 初始用户账号数据（默认密码为123456）
-- 清空现有用户数据（仅在测试/开发环境使用，生产环境需谨慎）
-- DELETE FROM users;

-- 插入系统预设用户数据（密码已加密：123456）
INSERT INTO users (id, username, password, nickname, email, phone, avatar, gender, status, is_super_admin, department_id, position, hire_date, remark, created_by, created_time, updated_by, updated_time, version) VALUES
(1, 'admin', '$2a$10$r6H8jqDn0d.9vKJ8wL7Dme9vQdZQ1X2yX3zY4V5W6r7S8T9U0v1w2', '超级管理员', 'admin@ccms.com', '13800138001', '', 1, 1, 1, 1, '系统管理员', NOW(), '系统超级管理员账号', 'system', NOW(), 'system', NOW(), 0),
(2, 'system', '$2a$10$r6H8jqDn0d.9vKJ8wL7Dme9vQdZQ1X2yX3zY4V5W6r7S8T9U0v1w2', '系统管理员', 'system@ccms.com', '13800138002', '', 1, 1, 0, 1, '系统管理员', NOW(), '系统管理员账号', 'system', NOW(), 'system', NOW(), 0),
(3, 'finance', '$2a$10$r6H8jqDn0d.9vKJ8wL7Dme9vQdZQ1X2yX3zY4V5W6r7S8T9U0v1w2', '财务管理员', 'finance@ccms.com', '13800138003', '', 1, 1, 0, 1, '财务经理', NOW(), '财务管理员账号', 'system', NOW(), 'system', NOW(), 0),
(4, 'manager', '$2a$10$r6H8jqDn0d.9vKJ8wL7Dme9vQdZQ1X2yX3zY4V5W6r7S8T9U0v1w2', '部门经理', 'manager@ccms.com', '13800138004', '', 1, 1, 0, 1, '部门经理', NOW(), '部门经理账号', 'system', NOW(), 'system', NOW(), 0),
(5, 'user', '$2a$10$r6H8jqDn0d.9vKJ8wL7Dme9vQdZQ1X2yX3zY4V5W6r7S8T9U0v1w2', '普通用户', 'user@ccms.com', '13800138005', '', 1, 1, 0, 1, '普通员工', NOW(), '普通用户账号', 'system', NOW(), 'system', NOW(), 0);

-- 5. 用户角色关联数据
-- 清空现有用户角色关联数据
-- DELETE FROM user_roles;

-- 为每个用户分配相应的角色
INSERT INTO user_roles (user_id, role_id, created_by, created_time, updated_by, updated_time, version) VALUES
(1, 1, 'system', NOW(), 'system', NOW(), 0),  -- 超级管理员分配超级管理员角色
(2, 2, 'system', NOW(), 'system', NOW(), 0),  -- 系统管理员分配系统管理员角色
(3, 3, 'system', NOW(), 'system', NOW(), 0),  -- 财务管理员分配财务管理员角色
(4, 4, 'system', NOW(), 'system', NOW(), 0),  -- 部门经理分配部门经理角色
(5, 5, 'system', NOW(), 'system', NOW(), 0);   -- 普通用户分配普通用户角色