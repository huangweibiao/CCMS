import { FullConfig } from '@playwright/test';

async function globalTeardown(config: FullConfig) {
  // 全局测试清理
  console.log('开始全局测试清理...');
  
  // 在这里可以执行全局清理操作
  // 比如：清理测试数据库、删除测试文件、释放资源等
  
  // 计算测试执行时间
  const startTime = process.env.START_TIME ? new Date(process.env.START_TIME) : new Date();
  const endTime = new Date();
  const duration = endTime.getTime() - startTime.getTime();
  
  console.log(`测试结束时间: ${endTime.toISOString()}`);
  console.log(`测试执行时长: ${Math.round(duration / 1000)}秒`);
  
  // 清理全局环境变量
  delete process.env.TEST_ENV;
  delete process.env.START_TIME;
  
  console.log('全局测试清理完成');
}

export default globalTeardown;