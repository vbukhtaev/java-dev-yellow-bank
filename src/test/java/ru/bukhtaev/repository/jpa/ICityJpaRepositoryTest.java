package ru.bukhtaev.repository.jpa;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import ru.bukhtaev.AbstractContainerizedTest;
import ru.bukhtaev.model.City;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Тест JPA-репозитория городов.
 */
@DataJpaTest
class ICityJpaRepositoryTest extends AbstractContainerizedTest {

    /**
     * Тестируемый JPA-репозиторий городов.
     */
    @Autowired
    private ICityJpaRepository underTest;

    private City cityKazan;
    private City cityYekaterinburg;

    @BeforeEach
    void setUp() {
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
    void findFirstByName_withExistentName_shouldReturnFoundEntity() {
        // given
        final City saved = underTest.save(cityYekaterinburg);
        underTest.save(cityKazan);
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
        underTest.save(cityYekaterinburg);
        underTest.save(cityKazan);
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
        final City saved = underTest.save(cityKazan);
        underTest.save(cityYekaterinburg);
        assertThat(underTest.findAll()).hasSize(2);

        // when
        final var optCity = underTest.findFirstByNameAndIdNot(
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
        final City saved = underTest.save(cityKazan);
        underTest.save(cityYekaterinburg);
        assertThat(underTest.findAll()).hasSize(2);

        // when
        final var optCity = underTest.findFirstByNameAndIdNot(
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
        underTest.save(cityYekaterinburg);
        underTest.save(cityKazan);
        assertThat(underTest.findAll()).hasSize(2);

        // when
        final var optCity = underTest.findFirstByNameAndIdNot(
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
        final City saved = underTest.save(cityYekaterinburg);
        underTest.save(cityKazan);
        assertThat(underTest.findAll()).hasSize(2);

        // when
        final var optCity = underTest.findFirstByNameAndIdNot(
                anotherName,
                saved.getId()
        );

        // then
        assertThat(optCity).isNotPresent();
    }
}