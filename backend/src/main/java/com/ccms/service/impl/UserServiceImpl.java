package com.ccms.service.impl;

import com.ccms.entity.system.user.User;
import com.ccms.repository.system.user.UserRepository;
import com.ccms.service.UserService;
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
    private final PasswordEncoder passwordEncoder;

    // 简单令牌存储（实际项目中应使用Redis等持久化存储）
    private final Map<String, Long> tokenStorage = new java.util.concurrent.ConcurrentHashMap<>();

    @Autowired
    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    @Override
    public Object login(String username, String password) {
        // 根据用户名查找用户
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isEmpty()) {
            return null;
        }
        User user = userOpt.get();

        // 检查用户状态
        if (!user.isEnabled()) {
            return null;
        }

        // 验证密码
        if (!passwordEncoder.matches(password, user.getPassword())) {
            return null;
        }

        // 生成令牌
        String token = generateToken(user);

        // 更新登录信息
        user.setLastLoginTime(LocalDateTime.now());
        user.setLoginCount(user.getLoginCount() + 1);
        userRepository.save(user);

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
        try {
            String username = getUsernameFromToken(token);
            if (username == null) {
                return false;
            }
            User user = loadUserByUsername(username);
            if (user == null) {
                return false;
            }
            return hasPermission(user.getId(), permission);
        } catch (Exception e) {
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
        Map<String, Object> permissions = new HashMap<>();

        Long userId = tokenStorage.get(token);
        if (userId == null) {
            permissions.put("permissions", new String[0]);
            permissions.put("role", "GUEST");
            return permissions;
        }

        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            permissions.put("permissions", new String[0]);
            permissions.put("role", "GUEST");
            return permissions;
        }

        permissions.put("permissions", user.getPermissions());
        permissions.put("roles", user.getRoleCodes());
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
}
