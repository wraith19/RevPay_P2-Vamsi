package com.rev.app.repository;

import com.rev.app.entity.SecurityQuestion;
import com.rev.app.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ISecurityQuestionRepository extends JpaRepository<SecurityQuestion, Long> {

    // Explicit CRUD operations for clarity
    <S extends SecurityQuestion> S save(S entity);

    Optional<SecurityQuestion> findById(Long id);

    List<SecurityQuestion> findAll();

    boolean existsById(Long id);

    long count();

    void deleteById(Long id);

    void delete(SecurityQuestion entity);

    List<SecurityQuestion> findByUser(User user);

    List<SecurityQuestion> findByUserId(Long userId);
}
