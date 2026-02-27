package com.rev.app.repository;

import com.rev.app.entity.Invoice;
import com.rev.app.entity.InvoiceStatus;
import com.rev.app.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface IInvoiceRepository extends JpaRepository<Invoice, Long> {

    // Explicit CRUD operations for clarity
    <S extends Invoice> S save(S entity);

    Optional<Invoice> findById(Long id);

    List<Invoice> findAll();

    boolean existsById(Long id);

    long count();

    void deleteById(Long id);

    void delete(Invoice entity);

    List<Invoice> findByBusinessUserOrderByCreatedAtDesc(User businessUser);

    List<Invoice> findByBusinessUserAndStatus(User businessUser, InvoiceStatus status);

    Optional<Invoice> findByInvoiceNumber(String invoiceNumber);

    @Query("SELECT COALESCE(SUM(i.totalAmount), 0) FROM Invoice i WHERE i.businessUser = :user AND i.status = 'PAID'")
    BigDecimal sumPaidInvoiceAmount(@Param("user") User user);

    @Query("SELECT COALESCE(SUM(i.totalAmount), 0) FROM Invoice i WHERE i.businessUser = :user AND (i.status = 'SENT' OR i.status = 'OVERDUE')")
    BigDecimal sumOutstandingInvoiceAmount(@Param("user") User user);

    long countByBusinessUserAndStatus(User businessUser, InvoiceStatus status);
}
