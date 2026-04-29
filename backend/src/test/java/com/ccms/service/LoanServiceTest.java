package com.ccms.service;

import com.ccms.BaseTest;
import com.ccms.entity.expense.LoanMain;
import com.ccms.repository.expense.LoanMainRepository;
import com.ccms.service.impl.LoanServiceImpl;
import com.ccms.service.MessageService;
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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * 借款服务测试类
 */
class LoanServiceTest extends BaseTest {

    @Mock
    private LoanMainRepository loanMainRepository;
    
    @Mock
    private MessageService messageService;

    @InjectMocks
    private LoanServiceImpl loanService;

    private LoanMain testLoan;

    @BeforeEach
    public void setUp() {
        super.setUp();
        testLoan = createTestLoan();
    }

    /**
     * 创建测试借款数据
     */
    private LoanMain createTestLoan() {
        LoanMain loan = new LoanMain();
        loan.setId(1L);
        loan.setLoanNo("LN202504290001");
        loan.setLoanUserId(1L);
        loan.setLoanDeptId(1L);
        loan.setLoanAmount(new BigDecimal("5000.00"));
        loan.setRepaidAmount(new BigDecimal("0.00"));
        loan.setExpectRepayDate(LocalDate.now().plusDays(30));
        loan.setStatus(1);
        loan.setPurpose("出差备用金");
        return loan;
    }

    @Test
    @DisplayName("申请借款-成功")
    void applyLoan_ShouldSuccess() {
        // Arrange
        testLoan.setStatus(null); // 清除状态，让applyLoan自动设置
        when(loanMainRepository.save(any(LoanMain.class))).thenReturn(testLoan);

        // Act
        LoanMain result = loanService.applyLoan(testLoan);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getLoanNo()).startsWith("LN");
        assertThat(result.getLoanAmount()).isEqualTo(new BigDecimal("5000.00"));
        assertThat(result.getStatus()).isEqualTo(0); // 草稿状态
        verify(loanMainRepository, times(1)).save(testLoan);
    }

    @Test
    @DisplayName("根据ID获取借款单-存在")
    void getLoanById_WhenLoanExists_ShouldReturnLoan() {
        // Arrange
        when(loanMainRepository.findById(1L)).thenReturn(Optional.of(testLoan));

        // Act
        LoanMain result = loanService.getLoanById(1L);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        verify(loanMainRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("根据ID获取借款单-不存在")
    void getLoanById_WhenLoanNotExists_ShouldThrowException() {
        // Arrange
        when(loanMainRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> loanService.getLoanById(999L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("借款单不存在");
        
        verify(loanMainRepository, times(1)).findById(999L);
    }

    @Test
    @DisplayName("根据用户ID获取借款列表")
    void getLoansByUserId_ShouldReturnLoansList() {
        // Arrange
        List<LoanMain> loans = Arrays.asList(testLoan);
        when(loanMainRepository.findByLoanUserId(1L)).thenReturn(loans);

        // Act
        List<LoanMain> result = loanService.getLoansByUserId(1L);

        // Assert
        assertThat(result).isNotEmpty();
        assertThat(result.size()).isEqualTo(1);
        assertThat(result.get(0).getLoanUserId()).isEqualTo(1L);
        verify(loanMainRepository, times(1)).findByLoanUserId(1L);
    }

    @Test
    @DisplayName("根据用户ID获取分页借款列表")
    void getLoansByUserIdWithPagination_ShouldReturnPage() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<LoanMain> loanPage = new PageImpl<>(Arrays.asList(testLoan), pageable, 1);
        when(loanMainRepository.findByLoanUserId(1L, pageable)).thenReturn(loanPage);

        // Act
        Page<LoanMain> result = loanService.getLoansByUserId(1L, pageable);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getContent()).isNotEmpty();
        assertThat(result.getTotalElements()).isEqualTo(1);
        verify(loanMainRepository, times(1)).findByLoanUserId(1L, pageable);
    }

    @Test
    @DisplayName("根据状态获取借款列表")
    void getLoansByStatus_ShouldReturnFilteredLoans() {
        // Arrange
        List<LoanMain> loans = Arrays.asList(testLoan);
        when(loanMainRepository.findByStatus(1)).thenReturn(loans);

        // Act
        List<LoanMain> result = loanService.getLoansByStatus(1);

        // Assert
        assertThat(result).isNotEmpty();
        assertThat(result.get(0).getStatus()).isEqualTo(1);
        verify(loanMainRepository, times(1)).findByStatus(1);
    }

    @Test
    @DisplayName("更新借款单信息")
    void updateLoan_ShouldSuccess() {
        // Arrange
        LoanMain updatedLoan = createTestLoan();
        updatedLoan.setPurpose("修改后的用途");
        updatedLoan.setStatus(0); // 草稿状态才能更新
        
        // 模拟借款单存在（状态为草稿0，才能被修改）
        LoanMain existingLoan = createTestLoan();
        existingLoan.setStatus(0); // 草稿状态
        
        when(loanMainRepository.findById(1L)).thenReturn(Optional.of(existingLoan));
        when(loanMainRepository.save(any(LoanMain.class))).thenReturn(updatedLoan);

        // Act
        LoanMain result = loanService.updateLoan(updatedLoan);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getPurpose()).isEqualTo("修改后的用途");
        verify(loanMainRepository, times(1)).findById(1L);
        verify(loanMainRepository, times(1)).save(any(LoanMain.class));
    }

    @Test
    @DisplayName("获取用户借款总额")
    void getUserTotalLoanAmount_ShouldReturnCorrectAmount() {
        // Arrange
        when(loanMainRepository.sumLoanAmountByUserId(1L)).thenReturn(new BigDecimal("10000.00"));

        // Act
        BigDecimal result = loanService.getUserTotalLoanAmount(1L);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(new BigDecimal("10000.00"));
        verify(loanMainRepository, times(1)).sumLoanAmountByUserId(1L);
    }

    @Test
    @DisplayName("获取用户未还余额")
    void getUserUnpaidBalance_ShouldReturnCorrectBalance() {
        // Arrange
        when(loanMainRepository.sumBalanceAmountByUserId(1L)).thenReturn(new BigDecimal("5000.00"));

        // Act
        BigDecimal result = loanService.getUserUnpaidBalance(1L);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(new BigDecimal("5000.00"));
        verify(loanMainRepository, times(1)).sumBalanceAmountByUserId(1L);
    }

    @Test
    @DisplayName("审批借款申请-成功")
    void approveLoan_WhenLoanExists_ShouldSuccess() {
        // Arrange
        when(loanMainRepository.findById(1L)).thenReturn(Optional.of(testLoan));
        when(loanMainRepository.save(any(LoanMain.class))).thenReturn(testLoan);

        // Act
        boolean result = loanService.approveLoan(1L, "审批通过");

        // Assert
        assertThat(result).isTrue();
        verify(loanMainRepository, times(1)).findById(1L);
        verify(loanMainRepository, times(1)).save(testLoan);
    }

    @Test
    @DisplayName("审批借款申请-借款不存在")
    void approveLoan_WhenLoanNotExists_ShouldThrowException() {
        // Arrange
        when(loanMainRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> loanService.approveLoan(999L, "审批通过"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("借款单不存在");
        
        verify(loanMainRepository, times(1)).findById(999L);
        verify(loanMainRepository, never()).save(any(LoanMain.class));
    }

    @Test
    @DisplayName("生成借款单号")
    void generateLoanNo_ShouldGenerateValidNumber() {
        // Act
        String loanNo = loanService.generateLoanNo();

        // Assert
        assertThat(loanNo).isNotNull();
        assertThat(loanNo).startsWith("LN");
        assertThat(loanNo).hasSize(15); // LN + 8位日期 + 5位时间戳
    }

    @Test
    @DisplayName("检查用户是否可以借款-无逾期且金额合规")
    void canUserBorrow_WhenNoOverdue_ShouldReturnTrue() {
        // Arrange: 模拟没有逾期借款
        when(loanMainRepository.findOverdueLoans()).thenReturn(Arrays.asList());
        when(loanMainRepository.findByRepayDateRange(any(), any())).thenReturn(Arrays.asList());

        // Act
        boolean result = loanService.canUserBorrow(1L, new BigDecimal("5000.00"));

        // Assert
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("检查用户是否可以借款-有逾期借款")
    void canUserBorrow_WhenHasOverdue_ShouldReturnFalse() {
        // Arrange: 模拟有逾期借款
        LoanMain overdueLoan = createTestLoan();
        overdueLoan.setStatus(3); // 逾期状态
        when(loanMainRepository.findOverdueLoans()).thenReturn(Arrays.asList(overdueLoan));

        // Act
        boolean result = loanService.canUserBorrow(1L, new BigDecimal("5000.00"));

        // Assert
        assertThat(result).isFalse();
    }
}