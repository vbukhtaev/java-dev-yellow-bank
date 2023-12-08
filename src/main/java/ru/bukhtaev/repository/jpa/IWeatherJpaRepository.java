package ru.bukhtaev.repository.jpa;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.bukhtaev.model.Weather;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * JPA-репозиторий данных о погоде.
 */
@Repository
public interface IWeatherJpaRepository extends JpaRepository<Weather, UUID> {

    List<Weather> deleteAllById(final UUID id);

    List<Weather> deleteAllByCityName(final String cityName);

    List<Weather> findAllByCityName(final String cityName);

    List<Weather> findAllByCityName(
            final String cityName,
            final Pageable pageable
    );

    Optional<Weather> findFirstByCityIdAndDateTime(
            final UUID cityId,
            final LocalDateTime dateTime
    );

    Optional<Weather> findFirstByCityNameAndDateTime(
            final String cityName,
            final LocalDateTime dateTime
    );

    Optional<Weather> findFirstByCityIdAndDateTimeAndIdNot(
            final UUID cityId,
            final LocalDateTime dateTime,
            final UUID id
    );
}
