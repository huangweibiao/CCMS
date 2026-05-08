package com.ccms.controller.system;

import com.ccms.controller.ControllerTestBase;
import com.ccms.entity.system.user.User;
import com.ccms.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * UserController 单元测试类
 * 测试用户管理相关接口
 */
@WebMvcTest(UserController.class)
public class UserControllerTest extends ControllerTestBase {

    @MockBean
    private UserService userService;

    /**
     * given: 存在用户数据
     * when: 调用分页查询用户列表接口
     * then: 返回分页用户数据
     */
    @Test
    @DisplayName("分页查询用户列表 - 成功")
    public void testGetUserList_Success() throws Exception {
        // given
        User user1 = createTestUser(1L, "zhangsan", "张三", "zhangsan@ccms.com");
        User user2 = createTestUser(2L, "lisi", "李四", "lisi@ccms.com");
        List<User> userList = Arrays.asList(user1, user2);
        Page<User> userPage = new PageImpl<>(userList, PageRequest.of(0, 10), 2);

        when(userService.getUserList(0, 10, null, null)).thenReturn(userPage);

        // when & then
        performGet("/api/system/users")
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[0].id", is(1)))
                .andExpect(jsonPath("$.content[0].username", is("zhangsan")))
                .andExpect(jsonPath("$.content[0].name", is("张三")))
                .andExpect(jsonPath("$.content[1].id", is(2)))
                .andExpect(jsonPath("$.content[1].username", is("lisi")))
                .andExpect(jsonPath("$.totalElements", is(2)));
    }

    /**
     * given: 指定用户名和部门ID
     * when: 调用分页查询用户列表接口带参数
     * then: 返回符合条件的分页用户数据
     */
    @Test
    @DisplayName("分页查询用户列表 - 带查询参数")
    public void testGetUserList_WithParams() throws Exception {
        // given
        User user = createTestUser(1L, "zhangsan", "张三", "zhangsan@ccms.com");
        List<User> userList = Collections.singletonList(user);
        Page<User> userPage = new PageImpl<>(userList, PageRequest.of(0, 10), 1);

        when(userService.getUserList(0, 10, "zhangsan", 1L)).thenReturn(userPage);

        // when & then
        performGet("/api/system/users?page=0&size=10&username=zhangsan&deptId=1")
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].username", is("zhangsan")));
    }

    /**
     * given: 存在指定ID的用户
     * when: 调用根据ID获取用户信息接口
     * then: 返回用户详细信息
     */
    @Test
    @DisplayName("根据ID获取用户信息 - 成功")
    public void testGetUserById_Success() throws Exception {
        // given
        User user = createTestUser(1L, "zhangsan", "张三", "zhangsan@ccms.com");

        when(userService.getUserById(1L)).thenReturn(user);

        // when & then
        performGet("/api/system/users/{userId}", 1L)
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.username", is("zhangsan")))
                .andExpect(jsonPath("$.name", is("张三")))
                .andExpect(jsonPath("$.email", is("zhangsan@ccms.com")));
    }

    /**
     * given: 不存在指定ID的用户
     * when: 调用根据ID获取用户信息接口
     * then: 返回404状态码
     */
    @Test
    @DisplayName("根据ID获取用户信息 - 用户不存在")
    public void testGetUserById_NotFound() throws Exception {
        // given
        when(userService.getUserById(999L)).thenReturn(null);

        // when & then
        performGet("/api/system/users/{userId}", 999L)
                .andExpect(status().isNotFound());
    }

    /**
     * given: 存在指定用户名的用户
     * when: 调用根据用户名获取用户信息接口
     * then: 返回用户详细信息
     */
    @Test
    @DisplayName("根据用户名获取用户信息 - 成功")
    public void testGetUserByUsername_Success() throws Exception {
        // given
        User user = createTestUser(1L, "zhangsan", "张三", "zhangsan@ccms.com");

        when(userService.loadUserByUsername("zhangsan")).thenReturn(user);

        // when & then
        performGet("/api/system/users/username/{username}", "zhangsan")
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.username", is("zhangsan")))
                .andExpect(jsonPath("$.name", is("张三")));
    }

    /**
     * given: 不存在指定用户名的用户
     * when: 调用根据用户名获取用户信息接口
     * then: 返回404状态码
     */
    @Test
    @DisplayName("根据用户名获取用户信息 - 用户不存在")
    public void testGetUserByUsername_NotFound() throws Exception {
        // given
        when(userService.loadUserByUsername("notexist")).thenReturn(null);

        // when & then
        performGet("/api/system/users/username/{username}", "notexist")
                .andExpect(status().isNotFound());
    }

    /**
     * given: 提供有效的用户信息
     * when: 调用创建用户接口
     * then: 返回创建成功的用户信息
     */
    @Test
    @DisplayName("创建用户 - 成功")
    public void testCreateUser_Success() throws Exception {
        // given
        User user = createTestUser(null, "newuser", "新用户", "newuser@ccms.com");
        User createdUser = createTestUser(1L, "newuser", "新用户", "newuser@ccms.com");

        when(userService.createUser(org.mockito.ArgumentMatchers.any(User.class))).thenReturn(createdUser);

        // when & then
        performPost("/api/system/users", user)
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.username", is("newuser")))
                .andExpect(jsonPath("$.name", is("新用户")))
                .andExpect(jsonPath("$.email", is("newuser@ccms.com")));
    }

    /**
     * given: 提供有效的用户更新信息
     * when: 调用更新用户接口
     * then: 返回更新后的用户信息
     */
    @Test
    @DisplayName("更新用户 - 成功")
    public void testUpdateUser_Success() throws Exception {
        // given
        User user = createTestUser(null, "zhangsan", "张三修改", "zhangsan@ccms.com");
        User updatedUser = createTestUser(1L, "zhangsan", "张三修改", "zhangsan@ccms.com");

        when(userService.updateUser(org.mockito.ArgumentMatchers.any(User.class))).thenReturn(updatedUser);

        // when & then
        performPut("/api/system/users/1", user)
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("张三修改")));
    }

    /**
     * given: 存在指定ID的用户
     * when: 调用删除用户接口
     * then: 返回200状态码
     */
    @Test
    @DisplayName("删除用户 - 成功")
    public void testDeleteUser_Success() throws Exception {
        // given
        when(userService.deleteUser(1L)).thenReturn(true);

        // when & then
        performDelete("/api/system/users/{userId}", 1L)
                .andExpect(status().isOk());
    }

    /**
     * given: 删除用户失败
     * when: 调用删除用户接口
     * then: 返回400状态码
     */
    @Test
    @DisplayName("删除用户 - 失败")
    public void testDeleteUser_Failure() throws Exception {
        // given
        when(userService.deleteUser(1L)).thenReturn(false);

        // when & then
        performDelete("/api/system/users/{userId}", 1L)
                .andExpect(status().isBadRequest());
    }

    /**
     * given: 存在指定ID的用户
     * when: 调用更新用户状态接口（启用/禁用）
     * then: 返回200状态码
     */
    @Test
    @DisplayName("更新用户状态 - 成功")
    public void testUpdateUserStatus_Success() throws Exception {
        // given
        when(userService.updateUserStatus(1L, 0)).thenReturn(true);

        // when & then
        performPut("/api/system/users/{userId}/status?status=0", 1L)
                .andExpect(status().isOk());
    }

    /**
     * given: 更新用户状态失败
     * when: 调用更新用户状态接口
     * then: 返回400状态码
     */
    @Test
    @DisplayName("更新用户状态 - 失败")
    public void testUpdateUserStatus_Failure() throws Exception {
        // given
        when(userService.updateUserStatus(1L, 0)).thenReturn(false);

        // when & then
        performPut("/api/system/users/{userId}/status?status=0", 1L)
                .andExpect(status().isBadRequest());
    }

    /**
     * given: 存在指定ID的用户
     * when: 调用重置密码接口
     * then: 返回新密码
     */
    @Test
    @DisplayName("重置用户密码 - 成功")
    public void testResetPassword_Success() throws Exception {
        // given
        String newPassword = "newPassword123";
        when(userService.resetPassword(1L)).thenReturn(newPassword);

        // when & then
        performPost("/api/system/users/{userId}/reset-password", 1L)
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.newPassword", is(newPassword)));
    }

    /**
     * given: 存在指定ID的用户和角色列表
     * when: 调用分配用户角色接口
     * then: 返回200状态码
     */
    @Test
    @DisplayName("分配用户角色 - 成功")
    public void testAssignUserRoles_Success() throws Exception {
        // given
        Long[] roleIds = {1L, 2L, 3L};
        when(userService.assignUserRoles(1L, roleIds)).thenReturn(true);

        // when & then
        performPost("/api/system/users/1/roles", roleIds)
                .andExpect(status().isOk());
    }

    /**
     * given: 分配用户角色失败
     * when: 调用分配用户角色接口
     * then: 返回400状态码
     */
    @Test
    @DisplayName("分配用户角色 - 失败")
    public void testAssignUserRoles_Failure() throws Exception {
        // given
        Long[] roleIds = {1L, 2L};
        when(userService.assignUserRoles(1L, roleIds)).thenReturn(false);

        // when & then
        performPost("/api/system/users/1/roles", roleIds)
                .andExpect(status().isBadRequest());
    }

    /**
     * given: 存在指定ID的用户
     * when: 调用获取用户角色列表接口
     * then: 返回角色列表
     */
    @Test
    @DisplayName("获取用户角色列表 - 成功")
    public void testGetUserRoles_Success() throws Exception {
        // given
        List<String> roles = Arrays.asList("ADMIN", "USER", "MANAGER");
        when(userService.getUserRoles(1L)).thenReturn(roles);

        // when & then
        performGet("/api/system/users/{userId}/roles", 1L)
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0]", is("ADMIN")))
                .andExpect(jsonPath("$[1]", is("USER")))
                .andExpect(jsonPath("$[2]", is("MANAGER")));
    }

    /**
     * given: 用户名已存在
     * when: 调用检查用户名是否存在接口
     * then: 返回exists=true
     */
    @Test
    @DisplayName("检查用户名是否存在 - 已存在")
    public void testCheckUsernameExists_Exists() throws Exception {
        // given
        when(userService.isUsernameExist("zhangsan")).thenReturn(true);

        // when & then
        performGet("/api/system/users/check-username?username=zhangsan")
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.exists", is(true)));
    }

    /**
     * given: 用户名不存在
     * when: 调用检查用户名是否存在接口
     * then: 返回exists=false
     */
    @Test
    @DisplayName("检查用户名是否存在 - 不存在")
    public void testCheckUsernameExists_NotExists() throws Exception {
        // given
        when(userService.isUsernameExist("newuser")).thenReturn(false);

        // when & then
        performGet("/api/system/users/check-username?username=newuser")
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.exists", is(false)));
    }

    /**
     * given: 存在用户统计数据
     * when: 调用获取用户统计信息接口
     * then: 返回统计数据
     */
    @Test
    @DisplayName("获取用户统计信息 - 成功")
    public void testGetUserStatistics_Success() throws Exception {
        // given
        Map<String, Object> statistics = new HashMap<>();
        statistics.put("totalUsers", 100);
        statistics.put("activeUsers", 80);
        statistics.put("disabledUsers", 20);

        when(userService.getUserStatistics(null)).thenReturn(statistics);

        // when & then
        performGet("/api/system/users/statistics")
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.totalUsers", is(100)))
                .andExpect(jsonPath("$.activeUsers", is(80)))
                .andExpect(jsonPath("$.disabledUsers", is(20)));
    }

    /**
     * given: 指定部门ID
     * when: 调用获取用户统计信息接口带部门参数
     * then: 返回指定部门的统计数据
     */
    @Test
    @DisplayName("获取用户统计信息 - 带部门参数")
    public void testGetUserStatistics_WithDeptId() throws Exception {
        // given
        Map<String, Object> statistics = new HashMap<>();
        statistics.put("totalUsers", 50);
        statistics.put("activeUsers", 40);
        statistics.put("disabledUsers", 10);

        when(userService.getUserStatistics(1L)).thenReturn(statistics);

        // when & then
        performGet("/api/system/users/statistics?deptId=1")
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.totalUsers", is(50)));
    }

    /**
     * 创建测试用户对象
     */
    private User createTestUser(Long id, String username, String name, String email) {
        User user = new User();
        user.setId(id);
        user.setUsername(username);
        user.setName(name);
        user.setEmail(email);
        user.setPassword("password123");
        user.setEnabled(true);
        return user;
    }
}
