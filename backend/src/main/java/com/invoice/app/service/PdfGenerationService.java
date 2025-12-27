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

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    
    // Company Details
    private static final String COMPANY_NAME = "SHRIRAM LOGISTICS";
    private static final String COMPANY_ADDRESS = "No. 66/1, Mettu Street, Kaladipet Chennai- 600 019";
    private static final String COMPANY_CONTACT = "Contact No : 044 - 4213 3684";
    private static final String COMPANY_EMAIL = "E-Mail : shriramlogics@gmail.com";
    private static final String COMPANY_STATE = "State: TamilNadu";
    private static final String COMPANY_GSTIN = "GSTIN: 33AJBPM6638G1ZA";
    private static final String COMPANY_PAN = "PAN No: AJBPM6638G";
    
    // Bank Details
    private static final String BANK_NAME = "CANARA BANK";
    private static final String BANK_ACCOUNT = "60151400000726";
    private static final String BANK_BRANCH = "Mylapore Branch";
    private static final String BANK_IFSC = "CNRB0016015";

    public byte[] generateInvoicePdf(Invoice invoice) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4, 20, 20, 20, 20);
        
        try {
            PdfWriter.getInstance(document, baos);
            document.open();

            // Generate DUPLICATE COPY first
            generateInvoicePage(document, invoice, "DUPLICATE COPY");
            
            // Add new page for ORIGINAL COPY
            document.newPage();
            generateInvoicePage(document, invoice, "ORIGINAL COPY");

        } catch (DocumentException e) {
            throw new RuntimeException("Error generating PDF", e);
        } finally {
            document.close();
        }

        return baos.toByteArray();
    }
    
    private void generateInvoicePage(Document document, Invoice invoice, String copyType) throws DocumentException {
        // Fonts matching the original PDF
        Font companyFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, java.awt.Color.BLUE);
        Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9);
        Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 8);
        Font smallFont = FontFactory.getFont(FontFactory.HELVETICA, 7);
        Font tinyFont = FontFactory.getFont(FontFactory.HELVETICA, 6);
        Font copyTypeFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, java.awt.Color.RED);
        
        // Copy Type Header (top right)
        Paragraph copyHeader = new Paragraph(copyType, copyTypeFont);
        copyHeader.setAlignment(Element.ALIGN_RIGHT);
        document.add(copyHeader);
        
        // Company Header - centered
        Paragraph companyName = new Paragraph(COMPANY_NAME, companyFont);
        companyName.setAlignment(Element.ALIGN_CENTER);
        document.add(companyName);
        
        Paragraph contactInfo = new Paragraph(COMPANY_CONTACT + "     " + COMPANY_EMAIL, smallFont);
        contactInfo.setAlignment(Element.ALIGN_CENTER);
        document.add(contactInfo);
        
        Paragraph gstInfo = new Paragraph(COMPANY_STATE + "  " + COMPANY_GSTIN + "  " + COMPANY_PAN, smallFont);
        gstInfo.setAlignment(Element.ALIGN_CENTER);
        gstInfo.setSpacingAfter(8);
        document.add(gstInfo);
        
        // INVOICE Title - centered with box
        PdfPTable invoiceTitleTable = new PdfPTable(1);
        invoiceTitleTable.setWidthPercentage(20);
        PdfPCell invoiceTitleCell = new PdfPCell(new Phrase("INVOICE", headerFont));
        invoiceTitleCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        invoiceTitleCell.setPadding(3);
        invoiceTitleCell.setBorder(Rectangle.BOX);
        invoiceTitleTable.addCell(invoiceTitleCell);
        document.add(invoiceTitleTable);
        
        // Main layout table (Party on left, Invoice details on right)
        PdfPTable mainTable = new PdfPTable(2);
        mainTable.setWidthPercentage(100);
        mainTable.setWidths(new float[]{65f, 35f});
        mainTable.setSpacingBefore(5);
        
        // Party Details Cell (left) - with border
        StringBuilder partyInfo = new StringBuilder();
        partyInfo.append("M/S ").append(nullSafe(invoice.getPartyName())).append("\n");
        partyInfo.append(nullSafe(invoice.getPartyAddress())).append("\n");
        if (invoice.getPartyGst() != null && !invoice.getPartyGst().isEmpty()) {
            partyInfo.append("GSTIN: ").append(invoice.getPartyGst());
        }
        
        PdfPCell partyCell = new PdfPCell(new Phrase(partyInfo.toString(), normalFont));
        partyCell.setBorder(Rectangle.BOX);
        partyCell.setPadding(5);
        mainTable.addCell(partyCell);
        
        // Invoice Details Cell (right) - with border
        StringBuilder invoiceDetails = new StringBuilder();
        invoiceDetails.append(COMPANY_ADDRESS).append("\n");
        invoiceDetails.append("INVOICE  NO : ").append(nullSafe(invoice.getInvoiceNo())).append("\n");
        String dateStr = invoice.getInvoiceDate() != null ? invoice.getInvoiceDate().format(DATE_FORMAT) : "";
        invoiceDetails.append("DATE : ").append(dateStr);
        
        PdfPCell invoiceCell = new PdfPCell(new Phrase(invoiceDetails.toString(), normalFont));
        invoiceCell.setBorder(Rectangle.BOX);
        invoiceCell.setPadding(5);
        mainTable.addCell(invoiceCell);
        
        document.add(mainTable);
        
        // Main content table with header and items
        PdfPTable contentTable = new PdfPTable(2);
        contentTable.setWidthPercentage(100);
        contentTable.setWidths(new float[]{85f, 15f});
        contentTable.setSpacingBefore(3);
        
        // Left side: "DESCRIPTION OF GOODS/SERVICES" header
        PdfPCell descHeaderCell = new PdfPCell(new Phrase("DESCRIPTION OF GOODS/SERVICES", headerFont));
        descHeaderCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        descHeaderCell.setPadding(3);
        descHeaderCell.setBorder(Rectangle.BOX);
        contentTable.addCell(descHeaderCell);
        
        // Right side: "Amount" header
        PdfPCell amountHeaderCell = new PdfPCell(new Phrase("Amount", headerFont));
        amountHeaderCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        amountHeaderCell.setPadding(3);
        amountHeaderCell.setBorder(Rectangle.BOX);
        contentTable.addCell(amountHeaderCell);
        
        document.add(contentTable);
        
        // Items section
        BigDecimal itemsTotal = BigDecimal.ZERO;
        List<InvoiceItem> items = invoice.getItems();
        int sno = 1;
        
        if (items != null && !items.isEmpty()) {
            for (InvoiceItem item : items) {
                PdfPTable itemTable = new PdfPTable(2);
                itemTable.setWidthPercentage(100);
                itemTable.setWidths(new float[]{85f, 15f});
                
                // Left cell - Transportation details
                PdfPTable leftContent = new PdfPTable(1);
                leftContent.setWidthPercentage(100);
                
                // Row 1: Serial number and "Transportation Charges"
                PdfPCell transChargesCell = new PdfPCell(new Phrase(sno + " Transportation Charges", normalFont));
                transChargesCell.setBorder(Rectangle.NO_BORDER);
                transChargesCell.setPadding(2);
                leftContent.addCell(transChargesCell);
                
                // Row 2: LR details table
                PdfPTable lrTable = new PdfPTable(6);
                lrTable.setWidthPercentage(100);
                lrTable.setWidths(new float[]{1f, 1f, 1f, 1.2f, 2f, 1.5f});
                
                // LR Headers
                String[] lrHeaders = {"L.R. No", "Date", "FROM", "To", "Description of Goods", "Pkgs"};
                for (String h : lrHeaders) {
                    PdfPCell hCell = new PdfPCell(new Phrase(h, tinyFont));
                    hCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                    hCell.setPadding(2);
                    hCell.setBorder(Rectangle.BOX);
                    hCell.setBackgroundColor(new java.awt.Color(240, 240, 240));
                    lrTable.addCell(hCell);
                }
                
                // LR Data
                addCell(lrTable, nullSafe(item.getLrNo()), tinyFont, Element.ALIGN_CENTER);
                String lrDateStr = item.getLrDate() != null ? item.getLrDate().format(DATE_FORMAT) : "";
                addCell(lrTable, lrDateStr, tinyFont, Element.ALIGN_CENTER);
                addCell(lrTable, nullSafe(item.getFromLocation()), tinyFont, Element.ALIGN_CENTER);
                addCell(lrTable, nullSafe(item.getToLocation()), tinyFont, Element.ALIGN_CENTER);
                addCell(lrTable, nullSafe(item.getGoodsDescription()), tinyFont, Element.ALIGN_CENTER);
                
                String pkg = nullSafe(item.getPackageType());
                if (pkg.isEmpty()) pkg = "AS PER INVOICE";
                addCell(lrTable, pkg, tinyFont, Element.ALIGN_CENTER);
                
                PdfPCell lrTableCell = new PdfPCell(lrTable);
                lrTableCell.setBorder(Rectangle.NO_BORDER);
                lrTableCell.setPadding(2);
                leftContent.addCell(lrTableCell);
                
                // Vehicle Type row
                String vehicleType = nullSafe(item.getVehicleType());
                if (!vehicleType.isEmpty()) {
                    PdfPCell vehicleCell = new PdfPCell(new Phrase("VehicleType:" + vehicleType, tinyFont));
                    vehicleCell.setBorder(Rectangle.NO_BORDER);
                    vehicleCell.setPadding(2);
                    vehicleCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                    leftContent.addCell(vehicleCell);
                }
                
                PdfPCell leftCell = new PdfPCell(leftContent);
                leftCell.setBorder(Rectangle.BOX);
                leftCell.setPadding(3);
                itemTable.addCell(leftCell);
                
                // Right cell - Amount
                BigDecimal amt = item.getAmount() != null ? item.getAmount() : BigDecimal.ZERO;
                itemsTotal = itemsTotal.add(amt);
                PdfPCell amtCell = new PdfPCell(new Phrase(formatAmountNoDecimals(amt), normalFont));
                amtCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                amtCell.setVerticalAlignment(Element.ALIGN_TOP);
                amtCell.setPadding(5);
                amtCell.setBorder(Rectangle.BOX);
                itemTable.addCell(amtCell);
                
                document.add(itemTable);
                sno++;
            }
        }
        
        // GST Note
        PdfPTable gstNoteTable = new PdfPTable(2);
        gstNoteTable.setWidthPercentage(100);
        gstNoteTable.setWidths(new float[]{85f, 15f});
        
        PdfPCell gstNoteCell = new PdfPCell(new Phrase("GST TO BE PAID BY THE SERVICE RECEIPIENT", smallFont));
        gstNoteCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        gstNoteCell.setPadding(3);
        gstNoteCell.setBorder(Rectangle.BOX);
        gstNoteTable.addCell(gstNoteCell);
        
        PdfPCell emptyCell = new PdfPCell(new Phrase("", smallFont));
        emptyCell.setBorder(Rectangle.BOX);
        gstNoteTable.addCell(emptyCell);
        
        document.add(gstNoteTable);
        
        // Total row
        BigDecimal grandTotal = invoice.getTotalAmount() != null ? invoice.getTotalAmount() : itemsTotal;
        
        PdfPTable totalTable = new PdfPTable(2);
        totalTable.setWidthPercentage(100);
        totalTable.setWidths(new float[]{85f, 15f});
        
        PdfPCell totalLabelCell = new PdfPCell(new Phrase("TOTAL", headerFont));
        totalLabelCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        totalLabelCell.setPadding(3);
        totalLabelCell.setBorder(Rectangle.BOX);
        totalTable.addCell(totalLabelCell);
        
        PdfPCell totalAmountCell = new PdfPCell(new Phrase(formatAmountNoDecimals(grandTotal), headerFont));
        totalAmountCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        totalAmountCell.setPadding(3);
        totalAmountCell.setBorder(Rectangle.BOX);
        totalTable.addCell(totalAmountCell);
        
        document.add(totalTable);
        
        // Amount in Words
        String amountInWords = invoice.getAmountInWords() != null ? invoice.getAmountInWords() : "";
        Paragraph amtWords = new Paragraph("Amount Chargeable in Words :\n" + amountInWords.toUpperCase(), normalFont);
        amtWords.setSpacingBefore(5);
        document.add(amtWords);
        
        // Bottom section: Bank Details (left) and Signature (right)
        PdfPTable bottomTable = new PdfPTable(2);
        bottomTable.setWidthPercentage(100);
        bottomTable.setSpacingBefore(8);
        bottomTable.setWidths(new float[]{50f, 50f});
        
        // Bank Details (left)
        StringBuilder bankInfo = new StringBuilder();
        bankInfo.append("     Company Bank Details\n");
        bankInfo.append("GST TO BE PAID BY CONSIGNOR/\n");
        bankInfo.append("CONSIGNEE/GTA/OTHERS\n\n");
        bankInfo.append("Bank Name :   ").append(BANK_NAME).append("\n");
        bankInfo.append("A/C. No :   ").append(BANK_ACCOUNT).append("\n");
        bankInfo.append("Branch :   ").append(BANK_BRANCH).append("\n");
        bankInfo.append("IFSC Code :   ").append(BANK_IFSC);
        
        PdfPCell bankCell = new PdfPCell(new Phrase(bankInfo.toString(), smallFont));
        bankCell.setBorder(Rectangle.BOX);
        bankCell.setPadding(5);
        bottomTable.addCell(bankCell);
        
        // Signature (right)
        StringBuilder signInfo = new StringBuilder();
        signInfo.append("         FOR ").append(COMPANY_NAME).append("\n\n\n\n");
        signInfo.append("    Authorised signature");
        
        PdfPCell signCell = new PdfPCell(new Phrase(signInfo.toString(), normalFont));
        signCell.setBorder(Rectangle.BOX);
        signCell.setPadding(5);
        signCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        bottomTable.addCell(signCell);
        
        document.add(bottomTable);
        
        // Declaration
        Paragraph declaration = new Paragraph("Declaration          \nThe goods were dispatched as per above details. \nKindly payment & oblige.", smallFont);
        declaration.setSpacingBefore(5);
        document.add(declaration);
        
        // Jurisdiction
        Paragraph jurisdiction = new Paragraph("E. & O.E SUBJECT TO CHENNAI JURISDICTION", smallFont);
        jurisdiction.setAlignment(Element.ALIGN_CENTER);
        jurisdiction.setSpacingBefore(5);
        document.add(jurisdiction);
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
    
    private String formatAmount(BigDecimal amount) {
        if (amount == null) return "0.00";
        return String.format("%,.2f", amount);
    }
    
    private String formatAmountNoDecimals(BigDecimal amount) {
        if (amount == null) return "0";
        return String.format("%,.0f", amount);
    }
}
