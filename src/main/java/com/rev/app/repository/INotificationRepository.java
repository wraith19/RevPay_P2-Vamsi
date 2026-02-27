package com.rev.app.repository;

import com.rev.app.entity.Notification;
import com.rev.app.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface INotificationRepository extends JpaRepository<Notification, Long> {

    // Explicit CRUD operations for clarity
    <S extends Notification> S save(S entity);

    Optional<Notification> findById(Long id);

    List<Notification> findAll();

    boolean existsById(Long id);

    long count();

    void deleteById(Long id);

    void delete(Notification entity);

    List<Notification> findByUserOrderByTimestampDesc(User user);

    List<Notification> findByUserAndIsReadFalseOrderByTimestampDesc(User user);

    long countByUserAndIsReadFalse(User user);

    Optional<Notification> findByIdAndUser(Long id, User user);
}
