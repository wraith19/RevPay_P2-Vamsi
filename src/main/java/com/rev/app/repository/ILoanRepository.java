package com.rev.app.repository;

import com.rev.app.entity.Loan;
import com.rev.app.entity.LoanStatus;
import com.rev.app.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ILoanRepository extends JpaRepository<Loan, Long> {

    // Explicit CRUD operations for clarity
    <S extends Loan> S save(S entity);

    Optional<Loan> findById(Long id);

    List<Loan> findAll();

    boolean existsById(Long id);

    long count();

    void deleteById(Long id);

    void delete(Loan entity);

    List<Loan> findByBusinessUserOrderByAppliedAtDesc(User businessUser);

    List<Loan> findByBusinessUserAndStatus(User businessUser, LoanStatus status);

    List<Loan> findByStatus(LoanStatus status);
}
