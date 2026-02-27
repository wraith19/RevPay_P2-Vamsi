package com.rev.app.repository;

import com.rev.app.entity.Role;
import com.rev.app.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IUserRepository extends JpaRepository<User, Long> {

    // Explicit CRUD operations for clarity
    <S extends User> S save(S entity);

    Optional<User> findById(Long id);

    List<User> findAll();

    boolean existsById(Long id);

    long count();

    void deleteById(Long id);

    void delete(User entity);

    Optional<User> findByEmail(String email);

    Optional<User> findByPhone(String phone);

    Optional<User> findByEmailOrPhone(String email, String phone);

    boolean existsByEmail(String email);

    boolean existsByPhone(String phone);

    List<User> findAllByOrderByCreatedAtDesc();

    List<User> findByRoleOrderByCreatedAtDesc(Role role);

    List<User> findByRoleAndBusinessVerifiedOrderByCreatedAtDesc(Role role, Boolean businessVerified);

    long countByRole(Role role);

    long countByRoleAndBusinessVerified(Role role, Boolean businessVerified);
}
