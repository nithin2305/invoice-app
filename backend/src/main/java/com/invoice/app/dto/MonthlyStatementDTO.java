package com.invoice.app.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MonthlyStatementDTO {
    private int year;
    private int month;
    private String monthName;
    private int totalInvoices;
    private BigDecimal totalAmount;
    private List<InvoiceDTO> invoices;
}
