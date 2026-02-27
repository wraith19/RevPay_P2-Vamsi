package com.rev.app.repository;

import com.rev.app.entity.InvoiceItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface IInvoiceItemRepository extends JpaRepository<InvoiceItem, Long> {

    // Explicit CRUD operations for clarity
    <S extends InvoiceItem> S save(S entity);

    Optional<InvoiceItem> findById(Long id);

    List<InvoiceItem> findAll();

    boolean existsById(Long id);

    long count();

    void deleteById(Long id);

    void delete(InvoiceItem entity);

    List<InvoiceItem> findByInvoiceId(Long invoiceId);
}
