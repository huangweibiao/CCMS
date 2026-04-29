const { defineConfig } = require('@playwright/test');

module.exports = defineConfig({
  reporter: [
    ['html', { 
      outputFolder: 'playwright-report',
      open: 'never'
    }],
    ['json', { 
      outputFile: 'playwright-report/results.json'
    }],
    ['junit', { 
      outputFile: 'playwright-report/results.xml'
    }],
    ['line'],
    ['allure-playwright'],
    ['./custom-reporter.js']
  ],
  
  // 自定义报告器配置
  customReporter: {
    generateSummary: true,
    exportToPDF: false,
    sendEmail: false,
    attachments: ['screenshots', 'videos', 'traces']
  }
});

// 扩展Playwright报告器
const fs = require('fs');
const path = require('path');

class CustomReporter {
  constructor(options) {
    this.options = options;
    this.results = {
      total: 0,
      passed: 0,
      failed: 0,
      skipped: 0,
      duration: 0,
      suites: [],
      testCases: [],
      timestamp: new Date().toISOString()
    };
  }

  onBegin(config, suite) {
    console.log(`开始执行测试套件: ${suite.title}`);
    this.results.startTime = new Date().toISOString();
  }

  onTestBegin(test) {
    console.log(`开始执行测试: ${test.title}`);
    this.results.total++;
  }

  onTestEnd(test, result) {
    const testCase = {
      title: test.title,
      status: result.status,
      duration: result.duration,
      error: result.error ? result.error.message : null,
      suite: test.parent.title,
      timestamp: new Date().toISOString()
    };

    this.results.testCases.push(testCase);

    switch (result.status) {
      case 'passed':
        this.results.passed++;
        break;
      case 'failed':
        this.results.failed++;
        break;
      case 'skipped':
        this.results.skipped++;
        break;
    }

    this.results.duration += result.duration;
  }

  onEnd(result) {
    this.results.endTime = new Date().toISOString();
    
    // 生成总结报告
    this.generateSummaryReport();
    
    // 生成详细报告
    this.generateDetailedReport();
    
    // 生成趋势分析
    this.generateTrendAnalysis();
    
    // 导出报告到文件
    this.exportReports();
  }

  generateSummaryReport() {
    const summary = {
      测试总结: {
        '总测试数': this.results.total,
        '通过数': this.results.passed,
        '失败数': this.results.failed,
        '跳过数': this.results.skipped,
        '通过率': `${((this.results.passed / this.results.total) * 100).toFixed(2)}%`,
        '总耗时': `${(this.results.duration / 1000).toFixed(2)}秒`
      },
      模块覆盖情况: this.calculateModuleCoverage(),
      '性能指标': this.calculatePerformanceMetrics(),
      '建议改进': this.generateImprovementSuggestions()
    };

    console.log('=== 测试执行总结 ===');
    console.table(summary.测试总结);
    console.log('模块覆盖情况:', summary.模块覆盖情况);
  }

  calculateModuleCoverage() {
    const modules = {};
    this.results.testCases.forEach(testCase => {
      const moduleName = testCase.suite || '未知模块';
      if (!modules[moduleName]) {
        modules[moduleName] = { total: 0, passed: 0, failed: 0 };
      }
      modules[moduleName].total++;
      
      if (testCase.status === 'passed') modules[moduleName].passed++;
      if (testCase.status === 'failed') modules[moduleName].failed++;
    });

    return Object.entries(modules).map(([module, stats]) => ({
      模块: module,
      测试数: stats.total,
      通过率: `${((stats.passed / stats.total) * 100).toFixed(2)}%`
    }));
  }

  calculatePerformanceMetrics() {
    const durations = this.results.testCases.map(tc => tc.duration);
    const avgDuration = durations.reduce((a, b) => a + b, 0) / durations.length;
    
    return {
      '平均测试时间': `${(avgDuration / 1000).toFixed(2)}秒`,
      '最长测试时间': `${(Math.max(...durations) / 1000).toFixed(2)}秒`,
      '最短测试时间': `${(Math.min(...durations) / 1000).toFixed(2)}秒`
    };
  }

  generateImprovementSuggestions() {
    const suggestions = [];
    
    if (this.results.failed > 0) {
      suggestions.push(`有 ${this.results.failed} 个测试失败，需要检查相关功能`);
    }
    
    const slowTests = this.results.testCases.filter(tc => tc.duration > 30000);
    if (slowTests.length > 0) {
      suggestions.push(`有 ${slowTests.length} 个测试执行时间超过30秒，建议优化`);
    }
    
    if (this.results.skipped > 0) {
      suggestions.push(`有 ${this.results.skipped} 个测试被跳过，建议补充测试用例`);
    }
    
    return suggestions.length > 0 ? suggestions : ['测试质量良好，继续保持！'];
  }

  generateDetailedReport() {
    const detailedReport = {
      测试详情: this.results.testCases.map(tc => ({
        测试标题: tc.title,
        状态: tc.status,
        耗时: `${(tc.duration / 1000).toFixed(2)}秒`,
        模块: tc.suite,
        时间: tc.timestamp
      })),
      
      失败测试分析: this.results.testCases
        .filter(tc => tc.status === 'failed')
        .map(tc => ({
          测试标题: tc.title,
          错误信息: tc.error,
          模块: tc.suite
        }))
    };

    return detailedReport;
  }

  generateTrendAnalysis() {
    // 这里可以集成历史数据对比
    return {
      '本次通过率': `${((this.results.passed / this.results.total) * 100).toFixed(2)}%`,
      '趋势分析': '首次执行，无历史数据对比'
    };
  }

  exportReports() {
    const reportDir = path.join(process.cwd(), 'test-reports');
    
    // 确保目录存在
    if (!fs.existsSync(reportDir)) {
      fs.mkdirSync(reportDir, { recursive: true });
    }

    // 导出JSON报告
    fs.writeFileSync(
      path.join(reportDir, 'test-results.json'),
      JSON.stringify(this.results, null, 2)
    );

    // 导出HTML报告
    this.generateHTMLReport(reportDir);

    // 导出Markdown报告
    this.generateMarkdownReport(reportDir);

    console.log(`报告已导出到: ${reportDir}`);
  }

  generateHTMLReport(reportDir) {
    const htmlContent = `
<!DOCTYPE html>
<html>
<head>
    <title>CCMS UI自动化测试报告</title>
    <style>
        body { font-family: Arial, sans-serif; margin: 20px; }
        .summary { background: #f5f5f5; padding: 20px; border-radius: 5px; }
        .failed { color: red; }
        .passed { color: green; }
        table { width: 100%; border-collapse: collapse; }
        th, td { border: 1px solid #ddd; padding: 8px; text-align: left; }
        th { background-color: #f2f2f2; }
    </style>
</head>
<body>
    <h1>CCMS UI自动化测试报告</h1>
    <div class="summary">
        <h2>测试总结</h2>
        <p><strong>通过率:</strong> <span class="${this.results.passed === this.results.total ? 'passed' : 'failed'}">${((this.results.passed / this.results.total) * 100).toFixed(2)}%</span></p>
        <p><strong>总测试数:</strong> ${this.results.total}</p>
        <p><strong>通过:</strong> ${this.results.passed}</p>
        <p><strong>失败:</strong> ${this.results.failed}</p>
        <p><strong>跳过:</strong> ${this.results.skipped}</p>
    </div>
    
    <h2>详细测试结果</h2>
    <table>
        <thead>
            <tr>
                <th>测试标题</th>
                <th>状态</th>
                <th>耗时</th>
                <th>模块</th>
            </tr>
        </thead>
        <tbody>
            ${this.results.testCases.map(tc => `
                <tr>
                    <td>${tc.title}</td>
                    <td class="${tc.status}">${tc.status}</td>
                    <td>${(tc.duration / 1000).toFixed(2)}秒</td>
                    <td>${tc.suite}</td>
                </tr>
            `).join('')}
        </tbody>
    </table>
</body>
</html>
    `;

    fs.writeFileSync(path.join(reportDir, 'test-report.html'), htmlContent);
  }

  generateMarkdownReport(reportDir) {
    const markdownContent = `
# CCMS UI自动化测试报告

## 测试总结

- **总测试数**: ${this.results.total}
- **通过数**: ${this.results.passed}
- **失败数**: ${this.results.failed}
- **跳过数**: ${this.results.skipped}
- **通过率**: ${((this.results.passed / this.results.total) * 100).toFixed(2)}%
- **总耗时**: ${(this.results.duration / 1000).toFixed(2)}秒

## 模块覆盖情况

${this.calculateModuleCoverage().map(module => 
  `- **${module.模块}**: ${module.测试数} 个测试，通过率 ${module.通过率}`
).join('\n')}

## 详细结果

| 测试标题 | 状态 | 耗时 | 模块 |
|----------|------|------|------|
${this.results.testCases.map(tc => 
  `| ${tc.title} | ${tc.status} | ${(tc.duration / 1000).toFixed(2)}秒 | ${tc.suite} |`
).join('\n')}

## 建议改进

${this.generateImprovementSuggestions().map(suggestion => 
  `- ${suggestion}`
).join('\n')}

---
*报告生成时间: ${new Date().toISOString()}*
    `;

    fs.writeFileSync(path.join(reportDir, 'test-report.md'), markdownContent);
  }
}

module.exports = CustomReporter;