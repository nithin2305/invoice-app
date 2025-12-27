package com.invoice.app.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceItemDTO {
    private Long id;
    private String lrNo;
    private String lrDate;
    private String fromLocation;
    private String toLocation;
    private String goodsDescription;
    private String packageType;
    private Integer packageCount;
    private String vehicleNumber;
    private String vehicleType;
    private BigDecimal amount;
}
