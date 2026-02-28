package com.rev.app.repository;

import com.rev.app.entity.Transaction;
import com.rev.app.entity.TransactionStatus;
import com.rev.app.entity.TransactionType;
import com.rev.app.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ITransactionRepository extends JpaRepository<Transaction, Long> {

    // Explicit CRUD operations for clarity
    <S extends Transaction> S save(S entity);

    Optional<Transaction> findById(Long id);

    List<Transaction> findAll();

    boolean existsById(Long id);

    long count();

    void deleteById(Long id);

    void delete(Transaction entity);

    @Query("SELECT t FROM Transaction t WHERE t.sender = :user OR t.receiver = :user ORDER BY t.timestamp DESC")
        List<Transaction> findByUser(@Param("user") User user);

        List<Transaction> findBySenderOrderByTimestampDesc(User sender);

        List<Transaction> findByReceiverOrderByTimestampDesc(User receiver);

        Optional<Transaction> findByTransactionId(String transactionId);

        @Query("SELECT t FROM Transaction t WHERE (t.sender = :user OR t.receiver = :user) AND t.type = :type ORDER BY t.timestamp DESC")
        List<Transaction> findByUserAndType(@Param("user") User user, @Param("type") TransactionType type);

        @Query("SELECT t FROM Transaction t WHERE (t.sender = :user OR t.receiver = :user) AND t.timestamp BETWEEN :start AND :end ORDER BY t.timestamp DESC")
        List<Transaction> findByUserAndDateRange(@Param("user") User user, @Param("start") LocalDateTime start,
                        @Param("end") LocalDateTime end);

        @Query("SELECT t FROM Transaction t WHERE (t.sender = :user OR t.receiver = :user) AND t.status = :status ORDER BY t.timestamp DESC")
        List<Transaction> findByUserAndStatus(@Param("user") User user, @Param("status") TransactionStatus status);

        @Query("SELECT t FROM Transaction t WHERE (t.sender = :user OR t.receiver = :user) " +
                        "AND (:type IS NULL OR t.type = :type) " +
                        "AND (:status IS NULL OR t.status = :status) " +
                        "AND (:startDate IS NULL OR t.timestamp >= :startDate) " +
                        "AND (:endDate IS NULL OR t.timestamp <= :endDate) " +
                        "AND (:minAmount IS NULL OR t.amount >= :minAmount) " +
                        "AND (:maxAmount IS NULL OR t.amount <= :maxAmount) " +
                        "ORDER BY t.timestamp DESC")
        List<Transaction> findByFilters(@Param("user") User user,
                        @Param("type") TransactionType type,
                        @Param("status") TransactionStatus status,
                        @Param("startDate") LocalDateTime startDate,
                        @Param("endDate") LocalDateTime endDate,
                        @Param("minAmount") BigDecimal minAmount,
                        @Param("maxAmount") BigDecimal maxAmount);

        @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.receiver = :user AND t.status = com.rev.app.entity.TransactionStatus.SUCCESS")
        BigDecimal sumReceivedAmount(@Param("user") User user);

        @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.sender = :user AND t.status = com.rev.app.entity.TransactionStatus.SUCCESS")
        BigDecimal sumSentAmount(@Param("user") User user);

        @Query("SELECT t FROM Transaction t WHERE t.receiver = :user AND t.status = com.rev.app.entity.TransactionStatus.SUCCESS ORDER BY t.amount DESC")
        List<Transaction> findTopTransactionsByReceiver(@Param("user") User user);
}
