-- 审批流程测试数据初始化脚本
-- 本脚本为审批流程模块提供完整的测试场景数据

-- 1. 清理现有测试数据
DELETE FROM ccms_approval_record WHERE instance_id IN (SELECT id FROM ccms_approval_instance WHERE business_id LIKE 'TEST%');
DELETE FROM ccms_approval_instance WHERE business_id LIKE 'TEST%';
DELETE FROM ccms_approval_node WHERE config_id IN (SELECT id FROM ccms_approval_flow_config WHERE flow_name LIKE '测试流程%');
DELETE FROM ccms_approval_flow_config WHERE flow_name LIKE '测试流程%';

-- 2. 创建测试流程配置
INSERT INTO ccms_approval_flow_config (id, business_type, flow_name, description, min_amount, max_amount, enable_condition, is_active, version, created_by, created_time, updated_by, updated_time) VALUES
-- 测试流程1: 简单流程（单级审批）
(10001, 'EXPENSE_REIMBURSE', '测试流程-简单报销', '测试用简单报销流程，金额0-1000元', 0.00, 1000.00, 0, 1, 1, 'system', NOW(), 'system', NOW()),
-- 测试流程2: 中等复杂流程（两级审批） 
(10002, 'EXPENSE_REIMBURSE', '测试流程-中等报销', '测试用中等报销流程，金额1001-5000元', 1001.00, 5000.00, 0, 1, 1, 'system', NOW(), 'system', NOW()),
-- 测试流程3: 复杂流程（三级审批）
(10003, 'EXPENSE_REIMBURSE', '测试流程-复杂报销', '测试用复杂报销流程，金额5001-10000元', 5001.00, 10000.00, 0, 1, 1, 'system', NOW(), 'system', NOW()),
-- 测试流程4: 借款申请流程
(10004, 'LOAN_APPLY', '测试流程-借款申请', '测试用借款申请流程，金额0-20000元', 0.00, 20000.00, 0, 1, 1, 'system', NOW(), 'system', NOW()),
-- 测试流程5: 专项审批流程（按部门）
(10005, 'SPECIAL_APPROVAL', '测试流程-专项审批', '测试用专项审批流程', 0.00, 999999.99, 1, 1, 1, 'system', NOW(), 'system', NOW());

-- 3. 创建测试审批节点
INSERT INTO ccms_approval_node (id, config_id, node_name, node_order, approver_type, approver_id, is_required, condition_expression, created_by, created_time, updated_by, updated_time) VALUES
-- 简单流程节点（单级）
(100001, 10001, '部门经理审批', 1, 'ROLE', 2, 1, NULL, 'system', NOW(), 'system', NOW()),
-- 中等流程节点（两级）
(100002, 10002, '部门经理审批', 1, 'ROLE', 2, 1, NULL, 'system', NOW(), 'system', NOW()),
(100003, 10002, '财务审批', 2, 'ROLE', 3, 1, NULL, 'system', NOW(), 'system', NOW()),
-- 复杂流程节点（三级）
(100004, 10003, '部门经理审批', 1, 'ROLE', 2, 1, NULL, 'system', NOW(), 'system', NOW()),
(100005, 10003, '财务审批', 2, 'ROLE', 3, 1, NULL, 'system', NOW(), 'system', NOW()),
(100006, 10003, '总经理审批', 3, 'ROLE', 4, 1, NULL, 'system', NOW(), 'system', NOW()),
-- 借款流程节点
(100007, 10004, '部门经理审批', 1, 'ROLE', 2, 1, NULL, 'system', NOW(), 'system', NOW()),
(100008, 10004, '财务主管审批', 2, 'ROLE', 5, 1, NULL, 'system', NOW(), 'system', NOW()),
-- 专项流程节点（带条件）
(100009, 10005, '部门初审', 1, 'USER', 1001, 1, 'amount > 10000', 'system', NOW(), 'system', NOW()),
(100010, 10005, '专家审批', 2, 'ROLE', 6, 0, 'amount > 50000', 'system', NOW(), 'system', NOW()),
(100011, 10005, '委员会审批', 3, 'ROLE', 7, 0, 'amount > 100000', 'system', NOW(), 'system', NOW());

-- 4. 创建测试审批实例
INSERT INTO ccms_approval_instance (id, business_type, business_id, applicant_id, title, amount, status, current_approver_id, current_node_name, config_id, create_time, update_time) VALUES
-- 场景1: 简单报销（待审批）
(200001, 'EXPENSE_REIMBURSE', 'TEST_SIMPLE_001', 1001, '测试简单报销申请-差旅费', 800.00, 'APPROVING', 1002, '部门经理审批', 10001, NOW(), NOW()),
-- 场景2: 简单报销（已批准）
(200002, 'EXPENSE_REIMBURSE', 'TEST_SIMPLE_002', 1002, '测试简单报销申请-办公用品', 350.00, 'APPROVED', NULL, NULL, 10001, NOW(), NOW()),
-- 场景3: 中等报销（多级审批中）
(200003, 'EXPENSE_REIMBURSE', 'TEST_MEDIUM_001', 1003, '测试中等报销申请-项目费用', 2800.00, 'APPROVING', 1003, '财务审批', 10002, NOW(), NOW()),
-- 场景4: 复杂报销（多级审批中）
(200004, 'EXPENSE_REIMBURSE', 'TEST_COMPLEX_001', 1004, '测试复杂报销申请-设备采购', 7500.00, 'APPROVING', 1004, '总经理审批', 10003, NOW(), NOW()),
-- 场景5: 借款申请（已驳回）
(200005, 'LOAN_APPLY', 'TEST_LOAN_001', 1001, '测试借款申请-项目备用金', 5000.00, 'REJECTED', NULL, NULL, 10004, NOW(), NOW()),
-- 场景6: 专项审批（已取消）
(200006, 'SPECIAL_APPROVAL', 'TEST_SPECIAL_001', 1002, '测试专项审批申请-特殊采购', 15000.00, 'CANCELED', NULL, NULL, 10005, NOW(), NOW()),
-- 场景7: 简单报销（多实例并发测试）
(200007, 'EXPENSE_REIMBURSE', 'TEST_CONCURRENT_001', 1001, '并发测试报销1', 450.00, 'APPROVING', 1002, '部门经理审批', 10001, NOW(), NOW()),
(200008, 'EXPENSE_REIMBURSE', 'TEST_CONCURRENT_002', 1001, '并发测试报销2', 550.00, 'APPROVING', 1002, '部门经理审批', 10001, NOW(), NOW()),
(200009, 'EXPENSE_REIMBURSE', 'TEST_CONCURRENT_003', 1001, '并发测试报销3', 650.00, 'APPROVING', 1002, '部门经理审批', 10001, NOW(), NOW());

-- 5. 创建测试审批记录
INSERT INTO ccms_approval_record (id, instance_id, approver_id, action, remarks, create_time) VALUES
-- 场景2的审批记录（已批准）
(300001, 200002, 1002, 'APPROVE', '申请合规，同意报销', NOW()),
-- 场景3的第一级审批记录
(300002, 200003, 1002, 'APPROVE', '部门审核通过，转财务审批', NOW()),
-- 场景4的前两级审批记录
(300003, 200004, 1002, 'APPROVE', '部门审核通过，转财务', NOW()),
(300004, 200004, 1003, 'APPROVE', '财务审核通过，转总经理', NOW()),
-- 场景5的驳回记录
(300005, 200005, 1002, 'REJECT', '借款理由不充分，请补充说明', NOW()),
-- 场景6的取消记录
(300006, 200006, 1001, 'CANCEL', '申请信息需要修改，取消审批', NOW()),
-- 场景7的审批记录
(300007, 200007, 1002, 'APPROVE', '同意并发测试报销1', NOW()),
(300008, 200008, 1002, 'REJECT', '并发测试报销2申请理由不清晰', NOW());

-- 6. 创建测试用户和角色关联数据（简化版）
INSERT INTO ccms_user (id, username, real_name, department_id, status) VALUES
(1001, 'zhangsan', '张三', 1, 'ACTIVE'),
(1002, 'lisi', '李四（部门经理）', 1, 'ACTIVE'),
(1003, 'wangwu', '王五（财务）', 2, 'ACTIVE'),
(1004, 'zhaoliu', '赵六（总经理）', 3, 'ACTIVE'),
(1005, 'qianqi', '钱七（财务主管）', 2, 'ACTIVE'),
(1006, 'sunba', '孙八（专家）', 4, 'ACTIVE'),
(1007, 'zhoujiu', '周九（委员会）', 5, 'ACTIVE');

INSERT INTO ccms_role (id, role_name, role_code, description) VALUES
(2, '部门经理', 'DEPT_MANAGER', '部门审批权限'),
(3, '财务人员', 'FINANCE_STAFF', '财务审批权限'),
(4, '总经理', 'GENERAL_MANAGER', '最终审批权限'),
(5, '财务主管', 'FINANCE_MANAGER', '财务主管审批权限'),
(6, '专家', 'EXPERT', '专业领域审批权限'),
(7, '委员会成员', 'COMMITTEE', '重大项目审批权限');

-- 7. 测试业务数据（关联的财务数据）
INSERT INTO ccms_expense_apply_main (id, apply_no, applicant_id, apply_amount, apply_reason, status, create_time) VALUES
('TEST_SIMPLE_001', 'TEST202401001', 1001, 800.00, '差旅费用报销测试', 'APPROVING', NOW()),
('TEST_SIMPLE_002', 'TEST202401002', 1002, 350.00, '办公用品采购测试', 'APPROVED', NOW()),
('TEST_MEDIUM_001', 'TEST202401003', 1003, 2800.00, '项目费用报销测试', 'APPROVING', NOW()),
('TEST_COMPLEX_001', 'TEST202401004', 1004, 7500.00, '设备采购报销测试', 'APPROVING', NOW()),
('TEST_LOAN_001', 'TEST202401005', 1001, 5000.00, '项目备用金借款测试', 'REJECTED', NOW()),
('TEST_SPECIAL_001', 'TEST202401006', 1002, 15000.00, '特殊采购审批测试', 'CANCELED', NOW());

-- 8. 创建审计日志测试数据（新添加表）
INSERT INTO ccms_approval_audit_log (id, instance_id, action_type, description, old_status, new_status, operator_id, ip_address, user_agent, create_time) VALUES
(400001, 200001, 'SUBMIT', '用户提交报销申请', 'DRAFT', 'APPROVING', 1001, '192.168.1.100', 'Mozilla/5.0 Chrome/120.0', NOW()),
(400002, 200002, 'SUBMIT', '用户提交办公用品申请', 'DRAFT', 'APPROVING', 1002, '192.168.1.101', 'Mozilla/5.0 Safari/537.36', NOW()),
(400003, 200002, 'APPROVE', '部门经理审批通过', 'APPROVING', 'APPROVED', 1002, '192.168.1.102', 'Mozilla/5.0 Firefox/121.0', NOW()),
(400004, 200005, 'REJECT', '借款申请被驳回', 'APPROVING', 'REJECTED', 1002, '192.168.1.103', 'Mozilla/5.0 Chrome/121.0', NOW()),
(400005, 200006, 'CANCEL', '用户取消专项审批', 'APPROVING', 'CANCELED', 1002, '192.168.1.104', 'Mozilla/5.0 Edge/120.0', NOW());

-- 9. 创建性能测试相关数据
INSERT INTO ccms_approval_instance (id, business_type, business_id, applicant_id, title, amount, status, config_id, create_time, update_time)
SELECT 
    300000 + n as id,
    'EXPENSE_REIMBURSE' as business_type,
    CONCAT('PERF_TEST_', LPAD(n, 5, '0')) as business_id,
    1001 + (n % 10) as applicant_id,
    CONCAT('性能测试申请-', n) as title,
    ROUND(RAND() * 1000, 2) as amount,
    CASE WHEN n % 5 = 0 THEN 'APPROVED' 
         WHEN n % 5 = 1 THEN 'REJECTED'
         WHEN n % 5 = 2 THEN 'CANCELED'
         ELSE 'APPROVING' END as status,
    10001 + (n % 3) as config_id,
    DATE_SUB(NOW(), INTERVAL n HOUR) as create_time,
    DATE_SUB(NOW(), INTERVAL n % 10 HOUR) as update_time
FROM (SELECT @row := @row + 1 as n FROM (SELECT 0 UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL SELECT 5) t1,
(SELECT 0 UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL SELECT 5) t2,
(SELECT 0 UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL SELECT 5) t3,
(SELECT 0 UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL SELECT 5) t4,
(SELECT 0 UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL SELECT 5) t5,
(SELECT @row := 0) r) numbers
WHERE n <= 1000;

-- 10. 创建复杂查询场景数据
INSERT INTO ccms_approval_instance (id, business_type, business_id, applicant_id, title, amount, status, config_id, create_time, update_time) VALUES
-- 批量操作测试数据
(400001, 'EXPENSE_REIMBURSE', 'BATCH_TEST_001', 1001, '批量测试-差旅费', 1200.00, 'APPROVING', 10002, NOW(), NOW()),
(400002, 'EXPENSE_REIMBURSE', 'BATCH_TEST_002', 1001, '批量测试-办公费', 800.00, 'APPROVING', 10001, NOW(), NOW()),
(400003, 'LOAN_APPLY', 'BATCH_TEST_003', 1001, '批量测试-借款', 3000.00, 'APPROVING', 10004, NOW(), NOW()),
(400004, 'EXPENSE_REIMBURSE', 'BATCH_TEST_004', 1002, '批量测试-物料', 2500.00, 'APPROVING', 10002, NOW(), NOW()),
(400005, 'SPECIAL_APPROVAL', 'BATCH_TEST_005', 1002, '批量测试-专项', 50000.00, 'APPROVING', 10005, NOW(), NOW());

-- 更新最新状态为已完成的测试数据
UPDATE ccms_approval_instance 
SET status = 'APPROVED', current_approver_id = NULL, current_node_name = NULL, update_time = NOW()
WHERE business_id IN ('TEST_SIMPLE_002');

-- 更新时间戳，确保测试数据时间分布
UPDATE ccms_approval_instance 
SET create_time = DATE_SUB(NOW(), INTERVAL FLOOR(RAND() * 30) DAY),
    update_time = DATE_SUB(NOW(), INTERVAL FLOOR(RAND() * 10) DAY)
WHERE create_time = update_time;

COMMIT;