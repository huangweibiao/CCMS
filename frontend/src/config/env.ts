// 环境变量配置管理模块
// 统一管理所有环境变量，提供类型安全的访问接口

/**
 * 环境变量配置接口
 */
export interface EnvConfig {
  // 基础配置
  appTitle: string;
  appDescription: string;
  apiBaseUrl: string;
  port: number;
  host: string;
  frontendUrl: string;
  
  // 功能开关
  enableMock: boolean;
  enableDebug: boolean;
  enablePerformanceMonitor: boolean;
  
  // 系统配置
  logLevel: 'debug' | 'info' | 'warn' | 'error';
  buildTime: string;
  gitCommit: string;
}

/**
 * 解析环境变量，转换为类型安全的配置对象
 */
function parseEnvConfig(): EnvConfig {
  return {
    // 基础配置
    appTitle: import.meta.env.VITE_APP_TITLE || '企业级费控管理系统',
    appDescription: import.meta.env.VITE_APP_DESCRIPTION || '企业级费控管理系统',
    apiBaseUrl: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080',
    port: parseInt(import.meta.env.VITE_PORT || '3000', 10),
    host: import.meta.env.VITE_HOST || 'localhost',
    frontendUrl: import.meta.env.VITE_FRONTEND_URL || 'http://localhost:3000',
    
    // 功能开关 - 转换为布尔值
    enableMock: import.meta.env.VITE_ENABLE_MOCK === 'true',
    enableDebug: import.meta.env.VITE_ENABLE_DEBUG === 'true',
    enablePerformanceMonitor: import.meta.env.VITE_ENABLE_PERFORMANCE_MONITOR === 'true',
    
    // 系统配置
    logLevel: (import.meta.env.VITE_LOG_LEVEL as 'debug' | 'info' | 'warn' | 'error') || 'info',
    buildTime: import.meta.env.VITE_BUILD_TIME || new Date().toISOString(),
    gitCommit: import.meta.env.VITE_GIT_COMMIT || 'local',
  };
}

/**
 * 全局环境配置实例
 */
export const envConfig = parseEnvConfig();

/**
 * 环境变量工具函数
 */
export class EnvUtils {
  /**
   * 获取当前环境类型
   */
  static getEnvironment(): 'development' | 'production' | 'test' {
    return import.meta.env.MODE as 'development' | 'production' | 'test';
  }
  
  /**
   * 检查是否为开发环境
   */
  static isDevelopment(): boolean {
    return this.getEnvironment() === 'development';
  }
  
  /**
   * 检查是否为生产环境
   */
  static isProduction(): boolean {
    return this.getEnvironment() === 'production';
  }
  
  /**
   * 检查是否为测试环境
   */
  static isTest(): boolean {
    return this.getEnvironment() === 'test';
  }
  
  /**
   * 启用调试功能的条件
   */
  static canDebug(): boolean {
    return envConfig.enableDebug || this.isDevelopment();
  }
  
  /**
   * 启用Mock数据的条件
   */
  static shouldMock(): boolean {
    return envConfig.enableMock || this.isTest();
  }
  
  /**
   * 启用性能监控的条件
   */
  static shouldMonitorPerformance(): boolean {
    return envConfig.enablePerformanceMonitor;
  }
  
  /**
   * 获取日志级别数值
   */
  static getLogLevelValue(): number {
    const levels = { debug: 0, info: 1, warn: 2, error: 3 };
    return levels[envConfig.logLevel] || 1;
  }
  
  /**
   * 检查是否应该记录指定级别的日志
   */
  static shouldLog(level: 'debug' | 'info' | 'warn' | 'error'): boolean {
    const currentLevel = this.getLogLevelValue();
    const targetLevel = { debug: 0, info: 1, warn: 2, error: 3 }[level];
    return targetLevel >= currentLevel;
  }
}

/**
 * 环境变量校验
 * 在应用启动时验证关键环境变量是否配置正确
 */
export function validateEnvConfig(): { isValid: boolean; errors: string[] } {
  const errors: string[] = [];
  
  // 必填字段检查
  if (!envConfig.apiBaseUrl) {
    errors.push('VITE_API_BASE_URL is required');
  }
  
  if (!envConfig.appTitle) {
    errors.push('VITE_APP_TITLE is required');
  }
  
  // URL格式检查
  try {
    new URL(envConfig.apiBaseUrl);
  } catch (error) {
    errors.push('VITE_API_BASE_URL must be a valid URL');
  }
  
  // 端口范围检查
  if (envConfig.port < 1 || envConfig.port > 65535) {
    errors.push('VITE_PORT must be between 1 and 65535');
  }
  
  return {
    isValid: errors.length === 0,
    errors
  };
}

// 导出类型安全的配置对象
export default envConfig;