package ru.bukhtaev.service.crud;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import ru.bukhtaev.exception.DataNotFoundException;
import ru.bukhtaev.exception.InvalidPropertyException;
import ru.bukhtaev.exception.UniqueWeatherException;
import ru.bukhtaev.model.City;
import ru.bukhtaev.model.Weather;
import ru.bukhtaev.model.WeatherType;
import ru.bukhtaev.repository.jdbc.CityJdbcRepository;
import ru.bukhtaev.repository.jdbc.WeatherJdbcRepository;
import ru.bukhtaev.repository.jdbc.WeatherTypeJdbcRepository;
import ru.bukhtaev.validation.MessageProvider;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.springframework.transaction.TransactionDefinition.ISOLATION_READ_COMMITTED;
import static org.springframework.transaction.TransactionDefinition.ISOLATION_SERIALIZABLE;
import static ru.bukhtaev.model.BaseEntity.FIELD_ID;
import static ru.bukhtaev.model.Weather.*;
import static ru.bukhtaev.util.Utils.DATE_TIME_FORMATTER;
import static ru.bukhtaev.validation.MessageUtils.*;

/**
 * JDBC-реализация сервиса CRUD операций над данными о погоде.
 */
@Service("weatherCrudServiceJdbc")
public class WeatherCrudServiceJdbcImpl implements IWeatherCrudService {

    /**
     * Репозиторий городов.
     */
    private final CityJdbcRepository cityRepository;

    /**
     * Репозиторий типов погоды.
     */
    private final WeatherTypeJdbcRepository weatherTypeRepository;

    /**
     * Репозиторий данных о погоде.
     */
    private final WeatherJdbcRepository weatherRepository;

    /**
     * Объект для управления транзакциями.
     */
    private final TransactionTemplate transactionTemplate;

    /**
     * Сервис предоставления сообщений.
     */
    private final MessageProvider messageProvider;

    /**
     * Конструктор.
     *
     * @param cityRepository        репозиторий городов
     * @param weatherTypeRepository репозиторий типов погоды
     * @param weatherRepository     репозиторий данных о погоде
     * @param transactionTemplate   объект для управления транзакциями
     * @param messageProvider       сервис предоставления сообщений
     */
    @Autowired
    public WeatherCrudServiceJdbcImpl(
            final CityJdbcRepository cityRepository,
            final WeatherTypeJdbcRepository weatherTypeRepository,
            final WeatherJdbcRepository weatherRepository,
            final TransactionTemplate transactionTemplate,
            final MessageProvider messageProvider
    ) {
        this.cityRepository = cityRepository;
        this.weatherTypeRepository = weatherTypeRepository;
        this.weatherRepository = weatherRepository;
        this.transactionTemplate = transactionTemplate;
        this.messageProvider = messageProvider;
    }

    @Override
    public Weather getById(final UUID id) {
        transactionTemplate.setReadOnly(true);
        transactionTemplate.setIsolationLevel(ISOLATION_READ_COMMITTED);
        return transactionTemplate.execute(status -> findWeatherById(id));
    }

    @Override
    public List<Weather> getAll() {
        transactionTemplate.setReadOnly(true);
        transactionTemplate.setIsolationLevel(ISOLATION_READ_COMMITTED);
        return transactionTemplate.execute(status -> weatherRepository.findAll());
    }

    @Override
    public Weather create(final Weather newWeather) {
        transactionTemplate.setReadOnly(false);
        transactionTemplate.setIsolationLevel(ISOLATION_SERIALIZABLE);
        return transactionTemplate.execute(status -> {
            weatherRepository.findFirstByCityIdAndDateTime(
                    newWeather.getCity().getId(),
                    newWeather.getDateTime()
            ).ifPresent(weather -> {
                throw new UniqueWeatherException(
                        messageProvider.getMessage(
                                MESSAGE_CODE_WEATHER_UNIQUE_CITY_AND_TIME,
                                weather.getCity().getId(),
                                weather.getDateTime().format(DATE_TIME_FORMATTER)
                        ),
                        FIELD_CITY,
                        FIELD_DATE_TIME
                );
            });

            final City newCity = newWeather.getCity();
            if (newCity == null || newCity.getId() == null) {
                throw new InvalidPropertyException(
                        messageProvider.getMessage(MESSAGE_CODE_INVALID_FIELD),
                        "cityId"
                );
            }

            final WeatherType newType = newWeather.getType();
            if (newType == null || newType.getId() == null) {
                throw new InvalidPropertyException(
                        messageProvider.getMessage(MESSAGE_CODE_INVALID_FIELD),
                        FIELD_TYPE
                );
            }

            final City foundCity = findCityById(newCity.getId());
            newWeather.setCity(foundCity);

            final WeatherType foundType = findWeatherTypeById(newType.getId());
            newWeather.setType(foundType);

            return weatherRepository.create(newWeather);
        });
    }

    @Override
    public void delete(final UUID id) {
        transactionTemplate.setReadOnly(false);
        transactionTemplate.setIsolationLevel(ISOLATION_READ_COMMITTED);
        transactionTemplate.executeWithoutResult(status -> weatherRepository.deleteById(id));
    }

    @Override
    public Weather update(final UUID id, final Weather changedWeather) {
        transactionTemplate.setReadOnly(false);
        transactionTemplate.setIsolationLevel(ISOLATION_SERIALIZABLE);
        return transactionTemplate.execute(status -> {
            final Weather weatherToBeUpdated = findWeatherById(id);

            weatherRepository.findFirstByCityIdAndDateTimeWithAnotherId(
                    changedWeather.getCity().getId(),
                    changedWeather.getDateTime(),
                    id
            ).ifPresent(weather -> {
                throw new UniqueWeatherException(
                        messageProvider.getMessage(
                                MESSAGE_CODE_WEATHER_UNIQUE_CITY_AND_TIME,
                                weather.getCity().getName(),
                                weather.getDateTime().format(DATE_TIME_FORMATTER)
                        ),
                        FIELD_CITY,
                        FIELD_DATE_TIME
                );
            });

            Optional.ofNullable(changedWeather.getTemperature()).ifPresent(weatherToBeUpdated::setTemperature);
            Optional.ofNullable(changedWeather.getDateTime()).ifPresent(weatherToBeUpdated::setDateTime);

            final City newCity = changedWeather.getCity();
            if (newCity != null && newCity.getId() != null) {
                final City city = findCityById(newCity.getId());
                weatherToBeUpdated.setCity(city);
            }

            final WeatherType newType = changedWeather.getType();
            if (newType != null && newType.getId() != null) {
                final WeatherType type = findWeatherTypeById(newType.getId());
                weatherToBeUpdated.setType(type);
            }

            return weatherRepository.change(id, weatherToBeUpdated);
        });
    }

    @Override
    public Weather replace(final UUID id, final Weather newWeather) {
        transactionTemplate.setReadOnly(false);
        transactionTemplate.setIsolationLevel(ISOLATION_SERIALIZABLE);
        return transactionTemplate.execute(status -> {
            final Weather weatherToBeReplaced = findWeatherById(id);

            weatherRepository.findFirstByCityIdAndDateTimeWithAnotherId(
                    newWeather.getCity().getId(),
                    newWeather.getDateTime(),
                    id
            ).ifPresent(weather -> {
                throw new UniqueWeatherException(
                        messageProvider.getMessage(
                                MESSAGE_CODE_WEATHER_UNIQUE_CITY_AND_TIME,
                                weather.getCity().getName(),
                                weather.getDateTime().format(DATE_TIME_FORMATTER)
                        ),
                        FIELD_CITY,
                        FIELD_DATE_TIME
                );
            });

            weatherToBeReplaced.setTemperature(newWeather.getTemperature());
            weatherToBeReplaced.setDateTime(newWeather.getDateTime());

            final City newCity = newWeather.getCity();
            if (newCity == null || newCity.getId() == null) {
                throw new InvalidPropertyException(
                        messageProvider.getMessage(MESSAGE_CODE_INVALID_FIELD),
                        FIELD_CITY
                );
            }

            final City foundCity = findCityById(newCity.getId());
            weatherToBeReplaced.setCity(foundCity);

            final WeatherType newType = newWeather.getType();
            if (newType == null || newType.getId() == null) {
                throw new InvalidPropertyException(
                        messageProvider.getMessage(MESSAGE_CODE_INVALID_FIELD),
                        FIELD_CITY
                );
            }

            final WeatherType foundType = findWeatherTypeById(newType.getId());
            weatherToBeReplaced.setType(foundType);

            return weatherRepository.change(id, weatherToBeReplaced);
        });
    }


    @Override
    public List<Weather> getTemperatures(final String cityName) {
        transactionTemplate.setReadOnly(true);
        transactionTemplate.setIsolationLevel(ISOLATION_READ_COMMITTED);
        return transactionTemplate.execute(status ->
                weatherRepository.findAll()
                        .stream()
                        .filter(weather -> weather.getCity().getName().equals(cityName)
                                && weather.getDateTime().toLocalDate().equals(LocalDate.now())
                        )
                        .toList()
        );
    }

    @Override
    public Double getTemperature(final String cityName, final ChronoUnit timeUnit) {
        transactionTemplate.setReadOnly(true);
        transactionTemplate.setIsolationLevel(ISOLATION_READ_COMMITTED);
        return transactionTemplate.execute(status -> {
            final LocalDateTime now = LocalDateTime.now();

            final Weather weather = weatherRepository.findAllByCityName(cityName)
                    .stream()
                    .filter(w -> w.getDateTime().truncatedTo(timeUnit)
                            .equals(now.truncatedTo(timeUnit)))
                    .findFirst()
                    .orElseThrow(() -> new DataNotFoundException(
                            messageProvider.getMessage(MESSAGE_CODE_TEMPERATURE_NOT_FOUND, cityName)
                    ));

            return weather.getTemperature();
        });
    }

    @Override
    public void delete(final String cityName) {
        transactionTemplate.setReadOnly(false);
        transactionTemplate.setIsolationLevel(ISOLATION_READ_COMMITTED);
        transactionTemplate.executeWithoutResult(status ->
                weatherRepository.deleteAllByCityName(cityName)
        );
    }

    /**
     * Возвращает запись о погоде с указанным ID, если она существует.
     * В противном случае выбрасывает {@link DataNotFoundException}.
     *
     * @param id ID
     * @return запись о погоде с указанным ID, если она существует
     */
    private Weather findWeatherById(final UUID id) {
        return weatherRepository.findById(id)
                .orElseThrow(() -> new DataNotFoundException(
                        messageProvider.getMessage(
                                MESSAGE_CODE_WEATHER_NOT_FOUND,
                                id
                        ),
                        FIELD_ID
                ));
    }

    /**
     * Возвращает тип погоды с указанным ID, если он существует.
     * В противном случае выбрасывает {@link DataNotFoundException}.
     *
     * @param id ID
     * @return тип погоды с указанным ID, если он существует
     */
    private WeatherType findWeatherTypeById(final UUID id) {
        return weatherTypeRepository.findById(id)
                .orElseThrow(() -> new DataNotFoundException(
                        messageProvider.getMessage(
                                MESSAGE_CODE_WEATHER_TYPE_NOT_FOUND,
                                id
                        ),
                        FIELD_ID
                ));
    }

    /**
     * Возвращает город с указанным ID, если он существует.
     * В противном случае выбрасывает {@link DataNotFoundException}.
     *
     * @param id ID
     * @return город с указанным ID, если он существует
     */
    private City findCityById(final UUID id) {
        return cityRepository.findById(id)
                .orElseThrow(() -> new DataNotFoundException(
                        messageProvider.getMessage(
                                MESSAGE_CODE_CITY_NOT_FOUND,
                                id
                        ),
                        FIELD_ID
                ));
    }
}
