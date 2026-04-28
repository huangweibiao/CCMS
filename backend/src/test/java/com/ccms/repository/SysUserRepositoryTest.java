package com.ccms.repository;

import com.ccms.entity.SysUser;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
public class SysUserRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private SysUserRepository sysUserRepository;

    @Test
    public void testSaveAndFindById() {
        // 准备测试数据
        SysUser user = createTestUser("testuser", "张三", "test@example.com", "13800138000");
        
        // 保存用户
        SysUser savedUser = sysUserRepository.save(user);
        
        // 验证保存成功
        assertThat(savedUser.getId()).isNotNull();
        assertThat(savedUser.getUsername()).isEqualTo("testuser");
        
        // 根据ID查询
        Optional<SysUser> foundUser = sysUserRepository.findById(savedUser.getId());
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getUsername()).isEqualTo("testuser");
    }

    @Test
    public void testFindByUsername() {
        // 准备多个测试用户
        SysUser user1 = createTestUser("user1", "张三", "user1@example.com", "13800138001");
        SysUser user2 = createTestUser("user2", "李四", "user2@example.com", "13800138002");
        
        entityManager.persist(user1);
        entityManager.persist(user2);
        entityManager.flush();
        
        // 根据用户名查询
        Optional<SysUser> foundUser = sysUserRepository.findByUsername("user1");
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getName()).isEqualTo("张三");
    }

    @Test
    public void testFindByUserCode() {
        SysUser user = createTestUser("testuser", "张三", "test@example.com", "13800138000");
        user.setUserCode("EMP001");
        
        entityManager.persist(user);
        entityManager.flush();
        
        // 根据工号查询
        Optional<SysUser> foundUser = sysUserRepository.findByUserCode("EMP001");
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getUsername()).isEqualTo("testuser");
    }

    @Test
    public void testFindByEmail() {
        SysUser user = createTestUser("testuser", "张三", "test@example.com", "13800138000");
        
        entityManager.persist(user);
        entityManager.flush();
        
        // 根据邮箱查询
        Optional<SysUser> foundUser = sysUserRepository.findByEmail("test@example.com");
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getName()).isEqualTo("张三");
    }

    @Test
    public void testFindByPhone() {
        SysUser user = createTestUser("testuser", "张三", "test@example.com", "13800138000");
        
        entityManager.persist(user);
        entityManager.flush();
        
        // 根据手机号查询
        Optional<SysUser> foundUser = sysUserRepository.findByPhone("13800138000");
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getName()).isEqualTo("张三");
    }

    @Test
    public void testFindByDeptId() {
        // 创建多个部门用户
        SysUser user1 = createTestUser("user1", "张三", "user1@example.com", "13800138001");
        user1.setDeptId(1001L);
        
        SysUser user2 = createTestUser("user2", "李四", "user2@example.com", "13800138002");
        user2.setDeptId(1001L);
        
        SysUser user3 = createTestUser("user3", "王五", "user3@example.com", "13800138003");
        user3.setDeptId(1002L);
        
        entityManager.persist(user1);
        entityManager.persist(user2);
        entityManager.persist(user3);
        entityManager.flush();
        
        // 根据部门ID查询
        List<SysUser> deptUsers = sysUserRepository.findByDeptId(1001L);
        assertThat(deptUsers).hasSize(2);
        assertThat(deptUsers).extracting("username").containsExactlyInAnyOrder("user1", "user2");
    }

    @Test
    public void testExistsByUsername() {
        SysUser user = createTestUser("testuser", "张三", "test@example.com", "13800138000");
        
        entityManager.persist(user);
        entityManager.flush();
        
        // 验证用户名的存在性
        assertThat(sysUserRepository.existsByUsername("testuser")).isTrue();
        assertThat(sysUserRepository.existsByUsername("nonexistent")).isFalse();
    }

    @Test
    public void testFindByUsernameOrNameContaining() {
        SysUser user1 = createTestUser("zhangsan", "张三", "zhangsan@example.com", "13800138001");
        SysUser user2 = createTestUser("lisi", "李四", "lisi@example.com", "13800138002");
        SysUser user3 = createTestUser("wangwu", "王五", "wangwu@example.com", "13800138003");
        
        entityManager.persist(user1);
        entityManager.persist(user2);
        entityManager.persist(user3);
        entityManager.flush();
        
        // 测试模糊搜索
        List<SysUser> results = sysUserRepository.findByUsernameOrNameContaining("zhang");
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getUsername()).isEqualTo("zhangsan");
    }

    @Test
    public void testFindByRoleId() {
        SysUser user1 = createTestUser("user1", "张三", "user1@example.com", "13800138001");
        user1.setRoleId(2001L);
        
        SysUser user2 = createTestUser("user2", "李四", "user2@example.com", "13800138002");
        user2.setRoleId(2001L);
        
        SysUser user3 = createTestUser("user3", "王五", "user3@example.com", "13800138003");
        user3.setRoleId(2002L);
        
        entityManager.persist(user1);
        entityManager.persist(user2);
        entityManager.persist(user3);
        entityManager.flush();
        
        // 根据角色ID查询
        List<SysUser> roleUsers = sysUserRepository.findByRoleId(2001L);
        assertThat(roleUsers).hasSize(2);
        assertThat(roleUsers).extracting("username").containsExactlyInAnyOrder("user1", "user2");
    }

    @Test
    public void testSoftDeleteFunctionality() {
        SysUser user = createTestUser("testuser", "张三", "test@example.com", "13800138000");
        
        SysUser savedUser = sysUserRepository.save(user);
        
        // 软删除用户
        sysUserRepository.softDelete(savedUser);
        entityManager.flush();
        
        // 验证软删除成功
        Optional<SysUser> deletedUser = sysUserRepository.findById(savedUser.getId());
        assertThat(deletedUser).isPresent();
        assertThat(deletedUser.get().getDelFlag()).isEqualTo(1);
        
        // 验证active查询不会返回已删除的用户
        Optional<SysUser> activeUser = sysUserRepository.findActiveById(savedUser.getId());
        assertThat(activeUser).isEmpty();
    }

    @Test
    public void testFindAllActive() {
        // 创建活跃和已删除的用户
        SysUser activeUser1 = createTestUser("active1", "张三", "active1@example.com", "13800138001");
        activeUser1.setDelFlag(0);
        
        SysUser activeUser2 = createTestUser("active2", "李四", "active2@example.com", "13800138002");
        activeUser2.setDelFlag(0);
        
        SysUser deletedUser = createTestUser("deleted", "王五", "deleted@example.com", "13800138003");
        deletedUser.setDelFlag(1);
        
        entityManager.persist(activeUser1);
        entityManager.persist(activeUser2);
        entityManager.persist(deletedUser);
        entityManager.flush();
        
        // 测试只返回活跃用户
        List<SysUser> activeUsers = sysUserRepository.findAllActive();
        assertThat(activeUsers).hasSize(2);
        assertThat(activeUsers).extracting("username").containsExactlyInAnyOrder("active1", "active2");
    }

    private SysUser createTestUser(String username, String name, String email, String phone) {
        SysUser user = new SysUser();
        user.setUsername(username);
        user.setPassword("encodedPassword");
        user.setName(name);
        user.setEmail(email);
        user.setPhone(phone);
        user.setStatus(1);
        user.setDelFlag(0);
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());
        return user;
    }
}