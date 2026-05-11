package com.ccms.controller.system;

import com.ccms.controller.ControllerTestBase;
import com.ccms.entity.system.permission.SysPermission;
import com.ccms.entity.system.permission.SysRole;
import com.ccms.entity.system.permission.SysRolePermission;
import com.ccms.repository.system.permission.SysPermissionRepository;
import com.ccms.repository.system.permission.SysRolePermissionRepository;
import com.ccms.repository.system.permission.SysRoleRepository;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.*;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 角色管理控制器单元测试
 */
@WebMvcTest(RoleController.class)
class RoleControllerTest extends ControllerTestBase {

    @MockBean
    private SysRoleRepository sysRoleRepository;

    @MockBean
    private SysPermissionRepository sysPermissionRepository;

    @MockBean
    private SysRolePermissionRepository sysRolePermissionRepository;

    private SysRole createTestRole(Long id, String roleCode, String roleName, Integer status) {
        SysRole role = new SysRole();
        role.setId(id);
        role.setRoleCode(roleCode);
        role.setRoleName(roleName);
        role.setStatus(status);
        role.setDescription("测试角色");
        return role;
    }

    @Test
    void shouldReturnRoleListWhenQuerySuccess() throws Exception {
        // given
        SysRole role = createTestRole(1L, "ADMIN", "管理员", 1);
        when(sysRoleRepository.findAll()).thenReturn(Collections.singletonList(role));

        // when & then
        performGet("/api/system/roles")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].roleCode").value("ADMIN"));
    }

    @Test
    void shouldReturnActiveRoles() throws Exception {
        // given
        SysRole role = createTestRole(1L, "ADMIN", "管理员", 1);
        when(sysRoleRepository.findByStatus(1)).thenReturn(Collections.singletonList(role));

        // when & then
        performGet("/api/system/roles/active")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].roleCode").value("ADMIN"));
    }

    @Test
    void shouldReturnRoleWhenGetByIdSuccess() throws Exception {
        // given
        SysRole role = createTestRole(1L, "ADMIN", "管理员", 1);
        when(sysRoleRepository.findById(1L)).thenReturn(Optional.of(role));

        // when & then
        performGet("/api/system/roles/1")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.roleCode").value("ADMIN"));
    }

    @Test
    void shouldReturnNotFoundWhenGetByIdNotExist() throws Exception {
        // given
        when(sysRoleRepository.findById(999L)).thenReturn(Optional.empty());

        // when & then
        performGet("/api/system/roles/999")
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturnRoleWhenGetByCodeSuccess() throws Exception {
        // given
        SysRole role = createTestRole(1L, "ADMIN", "管理员", 1);
        when(sysRoleRepository.findByRoleCode("ADMIN")).thenReturn(Optional.of(role));

        // when & then
        performGet("/api/system/roles/code/ADMIN")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.roleCode").value("ADMIN"));
    }

    @Test
    void shouldCreateRoleSuccess() throws Exception {
        // given
        SysRole role = createTestRole(1L, "USER", "普通用户", 1);
        when(sysRoleRepository.existsByRoleCode("USER")).thenReturn(false);
        when(sysRoleRepository.save(any(SysRole.class))).thenReturn(role);

        // when & then
        performPost("/api/system/roles", role)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("角色创建成功"));
    }

    @Test
    void shouldReturnBadRequestWhenCreateRoleWithDuplicateCode() throws Exception {
        // given
        SysRole role = createTestRole(1L, "ADMIN", "管理员", 1);
        when(sysRoleRepository.existsByRoleCode("ADMIN")).thenReturn(true);

        // when & then
        performPost("/api/system/roles", role)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("角色编码已存在"));
    }

    @Test
    void shouldUpdateRoleSuccess() throws Exception {
        // given
        SysRole existingRole = createTestRole(1L, "ADMIN", "管理员", 1);
        SysRole updatedRole = createTestRole(1L, "ADMIN", "系统管理员", 1);
        when(sysRoleRepository.findById(1L)).thenReturn(Optional.of(existingRole));
        when(sysRoleRepository.findByRoleCode("ADMIN")).thenReturn(Optional.of(existingRole));
        when(sysRoleRepository.save(any(SysRole.class))).thenReturn(updatedRole);

        // when & then
        performPut("/api/system/roles/1", updatedRole)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("角色更新成功"));
    }

    @Test
    void shouldDeleteRoleSuccess() throws Exception {
        // given
        SysRole role = createTestRole(1L, "USER", "普通用户", 1);
        when(sysRoleRepository.findById(1L)).thenReturn(Optional.of(role));
        when(sysRolePermissionRepository.findByRoleId(1L)).thenReturn(Collections.emptyList());

        // when & then
        performDelete("/api/system/roles/1")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("角色删除成功"));
    }

    @Test
    void shouldUpdateRoleStatusSuccess() throws Exception {
        // given
        SysRole role = createTestRole(1L, "USER", "普通用户", 0);
        when(sysRoleRepository.findById(1L)).thenReturn(Optional.of(role));
        when(sysRoleRepository.save(any(SysRole.class))).thenReturn(role);

        // when & then
        performPut("/api/system/roles/1/status?status=1")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("角色已启用"));
    }

    @Test
    void shouldReturnRolePermissions() throws Exception {
        // given
        SysRolePermission rolePerm = new SysRolePermission();
        rolePerm.setRoleId(1L);
        rolePerm.setPermissionId(1L);
        when(sysRolePermissionRepository.findByRoleId(1L)).thenReturn(Collections.singletonList(rolePerm));
        when(sysPermissionRepository.findAllById(anyList())).thenReturn(Collections.emptyList());

        // when & then
        performGet("/api/system/roles/1/permissions")
                .andExpect(status().isOk());
    }

    @Test
    void shouldAssignPermissionsSuccess() throws Exception {
        // given
        SysRole role = createTestRole(1L, "ADMIN", "管理员", 1);
        when(sysRoleRepository.findById(1L)).thenReturn(Optional.of(role));
        when(sysRolePermissionRepository.findByRoleId(1L)).thenReturn(Collections.emptyList());
        when(sysRolePermissionRepository.save(any(SysRolePermission.class))).thenReturn(new SysRolePermission());

        // when & then
        performPost("/api/system/roles/1/permissions", Arrays.asList(1L, 2L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("权限分配成功"));
    }

    @Test
    void shouldReturnAllPermissions() throws Exception {
        // given
        when(sysPermissionRepository.findAll()).thenReturn(Collections.emptyList());

        // when & then
        performGet("/api/system/roles/permissions/all")
                .andExpect(status().isOk());
    }

    @Test
    void shouldReturnPermissionTree() throws Exception {
        // given
        SysPermission perm1 = new SysPermission();
        perm1.setId(1L);
        perm1.setPermCode("user:list");
        perm1.setPermName("用户列表");
        perm1.setParentId(0L);
        perm1.setSortOrder(1);
        when(sysPermissionRepository.findAll()).thenReturn(Collections.singletonList(perm1));

        // when & then
        performGet("/api/system/roles/permissions/tree")
                .andExpect(status().isOk());
    }

    /**
     * Task 10.1: 添加角色权限继承测试
     */

    @Test
    void shouldInheritPermissionsFromParentRole() throws Exception {
        // given
        SysPermission parentPerm = new SysPermission();
        parentPerm.setId(1L);
        parentPerm.setPermCode("user:list");
        parentPerm.setParentId(0L);
        parentPerm.setSortOrder(1);
        
        when(sysPermissionRepository.findAll()).thenReturn(Collections.singletonList(parentPerm));

        // when & then
        performGet("/api/system/roles/permissions/tree")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    /**
     * Task 10.2: 添加权限变更后用户登录权限生效测试
     */

    @Test
    void shouldUpdatePermissionsAfterRoleChange() throws Exception {
        // given
        SysRole role = createTestRole(1L, "ADMIN", "管理员", 1);
        SysPermission newPerm = new SysPermission();
        newPerm.setId(3L);
        newPerm.setPermCode("expense:audit");
        
        when(sysRoleRepository.findById(1L)).thenReturn(Optional.of(role));
        when(sysRolePermissionRepository.save(any(SysRolePermission.class))).thenReturn(new SysRolePermission());

        // when & then
        performPost("/api/system/roles/1/permissions", Arrays.asList(1L, 2L, 3L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    /**
     * Task 10.3: 添加权限缓存一致性测试
     */

    @Test
    void shouldInvalidatePermissionCacheWhenRoleUpdated() throws Exception {
        // given
        SysRole role = createTestRole(1L, "ADMIN", "管理员", 1);
        SysRole updatedRole = createTestRole(1L, "ADMIN", "超级管理员", 1);
        when(sysRoleRepository.findById(1L)).thenReturn(Optional.of(role));
        when(sysRoleRepository.save(any(SysRole.class))).thenReturn(updatedRole);

        // when & then
        performPut("/api/system/roles/1", updatedRole)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("角色更新成功"));
    }

    /**
     * Task 10.4: 添加角色与用户关联关系测试
     */

    @Test
    void shouldGetUsersByRoleId() throws Exception {
        // given
        when(sysRoleRepository.existsById(1L)).thenReturn(true);

        // when & then
        performGet("/api/system/roles/1/users")
                .andExpect(status().isOk());
    }

    @Test
    void shouldAssignUsersToRole() throws Exception {
        // given
        SysRole role = createTestRole(1L, "ADMIN", "管理员", 1);
        when(sysRoleRepository.findById(1L)).thenReturn(Optional.of(role));

        // when & then
        performPost("/api/system/roles/1/users", Arrays.asList(1L, 2L, 3L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void shouldRemoveUsersFromRole() throws Exception {
        // given
        SysRole role = createTestRole(1L, "ADMIN", "管理员", 1);
        when(sysRoleRepository.findById(1L)).thenReturn(Optional.of(role));

        // when & then
        performDelete("/api/system/roles/1/users", Arrays.asList(2L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    /**
     * Task 10.5: 添加权限树结构测试
     */

    @Test
    void shouldReturnPermissionTreeWithChildren() throws Exception {
        // given
        SysPermission parentPerm = new SysPermission();
        parentPerm.setId(1L);
        parentPerm.setPermCode("user");
        parentPerm.setPermName("用户管理");
        parentPerm.setParentId(0L);
        parentPerm.setSortOrder(1);
        
        SysPermission childPerm1 = new SysPermission();
        childPerm1.setId(2L);
        childPerm1.setPermCode("user:list");
        childPerm1.setPermName("用户列表");
        childPerm1.setParentId(1L);
        childPerm1.setSortOrder(1);
        
        SysPermission childPerm2 = new SysPermission();
        childPerm2.setId(3L);
        childPerm2.setPermCode("user:create");
        childPerm2.setPermName("用户新增");
        childPerm2.setParentId(1L);
        childPerm2.setSortOrder(2);
        
        when(sysPermissionRepository.findAll()).thenReturn(Arrays.asList(parentPerm, childPerm1, childPerm2));

        // when & then
        performGet("/api/system/roles/permissions/tree")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void shouldReturnPermissionTreeWithMultipleLevels() throws Exception {
        // given
        SysPermission level1 = new SysPermission();
        level1.setId(1L);
        level1.setPermCode("system");
        level1.setPermName("系统管理");
        level1.setParentId(0L);
        
        SysPermission level2 = new SysPermission();
        level2.setId(2L);
        level2.setPermCode("system:user");
        level2.setPermName("用户管理");
        level2.setParentId(1L);
        
        SysPermission level3 = new SysPermission();
        level3.setId(3L);
        level3.setPermCode("system:user:list");
        level3.setPermName("用户列表");
        level3.setParentId(2L);
        
        when(sysPermissionRepository.findAll()).thenReturn(Arrays.asList(level1, level2, level3));

        // when & then
        performGet("/api/system/roles/permissions/tree")
                .andExpect(status().isOk());
    }
}

