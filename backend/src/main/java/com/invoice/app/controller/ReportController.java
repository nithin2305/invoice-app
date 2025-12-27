package com.invoice.app.controller;

import com.invoice.app.entity.Invoice;
import com.invoice.app.service.ExcelGenerationService;
import com.invoice.app.service.InvoiceService;
import com.invoice.app.service.PdfGenerationService;
import com.invoice.app.repository.InvoiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ReportController {

    private final InvoiceRepository invoiceRepository;
    private final PdfGenerationService pdfGenerationService;
    private final ExcelGenerationService excelGenerationService;

    @GetMapping("/invoices/pdf")
    public ResponseEntity<byte[]> getInvoiceReportPdf(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        List<Invoice> invoices = invoiceRepository.findByInvoiceDateBetween(startDate, endDate);
        
        // Generate a simple PDF report (we can enhance this later)
        byte[] pdfBytes = generateReportPdf(invoices, startDate, endDate);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        String filename = "invoice-report-" + startDate.format(DateTimeFormatter.ISO_DATE) + 
                         "-to-" + endDate.format(DateTimeFormatter.ISO_DATE) + ".pdf";
        headers.setContentDispositionFormData("attachment", filename);
        headers.setContentLength(pdfBytes.length);

        return ResponseEntity.ok()
                .headers(headers)
                .body(pdfBytes);
    }

    @GetMapping("/invoices/excel")
    public ResponseEntity<byte[]> getInvoiceReportExcel(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        List<Invoice> invoices = invoiceRepository.findByInvoiceDateBetween(startDate, endDate);
        byte[] excelBytes = excelGenerationService.generateInvoiceReport(invoices);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
        String filename = "invoice-report-" + startDate.format(DateTimeFormatter.ISO_DATE) + 
                         "-to-" + endDate.format(DateTimeFormatter.ISO_DATE) + ".xlsx";
        headers.setContentDispositionFormData("attachment", filename);
        headers.setContentLength(excelBytes.length);

        return ResponseEntity.ok()
                .headers(headers)
                .body(excelBytes);
    }

    private byte[] generateReportPdf(List<Invoice> invoices, LocalDate startDate, LocalDate endDate) {
        // For now, use the monthly statement generator with custom title
        // We can create a dedicated report generator if needed
        String periodTitle = startDate.format(DateTimeFormatter.ofPattern("dd-MMM-yyyy")) + 
                           " to " + endDate.format(DateTimeFormatter.ofPattern("dd-MMM-yyyy"));
        return pdfGenerationService.generateMonthlyStatementPdf(invoices, 
                                                                startDate.getYear(), 
                                                                startDate.getMonthValue(), 
                                                                periodTitle);
    }
}
