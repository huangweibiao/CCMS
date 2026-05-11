package com.ccms.controller.invoice;

import com.ccms.controller.ControllerTestBase;
import com.ccms.entity.expense.ExpenseInvoice;
import com.ccms.repository.expense.ExpenseInvoiceRepository;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 发票信息控制器单元测试
 */
@WebMvcTest(InvoiceInfoController.class)
class InvoiceInfoControllerTest extends ControllerTestBase {

    @MockBean
    private ExpenseInvoiceRepository invoiceRepository;

    private ExpenseInvoice createTestInvoice(Long id, String invoiceNo, Integer invoiceType) {
        ExpenseInvoice invoice = new ExpenseInvoice();
        invoice.setId(id);
        invoice.setInvoiceNo(invoiceNo);
        invoice.setInvoiceCode("044001900111");
        invoice.setInvoiceType(invoiceType);
        invoice.setInvoiceAmount(new BigDecimal("1000.00"));
        invoice.setTaxAmount(new BigDecimal("100.00"));
        invoice.setInvoiceDate(Date.valueOf(LocalDate.now()));
        invoice.setVerifyStatus(0);
        return invoice;
    }

    @Test
    void shouldReturnInvoiceListWhenQuerySuccess() throws Exception {
        ExpenseInvoice invoice = createTestInvoice(1L, "INV202501010001", 1);
        Page<ExpenseInvoice> page = new PageImpl<>(
                Collections.singletonList(invoice),
                PageRequest.of(0, 10),
                1
        );
        when(invoiceRepository.findAll(any(PageRequest.class))).thenReturn(page);

        performGet("/api/invoice/info")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].invoiceNo").value("INV202501010001"));
    }

    @Test
    void shouldReturnInvoiceListByInvoiceType() throws Exception {
        ExpenseInvoice invoice = createTestInvoice(1L, "INV202501010001", 1);
        when(invoiceRepository.findByInvoiceType(1)).thenReturn(Collections.singletonList(invoice));

        performGet("/api/invoice/info?invoiceType=1")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].invoiceType").value(1));
    }

    @Test
    void shouldReturnInvoiceListByVerifyStatus() throws Exception {
        ExpenseInvoice invoice = createTestInvoice(1L, "INV202501010001", 1);
        invoice.setVerifyStatus(1);
        when(invoiceRepository.findByVerifyStatus(1)).thenReturn(Collections.singletonList(invoice));

        performGet("/api/invoice/info?verifyStatus=1")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].verifyStatus").value(1));
    }

    @Test
    void shouldReturnInvoiceWhenGetByIdSuccess() throws Exception {
        ExpenseInvoice invoice = createTestInvoice(1L, "INV202501010001", 1);
        when(invoiceRepository.findById(1L)).thenReturn(Optional.of(invoice));

        performGet("/api/invoice/info/1")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.invoiceNo").value("INV202501010001"));
    }

    @Test
    void shouldReturnNotFoundWhenGetByIdNotExist() throws Exception {
        when(invoiceRepository.findById(999L)).thenReturn(Optional.empty());

        performGet("/api/invoice/info/999")
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturnInvoiceWhenGetByNoSuccess() throws Exception {
        ExpenseInvoice invoice = createTestInvoice(1L, "INV202501010001", 1);
        when(invoiceRepository.findByInvoiceNo("INV202501010001")).thenReturn(Collections.singletonList(invoice));

        performGet("/api/invoice/info/no/INV202501010001")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].invoiceNo").value("INV202501010001"));
    }

    @Test
    void shouldReturnNotFoundWhenGetByNoNotExist() throws Exception {
        when(invoiceRepository.findByInvoiceNo("NONEXISTENT")).thenReturn(Collections.emptyList());

        performGet("/api/invoice/info/no/NONEXISTENT")
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldCreateInvoiceSuccess() throws Exception {
        ExpenseInvoice invoice = createTestInvoice(1L, "INV202501010001", 1);
        when(invoiceRepository.save(any(ExpenseInvoice.class))).thenReturn(invoice);

        performPost("/api/invoice/info", invoice)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.verifyStatus").value(0));
    }

    @Test
    void shouldUpdateInvoiceSuccess() throws Exception {
        ExpenseInvoice existing = createTestInvoice(1L, "INV202501010001", 1);
        ExpenseInvoice updated = createTestInvoice(1L, "INV202501010001", 2);
        
        when(invoiceRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(invoiceRepository.save(any(ExpenseInvoice.class))).thenReturn(updated);

        performPut("/api/invoice/info/1", updated)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.invoiceType").value(2));
    }

    @Test
    void shouldReturnNotFoundWhenUpdateNonExistent() throws Exception {
        ExpenseInvoice invoice = createTestInvoice(1L, "INV202501010001", 1);
        when(invoiceRepository.findById(999L)).thenReturn(Optional.empty());

        performPut("/api/invoice/info/999", invoice)
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldDeleteInvoiceSuccess() throws Exception {
        ExpenseInvoice invoice = createTestInvoice(1L, "INV202501010001", 1);
        when(invoiceRepository.findById(1L)).thenReturn(Optional.of(invoice));

        performDelete("/api/invoice/info/1")
                .andExpect(status().isOk());

        verify(invoiceRepository, times(1)).deleteById(1L);
    }

    @Test
    void shouldReturnNotFoundWhenDeleteNonExistent() throws Exception {
        when(invoiceRepository.findById(999L)).thenReturn(Optional.empty());

        performDelete("/api/invoice/info/999")
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldVerifyInvoiceSuccess() throws Exception {
        ExpenseInvoice invoice = createTestInvoice(1L, "INV202501010001", 1);
        when(invoiceRepository.findById(1L)).thenReturn(Optional.of(invoice));
        when(invoiceRepository.save(any(ExpenseInvoice.class))).thenReturn(invoice);

        performPost("/api/invoice/info/1/verify")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("发票验真成功"))
                .andExpect(jsonPath("$.verifyStatus").value(1))
                .andExpect(jsonPath("$.verifyResult").value("验真通过"));
    }

    @Test
    void shouldReturnNotFoundWhenVerifyNonExistent() throws Exception {
        when(invoiceRepository.findById(999L)).thenReturn(Optional.empty());

        performPost("/api/invoice/info/999/verify")
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturnInvoicesByReimburseId() throws Exception {
        ExpenseInvoice invoice1 = createTestInvoice(1L, "INV202501010001", 1);
        ExpenseInvoice invoice2 = createTestInvoice(2L, "INV202501010002", 2);
        when(invoiceRepository.findByReimburseId(1L)).thenReturn(Arrays.asList(invoice1, invoice2));

        performGet("/api/invoice/info/reimburse/1")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    void shouldReturnStatisticsByReimburseDetailId() throws Exception {
        ExpenseInvoice invoice1 = createTestInvoice(1L, "INV001", 1);
        ExpenseInvoice invoice2 = createTestInvoice(2L, "INV002", 2);
        when(invoiceRepository.findByReimburseDetailId(1L)).thenReturn(Arrays.asList(invoice1, invoice2));

        performGet("/api/invoice/info/statistics?reimburseDetailId=1")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(2))
                .andExpect(jsonPath("$.totalAmount").value(2000.00))
                .andExpect(jsonPath("$.totalTax").value(200.00));
    }

    @Test
    void shouldReturnOverallStatistics() throws Exception {
        when(invoiceRepository.count()).thenReturn(100L);
        when(invoiceRepository.countByInvoiceType(1)).thenReturn(40L);
        when(invoiceRepository.countByInvoiceType(2)).thenReturn(30L);
        when(invoiceRepository.countByInvoiceType(3)).thenReturn(30L);
        when(invoiceRepository.findByVerifyStatus(0)).thenReturn(Collections.emptyList());
        when(invoiceRepository.findByVerifyStatus(1)).thenReturn(Collections.emptyList());
        when(invoiceRepository.findByVerifyStatus(2)).thenReturn(Collections.emptyList());

        performGet("/api/invoice/info/statistics")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(100))
                .andExpect(jsonPath("$.vatSpecial").value(40))
                .andExpect(jsonPath("$.vatNormal").value(30))
                .andExpect(jsonPath("$.electronic").value(30))
                .andExpect(jsonPath("$.unverified").value(0))
                .andExpect(jsonPath("$.verified").value(0))
                .andExpect(jsonPath("$.verifyFailed").value(0));
    }
}
