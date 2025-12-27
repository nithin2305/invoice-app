package com.invoice.app.controller;

import com.invoice.app.dto.InvoiceDTO;
import com.invoice.app.dto.MonthlyStatementDTO;
import com.invoice.app.entity.Invoice;
import com.invoice.app.service.InvoiceService;
import com.invoice.app.service.PdfGenerationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/invoices")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class InvoiceController {

    private final InvoiceService invoiceService;
    private final PdfGenerationService pdfGenerationService;

    @PostMapping
    public ResponseEntity<InvoiceDTO> createInvoice(@RequestBody InvoiceDTO invoiceDTO) {
        InvoiceDTO created = invoiceService.createInvoice(invoiceDTO);
        return ResponseEntity.ok(created);
    }

    @GetMapping("/{id}")
    public ResponseEntity<InvoiceDTO> getInvoice(@PathVariable Long id) {
        InvoiceDTO invoice = invoiceService.getInvoice(id);
        return ResponseEntity.ok(invoice);
    }

    @GetMapping("/search")
    public ResponseEntity<List<InvoiceDTO>> searchInvoices(
            @RequestParam(value = "invoiceNo", required = false) String invoiceNo,
            @RequestParam(value = "lrNo", required = false) String lrNo) {
        List<InvoiceDTO> invoices = invoiceService.searchInvoices(invoiceNo, lrNo);
        return ResponseEntity.ok(invoices);
    }

    @GetMapping
    public ResponseEntity<List<InvoiceDTO>> getAllInvoices() {
        List<InvoiceDTO> invoices = invoiceService.getAllInvoices();
        return ResponseEntity.ok(invoices);
    }

    @GetMapping("/next-number")
    public ResponseEntity<String> getNextInvoiceNumber() {
        String nextNumber = invoiceService.getNextInvoiceNumber();
        return ResponseEntity.ok(nextNumber);
    }

    @PutMapping("/{id}")
    public ResponseEntity<InvoiceDTO> updateInvoice(@PathVariable Long id, @RequestBody InvoiceDTO invoiceDTO) {
        InvoiceDTO updated = invoiceService.updateInvoice(id, invoiceDTO);
        return ResponseEntity.ok(updated);
    }

    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> getInvoicePdf(@PathVariable Long id) {
        Invoice invoice = invoiceService.getInvoiceEntity(id);
        byte[] pdfBytes = pdfGenerationService.generateInvoicePdf(invoice);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("inline", "invoice-" + invoice.getInvoiceNo() + ".pdf");
        headers.setContentLength(pdfBytes.length);

        return ResponseEntity.ok()
                .headers(headers)
                .body(pdfBytes);
    }
}
