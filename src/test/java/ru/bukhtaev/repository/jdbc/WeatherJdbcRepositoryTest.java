package ru.bukhtaev.repository.jdbc;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import ru.bukhtaev.AbstractContainerizedTest;
import ru.bukhtaev.model.City;
import ru.bukhtaev.model.Weather;
import ru.bukhtaev.model.WeatherType;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Модульные тесты для JDBC-репозитория данных о погоде {@link WeatherJdbcRepository}.
 */
@JdbcTest
class WeatherJdbcRepositoryTest extends AbstractContainerizedTest {

    /**
     * Текущая дата и время.
     */
    protected static final LocalDateTime NOW = LocalDateTime.now().withNano(0);

    /**
     * Дата и время сутки назад от текущей.
     */
    protected static final LocalDateTime YESTERDAY = NOW.minusDays(1);

    /**
     * Объект для выполнения SQL-запросов
     * с использованием именованных параметров.
     */
    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    /**
     * Тестируемый JDBC-репозиторий данных о погоде.
     */
    private WeatherJdbcRepository underTest;

    /**
     * Репозиторий городов.
     */
    private CityJdbcRepository cityRepository;

    /**
     * Репозиторий типов погоды.
     */
    private WeatherTypeJdbcRepository typeRepository;

    private Weather weather1;
    private Weather weather2;
    private Weather weather3;

    private City cityKazan;
    private City cityYekaterinburg;

    private WeatherType typeClear;
    private WeatherType typeBlizzard;

    @BeforeEach
    void setUp() {
        cityRepository = new CityJdbcRepository(jdbcTemplate);
        typeRepository = new WeatherTypeJdbcRepository(jdbcTemplate);
        underTest = new WeatherJdbcRepository(jdbcTemplate);

        cityKazan = cityRepository.create(
                City.builder()
                        .name("Казань")
                        .build()
        );
        cityYekaterinburg = cityRepository.create(
                City.builder()
                        .name("Екатеринбург")
                        .build()
        );
        typeClear = typeRepository.create(
                WeatherType.builder()
                        .name("Ясно")
                        .build()
        );
        typeBlizzard = typeRepository.create(
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
    void findById_withExistentId_shouldReturnFoundEntity() {
        // given
        final Weather saved = underTest.create(weather1);
        underTest.create(weather2);
        underTest.create(weather3);
        assertThat(underTest.findAll()).hasSize(3);

        // when
        final var optWeather = underTest.findById(saved.getId());

        // then
        assertThat(optWeather).isPresent();
        assertThat(optWeather.get().getId())
                .isEqualTo(saved.getId());
        assertThat(optWeather.get().getCity().getId())
                .isEqualTo(weather1.getCity().getId());
        assertThat(optWeather.get().getCity().getName())
                .isEqualTo(weather1.getCity().getName());
        assertThat(optWeather.get().getType().getId())
                .isEqualTo(weather1.getType().getId());
        assertThat(optWeather.get().getType().getName())
                .isEqualTo(weather1.getType().getName());
        assertThat(optWeather.get().getTemperature())
                .isEqualTo(weather1.getTemperature());
        assertThat(optWeather.get().getDateTime())
                .isEqualTo(weather1.getDateTime());
    }

    @Test
    void findById_withNonExistentId_shouldReturnEmptyOptional() {
        // given
        final UUID anotherId = UUID.randomUUID();
        underTest.create(weather1);
        underTest.create(weather2);
        underTest.create(weather3);
        assertThat(underTest.findAll()).hasSize(3);

        // when
        final var optWeather = underTest.findById(anotherId);

        // then
        assertThat(optWeather).isNotPresent();
    }

    @Test
    void findAll_withExistentData_shouldReturnAllEntities() {
        // given
        final Weather savedWeather1 = underTest.create(weather1);
        final Weather savedWeather2 = underTest.create(weather2);
        final Weather savedWeather3 = underTest.create(weather3);
        final var savedData = List.of(
                savedWeather1,
                savedWeather2,
                savedWeather3
        );
        assertThat(underTest.findAll()).hasSize(3);

        // when
        final var weatherData = underTest.findAll();

        // then
        assertThat(weatherData).hasSize(3);

        for (int i = 0; i < weatherData.size(); i++) {
            final Weather weather = weatherData.get(i);
            assertThat(weather.getId())
                    .isEqualTo(savedData.get(i).getId());
            assertThat(weather.getCity().getId())
                    .isEqualTo(savedData.get(i).getCity().getId());
            assertThat(weather.getCity().getName())
                    .isEqualTo(savedData.get(i).getCity().getName());
            assertThat(weather.getType().getId())
                    .isEqualTo(savedData.get(i).getType().getId());
            assertThat(weather.getType().getName())
                    .isEqualTo(savedData.get(i).getType().getName());
            assertThat(weather.getTemperature())
                    .isEqualTo(savedData.get(i).getTemperature());
            assertThat(weather.getDateTime())
                    .isEqualTo(savedData.get(i).getDateTime());
        }
    }

    @Test
    void findAll_withNonExistentData_shouldReturnEmptyList() {
        // given
        underTest.deleteAll();
        assertThat(underTest.findAll()).isEmpty();

        // when
        final var weatherData = underTest.findAll();

        // then
        assertThat(weatherData).isEmpty();
    }

    @Test
    void findAllByCityName_withExistentCityName_shouldDeleteAllMatchingEntities() {
        // given
        underTest.create(weather1);
        final Weather savedWeather2 = underTest.create(weather2);
        final Weather savedWeather3 = underTest.create(weather3);
        final var savedData = List.of(
                savedWeather2,
                savedWeather3
        );
        assertThat(underTest.findAll()).hasSize(3);

        // when
        final var foundData = underTest.findAllByCityName(cityYekaterinburg.getName());

        // then
        assertThat(foundData).hasSize(2);

        for (int i = 0; i < foundData.size(); i++) {
            final Weather weather = foundData.get(i);
            assertThat(weather.getId())
                    .isEqualTo(savedData.get(i).getId());
            assertThat(weather.getCity().getId())
                    .isEqualTo(savedData.get(i).getCity().getId());
            assertThat(weather.getCity().getName())
                    .isEqualTo(savedData.get(i).getCity().getName());
            assertThat(weather.getType().getId())
                    .isEqualTo(savedData.get(i).getType().getId());
            assertThat(weather.getType().getName())
                    .isEqualTo(savedData.get(i).getType().getName());
            assertThat(weather.getTemperature())
                    .isEqualTo(savedData.get(i).getTemperature());
            assertThat(weather.getDateTime())
                    .isEqualTo(savedData.get(i).getDateTime());
        }
    }

    @Test
    void findAllByCityName_withNonExistentCityName_shouldNotDeleteAnything() {
        // given
        final String anotherCityName = "Новосибирск";
        underTest.create(weather1);
        underTest.create(weather2);
        underTest.create(weather3);
        assertThat(underTest.findAll()).hasSize(3);

        // when
        final var foundData = underTest.findAllByCityName(anotherCityName);

        // then
        assertThat(foundData).isEmpty();
    }

    @Test
    void deleteById_withExistentId_shouldDeleteMatchingEntity() {
        // given
        final Weather savedWeather1 = underTest.create(weather1);
        final Weather savedWeather2 = underTest.create(weather2);
        final Weather savedWeather3 = underTest.create(weather3);
        final List<Weather> remainingData = List.of(
                savedWeather2,
                savedWeather3
        );
        assertThat(underTest.findAll()).hasSize(3);

        // when
        underTest.deleteById(savedWeather1.getId());

        // then
        final var weatherData = underTest.findAll();
        assertThat(weatherData).hasSize(2);

        for (int i = 0; i < weatherData.size(); i++) {
            final Weather weather = weatherData.get(i);
            assertThat(weather.getId())
                    .isEqualTo(remainingData.get(i).getId());
            assertThat(weather.getCity().getId())
                    .isEqualTo(remainingData.get(i).getCity().getId());
            assertThat(weather.getCity().getName())
                    .isEqualTo(remainingData.get(i).getCity().getName());
            assertThat(weather.getType().getId())
                    .isEqualTo(remainingData.get(i).getType().getId());
            assertThat(weather.getType().getName())
                    .isEqualTo(remainingData.get(i).getType().getName());
            assertThat(weather.getTemperature())
                    .isEqualTo(remainingData.get(i).getTemperature());
            assertThat(weather.getDateTime())
                    .isEqualTo(remainingData.get(i).getDateTime());
        }
    }

    @Test
    void deleteById_withNonExistentId_shouldNotDeleteAnything() {
        // given
        final UUID anotherId = UUID.randomUUID();
        final Weather savedWeather1 = underTest.create(weather1);
        final Weather savedWeather2 = underTest.create(weather2);
        final Weather savedWeather3 = underTest.create(weather3);
        final var savedData = List.of(
                savedWeather1,
                savedWeather2,
                savedWeather3
        );
        assertThat(underTest.findAll()).hasSize(3);

        // when
        underTest.deleteById(anotherId);

        // then
        final var weatherData = underTest.findAll();
        assertThat(weatherData).hasSize(3);
        for (int i = 0; i < weatherData.size(); i++) {
            final Weather weather = weatherData.get(i);
            assertThat(weather.getId())
                    .isEqualTo(savedData.get(i).getId());
            assertThat(weather.getCity().getId())
                    .isEqualTo(savedData.get(i).getCity().getId());
            assertThat(weather.getCity().getName())
                    .isEqualTo(savedData.get(i).getCity().getName());
            assertThat(weather.getType().getId())
                    .isEqualTo(savedData.get(i).getType().getId());
            assertThat(weather.getType().getName())
                    .isEqualTo(savedData.get(i).getType().getName());
            assertThat(weather.getTemperature())
                    .isEqualTo(savedData.get(i).getTemperature());
            assertThat(weather.getDateTime())
                    .isEqualTo(savedData.get(i).getDateTime());
        }
    }

    @Test
    void deleteAll_shouldDeleteAllEntities() {
        // given
        underTest.create(weather1);
        underTest.create(weather2);
        underTest.create(weather3);
        assertThat(underTest.findAll()).hasSize(3);

        // when
        underTest.deleteAll();

        // then
        assertThat(underTest.findAll()).isEmpty();
    }

    @Test
    void deleteAllByCityName_withExistentCityName_shouldDeleteAllMatchingEntities() {
        // given
        final Weather saved = underTest.create(weather1);
        underTest.create(weather2);
        underTest.create(weather3);
        assertThat(underTest.findAll()).hasSize(3);

        // when
        underTest.deleteAllByCityName(cityYekaterinburg.getName());

        // then
        final var weatherData = underTest.findAll();
        assertThat(weatherData).hasSize(1);

        final Weather weather = weatherData.get(0);
        assertThat(weather.getId())
                .isEqualTo(saved.getId());
        assertThat(weather.getCity().getId())
                .isEqualTo(weather1.getCity().getId());
        assertThat(weather.getCity().getName())
                .isEqualTo(weather1.getCity().getName());
        assertThat(weather.getType().getId())
                .isEqualTo(weather1.getType().getId());
        assertThat(weather.getType().getName())
                .isEqualTo(weather1.getType().getName());
        assertThat(weather.getTemperature())
                .isEqualTo(weather1.getTemperature());
        assertThat(weather.getDateTime())
                .isEqualTo(weather1.getDateTime());
    }

    @Test
    void deleteAllByCityName_withNonExistentCityName_shouldNotDeleteAnything() {
        // given
        final String anotherCityName = "Новосибирск";
        final Weather savedWeather1 = underTest.create(weather1);
        final Weather savedWeather2 = underTest.create(weather2);
        final Weather savedWeather3 = underTest.create(weather3);
        final var savedData = List.of(
                savedWeather1,
                savedWeather2,
                savedWeather3
        );
        assertThat(underTest.findAll()).hasSize(3);

        // when
        underTest.deleteAllByCityName(anotherCityName);

        // then
        final var weatherData = underTest.findAll();
        assertThat(weatherData).hasSize(3);
        for (int i = 0; i < weatherData.size(); i++) {
            final Weather weather = weatherData.get(i);
            assertThat(weather.getId())
                    .isEqualTo(savedData.get(i).getId());
            assertThat(weather.getCity().getId())
                    .isEqualTo(savedData.get(i).getCity().getId());
            assertThat(weather.getCity().getName())
                    .isEqualTo(savedData.get(i).getCity().getName());
            assertThat(weather.getType().getId())
                    .isEqualTo(savedData.get(i).getType().getId());
            assertThat(weather.getType().getName())
                    .isEqualTo(savedData.get(i).getType().getName());
            assertThat(weather.getTemperature())
                    .isEqualTo(savedData.get(i).getTemperature());
            assertThat(weather.getDateTime())
                    .isEqualTo(savedData.get(i).getDateTime());
        }
    }

    @Test
    void create_withNonExistentCityAndDateTimeCombination_shouldCreateEntity() {
        // given
        final var newWeather = Weather.builder()
                .city(cityKazan)
                .type(typeClear)
                .temperature(0.46)
                .dateTime(YESTERDAY)
                .build();
        underTest.create(weather1);
        underTest.create(weather2);
        underTest.create(weather3);
        final var savedData = List.of(
                weather1,
                weather2,
                weather3,
                newWeather
        );
        assertThat(underTest.findAll()).hasSize(3);

        // when
        underTest.create(newWeather);

        // then
        final var weatherData = underTest.findAll();
        assertThat(weatherData).hasSize(4);

        for (int i = 0; i < weatherData.size(); i++) {
            final Weather weather = weatherData.get(i);
            assertThat(weather.getId())
                    .isNotNull();
            assertThat(weather.getCity().getId())
                    .isEqualTo(savedData.get(i).getCity().getId());
            assertThat(weather.getCity().getName())
                    .isEqualTo(savedData.get(i).getCity().getName());
            assertThat(weather.getType().getId())
                    .isEqualTo(savedData.get(i).getType().getId());
            assertThat(weather.getType().getName())
                    .isEqualTo(savedData.get(i).getType().getName());
            assertThat(weather.getTemperature())
                    .isEqualTo(savedData.get(i).getTemperature());
            assertThat(weather.getDateTime())
                    .isEqualTo(savedData.get(i).getDateTime());
        }
    }

    @Test
    void create_withExistentCityAndDateTimeCombination_shouldThrowException() {
        // given
        underTest.create(weather1);
        underTest.create(weather2);
        underTest.create(weather3);
        final var savedData = List.of(
                weather1,
                weather2,
                weather3
        );
        assertThat(underTest.findAll()).hasSize(3);

        // when
        // then
        assertThatThrownBy(() -> underTest.create(weather1))
                .isInstanceOf(DuplicateKeyException.class);

        final var weatherData = underTest.findAll();
        assertThat(weatherData).hasSize(3);

        for (int i = 0; i < weatherData.size(); i++) {
            final Weather weather = weatherData.get(i);
            assertThat(weather.getId())
                    .isNotNull();
            assertThat(weather.getCity().getId())
                    .isEqualTo(savedData.get(i).getCity().getId());
            assertThat(weather.getCity().getName())
                    .isEqualTo(savedData.get(i).getCity().getName());
            assertThat(weather.getType().getId())
                    .isEqualTo(savedData.get(i).getType().getId());
            assertThat(weather.getType().getName())
                    .isEqualTo(savedData.get(i).getType().getName());
            assertThat(weather.getTemperature())
                    .isEqualTo(savedData.get(i).getTemperature());
            assertThat(weather.getDateTime())
                    .isEqualTo(savedData.get(i).getDateTime());
        }
    }

    @Test
    void change_withNonExistentCityAndDateTimeCombination_shouldUpdateEntity() {
        // given
        final var newWeather = Weather.builder()
                .city(cityKazan)
                .type(typeBlizzard)
                .temperature(0.46)
                .dateTime(YESTERDAY)
                .build();
        final Weather savedWeather1 = underTest.create(weather1);
        underTest.create(weather2);
        underTest.create(weather3);
        assertThat(underTest.findAll()).hasSize(3);

        // when
        underTest.change(savedWeather1.getId(), newWeather);

        // then
        final var weatherData = underTest.findAll();
        assertThat(weatherData).hasSize(3);
        final Weather weather = weatherData.get(0);
        assertThat(weather).isNotNull();
        assertThat(weather.getId())
                .isEqualTo(savedWeather1.getId());
        assertThat(weather.getCity().getId())
                .isEqualTo(newWeather.getCity().getId());
        assertThat(weather.getCity().getName())
                .isEqualTo(newWeather.getCity().getName());
        assertThat(weather.getType().getId())
                .isEqualTo(newWeather.getType().getId());
        assertThat(weather.getType().getName())
                .isEqualTo(newWeather.getType().getName());
        assertThat(weather.getTemperature())
                .isEqualTo(newWeather.getTemperature());
        assertThat(weather.getDateTime())
                .isEqualTo(newWeather.getDateTime());
    }

    @Test
    void change_withExistentCityAndDateTimeCombination_shouldThrowException() {
        // given
        final Weather savedWeather1 = underTest.create(weather1);
        final Weather savedWeather2 = underTest.create(weather2);
        final Weather savedWeather3 = underTest.create(weather3);
        final UUID weather1Id = savedWeather1.getId();
        final var savedData = List.of(
                savedWeather1,
                savedWeather2,
                savedWeather3
        );
        assertThat(underTest.findAll()).hasSize(3);

        // when
        // then
        assertThatThrownBy(() -> underTest.change(weather1Id, weather2))
                .isInstanceOf(DuplicateKeyException.class);

        final var weatherData = underTest.findAll();
        assertThat(weatherData).hasSize(3);

        for (int i = 0; i < weatherData.size(); i++) {
            final Weather weather = weatherData.get(i);
            assertThat(weather.getId())
                    .isEqualTo(savedData.get(i).getId());
            assertThat(weather.getCity().getId())
                    .isEqualTo(savedData.get(i).getCity().getId());
            assertThat(weather.getCity().getName())
                    .isEqualTo(savedData.get(i).getCity().getName());
            assertThat(weather.getType().getId())
                    .isEqualTo(savedData.get(i).getType().getId());
            assertThat(weather.getType().getName())
                    .isEqualTo(savedData.get(i).getType().getName());
            assertThat(weather.getTemperature())
                    .isEqualTo(savedData.get(i).getTemperature());
            assertThat(weather.getDateTime())
                    .isEqualTo(savedData.get(i).getDateTime());
        }
    }

    @Test
    void findFirstByCityIdAndDateTime_withExistentCityIdAndDateTimeCombination_shouldReturnFoundEntity() {
        // given
        final Weather saved = underTest.create(weather1);
        underTest.create(weather2);
        underTest.create(weather3);
        assertThat(underTest.findAll()).hasSize(3);

        // when
        final var optWeather = underTest.findFirstByCityIdAndDateTime(
                cityKazan.getId(),
                NOW
        );

        // then
        assertThat(optWeather).isPresent();
        assertThat(optWeather.get().getId())
                .isEqualTo(saved.getId());
        assertThat(optWeather.get().getCity().getId())
                .isEqualTo(weather1.getCity().getId());
        assertThat(optWeather.get().getCity().getName())
                .isEqualTo(weather1.getCity().getName());
        assertThat(optWeather.get().getType().getId())
                .isEqualTo(weather1.getType().getId());
        assertThat(optWeather.get().getType().getName())
                .isEqualTo(weather1.getType().getName());
        assertThat(optWeather.get().getTemperature())
                .isEqualTo(weather1.getTemperature());
        assertThat(optWeather.get().getDateTime())
                .isEqualTo(weather1.getDateTime());
    }

    @Test
    void findFirstByCityIdAndDateTime_withNonExistentName_shouldReturnEmptyOptional() {
        // given
        underTest.create(weather1);
        underTest.create(weather2);
        underTest.create(weather3);
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
    void findFirstByCityIdAndDateTimeWithAnotherId_withExistentDataAndNonExistentId_shouldReturnFoundEntity() {
        // given
        final UUID anotherId = UUID.randomUUID();
        final Weather saved = underTest.create(weather1);
        underTest.create(weather2);
        underTest.create(weather3);
        assertThat(underTest.findAll()).hasSize(3);

        // when
        final var optWeather = underTest.findFirstByCityIdAndDateTimeWithAnotherId(
                cityKazan.getId(),
                NOW,
                anotherId
        );

        // then
        assertThat(optWeather).isPresent();
        assertThat(optWeather.get().getId())
                .isEqualTo(saved.getId());
        assertThat(optWeather.get().getCity().getId())
                .isEqualTo(weather1.getCity().getId());
        assertThat(optWeather.get().getCity().getName())
                .isEqualTo(weather1.getCity().getName());
        assertThat(optWeather.get().getType().getId())
                .isEqualTo(weather1.getType().getId());
        assertThat(optWeather.get().getType().getName())
                .isEqualTo(weather1.getType().getName());
        assertThat(optWeather.get().getTemperature())
                .isEqualTo(weather1.getTemperature());
        assertThat(optWeather.get().getDateTime())
                .isEqualTo(weather1.getDateTime());
    }

    @Test
    void findFirstByCityIdAndDateTimeWithAnotherId_withExistentDataAndExistentId_shouldReturnEmptyOptional() {
        // given
        final Weather saved = underTest.create(weather1);
        underTest.create(weather2);
        underTest.create(weather3);
        assertThat(underTest.findAll()).hasSize(3);

        // when
        final var optWeather = underTest.findFirstByCityIdAndDateTimeWithAnotherId(
                cityKazan.getId(),
                NOW,
                saved.getId()
        );

        // then
        assertThat(optWeather).isNotPresent();
    }

    @Test
    void findFirstByCityIdAndDateTimeWithAnotherId_withNonExistentDataAndNonExistentId_shouldReturnEmptyOptional() {
        // given
        final UUID anotherId = UUID.randomUUID();
        underTest.create(weather1);
        underTest.create(weather2);
        underTest.create(weather3);
        assertThat(underTest.findAll()).hasSize(3);

        // when
        final var optWeather = underTest.findFirstByCityIdAndDateTimeWithAnotherId(
                cityKazan.getId(),
                YESTERDAY,
                anotherId
        );

        // then
        assertThat(optWeather).isNotPresent();
    }

    @Test
    void findFirstByCityIdAndDateTimeWithAnotherId_withNonExistentDataAndExistentId_shouldReturnEmptyOptional() {
        // given
        final Weather saved = underTest.create(weather1);
        underTest.create(weather2);
        underTest.create(weather3);
        assertThat(underTest.findAll()).hasSize(3);

        // when
        final var optWeather = underTest.findFirstByCityIdAndDateTimeWithAnotherId(
                cityKazan.getId(),
                YESTERDAY,
                saved.getId()
        );

        // then
        assertThat(optWeather).isNotPresent();
    }
}