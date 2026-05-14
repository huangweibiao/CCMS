import { vi } from 'vitest'
import { config } from '@vue/test-utils'

// Mock window.matchMedia
Object.defineProperty(window, 'matchMedia', {
  writable: true,
  value: vi.fn().mockImplementation(query => ({
    matches: false,
    media: query,
    onchange: null,
    addListener: vi.fn(),
    removeListener: vi.fn(),
    addEventListener: vi.fn(),
    removeEventListener: vi.fn(),
    dispatchEvent: vi.fn(),
  })),
})

// Mock window.localStorage
Object.defineProperty(window, 'localStorage', {
  writable: true,
  value: {
    getItem: vi.fn(),
    setItem: vi.fn(),
    removeItem: vi.fn(),
    clear: vi.fn(),
  },
})

// Mock window.sessionStorage
Object.defineProperty(window, 'sessionStorage', {
  writable: true,
  value: {
    getItem: vi.fn(),
    setItem: vi.fn(),
    removeItem: vi.fn(),
    clear: vi.fn(),
  },
})

// Mock Element Plus components that might be used in tests
const mockElementPlusComponents = {
  ElButton: {
    template: '<button><slot /></button>',
  },
  ElInput: {
    template: '<input />',
  },
  ElForm: {
    template: '<form><slot /></form>',
  },
  ElFormItem: {
    template: '<div><slot /></div>',
  },
}

// Configure Vue Test Utils
try {
  config.global.stubs = {
    ...mockElementPlusComponents,
  }
} catch (error) {
  console.warn('Vue Test Utils config error:', error)
}

// Mock axios for API calls
global.fetch = vi.fn()

// Mock console methods to keep test output clean
console.warn = vi.fn()
console.error = vi.fn()