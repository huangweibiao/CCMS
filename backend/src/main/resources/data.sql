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