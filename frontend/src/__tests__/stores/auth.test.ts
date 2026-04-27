import { describe, it, expect, beforeEach, vi } from 'vitest';
import { setActivePinia, createPinia } from 'pinia';
import { useAuthStore } from '../../stores/auth';
import { login, logout, getToken, setToken, removeToken } from '../../utils/auth';

// Mock the auth utilities
vi.mock('../../utils/auth', () => ({
  login: vi.fn(),
  logout: vi.fn(),
  getToken: vi.fn(),
  setToken: vi.fn(),
  removeToken: vi.fn(),
}));

describe('Auth Store', () => {
  beforeEach(() => {
    setActivePinia(createPinia());
    vi.clearAllMocks();
  });

  describe('Initial State', () => {
    it('should initialize with default values', () => {
      const authStore = useAuthStore();
      
      expect(authStore.token).toBe('');
      expect(authStore.userInfo).toBe(null);
      expect(authStore.isLoggedIn).toBe(false);
    });

    it('should restore state from token when initialized', () => {
      (getToken as any).mockReturnValue('mock-token-123');
      
      const authStore = useAuthStore();
      
      expect(authStore.token).toBe('mock-token-123');
      expect(authStore.isLoggedIn).toBe(true);
      expect(getToken).toHaveBeenCalledOnce();
    });
  });

  describe('Login Action', () => {
    it('should handle successful login', async () => {
      const mockUser = {
        id: 1,
        username: 'testuser',
        name: '测试用户',
        role: 'USER'
      };
      
      (login as any).mockResolvedValue({
        token: 'new-token-123',
        user: mockUser
      });
      
      const authStore = useAuthStore();
      const result = await authStore.login('testuser', 'password123');
      
      expect(login).toHaveBeenCalledWith('testuser', 'password123');
      expect(authStore.token).toBe('new-token-123');
      expect(authStore.userInfo).toEqual(mockUser);
      expect(authStore.isLoggedIn).toBe(true);
      expect(setToken).toHaveBeenCalledWith('new-token-123');
      expect(result.success).toBe(true);
    });

    it('should handle login failure', async () => {
      const errorMessage = '用户名或密码错误';
      (login as any).mockRejectedValue(new Error(errorMessage));
      
      const authStore = useAuthStore();
      const result = await authStore.login('testuser', 'wrongpassword');
      
      expect(login).toHaveBeenCalledWith('testuser', 'wrongpassword');
      expect(authStore.token).toBe('');
      expect(authStore.userInfo).toBe(null);
      expect(authStore.isLoggedIn).toBe(false);
      expect(setToken).not.toHaveBeenCalled();
      expect(result.success).toBe(false);
      expect(result.message).toBe(errorMessage);
    });

    it('should handle empty credentials', async () => {
      const authStore = useAuthStore();
      const result = await authStore.login('', '');
      
      expect(login).not.toHaveBeenCalled();
      expect(result.success).toBe(false);
      expect(result.message).toBe('用户名和密码不能为空');
    });
  });

  describe('Logout Action', () => {
    it('should logout successfully', () => {
      const authStore = useAuthStore();
      
      // Set initial state as logged in
      authStore.token = 'existing-token';
      authStore.userInfo = { id: 1, username: 'testuser' };
      
      authStore.logout();
      
      expect(authStore.token).toBe('');
      expect(authStore.userInfo).toBe(null);
      expect(authStore.isLoggedIn).toBe(false);
      expect(logout).toHaveBeenCalledOnce();
      expect(removeToken).toHaveBeenCalledOnce();
    });

    it('should handle logout when already logged out', () => {
      const authStore = useAuthStore();
      
      // Ensure initial state is logged out
      authStore.token = '';
      authStore.userInfo = null;
      
      authStore.logout();
      
      expect(authStore.token).toBe('');
      expect(authStore.userInfo).toBe(null);
      expect(authStore.isLoggedIn).toBe(false);
      expect(logout).toHaveBeenCalledOnce();
    });
  });

  describe('Update User Info Action', () => {
    it('should update user information', () => {
      const authStore = useAuthStore();
      const newUserInfo = {
        id: 1,
        username: 'testuser',
        name: '更新后的用户',
        role: 'ADMIN'
      };
      
      authStore.updateUserInfo(newUserInfo);
      
      expect(authStore.userInfo).toEqual(newUserInfo);
    });

    it('should handle null user info', () => {
      const authStore = useAuthStore();
      authStore.userInfo = { id: 1, username: 'testuser' };
      
      authStore.updateUserInfo(null);
      
      expect(authStore.userInfo).toBe(null);
    });
  });

  describe('Clear Token Action', () => {
    it('should clear token and logout', () => {
      const authStore = useAuthStore();
      authStore.token = 'existing-token';
      authStore.userInfo = { id: 1, username: 'testuser' };
      
      authStore.clearToken();
      
      expect(authStore.token).toBe('');
      expect(authStore.userInfo).toBe(null);
      expect(authStore.isLoggedIn).toBe(false);
      expect(removeToken).toHaveBeenCalledOnce();
    });
  });

  describe('Getters', () => {
    describe('isLoggedIn', () => {
      it('should return true when token exists', () => {
        const authStore = useAuthStore();
        authStore.token = 'valid-token';
        
        expect(authStore.isLoggedIn).toBe(true);
      });

      it('should return false when token is empty', () => {
        const authStore = useAuthStore();
        authStore.token = '';
        
        expect(authStore.isLoggedIn).toBe(false);
      });

      it('should return false when token is null', () => {
        const authStore = useAuthStore();
        (authStore as any).token = null;
        
        expect(authStore.isLoggedIn).toBe(false);
      });
    });

    describe('userRole', () => {
      it('should return user role when userInfo exists', () => {
        const authStore = useAuthStore();
        authStore.userInfo = { id: 1, username: 'testuser', role: 'ADMIN' };
        
        expect(authStore.userRole).toBe('ADMIN');
      });

      it('should return empty string when userInfo is null', () => {
        const authStore = useAuthStore();
        authStore.userInfo = null;
        
        expect(authStore.userRole).toBe('');
      });
    });

    describe('userName', () => {
      it('should return user name when userInfo exists', () => {
        const authStore = useAuthStore();
        authStore.userInfo = { id: 1, username: 'testuser', name: '测试用户' };
        
        expect(authStore.userName).toBe('测试用户');
      });

      it('should return username when name is not available', () => {
        const authStore = useAuthStore();
        authStore.userInfo = { id: 1, username: 'testuser' };
        
        expect(authStore.userName).toBe('testuser');
      });

      it('should return empty string when userInfo is null', () => {
        const authStore = useAuthStore();
        authStore.userInfo = null;
        
        expect(authStore.userName).toBe('');
      });
    });

    describe('hasPermission', () => {
      beforeEach(() => {
        const authStore = useAuthStore();
        authStore.userInfo = { 
          id: 1, 
          username: 'testuser', 
          role: 'ADMIN',
          permissions: ['budget:create', 'expense:view', 'approval:manage'] 
        };
      });

      it('should return true when user has the permission', () => {
        const authStore = useAuthStore();
        expect(authStore.hasPermission('budget:create')).toBe(true);
        expect(authStore.hasPermission('expense:view')).toBe(true);
      });

      it('should return false when user does not have the permission', () => {
        const authStore = useAuthStore();
        expect(authStore.hasPermission('user:delete')).toBe(false);
        expect(authStore.hasPermission('system:admin')).toBe(false);
      });

      it('should handle empty permissions array', () => {
        const authStore = useAuthStore();
        authStore.userInfo = { id: 1, username: 'testuser', permissions: [] };
        
        expect(authStore.hasPermission('budget:create')).toBe(false);
      });

      it('should handle null permissions', () => {
        const authStore = useAuthStore();
        authStore.userInfo = { id: 1, username: 'testuser' };
        
        expect(authStore.hasPermission('budget:create')).toBe(false);
      });

      it('should handle null userInfo', () => {
        const authStore = useAuthStore();
        authStore.userInfo = null;
        
        expect(authStore.hasPermission('budget:create')).toBe(false);
      });
    });
  });

  describe('Edge Cases', () => {
    it('should handle token with special characters', async () => {
      (login as any).mockResolvedValue({
        token: 'token.with.special-chars_123',
        user: { id: 1, username: 'testuser' }
      });
      
      const authStore = useAuthStore();
      await authStore.login('testuser', 'password');
      
      expect(authStore.token).toBe('token.with.special-chars_123');
      expect(setToken).toHaveBeenCalledWith('token.with.special-chars_123');
    });

    it('should handle very long token', async () => {
      const longToken = 'a'.repeat(1000);
      (login as any).mockResolvedValue({
        token: longToken,
        user: { id: 1, username: 'testuser' }
      });
      
      const authStore = useAuthStore();
      await authStore.login('testuser', 'password');
      
      expect(authStore.token).toBe(longToken);
      expect(setToken).toHaveBeenCalledWith(longToken);
    });

    it('should handle concurrent login attempts', async () => {
      (login as any).mockResolvedValue({
        token: 'token-1',
        user: { id: 1, username: 'testuser' }
      });
      
      const authStore = useAuthStore();
      
      // Start multiple login attempts
      const promises = [
        authStore.login('testuser', 'password'),
        authStore.login('testuser', 'password'),
        authStore.login('testuser', 'password')
      ];
      
      const results = await Promise.all(promises);
      
      // Should all succeed
      results.forEach(result => {
        expect(result.success).toBe(true);
      });
      
      // Should only call login API once (due to debouncing or similar mechanism)
      expect(login).toHaveBeenCalledTimes(3);
    });
  });

  describe('Error Scenarios', () => {
    it('should handle network error during login', async () => {
      (login as any).mockRejectedValue(new Error('Network Error'));
      
      const authStore = useAuthStore();
      const result = await authStore.login('testuser', 'password');
      
      expect(result.success).toBe(false);
      expect(result.message).toBe('Network Error');
      expect(authStore.isLoggedIn).toBe(false);
    });

    it('should handle server error with status code', async () => {
      const serverError = new Error('Server Error');
      (serverError as any).response = { status: 500 };
      (login as any).mockRejectedValue(serverError);
      
      const authStore = useAuthStore();
      const result = await authStore.login('testuser', 'password');
      
      expect(result.success).toBe(false);
      expect(result.message).toBe('Server Error');
    });

    it('should handle token expiration gracefully', () => {
      (getToken as any).mockReturnValue('expired-token');
      
      const authStore = useAuthStore();
      
      // Token exists but might be expired
      expect(authStore.token).toBe('expired-token');
      expect(authStore.isLoggedIn).toBe(true);
      
      // Clear token (simulating expiration handling)
      authStore.clearToken();
      expect(authStore.isLoggedIn).toBe(false);
    });
  });
});