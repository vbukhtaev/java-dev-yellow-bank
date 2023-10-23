package ru.bukhtaev.repository.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.bukhtaev.model.WeatherType;

import java.util.Optional;
import java.util.UUID;

/**
 * JPA-репозиторий типов погоды.
 */
@Repository
public interface IWeatherTypeJpaRepository extends JpaRepository<WeatherType, UUID> {

    Optional<WeatherType> findFirstByName(final String name);

    Optional<WeatherType> findFirstByNameAndIdNot(final String name, final UUID id);
}
