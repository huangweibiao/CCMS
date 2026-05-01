import { config } from '@vue/test-utils';
import { createPinia } from 'pinia';
import ElementPlus from 'element-plus';
import router from '@/router';

// 全局配置Vue Test Utils
config.global.plugins = [createPinia(), ElementPlus, router];

// 模拟Element Plus图标
config.global.stubs = {
  'el-icon': true,
  'el-button': true,
  'el-dialog': true,
  'el-form': true,
  'el-table': true,
  'el-pagination': true,
  'el-select': true,
  'el-input': true
};

// 模拟全局组件
config.global.components = {
  'router-link': { template: '<a><slot /></a>' },
  'router-view': { template: '<div><slot /></div>' }
};

// 模拟window对象属性
Object.defineProperty(window, 'matchMedia', {
  writable: true,
  value: vi.fn().mockImplementation(query => ({
    matches: false,
    media: query,
    onchange: null,
    addListener: vi.fn(), // deprecated
    removeListener: vi.fn(), // deprecated
    addEventListener: vi.fn(),
    removeEventListener: vi.fn(),
    dispatchEvent: vi.fn(),
  })),
});

// 模拟ResizeObserver
global.ResizeObserver = vi.fn().mockImplementation(() => ({
  observe: vi.fn(),
  unobserve: vi.fn(),
  disconnect: vi.fn(),
}));