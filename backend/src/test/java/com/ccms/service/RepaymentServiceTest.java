package com.ccms.service;

import com.ccms.BaseTest;
import com.ccms.entity.expense.LoanMain;
import com.ccms.entity.expense.RepaymentRecord;
import com.ccms.repository.expense.LoanMainRepository;
import com.ccms.repository.expense.RepaymentRecordRepository;
import com.ccms.service.impl.RepaymentServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * 还款服务测试类
 */
class RepaymentServiceTest extends BaseTest {

    @Mock
    private RepaymentRecordRepository repaymentRecordRepository;
    
    @Mock
    private LoanMainRepository loanMainRepository;

    @Mock
    private LoanService loanService;

    @InjectMocks
    private RepaymentServiceImpl repaymentService;

    private RepaymentRecord testRepayment;
    private LoanMain testLoan;

    @BeforeEach
    public void setUp() {
        super.setUp();
        testRepayment = createTestRepayment();
        testLoan = createTestLoan();
    }

    /**
     * 创建测试还款数据
     */
    private RepaymentRecord createTestRepayment() {
        RepaymentRecord repayment = new RepaymentRecord();
        repayment.setId(1L);
        repayment.setLoanId(1L);
        repayment.setRepayAmount(new BigDecimal("1000.00"));
        repayment.setRepayDate(LocalDate.now());
        repayment.setRepayType(1); // 现金还款
        repayment.setRepayBy(1L); // 还款人ID
        repayment.setRemark("正常还款");
        return repayment;
    }

    /**
     * 创建测试借款数据
     */
    private LoanMain createTestLoan() {
        LoanMain loan = new LoanMain();
        loan.setId(1L);
        loan.setLoanAmount(new BigDecimal("5000.00"));
        loan.setRepaidAmount(new BigDecimal("2000.00"));
        loan.setStatus(2); // 已放款状态
        return loan;
    }

    @Test
    @DisplayName("创建还款记录验证默认设置")
    void createRepayment_ShouldSetCorrectDefaults() {
        // Arrange
        when(repaymentRecordRepository.save(any(RepaymentRecord.class))).thenReturn(testRepayment);
        when(loanService.getLoanById(1L)).thenReturn(testLoan);
        
        // Clear repayDate to test default date setting
        testRepayment.setRepayDate(null);

        // Act
        RepaymentRecord result = repaymentService.createRepayment(testRepayment);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getRepayDate()).isNotNull(); // Should be set to current date
        verify(repaymentRecordRepository, times(1)).save(testRepayment);
    }

    @Test
    @DisplayName("根据ID获取还款记录-存在")
    void getRepaymentById_WhenRepaymentExists_ShouldReturnRepayment() {
        // Arrange
        when(repaymentRecordRepository.findById(1L)).thenReturn(Optional.of(testRepayment));

        // Act
        RepaymentRecord result = repaymentService.getRepaymentById(1L);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        verify(repaymentRecordRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("根据ID获取还款记录-不存在")
    void getRepaymentById_WhenRepaymentNotExists_ShouldThrowException() {
        // Arrange
        when(repaymentRecordRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> repaymentService.getRepaymentById(999L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("还款记录不存在");
        
        verify(repaymentRecordRepository, times(1)).findById(999L);
    }

    @Test
    @DisplayName("根据借款单ID获取还款记录")
    void getRepaymentsByLoanId_ShouldReturnRepaymentsList() {
        // Arrange
        List<RepaymentRecord> repayments = Arrays.asList(testRepayment);
        when(repaymentRecordRepository.findByLoanId(1L)).thenReturn(repayments);

        // Act
        List<RepaymentRecord> result = repaymentService.getRepaymentsByLoanId(1L);

        // Assert
        assertThat(result).isNotEmpty();
        assertThat(result.size()).isEqualTo(1);
        assertThat(result.get(0).getLoanId()).isEqualTo(1L);
        verify(repaymentRecordRepository, times(1)).findByLoanId(1L);
    }

    @Test
    @DisplayName("根据状态查询还款记录")
    void findRepaymentsByStatus_ShouldReturnAllRepayments() {
        // Arrange
        List<RepaymentRecord> repayments = Arrays.asList(testRepayment);
        when(repaymentRecordRepository.findAll()).thenReturn(repayments);

        // Act
        List<RepaymentRecord> result = repaymentService.findRepaymentsByStatus(1);

        // Assert
        assertThat(result).isNotEmpty();
        assertThat(result.get(0).getRepayType()).isEqualTo(1);
        verify(repaymentRecordRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("获取日期范围内的还款记录")
    void getRepaymentsByDateRange_ShouldReturnFilteredRepayments() {
        // Arrange
        LocalDate startDate = LocalDate.now().minusDays(30);
        LocalDate endDate = LocalDate.now();
        List<RepaymentRecord> repayments = Arrays.asList(testRepayment);
        when(repaymentRecordRepository.findByRepayDateRange(startDate, endDate)).thenReturn(repayments);

        // Act
        List<RepaymentRecord> result = repaymentService.getRepaymentsByDateRange(startDate, endDate);

        // Assert
        assertThat(result).isNotEmpty();
        assertThat(result.get(0).getRepayDate()).isEqualTo(LocalDate.now());
        verify(repaymentRecordRepository, times(1)).findByRepayDateRange(startDate, endDate);
    }

    @Test
    @DisplayName("统计借款单的总还款金额")
    void getTotalRepaymentAmount_ShouldReturnCorrectAmount() {
        // Arrange
        when(repaymentRecordRepository.sumRepayAmountByLoanId(1L)).thenReturn(new BigDecimal("3000.00"));

        // Act
        BigDecimal result = repaymentService.getTotalRepaymentAmount(1L);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(new BigDecimal("3000.00"));
        verify(repaymentRecordRepository, times(1)).sumRepayAmountByLoanId(1L);
    }

    @Test
    @DisplayName("统计借款单的总还款金额-无记录")
    void getTotalRepaymentAmount_WhenNoRecords_ShouldReturnZero() {
        // Arrange
        when(repaymentRecordRepository.sumRepayAmountByLoanId(999L)).thenReturn(null);

        // Act
        BigDecimal result = repaymentService.getTotalRepaymentAmount(999L);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(BigDecimal.ZERO);
        verify(repaymentRecordRepository, times(1)).sumRepayAmountByLoanId(999L);
    }

    @Test
    @DisplayName("更新还款记录-报销抵扣类型")
    void updateRepayment_ForReimburseType_ShouldSuccess() {
        // Arrange
        RepaymentRecord reimburseRepayment = createTestRepayment();
        reimburseRepayment.setRepayType(2); // 报销抵扣类型
        when(repaymentRecordRepository.findById(1L)).thenReturn(Optional.of(reimburseRepayment));
        when(repaymentRecordRepository.save(any(RepaymentRecord.class))).thenReturn(reimburseRepayment);
        when(loanService.getLoanById(1L)).thenReturn(testLoan);

        // Act
        RepaymentRecord result = repaymentService.updateRepayment(reimburseRepayment);

        // Assert
        assertThat(result).isNotNull();
        verify(repaymentRecordRepository, times(1)).findById(1L);
        verify(repaymentRecordRepository, times(1)).save(any(RepaymentRecord.class));
    }

    @Test
    @DisplayName("更新还款记录-非报销抵扣类型")
    void updateRepayment_ForNonReimburseType_ShouldThrowException() {
        // Arrange
        when(repaymentRecordRepository.findById(1L)).thenReturn(Optional.of(testRepayment));
        when(loanService.getLoanById(1L)).thenReturn(testLoan);

        // Act & Assert
        assertThatThrownBy(() -> repaymentService.updateRepayment(testRepayment))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("只能修改报销抵扣类型的还款记录");
        
        verify(repaymentRecordRepository, times(1)).findById(1L);
        verify(repaymentRecordRepository, never()).save(any(RepaymentRecord.class));
    }

    @Test
    @DisplayName("还款确认")
    void confirmRepayment_ShouldSuccess() {
        // Arrange
        when(repaymentRecordRepository.findById(1L)).thenReturn(Optional.of(testRepayment));

        // Act
        RepaymentRecord result = repaymentService.confirmRepayment(1L);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        verify(repaymentRecordRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("删除还款记录")
    void deleteRepayment_ShouldSuccess() {
        // Arrange
        when(repaymentRecordRepository.findById(1L)).thenReturn(Optional.of(testRepayment));

        // Act
        repaymentService.deleteRepayment(1L);

        // Assert
        verify(repaymentRecordRepository, times(1)).findById(1L);
        verify(repaymentRecordRepository, times(1)).delete(testRepayment);
    }

    @Test
    @DisplayName("删除还款记录-记录不存在")
    void deleteRepayment_WhenRecordNotExists_ShouldThrowException() {
        // Arrange
        when(repaymentRecordRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> repaymentService.deleteRepayment(999L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("还款记录不存在");
        
        verify(repaymentRecordRepository, times(1)).findById(999L);
        verify(repaymentRecordRepository, never()).delete(any(RepaymentRecord.class));
    }

    @Test
    @DisplayName("分页查询还款记录")
    void findRepayments_ShouldReturnPage() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<RepaymentRecord> repaymentPage = new PageImpl<>(Arrays.asList(testRepayment), pageable, 1);
        when(repaymentRecordRepository.findAll(pageable)).thenReturn(repaymentPage);

        // Act
        Page<RepaymentRecord> result = repaymentService.findRepayments(pageable);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getContent()).isNotEmpty();
        assertThat(result.getTotalElements()).isEqualTo(1);
        verify(repaymentRecordRepository, times(1)).findAll(pageable);
    }

    @Test
    @DisplayName("还款前校验-成功")
    void validateRepayment_ShouldReturnTrue() {
        // Arrange
        when(loanService.getLoanById(1L)).thenReturn(testLoan);

        // Act
        boolean result = repaymentService.validateRepayment(1L, new BigDecimal("1000.00"));

        // Assert
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("还款前校验-金额无效")
    void validateRepayment_WhenAmountInvalid_ShouldThrowException() {
        // Arrange
        when(loanService.getLoanById(1L)).thenReturn(testLoan);

        // Act & Assert
        assertThatThrownBy(() -> repaymentService.validateRepayment(1L, BigDecimal.ZERO))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("还款金额必须大于0");
    }
}