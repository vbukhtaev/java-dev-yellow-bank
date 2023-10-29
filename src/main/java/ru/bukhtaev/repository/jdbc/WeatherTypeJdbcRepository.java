package ru.bukhtaev.repository.jdbc;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.bukhtaev.model.WeatherType;

import java.util.*;

/**
 * JDBC-репозиторий типов погоды.
 */
@Repository
public class WeatherTypeJdbcRepository {

    /**
     * Маппер для объектов типа {@link WeatherType}.
     */
    private static final RowMapper<WeatherType> MAPPER = new BeanPropertyRowMapper<>(WeatherType.class);

    /**
     * Шаблон SQL-запроса для получения типа погоды по названию.
     */
    private static final String SELECT_BY_NAME = """
            SELECT *
            FROM weather_type
            WHERE name = :name
            """;

    /**
     * Шаблон SQL-запроса для получения типа погоды по названию и отличающемуся ID.
     */
    private static final String SELECT_BY_NAME_WITH_ANOTHER_ID = """
            SELECT *
            FROM weather_type
            WHERE id <> :id
            AND name = :name
            """;

    /**
     * Шаблон SQL-запроса для получения типа погоды по ID.
     */
    private static final String SELECT_BY_ID = """
            SELECT *
            FROM weather_type
            WHERE id = :id
            """;

    /**
     * Шаблон SQL-запроса для получения всех типов погоды.
     */
    private static final String SELECT_ALL = """
            SELECT *
            FROM weather_type
            """;

    /**
     * Шаблон SQL-запроса для удаления типа погоды.
     */
    private static final String DELETE = """
            DELETE FROM weather_type
            WHERE id = :id
            """;

    /**
     * Шаблон SQL-запроса для удаления всех типов погоды.
     */
    private static final String DELETE_ALL = """
            DELETE FROM weather_type
            WHERE TRUE
            """;

    /**
     * Шаблон SQL-запроса для добавления типа погоды.
     */
    private static final String INSERT = """
            INSERT INTO weather_type(id, name)
            VALUES (RANDOM_UUID(), :name)
            """;

    /**
     * Шаблон SQL-запроса для изменения типа погоды.
     */
    private static final String UPDATE = """
            UPDATE weather_type
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
    public WeatherTypeJdbcRepository(final NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Возвращает объект типа {@link Optional} с типом погоды с указанным ID, если он существует.
     * В противном случае возвращает пустой объект типа {@link Optional}.
     *
     * @param id ID
     * @return объект типа {@link Optional} с типом погоды с указанным ID, если он существует
     */
    public Optional<WeatherType> findById(final UUID id) {
        return jdbcTemplate.query(
                        SELECT_BY_ID,
                        Map.of(PARAM_ID, id),
                        MAPPER
                ).stream()
                .findAny();
    }

    /**
     * Возвращает все типы погоды.
     *
     * @return все типы погоды
     */
    public List<WeatherType> findAll() {
        return jdbcTemplate.query(SELECT_ALL, MAPPER);
    }

    /**
     * Удаляет тип погоды с указанным ID.
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
     * Удаляет все типы погоды.
     */
    public void deleteAll() {
        jdbcTemplate.update(
                DELETE_ALL,
                Collections.emptyMap()
        );
    }

    /**
     * Создает тип погоды.
     *
     * @param weatherType тип погоды
     * @return созданный тип погоды
     */
    public WeatherType create(final WeatherType weatherType) {
        jdbcTemplate.update(
                INSERT,
                Map.of(PARAM_NAME, weatherType.getName())
        );

        return jdbcTemplate.queryForObject(
                SELECT_BY_NAME,
                Map.of(PARAM_NAME, weatherType.getName()),
                MAPPER
        );
    }

    /**
     * Изменяет тип погоды с указанным ID.
     *
     * @param id          ID
     * @param weatherType тип погоды
     * @return измененный тип погоды
     */
    public WeatherType change(final UUID id, final WeatherType weatherType) {
        jdbcTemplate.update(
                UPDATE,
                Map.of(
                        PARAM_ID, id,
                        PARAM_NAME, weatherType.getName()
                )
        );

        return jdbcTemplate.queryForObject(
                SELECT_BY_NAME,
                Map.of(PARAM_NAME, weatherType.getName()),
                MAPPER
        );
    }

    /**
     * Возвращает объект типа {@link Optional} с типом погоды с указанным названием,
     * и ID, отличающимся от указанного, если такой тип погоды существует.
     * В противном случае возвращает пустой объект типа {@link Optional}.
     *
     * @param name название
     * @param id   ID
     * @return объект типа {@link Optional} с типом погоды с указанным названием,
     * и ID, отличающимся от указанного, если такой тип погоды существует
     */
    public Optional<WeatherType> findFirstByNameWithAnotherId(final String name, final UUID id) {
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
     * Возвращает объект типа {@link Optional} с типом погоды с указанным названием, если он существует.
     * В противном случае возвращает пустой объект типа {@link Optional}.
     *
     * @param name название
     * @return объект типа {@link Optional} с типом погоды с указанным названием, если он существует
     */
    public Optional<WeatherType> findFirstByName(final String name) {
        return jdbcTemplate.query(
                        SELECT_BY_NAME,
                        Map.of(PARAM_NAME, name),
                        MAPPER
                ).stream()
                .findAny();
    }
}
