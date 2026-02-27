package com.rev.app.repository;

import com.rev.app.entity.PaymentMethod;
import com.rev.app.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface IPaymentMethodRepository extends JpaRepository<PaymentMethod, Long> {

    // Explicit CRUD operations for clarity
    <S extends PaymentMethod> S save(S entity);

    Optional<PaymentMethod> findById(Long id);

    List<PaymentMethod> findAll();

    boolean existsById(Long id);

    long count();

    void deleteById(Long id);

    void delete(PaymentMethod entity);

    List<PaymentMethod> findByUser(User user);

    List<PaymentMethod> findByUserId(Long userId);

    Optional<PaymentMethod> findByUserAndIsDefaultTrue(User user);
}
