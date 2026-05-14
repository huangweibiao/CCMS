package com.ccms.service.impl;

import com.ccms.entity.system.permission.Menu;
import com.ccms.entity.system.user.User;
import com.ccms.repository.system.permission.MenuRepository;
import com.ccms.repository.system.user.UserRepository;
import com.ccms.service.UserService;
import com.ccms.util.PermissionMonitorUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

/**
 * 用户管理服务实现类
 *
 * @author 系统生成
 */
@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final MenuRepository menuRepository;
    private final PasswordEncoder passwordEncoder;
    private final PermissionMonitorUtil permissionMonitorUtil;
    
    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    // 简单令牌存储（实际项目中应使用Redis等持久化存储）
    private final Map<String, Long> tokenStorage = new java.util.concurrent.ConcurrentHashMap<>();

    @Autowired
    public UserServiceImpl(UserRepository userRepository, MenuRepository menuRepository, PermissionMonitorUtil permissionMonitorUtil) {
        this.userRepository = userRepository;
        this.menuRepository = menuRepository;
        this.passwordEncoder = new BCryptPasswordEncoder();
        this.permissionMonitorUtil = permissionMonitorUtil;
    }

    @Override
    public Object login(String username, String password) {
        long startTime = System.currentTimeMillis();
        String requestIp = getClientIP(); // 需要在类中添加获取客户端IP的方法
        
        logger.info("登录请求开始 - username: {}, ip: {}", username, requestIp);
        
        // 根据用户名查找用户
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isEmpty()) {
            permissionMonitorUtil.logLoginFailure(username, requestIp, "用户不存在");
            logger.warn("登录失败 - 用户不存在: {}", username);
            return null;
        }
        User user = userOpt.get();

        // 检查用户状态
        if (!user.isEnabled()) {
            permissionMonitorUtil.logLoginFailure(username, requestIp, "用户已被禁用");
            logger.warn("登录失败 - 用户被禁用: {}", username);
            return null;
        }

        // 验证密码
        if (!passwordEncoder.matches(password, user.getPassword())) {
            permissionMonitorUtil.logLoginFailure(username, requestIp, "密码错误");
            logger.warn("登录失败 - 密码错误: {}", username);
            return null;
        }

        // 生成令牌
        String token = generateToken(user);

        // 更新登录信息
        user.setLastLoginTime(LocalDateTime.now());
        user.setLoginCount(user.getLoginCount() + 1);
        userRepository.save(user);

        // 获取用户权限信息
        Map<String, Object> permissions = getLoginPermissions(user.getId());
        
        // 返回登录响应
        Map<String, Object> response = new HashMap<>();
        response.put("token", token);
        response.put("user", constructUserProfile(username));
        response.put("permissions", permissions);

        // 记录登录成功
        permissionMonitorUtil.logLoginSuccess(user.getId().toString(), username, requestIp);
        
        long endTime = System.currentTimeMillis();
        logger.info("登录成功 - username: {}, userId: {}, time: {}ms", 
                   username, user.getId(), (endTime - startTime));

        return response;
    }
    
    /**
     * 获取客户端IP地址（简化实现）
     */
    private String getClientIP() {
        // 在实际项目中，这里应该从HttpServletRequest中获取真实IP
        return "127.0.0.1"; // 临时返回本地IP
    }

    @Override
    public void logout(String token) {
        tokenStorage.remove(token);
    }

    @Override
    public boolean hasPermission(Long userId, String permissionCode) {
        // 简化实现
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return false;
        }

        if ("admin".equals(user.getUsername())) {
            return true;
        }

        // 基本权限检查
        return user.hasPermission(permissionCode);
    }

    @Override
    public List<String> getUserRoles(Long userId) {
        List<String> roles = new ArrayList<>();
        User user = userRepository.findById(userId).orElse(null);
        if (user != null) {
            roles.addAll(user.getRoleCodes());
        }
        return roles;
    }

    @Override
    public String generateToken(User user) {
        String token = UUID.randomUUID().toString().replace("-", "");
        tokenStorage.put(token, user.getId());
        return token;
    }

    @Override
    public boolean validateToken(String token) {
        return tokenStorage.containsKey(token);
    }

    @Override
    public User getUserByToken(String token) {
        Long userId = tokenStorage.get(token);
        if (userId != null) {
            return userRepository.findById(userId).orElse(null);
        }
        return null;
    }

    @Override
    public boolean changePassword(String token, String oldPassword, String newPassword) {
        Long userId = getUserIdFromToken(token);
        if (userId == null) {
            return false;
        }

        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return false;
        }

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            return false;
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setUpdateTime(LocalDateTime.now());
        userRepository.save(user);

        return true;
    }

    @Override
    public String resetPassword(Long userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return null;
        }

        String newPassword = generateRandomPassword();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        return newPassword;
    }

    @Override
    public boolean isUsernameExist(String username) {
        return userRepository.existsByUsername(username);
    }

    @Override
    public User register(User user, List<Long> roleIds) {
        if (isUsernameExist(user.getUsername())) {
            throw new RuntimeException("用户名已存在");
        }

        // 设置默认密码
        if (user.getPassword() == null || user.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode("123456"));
        } else {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }

        // 设置默认状态
        user.setEnabled(true);
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());

        return userRepository.save(user);
    }

    @Override
    public boolean checkPermission(String token, String permission) {
        long startTime = System.currentTimeMillis();
        
        try {
            String username = getUsernameFromToken(token);
            if (username == null) {
                permissionMonitorUtil.logPermissionDenied("unknown", permission, "Token无效");
                logger.warn("权限检查失败 - Token无效: permission={}", permission);
                return false;
            }
            User user = loadUserByUsername(username);
            if (user == null) {
                permissionMonitorUtil.logPermissionDenied("unknown", permission, "用户不存在");
                logger.warn("权限检查失败 - 用户不存在: username={}, permission={}", username, permission);
                return false;
            }
            
            boolean result = hasPermission(user.getId(), permission);
            long endTime = System.currentTimeMillis();
            
            if (result) {
                permissionMonitorUtil.logPermissionAllowed(user.getId().toString(), permission, "基于Token检查");
                logger.debug("权限检查通过: username={}, userId={}, permission={}, time={}ms", 
                           username, user.getId(), permission, (endTime - startTime));
            } else {
                permissionMonitorUtil.logPermissionDenied(user.getId().toString(), permission, "权限不足");
                logger.warn("权限检查失败 - 权限不足: username={}, userId={}, permission={}, time={}ms", 
                          username, user.getId(), permission, (endTime - startTime));
            }
            
            return result;
        } catch (Exception e) {
            permissionMonitorUtil.logPermissionDenied("unknown", permission, "检查异常: " + e.getMessage());
            logger.error("权限检查异常 - permission={}, error={}", permission, e.getMessage(), e);
            return false;
        }
    }

    @Override
    public Page<User> getUserList(int page, int size, String username, Long deptId) {
        PageRequest pageRequest = PageRequest.of(page, size);
        // 简化实现 - 返回所有用户分页
        return userRepository.findAll(pageRequest);
    }

    @Override
    public User getUserById(Long userId) {
        return userRepository.findById(userId).orElse(null);
    }

    @Override
    public User createUser(User user) {
        if (user.getPassword() == null || user.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode("123456"));
        } else {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }
        user.setEnabled(true);
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());
        return userRepository.save(user);
    }

    @Override
    public User updateUser(User user) {
        user.setUpdateTime(LocalDateTime.now());
        return userRepository.save(user);
    }

    @Override
    public boolean deleteUser(Long userId) {
        try {
            userRepository.deleteById(userId);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean updateUserStatus(Long userId, Integer status) {
        try {
            User user = userRepository.findById(userId).orElse(null);
            if (user == null) {
                return false;
            }
            user.setEnabled(status == 1);
            user.setUpdateTime(LocalDateTime.now());
            userRepository.save(user);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean assignUserRoles(Long userId, Long[] roleIds) {
        // 简化实现
        return true;
    }

    @Override
    public Map<String, Object> getUserStatistics(Long deptId) {
        Map<String, Object> stats = new HashMap<>();
        long totalUsers = userRepository.count();
        long activeUsers = userRepository.findByEnabledTrue().size();

        stats.put("totalUsers", totalUsers);
        stats.put("activeUsers", activeUsers);
        stats.put("inactiveUsers", totalUsers - activeUsers);

        return stats;
    }

    @Override
    public User loadUserByUsername(String username) {
        return userRepository.findByUsername(username).orElse(null);
    }

    @Override
    public String getUsernameFromToken(String token) {
        Long userId = tokenStorage.get(token);
        if (userId != null) {
            User user = userRepository.findById(userId).orElse(null);
            if (user != null) {
                return user.getUsername();
            }
        }
        return null;
    }

    @Override
    public String refreshToken(String token) {
        if (!validateToken(token)) {
            throw new RuntimeException("Token无效");
        }

        Long userId = tokenStorage.get(token);
        if (userId == null) {
            throw new RuntimeException("用户信息不存在");
        }

        // 移除旧token
        tokenStorage.remove(token);

        // 生成新token
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        return generateToken(user);
    }

    @Override
    public Map<String, Object> getUserPermissions(String token) {
        long startTime = System.currentTimeMillis();
        Map<String, Object> permissions = new HashMap<>();

        Long userId = tokenStorage.get(token);
        if (userId == null) {
            logger.warn("获取用户权限失败 - Token无效: token=[REDACTED]");
            permissions.put("permissions", new String[0]);
            permissions.put("role", "GUEST");
            return permissions;
        }

        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            permissionMonitorUtil.logPermissionQuery(userId.toString(), "权限查询", "用户不存在");
            logger.warn("获取用户权限失败 - 用户不存在: userId={}", userId);
            permissions.put("permissions", new String[0]);
            permissions.put("role", "GUEST");
            return permissions;
        }

        permissions.put("permissions", user.getPermissions());
        permissions.put("roles", user.getRoleCodes());
        permissions.put("userId", userId);
        permissions.put("username", user.getUsername());
        
        long endTime = System.currentTimeMillis();
        permissionMonitorUtil.logPermissionQuery(userId.toString(), "权限集合查询", "成功");
        logger.info("获取用户权限成功: username={}, userId={}, time={}ms", 
                   user.getUsername(), userId, (endTime - startTime));

        return permissions;
    }

    @Override
    public Map<String, Object> getUserProfileByToken(String tokenParam) {
        Long userId = tokenStorage.get(tokenParam);
        if (userId == null) {
            return null;
        }

        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return null;
        }

        Map<String, Object> profile = new HashMap<>();
        profile.put("id", user.getId());
        profile.put("username", user.getUsername());
        profile.put("name", user.getName());
        profile.put("email", user.getEmail());
        profile.put("phone", user.getPhone());
        profile.put("employeeNo", user.getEmployeeNo());
        profile.put("position", user.getPosition());
        profile.put("status", user.getStatus());
        profile.put("enabled", user.isEnabled());
        profile.put("createTime", user.getCreateTime());
        profile.put("updateTime", user.getUpdateTime());
        profile.put("lastLoginTime", user.getLastLoginTime());

        return profile;
    }

    // ========== 辅助方法 ==========

    private Long getUserIdFromToken(String token) {
        return tokenStorage.get(token);
    }

    private String generateRandomPassword() {
        return UUID.randomUUID().toString().substring(0, 8);
    }

    /**
     * 构建用户档案信息
     */
    private Map<String, Object> constructUserProfile(String username) {
        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null) {
            return null;
        }

        Map<String, Object> profile = new HashMap<>();
        profile.put("id", user.getId());
        profile.put("username", user.getUsername());
        profile.put("name", user.getName());
        profile.put("email", user.getEmail());
        profile.put("phone", user.getPhone());
        profile.put("employeeNo", user.getEmployeeNo());
        profile.put("position", user.getPosition());
        profile.put("status", user.getStatus());
        profile.put("enabled", user.isEnabled());
        profile.put("createTime", user.getCreateTime());
        profile.put("lastLoginTime", user.getLastLoginTime());
        profile.put("roles", user.getRoleCodes());

        return profile;
    }

    /**
     * 获取登录所需的权限信息
     */
    private Map<String, Object> getLoginPermissions(Long userId) {
        Map<String, Object> permissions = new HashMap<>();
        
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            permissions.put("permissionCodes", new ArrayList<String>());
            permissions.put("menuTree", new ArrayList<Map<String, Object>>());
            permissions.put("menuList", new ArrayList<Map<String, Object>>());
            return permissions;
        }

        // 获取权限标识集合
        Set<String> permissionCodes = menuRepository.findUserPermissionCodes(userId);
        permissions.put("permissionCodes", new ArrayList<>(permissionCodes));

        // 获取菜单树和菜单列表
        List<Menu> userMenus = menuRepository.findUserMenuTree(userId);
        permissions.put("menuTree", buildMenuTree(userMenus));
        permissions.put("menuList", convertMenuToMapList(userMenus));

        return permissions;
    }

    @Override
    public List<Map<String, Object>> getUserMenuTree(String token) {
        long startTime = System.currentTimeMillis();
        
        Long userId = tokenStorage.get(token);
        if (userId == null) {
            logger.warn("获取用户菜单树失败 - Token无效");
            return new ArrayList<>();
        }

        // 获取用户有权限的菜单
        List<Menu> userMenus = menuRepository.findUserMenuTree(userId);
        List<Map<String, Object>> menuTree = buildMenuTree(userMenus);
        
        long endTime = System.currentTimeMillis();
        permissionMonitorUtil.logMenuAccess(userId.toString(), "菜单树查询", userMenus.size());
        logger.info("获取用户菜单树成功: userId={}, menuCount={}, time={}ms", 
                   userId, userMenus.size(), (endTime - startTime));
        
        return menuTree;
    }

    @Override
    public List<Map<String, Object>> getUserMenuList(String token) {
        Long userId = tokenStorage.get(token);
        if (userId == null) {
            return new ArrayList<>();
        }

        // 获取用户有权限的菜单
        List<Menu> userMenus = menuRepository.findUserMenuTree(userId);
        return convertMenuToMapList(userMenus);
    }

    public boolean validateUserPermission(Long userId, String permissionCode) {
        long startTime = System.currentTimeMillis();
        
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            PermissionMonitorUtil.logPermissionDenied(userId.toString(), permissionCode, "用户不存在");
            logger.warn("权限验证失败 - 用户不存在: userId={}, permission={}", userId, permissionCode);
            return false;
        }
        User user = userOpt.get();
        
        // 超级管理员拥有所有权限
        if (user.getRole() != null && user.getRole().isSuperAdmin()) {
            long endTime = System.currentTimeMillis();
            PermissionMonitorUtil.logPermissionAllowed(userId.toString(), permissionCode, "超级管理员权限");
            logger.debug("权限验证通过 - 超级管理员: userId={}, permission={}, time={}ms", 
                       userId, permissionCode, (endTime - startTime));
            return true;
        }
        
        boolean hasPermission = false;
        String grantMethod = "";
        
        // 如果用户有自定义权限，优先使用自定义权限
        if (user.getPermissions() != null && !user.getPermissions().isEmpty()) {
            hasPermission = user.getPermissions().contains(permissionCode);
            grantMethod = "用户自定义权限";
        } else if (user.getRole() != null && user.getRole().getPermissions() != null) {
            // 否则使用角色权限
            hasPermission = user.getRole().getPermissions().contains(permissionCode);
            grantMethod = "角色权限";
        }
        
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        
        if (hasPermission) {
            PermissionMonitorUtil.logPermissionAllowed(userId.toString(), permissionCode, grantMethod);
            logger.debug("权限验证通过 - {}: userId={}, permission={}, time={}ms", 
                       grantMethod, userId, permissionCode, duration);
        } else {
            PermissionMonitorUtil.logPermissionDenied(userId.toString(), permissionCode, "权限不足");
            logger.warn("权限验证失败 - 权限不足: userId={}, permission={}, time={}ms", 
                      userId, permissionCode, duration);
        }
        
        return hasPermission;
    }

    @Override
    public List<String> getUserPermissionCodes(String token) {
        Long userId = tokenStorage.get(token);
        if (userId == null) {
            return new ArrayList<>();
        }

        Set<String> permissionCodes = menuRepository.findUserPermissionCodes(userId);
        return new ArrayList<>(permissionCodes);
    }

    // ========== 菜单权限辅助方法 ==========

    /**
     * 构建菜单树形结构
     */
    private List<Map<String, Object>> buildMenuTree(List<Menu> menus) {
        if (menus == null || menus.isEmpty()) {
            return new ArrayList<>();
        }

        // 按父菜单ID分组
        Map<Long, List<Map<String, Object>>> childrenMap = new HashMap<>();
        List<Map<String, Object>> menuTree = new ArrayList<>();

        // 先转换所有菜单为Map格式
        for (Menu menu : menus) {
            Map<String, Object> menuMap = convertMenuToMap(menu);
            
            Long parentId = menu.getParentId();
            if (parentId == null || parentId == 0) {
                menuTree.add(menuMap);
            } else {
                childrenMap.computeIfAbsent(parentId, k -> new ArrayList<>()).add(menuMap);
            }
        }

        // 为每个顶层菜单设置子菜单
        for (Map<String, Object> menuMap : menuTree) {
            Long menuId = (Long) menuMap.get("id");
            List<Map<String, Object>> children = childrenMap.get(menuId);
            if (children != null) {
                menuMap.put("children", children);
            }
        }

        return menuTree;
    }

    /**
     * 将菜单列表转换为Map列表
     */
    private List<Map<String, Object>> convertMenuToMapList(List<Menu> menus) {
        List<Map<String, Object>> menuList = new ArrayList<>();
        for (Menu menu : menus) {
            menuList.add(convertMenuToMap(menu));
        }
        return menuList;
    }

    /**
     * 将单个菜单转换为Map格式
     */
    private Map<String, Object> convertMenuToMap(Menu menu) {
        Map<String, Object> menuMap = new HashMap<>();
        menuMap.put("id", menu.getId());
        menuMap.put("menuName", menu.getMenuName());
        menuMap.put("menuType", menu.getMenuType());
        menuMap.put("parentId", menu.getParentId());
        menuMap.put("path", menu.getPath());
        menuMap.put("component", menu.getComponent());
        menuMap.put("permissionCode", menu.getPermissionCode());
        menuMap.put("icon", menu.getIcon());
        menuMap.put("sortOrder", menu.getSortOrder());
        menuMap.put("visible", menu.getVisible());
        menuMap.put("status", menu.getStatus());
        menuMap.put("createTime", menu.getCreateTime());
        return menuMap;
    }

    /**
     * 根据用户ID获取用户有权限的菜单
     */
    private List<Menu> getMenusByUserId(Long userId) {
        return menuRepository.findUserMenuTree(userId);
    }
    
    @Override
    public boolean checkMenuPermission(String token, String menuCode) {
        String userId = getUsernameFromToken(token);
        PermissionMonitorUtil.logMenuAccess(userId, "menu_permission_check", 1);
        
        List<Menu> userMenus = getMenusByUserId(Long.valueOf(userId));
        for (Menu menu : userMenus) {
            if (menu.getPermissionCode() != null && menu.getPermissionCode().equals(menuCode)) {
                PermissionMonitorUtil.logPermissionAllowed(userId, menuCode, "menu_check");
                return true;
            }
        }
        PermissionMonitorUtil.logPermissionDenied(userId, menuCode, "menu_check");
        return false;
    }
}
