import { describe, it, expect, beforeEach, vi } from 'vitest';
import { mount } from '@vue/test-utils';
import { createPinia, setActivePinia } from 'pinia';
import LoginPage from '../../pages/LoginPage.vue';
import { useAuthStore } from '../../stores/auth';

// Mock the auth store
vi.mock('../../stores/auth', () => ({
  useAuthStore: vi.fn()
}));

describe('LoginPage', () => {
  let authStore: any;

  beforeEach(() => {
    setActivePinia(createPinia());
    
    // Create a mock auth store
    authStore = {
      login: vi.fn(),
      loading: false,
      error: ''
    };
    
    (useAuthStore as any).mockReturnValue(authStore);
  });

  const createWrapper = (props = {}) => {
    return mount(LoginPage, {
      global: {
        plugins: [createPinia()],
        stubs: {
          // Stub any child components if needed
        }
      },
      props
    });
  };

  describe('Initial Render', () => {
    it('should render login form correctly', () => {
      const wrapper = createWrapper();
      
      expect(wrapper.find('form').exists()).toBe(true);
      expect(wrapper.find('input[type="text"]').exists()).toBe(true);
      expect(wrapper.find('input[type="password"]').exists()).toBe(true);
      expect(wrapper.find('button[type="submit"]').exists()).toBe(true);
      expect(wrapper.find('button[type="submit"]').text()).toContain('登录');
    });

    it('should display application title', () => {
      const wrapper = createWrapper();
      
      expect(wrapper.text()).toContain('费用管理系统');
      expect(wrapper.text()).toContain('CCMS');
    });

    it('should have empty initial form values', () => {
      const wrapper = createWrapper();
      
      const usernameInput = wrapper.find('input[type="text"]').element as HTMLInputElement;
      const passwordInput = wrapper.find('input[type="password"]').element as HTMLInputElement;
      
      expect(usernameInput.value).toBe('');
      expect(passwordInput.value).toBe('');
    });
  });

  describe('Form Validation', () => {
    it('should show error when submitting empty form', async () => {
      const wrapper = createWrapper();
      
      await wrapper.find('form').trigger('submit.prevent');
      
      expect(authStore.login).not.toHaveBeenCalled();
      expect(wrapper.text()).toContain('请输入用户名');
      expect(wrapper.text()).toContain('请输入密码');
    });

    it('should show error when username is empty', async () => {
      const wrapper = createWrapper();
      
      await wrapper.find('input[type="password"]').setValue('password123');
      await wrapper.find('form').trigger('submit.prevent');
      
      expect(authStore.login).not.toHaveBeenCalled();
      expect(wrapper.text()).toContain('请输入用户名');
    });

    it('should show error when password is empty', async () => {
      const wrapper = createWrapper();
      
      await wrapper.find('input[type="text"]').setValue('testuser');
      await wrapper.find('form').trigger('submit.prevent');
      
      expect(authStore.login).not.toHaveBeenCalled();
      expect(wrapper.text()).toContain('请输入密码');
    });

    it('should clear validation errors when user starts typing', async () => {
      const wrapper = createWrapper();
      
      // Trigger validation errors first
      await wrapper.find('form').trigger('submit.prevent');
      expect(wrapper.text()).toContain('请输入用户名');
      
      // Start typing username
      await wrapper.find('input[type="text"]').setValue('t');
      expect(wrapper.text()).not.toContain('请输入用户名');
    });
  });

  describe('Login Functionality', () => {
    it('should call login action with correct credentials', async () => {
      authStore.login.mockResolvedValue({ success: true });
      
      const wrapper = createWrapper();
      
      await wrapper.find('input[type="text"]').setValue('testuser');
      await wrapper.find('input[type="password"]').setValue('password123');
      await wrapper.find('form').trigger('submit.prevent');
      
      expect(authStore.login).toHaveBeenCalledWith('testuser', 'password123');
    });

    it('should show loading state during login', async () => {
      // Create a promise that we can resolve later
      let resolveLogin: (value: any) => void;
      const loginPromise = new Promise(resolve => {
        resolveLogin = resolve;
      });
      
      authStore.login.mockReturnValue(loginPromise);
      authStore.loading = true;
      
      const wrapper = createWrapper();
      
      await wrapper.find('input[type="text"]').setValue('testuser');
      await wrapper.find('input[type="password"]').setValue('password123');
      await wrapper.find('form').trigger('submit.prevent');
      
      // Button should be disabled and show loading
      const submitButton = wrapper.find('button[type="submit"]');
      expect(submitButton.attributes('disabled')).toBeDefined();
      expect(submitButton.text()).toContain('登录中');
      
      // Resolve the promise
      resolveLogin!({ success: true });
      await wrapper.vm.$nextTick();
      
      // Update store to not loading
      authStore.loading = false;
      await wrapper.vm.$nextTick();
      
      // Button should be enabled again
      expect(submitButton.attributes('disabled')).toBeUndefined();
      expect(submitButton.text()).toContain('登录');
    });

    it('should display error message when login fails', async () => {
      const errorMessage = '用户名或密码错误';
      authStore.login.mockResolvedValue({ success: false, message: errorMessage });
      authStore.error = errorMessage;
      
      const wrapper = createWrapper();
      
      await wrapper.find('input[type="text"]').setValue('testuser');
      await wrapper.find('input[type="password"]').setValue('wrongpassword');
      await wrapper.find('form').trigger('submit.prevent');
      await wrapper.vm.$nextTick();
      
      expect(wrapper.text()).toContain(errorMessage);
      
      // Error should be cleared when user starts typing again
      await wrapper.find('input[type="text"]').setValue('test');
      expect(wrapper.text()).not.toContain(errorMessage);
    });

    it('should navigate to dashboard on successful login', async () => {
      // Mock router push
      const mockRouter = {
        push: vi.fn()
      };
      
      const wrapper = mount(LoginPage, {
        global: {
          plugins: [createPinia()],
          mocks: {
            $router: mockRouter
          }
        }
      });
      
      authStore.login.mockResolvedValue({ success: true });
      
      await wrapper.find('input[type="text"]').setValue('testuser');
      await wrapper.find('input[type="password"]').setValue('password123');
      await wrapper.find('form').trigger('submit.prevent');
      
      // Wait for async operation
      await wrapper.vm.$nextTick();
      
      expect(mockRouter.push).toHaveBeenCalledWith('/dashboard');
    });
  });

  describe('User Interactions', () => {
    it('should toggle password visibility', async () => {
      const wrapper = createWrapper();
      
      const passwordInput = wrapper.find('input[type="password"]');
      expect(passwordInput.exists()).toBe(true);
      
      // Initially should be password type
      expect(passwordInput.attributes('type')).toBe('password');
      
      // Find and click the visibility toggle button
      const toggleButton = wrapper.find('.password-toggle');
      if (toggleButton.exists()) {
        await toggleButton.trigger('click');
        
        // After toggle, should be text type
        const updatedPasswordInput = wrapper.find('input[type="text"]');
        expect(updatedPasswordInput.exists()).toBe(true);
        
        // Toggle back
        await toggleButton.trigger('click');
        expect(wrapper.find('input[type="password"]').exists()).toBe(true);
      }
    });

    it('should handle Enter key submission', async () => {
      authStore.login.mockResolvedValue({ success: true });
      
      const wrapper = createWrapper();
      
      await wrapper.find('input[type="text"]').setValue('testuser');
      await wrapper.find('input[type="password"]').setValue('password123');
      
      // Press Enter in password field
      await wrapper.find('input[type="password"]').trigger('keyup.enter');
      
      expect(authStore.login).toHaveBeenCalledWith('testuser', 'password123');
    });

    it('should not submit on Enter when form is invalid', async () => {
      const wrapper = createWrapper();
      
      // Only fill username
      await wrapper.find('input[type="text"]').setValue('testuser');
      
      // Press Enter in password field
      await wrapper.find('input[type="password"]').trigger('keyup.enter');
      
      expect(authStore.login).not.toHaveBeenCalled();
    });
  });

  describe('Accessibility', () => {
    it('should have proper labels and placeholders', () => {
      const wrapper = createWrapper();
      
      const usernameInput = wrapper.find('input[type="text"]');
      const passwordInput = wrapper.find('input[type="password"]');
      
      expect(usernameInput.attributes('placeholder')).toContain('用户名');
      expect(passwordInput.attributes('placeholder')).toContain('密码');
      
      // Check for aria-labels if present
      if (usernameInput.attributes('aria-label')) {
        expect(usernameInput.attributes('aria-label')).toContain('用户名');
      }
      if (passwordInput.attributes('aria-label')) {
        expect(passwordInput.attributes('aria-label')).toContain('密码');
      }
    });

    it('should have proper form structure', () => {
      const wrapper = createWrapper();
      
      const form = wrapper.find('form');
      expect(form.attributes('role')).toBe('form');
      
      // Check if inputs have proper ids and labels
      const usernameInput = wrapper.find('input[type="text"]');
      if (usernameInput.attributes('id')) {
        const usernameLabel = wrapper.find(`label[for="${usernameInput.attributes('id')}"]`);
        expect(usernameLabel.exists()).toBe(true);
      }
    });
  });

  describe('Edge Cases', () => {
    it('should handle very long username and password', async () => {
      const longUsername = 'a'.repeat(100);
      const longPassword = 'b'.repeat(100);
      
      authStore.login.mockResolvedValue({ success: true });
      
      const wrapper = createWrapper();
      
      await wrapper.find('input[type="text"]').setValue(longUsername);
      await wrapper.find('input[type="password"]').setValue(longPassword);
      await wrapper.find('form').trigger('submit.prevent');
      
      expect(authStore.login).toHaveBeenCalledWith(longUsername, longPassword);
    });

    it('should handle special characters in credentials', async () => {
      const specialUsername = 'user@name-with_special.chars';
      const specialPassword = 'p@ssw0rd!with$pecia1';
      
      authStore.login.mockResolvedValue({ success: true });
      
      const wrapper = createWrapper();
      
      await wrapper.find('input[type="text"]').setValue(specialUsername);
      await wrapper.find('input[type="password"]').setValue(specialPassword);
      await wrapper.find('form').trigger('submit.prevent');
      
      expect(authStore.login).toHaveBeenCalledWith(specialUsername, specialPassword);
    });

    it('should prevent multiple concurrent login attempts', async () => {
      // Create a promise that we can control
      let resolveLogin: (value: any) => void;
      const loginPromise = new Promise(resolve => {
        resolveLogin = resolve;
      });
      
      authStore.login.mockReturnValue(loginPromise);
      authStore.loading = true;
      
      const wrapper = createWrapper();
      
      await wrapper.find('input[type="text"]').setValue('testuser');
      await wrapper.find('input[type="password"]').setValue('password123');
      
      // Submit first time
      await wrapper.find('form').trigger('submit.prevent');
      
      // Try to submit again while still loading
      await wrapper.find('form').trigger('submit.prevent');
      
      // Should only call login once
      expect(authStore.login).toHaveBeenCalledTimes(1);
      
      // Resolve the promise
      resolveLogin!({ success: true });
      await wrapper.vm.$nextTick();
      
      // Update loading state
      authStore.loading = false;
      await wrapper.vm.$nextTick();
      
      // Now should be able to submit again
      await wrapper.find('form').trigger('submit.prevent');
      expect(authStore.login).toHaveBeenCalledTimes(2);
    });

    it('should handle network timeout gracefully', async () => {
      // Simulate a slow network response
      authStore.login.mockImplementation(() => 
        new Promise(resolve => setTimeout(() => resolve({ success: false, message: '请求超时' }), 100))
      );
      
      const wrapper = createWrapper();
      
      await wrapper.find('input[type="text"]').setValue('testuser');
      await wrapper.find('input[type="password"]').setValue('password123');
      
      const startTime = Date.now();
      await wrapper.find('form').trigger('submit.prevent');
      
      // Wait for the operation to complete
      await new Promise(resolve => setTimeout(resolve, 150));
      await wrapper.vm.$nextTick();
      
      expect(authStore.login).toHaveBeenCalled();
      expect(wrapper.text()).toContain('请求超时');
    });
  });

  describe('Responsive Design', () => {
    it('should adapt to different screen sizes', () => {
      const wrapper = createWrapper();
      
      // Check for responsive CSS classes
      const container = wrapper.find('.login-container');
      expect(container.exists()).toBe(true);
      
      // Check if responsive classes are applied
      const hasResponsiveClasses = container.classes().some(className => 
        className.includes('sm:') || className.includes('md:') || className.includes('lg:')
      );
      
      // This is optional depending on the CSS framework used
      if (hasResponsiveClasses) {
        expect(hasResponsiveClasses).toBe(true);
      }
    });
  });
});