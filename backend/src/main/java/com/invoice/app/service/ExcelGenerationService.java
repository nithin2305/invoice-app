package com.invoice.app.service;

import com.invoice.app.entity.Invoice;
import com.invoice.app.entity.InvoiceItem;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ExcelGenerationService {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    public byte[] generateInvoiceReport(List<Invoice> invoices) {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Invoice Report");

            // Create header style
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setFontHeightInPoints((short) 12);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setBorderBottom(BorderStyle.THIN);
            headerStyle.setBorderTop(BorderStyle.THIN);
            headerStyle.setBorderLeft(BorderStyle.THIN);
            headerStyle.setBorderRight(BorderStyle.THIN);

            // Create data style
            CellStyle dataStyle = workbook.createCellStyle();
            dataStyle.setBorderBottom(BorderStyle.THIN);
            dataStyle.setBorderTop(BorderStyle.THIN);
            dataStyle.setBorderLeft(BorderStyle.THIN);
            dataStyle.setBorderRight(BorderStyle.THIN);

            // Create currency style
            CellStyle currencyStyle = workbook.createCellStyle();
            currencyStyle.cloneStyleFrom(dataStyle);
            currencyStyle.setDataFormat(workbook.createDataFormat().getFormat("₹#,##0.00"));

            // Header row
            Row headerRow = sheet.createRow(0);
            String[] headers = {
                "Invoice Date", "LR Number", "Invoice Number", "Client Name", 
                "GST Number", "From", "To", "Description", "Amount"
            };
            
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Data rows
            int rowNum = 1;
            BigDecimal totalAmount = BigDecimal.ZERO;

            for (Invoice invoice : invoices) {
                if (invoice.getItems() != null && !invoice.getItems().isEmpty()) {
                    for (InvoiceItem item : invoice.getItems()) {
                        Row row = sheet.createRow(rowNum++);
                        
                        // Invoice Date
                        Cell cell0 = row.createCell(0);
                        cell0.setCellValue(invoice.getInvoiceDate() != null ? 
                            invoice.getInvoiceDate().format(DATE_FORMAT) : "");
                        cell0.setCellStyle(dataStyle);

                        // LR Number
                        Cell cell1 = row.createCell(1);
                        cell1.setCellValue(nullSafe(item.getLrNo()));
                        cell1.setCellStyle(dataStyle);

                        // Invoice Number
                        Cell cell2 = row.createCell(2);
                        cell2.setCellValue(nullSafe(invoice.getInvoiceNo()));
                        cell2.setCellStyle(dataStyle);

                        // Client Name
                        Cell cell3 = row.createCell(3);
                        cell3.setCellValue(nullSafe(invoice.getPartyName()));
                        cell3.setCellStyle(dataStyle);

                        // GST Number
                        Cell cell4 = row.createCell(4);
                        cell4.setCellValue(nullSafe(invoice.getPartyGst()));
                        cell4.setCellStyle(dataStyle);

                        // From
                        Cell cell5 = row.createCell(5);
                        cell5.setCellValue(nullSafe(item.getFromLocation()));
                        cell5.setCellStyle(dataStyle);

                        // To
                        Cell cell6 = row.createCell(6);
                        cell6.setCellValue(nullSafe(item.getToLocation()));
                        cell6.setCellStyle(dataStyle);

                        // Description
                        Cell cell7 = row.createCell(7);
                        cell7.setCellValue(nullSafe(item.getGoodsDescription()));
                        cell7.setCellStyle(dataStyle);

                        // Amount
                        Cell cell8 = row.createCell(8);
                        BigDecimal amount = item.getAmount() != null ? item.getAmount() : BigDecimal.ZERO;
                        cell8.setCellValue(amount.doubleValue());
                        cell8.setCellStyle(currencyStyle);

                        totalAmount = totalAmount.add(amount);
                    }
                }
            }

            // Total row
            Row totalRow = sheet.createRow(rowNum);
            Cell totalLabelCell = totalRow.createCell(7);
            totalLabelCell.setCellValue("TOTAL:");
            CellStyle totalLabelStyle = workbook.createCellStyle();
            totalLabelStyle.cloneStyleFrom(headerStyle);
            totalLabelStyle.setAlignment(HorizontalAlignment.RIGHT);
            totalLabelCell.setCellStyle(totalLabelStyle);

            Cell totalAmountCell = totalRow.createCell(8);
            totalAmountCell.setCellValue(totalAmount.doubleValue());
            CellStyle totalAmountStyle = workbook.createCellStyle();
            totalAmountStyle.cloneStyleFrom(headerStyle);
            totalAmountStyle.setDataFormat(workbook.createDataFormat().getFormat("₹#,##0.00"));
            totalAmountCell.setCellStyle(totalAmountStyle);

            // Auto-size columns
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(baos);
            return baos.toByteArray();

        } catch (IOException e) {
            throw new RuntimeException("Error generating Excel report", e);
        }
    }

    private String nullSafe(String value) {
        return value != null ? value : "";
    }
}
