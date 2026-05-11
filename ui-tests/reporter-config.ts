import { Reporter, TestCase, TestResult, TestStep, FullConfig, Suite } from '@playwright/test/reporter';
import * as fs from 'fs';
import * as path from 'path';

/**
 * 自定义测试报告生成器
 * 生成包含测试统计、执行时间、失败详情等信息的综合报告
 */
class CustomReporter implements Reporter {
  private startTime: number = 0;
  private testResults: any[] = [];
  private config!: FullConfig;

  onBegin(config: FullConfig, suite: Suite) {
    this.config = config;
    this.startTime = Date.now();
    console.log(`\n🚀 开始执行测试套件: ${suite.title || 'CCMS E2E Tests'}`);
    console.log(`📊 总测试用例数: ${suite.allTests().length}`);
    console.log(`🌐 测试项目: ${config.projects.map(p => p.name).join(', ')}\n`);
  }

  onTestBegin(test: TestCase) {
    console.log(`▶️  ${test.title}`);
  }

  onTestEnd(test: TestCase, result: TestResult) {
    const duration = result.duration;
    const status = result.status;
    
    this.testResults.push({
      title: test.title,
      status: status,
      duration: duration,
      retries: test.retries,
      project: test.parent.project()?.name || 'default',
      file: test.location.file,
      line: test.location.line,
      error: result.error?.message || null,
      steps: result.steps.map((step: TestStep) => ({
        title: step.title,
        duration: step.duration,
        error: step.error?.message
      }))
    });

    const icon = status === 'passed' ? '✅' : status === 'failed' ? '❌' : '⏭️';
    console.log(`${icon} ${test.title} (${duration}ms)`);
    
    if (result.error) {
      console.log(`   错误: ${result.error.message}`);
    }
  }

  onEnd(result: { status: string }) {
    const endTime = Date.now();
    const totalDuration = endTime - this.startTime;
    
    const passed = this.testResults.filter(r => r.status === 'passed').length;
    const failed = this.testResults.filter(r => r.status === 'failed').length;
    const skipped = this.testResults.filter(r => r.status === 'skipped').length;
    const flaky = this.testResults.filter(r => r.status === 'interrupted').length;
    
    // 生成摘要
    console.log('\n' + '='.repeat(60));
    console.log('📋 测试执行摘要');
    console.log('='.repeat(60));
    console.log(`总用时: ${(totalDuration / 1000).toFixed(2)}s`);
    console.log(`✅ 通过: ${passed}`);
    console.log(`❌ 失败: ${failed}`);
    console.log(`⏭️ 跳过: ${skipped}`);
    console.log(`🔄 不稳定: ${flaky}`);
    console.log(`总计: ${this.testResults.length}`);
    console.log('='.repeat(60));

    // 生成详细报告
    this.generateReport({
      summary: {
        total: this.testResults.length,
        passed,
        failed,
        skipped,
        flaky,
        duration: totalDuration,
        timestamp: new Date().toISOString()
      },
      tests: this.testResults
    });
  }

  private generateReport(data: any) {
    const reportDir = path.join(process.cwd(), 'custom-report');
    if (!fs.existsSync(reportDir)) {
      fs.mkdirSync(reportDir, { recursive: true });
    }

    // 生成 JSON 报告
    fs.writeFileSync(
      path.join(reportDir, 'test-report.json'),
      JSON.stringify(data, null, 2)
    );

    // 生成 HTML 报告
    const htmlContent = this.generateHTML(data);
    fs.writeFileSync(
      path.join(reportDir, 'test-report.html'),
      htmlContent
    );

    console.log(`\n📄 自定义报告已生成: ${reportDir}/test-report.html`);
  }

  private generateHTML(data: any): string {
    const { summary, tests } = data;
    const passRate = ((summary.passed / summary.total) * 100).toFixed(1);
    
    return `<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>CCMS E2E 测试报告</title>
    <style>
        * { margin: 0; padding: 0; box-sizing: border-box; }
        body { 
            font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
            background: #f5f5f5;
            padding: 20px;
        }
        .container { max-width: 1400px; margin: 0 auto; }
        .header { 
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: white;
            padding: 30px;
            border-radius: 12px;
            margin-bottom: 20px;
        }
        .header h1 { font-size: 28px; margin-bottom: 10px; }
        .header .timestamp { opacity: 0.9; }
        .summary-cards {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
            gap: 15px;
            margin-bottom: 20px;
        }
        .card {
            background: white;
            padding: 20px;
            border-radius: 8px;
            box-shadow: 0 2px 4px rgba(0,0,0,0.1);
        }
        .card h3 { font-size: 14px; color: #666; margin-bottom: 8px; }
        .card .value { font-size: 32px; font-weight: bold; }
        .card.passed .value { color: #52c41a; }
        .card.failed .value { color: #f5222d; }
        .card.skipped .value { color: #faad14; }
        .card.total .value { color: #1890ff; }
        .progress-bar {
            background: white;
            padding: 20px;
            border-radius: 8px;
            margin-bottom: 20px;
        }
        .progress {
            height: 30px;
            background: #f0f0f0;
            border-radius: 15px;
            overflow: hidden;
            display: flex;
        }
        .progress-segment {
            height: 100%;
            transition: width 0.3s ease;
        }
        .progress-segment.passed { background: #52c41a; }
        .progress-segment.failed { background: #f5222d; }
        .progress-segment.skipped { background: #faad14; }
        .test-list {
            background: white;
            border-radius: 8px;
            overflow: hidden;
        }
        .test-list-header {
            background: #fafafa;
            padding: 15px 20px;
            border-bottom: 1px solid #e8e8e8;
            font-weight: 600;
        }
        .test-item {
            padding: 15px 20px;
            border-bottom: 1px solid #e8e8e8;
            display: flex;
            align-items: center;
            gap: 15px;
        }
        .test-item:last-child { border-bottom: none; }
        .test-item:hover { background: #f5f5f5; }
        .status-icon {
            width: 24px;
            height: 24px;
            border-radius: 50%;
            display: flex;
            align-items: center;
            justify-content: center;
            font-size: 14px;
        }
        .status-icon.passed { background: #f6ffed; color: #52c41a; }
        .status-icon.failed { background: #fff1f0; color: #f5222d; }
        .status-icon.skipped { background: #fffbe6; color: #faad14; }
        .test-info { flex: 1; }
        .test-title { font-weight: 500; margin-bottom: 4px; }
        .test-meta { font-size: 12px; color: #999; }
        .test-duration { color: #666; font-size: 14px; }
        .filter-buttons {
            display: flex;
            gap: 10px;
            margin-bottom: 15px;
        }
        .filter-btn {
            padding: 8px 16px;
            border: 1px solid #d9d9d9;
            background: white;
            border-radius: 4px;
            cursor: pointer;
            transition: all 0.3s;
        }
        .filter-btn:hover, .filter-btn.active {
            background: #1890ff;
            color: white;
            border-color: #1890ff;
        }
    </style>
</head>
<body>
    <div class="container">
        <div class="header">
            <h1>🧪 CCMS E2E 测试报告</h1>
            <div class="timestamp">生成时间: ${new Date(summary.timestamp).toLocaleString('zh-CN')}</div>
        </div>
        
        <div class="summary-cards">
            <div class="card total">
                <h3>总测试数</h3>
                <div class="value">${summary.total}</div>
            </div>
            <div class="card passed">
                <h3>通过</h3>
                <div class="value">${summary.passed}</div>
            </div>
            <div class="card failed">
                <h3>失败</h3>
                <div class="value">${summary.failed}</div>
            </div>
            <div class="card skipped">
                <h3>跳过</h3>
                <div class="value">${summary.skipped}</div>
            </div>
        </div>
        
        <div class="progress-bar">
            <h3 style="margin-bottom: 10px;">通过率: ${passRate}%</h3>
            <div class="progress">
                <div class="progress-segment passed" style="width: ${(summary.passed/summary.total)*100}%"></div>
                <div class="progress-segment failed" style="width: ${(summary.failed/summary.total)*100}%"></div>
                <div class="progress-segment skipped" style="width: ${(summary.skipped/summary.total)*100}%"></div>
            </div>
        </div>
        
        <div class="filter-buttons">
            <button class="filter-btn active" onclick="filterTests('all')">全部</button>
            <button class="filter-btn" onclick="filterTests('passed')">通过</button>
            <button class="filter-btn" onclick="filterTests('failed')">失败</button>
            <button class="filter-btn" onclick="filterTests('skipped')">跳过</button>
        </div>
        
        <div class="test-list">
            <div class="test-list-header">测试用例详情</div>
            ${tests.map(test => `
                <div class="test-item" data-status="${test.status}">
                    <div class="status-icon ${test.status}">
                        ${test.status === 'passed' ? '✓' : test.status === 'failed' ? '✗' : '⊘'}
                    </div>
                    <div class="test-info">
                        <div class="test-title">${test.title}</div>
                        <div class="test-meta">${test.file}:${test.line} · ${test.project}</div>
                    </div>
                    <div class="test-duration">${test.duration}ms</div>
                </div>
            `).join('')}
        </div>
    </div>
    
    <script>
        function filterTests(status) {
            const items = document.querySelectorAll('.test-item');
            const buttons = document.querySelectorAll('.filter-btn');
            
            buttons.forEach(btn => btn.classList.remove('active'));
            event.target.classList.add('active');
            
            items.forEach(item => {
                if (status === 'all' || item.dataset.status === status) {
                    item.style.display = 'flex';
                } else {
                    item.style.display = 'none';
                }
            });
        }
    </script>
</body>
</html>`;
  }
}

export default CustomReporter;
