package com.rev.app.repository;

import com.rev.app.entity.Wallet;
import com.rev.app.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;

@Repository
public interface IWalletRepository extends JpaRepository<Wallet, Long> {

    // Explicit CRUD operations for clarity
    <S extends Wallet> S save(S entity);

    Optional<Wallet> findById(Long id);

    List<Wallet> findAll();

    boolean existsById(Long id);

    long count();

    void deleteById(Long id);

    void delete(Wallet entity);

    Optional<Wallet> findByUser(User user);

    Optional<Wallet> findByUserId(Long userId);
}
