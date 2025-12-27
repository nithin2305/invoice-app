package com.invoice.app.repository;

import com.invoice.app.entity.Invoice;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {

    Optional<Invoice> findByInvoiceNo(String invoiceNo);

    List<Invoice> findByInvoiceNoContainingIgnoreCase(String invoiceNo);

    @Query("SELECT DISTINCT i FROM Invoice i LEFT JOIN i.items it " +
           "WHERE (:invoiceNo IS NULL OR LOWER(i.invoiceNo) LIKE LOWER(CONCAT('%', :invoiceNo, '%'))) " +
           "AND (:lrNo IS NULL OR LOWER(it.lrNo) LIKE LOWER(CONCAT('%', :lrNo, '%')))")
    List<Invoice> searchInvoices(@Param("invoiceNo") String invoiceNo, @Param("lrNo") String lrNo);

    @Query("SELECT i FROM Invoice i WHERE i.invoiceDate BETWEEN :startDate AND :endDate ORDER BY i.invoiceDate")
    List<Invoice> findByInvoiceDateBetween(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT i FROM Invoice i WHERE YEAR(i.invoiceDate) = :year AND MONTH(i.invoiceDate) = :month ORDER BY i.invoiceDate")
    List<Invoice> findByYearAndMonth(@Param("year") int year, @Param("month") int month);

    @Query("SELECT i FROM Invoice i ORDER BY i.id DESC")
    List<Invoice> findLatestInvoice(Pageable pageable);
}
