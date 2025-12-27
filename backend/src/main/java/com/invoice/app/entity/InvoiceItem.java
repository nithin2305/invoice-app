package com.invoice.app.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "invoice_items")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invoice_id", nullable = false)
    @JsonIgnore
    private Invoice invoice;

    @Column(name = "lr_no", length = 50)
    private String lrNo;

    @Column(name = "lr_date")
    private LocalDate lrDate;

    @Column(name = "from_location")
    private String fromLocation;

    @Column(name = "to_location")
    private String toLocation;

    @Column(name = "goods_description", length = 500)
    private String goodsDescription;

    @Column(name = "package_type", length = 50)
    private String packageType;

    @Column(name = "package_count")
    private Integer packageCount;

    @Column(name = "vehicle_number", length = 20)
    private String vehicleNumber;

    @Column(name = "vehicle_type", length = 50)
    private String vehicleType;

    @Column(precision = 12, scale = 2)
    private BigDecimal amount = BigDecimal.ZERO;
}
