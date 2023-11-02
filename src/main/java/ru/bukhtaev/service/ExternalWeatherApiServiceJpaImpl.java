package ru.bukhtaev.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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

import static org.springframework.transaction.annotation.Isolation.SERIALIZABLE;

/**
 * JPA-реализация сервиса для выполнения запросов к внешнему API данных о погоде.
 */
@Slf4j
@Service("weatherApiServiceJpa")
public class ExternalWeatherApiServiceJpaImpl extends AbstractExternalWeatherApiService {

    /**
     * JPA-реализация сервиса CRUD операций над данными о погоде.
     */
    private final IWeatherCrudService jpaWeatherCrudService;

    /**
     * JPA-реализация сервиса CRUD операций над городами.
     */
    private final ICityCrudService jpaCityCrudService;

    /**
     * JPA-реализация сервиса CRUD операций над типами погоды.
     */
    private final IWeatherTypeCrudService jpaTypeCrudService;

    /**
     * Конструктор.
     *
     * @param restTemplate          клиент
     * @param jpaWeatherCrudService JPA-реализация сервиса CRUD операций над данными о погоде
     * @param jpaCityCrudService    JPA-реализация сервиса CRUD операций над городами
     * @param jpaTypeCrudService    JPA-реализация сервиса CRUD операций над типами погоды
     * @param apiConfig             параметры конфигурации внешнего API
     * @param objectMapper          маппер объектов
     * @param dtoMapper             маппер для объектов типа {@link Weather}
     */
    @Autowired
    public ExternalWeatherApiServiceJpaImpl(
            ExternalApiConfigParams apiConfig,
            RestTemplate restTemplate,
            ObjectMapper objectMapper,
            IWeatherMapper dtoMapper,
            @Qualifier("weatherCrudServiceJpa") IWeatherCrudService jpaWeatherCrudService,
            @Qualifier("cityCrudServiceJpa") ICityCrudService jpaCityCrudService,
            @Qualifier("typeCrudServiceJpa") IWeatherTypeCrudService jpaTypeCrudService
    ) {
        super(
                apiConfig,
                restTemplate,
                objectMapper,
                dtoMapper
        );
        this.jpaWeatherCrudService = jpaWeatherCrudService;
        this.jpaCityCrudService = jpaCityCrudService;
        this.jpaTypeCrudService = jpaTypeCrudService;
    }

    @Override
    @Transactional(isolation = SERIALIZABLE)
    public Weather saveWithTransaction(final Weather weather) {
        final String cityName = weather.getCity().getName();
        final Optional<City> optCity = jpaCityCrudService.getByName(cityName);
        final City city = optCity.orElseGet(() -> {
            log.info(
                    "City with name <{}> was not found! Creating a new one...",
                    cityName
            );
            return jpaCityCrudService.create(
                    City.builder()
                            .name(cityName)
                            .build()
            );
        });
        weather.setCity(city);

        final String typeName = weather.getType().getName();
        final Optional<WeatherType> optType = jpaTypeCrudService.getByName(typeName);
        final WeatherType type = optType.orElseGet(() -> {
            log.info(
                    "Weather type with name <{}> was not found! Creating a new one...",
                    typeName
            );
            return jpaTypeCrudService.create(
                    WeatherType.builder()
                            .name(typeName)
                            .build()
            );
        });
        weather.setType(type);

        return jpaWeatherCrudService.create(weather);
    }
}
