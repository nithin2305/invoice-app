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

    /**
     * Generate Excel file for a single invoice
     */
    public byte[] generateSingleInvoiceExcel(Invoice invoice) {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Invoice " + nullSafe(invoice.getInvoiceNo()));

            // Create styles
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

            CellStyle titleStyle = workbook.createCellStyle();
            Font titleFont = workbook.createFont();
            titleFont.setBold(true);
            titleFont.setFontHeightInPoints((short) 14);
            titleStyle.setFont(titleFont);

            CellStyle labelStyle = workbook.createCellStyle();
            Font labelFont = workbook.createFont();
            labelFont.setBold(true);
            labelStyle.setFont(labelFont);

            CellStyle dataStyle = workbook.createCellStyle();
            dataStyle.setBorderBottom(BorderStyle.THIN);
            dataStyle.setBorderTop(BorderStyle.THIN);
            dataStyle.setBorderLeft(BorderStyle.THIN);
            dataStyle.setBorderRight(BorderStyle.THIN);

            CellStyle currencyStyle = workbook.createCellStyle();
            currencyStyle.cloneStyleFrom(dataStyle);
            currencyStyle.setDataFormat(workbook.createDataFormat().getFormat("₹#,##0.00"));

            int rowNum = 0;

            // Company Header
            Row companyRow = sheet.createRow(rowNum++);
            Cell companyCell = companyRow.createCell(0);
            companyCell.setCellValue("SHRIRAM LOGISTICS");
            companyCell.setCellStyle(titleStyle);

            Row addressRow = sheet.createRow(rowNum++);
            addressRow.createCell(0).setCellValue("No. 66/1, Mettu Street, Kaladipet Chennai- 600 019");

            rowNum++; // Empty row

            // Invoice Details
            Row invoiceNoRow = sheet.createRow(rowNum++);
            Cell invNoLabel = invoiceNoRow.createCell(0);
            invNoLabel.setCellValue("Invoice No:");
            invNoLabel.setCellStyle(labelStyle);
            invoiceNoRow.createCell(1).setCellValue(nullSafe(invoice.getInvoiceNo()));

            Row invoiceDateRow = sheet.createRow(rowNum++);
            Cell invDateLabel = invoiceDateRow.createCell(0);
            invDateLabel.setCellValue("Invoice Date:");
            invDateLabel.setCellStyle(labelStyle);
            invoiceDateRow.createCell(1).setCellValue(invoice.getInvoiceDate() != null ? 
                invoice.getInvoiceDate().format(DATE_FORMAT) : "");

            rowNum++; // Empty row

            // Party Details
            Row partyHeader = sheet.createRow(rowNum++);
            Cell partyHeaderCell = partyHeader.createCell(0);
            partyHeaderCell.setCellValue("Party Details");
            partyHeaderCell.setCellStyle(titleStyle);

            Row partyNameRow = sheet.createRow(rowNum++);
            Cell partyNameLabel = partyNameRow.createCell(0);
            partyNameLabel.setCellValue("Party Name:");
            partyNameLabel.setCellStyle(labelStyle);
            partyNameRow.createCell(1).setCellValue(nullSafe(invoice.getPartyName()));

            Row partyAddressRow = sheet.createRow(rowNum++);
            Cell partyAddrLabel = partyAddressRow.createCell(0);
            partyAddrLabel.setCellValue("Address:");
            partyAddrLabel.setCellStyle(labelStyle);
            partyAddressRow.createCell(1).setCellValue(nullSafe(invoice.getPartyAddress()));

            Row partyGstRow = sheet.createRow(rowNum++);
            Cell partyGstLabel = partyGstRow.createCell(0);
            partyGstLabel.setCellValue("GSTIN:");
            partyGstLabel.setCellStyle(labelStyle);
            partyGstRow.createCell(1).setCellValue(nullSafe(invoice.getPartyGst()));

            rowNum++; // Empty row

            // Items Header
            Row itemsHeader = sheet.createRow(rowNum++);
            Cell itemsHeaderCell = itemsHeader.createCell(0);
            itemsHeaderCell.setCellValue("Line Items");
            itemsHeaderCell.setCellStyle(titleStyle);

            // Items Table Header
            Row itemHeaderRow = sheet.createRow(rowNum++);
            String[] itemHeaders = {"#", "LR No", "LR Date", "From", "To", "Description", "Pkg Type", "Vehicle No", "Vehicle Type", "Amount"};
            for (int i = 0; i < itemHeaders.length; i++) {
                Cell cell = itemHeaderRow.createCell(i);
                cell.setCellValue(itemHeaders[i]);
                cell.setCellStyle(headerStyle);
            }

            // Items Data
            BigDecimal itemsTotal = BigDecimal.ZERO;
            if (invoice.getItems() != null) {
                int itemNum = 1;
                for (InvoiceItem item : invoice.getItems()) {
                    Row row = sheet.createRow(rowNum++);

                    Cell cell0 = row.createCell(0);
                    cell0.setCellValue(itemNum++);
                    cell0.setCellStyle(dataStyle);

                    Cell cell1 = row.createCell(1);
                    cell1.setCellValue(nullSafe(item.getLrNo()));
                    cell1.setCellStyle(dataStyle);

                    Cell cell2 = row.createCell(2);
                    cell2.setCellValue(item.getLrDate() != null ? item.getLrDate().format(DATE_FORMAT) : "");
                    cell2.setCellStyle(dataStyle);

                    Cell cell3 = row.createCell(3);
                    cell3.setCellValue(nullSafe(item.getFromLocation()));
                    cell3.setCellStyle(dataStyle);

                    Cell cell4 = row.createCell(4);
                    cell4.setCellValue(nullSafe(item.getToLocation()));
                    cell4.setCellStyle(dataStyle);

                    Cell cell5 = row.createCell(5);
                    cell5.setCellValue(nullSafe(item.getGoodsDescription()));
                    cell5.setCellStyle(dataStyle);

                    Cell cell6 = row.createCell(6);
                    cell6.setCellValue(nullSafe(item.getPackageType()));
                    cell6.setCellStyle(dataStyle);

                    Cell cell7 = row.createCell(7);
                    cell7.setCellValue(nullSafe(item.getVehicleNumber()));
                    cell7.setCellStyle(dataStyle);

                    Cell cell8 = row.createCell(8);
                    cell8.setCellValue(nullSafe(item.getVehicleType()));
                    cell8.setCellStyle(dataStyle);

                    Cell cell9 = row.createCell(9);
                    BigDecimal amount = item.getAmount() != null ? item.getAmount() : BigDecimal.ZERO;
                    cell9.setCellValue(amount.doubleValue());
                    cell9.setCellStyle(currencyStyle);

                    itemsTotal = itemsTotal.add(amount);
                }
            }

            rowNum++; // Empty row

            // Charges Section
            Row chargesHeader = sheet.createRow(rowNum++);
            Cell chargesHeaderCell = chargesHeader.createCell(0);
            chargesHeaderCell.setCellValue("Additional Charges");
            chargesHeaderCell.setCellStyle(titleStyle);

            Row haltingRow = sheet.createRow(rowNum++);
            Cell haltingLabel = haltingRow.createCell(0);
            haltingLabel.setCellValue("Halting Charges:");
            haltingLabel.setCellStyle(labelStyle);
            Cell haltingValue = haltingRow.createCell(1);
            haltingValue.setCellValue(invoice.getHaltingCharges() != null ? invoice.getHaltingCharges().doubleValue() : 0);
            haltingValue.setCellStyle(currencyStyle);

            Row loadingRow = sheet.createRow(rowNum++);
            Cell loadingLabel = loadingRow.createCell(0);
            loadingLabel.setCellValue("Loading Charges:");
            loadingLabel.setCellStyle(labelStyle);
            Cell loadingValue = loadingRow.createCell(1);
            loadingValue.setCellValue(invoice.getLoadingCharges() != null ? invoice.getLoadingCharges().doubleValue() : 0);
            loadingValue.setCellStyle(currencyStyle);

            Row unloadingRow = sheet.createRow(rowNum++);
            Cell unloadingLabel = unloadingRow.createCell(0);
            unloadingLabel.setCellValue("Unloading Charges:");
            unloadingLabel.setCellStyle(labelStyle);
            Cell unloadingValue = unloadingRow.createCell(1);
            unloadingValue.setCellValue(invoice.getUnloadingCharges() != null ? invoice.getUnloadingCharges().doubleValue() : 0);
            unloadingValue.setCellStyle(currencyStyle);

            rowNum++; // Empty row

            // Total
            Row totalRow = sheet.createRow(rowNum++);
            Cell totalLabel = totalRow.createCell(0);
            totalLabel.setCellValue("TOTAL AMOUNT:");
            totalLabel.setCellStyle(titleStyle);
            Cell totalValue = totalRow.createCell(1);
            BigDecimal grandTotal = invoice.getTotalAmount() != null ? invoice.getTotalAmount() : itemsTotal;
            totalValue.setCellValue(grandTotal.doubleValue());
            
            CellStyle totalCurrencyStyle = workbook.createCellStyle();
            totalCurrencyStyle.cloneStyleFrom(currencyStyle);
            Font totalFont = workbook.createFont();
            totalFont.setBold(true);
            totalFont.setFontHeightInPoints((short) 12);
            totalCurrencyStyle.setFont(totalFont);
            totalValue.setCellStyle(totalCurrencyStyle);

            // Amount in Words
            if (invoice.getAmountInWords() != null && !invoice.getAmountInWords().isEmpty()) {
                Row amtWordsRow = sheet.createRow(rowNum++);
                Cell amtWordsLabel = amtWordsRow.createCell(0);
                amtWordsLabel.setCellValue("Amount in Words:");
                amtWordsLabel.setCellStyle(labelStyle);
                amtWordsRow.createCell(1).setCellValue(invoice.getAmountInWords());
            }

            // Auto-size columns
            for (int i = 0; i < itemHeaders.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(baos);
            return baos.toByteArray();

        } catch (IOException e) {
            throw new RuntimeException("Error generating invoice Excel", e);
        }
    }
}
