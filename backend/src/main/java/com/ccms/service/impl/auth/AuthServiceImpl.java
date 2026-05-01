package com.ccms.service.impl.auth;

import com.ccms.entity.system.Department;
import com.ccms.entity.system.User;
import com.ccms.repository.system.UserRepository;
import com.ccms.service.auth.AuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

/**
 * 权限认证服务实现类
 */
@Service
@Transactional
public class AuthServiceImpl implements AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthServiceImpl.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public AuthServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElse(null);
    }

    @Override
    public boolean validatePassword(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }

    @Override
    public Set<String> getUserPermissions(Long userId) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            return Collections.emptySet();
        }
        
        User user = userOpt.get();
        Set<String> permissions = new HashSet<>();
        
        // 从用户角色中获取所有权限
        user.getRoles().forEach(role -> {
            permissions.addAll(role.getPermissions());
        });
        
        return permissions;
    }

    @Override
    public Set<String> getUserRoles(Long userId) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            return Collections.emptySet();
        }
        
        User user = userOpt.get();
        Set<String> roles = new HashSet<>();
        
        user.getRoles().forEach(role -> {
            roles.add(role.getRoleCode());
        });
        
        return roles;
    }

    @Override
    public boolean hasPermission(Long userId, String permission) {
        Set<String> permissions = getUserPermissions(userId);
        return permissions.contains(permission);
    }

    @Override
    public boolean hasRole(Long userId, String roleCode) {
        Set<String> roles = getUserRoles(userId);
        return roles.contains(roleCode);
    }

    @Override
    public List<Long> getUserDataScope(Long userId) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            return Collections.emptyList();
        }
        
        User user = userOpt.get();
        List<Long> deptIds = new ArrayList<>();
        
        // 如果用户关联了部门，添加部门ID
        if (user.getDepartment() != null) {
            deptIds.add(user.getDepartment().getId());
            
            // 如果是部门负责人，可能包含子部门
            if (isDepartmentHead(user)) {
                // 这里需要递归获取子部门ID
                // 简化实现：返回当前部门ID
                deptIds.addAll(getSubDepartmentIds(user.getDepartment()));
            }
        }
        
        // 根据角色数据范围添加更多的部门ID
        user.getRoles().forEach(role -> {
            switch (role.getDataScope()) {
                case 1: // 全部数据
                    deptIds.clear();
                    deptIds.add(-1L); // 特殊标记表示所有数据
                    break;
                case 2: // 本部门数据
                    if (user.getDepartment() != null) {
                        deptIds.add(user.getDepartment().getId());
                    }
                    break;
                case 3: // 本部门及以下数据
                    if (user.getDepartment() != null) {
                        deptIds.addAll(getSubDepartmentIds(user.getDepartment()));
                    }
                    break;
            }
        });
        
        return deptIds;
    }

    @Override
    public boolean canAccessDepartment(Long userId, Long departmentId) {
        List<Long> accessibleDepts = getUserDataScope(userId);
        
        // 如果包含-1（所有数据），则允许访问
        if (accessibleDepts.contains(-1L)) {
            return true;
        }
        
        return accessibleDepts.contains(departmentId);
    }

    @Override
    public boolean canAccessModule(Long userId, String moduleCode) {
        Set<String> permissions = getUserPermissions(userId);
        
        // 模块权限格式：module:{moduleCode}:access
        String modulePermission = "module:" + moduleCode + ":access";
        
        return permissions.contains(modulePermission) || 
               permissions.contains("*:*:*") || // 超级权限
               permissions.contains("module:*:access"); // 所有模块权限
    }

    @Override
    public boolean verifyAccess(Long userId, String resourceCode, String operation) {
        Set<String> permissions = getUserPermissions(userId);
        
        // 权限格式：{resourceCode}:{operation}
        String specificPermission = resourceCode + ":" + operation;
        String catchAllPermission = resourceCode + ":*";
        String wildcardPermission = "*:*";
        
        return permissions.contains(specificPermission) ||
               permissions.contains(catchAllPermission) ||
               permissions.contains(wildcardPermission);
    }

    @Override
    public String generateAccessToken(User user) {
        // 简化实现：生成基于时间的令牌
        // 实际生产环境应该使用JWT等方式
        String token = Base64.getEncoder().encodeToString(
            (user.getUsername() + ":" + System.currentTimeMillis()).getBytes()
        );
        
        // 记录登录日志
        recordLoginLog(user.getId(), "127.0.0.1", true, "用户登录成功");
        
        return token;
    }

    @Override
    public boolean validateAccessToken(String token) {
        // 简化实现：检查令牌格式和过期时间
        try {
            String decoded = new String(Base64.getDecoder().decode(token));
            String[] parts = decoded.split(":");
            if (parts.length == 2) {
                long timestamp = Long.parseLong(parts[1]);
                // 检查是否在24小时内
                return System.currentTimeMillis() - timestamp < 24 * 60 * 60 * 1000;
            }
        } catch (Exception e) {
            logger.warn("令牌验证失败: {}", e.getMessage());
        }
        return false;
    }

    @Override
    public User getUserByToken(String token) {
        if (!validateAccessToken(token)) {
            return null;
        }
        
        try {
            String decoded = new String(Base64.getDecoder().decode(token));
            String[] parts = decoded.split(":");
            if (parts.length == 2) {
                return findByUsername(parts[0]);
            }
        } catch (Exception e) {
            logger.warn("从令牌获取用户失败: {}", e.getMessage());
        }
        
        return null;
    }

    @Override
    public String refreshAccessToken(String oldToken) {
        User user = getUserByToken(oldToken);
        if (user != null) {
            return generateAccessToken(user);
        }
        return null;
    }

    @Override
    public void recordLoginLog(Long userId, String ipAddress, boolean success, String message) {
        // 简化实现：记录到控制台
        logger.info("用户登录日志 - 用户ID: {}, IP: {}, 结果: {}, 消息: {}", 
                   userId, ipAddress, success ? "成功" : "失败", message);
    }

    @Override
    public void recordOperationLog(Long userId, String operation, String resource, String description) {
        // 简化实现：记录到控制台
        logger.info("操作日志 - 用户ID: {}, 操作: {}, 资源: {}, 描述: {}", 
                   userId, operation, resource, description);
    }

    /**
     * 检查用户是否为部门负责人
     */
    private boolean isDepartmentHead(User user) {
        // 简化实现：通过职位名称判断
        String position = user.getPosition();
        return position != null && (
            position.contains("总监") || 
            position.contains("经理") || 
            position.contains("主管")
        );
    }

    /**
     * 获取子部门ID列表（递归实现）
     */
    private List<Long> getSubDepartmentIds(Department department) {
        List<Long> deptIds = new ArrayList<>();
        deptIds.add(department.getId());
        
        // 实际实现需要查询数据库获取子部门
        // 这里简化处理，只返回当前部门ID
        
        return deptIds;
    }
}