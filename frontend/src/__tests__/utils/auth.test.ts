import { describe, it, expect, vi, beforeEach } from 'vitest';
import {
  isLoggedIn,
  getToken,
  setToken,
  removeToken,
  getUserInfo,
  setUserInfo,
  removeUserInfo
} from '../../utils/auth';

// Mock localStorage
const localStorageMock = (() => {
  let store: Record<string, string> = {};
  return {
    getItem: vi.fn((key: string) => store[key] || null),
    setItem: vi.fn((key: string, value: string) => {
      store[key] = value.toString();
    }),
    removeItem: vi.fn((key: string) => {
      delete store[key];
    }),
    clear: vi.fn(() => {
      store = {};
    }),
  };
})();

Object.defineProperty(window, 'localStorage', {
  value: localStorageMock,
});

describe('Authentication Utilities', () => {
  beforeEach(() => {
    localStorage.clear();
  });

  describe('Token Management', () => {
    it('should set and get token correctly', () => {
      const token = 'test-jwt-token';
      setToken(token);
      expect(getToken()).toBe(token);
      expect(localStorage.setItem).toHaveBeenCalledWith('ccms_token', token);
      expect(localStorage.getItem).toHaveBeenCalledWith('ccms_token');
    });

    it('should remove token correctly', () => {
      setToken('test-token');
      removeToken();
      expect(getToken()).toBeNull();
      expect(localStorage.removeItem).toHaveBeenCalledWith('ccms_token');
    });

    it('should return null when token does not exist', () => {
      expect(getToken()).toBeNull();
    });
  });

  describe('User Info Management', () => {
    const userInfo = {
      id: 1,
      username: 'testuser',
      realName: '测试用户',
      deptId: 101,
      role: 'admin',
      permissions: ['user:read', 'user:write']
    };

    it('should set and get user info correctly', () => {
      setUserInfo(userInfo);
      const retrieved = getUserInfo();
      expect(retrieved).toEqual(userInfo);
      expect(localStorage.setItem).toHaveBeenCalledWith(
        'ccms_user',
        JSON.stringify(userInfo)
      );
    });

    it('should remove user info correctly', () => {
      setUserInfo(userInfo);
      removeUserInfo();
      expect(getUserInfo()).toBeNull();
      expect(localStorage.removeItem).toHaveBeenCalledWith('ccms_user');
    });

    it('should return null when user info does not exist', () => {
      expect(getUserInfo()).toBeNull();
    });

    it('should handle malformed JSON in user info', () => {
      localStorage.setItem('ccms_user', 'invalid-json');
      expect(getUserInfo()).toBeNull();
    });
  });

  describe('Login Status Check', () => {
    it('should return true when token and user info exist', () => {
      setToken('test-token');
      setUserInfo({ id: 1, username: 'testuser' });
      expect(isLoggedIn()).toBe(true);
    });

    it('should return false when token does not exist', () => {
      setUserInfo({ id: 1, username: 'testuser' });
      expect(isLoggedIn()).toBe(false);
    });

    it('should return false when user info does not exist', () => {
      setToken('test-token');
      expect(isLoggedIn()).toBe(false);
    });

    it('should return false when both token and user info are missing', () => {
      expect(isLoggedIn()).toBe(false);
    });
  });

  describe('Edge Cases', () => {
    it('should handle empty token string', () => {
      setToken('');
      expect(getToken()).toBe('');
    });

    it('should handle null token', () => {
      setToken(null as any);
      expect(getToken()).toBeNull();
    });

    it('should handle undefined token', () => {
      setToken(undefined as any);
      expect(getToken()).toBeNull();
    });

    it('should handle partial user info', () => {
      const partialInfo = { username: 'testuser' };
      setUserInfo(partialInfo as any);
      expect(getUserInfo()).toEqual(partialInfo);
    });
  });
});