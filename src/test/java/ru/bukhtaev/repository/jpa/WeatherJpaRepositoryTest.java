package ru.bukhtaev.repository.jpa;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import ru.bukhtaev.AbstractContainerizedTest;
import ru.bukhtaev.model.City;
import ru.bukhtaev.model.Weather;
import ru.bukhtaev.model.WeatherType;
import ru.bukhtaev.util.WeatherSort;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Тест JPA-репозитория данных о погоде.
 */
@DataJpaTest
class WeatherJpaRepositoryTest extends AbstractContainerizedTest {

    /**
     * Текущая дата и время.
     */
    protected static final LocalDateTime NOW = LocalDateTime.now().withNano(0);

    /**
     * Дата и время сутки назад от текущей.
     */
    protected static final LocalDateTime YESTERDAY = NOW.minusDays(1);

    /**
     * Тестируемый JPA-репозиторий данных о погоде.
     */
    @Autowired
    private IWeatherJpaRepository underTest;

    /**
     * Репозиторий городов.
     */
    @Autowired
    private ICityJpaRepository cityRepository;

    /**
     * Репозиторий типов погоды.
     */
    @Autowired
    private IWeatherTypeJpaRepository typeRepository;

    private Weather weather1;
    private Weather weather2;
    private Weather weather3;

    private City cityKazan;
    private City cityYekaterinburg;

    private WeatherType typeClear;
    private WeatherType typeBlizzard;

    @BeforeEach
    void setUp() {
        cityKazan = cityRepository.save(
                City.builder()
                        .name("Казань")
                        .build()
        );
        cityYekaterinburg = cityRepository.save(
                City.builder()
                        .name("Екатеринбург")
                        .build()
        );
        typeClear = typeRepository.save(
                WeatherType.builder()
                        .name("Ясно")
                        .build()
        );
        typeBlizzard = typeRepository.save(
                WeatherType.builder()
                        .name("Метель")
                        .build()
        );

        weather1 = Weather.builder()
                .city(cityKazan)
                .type(typeClear)
                .temperature(24.57)
                .dateTime(NOW)
                .build();
        weather2 = Weather.builder()
                .city(cityYekaterinburg)
                .type(typeBlizzard)
                .temperature(-28.72)
                .dateTime(YESTERDAY)
                .build();
        weather3 = Weather.builder()
                .city(cityYekaterinburg)
                .type(typeClear)
                .temperature(0.46)
                .dateTime(NOW)
                .build();
    }

    @AfterEach
    void tearDown() {
        underTest.deleteAll();
        cityRepository.deleteAll();
        typeRepository.deleteAll();
    }

    @Test
    void deleteAllByCityName_withExistentCityName_shouldDeleteMatchingEntities() {
        // given
        underTest.save(weather1);
        underTest.save(weather2);
        underTest.save(weather3);
        assertThat(underTest.findAll()).hasSize(3);

        // when
        final List<Weather> removed = underTest.deleteAllByCityName(cityYekaterinburg.getName());

        // then
        assertThat(removed).containsOnly(weather2, weather3);
        final var weatherData = underTest.findAll();
        assertThat(weatherData)
                .hasSize(1)
                .containsOnly(weather1);
    }

    @Test
    void deleteAllByCityName_withNonExistentCityName_shouldNotDeleteAnything() {
        // given
        final String anotherCityName = "Новосибирск";
        underTest.save(weather1);
        underTest.save(weather2);
        underTest.save(weather3);
        assertThat(underTest.findAll()).hasSize(3);

        // when
        final List<Weather> removed = underTest.deleteAllByCityName(anotherCityName);

        // then
        assertThat(removed).isEmpty();
        final var weatherData = underTest.findAll();
        assertThat(weatherData)
                .hasSize(3)
                .containsAll(List.of(
                        weather1,
                        weather2,
                        weather3
                ));
    }

    @Test
    void deleteAllById_withExistentId_shouldDeleteAndReturnMatchingEntities() {
        // given
        underTest.save(weather1);
        underTest.save(weather2);
        underTest.save(weather3);
        assertThat(underTest.findAll()).hasSize(3);

        // when
        final List<Weather> removed = underTest.deleteAllById(weather2.getId());

        // then
        assertThat(removed).hasSize(1);
        final Weather weather = removed.get(0);
        assertThat(weather.getId())
                .isEqualTo(weather2.getId());
        assertThat(weather.getCity())
                .isEqualTo(weather2.getCity());
        assertThat(weather.getType())
                .isEqualTo(weather2.getType());
        assertThat(weather.getTemperature())
                .isEqualTo(weather2.getTemperature());
        assertThat(weather.getDateTime())
                .isEqualTo(weather2.getDateTime());
        final var weatherData = underTest.findAll();
        assertThat(weatherData)
                .hasSize(2)
                .containsOnly(weather1, weather3);
    }

    @Test
    void deleteAllById_withNonExistentId_shouldNotDeleteAnything() {
        // given
        final UUID anotherId = UUID.randomUUID();
        underTest.save(weather1);
        underTest.save(weather2);
        underTest.save(weather3);
        assertThat(underTest.findAll()).hasSize(3);

        // when
        final List<Weather> removed = underTest.deleteAllById(anotherId);

        // then
        assertThat(removed).isEmpty();
        final var weatherData = underTest.findAll();
        assertThat(weatherData)
                .hasSize(3)
                .containsAll(List.of(
                        weather1,
                        weather2,
                        weather3
                ));
    }

    @Test
    void findAllByCityName_withExistentCityName_shouldReturnFoundEntities() {
        // given
        underTest.save(weather1);
        underTest.save(weather2);
        underTest.save(weather3);
        assertThat(underTest.findAll()).hasSize(3);

        // when
        final var weatherData = underTest.findAllByCityName(cityYekaterinburg.getName());

        // then
        assertThat(weatherData)
                .hasSize(2)
                .containsOnly(weather2, weather3);
    }

    @Test
    void findAllByCityName_withNonExistentCityName_shouldReturnEmptyList() {
        // given
        final String anotherCityName = "Новосибирск";
        underTest.save(weather1);
        underTest.save(weather2);
        underTest.save(weather3);
        assertThat(underTest.findAll()).hasSize(3);

        // when
        final var weatherData = underTest.findAllByCityName(anotherCityName);

        // then
        assertThat(weatherData).isEmpty();
    }

    @Test
    void findAllByCityName_withPageableAndExistentCityName_shouldReturnFoundEntities() {
        // given
        final var todayWeather = Weather.builder()
                .city(cityKazan)
                .type(typeClear)
                .temperature(-15.7)
                .dateTime(NOW)
                .build();

        final var yesterdayWeather = Weather.builder()
                .city(cityKazan)
                .type(typeClear)
                .temperature(-18.3)
                .dateTime(NOW.minusDays(1))
                .build();

        final var tomorrowWeather = Weather.builder()
                .city(cityKazan)
                .type(typeClear)
                .temperature(-16.5)
                .dateTime(NOW.plusDays(1))
                .build();

        underTest.save(todayWeather);
        underTest.save(yesterdayWeather);
        underTest.save(tomorrowWeather);
        assertThat(underTest.findAll()).hasSize(3);

        final var pageRequest = PageRequest.of(
                0,
                2,
                WeatherSort.DATE_TIME_DESC.getSortValue()
        );

        // when
        final var weatherData = underTest.findAllByCityName(
                cityKazan.getName(),
                pageRequest
        );

        // then
        assertThat(weatherData)
                .hasSize(2)
                .containsOnly(todayWeather, tomorrowWeather);
    }

    @Test
    void findAllByCityName_withPageableAndNonExistentCityName_shouldReturnEmptyList() {
        // given
        final var todayWeather = Weather.builder()
                .city(cityKazan)
                .type(typeClear)
                .temperature(-15.7)
                .dateTime(NOW)
                .build();

        final var yesterdayWeather = Weather.builder()
                .city(cityKazan)
                .type(typeClear)
                .temperature(-18.3)
                .dateTime(NOW.minusDays(1))
                .build();

        final var tomorrowWeather = Weather.builder()
                .city(cityKazan)
                .type(typeClear)
                .temperature(-16.5)
                .dateTime(NOW.plusDays(1))
                .build();

        final String anotherCityName = "Новосибирск";
        underTest.save(todayWeather);
        underTest.save(yesterdayWeather);
        underTest.save(tomorrowWeather);
        assertThat(underTest.findAll()).hasSize(3);

        final var pageRequest = PageRequest.of(
                0,
                2,
                WeatherSort.DATE_TIME_DESC.getSortValue()
        );

        // when
        final var weatherData = underTest.findAllByCityName(
                anotherCityName,
                pageRequest
        );

        // then
        assertThat(weatherData).isEmpty();
    }

    @Test
    void findFirstByCityIdAndDateTime_withExistentData_shouldReturnFoundEntity() {
        // given
        underTest.save(weather1);
        underTest.save(weather2);
        underTest.save(weather3);
        assertThat(underTest.findAll()).hasSize(3);

        // when
        final var optWeather = underTest.findFirstByCityIdAndDateTime(
                cityKazan.getId(),
                NOW
        );

        // then
        assertThat(optWeather).isPresent();
        assertThat(optWeather.get().getCity())
                .isEqualTo(weather1.getCity());
        assertThat(optWeather.get().getType())
                .isEqualTo(weather1.getType());
        assertThat(optWeather.get().getTemperature())
                .isEqualTo(weather1.getTemperature());
        assertThat(optWeather.get().getDateTime())
                .isEqualTo(weather1.getDateTime());
    }

    @Test
    void findFirstByCityIdAndDateTime_withNonExistentData_shouldReturnEmptyOptional() {
        // given
        underTest.save(weather1);
        underTest.save(weather2);
        underTest.save(weather3);
        assertThat(underTest.findAll()).hasSize(3);

        // when
        final var optWeather = underTest.findFirstByCityIdAndDateTime(
                cityKazan.getId(),
                YESTERDAY
        );

        // then
        assertThat(optWeather).isNotPresent();
    }

    @Test
    void findFirstByCityNameAndDateTime_withExistentData_shouldReturnFoundEntity() {
        // given
        underTest.save(weather1);
        underTest.save(weather2);
        underTest.save(weather3);
        assertThat(underTest.findAll()).hasSize(3);

        // when
        final var optWeather = underTest.findFirstByCityNameAndDateTime(
                cityKazan.getName(),
                NOW
        );

        // then
        assertThat(optWeather).isPresent();
        assertThat(optWeather.get().getCity())
                .isEqualTo(weather1.getCity());
        assertThat(optWeather.get().getType())
                .isEqualTo(weather1.getType());
        assertThat(optWeather.get().getTemperature())
                .isEqualTo(weather1.getTemperature());
        assertThat(optWeather.get().getDateTime())
                .isEqualTo(weather1.getDateTime());
    }

    @Test
    void findFirstByCityNameAndDateTime_withNonExistentData_shouldReturnEmptyOptional() {
        // given
        underTest.save(weather1);
        underTest.save(weather2);
        underTest.save(weather3);
        assertThat(underTest.findAll()).hasSize(3);

        // when
        final var optWeather = underTest.findFirstByCityNameAndDateTime(
                cityKazan.getName(),
                YESTERDAY
        );

        // then
        assertThat(optWeather).isNotPresent();
    }

    @Test
    void findFirstByCityIdAndDateTimeAndIdNot_withExistentDataAndNonExistentId_shouldReturnFoundEntity() {
        // given
        final UUID anotherId = UUID.randomUUID();
        underTest.save(weather1);
        underTest.save(weather2);
        underTest.save(weather3);
        assertThat(underTest.findAll()).hasSize(3);

        // when
        final var optWeather = underTest.findFirstByCityIdAndDateTimeAndIdNot(
                cityKazan.getId(),
                NOW,
                anotherId
        );

        // then
        assertThat(optWeather).isPresent();
        assertThat(optWeather.get().getCity())
                .isEqualTo(weather1.getCity());
        assertThat(optWeather.get().getType())
                .isEqualTo(weather1.getType());
        assertThat(optWeather.get().getTemperature())
                .isEqualTo(weather1.getTemperature());
        assertThat(optWeather.get().getDateTime())
                .isEqualTo(weather1.getDateTime());
    }

    @Test
    void findFirstByCityIdAndDateTimeAndIdNot_withExistentDataAndExistentId_shouldReturnEmptyOptional() {
        // given
        final Weather saved = underTest.save(weather1);
        underTest.save(weather2);
        underTest.save(weather3);
        assertThat(underTest.findAll()).hasSize(3);

        // when
        final var optWeather = underTest.findFirstByCityIdAndDateTimeAndIdNot(
                cityKazan.getId(),
                NOW,
                saved.getId()
        );

        // then
        assertThat(optWeather).isNotPresent();
    }

    @Test
    void findFirstByCityIdAndDateTimeAndIdNot_withNonExistentDataAndNonExistentId_shouldReturnEmptyOptional() {
        // given
        final UUID anotherId = UUID.randomUUID();
        underTest.save(weather1);
        underTest.save(weather2);
        underTest.save(weather3);
        assertThat(underTest.findAll()).hasSize(3);

        // when
        final var optWeather = underTest.findFirstByCityIdAndDateTimeAndIdNot(
                cityKazan.getId(),
                YESTERDAY,
                anotherId
        );

        // then
        assertThat(optWeather).isNotPresent();
    }

    @Test
    void findFirstByCityIdAndDateTimeAndIdNot_withNonExistentDataAndExistentId_shouldReturnEmptyOptional() {
        // given
        underTest.save(weather1);
        underTest.save(weather2);
        underTest.save(weather3);
        assertThat(underTest.findAll()).hasSize(3);

        // when
        final var optWeather = underTest.findFirstByCityIdAndDateTimeAndIdNot(
                cityKazan.getId(),
                YESTERDAY,
                weather1.getId()
        );

        // then
        assertThat(optWeather).isNotPresent();
    }
}