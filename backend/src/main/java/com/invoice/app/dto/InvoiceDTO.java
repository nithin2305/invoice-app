package com.invoice.app.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceDTO {
    private Long id;
    private String invoiceNo;
    private String invoiceDate;
    private Long partyId;
    private String partyName;
    private String partyAddress;
    private String partyGst;
    private BigDecimal haltingCharges;
    private BigDecimal loadingCharges;
    private BigDecimal unloadingCharges;
    private BigDecimal totalAmount;
    private String amountInWords;
    private String remarks;
    private List<InvoiceItemDTO> items;
}
