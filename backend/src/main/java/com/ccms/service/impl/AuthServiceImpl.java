package com.ccms.service.impl;

import com.ccms.entity.system.user.SysUser;
import com.ccms.repository.system.user.SysUserRepository;
import com.ccms.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

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

    @Override
    public Object login(String username, String password) {
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
        
        // 生成令牌
        String token = generateToken(user);
        
        // 返回登录响应
        Map<String, Object> response = new HashMap<>();
        response.put("token", token);
        response.put("user", constructUserProfile(username));
        
        return response;
    }
    
    @Override
    public void logout(String token) {
        tokenStorage.remove(token);
    }
    
    @Override
    public boolean hasPermission(Long userId, String permissionCode) {
        // 简化实现
        SysUser user = sysUserRepository.findById(userId).orElse(null);
        if (user == null) {
            return false;
        }
        
        if ("admin".equals(user.getUsername())) {
            return true;
        }
        
        // 基本权限检查
        return true; // 简化处理
    }
    
    @Override
    public List<String> getUserRoles(Long userId) {
        List<String> roles = new ArrayList<>();
        SysUser user = sysUserRepository.findById(userId).orElse(null);
        if (user != null) {
            if ("admin".equals(user.getUsername())) {
                roles.add("ADMIN");
            } else {
                roles.add("USER");
            }
        }
        return roles;
    }
    
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
        if (userId != null) {
            return sysUserRepository.findById(userId).orElse(null);
        }
        return null;
    }
    
    @Override
    public boolean changePassword(String token, String oldPassword, String newPassword) {
        Long userId = getUserIdFromToken(token);
        if (userId == null) {
            return false;
        }
        
        SysUser user = sysUserRepository.findById(userId).orElse(null);
        if (user == null) {
            return false;
        }
        
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            return false;
        }
        
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setUpdateTime(LocalDateTime.now());
        user.setUpdateBy(userId);
        sysUserRepository.save(user);
        
        return true;
    }
    
    @Override
    public String resetPassword(Long userId) {
        SysUser user = sysUserRepository.findById(userId).orElse(null);
        if (user == null) {
            return null;
        }
        
        String newPassword = generateRandomPassword();
        user.setPassword(passwordEncoder.encode(newPassword));
        sysUserRepository.save(user);
        
        return newPassword;
    }
    
    @Override
    public boolean isUsernameExist(String username) {
        return sysUserRepository.findByUsername(username).isPresent();
    }
    
    @Override
    public SysUser register(SysUser user, List<Long> roleIds) {
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
        if (user.getStatus() == null) {
            user.setStatus(1);
        }
        
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());
        
        return sysUserRepository.save(user);
    }
    
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
    public org.springframework.data.domain.Page<SysUser> getUserList(int page, int size, String username, Long deptId) {
        // 简化实现 - 返回空页面
        return org.springframework.data.domain.Page.empty();
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
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());
        return sysUserRepository.save(user);
    }
    
    @Override
    public SysUser updateUser(SysUser user) {
        user.setUpdateTime(LocalDateTime.now());
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
            user.setUpdateTime(LocalDateTime.now());
            sysUserRepository.save(user);
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
        long totalUsers = sysUserRepository.count();
        long activeUsers = sysUserRepository.countByStatus(1);
        
        stats.put("totalUsers", totalUsers);
        stats.put("activeUsers", activeUsers);
        stats.put("inactiveUsers", totalUsers - activeUsers);
        
        return stats;
    }
    
    @Override
    public SysUser loadUserByUsername(String username) {
        return sysUserRepository.findByUsername(username).orElse(null);
    }
    
    @Override
    public String getUsernameFromToken(String token) {
        Long userId = tokenStorage.get(token);
        if (userId != null) {
            SysUser user = sysUserRepository.findById(userId).orElse(null);
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
        SysUser user = sysUserRepository.findById(userId).orElse(null);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        
        String newToken = generateToken(user);
        return newToken;
    }
    
    @Override
    public Map<String, Object> getUserPermissions(String token) {
        Map<String, Object> permissions = new HashMap<>();
        
        Long userId = tokenStorage.get(token);
        if (userId == null) {
            permissions.put("permissions", new String[0]);
            permissions.put("role", "GUEST");
            return permissions;
        }
        
        SysUser user = sysUserRepository.findById(userId).orElse(null);
        if (user == null) {
            permissions.put("permissions", new String[0]);
            permissions.put("role", "GUEST");
            return permissions;
        }
        
        if ("admin".equals(user.getUsername())) {
            permissions.put("permissions", new String[]{
                "user:read", "user:write", "user:delete", 
                "expense:read", "expense:write", "expense:approve",
                "budget:read", "budget:write", "budget:approve"
            });
            permissions.put("role", "ADMIN");
        } else {
            permissions.put("permissions", new String[]{
                "expense:read", "expense:write",
                "budget:read"
            });
            permissions.put("role", "USER");
        }
        
        permissions.put("userId", userId);
        permissions.put("username", user.getUsername());
        
        return permissions;
    }
    
@Override 
    public Map<String, Object> getUserProfileByToken(String tokenParam) {
        Long userId = tokenStorage.get(tokenParam);
        if (userId == null) {
            return null;
        }
        
        SysUser user = sysUserRepository.findById(userId).orElse(null);
        if (user == null) {
            return null;
        }
        
        Map<String, Object> profile = new HashMap<>();
        profile.put("id", user.getId());
        profile.put("username", user.getUsername());
        profile.put("realName", user.getUserName());
        profile.put("email", user.getEmail());
        profile.put("phone", user.getMobile());
        profile.put("status", user.getStatus());
        profile.put("createTime", user.getCreateTime());
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
     * 构建用户档案信息 (私有方法，用于替换删除的 getUserProfile)
     */
    private Map<String, Object> constructUserProfile(String username) {
        SysUser user = sysUserRepository.findByUsername(username).orElse(null);
        if (user == null) {
            return null;
        }
        
        Map<String, Object> profile = new HashMap<>();
        profile.put("id", user.getId());
        profile.put("username", user.getUsername());
        profile.put("realName", user.getUserName());
        profile.put("email", user.getEmail());
        profile.put("phone", user.getMobile());
        profile.put("status", user.getStatus());
        profile.put("createTime", user.getCreateTime());
        profile.put("lastLoginTime", user.getLastLoginTime());
        
        return profile;
    }
}