import { describe, it, expect, beforeEach, vi, afterEach } from 'vitest';
import axios from 'axios';
import { 
  login, 
  logout, 
  getCurrentUser, 
  refreshToken, 
  changePassword 
} from '../../api/auth';

// Mock axios
vi.mock('axios');
const mockedAxios = vi.mocked(axios);

describe('Auth API', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    
    // Mock localStorage
    global.localStorage = {
      getItem: vi.fn(),
      setItem: vi.fn(),
      removeItem: vi.fn(),
      clear: vi.fn(),
      length: 0,
      key: vi.fn()
    } as any;
  });

  afterEach(() => {
    vi.restoreAllMocks();
  });

  describe('login', () => {
    it('should login successfully with valid credentials', async () => {
      const mockResponse = {
        data: {
          token: 'mock-jwt-token',
          user: {
            id: 1,
            username: 'testuser',
            name: '测试用户',
            role: 'USER'
          }
        }
      };
      
      mockedAxios.post.mockResolvedValue(mockResponse);
      
      const result = await login('testuser', 'password123');
      
      expect(mockedAxios.post).toHaveBeenCalledWith('/api/auth/login', {
        username: 'testuser',
        password: 'password123'
      });
      
      expect(result).toEqual(mockResponse.data);
    });

    it('should handle login failure with invalid credentials', async () => {
      const mockError = {
        response: {
          status: 401,
          data: {
            message: '用户名或密码错误'
          }
        }
      };
      
      mockedAxios.post.mockRejectedValue(mockError);
      
      await expect(login('wronguser', 'wrongpass')).rejects.toThrow('用户名或密码错误');
      
      expect(mockedAxios.post).toHaveBeenCalledWith('/api/auth/login', {
        username: 'wronguser',
        password: 'wrongpass'
      });
    });

    it('should handle network error during login', async () => {
      mockedAxios.post.mockRejectedValue(new Error('Network Error'));
      
      await expect(login('testuser', 'password123')).rejects.toThrow('网络错误，请检查网络连接');
    });

    it('should handle server error during login', async () => {
      const mockError = {
        response: {
          status: 500,
          data: {
            message: '服务器内部错误'
          }
        }
      };
      
      mockedAxios.post.mockRejectedValue(mockError);
      
      await expect(login('testuser', 'password123')).rejects.toThrow('服务器内部错误');
    });

    it('should handle timeout during login', async () => {
      mockedAxios.post.mockRejectedValue({ 
        code: 'ECONNABORTED', 
        message: 'timeout of 5000ms exceeded' 
      });
      
      await expect(login('testuser', 'password123')).rejects.toThrow('请求超时，请稍后重试');
    });

    it('should validate input parameters', async () => {
      await expect(login('', 'password123')).rejects.toThrow('用户名和密码不能为空');
      await expect(login('testuser', '')).rejects.toThrow('用户名和密码不能为空');
      await expect(login('', '')).rejects.toThrow('用户名和密码不能为空');
      
      expect(mockedAxios.post).not.toHaveBeenCalled();
    });
  });

  describe('logout', () => {
    it('should logout successfully', async () => {
      mockedAxios.post.mockResolvedValue({ data: { success: true } });
      
      const result = await logout();
      
      expect(mockedAxios.post).toHaveBeenCalledWith('/api/auth/logout');
      expect(result).toEqual({ success: true });
    });

    it('should handle logout failure', async () => {
      mockedAxios.post.mockRejectedValue(new Error('Logout failed'));
      
      await expect(logout()).rejects.toThrow('Logout failed');
    });

    it('should clear local storage on logout', async () => {
      mockedAxios.post.mockResolvedValue({ data: { success: true } });
      
      await logout();
      
      expect(localStorage.removeItem).toHaveBeenCalledWith('token');
      expect(localStorage.removeItem).toHaveBeenCalledWith('userInfo');
    });
  });

  describe('getCurrentUser', () => {
    it('should get current user info successfully', async () => {
      const mockUser = {
        id: 1,
        username: 'testuser',
        name: '测试用户',
        role: 'USER',
        permissions: ['budget:view', 'expense:create']
      };
      
      mockedAxios.get.mockResolvedValue({ data: mockUser });
      
      const result = await getCurrentUser();
      
      expect(mockedAxios.get).toHaveBeenCalledWith('/api/auth/me');
      expect(result).toEqual(mockUser);
    });

    it('should handle unauthorized access', async () => {
      const mockError = {
        response: {
          status: 401,
          data: {
            message: '认证失败'
          }
        }
      };
      
      mockedAxios.get.mockRejectedValue(mockError);
      
      await expect(getCurrentUser()).rejects.toThrow('认证失败');
    });

    it('should handle user not found', async () => {
      const mockError = {
        response: {
          status: 404,
          data: {
            message: '用户不存在'
          }
        }
      };
      
      mockedAxios.get.mockRejectedValue(mockError);
      
      await expect(getCurrentUser()).rejects.toThrow('用户不存在');
    });
  });

  describe('refreshToken', () => {
    it('should refresh token successfully', async () => {
      const mockResponse = {
        data: {
          token: 'new-refreshed-token',
          refreshToken: 'new-refresh-token'
        }
      };
      
      mockedAxios.post.mockResolvedValue(mockResponse);
      
      const result = await refreshToken('old-refresh-token');
      
      expect(mockedAxios.post).toHaveBeenCalledWith('/api/auth/refresh', {
        refreshToken: 'old-refresh-token'
      });
      expect(result).toEqual(mockResponse.data);
    });

    it('should handle invalid refresh token', async () => {
      const mockError = {
        response: {
          status: 400,
          data: {
            message: '无效的刷新令牌'
          }
        }
      };
      
      mockedAxios.post.mockRejectedValue(mockError);
      
      await expect(refreshToken('invalid-token')).rejects.toThrow('无效的刷新令牌');
    });

    it('should handle expired refresh token', async () => {
      const mockError = {
        response: {
          status: 401,
          data: {
            message: '刷新令牌已过期'
          }
        }
      };
      
      mockedAxios.post.mockRejectedValue(mockError);
      
      await expect(refreshToken('expired-token')).rejects.toThrow('刷新令牌已过期');
    });

    it('should validate refresh token parameter', async () => {
      await expect(refreshToken('')).rejects.toThrow('刷新令牌不能为空');
      await expect(refreshToken(null as any)).rejects.toThrow('刷新令牌不能为空');
      
      expect(mockedAxios.post).not.toHaveBeenCalled();
    });
  });

  describe('changePassword', () => {
    it('should change password successfully', async () => {
      mockedAxios.post.mockResolvedValue({ data: { success: true } });
      
      const result = await changePassword('oldpass', 'newpass');
      
      expect(mockedAxios.post).toHaveBeenCalledWith('/api/auth/change-password', {
        oldPassword: 'oldpass',
        newPassword: 'newpass'
      });
      expect(result).toEqual({ success: true });
    });

    it('should handle wrong old password', async () => {
      const mockError = {
        response: {
          status: 400,
          data: {
            message: '原密码不正确'
          }
        }
      };
      
      mockedAxios.post.mockRejectedValue(mockError);
      
      await expect(changePassword('wrongold', 'newpass')).rejects.toThrow('原密码不正确');
    });

    it('should handle password policy violation', async () => {
      const mockError = {
        response: {
          status: 422,
          data: {
            message: '新密码不符合安全要求'
          }
        }
      };
      
      mockedAxios.post.mockRejectedValue(mockError);
      
      await expect(changePassword('oldpass', 'weak')).rejects.toThrow('新密码不符合安全要求');
    });

    it('should validate password parameters', async () => {
      await expect(changePassword('', 'newpass')).rejects.toThrow('原密码和新密码不能为空');
      await expect(changePassword('oldpass', '')).rejects.toThrow('原密码和新密码不能为空');
      await expect(changePassword('oldpass', 'short')).rejects.toThrow('新密码长度不能少于6位');
      
      expect(mockedAxios.post).not.toHaveBeenCalled();
    });

    it('should prevent using the same password', async () => {
      await expect(changePassword('samepass', 'samepass')).rejects.toThrow('新密码不能与原密码相同');
      
      expect(mockedAxios.post).not.toHaveBeenCalled();
    });
  });

  describe('Error Handling Edge Cases', () => {
    it('should handle malformed response data', async () => {
      mockedAxios.post.mockResolvedValue({ 
        data: null // Malformed response
      });
      
      await expect(login('testuser', 'password123')).rejects.toThrow('服务器返回数据格式错误');
    });

    it('should handle response without expected fields', async () => {
      mockedAxios.post.mockResolvedValue({ 
        data: { success: true } // Missing token field
      });
      
      await expect(login('testuser', 'password123')).rejects.toThrow('服务器返回数据格式错误');
    });

    it('should handle unexpected error structure', async () => {
      mockedAxios.post.mockRejectedValue({
        // Missing response property
        message: 'Unexpected error'
      });
      
      await expect(login('testuser', 'password123')).rejects.toThrow('Unexpected error');
    });

    it('should handle CORS error', async () => {
      mockedAxios.post.mockRejectedValue({
        message: 'Network Error',
        code: 'ERR_NETWORK'
      });
      
      await expect(login('testuser', 'password123')).rejects.toThrow('网络错误，请检查网络连接');
    });

    it('should handle request cancellation', async () => {
      mockedAxios.post.mockRejectedValue({
        message: 'Request cancelled',
        code: 'ERR_CANCELED'
      });
      
      await expect(login('testuser', 'password123')).rejects.toThrow('请求已被取消');
    });
  });

  describe('Authentication Headers', () => {
    it('should include authorization header for authenticated requests', async () => {
      // Set up a mock token in localStorage
      (localStorage.getItem as any).mockReturnValue('mock-token');
      
      mockedAxios.get.mockResolvedValue({ data: {} });
      
      await getCurrentUser();
      
      expect(mockedAxios.get).toHaveBeenCalledWith('/api/auth/me', {
        headers: {
          'Authorization': 'Bearer mock-token'
        }
      });
    });

    it('should handle missing token gracefully', async () => {
      (localStorage.getItem as any).mockReturnValue(null);
      
      mockedAxios.get.mockResolvedValue({ data: {} });
      
      await getCurrentUser();
      
      // Should still make the request without authorization header
      expect(mockedAxios.get).toHaveBeenCalledWith('/api/auth/me', {
        headers: {}
      });
    });
  });

  describe('Performance and Security', () => {
    it('should not log sensitive information', async () => {
      const consoleSpy = vi.spyOn(console, 'error');
      
      mockedAxios.post.mockRejectedValue(new Error('Login failed'));
      
      try {
        await login('testuser', 'sensitivepassword');
      } catch (error) {
        // Error should not contain password
        expect(consoleSpy).not.toHaveBeenCalledWith(expect.stringContaining('sensitivepassword'));
      }
      
      consoleSpy.mockRestore();
    });

    it('should handle concurrent requests correctly', async () => {
      mockedAxios.post.mockResolvedValue({ 
        data: { token: 'token', user: {} } 
      });
      
      // Make multiple concurrent login requests
      const requests = [
        login('user1', 'pass1'),
        login('user2', 'pass2'),
        login('user3', 'pass3')
      ];
      
      const results = await Promise.all(requests);
      
      expect(results).toHaveLength(3);
      expect(mockedAxios.post).toHaveBeenCalledTimes(3);
    });

    it('should enforce request timeout', async () => {
      // Mock axios instance with timeout
      const axiosInstance = {
        post: vi.fn()
      };
      
      // Simulate timeout
      axiosInstance.post.mockImplementation(() => 
        new Promise((_, reject) => 
          setTimeout(() => reject({ code: 'ECONNABORTED' }), 6000)
        )
      );
      
      // Replace the mocked axios with our instance
      vi.mocked(axios).mockReturnValue(axiosInstance as any);
      
      await expect(login('testuser', 'password123')).rejects.toBeDefined();
    });
  });

  describe('Input Validation Edge Cases', () => {
    it('should handle extremely long usernames and passwords', async () => {
      const longUsername = 'a'.repeat(1000);
      const longPassword = 'b'.repeat(1000);
      
      await expect(login(longUsername, longPassword)).rejects.toThrow('用户名长度不能超过100个字符');
    });

    it('should handle special characters in credentials', async () => {
      const specialUsername = 'user@domain.com';
      const specialPassword = 'p@ssw0rd!#$%';
      
      mockedAxios.post.mockResolvedValue({ 
        data: { token: 'token', user: {} } 
      });
      
      await login(specialUsername, specialPassword);
      
      expect(mockedAxios.post).toHaveBeenCalledWith('/api/auth/login', {
        username: specialUsername,
        password: specialPassword
      });
    });

    it('should handle unicode characters in credentials', async () => {
      const unicodeUsername = '用户名字';
      const unicodePassword = '密码123';
      
      mockedAxios.post.mockResolvedValue({ 
        data: { token: 'token', user: {} } 
      });
      
      await login(unicodeUsername, unicodePassword);
      
      expect(mockedAxios.post).toHaveBeenCalledWith('/api/auth/login', {
        username: unicodeUsername,
        password: unicodePassword
      });
    });

    it('should handle SQL injection attempts', async () => {
      const sqlInjectionUsername = "admin' OR '1'='1' --";
      const sqlInjectionPassword = "password";
      
      mockedAxios.post.mockRejectedValue({
        response: {
          status: 400,
          data: { message: '无效的用户名格式' }
        }
      });
      
      await expect(login(sqlInjectionUsername, sqlInjectionPassword)).rejects.toThrow('无效的用户名格式');
    });
  });
});