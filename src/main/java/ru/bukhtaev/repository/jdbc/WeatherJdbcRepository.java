package ru.bukhtaev.repository.jdbc;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.bukhtaev.model.Weather;

import java.time.LocalDateTime;
import java.util.*;

/**
 * JDBC-репозиторий данных о погоде.
 */
@Repository
public class WeatherJdbcRepository {

    /**
     * Маппер для объектов типа {@link Weather}.
     */
    private static final RowMapper<Weather> MAPPER = new WeatherRowMapper();

    /**
     * Шаблон SQL-запроса для получения записи о погоде по ID города, дате и времени.
     */
    private static final String SELECT_BY_CITY_ID_AND_DATE_TIME = """
            SELECT w.id AS id,
                   w.temperature AS temperature,
                   w.date_time AS date_time,
                   c.id AS city_id,
                   c.name AS city_name,
                   t.id AS type_id,
                   t.name AS type_name
            FROM weather AS w
                     JOIN city AS c
                          ON w.city_id = c.id
                     JOIN weather_type AS t
                          ON w.weather_type_id = t.id
            WHERE c.id = :cityId
            AND w.date_time = :dateTime
            """;

    /**
     * Шаблон SQL-запроса для получения записи о погоде по ID города, дате, времени и отличающемуся ID.
     */
    private static final String SELECT_BY_CITY_ID_AND_DATE_TIME_WITH_ANOTHER_ID = """
            SELECT w.id AS id,
                   w.temperature AS temperature,
                   w.date_time AS date_time,
                   c.id AS city_id,
                   c.name AS city_name,
                   t.id AS type_id,
                   t.name AS type_name
            FROM weather AS w
                     JOIN city AS c
                          ON w.city_id = c.id
                     JOIN weather_type AS t
                          ON w.weather_type_id = t.id
            WHERE w.id <> :id
            AND c.id = :cityId
            AND w.date_time = :dateTime
            """;

    /**
     * Шаблон SQL-запроса для получения записи о погоде по ID.
     */
    private static final String SELECT_BY_ID = """
            SELECT w.id AS id,
                   w.temperature AS temperature,
                   w.date_time AS date_time,
                   c.id AS city_id,
                   c.name AS city_name,
                   t.id AS type_id,
                   t.name AS type_name
            FROM weather AS w
                     JOIN city AS c
                          ON w.city_id = c.id
                     JOIN weather_type AS t
                          ON w.weather_type_id = t.id
            WHERE w.id = :id
            """;

    /**
     * Шаблон SQL-запроса для получения всех данных о погоде.
     */
    private static final String SELECT_ALL = """
            SELECT w.id AS id,
                   w.temperature AS temperature,
                   w.date_time AS date_time,
                   c.id AS city_id,
                   c.name AS city_name,
                   t.id AS type_id,
                   t.name AS type_name
            FROM weather AS w
                     JOIN city AS c
                          ON w.city_id = c.id
                     JOIN weather_type AS t
                          ON w.weather_type_id = t.id
            """;

    /**
     * Шаблон SQL-запроса для получения всех данных о погоде по названию города.
     */
    private static final String SELECT_ALL_BY_CITY_NAME = """
            SELECT w.id AS id,
                   w.temperature AS temperature,
                   w.date_time AS date_time,
                   c.id AS city_id,
                   c.name AS city_name,
                   t.id AS type_id,
                   t.name AS type_name
            FROM weather AS w
                     JOIN city AS c
                          ON w.city_id = c.id
                     JOIN weather_type AS t
                          ON w.weather_type_id = t.id
            WHERE c.name = :cityName
            """;

    /**
     * Шаблон SQL-запроса для удаления записи о погоде.
     */
    private static final String DELETE_BY_ID = """
            DELETE FROM weather
            WHERE id = :id
            """;

    /**
     * Шаблон SQL-запроса для удаления всех данных о погоде.
     */
    private static final String DELETE_ALL = """
            DELETE FROM weather
            WHERE TRUE
            """;

    /**
     * Шаблон SQL-запроса для удаления всех данных о погоде по названию города.
     */
    private static final String DELETE_BY_CITY_NAME = """
            DELETE FROM weather
            WHERE city_id IN
            (SELECT id FROM city WHERE name = :cityName)
            """;

    /**
     * Шаблон SQL-запроса для добавления записи о погоде.
     */
    private static final String INSERT = """
            INSERT INTO weather(id, city_id, weather_type_id, temperature, date_time)
            VALUES (RANDOM_UUID(), :cityId, :weatherTypeId, :temperature, :dateTime)
            """;

    /**
     * Шаблон SQL-запроса для изменения записи о погоде.
     */
    private static final String UPDATE = """
            UPDATE weather
            SET city_id = :cityId,
                weather_type_id = :weatherTypeId,
                temperature = :temperature,
                date_time = :dateTime
            WHERE id = :id
            """;

    /**
     * Название параметра для передачи ID города.
     */
    private static final String PARAM_CITY_ID = "cityId";

    /**
     * Название параметра для передачи ID типа погоды.
     */
    private static final String PARAM_WEATHER_TYPE_ID = "weatherTypeId";

    /**
     * Название параметра для передачи температуры.
     */
    private static final String PARAM_TEMPERATURE = "temperature";

    /**
     * Название параметра для передачи даты и времени.
     */
    private static final String PARAM_DATE_TIME = "dateTime";

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
    public WeatherJdbcRepository(final NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Возвращает объект типа {@link Optional} с записью о погоде с указанным ID, если она существует.
     * В противном случае возвращает пустой объект типа {@link Optional}.
     *
     * @param id ID
     * @return объект типа {@link Optional} с записью о погоде с указанным ID, если она существует
     */
    public Optional<Weather> findById(final UUID id) {
        return jdbcTemplate.query(
                        SELECT_BY_ID,
                        Map.of(PARAM_ID, id),
                        MAPPER
                ).stream()
                .findAny();
    }

    /**
     * Возвращает все данные о погоде.
     *
     * @return все данные о погоде
     */
    public List<Weather> findAll() {
        return jdbcTemplate.query(SELECT_ALL, MAPPER);
    }

    /**
     * Возвращает все данные о погоде в городе с указанным названием.
     *
     * @param cityName название города
     * @return все данные о погоде в городе с указанным названием
     */
    public List<Weather> findAllByCityName(final String cityName) {
        return jdbcTemplate.query(
                SELECT_ALL_BY_CITY_NAME,
                Map.of("cityName", cityName),
                MAPPER
        );
    }


    /**
     * Удаляет запись о погоде с указанным ID.
     *
     * @param id ID
     */
    public void deleteById(final UUID id) {
        jdbcTemplate.update(
                DELETE_BY_ID,
                Map.of(PARAM_ID, id)
        );
    }

    /**
     * Удаляет все данные о погоде.
     */
    public void deleteAll() {
        jdbcTemplate.update(
                DELETE_ALL,
                Collections.emptyMap()
        );
    }

    /**
     * Удаляет все данные о погоде в городе с указанным названием.
     *
     * @param cityName название города
     */
    public void deleteAllByCityName(final String cityName) {
        jdbcTemplate.update(
                DELETE_BY_CITY_NAME,
                Map.of("cityName", cityName)
        );
    }

    /**
     * Создает запись о погоде.
     *
     * @param weather запись о погоде
     * @return созданную запись о погоде
     */
    public Weather create(final Weather weather) {
        jdbcTemplate.update(
                INSERT,
                Map.of(
                        PARAM_CITY_ID, weather.getCity().getId(),
                        PARAM_WEATHER_TYPE_ID, weather.getType().getId(),
                        PARAM_TEMPERATURE, weather.getTemperature(),
                        PARAM_DATE_TIME, weather.getDateTime()
                )
        );

        return jdbcTemplate.queryForObject(
                SELECT_BY_CITY_ID_AND_DATE_TIME,
                Map.of(
                        PARAM_CITY_ID, weather.getCity().getId(),
                        PARAM_DATE_TIME, weather.getDateTime()
                ),
                MAPPER
        );
    }

    /**
     * Изменяет запись о погоде.
     *
     * @param id      ID
     * @param weather запись о погоде
     * @return измененную запись о погоде
     */
    public Weather change(final UUID id, final Weather weather) {
        jdbcTemplate.update(
                UPDATE,
                Map.of(
                        PARAM_ID, id,
                        PARAM_CITY_ID, weather.getCity().getId(),
                        PARAM_WEATHER_TYPE_ID, weather.getType().getId(),
                        PARAM_TEMPERATURE, weather.getTemperature(),
                        PARAM_DATE_TIME, weather.getDateTime()
                )
        );

        return jdbcTemplate.queryForObject(
                SELECT_BY_CITY_ID_AND_DATE_TIME,
                Map.of(
                        PARAM_CITY_ID, weather.getCity().getId(),
                        PARAM_DATE_TIME, weather.getDateTime()
                ),
                MAPPER
        );
    }

    /**
     * Возвращает объект типа {@link Optional} с записью о погоде с указанным ID города, датой, временем,
     * и ID, отличающимся от указанного, если такая запись о погоде существует.
     * В противном случае возвращает пустой объект типа {@link Optional}.
     *
     * @param cityId   ID города
     * @param dateTime дата и время
     * @param id       ID
     * @return объект типа {@link Optional} с записью о погоде с указанным ID города, датой, временем,
     * и ID, отличающимся от указанного, если такая запись о погоде существует
     */
    public Optional<Weather> findFirstByCityIdAndDateTimeWithAnotherId(
            final UUID cityId,
            final LocalDateTime dateTime,
            final UUID id
    ) {
        return jdbcTemplate.query(
                        SELECT_BY_CITY_ID_AND_DATE_TIME_WITH_ANOTHER_ID,
                        Map.of(
                                PARAM_ID, id,
                                PARAM_CITY_ID, cityId,
                                PARAM_DATE_TIME, dateTime
                        ),
                        MAPPER
                ).stream()
                .findAny();
    }

    /**
     * Возвращает объект типа {@link Optional} с записью о погоде с указанным ID города, датой и временем,
     * если такая запись о погоде существует.
     * В противном случае возвращает пустой объект типа {@link Optional}.
     *
     * @param cityId   ID города
     * @param dateTime дата и время
     * @return объект типа {@link Optional} с записью о погоде с указанным ID города, датой и временем,
     * если такая запись о погоде существует
     */
    public Optional<Weather> findFirstByCityIdAndDateTime(
            final UUID cityId,
            final LocalDateTime dateTime
    ) {
        return jdbcTemplate.query(
                        SELECT_BY_CITY_ID_AND_DATE_TIME,
                        Map.of(
                                PARAM_CITY_ID, cityId,
                                PARAM_DATE_TIME, dateTime
                        ),
                        MAPPER
                ).stream()
                .findAny();
    }
}
