package com.invoice.app.service;

import com.invoice.app.config.GlobalExceptionHandler;
import com.invoice.app.dto.InvoiceDTO;
import com.invoice.app.dto.InvoiceItemDTO;
import com.invoice.app.dto.MonthlyStatementDTO;
import com.invoice.app.entity.Client;
import com.invoice.app.entity.Invoice;
import com.invoice.app.entity.InvoiceItem;
import com.invoice.app.repository.ClientRepository;
import com.invoice.app.repository.InvoiceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InvoiceServiceTest {

    @Mock
    private InvoiceRepository invoiceRepository;

    @Mock
    private ClientRepository clientRepository;

    @InjectMocks
    private InvoiceService invoiceService;

    private Invoice testInvoice;
    private InvoiceDTO testInvoiceDTO;

    @BeforeEach
    void setUp() {
        testInvoice = new Invoice();
        testInvoice.setId(1L);
        testInvoice.setInvoiceNo("INV-001");
        testInvoice.setInvoiceDate(LocalDate.of(2024, 1, 15));
        testInvoice.setPartyName("Test Client");
        testInvoice.setTotalAmount(new BigDecimal("5000.00"));

        testInvoiceDTO = new InvoiceDTO();
        testInvoiceDTO.setInvoiceNo("INV-001");
        testInvoiceDTO.setInvoiceDate("2024-01-15");
        testInvoiceDTO.setPartyName("Test Client");
        testInvoiceDTO.setTotalAmount(new BigDecimal("5000.00"));
    }

    @Test
    void createInvoice_shouldSaveAndReturnInvoice() {
        when(invoiceRepository.save(any(Invoice.class))).thenReturn(testInvoice);

        InvoiceDTO result = invoiceService.createInvoice(testInvoiceDTO);

        assertNotNull(result);
        assertEquals("INV-001", result.getInvoiceNo());
        assertEquals("Test Client", result.getPartyName());
        verify(invoiceRepository, times(1)).save(any(Invoice.class));
    }

    @Test
    void getInvoice_shouldReturnInvoice() {
        when(invoiceRepository.findById(1L)).thenReturn(Optional.of(testInvoice));

        InvoiceDTO result = invoiceService.getInvoice(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("INV-001", result.getInvoiceNo());
    }

    @Test
    void getInvoice_shouldThrowExceptionWhenNotFound() {
        when(invoiceRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(GlobalExceptionHandler.ResourceNotFoundException.class, () -> invoiceService.getInvoice(999L));
    }

    @Test
    void searchInvoices_shouldReturnMatchingInvoices() {
        Invoice invoice2 = new Invoice();
        invoice2.setId(2L);
        invoice2.setInvoiceNo("INV-002");

        when(invoiceRepository.searchInvoices(eq("INV"), isNull()))
                .thenReturn(Arrays.asList(testInvoice, invoice2));

        List<InvoiceDTO> results = invoiceService.searchInvoices("INV", null);

        assertNotNull(results);
        assertEquals(2, results.size());
    }

    @Test
    void getMonthlyStatement_shouldReturnStatement() {
        Invoice invoice1 = new Invoice();
        invoice1.setId(1L);
        invoice1.setInvoiceNo("INV-001");
        invoice1.setTotalAmount(new BigDecimal("3000.00"));

        Invoice invoice2 = new Invoice();
        invoice2.setId(2L);
        invoice2.setInvoiceNo("INV-002");
        invoice2.setTotalAmount(new BigDecimal("2000.00"));

        when(invoiceRepository.findByYearAndMonth(2024, 1))
                .thenReturn(Arrays.asList(invoice1, invoice2));

        MonthlyStatementDTO result = invoiceService.getMonthlyStatement(2024, 1);

        assertNotNull(result);
        assertEquals(2024, result.getYear());
        assertEquals(1, result.getMonth());
        assertEquals("January", result.getMonthName());
        assertEquals(2, result.getTotalInvoices());
        assertEquals(new BigDecimal("5000.00"), result.getTotalAmount());
    }
}
