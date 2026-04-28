package com.ccms.dto;

import lombok.Data;
import java.util.List;

@Data
public class LoginResponse {
    
    private String accessToken;
    
    private String refreshToken;
    
    private UserInfo userInfo;
    
    private Long expiresIn;
    
    @Data
    public static class UserInfo {
        private Long id;
        private String username;
        private String nickName;
        private String email;
        private String phone;
        private String avatar;
        private String deptName;
        private List<String> roles;
        private List<String> permissions;
    }
}