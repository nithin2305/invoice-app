package com.invoice.app.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "invoices")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Invoice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "invoice_no", nullable = false, unique = true)
    private String invoiceNo;

    @Column(name = "invoice_date")
    private LocalDate invoiceDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "party_id")
    private Client party;

    @Column(name = "party_name")
    private String partyName;

    @Column(name = "party_address", length = 500)
    private String partyAddress;

    @Column(name = "party_gst", length = 20)
    private String partyGst;

    @Column(name = "halting_charges", precision = 12, scale = 2)
    private BigDecimal haltingCharges = BigDecimal.ZERO;

    @Column(name = "loading_charges", precision = 12, scale = 2)
    private BigDecimal loadingCharges = BigDecimal.ZERO;

    @Column(name = "unloading_charges", precision = 12, scale = 2)
    private BigDecimal unloadingCharges = BigDecimal.ZERO;

    @Column(name = "total_amount", precision = 12, scale = 2)
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @Column(name = "amount_in_words", length = 500)
    private String amountInWords;

    @Column(length = 1000)
    private String remarks;

    @OneToMany(mappedBy = "invoice", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<InvoiceItem> items = new ArrayList<>();

    public void addItem(InvoiceItem item) {
        items.add(item);
        item.setInvoice(this);
    }

    public void removeItem(InvoiceItem item) {
        items.remove(item);
        item.setInvoice(null);
    }
}
