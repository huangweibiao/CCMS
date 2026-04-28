package com.ccms.service.impl;

import com.ccms.entity.system.SysUser;
import com.ccms.repository.system.SysUserRepository;
import com.ccms.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.Map;

/**
 * 用户认证与权限服务实现类
 * 
 * @author 系统生成
 */
@Service
public class AuthServiceImpl implements AuthService {

    private final SysUserRepository sysUserRepository;
    private final PasswordEncoder passwordEncoder;
    
    // 简单令牌存储（实际项目中应使用Redis等持久化存储）
    private final Map<String, Long> tokenStorage = new java.util.concurrent.ConcurrentHashMap<>();

    @Autowired
    public AuthServiceImpl(SysUserRepository sysUserRepository) {
        this.sysUserRepository = sysUserRepository;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    /**
     * 用户登录认证
     * 验证用户名和密码的正确性，并返回认证成功的用户对象
     * 
     * @param username 用户名
     * @param password 密码
     * @return 认证成功的用户对象，失败返回null
     * @throws RuntimeException 当数据库查询失败时抛出异常
     */
    @Override
    public SysUser login(String username, String password) {
        // 根据用户名查找用户
        Optional<SysUser> userOpt = sysUserRepository.findByUsername(username);
        if (userOpt.isEmpty()) {
            return null;
        }
        
        SysUser user = userOpt.get();
        
        // 检查用户状态
        if (user.getStatus() != 1) {
            return null;
        }
        
        // 验证密码
        if (!passwordEncoder.matches(password, user.getPassword())) {
            return null;
        }
        
        // 更新最后登录时间（实际项目中应在数据库更新）
        user.setLastLoginTime(java.time.LocalDateTime.now());
        
        return user;
    }

    /**
     * 用户登出
     * 从令牌存储中移除指定令牌，使该令牌失效
     * 
     * @param token 用户登录令牌
     */
    @Override
    public void logout(String token) {
        tokenStorage.remove(token);
    }

    /**
     * 检查用户是否具有特定权限
     * 验证指定用户ID是否拥有给定的权限代码
     * 
     * @param userId 用户ID
     * @param permissionCode 权限代码
     * @return true表示用户拥有该权限，false表示无权限
     */
    @Override
    public boolean hasPermission(Long userId, String permissionCode) {
        // 实现权限验证逻辑
        // 实际项目中应查询数据库验证用户权限
        
        // 临时实现：管理员拥有所有权限
        Optional<SysUser> userOpt = sysUserRepository.findById(userId);
        if (userOpt.isEmpty()) {
            return false;
        }
        
        SysUser user = userOpt.get();
        if ("admin".equals(user.getUsername())) {
            return true;
        }
        
        // 检查用户权限（简化的权限检查）
        return checkUserPermission(userId, permissionCode);
    }

    /**
     * 获取用户角色列表
     * 查询指定用户ID对应的角色信息
     * 
     * @param userId 用户ID
     * @return 用户角色列表，如果用户不存在返回空列表
     */
    @Override
    public List<String> getUserRoles(Long userId) {
        // 获取用户角色
        // 实际项目中应从数据库查询
        
        List<String> roles = new ArrayList<>();
        Optional<SysUser> userOpt = sysUserRepository.findById(userId);
        if (userOpt.isEmpty()) {
            return roles;
        }
        
        SysUser user = userOpt.get();
        if ("admin".equals(user.getUsername())) {
            roles.add("ADMIN");
        } else {
            roles.add("USER");
        }
        
        return roles;
    }

    /**
     * 生成用户登录令牌
     * 为用户生成唯一的登录令牌并存储在内存中
     * 
     * @param user 用户对象
     * @return 生成的令牌字符串
     */
    @Override
    public String generateToken(SysUser user) {
        String token = UUID.randomUUID().toString().replace("-", "");
        tokenStorage.put(token, user.getId());
        return token;
    }

    @Override
    public boolean validateToken(String token) {
        return tokenStorage.containsKey(token);
    }

    @Override
    public SysUser getUserByToken(String token) {
        Long userId = tokenStorage.get(token);
        if (userId == null) {
            return null;
        }
        return sysUserRepository.findById(userId).orElse(null);
    }

    @Override
    public boolean changePassword(Long userId, String oldPassword, String newPassword) {
        Optional<SysUser> userOpt = sysUserRepository.findById(userId);
        if (userOpt.isEmpty()) {
            return false;
        }
        
        SysUser user = userOpt.get();
        
        // 验证旧密码
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            return false;
        }
        
        // 更新密码
        user.setPassword(passwordEncoder.encode(newPassword));
        sysUserRepository.save(user);
        
        return true;
    }

    @Override
    public String resetPassword(Long userId) {
        Optional<SysUser> userOpt = sysUserRepository.findById(userId);
        if (userOpt.isEmpty()) {
            return null;
        }
        
        // 生成随机密码
        String newPassword = generateRandomPassword();
        SysUser user = userOpt.get();
        user.setPassword(passwordEncoder.encode(newPassword));
        sysUserRepository.save(user);
        
        return newPassword;
    }

    @Override
    public boolean isUsernameExist(String username) {
        return sysUserRepository.existsByUsername(username);
    }

    @Override
    public SysUser register(SysUser user, List<Long> roleIds) {
        // 检查用户名是否已存在
        if (isUsernameExist(user.getUsername())) {
            throw new RuntimeException("用户名已存在");
        }
        
        // 设置默认密码
        if (user.getPassword() == null || user.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode("123456")); // 默认密码
        } else {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }
        
        // 设置默认状态
        if (user.getStatus() == null) {
            user.setStatus(1); // 启用状态
        }
        
        // 保存用户
        SysUser savedUser = sysUserRepository.save(user);
        
        return savedUser;
    }
    
    /**
     * 检查用户权限（简化实现）
     */
    private boolean checkUserPermission(Long userId, String permissionCode) {
        // 实际项目中应查询数据库
        // 这里简化处理，检查常见的权限
        
        if (permissionCode.startsWith("READ_")) {
            return true;
        }
        
        if ("EXPENSE_APPLY".equals(permissionCode)) {
            return true;
        }
        
        return false;
    }
    
    /**
     * 生成随机密码
     */
    private String generateRandomPassword() {
        return UUID.randomUUID().toString().substring(0, 8);
    }
    
    // ========== 新增方法实现 ==========
    
    @Override
    public boolean checkPermission(String token, String permission) {
        try {
            String username = getUsernameFromToken(token);
            if (username == null) {
                return false;
            }
            SysUser user = loadUserByUsername(username);
            if (user == null) {
                return false;
            }
            return hasPermission(user.getId(), permission);
        } catch (Exception e) {
            return false;
        }
    }
    
    @Override
    public Object getUserList(int page, int size, String username, Long deptId) {
        // 简化实现 - 返回空列表
        return new ArrayList<SysUser>();
    }
    
    @Override
    public SysUser getUserById(Long userId) {
        return sysUserRepository.findById(userId).orElse(null);
    }
    
    @Override
    public SysUser createUser(SysUser user) {
        if (user.getPassword() == null || user.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode("123456"));
        } else {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }
        if (user.getStatus() == null) {
            user.setStatus(1);
        }
        return sysUserRepository.save(user);
    }
    
    @Override
    public SysUser updateUser(SysUser user) {
        return sysUserRepository.save(user);
    }
    
    @Override
    public boolean deleteUser(Long userId) {
        try {
            sysUserRepository.deleteById(userId);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    @Override
    public boolean updateUserStatus(Long userId, Integer status) {
        try {
            SysUser user = sysUserRepository.findById(userId).orElse(null);
            if (user == null) {
                return false;
            }
            user.setStatus(status);
            sysUserRepository.save(user);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    @Override
    public boolean assignUserRoles(Long userId, Long[] roleIds) {
        // 简化实现 - 角色分配逻辑
        return true;
    }
    
    @Override
    public Map<String, Object> getUserStatistics(Long deptId) {
        Map<String, Object> stats = new HashMap<>();
        long totalUsers = sysUserRepository.count();
        long activeUsers = sysUserRepository.countByStatus(1);
        
        stats.put("totalUsers", totalUsers);
        stats.put("activeUsers", activeUsers);
        stats.put("inactiveUsers", totalUsers - activeUsers);
        
        return stats;
    }
    
    @Override
    public SysUser loadUserByUsername(String username) {
        return sysUserRepository.findByUsername(username);
    }
    
    @Override
    public String getUsernameFromToken(String token) {
        // 简化实现 - 从内存中获取用户名
        Long userId = tokenStorage.get(token);
        if (userId != null) {
            SysUser user = sysUserRepository.findById(userId).orElse(null);
            if (user != null) {
                return user.getUsername();
            }
        }
        return null;
    }
}