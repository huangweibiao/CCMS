import { describe, it, expect } from 'vitest';
import {
  formatAmount,
  formatDate,
  formatDateTime,
  truncateText,
  formatFileSize,
  validateEmail,
  validatePhone,
  formatPercent,
  formatDuration
} from '../../utils/format';

describe('Format Utilities', () => {
  describe('Amount Formatting', () => {
    it('should format positive amount correctly', () => {
      expect(formatAmount(1234.56)).toBe('¥1,234.56');
      expect(formatAmount(1234567.89)).toBe('¥1,234,567.89');
    });

    it('should format negative amount correctly', () => {
      expect(formatAmount(-1234.56)).toBe('-¥1,234.56');
      expect(formatAmount(-1234567.89)).toBe('-¥1,234,567.89');
    });

    it('should format zero amount correctly', () => {
      expect(formatAmount(0)).toBe('¥0.00');
      expect(formatAmount(0.0)).toBe('¥0.00');
    });

    it('should handle decimal precision', () => {
      expect(formatAmount(1234.5678)).toBe('¥1,234.57'); // 四舍五入
      expect(formatAmount(1234.561, 3)).toBe('¥1,234.561'); // 自定义精度
    });

    it('should handle null and undefined values', () => {
      expect(formatAmount(null as any)).toBe('¥0.00');
      expect(formatAmount(undefined as any)).toBe('¥0.00');
    });

    it('should handle string inputs', () => {
      expect(formatAmount('1234.56' as any)).toBe('¥1,234.56');
      expect(formatAmount('invalid' as any)).toBe('¥0.00');
    });
  });

  describe('Date Formatting', () => {
    it('should format date string correctly', () => {
      expect(formatDate('2023-10-15')).toBe('2023-10-15');
      expect(formatDate('2023/10/15')).toBe('2023-10-15');
    });

    it('should format Date object correctly', () => {
      const date = new Date(2023, 9, 15); // 月份从0开始
      expect(formatDate(date)).toBe('2023-10-15');
    });

    it('should handle custom format', () => {
      const date = new Date(2023, 9, 15);
      expect(formatDate(date, 'YYYY/MM/DD')).toBe('2023/10/15');
      expect(formatDate(date, 'MM-DD-YYYY')).toBe('10-15-2023');
    });

    it('should handle null and undefined values', () => {
      expect(formatDate(null as any)).toBe('');
      expect(formatDate(undefined as any)).toBe('');
    });

    it('should handle invalid date string', () => {
      expect(formatDate('invalid-date')).toBe('');
    });
  });

  describe('DateTime Formatting', () => {
    it('should format datetime string correctly', () => {
      expect(formatDateTime('2023-10-15T14:30:00')).toBe('2023-10-15 14:30:00');
      expect(formatDateTime('2023/10/15 14:30:00')).toBe('2023-10-15 14:30:00');
    });

    it('should format Date object correctly', () => {
      const date = new Date(2023, 9, 15, 14, 30, 0);
      expect(formatDateTime(date)).toBe('2023-10-15 14:30:00');
    });

    it('should handle custom format', () => {
      const date = new Date(2023, 9, 15, 14, 30, 0);
      expect(formatDateTime(date, 'YYYY/MM/DD HH:mm')).toBe('2023/10/15 14:30');
      expect(formatDateTime(date, 'MM/DD/YYYY HH:mm:ss')).toBe('10/15/2023 14:30:00');
    });

    it('should handle null and undefined values', () => {
      expect(formatDateTime(null as any)).toBe('');
      expect(formatDateTime(undefined as any)).toBe('');
    });
  });

  describe('Text Truncation', () => {
    it('should truncate long text correctly', () => {
      const longText = '这是一段很长的文本，需要进行截断处理';
      expect(truncateText(longText, 10)).toBe('这是一段很长...');
      expect(truncateText(longText, 5)).toBe('这是一...');
    });

    it('should not truncate short text', () => {
      const shortText = '短文本';
      expect(truncateText(shortText, 10)).toBe('短文本');
      expect(truncateText(shortText, 3)).toBe('短文本');
    });

    it('should handle custom suffix', () => {
      const longText = '这是一段很长的文本';
      expect(truncateText(longText, 5, '***')).toBe('这是一***');
    });

    it('should handle null and undefined values', () => {
      expect(truncateText(null as any, 10)).toBe('');
      expect(truncateText(undefined as any, 10)).toBe('');
    });

    it('should handle empty string', () => {
      expect(truncateText('', 10)).toBe('');
    });
  });

  describe('File Size Formatting', () => {
    it('should format bytes correctly', () => {
      expect(formatFileSize(1023)).toBe('1023 B');
      expect(formatFileSize(1024)).toBe('1 KB');
      expect(formatFileSize(1048576)).toBe('1 MB');
      expect(formatFileSize(1073741824)).toBe('1 GB');
    });

    it('should format decimal sizes correctly', () => {
      expect(formatFileSize(1536)).toBe('1.5 KB');
      expect(formatFileSize(1572864)).toBe('1.5 MB');
      expect(formatFileSize(1610612736)).toBe('1.5 GB');
    });

    it('should handle zero size', () => {
      expect(formatFileSize(0)).toBe('0 B');
    });

    it('should handle negative size', () => {
      expect(formatFileSize(-1024)).toBe('0 B');
    });

    it('should handle null and undefined values', () => {
      expect(formatFileSize(null as any)).toBe('0 B');
      expect(formatFileSize(undefined as any)).toBe('0 B');
    });
  });

  describe('Email Validation', () => {
    it('should validate correct email addresses', () => {
      expect(validateEmail('test@example.com')).toBe(true);
      expect(validateEmail('user.name+tag@example.co.uk')).toBe(true);
      expect(validateEmail('test@sub.domain.com')).toBe(true);
    });

    it('should reject invalid email addresses', () => {
      expect(validateEmail('invalid-email')).toBe(false);
      expect(validateEmail('test@')).toBe(false);
      expect(validateEmail('@example.com')).toBe(false);
      expect(validateEmail('test@.com')).toBe(false);
      expect(validateEmail('')).toBe(false);
    });

    it('should handle null and undefined values', () => {
      expect(validateEmail(null as any)).toBe(false);
      expect(validateEmail(undefined as any)).toBe(false);
    });
  });

  describe('Phone Validation', () => {
    it('should validate correct phone numbers', () => {
      expect(validatePhone('13800138000')).toBe(true);
      expect(validatePhone('15555555555')).toBe(true);
      expect(validatePhone('18812345678')).toBe(true);
    });

    it('should reject invalid phone numbers', () => {
      expect(validatePhone('1234567890')).toBe(false); // 太短
      expect(validatePhone('138001380001')).toBe(false); // 太长
      expect(validatePhone('abcdefghijk')).toBe(false); // 包含字母
      expect(validatePhone('')).toBe(false); // 空字符串
    });

    it('should handle null and undefined values', () => {
      expect(validatePhone(null as any)).toBe(false);
      expect(validatePhone(undefined as any)).toBe(false);
    });
  });

  describe('Percentage Formatting', () => {
    it('should format decimal to percentage correctly', () => {
      expect(formatPercent(0.1234)).toBe('12.34%');
      expect(formatPercent(0.5)).toBe('50.00%');
      expect(formatPercent(1)).toBe('100.00%');
      expect(formatPercent(0.05678, 1)).toBe('5.7%'); // 自定义精度
    });

    it('should handle zero and negative values', () => {
      expect(formatPercent(0)).toBe('0.00%');
      expect(formatPercent(-0.1)).toBe('-10.00%');
    });

    it('should handle null and undefined values', () => {
      expect(formatPercent(null as any)).toBe('0.00%');
      expect(formatPercent(undefined as any)).toBe('0.00%');
    });
  });

  describe('Duration Formatting', () => {
    it('should format milliseconds correctly', () => {
      expect(formatDuration(1000)).toBe('1秒');
      expect(formatDuration(60000)).toBe('1分钟');
      expect(formatDuration(3600000)).toBe('1小时');
      expect(formatDuration(86400000)).toBe('1天');
    });

    it('should format complex durations correctly', () => {
      expect(formatDuration(3661000)).toBe('1小时1分钟1秒');
      expect(formatDuration(90061000)).toBe('1天1小时1分钟1秒');
    });

    it('should handle zero duration', () => {
      expect(formatDuration(0)).toBe('0秒');
    });

    it('should handle negative duration', () => {
      expect(formatDuration(-1000)).toBe('0秒');
    });

    it('should handle null and undefined values', () => {
      expect(formatDuration(null as any)).toBe('0秒');
      expect(formatDuration(undefined as any)).toBe('0秒');
    });

    it('should handle custom units', () => {
      expect(formatDuration(1000, { seconds: 's', minutes: 'm', hours: 'h', days: 'd' }))
        .toBe('1s');
      expect(formatDuration(3661000, { seconds: '秒', minutes: '分', hours: '时', days: '天' }))
        .toBe('1时1分1秒');
    });
  });

  describe('Edge Cases', () => {
    it('should handle very large numbers in amount formatting', () => {
      expect(formatAmount(999999999999.99)).toBe('¥999,999,999,999.99');
    });

    it('should handle very small numbers in amount formatting', () => {
      expect(formatAmount(0.0001)).toBe('¥0.00');
    });

    it('should handle very long text truncation', () => {
      const veryLongText = 'a'.repeat(1000);
      expect(truncateText(veryLongText, 100)).toHaveLength(103); // 100 + '...'
    });

    it('should handle very large file sizes', () => {
      expect(formatFileSize(1099511627776)).toBe('1 TB'); // 1 TB
    });
  });
});