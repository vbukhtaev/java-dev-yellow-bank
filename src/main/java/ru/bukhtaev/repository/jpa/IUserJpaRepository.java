package ru.bukhtaev.repository.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.bukhtaev.model.User;

import java.util.Optional;
import java.util.UUID;

/**
 * JPA-репозиторий пользователей.
 */
@Repository
public interface IUserJpaRepository extends JpaRepository<User, UUID> {

    Optional<User> findByUsername(final String username);
}
