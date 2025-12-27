package com.invoice.app.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.invoice.app.dto.InvoiceDTO;
import com.invoice.app.dto.InvoiceItemDTO;
import com.invoice.app.entity.Invoice;
import com.invoice.app.service.InvoiceService;
import com.invoice.app.service.PdfGenerationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(InvoiceController.class)
class InvoiceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private InvoiceService invoiceService;

    @MockBean
    private PdfGenerationService pdfGenerationService;

    @Test
    void createInvoice_shouldReturnCreatedInvoice() throws Exception {
        InvoiceDTO inputDTO = new InvoiceDTO();
        inputDTO.setInvoiceNo("INV-001");
        inputDTO.setInvoiceDate("2024-01-15");
        inputDTO.setPartyName("Test Client");
        inputDTO.setTotalAmount(new BigDecimal("5000.00"));

        InvoiceDTO outputDTO = new InvoiceDTO();
        outputDTO.setId(1L);
        outputDTO.setInvoiceNo("INV-001");
        outputDTO.setInvoiceDate("2024-01-15");
        outputDTO.setPartyName("Test Client");
        outputDTO.setTotalAmount(new BigDecimal("5000.00"));

        when(invoiceService.createInvoice(any(InvoiceDTO.class))).thenReturn(outputDTO);

        mockMvc.perform(post("/api/invoices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.invoiceNo").value("INV-001"))
                .andExpect(jsonPath("$.partyName").value("Test Client"));
    }

    @Test
    void getInvoice_shouldReturnInvoice() throws Exception {
        InvoiceDTO dto = new InvoiceDTO();
        dto.setId(1L);
        dto.setInvoiceNo("INV-001");
        dto.setPartyName("Test Client");

        when(invoiceService.getInvoice(1L)).thenReturn(dto);

        mockMvc.perform(get("/api/invoices/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.invoiceNo").value("INV-001"));
    }

    @Test
    void searchInvoices_shouldReturnMatchingInvoices() throws Exception {
        InvoiceDTO dto1 = new InvoiceDTO();
        dto1.setId(1L);
        dto1.setInvoiceNo("INV-001");

        InvoiceDTO dto2 = new InvoiceDTO();
        dto2.setId(2L);
        dto2.setInvoiceNo("INV-002");

        when(invoiceService.searchInvoices(eq("INV"), isNull())).thenReturn(Arrays.asList(dto1, dto2));

        mockMvc.perform(get("/api/invoices/search")
                        .param("invoiceNo", "INV"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void getInvoicePdf_shouldReturnPdf() throws Exception {
        Invoice invoice = new Invoice();
        invoice.setId(1L);
        invoice.setInvoiceNo("INV-001");

        byte[] pdfBytes = "PDF content".getBytes();

        when(invoiceService.getInvoiceEntity(1L)).thenReturn(invoice);
        when(pdfGenerationService.generateInvoicePdf(any(Invoice.class))).thenReturn(pdfBytes);

        mockMvc.perform(get("/api/invoices/1/pdf"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_PDF));
    }
}
