package ru.bukhtaev.repository.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.bukhtaev.model.City;

import java.util.Optional;
import java.util.UUID;

/**
 * JPA-репозиторий городов.
 */
@Repository
public interface ICityJpaRepository extends JpaRepository<City, UUID> {

    Optional<City> findFirstByName(final String name);

    Optional<City> findFirstByNameAndIdNot(final String name, final UUID id);
}
