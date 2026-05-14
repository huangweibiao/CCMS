package com.ccms.controller.system;

import com.ccms.entity.system.permission.Menu;
import com.ccms.repository.system.permission.MenuRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 菜单管理控制器
 * 基于D:\aitols\base-app项目的标准进行设计
 */
@RestController
@RequestMapping("/api/menu")
public class MenuController {

    private final MenuRepository menuRepository;

    @Autowired
    public MenuController(MenuRepository menuRepository) {
        this.menuRepository = menuRepository;
    }

    /**
     * 获取菜单列表
     */
    @GetMapping
    public ResponseEntity<List<Menu>> getMenuList() {
        List<Menu> menus = menuRepository.findByStatus(1);
        return ResponseEntity.ok(menus);
    }

    /**
     * 获取菜单树形结构
     */
    @GetMapping("/tree")
    public ResponseEntity<List<Menu>> getMenuTree() {
        List<Menu> menuTree = menuRepository.findMenuTree();
        return ResponseEntity.ok(menuTree);
    }

    /**
     * 根据ID获取菜单详情
     */
    @GetMapping("/{id}")
    public ResponseEntity<Menu> getMenuById(@PathVariable Long id) {
        Optional<Menu> menuOpt = menuRepository.findById(id);
        if (menuOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(menuOpt.get());
    }

    /**
     * 创建菜单
     */
    @PostMapping
    public ResponseEntity<Menu> createMenu(@RequestBody Menu menu) {
        // 验证菜单名称是否已存在
        if (menuRepository.existsByMenuNameAndIdNot(menu.getMenuName(), 0L)) {
            return ResponseEntity.badRequest().body(null);
        }

        // 验证权限标识是否已存在
        if (menu.getPermissionCode() != null && !menu.getPermissionCode().isEmpty()) {
            if (menuRepository.existsByPermissionCodeAndIdNot(menu.getPermissionCode(), 0L)) {
                return ResponseEntity.badRequest().body(null);
            }
        }

        menu.setCreateTime(LocalDateTime.now());
        menu.setUpdateTime(LocalDateTime.now());
        Menu savedMenu = menuRepository.save(menu);
        return ResponseEntity.ok(savedMenu);
    }

    /**
     * 更新菜单
     */
    @PutMapping("/{id}")
    public ResponseEntity<Menu> updateMenu(@PathVariable Long id, @RequestBody Menu menu) {
        Optional<Menu> existingMenuOpt = menuRepository.findById(id);
        if (existingMenuOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        // 验证菜单名称是否已存在（排除当前菜单）
        if (menuRepository.existsByMenuNameAndIdNot(menu.getMenuName(), id)) {
            return ResponseEntity.badRequest().body(null);
        }

        // 验证权限标识是否已存在（排除当前菜单）
        if (menu.getPermissionCode() != null && !menu.getPermissionCode().isEmpty()) {
            if (menuRepository.existsByPermissionCodeAndIdNot(menu.getPermissionCode(), id)) {
                return ResponseEntity.badRequest().body(null);
            }
        }

        Menu existingMenu = existingMenuOpt.get();
        existingMenu.setMenuName(menu.getMenuName());
        existingMenu.setMenuType(menu.getMenuType());
        existingMenu.setPath(menu.getPath());
        existingMenu.setComponent(menu.getComponent());
        existingMenu.setPermissionCode(menu.getPermissionCode());
        existingMenu.setIcon(menu.getIcon());
        existingMenu.setSortOrder(menu.getSortOrder());
        existingMenu.setVisible(menu.getVisible());
        existingMenu.setStatus(menu.getStatus());
        existingMenu.setParentId(menu.getParentId());
        existingMenu.setRemark(menu.getRemark());
        existingMenu.setUpdateTime(LocalDateTime.now());

        Menu updatedMenu = menuRepository.save(existingMenu);
        return ResponseEntity.ok(updatedMenu);
    }

    /**
     * 删除菜单
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Boolean>> deleteMenu(@PathVariable Long id) {
        // 检查是否存在子菜单
        List<Menu> childMenus = menuRepository.findByParentId(id);
        if (!childMenus.isEmpty()) {
            Map<String, Boolean> result = new HashMap<>();
            result.put("success", false);
            result.put("hasChildren", true);
            return ResponseEntity.badRequest().body(result);
        }

        try {
            menuRepository.deleteById(id);
            Map<String, Boolean> result = new HashMap<>();
            result.put("success", true);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Boolean> result = new HashMap<>();
            result.put("success", false);
            return ResponseEntity.badRequest().body(result);
        }
    }

    /**
     * 禁用/启用菜单
     */
    @PostMapping("/{id}/status")
    public ResponseEntity<Menu> updateMenuStatus(@PathVariable Long id, @RequestBody Map<String, Integer> request) {
        Integer status = request.get("status");
        if (status == null || (status != 0 && status != 1)) {
            return ResponseEntity.badRequest().build();
        }

        Optional<Menu> menuOpt = menuRepository.findById(id);
        if (menuOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Menu menu = menuOpt.get();
        menu.setStatus(status);
        menu.setUpdateTime(LocalDateTime.now());
        Menu updatedMenu = menuRepository.save(menu);
        return ResponseEntity.ok(updatedMenu);
    }

    /**
     * 获取用户有权限的菜单树
     */
    @GetMapping("/user/{userId}/tree")
    public ResponseEntity<List<Menu>> getUserMenuTree(@PathVariable Long userId) {
        List<Menu> userMenuTree = menuRepository.findUserMenuTree(userId);
        return ResponseEntity.ok(userMenuTree);
    }

    /**
     * 根据父菜单ID获取子菜单列表
     */
    @GetMapping("/parent/{parentId}")
    public ResponseEntity<List<Menu>> getChildMenus(@PathVariable Long parentId) {
        List<Menu> childMenus = menuRepository.findByParentIdAndStatus(parentId, 1);
        return ResponseEntity.ok(childMenus);
    }

    /**
     * 获取所有可见菜单
     */
    @GetMapping("/visible")
    public ResponseEntity<List<Menu>> getVisibleMenus() {
        List<Menu> visibleMenus = menuRepository.findByVisible(true);
        return ResponseEntity.ok(visibleMenus);
    }

    /**
     * 获取所有一级菜单
     */
    @GetMapping("/root")
    public ResponseEntity<List<Menu>> getRootMenus() {
        List<Menu> rootMenus = menuRepository.findRootMenus();
        return ResponseEntity.ok(rootMenus);
    }

    /**
     * 检查菜单名称是否已存在
     */
    @PostMapping("/check-name")
    public ResponseEntity<Map<String, Boolean>> checkMenuNameExists(@RequestBody Map<String, String> request) {
        String menuName = request.get("menuName");
        Long excludeId = request.get("excludeId") != null ? Long.valueOf(request.get("excludeId")) : 0L;
        
        boolean exists = menuRepository.existsByMenuNameAndIdNot(menuName, excludeId);
        Map<String, Boolean> result = new HashMap<>();
        result.put("exists", exists);
        return ResponseEntity.ok(result);
    }

    /**
     * 检查权限标识是否已存在
     */
    @PostMapping("/check-permission-code")
    public ResponseEntity<Map<String, Boolean>> checkPermissionCodeExists(@RequestBody Map<String, String> request) {
        String permissionCode = request.get("permissionCode");
        Long excludeId = request.get("excludeId") != null ? Long.valueOf(request.get("excludeId")) : 0L;
        
        if (permissionCode == null || permissionCode.isEmpty()) {
            Map<String, Boolean> result = new HashMap<>();
            result.put("exists", false);
            return ResponseEntity.ok(result);
        }
        
        boolean exists = menuRepository.existsByPermissionCodeAndIdNot(permissionCode, excludeId);
        Map<String, Boolean> result = new HashMap<>();
        result.put("exists", exists);
        return ResponseEntity.ok(result);
    }
}