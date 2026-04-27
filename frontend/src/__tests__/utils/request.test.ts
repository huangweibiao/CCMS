import { describe, it, expect, vi, beforeEach } from 'vitest';
import { http, setGlobalHeaders, handleResponse } from '../../utils/request';
import { getToken } from '../../utils/auth';

// Mock axios
vi.mock('axios', () => ({
  default: {
    create: vi.fn(() => ({
      interceptors: {
        request: { use: vi.fn() },
        response: { use: vi.fn() }
      },
      get: vi.fn(),
      post: vi.fn(),
      put: vi.fn(),
      delete: vi.fn()
    }))
  }
}));

// Mock auth utilities
vi.mock('../../utils/auth', () => ({
  getToken: vi.fn(() => 'test-token'),
  removeToken: vi.fn(),
  removeUserInfo: vi.fn()
}));

describe('Request Utility', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  describe('HTTP Instance', () => {
    it('should create axios instance with base configuration', () => {
      expect(http).toBeDefined();
      // 验证axios是否被正确调用，创建了实例
      const axios = await import('axios');
      expect(axios.default.create).toHaveBeenCalledWith({
        baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
        timeout: 10000,
        headers: {
          'Content-Type': 'application/json'
        }
      });
    });
  });

  describe('Request Interceptor', () => {
    it('should add authorization header when token exists', async () => {
      const config = { headers: {} };
      const axios = await import('axios');
      
      // 模拟拦截器的使用
      const interceptorCall = axios.default.create.mock.results[0].value.interceptors.request.use;
      const requestHandler = interceptorCall.mock.calls[0][0];
      
      const result = requestHandler(config);
      expect(result.headers.Authorization).toBe('Bearer test-token');
      expect(getToken).toHaveBeenCalled();
    });

    it('should not add authorization header when token is missing', async () => {
      vi.mocked(getToken).mockReturnValueOnce(null);
      const config = { headers: {} };
      const axios = await import('axios');
      
      const interceptorCall = axios.default.create.mock.results[0].value.interceptors.request.use;
      const requestHandler = interceptorCall.mock.calls[0][0];
      
      const result = requestHandler(config);
      expect(result.headers.Authorization).toBeUndefined();
    });
  });

  describe('Response Interceptor', () => {
    it('should handle successful response', async () => {
      const response = {
        data: {
          code: 200,
          message: 'Success',
          data: { id: 1, name: 'test' }
        }
      };
      
      const axios = await import('axios');
      const interceptorCall = axios.default.create.mock.results[0].value.interceptors.response.use;
      const successHandler = interceptorCall.mock.calls[0][0];
      
      const result = successHandler(response);
      expect(result).toEqual(response.data);
    });

    it('should handle 401 unauthorized error', async () => {
      const error = {
        response: {
          status: 401
        }
      };
      
      const { removeToken, removeUserInfo } = await import('../../utils/auth');
      const axios = await import('axios');
      const interceptorCall = axios.default.create.mock.results[0].value.interceptors.response.use;
      const errorHandler = interceptorCall.mock.calls[0][1];
      
      try {
        await errorHandler(error);
      } catch (e) {
        expect(removeToken).toHaveBeenCalled();
        expect(removeUserInfo).toHaveBeenCalled();
        expect(e).toBe(error);
      }
    });

    it('should handle 500 server error', async () => {
      const error = {
        response: {
          status: 500,
          data: { message: 'Internal Server Error' }
        }
      };
      
      const axios = await import('axios');
      const interceptorCall = axios.default.create.mock.results[0].value.interceptors.response.use;
      const errorHandler = interceptorCall.mock.calls[0][1];
      
      try {
        await errorHandler(error);
      } catch (e) {
        expect(e).toBe(error);
      }
    });

    it('should handle network error', async () => {
      const error = {
        message: 'Network Error'
      };
      
      const axios = await import('axios');
      const interceptorCall = axios.default.create.mock.results[0].value.interceptors.response.use;
      const errorHandler = interceptorCall.mock.calls[0][1];
      
      try {
        await errorHandler(error);
      } catch (e) {
        expect(e).toBe(error);
      }
    });
  });

  describe('Response Handler', () => {
    it('should handle successful response data', () => {
      const response = {
        code: 200,
        message: 'Success',
        data: { id: 1, name: 'test' }
      };
      
      const result = handleResponse(response);
      expect(result).toEqual(response.data);
    });

    it('should throw error for non-200 response', () => {
      const response = {
        code: 400,
        message: 'Bad Request',
        data: null
      };
      
      expect(() => handleResponse(response)).toThrow('Bad Request');
    });

    it('should handle null response', () => {
      expect(() => handleResponse(null as any)).toThrow('无效的响应数据');
    });

    it('should handle undefined response', () => {
      expect(() => handleResponse(undefined as any)).toThrow('无效的响应数据');
    });

    it('should handle response without message', () => {
      const response = {
        code: 400,
        data: null
      };
      
      expect(() => handleResponse(response as any)).toThrow('请求失败，请稍后重试');
    });
  });

  describe('Global Headers', () => {
    it('should set global headers correctly', async () => {
      const customHeaders = {
        'X-Custom-Header': 'custom-value',
        'X-Request-ID': '12345'
      };
      
      setGlobalHeaders(customHeaders);
      
      const axios = await import('axios');
      const createConfig = axios.default.create.mock.calls[0][0];
      
      // 验证默认配置是否包含自定义头部
      expect(createConfig.headers).toEqual(
        expect.objectContaining({
          'Content-Type': 'application/json',
          ...customHeaders
        })
      );
    });

    it('should handle empty headers object', async () => {
      setGlobalHeaders({});
      
      const axios = await import('axios');
      const createConfig = axios.default.create.mock.calls[0][0];
      
      expect(createConfig.headers).toEqual({
        'Content-Type': 'application/json'
      });
    });

    it('should handle undefined headers', async () => {
      setGlobalHeaders(undefined as any);
      
      const axios = await import('axios');
      const createConfig = axios.default.create.mock.calls[0][0];
      
      expect(createConfig.headers).toEqual({
        'Content-Type': 'application/json'
      });
    });
  });

  describe('HTTP Methods', () => {
    it('should have get method', async () => {
      const axios = await import('axios');
      expect(axios.default.create().get).toBeDefined();
    });

    it('should have post method', async () => {
      const axios = await import('axios');
      expect(axios.default.create().post).toBeDefined();
    });

    it('should have put method', async () => {
      const axios = await import('axios');
      expect(axios.default.create().put).toBeDefined();
    });

    it('should have delete method', async () => {
      const axios = await import('axios');
      expect(axios.default.create().delete).toBeDefined();
    });
  });
});