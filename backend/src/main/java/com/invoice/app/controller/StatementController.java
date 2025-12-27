package com.invoice.app.controller;

import com.invoice.app.dto.MonthlyStatementDTO;
import com.invoice.app.entity.Invoice;
import com.invoice.app.repository.InvoiceRepository;
import com.invoice.app.service.InvoiceService;
import com.invoice.app.service.PdfGenerationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;

@RestController
@RequestMapping("/api/statements")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class StatementController {

    private final InvoiceService invoiceService;
    private final InvoiceRepository invoiceRepository;
    private final PdfGenerationService pdfGenerationService;

    @GetMapping("/monthly")
    public ResponseEntity<MonthlyStatementDTO> getMonthlyStatement(
            @RequestParam(value = "year", required = false) Integer year,
            @RequestParam(value = "month", required = false) Integer month) {
        
        // Default to current year/month if not provided
        LocalDate now = LocalDate.now();
        int y = year != null ? year : now.getYear();
        int m = month != null ? month : now.getMonthValue();

        MonthlyStatementDTO statement = invoiceService.getMonthlyStatement(y, m);
        return ResponseEntity.ok(statement);
    }

    @GetMapping("/monthly/pdf")
    public ResponseEntity<byte[]> getMonthlyStatementPdf(
            @RequestParam(value = "year", required = false) Integer year,
            @RequestParam(value = "month", required = false) Integer month) {
        
        LocalDate now = LocalDate.now();
        int y = year != null ? year : now.getYear();
        int m = month != null ? month : now.getMonthValue();

        String monthName = Month.of(m).getDisplayName(TextStyle.FULL, Locale.ENGLISH);
        List<Invoice> invoices = invoiceRepository.findByYearAndMonth(y, m);
        
        byte[] pdfBytes = pdfGenerationService.generateMonthlyStatementPdf(invoices, y, m, monthName);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("inline", "statement-" + monthName + "-" + y + ".pdf");
        headers.setContentLength(pdfBytes.length);

        return ResponseEntity.ok()
                .headers(headers)
                .body(pdfBytes);
    }
}
