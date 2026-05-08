package com.ccms.controller.system;

import com.ccms.entity.system.permission.SysPermission;
import com.ccms.entity.system.permission.SysRole;
import com.ccms.entity.system.permission.SysRolePermission;
import com.ccms.repository.system.permission.SysPermissionRepository;
import com.ccms.repository.system.permission.SysRolePermissionRepository;
import com.ccms.repository.system.permission.SysRoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 角色管理控制器
 * 对应设计文档：4.1.3 角色表
 */
@RestController
@RequestMapping("/api/system/roles")
public class RoleController {

    private final SysRoleRepository sysRoleRepository;
    private final SysPermissionRepository sysPermissionRepository;
    private final SysRolePermissionRepository sysRolePermissionRepository;

    @Autowired
    public RoleController(SysRoleRepository sysRoleRepository,
                         SysPermissionRepository sysPermissionRepository,
                         SysRolePermissionRepository sysRolePermissionRepository) {
        this.sysRoleRepository = sysRoleRepository;
        this.sysPermissionRepository = sysPermissionRepository;
        this.sysRolePermissionRepository = sysRolePermissionRepository;
    }

    /**
     * 获取角色列表（分页）
     */
    @GetMapping
    public ResponseEntity<Page<SysRole>> getRoleList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String roleName,
            @RequestParam(required = false) Integer status) {
        
        List<SysRole> allRoles = sysRoleRepository.findAll();
        
        // 根据条件过滤
        List<SysRole> filtered = allRoles.stream()
                .filter(r -> roleName == null || r.getRoleName().contains(roleName))
                .filter(r -> status == null || status.equals(r.getStatus()))
                .toList();
        
        // 手动分页
        int start = Math.min(page * size, filtered.size());
        int end = Math.min(start + size, filtered.size());
        Page<SysRole> rolePage = new PageImpl<>(
                filtered.subList(start, end),
                PageRequest.of(page, size),
                filtered.size()
        );
        
        return ResponseEntity.ok(rolePage);
    }

    /**
     * 获取所有可用角色
     */
    @GetMapping("/active")
    public ResponseEntity<List<SysRole>> getActiveRoles() {
        List<SysRole> roles = sysRoleRepository.findByStatus(1);
        return ResponseEntity.ok(roles);
    }

    /**
     * 根据ID获取角色
     */
    @GetMapping("/{roleId}")
    public ResponseEntity<SysRole> getRoleById(@PathVariable Long roleId) {
        return sysRoleRepository.findById(roleId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * 根据编码获取角色
     */
    @GetMapping("/code/{roleCode}")
    public ResponseEntity<SysRole> getRoleByCode(@PathVariable String roleCode) {
        return sysRoleRepository.findByRoleCode(roleCode)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * 创建角色
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createRole(@RequestBody SysRole role) {
        Map<String, Object> result = new HashMap<>();
        
        // 检查编码是否已存在
        if (sysRoleRepository.existsByRoleCode(role.getRoleCode())) {
            result.put("success", false);
            result.put("message", "角色编码已存在");
            return ResponseEntity.badRequest().body(result);
        }
        
        // 设置默认值
        if (role.getStatus() == null) {
            role.setStatus(1);
        }
        
        SysRole saved = sysRoleRepository.save(role);
        result.put("success", true);
        result.put("message", "角色创建成功");
        result.put("data", saved);
        return ResponseEntity.ok(result);
    }

    /**
     * 更新角色
     */
    @PutMapping("/{roleId}")
    public ResponseEntity<Map<String, Object>> updateRole(@PathVariable Long roleId, @RequestBody SysRole role) {
        Map<String, Object> result = new HashMap<>();
        
        Optional<SysRole> existingOpt = sysRoleRepository.findById(roleId);
        if (existingOpt.isEmpty()) {
            result.put("success", false);
            result.put("message", "角色不存在");
            return ResponseEntity.badRequest().body(result);
        }
        
        // 检查编码是否与其他角色冲突
        Optional<SysRole> roleWithCode = sysRoleRepository.findByRoleCode(role.getRoleCode());
        if (roleWithCode.isPresent() && !roleWithCode.get().getId().equals(roleId)) {
            result.put("success", false);
            result.put("message", "角色编码已存在");
            return ResponseEntity.badRequest().body(result);
        }
        
        role.setId(roleId);
        SysRole updated = sysRoleRepository.save(role);
        result.put("success", true);
        result.put("message", "角色更新成功");
        result.put("data", updated);
        return ResponseEntity.ok(result);
    }

    /**
     * 删除角色
     */
    @DeleteMapping("/{roleId}")
    public ResponseEntity<Map<String, Object>> deleteRole(@PathVariable Long roleId) {
        Map<String, Object> result = new HashMap<>();
        
        Optional<SysRole> existingOpt = sysRoleRepository.findById(roleId);
        if (existingOpt.isEmpty()) {
            result.put("success", false);
            result.put("message", "角色不存在");
            return ResponseEntity.badRequest().body(result);
        }
        
        // 删除角色权限关联
        List<SysRolePermission> rolePerms = sysRolePermissionRepository.findByRoleId(roleId);
        sysRolePermissionRepository.deleteAll(rolePerms);
        
        sysRoleRepository.deleteById(roleId);
        result.put("success", true);
        result.put("message", "角色删除成功");
        return ResponseEntity.ok(result);
    }

    /**
     * 启用/禁用角色
     */
    @PutMapping("/{roleId}/status")
    public ResponseEntity<Map<String, Object>> updateRoleStatus(
            @PathVariable Long roleId,
            @RequestParam Integer status) {
        Map<String, Object> result = new HashMap<>();
        
        Optional<SysRole> roleOpt = sysRoleRepository.findById(roleId);
        if (roleOpt.isEmpty()) {
            result.put("success", false);
            result.put("message", "角色不存在");
            return ResponseEntity.badRequest().body(result);
        }
        
        SysRole role = roleOpt.get();
        role.setStatus(status);
        sysRoleRepository.save(role);
        
        result.put("success", true);
        result.put("message", status == 1 ? "角色已启用" : "角色已禁用");
        return ResponseEntity.ok(result);
    }

    /**
     * 获取角色的权限列表
     */
    @GetMapping("/{roleId}/permissions")
    public ResponseEntity<List<SysPermission>> getRolePermissions(@PathVariable Long roleId) {
        List<SysRolePermission> rolePerms = sysRolePermissionRepository.findByRoleId(roleId);
        List<Long> permIds = rolePerms.stream()
                .map(SysRolePermission::getPermissionId)
                .collect(Collectors.toList());
        
        List<SysPermission> permissions = sysPermissionRepository.findAllById(permIds);
        return ResponseEntity.ok(permissions);
    }

    /**
     * 分配角色权限
     */
    @PostMapping("/{roleId}/permissions")
    public ResponseEntity<Map<String, Object>> assignPermissions(
            @PathVariable Long roleId,
            @RequestBody List<Long> permissionIds) {
        Map<String, Object> result = new HashMap<>();
        
        Optional<SysRole> roleOpt = sysRoleRepository.findById(roleId);
        if (roleOpt.isEmpty()) {
            result.put("success", false);
            result.put("message", "角色不存在");
            return ResponseEntity.badRequest().body(result);
        }
        
        // 删除原有权限
        List<SysRolePermission> existing = sysRolePermissionRepository.findByRoleId(roleId);
        sysRolePermissionRepository.deleteAll(existing);
        
        // 添加新权限
        for (Long permId : permissionIds) {
            SysRolePermission rolePerm = new SysRolePermission();
            rolePerm.setRoleId(roleId);
            rolePerm.setPermissionId(permId);
            sysRolePermissionRepository.save(rolePerm);
        }
        
        result.put("success", true);
        result.put("message", "权限分配成功");
        return ResponseEntity.ok(result);
    }

    /**
     * 获取所有权限列表
     */
    @GetMapping("/permissions/all")
    public ResponseEntity<List<SysPermission>> getAllPermissions() {
        List<SysPermission> permissions = sysPermissionRepository.findAll();
        return ResponseEntity.ok(permissions);
    }

    /**
     * 获取权限树
     */
    @GetMapping("/permissions/tree")
    public ResponseEntity<List<Map<String, Object>>> getPermissionTree() {
        List<SysPermission> allPerms = sysPermissionRepository.findAll();
        List<Map<String, Object>> tree = buildPermissionTree(allPerms, 0L);
        return ResponseEntity.ok(tree);
    }

    /**
     * 构建权限树
     */
    private List<Map<String, Object>> buildPermissionTree(List<SysPermission> allPerms, Long parentId) {
        List<Map<String, Object>> tree = new ArrayList<>();
        
        for (SysPermission perm : allPerms) {
            Long permParentId = perm.getParentId();
            if (permParentId == null) permParentId = 0L;
            
            if (permParentId.equals(parentId)) {
                Map<String, Object> node = new HashMap<>();
                node.put("id", perm.getId());
                node.put("permCode", perm.getPermCode());
                node.put("permName", perm.getPermName());
                node.put("permType", perm.getPermType());
                node.put("parentId", perm.getParentId());
                node.put("sortOrder", perm.getSortOrder());
                node.put("children", buildPermissionTree(allPerms, perm.getId()));
                tree.add(node);
            }
        }
        
        tree.sort(Comparator.comparing(m -> (Integer) m.getOrDefault("sortOrder", 0)));
        return tree;
    }
}
