import { FullConfig } from '@playwright/test';

async function globalSetup(config: FullConfig) {
  // 全局测试准备
  console.log('开始全局测试设置...');
  
  // 在这里可以执行全局初始化操作
  // 比如：创建测试数据库、准备测试用户、清理测试环境等
  
  // 示例：记录测试开始时间
  const startTime = new Date();
  console.log(`测试开始时间: ${startTime.toISOString()}`);
  
  // 设置全局环境变量
  process.env.TEST_ENV = 'ui-automation';
  process.env.START_TIME = startTime.toISOString();
  
  console.log('全局测试设置完成');
}

export default globalSetup;