package com.rev.app.repository;

import com.rev.app.entity.MoneyRequest;
import com.rev.app.entity.RequestStatus;
import com.rev.app.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface IMoneyRequestRepository extends JpaRepository<MoneyRequest, Long> {

    // Explicit CRUD operations for clarity
    <S extends MoneyRequest> S save(S entity);

    Optional<MoneyRequest> findById(Long id);

    List<MoneyRequest> findAll();

    boolean existsById(Long id);

    long count();

    void deleteById(Long id);

    void delete(MoneyRequest entity);

    List<MoneyRequest> findByRequesterOrderByCreatedAtDesc(User requester);

    List<MoneyRequest> findByRequesteeOrderByCreatedAtDesc(User requestee);

    List<MoneyRequest> findByRequesteeAndStatus(User requestee, RequestStatus status);

    List<MoneyRequest> findByRequesterAndStatus(User requester, RequestStatus status);

    long countByRequesteeAndStatus(User requestee, RequestStatus status);
}
