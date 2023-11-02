package ru.bukhtaev.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.client.RestTemplate;
import ru.bukhtaev.config.external.ExternalApiConfigParams;
import ru.bukhtaev.dto.mapper.IWeatherMapper;
import ru.bukhtaev.model.City;
import ru.bukhtaev.model.Weather;
import ru.bukhtaev.model.WeatherType;
import ru.bukhtaev.service.crud.ICityCrudService;
import ru.bukhtaev.service.crud.IWeatherCrudService;
import ru.bukhtaev.service.crud.IWeatherTypeCrudService;

import java.util.Optional;

import static org.springframework.transaction.TransactionDefinition.ISOLATION_SERIALIZABLE;

/**
 * JDBC-реализация сервиса для выполнения запросов к внешнему API данных о погоде.
 */
@Slf4j
@Service("weatherApiServiceJdbc")
public class ExternalWeatherApiServiceJdbcImpl extends AbstractExternalWeatherApiService {

    /**
     * JDBC-реализация сервиса CRUD операций над данными о погоде.
     */
    private final IWeatherCrudService jdbcWeatherCrudService;

    /**
     * JDBC-реализация сервиса CRUD операций над городами.
     */
    private final ICityCrudService jdbcCityCrudService;

    /**
     * JDBC-реализация сервиса CRUD операций над типами погоды.
     */
    private final IWeatherTypeCrudService jdbcTypeCrudService;

    /**
     * Объект для управления транзакциями.
     */
    private final TransactionTemplate transactionTemplate;

    /**
     * @param restTemplate           клиент
     * @param jdbcWeatherCrudService JDBC-реализация сервиса CRUD операций над данными о погоде
     * @param jdbcCityCrudService    JDBC-реализация сервиса CRUD операций над городами
     * @param jdbcTypeCrudService    JDBC-реализация сервиса CRUD операций над типами погоды
     * @param transactionTemplate    объект для управления транзакциями
     * @param apiConfig              параметры конфигурации внешнего API
     * @param objectMapper           маппер объектов
     * @param dtoMapper              маппер для объектов типа {@link Weather}
     */
    @Autowired
    public ExternalWeatherApiServiceJdbcImpl(
            final ExternalApiConfigParams apiConfig,
            final RestTemplate restTemplate,
            final ObjectMapper objectMapper,
            final IWeatherMapper dtoMapper,
            final TransactionTemplate transactionTemplate,
            @Qualifier("weatherCrudServiceJdbc") final IWeatherCrudService jdbcWeatherCrudService,
            @Qualifier("cityCrudServiceJdbc") final ICityCrudService jdbcCityCrudService,
            @Qualifier("typeCrudServiceJdbc") final IWeatherTypeCrudService jdbcTypeCrudService
    ) {
        super(
                apiConfig,
                restTemplate,
                objectMapper,
                dtoMapper
        );
        this.jdbcWeatherCrudService = jdbcWeatherCrudService;
        this.jdbcCityCrudService = jdbcCityCrudService;
        this.jdbcTypeCrudService = jdbcTypeCrudService;
        this.transactionTemplate = transactionTemplate;
    }

    @Override
    public Weather saveWithTransaction(final Weather weather) {
        transactionTemplate.setReadOnly(false);
        transactionTemplate.setIsolationLevel(ISOLATION_SERIALIZABLE);
        return transactionTemplate.execute(status -> {
            final String cityName = weather.getCity().getName();
            final Optional<City> optCity = jdbcCityCrudService.getByName(cityName);
            final City city = optCity.orElseGet(() -> {
                log.info(
                        "City with name <{}> was not found! Creating a new one...",
                        cityName
                );
                return jdbcCityCrudService.create(
                        City.builder()
                                .name(cityName)
                                .build()
                );
            });
            weather.setCity(city);

            final String typeName = weather.getType().getName();
            final Optional<WeatherType> optType = jdbcTypeCrudService.getByName(typeName);
            final WeatherType type = optType.orElseGet(() -> {
                log.info(
                        "Weather type with name <{}> was not found! Creating a new one...",
                        typeName
                );
                return jdbcTypeCrudService.create(
                        WeatherType.builder()
                                .name(typeName)
                                .build()
                );
            });
            weather.setType(type);

            return jdbcWeatherCrudService.create(weather);
        });
    }
}
