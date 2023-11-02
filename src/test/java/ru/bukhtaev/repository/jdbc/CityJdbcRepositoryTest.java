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

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Модульные тесты для JDBC-репозитория городов {@link CityJdbcRepository}.
 */
@JdbcTest
class CityJdbcRepositoryTest extends AbstractContainerizedTest {

    /**
     * Объект для выполнения SQL-запросов
     * с использованием именованных параметров.
     */
    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    /**
     * Тестируемый JDBC-репозиторий городов.
     */
    private CityJdbcRepository underTest;

    private City cityKazan;
    private City cityYekaterinburg;

    @BeforeEach
    void setUp() {
        underTest = new CityJdbcRepository(jdbcTemplate);

        cityKazan = City.builder()
                .name("Казань")
                .build();
        cityYekaterinburg = City.builder()
                .name("Екатеринбург")
                .build();
    }

    @AfterEach
    void tearDown() {
        underTest.deleteAll();
    }

    @Test
    void findById_withExistentId_shouldReturnFoundEntity() {
        // given
        final City saved = underTest.create(cityYekaterinburg);
        underTest.create(cityKazan);
        assertThat(underTest.findAll()).hasSize(2);

        // when
        final var optCity = underTest.findById(saved.getId());

        // then
        assertThat(optCity).isPresent();
        assertThat(optCity.get().getId())
                .isEqualTo(saved.getId());
        assertThat(optCity.get().getName())
                .isEqualTo(cityYekaterinburg.getName());
    }

    @Test
    void findById_withNonExistentId_shouldReturnEmptyOptional() {
        // given
        final UUID anotherId = UUID.randomUUID();
        underTest.create(cityYekaterinburg);
        underTest.create(cityKazan);
        assertThat(underTest.findAll()).hasSize(2);

        // when
        final var optCity = underTest.findById(anotherId);

        // then
        assertThat(optCity).isNotPresent();
    }

    @Test
    void findAll_withExistentData_shouldReturnAllEntities() {
        // given
        final City savedYekaterinburg = underTest.create(cityYekaterinburg);
        final City savedKazan = underTest.create(cityKazan);
        assertThat(underTest.findAll()).hasSize(2);

        // when
        final var cities = underTest.findAll();

        // then
        assertThat(cities).hasSize(2);
        final City yekaterinburg = cities.get(0);
        assertThat(yekaterinburg).isNotNull();
        assertThat(yekaterinburg.getId())
                .isEqualTo(savedYekaterinburg.getId());
        assertThat(yekaterinburg.getName())
                .isEqualTo(cityYekaterinburg.getName());
        final City kazan = cities.get(1);
        assertThat(kazan).isNotNull();
        assertThat(kazan.getId())
                .isEqualTo(savedKazan.getId());
        assertThat(kazan.getName())
                .isEqualTo(cityKazan.getName());
    }

    @Test
    void findAll_withNonExistentData_shouldReturnEmptyList() {
        // given
        underTest.deleteAll();
        assertThat(underTest.findAll()).isEmpty();

        // when
        final var cities = underTest.findAll();

        // then
        assertThat(cities).isEmpty();
    }

    @Test
    void deleteById_withExistentId_shouldDeleteMatchingEntity() {
        // given
        final City saved = underTest.create(cityYekaterinburg);
        underTest.create(cityKazan);
        assertThat(underTest.findAll()).hasSize(2);

        // when
        underTest.deleteById(saved.getId());

        // then
        final var cities = underTest.findAll();
        assertThat(cities).hasSize(1);
        final City city = cities.get(0);
        assertThat(city).isNotNull();
        assertThat(city.getId())
                .isNotEqualTo(saved.getId());
        assertThat(city.getName())
                .isEqualTo(cityKazan.getName());
    }

    @Test
    void deleteById_withNonExistentId_shouldNotDeleteAnything() {
        // given
        final UUID anotherId = UUID.randomUUID();
        underTest.create(cityYekaterinburg);
        underTest.create(cityKazan);
        assertThat(underTest.findAll()).hasSize(2);

        // when
        underTest.deleteById(anotherId);

        // then
        assertThat(underTest.findAll()).hasSize(2);
    }

    @Test
    void deleteAll_shouldDeleteAllEntities() {
        // given
        underTest.create(cityYekaterinburg);
        underTest.create(cityKazan);
        assertThat(underTest.findAll()).hasSize(2);

        // when
        underTest.deleteAll();

        // then
        assertThat(underTest.findAll()).isEmpty();
    }

    @Test
    void create_withNonExistentName_shouldCreateEntity() {
        // given
        final String anotherName = "Новосибирск";
        final var newCity = City.builder()
                .name(anotherName)
                .build();
        underTest.create(cityYekaterinburg);
        underTest.create(cityKazan);
        assertThat(underTest.findAll()).hasSize(2);

        // when
        underTest.create(newCity);

        // then
        final var cities = underTest.findAll();
        assertThat(cities).hasSize(3);
        final City city = cities.get(2);
        assertThat(city).isNotNull();
        assertThat(city.getId())
                .isNotNull();
        assertThat(city.getName())
                .isEqualTo(anotherName);
    }

    @Test
    void create_withExistentName_shouldThrowException() {
        // given
        final City savedYekaterinburg = underTest.create(cityYekaterinburg);
        final City savedKazan = underTest.create(cityKazan);
        assertThat(underTest.findAll()).hasSize(2);

        // when
        // then
        assertThatThrownBy(() -> underTest.create(cityKazan))
                .isInstanceOf(DuplicateKeyException.class);

        final var cities = underTest.findAll();
        assertThat(cities).hasSize(2);
        final City yekaterinburg = cities.get(0);
        assertThat(yekaterinburg).isNotNull();
        assertThat(yekaterinburg.getId())
                .isEqualTo(savedYekaterinburg.getId());
        assertThat(yekaterinburg.getName())
                .isEqualTo(cityYekaterinburg.getName());
        final City kazan = cities.get(1);
        assertThat(kazan).isNotNull();
        assertThat(kazan.getId())
                .isEqualTo(savedKazan.getId());
        assertThat(kazan.getName())
                .isEqualTo(cityKazan.getName());
    }

    @Test
    void change_withNonExistentName_shouldUpdateEntity() {
        // given
        final String anotherName = "Новосибирск";
        final var newCity = City.builder()
                .name(anotherName)
                .build();
        final City saved = underTest.create(cityYekaterinburg);
        underTest.create(cityKazan);
        assertThat(underTest.findAll()).hasSize(2);

        // when
        underTest.change(saved.getId(), newCity);

        // then
        final var cities = underTest.findAll();
        assertThat(cities).hasSize(2);
        final City city = cities.get(0);
        assertThat(city).isNotNull();
        assertThat(city.getId())
                .isEqualTo(saved.getId());
        assertThat(city.getName())
                .isEqualTo(anotherName);
    }

    @Test
    void change_withExistentName_shouldThrowException() {
        // given
        final City savedYekaterinburg = underTest.create(cityYekaterinburg);
        final City savedKazan = underTest.create(cityKazan);
        final UUID yekaterinburgId = savedYekaterinburg.getId();
        assertThat(underTest.findAll()).hasSize(2);

        // when
        // then
        assertThatThrownBy(() -> underTest.change(yekaterinburgId, cityKazan))
                .isInstanceOf(DuplicateKeyException.class);

        final var cities = underTest.findAll();
        assertThat(cities).hasSize(2);
        final City yekaterinburg = cities.get(0);
        assertThat(yekaterinburg).isNotNull();
        assertThat(yekaterinburg.getId())
                .isEqualTo(savedYekaterinburg.getId());
        assertThat(yekaterinburg.getName())
                .isEqualTo(cityYekaterinburg.getName());
        final City kazan = cities.get(1);
        assertThat(kazan).isNotNull();
        assertThat(kazan.getId())
                .isEqualTo(savedKazan.getId());
        assertThat(kazan.getName())
                .isEqualTo(cityKazan.getName());
    }

    @Test
    void findFirstByName_withExistentName_shouldReturnFoundEntity() {
        // given
        final City saved = underTest.create(cityYekaterinburg);
        underTest.create(cityKazan);
        assertThat(underTest.findAll()).hasSize(2);

        // when
        final var optCity = underTest.findFirstByName(cityYekaterinburg.getName());

        // then
        assertThat(optCity).isPresent();
        assertThat(optCity.get().getId())
                .isEqualTo(saved.getId());
        assertThat(optCity.get().getName())
                .isEqualTo(cityYekaterinburg.getName());
    }

    @Test
    void findFirstByName_withNonExistentName_shouldReturnEmptyOptional() {
        // given
        final String anotherName = "Новосибирск";
        underTest.create(cityYekaterinburg);
        underTest.create(cityKazan);
        assertThat(underTest.findAll()).hasSize(2);

        // when
        final var optCity = underTest.findFirstByName(anotherName);

        // then
        assertThat(optCity).isNotPresent();
    }

    @Test
    void findFirstByNameAndIdNot_withExistentNameAndNonExistentId_shouldReturnFoundEntity() {
        // given
        final UUID anotherId = UUID.randomUUID();
        final City saved = underTest.create(cityKazan);
        underTest.create(cityYekaterinburg);
        assertThat(underTest.findAll()).hasSize(2);

        // when
        final var optCity = underTest.findFirstByNameWithAnotherId(
                cityKazan.getName(),
                anotherId
        );

        // then
        assertThat(optCity).isPresent();
        assertThat(optCity.get().getId())
                .isEqualTo(saved.getId());
        assertThat(optCity.get().getName())
                .isEqualTo(cityKazan.getName());
    }

    @Test
    void findFirstByNameAndIdNot_withExistentNameAndExistentId_shouldReturnEmptyOptional() {
        // given
        final City saved = underTest.create(cityKazan);
        underTest.create(cityYekaterinburg);
        assertThat(underTest.findAll()).hasSize(2);

        // when
        final var optCity = underTest.findFirstByNameWithAnotherId(
                cityKazan.getName(),
                saved.getId()
        );

        // then
        assertThat(optCity).isNotPresent();
    }

    @Test
    void findFirstByNameAndIdNot_withNonExistentNameAndNonExistentId_shouldReturnEmptyOptional() {
        // given
        final UUID anotherId = UUID.randomUUID();
        final String anotherName = "Новосибирск";
        underTest.create(cityYekaterinburg);
        underTest.create(cityKazan);
        assertThat(underTest.findAll()).hasSize(2);

        // when
        final var optCity = underTest.findFirstByNameWithAnotherId(
                anotherName,
                anotherId
        );

        // then
        assertThat(optCity).isNotPresent();
    }

    @Test
    void findFirstByNameAndIdNot_withNonExistentNameAndExistentId_shouldReturnEmptyOptional() {
        // given
        final String anotherName = "Новосибирск";
        final City saved = underTest.create(cityYekaterinburg);
        underTest.create(cityKazan);
        assertThat(underTest.findAll()).hasSize(2);

        // when
        final var optCity = underTest.findFirstByNameWithAnotherId(
                anotherName,
                saved.getId()
        );

        // then
        assertThat(optCity).isNotPresent();
    }
}