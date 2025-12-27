package com.invoice.app.service;

import com.invoice.app.entity.Invoice;
import com.invoice.app.entity.InvoiceItem;
import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PdfGenerationService {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    public byte[] generateInvoicePdf(Invoice invoice) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        Document document = new Document(PageSize.A4, 36, 36, 36, 36);
        
        try {
            PdfWriter.getInstance(document, baos);
            document.open();

            // Fonts
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
            Font companyNameFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
            Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11);
            Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 10);
            Font smallFont = FontFactory.getFont(FontFactory.HELVETICA, 9);

            // Company Header
            Paragraph companyName = new Paragraph("SHRIRAM LOGISTICS", companyNameFont);
            companyName.setAlignment(Element.ALIGN_CENTER);
            document.add(companyName);

            Paragraph companyAddress = new Paragraph("No. 66/1, Mettu Street, Kaladipet, Chennai - 600 019", normalFont);
            companyAddress.setAlignment(Element.ALIGN_CENTER);
            document.add(companyAddress);

            Paragraph companyContact = new Paragraph("Contact: 044 - 4213 3684  |  E-Mail: shriramlogics@gmail.com", smallFont);
            companyContact.setAlignment(Element.ALIGN_CENTER);
            document.add(companyContact);

            Paragraph companyState = new Paragraph("State: Tamil Nadu", smallFont);
            companyState.setAlignment(Element.ALIGN_CENTER);
            document.add(companyState);

            Paragraph companyGst = new Paragraph("GSTIN: 33AJBPM6638G1ZA  |  PAN No: AJBPM6638G", smallFont);
            companyGst.setAlignment(Element.ALIGN_CENTER);
            companyGst.setSpacingAfter(15);
            document.add(companyGst);

            // Horizontal line separator
            PdfPTable separatorTable = new PdfPTable(1);
            separatorTable.setWidthPercentage(100);
            PdfPCell separatorCell = new PdfPCell();
            separatorCell.setBorder(Rectangle.BOTTOM);
            separatorCell.setBorderWidth(1f);
            separatorCell.setFixedHeight(2f);
            separatorTable.addCell(separatorCell);
            separatorTable.setSpacingAfter(10);
            document.add(separatorTable);

            // Title
            Paragraph title = new Paragraph("TRANSPORT INVOICE", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(20);
            document.add(title);

            // Invoice details table
            PdfPTable headerTable = new PdfPTable(2);
            headerTable.setWidthPercentage(100);
            headerTable.setSpacingAfter(15);

            addCell(headerTable, "Invoice No: " + nullSafe(invoice.getInvoiceNo()), headerFont, Element.ALIGN_LEFT);
            String dateStr = invoice.getInvoiceDate() != null ? invoice.getInvoiceDate().format(DATE_FORMAT) : "";
            addCell(headerTable, "Date: " + dateStr, headerFont, Element.ALIGN_RIGHT);

            document.add(headerTable);

            // Party details
            PdfPTable partyTable = new PdfPTable(1);
            partyTable.setWidthPercentage(100);
            partyTable.setSpacingAfter(15);

            PdfPCell partyHeaderCell = new PdfPCell(new Phrase("Bill To:", headerFont));
            partyHeaderCell.setBorder(Rectangle.NO_BORDER);
            partyHeaderCell.setPaddingBottom(5);
            partyTable.addCell(partyHeaderCell);

            StringBuilder partyInfo = new StringBuilder();
            partyInfo.append(nullSafe(invoice.getPartyName())).append("\n");
            partyInfo.append(nullSafe(invoice.getPartyAddress())).append("\n");
            if (invoice.getPartyGst() != null && !invoice.getPartyGst().isEmpty()) {
                partyInfo.append("GST: ").append(invoice.getPartyGst());
            }

            PdfPCell partyCell = new PdfPCell(new Phrase(partyInfo.toString(), normalFont));
            partyCell.setBorder(Rectangle.BOX);
            partyCell.setPadding(10);
            partyTable.addCell(partyCell);

            document.add(partyTable);

            // Items table
            PdfPTable itemsTable = new PdfPTable(new float[]{1.2f, 1f, 1.5f, 2f, 1f, 1.2f, 1.2f});
            itemsTable.setWidthPercentage(100);
            itemsTable.setSpacingAfter(15);

            // Headers
            String[] headers = {"LR No / Date", "Route", "Description", "Package", "Vehicle", "Type", "Amount"};
            for (String header : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(header, headerFont));
                cell.setBackgroundColor(new java.awt.Color(230, 230, 230));
                cell.setPadding(5);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                itemsTable.addCell(cell);
            }

            // Data rows
            BigDecimal itemsTotal = BigDecimal.ZERO;
            List<InvoiceItem> items = invoice.getItems();
            if (items != null && !items.isEmpty()) {
                for (InvoiceItem item : items) {
                    String lrInfo = nullSafe(item.getLrNo());
                    if (item.getLrDate() != null) {
                        lrInfo += "\n" + item.getLrDate().format(DATE_FORMAT);
                    }
                    addCell(itemsTable, lrInfo, smallFont, Element.ALIGN_CENTER);

                    String route = nullSafe(item.getFromLocation()) + " → " + nullSafe(item.getToLocation());
                    addCell(itemsTable, route, smallFont, Element.ALIGN_CENTER);

                    addCell(itemsTable, nullSafe(item.getGoodsDescription()), smallFont, Element.ALIGN_LEFT);

                    String pkg = nullSafe(item.getPackageType());
                    if (item.getPackageCount() != null) {
                        pkg += " x " + item.getPackageCount();
                    }
                    addCell(itemsTable, pkg, smallFont, Element.ALIGN_CENTER);

                    addCell(itemsTable, nullSafe(item.getVehicleNumber()), smallFont, Element.ALIGN_CENTER);
                    addCell(itemsTable, nullSafe(item.getVehicleType()), smallFont, Element.ALIGN_CENTER);

                    BigDecimal amt = item.getAmount() != null ? item.getAmount() : BigDecimal.ZERO;
                    itemsTotal = itemsTotal.add(amt);
                    addCell(itemsTable, formatCurrency(amt), smallFont, Element.ALIGN_RIGHT);
                }
            }

            document.add(itemsTable);

            // Summary table
            PdfPTable summaryTable = new PdfPTable(2);
            summaryTable.setWidthPercentage(50);
            summaryTable.setHorizontalAlignment(Element.ALIGN_RIGHT);
            summaryTable.setSpacingAfter(10);

            addSummaryRow(summaryTable, "Items Total:", formatCurrency(itemsTotal), normalFont);
            
            BigDecimal halting = invoice.getHaltingCharges() != null ? invoice.getHaltingCharges() : BigDecimal.ZERO;
            BigDecimal loading = invoice.getLoadingCharges() != null ? invoice.getLoadingCharges() : BigDecimal.ZERO;
            BigDecimal unloading = invoice.getUnloadingCharges() != null ? invoice.getUnloadingCharges() : BigDecimal.ZERO;

            if (halting.compareTo(BigDecimal.ZERO) > 0) {
                addSummaryRow(summaryTable, "Halting Charges:", formatCurrency(halting), normalFont);
            }
            if (loading.compareTo(BigDecimal.ZERO) > 0) {
                addSummaryRow(summaryTable, "Loading Charges:", formatCurrency(loading), normalFont);
            }
            if (unloading.compareTo(BigDecimal.ZERO) > 0) {
                addSummaryRow(summaryTable, "Unloading Charges:", formatCurrency(unloading), normalFont);
            }

            BigDecimal grandTotal = invoice.getTotalAmount() != null ? invoice.getTotalAmount() : BigDecimal.ZERO;
            addSummaryRow(summaryTable, "Grand Total:", formatCurrency(grandTotal), headerFont);

            document.add(summaryTable);

            // Amount in words
            if (invoice.getAmountInWords() != null && !invoice.getAmountInWords().isEmpty()) {
                Paragraph amtWords = new Paragraph("Amount in Words: " + invoice.getAmountInWords(), normalFont);
                amtWords.setSpacingBefore(10);
                document.add(amtWords);
            }

            // Remarks
            if (invoice.getRemarks() != null && !invoice.getRemarks().isEmpty()) {
                Paragraph remarks = new Paragraph("Remarks: " + invoice.getRemarks(), smallFont);
                remarks.setSpacingBefore(15);
                document.add(remarks);
            }

            // Footer
            Paragraph footer = new Paragraph("\n\nAuthorized Signatory", normalFont);
            footer.setAlignment(Element.ALIGN_RIGHT);
            footer.setSpacingBefore(40);
            document.add(footer);

        } catch (DocumentException e) {
            throw new RuntimeException("Error generating PDF", e);
        } finally {
            document.close();
        }

        return baos.toByteArray();
    }

    public byte[] generateMonthlyStatementPdf(List<Invoice> invoices, int year, int month, String monthName) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4.rotate(), 36, 36, 36, 36);

        try {
            PdfWriter.getInstance(document, baos);
            document.open();

            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
            Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);
            Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 9);

            // Title
            Paragraph title = new Paragraph("MONTHLY STATEMENT - " + monthName.toUpperCase() + " " + year, titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(20);
            document.add(title);

            // Summary
            BigDecimal totalAmount = invoices.stream()
                    .map(Invoice::getTotalAmount)
                    .filter(amt -> amt != null)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            Paragraph summary = new Paragraph(
                    "Total Invoices: " + invoices.size() + "    |    Total Amount: " + formatCurrency(totalAmount),
                    headerFont
            );
            summary.setAlignment(Element.ALIGN_CENTER);
            summary.setSpacingAfter(20);
            document.add(summary);

            // Invoices table
            PdfPTable table = new PdfPTable(new float[]{0.5f, 1.5f, 1f, 2f, 2f, 1.5f, 1.2f});
            table.setWidthPercentage(100);

            String[] headers = {"#", "Invoice No", "Date", "Party Name", "Address", "GST", "Amount"};
            for (String header : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(header, headerFont));
                cell.setBackgroundColor(new java.awt.Color(230, 230, 230));
                cell.setPadding(5);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(cell);
            }

            int rowNum = 1;
            for (Invoice inv : invoices) {
                addCell(table, String.valueOf(rowNum++), normalFont, Element.ALIGN_CENTER);
                addCell(table, nullSafe(inv.getInvoiceNo()), normalFont, Element.ALIGN_LEFT);
                String dateStr = inv.getInvoiceDate() != null ? inv.getInvoiceDate().format(DATE_FORMAT) : "";
                addCell(table, dateStr, normalFont, Element.ALIGN_CENTER);
                addCell(table, nullSafe(inv.getPartyName()), normalFont, Element.ALIGN_LEFT);
                addCell(table, nullSafe(inv.getPartyAddress()), normalFont, Element.ALIGN_LEFT);
                addCell(table, nullSafe(inv.getPartyGst()), normalFont, Element.ALIGN_CENTER);
                BigDecimal amt = inv.getTotalAmount() != null ? inv.getTotalAmount() : BigDecimal.ZERO;
                addCell(table, formatCurrency(amt), normalFont, Element.ALIGN_RIGHT);
            }

            // Total row
            PdfPCell totalLabelCell = new PdfPCell(new Phrase("TOTAL", headerFont));
            totalLabelCell.setColspan(6);
            totalLabelCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            totalLabelCell.setPadding(5);
            totalLabelCell.setBackgroundColor(new java.awt.Color(240, 240, 240));
            table.addCell(totalLabelCell);

            PdfPCell totalAmountCell = new PdfPCell(new Phrase(formatCurrency(totalAmount), headerFont));
            totalAmountCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            totalAmountCell.setPadding(5);
            totalAmountCell.setBackgroundColor(new java.awt.Color(240, 240, 240));
            table.addCell(totalAmountCell);

            document.add(table);

        } catch (DocumentException e) {
            throw new RuntimeException("Error generating monthly statement PDF", e);
        } finally {
            document.close();
        }

        return baos.toByteArray();
    }

    private void addCell(PdfPTable table, String text, Font font, int alignment) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setPadding(4);
        cell.setHorizontalAlignment(alignment);
        table.addCell(cell);
    }

    private void addSummaryRow(PdfPTable table, String label, String value, Font font) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label, font));
        labelCell.setBorder(Rectangle.NO_BORDER);
        labelCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        labelCell.setPadding(3);
        table.addCell(labelCell);

        PdfPCell valueCell = new PdfPCell(new Phrase(value, font));
        valueCell.setBorder(Rectangle.NO_BORDER);
        valueCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        valueCell.setPadding(3);
        table.addCell(valueCell);
    }

    private String nullSafe(String value) {
        return value != null ? value : "";
    }

    private String formatCurrency(BigDecimal amount) {
        if (amount == null) return "₹0.00";
        return "₹" + String.format("%,.2f", amount);
    }
}
