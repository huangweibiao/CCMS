package com.ccms.controller.system;

import com.ccms.entity.system.permission.Menu;
import com.ccms.entity.system.user.User;
import com.ccms.repository.system.permission.MenuRepository;
import com.ccms.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 用户管理控制器
 * 对应设计文档：4.1.1 用户表 (sys_user)
 */
@RestController
@RequestMapping("/api/system/users")
public class UserController {

    private final UserService userService;
    private final MenuRepository menuRepository;

    @Autowired
    public UserController(UserService userService, MenuRepository menuRepository) {
        this.userService = userService;
        this.menuRepository = menuRepository;
    }

    /**
     * 获取用户列表（分页）
     */
    @GetMapping
    public ResponseEntity<Page<User>> getUserList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) Long deptId) {
        Page<User> userPage = userService.getUserList(page, size, username, deptId);
        return ResponseEntity.ok(userPage);
    }

    /**
     * 根据ID获取用户信息
     */
    @GetMapping("/{userId}")
    public ResponseEntity<User> getUserById(@PathVariable Long userId) {
        User user = userService.getUserById(userId);
        if (user != null) {
            return ResponseEntity.ok(user);
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * 根据用户名获取用户信息
     */
    @GetMapping("/username/{username}")
    public ResponseEntity<User> getUserByUsername(@PathVariable String username) {
        User user = userService.loadUserByUsername(username);
        if (user != null) {
            return ResponseEntity.ok(user);
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * 创建用户
     */
    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody User user) {
        User createdUser = userService.createUser(user);
        return ResponseEntity.ok(createdUser);
    }

    /**
     * 更新用户
     */
    @PutMapping("/{userId}")
    public ResponseEntity<User> updateUser(@PathVariable Long userId, @RequestBody User user) {
        user.setId(userId);
        User updatedUser = userService.updateUser(user);
        return ResponseEntity.ok(updatedUser);
    }

    /**
     * 删除用户
     */
    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long userId) {
        boolean success = userService.deleteUser(userId);
        if (success) {
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.badRequest().build();
    }

    /**
     * 更新用户状态
     */
    @PutMapping("/{userId}/status")
    public ResponseEntity<Void> updateUserStatus(
            @PathVariable Long userId,
            @RequestParam Integer status) {
        boolean success = userService.updateUserStatus(userId, status);
        if (success) {
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.badRequest().build();
    }

    /**
     * 重置用户密码
     */
    @PostMapping("/{userId}/reset-password")
    public ResponseEntity<Map<String, String>> resetPassword(@PathVariable Long userId) {
        String newPassword = userService.resetPassword(userId);
        Map<String, String> result = new HashMap<>();
        result.put("newPassword", newPassword);
        return ResponseEntity.ok(result);
    }

    /**
     * 分配用户角色
     */
    @PostMapping("/{userId}/roles")
    public ResponseEntity<Void> assignUserRoles(
            @PathVariable Long userId,
            @RequestBody Long[] roleIds) {
        boolean success = userService.assignUserRoles(userId, roleIds);
        if (success) {
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.badRequest().build();
    }

    /**
     * 获取用户角色列表
     */
    @GetMapping("/{userId}/roles")
    public ResponseEntity<List<String>> getUserRoles(@PathVariable Long userId) {
        List<String> roles = userService.getUserRoles(userId);
        return ResponseEntity.ok(roles);
    }

    /**
     * 检查用户名是否存在
     */
    @GetMapping("/check-username")
    public ResponseEntity<Map<String, Boolean>> checkUsernameExists(@RequestParam String username) {
        boolean exists = userService.isUsernameExist(username);
        Map<String, Boolean> result = new HashMap<>();
        result.put("exists", exists);
        return ResponseEntity.ok(result);
    }

    /**
     * 获取用户统计信息
     */
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getUserStatistics(
            @RequestParam(required = false) Long deptId) {
        Map<String, Object> statistics = userService.getUserStatistics(deptId);
        return ResponseEntity.ok(statistics);
    }

    /**
     * 获取用户菜单权限信息
     */
    @GetMapping("/{userId}/menu-permissions")
    public ResponseEntity<Map<String, Object>> getUserMenuPermissions(@PathVariable Long userId) {
        try {
            Map<String, Object> result = new HashMap<>();
            
            // 获取用户角色
            List<String> userRoles = userService.getUserRoles(userId);
            result.put("roles", userRoles);
            
            // 获取用户权限菜单
            List<Menu> userMenus = menuRepository.findUserMenuTree(userId);
            result.put("menus", userMenus);
            
            // 获取用户权限编码
            var permissionCodes = menuRepository.findUserPermissionCodes(userId);
            result.put("permissionCodes", permissionCodes);
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 检查用户是否有菜单权限
     */
    @PostMapping("/{userId}/check-menu-permission")
    public ResponseEntity<Map<String, Boolean>> checkUserMenuPermission(
            @PathVariable Long userId, 
            @RequestBody Map<String, String> request) {
        String menuCode = request.get("menuCode");
        boolean hasPermission = menuRepository.hasMenuPermission(userId, menuCode);
        
        Map<String, Boolean> result = new HashMap<>();
        result.put("hasPermission", hasPermission);
        return ResponseEntity.ok(result);
    }

    /**
     * 获取用户菜单树
     */
    @GetMapping("/{userId}/menu-tree")
    public ResponseEntity<List<Menu>> getUserMenuTree(@PathVariable Long userId) {
        List<Menu> menuTree = menuRepository.findUserMenuTree(userId);
        return ResponseEntity.ok(menuTree);
    }

    /**
     * 获取用户权限统计
     */
    @GetMapping("/{userId}/permission-stats")
    public ResponseEntity<Map<String, Object>> getUserPermissionStats(@PathVariable Long userId) {
        Map<String, Object> stats = new HashMap<>();
        
        // 角色数量
        List<String> userRoles = userService.getUserRoles(userId);
        stats.put("roleCount", userRoles.size());
        
        // 菜单权限数量
        List<Menu> userMenus = menuRepository.findUserMenuTree(userId);
        stats.put("menuCount", userMenus.size());
        
        // 权限编码数量
        var permissionCodes = menuRepository.findUserPermissionCodes(userId);
        stats.put("permissionCount", permissionCodes.size());
        
        // 菜单类型统计
        long dirCount = userMenus.stream().filter(Menu::isDir).count();
        long menuCount = userMenus.stream().filter(Menu::isMenu).count();
        long buttonCount = userMenus.stream().filter(Menu::isButton).count();
        
        stats.put("dirCount", dirCount);
        stats.put("menuCount", menuCount);
        stats.put("buttonCount", buttonCount);
        
        return ResponseEntity.ok(stats);
    }

    /**
     * 批量检查用户权限
     */
    @PostMapping("/{userId}/batch-check-permissions")
    public ResponseEntity<Map<String, Boolean>> batchCheckUserPermissions(
            @PathVariable Long userId,
            @RequestBody Map<String, List<String>> request) {
        List<String> permissionCodes = request.get("permissionCodes");
        Map<String, Boolean> result = new HashMap<>();
        
        if (permissionCodes != null) {
            for (String permission : permissionCodes) {
                boolean hasPermission = menuRepository.hasMenuPermission(userId, permission);
                result.put(permission, hasPermission);
            }
        }
        
        return ResponseEntity.ok(result);
    }

    /**
     * 获取用户可以访问的菜单编码列表
     */
    @GetMapping("/{userId}/accessible-menu-codes")
    public ResponseEntity<List<String>> getAccessibleMenuCodes(@PathVariable Long userId) {
        var menuCodes = menuRepository.findUserPermissionCodes(userId);
        return ResponseEntity.ok(List.copyOf(menuCodes));
    }

    /**
     * 验证用户是否有数据访问权限
     */
    @PostMapping("/{userId}/check-data-permission")
    public ResponseEntity<Map<String, Boolean>> checkDataPermission(
            @PathVariable Long userId,
            @RequestBody Map<String, Object> request) {
        Long targetDeptId = request.get("targetDeptId") != null ? 
            Long.valueOf(request.get("targetDeptId").toString()) : null;
        
        // 简化实现：管理员有全部权限，其他用户只能访问自己部门数据
        boolean hasPermission = checkUserDataPermission(userId, targetDeptId);
        
        Map<String, Boolean> result = new HashMap<>();
        result.put("hasPermission", hasPermission);
        return ResponseEntity.ok(result);
    }

    /**
     * 获取用户可访问的部门列表（数据权限）
     */
    @GetMapping("/{userId}/accessible-departments")
    public ResponseEntity<List<Long>> getAccessibleDepartments(@PathVariable Long userId) {
        List<Long> accessibleDepts = determineAccessibleDepartments(userId);
        return ResponseEntity.ok(accessibleDepts);
    }

    // ========== 辅助方法 ==========
    
    private boolean checkUserDataPermission(Long userId, Long targetDeptId) {
        User user = userService.getUserById(userId);
        
        // 管理员有全部权限
        if (user != null && "admin".equals(user.getUsername())) {
            return true;
        }
        
        // 普通用户只能访问自己部门的数据
        return user != null && targetDeptId != null && targetDeptId.equals(user.getDeptId());
    }
    
    private List<Long> determineAccessibleDepartments(Long userId) {
        User user = userService.getUserById(userId);
        
        // 管理员可以访问所有部门
        if (user != null && "admin".equals(user.getUsername())) {
            return List.of(); // 空列表表示所有部门
        }
        
        // 普通用户只能访问自己部门
        if (user != null && user.getDeptId() != null) {
            return List.of(user.getDeptId());
        }
        
        return List.of();
    }
}
