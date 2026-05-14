package com.ccms.controller.system;

import com.ccms.entity.system.permission.Menu;
import com.ccms.repository.system.permission.MenuRepository;
import com.ccms.repository.system.permission.RoleRepository;
import com.ccms.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 权限验证和管理控制器
 * 基于D:\aitols\base-app项目的标准进行设计
 */
@RestController
@RequestMapping("/api/permission")
public class PermissionController {

    private final UserService userService;
    private final MenuRepository menuRepository;
    private final RoleRepository roleRepository;

    @Autowired
    public PermissionController(UserService userService, MenuRepository menuRepository, RoleRepository roleRepository) {
        this.userService = userService;
        this.menuRepository = menuRepository;
        this.roleRepository = roleRepository;
    }

    /**
     * 批量校验权限
     */
    @PostMapping("/batch-check")
    public ResponseEntity<Map<String, Boolean>> batchCheckPermissions(
            @RequestHeader("Authorization") String token,
            @RequestBody Map<String, List<String>> request) {
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        List<String> permissions = request.get("permissions");
        Map<String, Boolean> result = new HashMap<>();
        
        if (permissions != null) {
            for (String permission : permissions) {
                boolean hasPermission = userService.checkPermission(token, permission);
                result.put(permission, hasPermission);
            }
        }
        
        return ResponseEntity.ok(result);
    }

    /**
     * 批量校验菜单权限
     */
    @PostMapping("/batch-check-menu")
    public ResponseEntity<Map<String, Boolean>> batchCheckMenuPermissions(
            @RequestHeader("Authorization") String token,
            @RequestBody Map<String, List<String>> request) {
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        List<String> menuCodes = request.get("menuCodes");
        Map<String, Boolean> result = new HashMap<>();
        
        if (menuCodes != null) {
            for (String menuCode : menuCodes) {
                boolean hasPermission = userService.checkMenuPermission(token, menuCode);
                result.put(menuCode, hasPermission);
            }
        }
        
        return ResponseEntity.ok(result);
    }

    /**
     * 获取用户完整的权限集合
     */
    @GetMapping("/full-set")
    public ResponseEntity<Map<String, Object>> getFullPermissionSet(
            @RequestHeader("Authorization") String token) {
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        Map<String, Object> permissionSet = new HashMap<>();
        
        // 权限列表
        List<String> permissionCodes = userService.getUserPermissionCodes(token);
        permissionSet.put("permissions", permissionCodes);
        
        // 菜单树
        List<Map<String, Object>> menuTree = userService.getUserMenuTree(token);
        permissionSet.put("menuTree", menuTree);
        
        // 菜单列表
        List<Map<String, Object>> menuList = userService.getUserMenuList(token);
        permissionSet.put("menuList", menuList);
        
        return ResponseEntity.ok(permissionSet);
    }

    /**
     * 校验用户是否拥有指定角色的权限
     */
    @PostMapping("/check-role")
    public ResponseEntity<Map<String, Boolean>> checkUserRole(
            @RequestHeader("Authorization") String token,
            @RequestBody Map<String, String> request) {
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        String roleCode = request.get("roleCode");
        Long userId = getUserIdFromToken(token);
        
        if (userId == null) {
            Map<String, Boolean> result = new HashMap<>();
            result.put("hasRole", false);
            return ResponseEntity.status(401).body(result);
        }
        
        // 获取用户角色并检查是否包含指定角色
        List<String> userRoles = userService.getUserRoles(userId);
        boolean hasRole = userRoles.contains(roleCode);
        
        Map<String, Boolean> result = new HashMap<>();
        result.put("hasRole", hasRole);
        return ResponseEntity.ok(result);
    }

    /**
     * 根据用户ID获取权限信息（管理员接口）
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<Map<String, Object>> getUserPermissionsById(@PathVariable Long userId) {
        try {
            Map<String, Object> result = new HashMap<>();
            
            // 获取用户角色
            List<String> userRoles = userService.getUserRoles(userId);
            result.put("roles", userRoles);
            
            // 获取用户权限编码
            Set<String> permissionCodes = menuRepository.findUserPermissionCodes(userId);
            result.put("permissions", permissionCodes);
            
            // 获取用户菜单树
            List<Menu> userMenuTree = menuRepository.findUserMenuTree(userId);
            result.put("menuTree", userMenuTree);
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 获取角色权限配置
     */
    @GetMapping("/role/{roleId}")
    public ResponseEntity<Map<String, Object>> getRolePermissions(@PathVariable Long roleId) {
        try {
            Map<String, Object> result = new HashMap<>();
            
            // 获取角色信息
            var roleOpt = roleRepository.findById(roleId);
            if (roleOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            result.put("role", roleOpt.get());
            
            // 获取角色关联的菜单
            List<Menu> roleMenus = menuRepository.findByRoleId(roleId);
            result.put("menus", roleMenus);
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 校验数据权限（根据数据范围）
     */
    @PostMapping("/data-scope")
    public ResponseEntity<Map<String, Boolean>> checkDataScope(
            @RequestHeader("Authorization") String token,
            @RequestBody Map<String, Object> request) {
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        Long deptId = request.get("deptId") != null ? Long.valueOf(request.get("deptId").toString()) : null;
        Long userId = getUserIdFromToken(token);
        
        if (userId == null) {
            Map<String, Boolean> result = new HashMap<>();
            result.put("hasPermission", false);
            return ResponseEntity.status(401).body(result);
        }
        
        // 简化实现：管理员有全部权限，其他用户只能查看本部门数据
        boolean hasPermission = checkDataScopePermission(userId, deptId);
        
        Map<String, Boolean> result = new HashMap<>();
        result.put("hasPermission", hasPermission);
        return ResponseEntity.ok(result);
    }

    /**
     * 获取用户可访问的部门列表（数据权限）
     */
    @GetMapping("/accessible-depts")
    public ResponseEntity<List<Long>> getAccessibleDepts(
            @RequestHeader("Authorization") String token) {
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        Long userId = getUserIdFromToken(token);
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }
        
        // 简化实现：管理员返回空列表（表示所有部门），普通用户返回自己部门
        List<Long> accessibleDepts = getAccessibleDepartments(userId);
        return ResponseEntity.ok(accessibleDepts);
    }

    // ========== 辅助方法 ==========
    
    private Long getUserIdFromToken(String token) {
        var user = userService.getUserByToken(token);
        return user != null ? user.getId() : null;
    }
    
    private boolean checkDataScopePermission(Long userId, Long deptId) {
        // 简化实现：管理员有全部权限
        var user = userService.getUserById(userId);
        if (user != null && "admin".equals(user.getUsername())) {
            return true;
        }
        
        // 普通用户只能查看自己部门的数据
        // 这里需要根据实际业务逻辑实现数据权限检查
        return user != null && deptId != null && deptId.equals(user.getDeptId());
    }
    
    private List<Long> getAccessibleDepartments(Long userId) {
        var user = userService.getUserById(userId);
        
        // 简化实现：管理员返回空列表（表示所有部门）
        if (user != null && "admin".equals(user.getUsername())) {
            return List.of();
        }
        
        // 普通用户只能访问自己部门
        if (user != null && user.getDeptId() != null) {
            return List.of(user.getDeptId());
        }
        
        return List.of();
    }
}