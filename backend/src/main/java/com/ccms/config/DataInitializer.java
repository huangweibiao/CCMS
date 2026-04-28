package com.ccms.config;

import com.ccms.entity.system.SysUser;
import com.ccms.entity.system.SysDept;
import com.ccms.entity.system.SysRole;
import com.ccms.entity.budget.BudgetCategory;
import com.ccms.entity.expense.ExpenseType;
import com.ccms.repository.system.SysUserRepository;
import com.ccms.repository.system.SysDeptRepository;
import com.ccms.repository.system.SysRoleRepository;
import com.ccms.repository.budget.BudgetCategoryRepository;
import com.ccms.repository.expense.ExpenseTypeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

/**
 * 数据初始化组件
 * 系统启动时自动初始化基础数据
 * 
 * @author 系统生成
 */
@Configuration
public class DataInitializer {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    @Autowired
    private SysUserRepository sysUserRepository;
    
    @Autowired
    private SysDeptRepository sysDeptRepository;
    
    @Autowired
    private SysRoleRepository sysRoleRepository;
    
    @Autowired
    private BudgetCategoryRepository budgetCategoryRepository;
    
    @Autowired
    private ExpenseTypeRepository expenseTypeRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Bean
    public CommandLineRunner initData() {
        return args -> {
            try {
                logger.info("开始初始化系统数据...");
                
                initDepartments();
                initRoles();
                initBudgetCategories();
                initExpenseTypes();
                initUsers();
                
                logger.info("系统数据初始化完成");
            } catch (Exception e) {
                logger.error("数据初始化失败", e);
            }
        };
    }

    @Transactional
    private void initDepartments() {
        if (sysDeptRepository.count() == 0) {
            logger.info("初始化部门数据...");
            
            List<SysDept> deptList = Arrays.asList(
                createDept("总公司", "1001", null, 1, "公司总部"),
                createDept("财务部", "100101", 1L, 2, "负责财务管理"),
                createDept("人事部", "100102", 1L, 3, "负责人力资源管理"),
                createDept("技术部", "100103", 1L, 4, "负责技术研发"),
                createDept("市场部", "100104", 1L, 5, "负责市场推广")
            );
            
            sysDeptRepository.saveAll(deptList);
            logger.info("已初始化 {} 个部门", deptList.size());
        }
    }

    @Transactional
    private void initRoles() {
        if (sysRoleRepository.count() == 0) {
            logger.info("初始化角色数据...");
            
            List<SysRole> roleList = Arrays.asList(
                createRole("超级管理员", "SUPER_ADMIN", "拥有系统所有权限", "系统管理"),
                createRole("管理员", "ADMIN", "拥有管理权限", "部门管理"),
                createRole("财务人员", "FINANCE", "财务相关权限", "财务管理"),
                createRole("部门经理", "DEPT_MANAGER", "部门管理权限", "部门审批"),
                createRole("普通员工", "EMPLOYEE", "员工基本权限", "日常使用")
            );
            
            sysRoleRepository.saveAll(roleList);
            logger.info("已初始化 {} 个角色", roleList.size());
        }
    }

    @Transactional
    private void initBudgetCategories() {
        if (budgetCategoryRepository.count() == 0) {
            logger.info("初始化预算分类数据...");
            
            List<BudgetCategory> categoryList = Arrays.asList(
                createBudgetCategory("办公用品", 1, "办公用品采购预算"),
                createBudgetCategory("差旅费用", 2, "员工差旅费用预算"),
                createBudgetCategory("培训费用", 3, "员工培训费用预算"),
                createBudgetCategory("设备采购", 4, "设备采购预算"),
                createBudgetCategory("市场推广", 5, "市场推广费用预算"),
                createBudgetCategory("软件采购", 6, "软件采购费用预算")
            );
            
            budgetCategoryRepository.saveAll(categoryList);
            logger.info("已初始化 {} 个预算分类", categoryList.size());
        }
    }

    @Transactional
    private void initExpenseTypes() {
        if (expenseTypeRepository.count() == 0) {
            logger.info("初始化费用类型数据...");
            
            List<ExpenseType> typeList = Arrays.asList(
                createExpenseType("差旅费", "TRAVEL", "差旅相关费用", 1),
                createExpenseType("交通费", "TRANSPORT", "交通相关费用", 2),
                createExpenseType("住宿费", "ACCOMMODATION", "住宿相关费用", 3),
                createExpenseType("餐饮费", "MEAL", "餐饮相关费用", 4),
                createExpenseType("办公用品", "OFFICE_SUPPLIES", "办公用品采购", 5),
                createExpenseType("培训费", "TRAINING", "培训相关费用", 6),
                createExpenseType("会议费", "MEETING", "会议相关费用", 7),
                createExpenseType("其他费用", "OTHER", "其他类型费用", 8)
            );
            
            expenseTypeRepository.saveAll(typeList);
            logger.info("已初始化 {} 个费用类型", typeList.size());
        }
    }

    @Transactional
    private void initUsers() {
        if (sysUserRepository.count() == 0) {
            logger.info("初始化用户数据...");
            
            // 获取部门
            SysDept adminDept = sysDeptRepository.findByName("财务部").orElse(null);
            
            if (adminDept != null) {
                // 创建管理员用户
                SysUser adminUser = new SysUser();
                adminUser.setUsername("admin");
                adminUser.setPassword(passwordEncoder.encode("admin123"));
                adminUser.setRealName("系统管理员");
                adminUser.setEmail("admin@ccms.com");
                adminUser.setPhone("13800138000");
                adminUser.setDeptId(adminDept.getId());
                adminUser.setStatus(1);
                adminUser.setCreateTime(LocalDateTime.now());
                adminUser.setUpdateTime(LocalDateTime.now());
                adminUser.setCreateUser(1L);
                adminUser.setUpdateUser(1L);
                
                sysUserRepository.save(adminUser);
                logger.info("已初始化管理员用户: {}", adminUser.getUsername());
            }
        }
    }

    private SysDept createDept(String name, String code, Long parentId, Integer orderNum, String description) {
        SysDept dept = new SysDept();
        dept.setDeptName(name);
        dept.setDeptCode(code);
        dept.setParentId(parentId);
        dept.setSortOrder(orderNum);
        dept.setDescription(description);
        dept.setStatus(1);
        dept.setCreateTime(LocalDateTime.now());
        dept.setUpdateTime(LocalDateTime.now());
        dept.setCreateUser(1L);
        dept.setUpdateUser(1L);
        return dept;
    }

    private SysRole createRole(String name, String code, String description, String category) {
        SysRole role = new SysRole();
        role.setRoleName(name);
        role.setRoleCode(code);
        role.setDescription(description);
        role.setCategory(category);
        role.setStatus(1);
        role.setCreateTime(LocalDateTime.now());
        role.setUpdateTime(LocalDateTime.now());
        role.setCreateUser(1L);
        role.setUpdateUser(1L);
        return role;
    }

    private BudgetCategory createBudgetCategory(String name, Integer orderNum, String description) {
        BudgetCategory category = new BudgetCategory();
        category.setCategoryName(name);
        category.setSortOrder(orderNum);
        category.setDescription(description);
        category.setEnabled(true);
        category.setCreateTime(LocalDateTime.now());
        category.setUpdateTime(LocalDateTime.now());
        category.setCreateUser(1L);
        category.setUpdateUser(1L);
        return category;
    }

    private ExpenseType createExpenseType(String name, String code, String description, Integer orderNum) {
        ExpenseType expenseType = new ExpenseType();
        expenseType.setTypeName(name);
        expenseType.setTypeCode(code);
        expenseType.setDescription(description);
        expenseType.setSortOrder(orderNum);
        expenseType.setEnabled(true);
        expenseType.setCreateTime(LocalDateTime.now());
        expenseType.setUpdateTime(LocalDateTime.now());
        expenseType.setCreateUser(1L);
        expenseType.setUpdateUser(1L);
        return expenseType;
    }
}