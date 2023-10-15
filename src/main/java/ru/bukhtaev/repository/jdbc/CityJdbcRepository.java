package ru.bukhtaev.repository.jdbc;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.bukhtaev.model.City;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * JDBC-репозиторий городов.
 */
@Repository
public class CityJdbcRepository {

    /**
     * Маппер для объектов типа {@link City}.
     */
    private static final RowMapper<City> MAPPER = new BeanPropertyRowMapper<>(City.class);

    /**
     * Шаблон SQL-запроса для получения города по названию.
     */
    private static final String SELECT_BY_NAME = """
            SELECT *
            FROM city
            WHERE name = :name
            """;

    /**
     * Шаблон SQL-запроса для получения города по названию и отличающемуся ID.
     */
    private static final String SELECT_BY_NAME_WITH_ANOTHER_ID = """
            SELECT *
            FROM city
            WHERE id <> :id
            AND name = :name
            """;

    /**
     * Шаблон SQL-запроса для получения города по ID.
     */
    private static final String SELECT_BY_ID = """
            SELECT *
            FROM city
            WHERE id = :id
            """;

    /**
     * Шаблон SQL-запроса для получения всех городов.
     */
    private static final String SELECT_ALL = """
            SELECT *
            FROM city
            """;

    /**
     * Шаблон SQL-запроса для удаления города.
     */
    private static final String DELETE = """
            DELETE FROM city
            WHERE id = :id
            """;

    /**
     * Шаблон SQL-запроса для добавления города.
     */
    private static final String INSERT = """
            INSERT INTO city(id, name)
            VALUES (RANDOM_UUID(), :name)
            """;

    /**
     * Шаблон SQL-запроса для изменения города.
     */
    private static final String UPDATE = """
            UPDATE city
            SET name = :name
            WHERE id = :id
            """;

    /**
     * Название параметра для передачи названия.
     */
    public static final String PARAM_NAME = "name";

    /**
     * Название параметра для передачи ID.
     */
    private static final String PARAM_ID = "id";

    /**
     * Объект для выполнения SQL-запросов с использованием именованных параметров.
     */
    private final NamedParameterJdbcTemplate jdbcTemplate;

    /**
     * Конструктор.
     *
     * @param jdbcTemplate объект для выполнения SQL-запросов
     */
    @Autowired
    public CityJdbcRepository(final NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Возвращает объект типа {@link Optional} с городом с указанным ID, если он существует.
     * В противном случае возвращает пустой объект типа {@link Optional}.
     *
     * @param id ID
     * @return объект типа {@link Optional} с городом с указанным ID, если он существует
     */
    public Optional<City> findById(final UUID id) {
        return jdbcTemplate.query(
                        SELECT_BY_ID,
                        Map.of(PARAM_ID, id),
                        MAPPER
                ).stream()
                .findAny();
    }

    /**
     * Возвращает все города.
     *
     * @return все города
     */
    public List<City> findAll() {
        return jdbcTemplate.query(SELECT_ALL, MAPPER);
    }

    /**
     * Удаляет город с указанным ID.
     *
     * @param id ID
     */
    public void deleteById(final UUID id) {
        jdbcTemplate.update(
                DELETE,
                Map.of(PARAM_ID, id)
        );
    }

    /**
     * Создает город.
     *
     * @param city город
     * @return созданный город
     */
    public City create(final City city) {
        jdbcTemplate.update(
                INSERT,
                Map.of(PARAM_NAME, city.getName())
        );

        return jdbcTemplate.queryForObject(
                SELECT_BY_NAME,
                Map.of(PARAM_NAME, city.getName()),
                MAPPER
        );
    }

    /**
     * Изменяет город с указанным ID.
     *
     * @param id   ID
     * @param city город
     * @return измененный город
     */
    public City change(final UUID id, final City city) {
        jdbcTemplate.update(
                UPDATE,
                Map.of(
                        PARAM_ID, id,
                        PARAM_NAME, city.getName()
                )
        );

        return jdbcTemplate.queryForObject(
                SELECT_BY_NAME,
                Map.of(PARAM_NAME, city.getName()),
                MAPPER
        );
    }

    /**
     * Возвращает объект типа {@link Optional} с городом с указанным названием,
     * и ID, отличающимся от указанного, если такой город существует.
     * В противном случае возвращает пустой объект типа {@link Optional}.
     *
     * @param name название
     * @param id   ID
     * @return объект типа {@link Optional} с городом с указанным названием,
     * и ID, отличающимся от указанного, если такой город существует
     */
    public Optional<City> findFirstByNameWithAnotherId(final String name, final UUID id) {
        return jdbcTemplate.query(
                        SELECT_BY_NAME_WITH_ANOTHER_ID,
                        Map.of(
                                PARAM_ID, id,
                                PARAM_NAME, name
                        ),
                        MAPPER
                ).stream()
                .findAny();
    }

    /**
     * Возвращает объект типа {@link Optional} с городом с указанным названием, если он существует.
     * В противном случае возвращает пустой объект типа {@link Optional}.
     *
     * @param name название
     * @return объект типа {@link Optional} с городом с указанным названием, если он существует
     */
    public Optional<City> findFirstByName(final String name) {
        return jdbcTemplate.query(
                        SELECT_BY_NAME,
                        Map.of(PARAM_NAME, name),
                        MAPPER
                ).stream()
                .findAny();
    }
}
