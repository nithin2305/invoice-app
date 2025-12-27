package com.invoice.app.service;

import com.invoice.app.config.GlobalExceptionHandler;
import com.invoice.app.dto.InvoiceDTO;
import com.invoice.app.dto.InvoiceItemDTO;
import com.invoice.app.dto.MonthlyStatementDTO;
import com.invoice.app.entity.Client;
import com.invoice.app.entity.Invoice;
import com.invoice.app.entity.InvoiceItem;
import com.invoice.app.repository.ClientRepository;
import com.invoice.app.repository.InvoiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final ClientRepository clientRepository;

    @Transactional
    public InvoiceDTO createInvoice(InvoiceDTO dto) {
        Invoice invoice = toEntity(dto);
        
        // Link party if partyId is provided
        if (dto.getPartyId() != null) {
            Client party = clientRepository.findById(dto.getPartyId()).orElse(null);
            invoice.setParty(party);
        }

        // Set items relationship
        if (dto.getItems() != null) {
            for (InvoiceItemDTO itemDTO : dto.getItems()) {
                InvoiceItem item = toItemEntity(itemDTO);
                invoice.addItem(item);
            }
        }

        Invoice saved = invoiceRepository.save(invoice);
        return toDTO(saved);
    }

    @Transactional(readOnly = true)
    public InvoiceDTO getInvoice(Long id) {
        return invoiceRepository.findById(id)
                .map(this::toDTO)
                .orElseThrow(() -> new GlobalExceptionHandler.ResourceNotFoundException("Invoice not found: " + id));
    }

    @Transactional(readOnly = true)
    public Invoice getInvoiceEntity(Long id) {
        return invoiceRepository.findById(id)
                .orElseThrow(() -> new GlobalExceptionHandler.ResourceNotFoundException("Invoice not found: " + id));
    }

    @Transactional(readOnly = true)
    public List<InvoiceDTO> searchInvoices(String invoiceNo, String lrNo) {
        List<Invoice> invoices;
        
        if ((invoiceNo == null || invoiceNo.trim().isEmpty()) && 
            (lrNo == null || lrNo.trim().isEmpty())) {
            // Return recent invoices if no search criteria
            invoices = invoiceRepository.findAll();
        } else {
            invoices = invoiceRepository.searchInvoices(
                invoiceNo != null && !invoiceNo.trim().isEmpty() ? invoiceNo.trim() : null,
                lrNo != null && !lrNo.trim().isEmpty() ? lrNo.trim() : null
            );
        }
        
        return invoices.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<InvoiceDTO> getAllInvoices() {
        return invoiceRepository.findAll()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public String getNextInvoiceNumber() {
        return invoiceRepository.findLatestInvoice()
                .map(invoice -> {
                    String lastInvoiceNo = invoice.getInvoiceNo();
                    try {
                        // Extract numeric part and increment
                        String numericPart = lastInvoiceNo.replaceAll("[^0-9]", "");
                        if (!numericPart.isEmpty()) {
                            int nextNumber = Integer.parseInt(numericPart) + 1;
                            // Preserve prefix if exists
                            String prefix = lastInvoiceNo.replaceAll("[0-9]", "");
                            return prefix + nextNumber;
                        }
                    } catch (NumberFormatException e) {
                        // If parsing fails, return default
                    }
                    return "INV001";
                })
                .orElse("INV001");
    }

    @Transactional
    public InvoiceDTO updateInvoice(Long id, InvoiceDTO dto) {
        Invoice existing = invoiceRepository.findById(id)
                .orElseThrow(() -> new GlobalExceptionHandler.ResourceNotFoundException("Invoice not found: " + id));
        
        // Update fields
        existing.setInvoiceNo(dto.getInvoiceNo());
        
        if (dto.getInvoiceDate() != null && !dto.getInvoiceDate().isEmpty()) {
            existing.setInvoiceDate(LocalDate.parse(dto.getInvoiceDate()));
        }
        
        // Link party if partyId is provided
        if (dto.getPartyId() != null) {
            Client party = clientRepository.findById(dto.getPartyId()).orElse(null);
            existing.setParty(party);
        }
        
        existing.setPartyName(dto.getPartyName());
        existing.setPartyAddress(dto.getPartyAddress());
        existing.setPartyGst(dto.getPartyGst());
        existing.setHaltingCharges(nullSafe(dto.getHaltingCharges()));
        existing.setLoadingCharges(nullSafe(dto.getLoadingCharges()));
        existing.setUnloadingCharges(nullSafe(dto.getUnloadingCharges()));
        existing.setTotalAmount(nullSafe(dto.getTotalAmount()));
        existing.setAmountInWords(dto.getAmountInWords());
        existing.setRemarks(dto.getRemarks());
        
        // Update items - clear and re-add
        existing.getItems().clear();
        if (dto.getItems() != null) {
            for (InvoiceItemDTO itemDTO : dto.getItems()) {
                InvoiceItem item = toItemEntity(itemDTO);
                existing.addItem(item);
            }
        }
        
        Invoice saved = invoiceRepository.save(existing);
        return toDTO(saved);
    }

    @Transactional(readOnly = true)
    public MonthlyStatementDTO getMonthlyStatement(int year, int month) {
        List<Invoice> invoices = invoiceRepository.findByYearAndMonth(year, month);
        
        List<InvoiceDTO> invoiceDTOs = invoices.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());

        BigDecimal totalAmount = invoices.stream()
                .map(Invoice::getTotalAmount)
                .filter(amt -> amt != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        String monthName = Month.of(month).getDisplayName(TextStyle.FULL, Locale.ENGLISH);

        MonthlyStatementDTO statement = new MonthlyStatementDTO();
        statement.setYear(year);
        statement.setMonth(month);
        statement.setMonthName(monthName);
        statement.setTotalInvoices(invoices.size());
        statement.setTotalAmount(totalAmount);
        statement.setInvoices(invoiceDTOs);

        return statement;
    }

    private InvoiceDTO toDTO(Invoice invoice) {
        InvoiceDTO dto = new InvoiceDTO();
        dto.setId(invoice.getId());
        dto.setInvoiceNo(invoice.getInvoiceNo());
        dto.setInvoiceDate(invoice.getInvoiceDate() != null ? invoice.getInvoiceDate().toString() : null);
        dto.setPartyId(invoice.getParty() != null ? invoice.getParty().getId() : null);
        dto.setPartyName(invoice.getPartyName());
        dto.setPartyAddress(invoice.getPartyAddress());
        dto.setPartyGst(invoice.getPartyGst());
        dto.setHaltingCharges(invoice.getHaltingCharges());
        dto.setLoadingCharges(invoice.getLoadingCharges());
        dto.setUnloadingCharges(invoice.getUnloadingCharges());
        dto.setTotalAmount(invoice.getTotalAmount());
        dto.setAmountInWords(invoice.getAmountInWords());
        dto.setRemarks(invoice.getRemarks());

        if (invoice.getItems() != null) {
            dto.setItems(invoice.getItems().stream()
                    .map(this::toItemDTO)
                    .collect(Collectors.toList()));
        } else {
            dto.setItems(new ArrayList<>());
        }

        return dto;
    }

    private InvoiceItemDTO toItemDTO(InvoiceItem item) {
        InvoiceItemDTO dto = new InvoiceItemDTO();
        dto.setId(item.getId());
        dto.setLrNo(item.getLrNo());
        dto.setLrDate(item.getLrDate() != null ? item.getLrDate().toString() : null);
        dto.setFromLocation(item.getFromLocation());
        dto.setToLocation(item.getToLocation());
        dto.setGoodsDescription(item.getGoodsDescription());
        dto.setPackageType(item.getPackageType());
        dto.setPackageCount(item.getPackageCount());
        dto.setVehicleNumber(item.getVehicleNumber());
        dto.setVehicleType(item.getVehicleType());
        dto.setAmount(item.getAmount());
        return dto;
    }

    private Invoice toEntity(InvoiceDTO dto) {
        Invoice invoice = new Invoice();
        invoice.setInvoiceNo(dto.getInvoiceNo());
        
        if (dto.getInvoiceDate() != null && !dto.getInvoiceDate().isEmpty()) {
            invoice.setInvoiceDate(LocalDate.parse(dto.getInvoiceDate()));
        }
        
        invoice.setPartyName(dto.getPartyName());
        invoice.setPartyAddress(dto.getPartyAddress());
        invoice.setPartyGst(dto.getPartyGst());
        invoice.setHaltingCharges(nullSafe(dto.getHaltingCharges()));
        invoice.setLoadingCharges(nullSafe(dto.getLoadingCharges()));
        invoice.setUnloadingCharges(nullSafe(dto.getUnloadingCharges()));
        invoice.setTotalAmount(nullSafe(dto.getTotalAmount()));
        invoice.setAmountInWords(dto.getAmountInWords());
        invoice.setRemarks(dto.getRemarks());
        
        return invoice;
    }

    private InvoiceItem toItemEntity(InvoiceItemDTO dto) {
        InvoiceItem item = new InvoiceItem();
        item.setLrNo(dto.getLrNo());
        
        if (dto.getLrDate() != null && !dto.getLrDate().isEmpty()) {
            item.setLrDate(LocalDate.parse(dto.getLrDate()));
        }
        
        item.setFromLocation(dto.getFromLocation());
        item.setToLocation(dto.getToLocation());
        item.setGoodsDescription(dto.getGoodsDescription());
        item.setPackageType(dto.getPackageType());
        item.setPackageCount(dto.getPackageCount());
        item.setVehicleNumber(dto.getVehicleNumber());
        item.setVehicleType(dto.getVehicleType());
        item.setAmount(nullSafe(dto.getAmount()));
        
        return item;
    }

    private BigDecimal nullSafe(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }
}
