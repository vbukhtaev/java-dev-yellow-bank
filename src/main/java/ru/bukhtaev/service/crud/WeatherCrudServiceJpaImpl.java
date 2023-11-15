package ru.bukhtaev.service.crud;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.bukhtaev.exception.DataNotFoundException;
import ru.bukhtaev.exception.InvalidPropertyException;
import ru.bukhtaev.exception.UniqueWeatherException;
import ru.bukhtaev.model.City;
import ru.bukhtaev.model.Weather;
import ru.bukhtaev.model.WeatherType;
import ru.bukhtaev.repository.jpa.ICityJpaRepository;
import ru.bukhtaev.repository.jpa.IWeatherJpaRepository;
import ru.bukhtaev.repository.jpa.IWeatherTypeJpaRepository;
import ru.bukhtaev.validation.MessageProvider;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;
import static org.springframework.transaction.annotation.Isolation.SERIALIZABLE;
import static ru.bukhtaev.model.BaseEntity.FIELD_ID;
import static ru.bukhtaev.model.Weather.*;
import static ru.bukhtaev.util.Utils.DATE_TIME_FORMATTER;
import static ru.bukhtaev.validation.MessageUtils.*;

/**
 * JPA-реализация сервиса CRUD операций над данными о погоде.
 */
@Service("weatherCrudServiceJpa")
@Transactional(
        isolation = READ_COMMITTED,
        readOnly = true
)
public class WeatherCrudServiceJpaImpl implements ICrudService<Weather, UUID> {

    /**
     * Репозиторий городов.
     */
    private final ICityJpaRepository cityRepository;

    /**
     * Репозиторий типов погоды.
     */
    private final IWeatherTypeJpaRepository weatherTypeRepository;

    /**
     * Репозиторий данных о погоде.
     */
    private final IWeatherJpaRepository weatherRepository;

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
     * @param messageProvider       сервис предоставления сообщений
     */
    @Autowired
    public WeatherCrudServiceJpaImpl(
            final ICityJpaRepository cityRepository,
            final IWeatherTypeJpaRepository weatherTypeRepository,
            final IWeatherJpaRepository weatherRepository,
            final MessageProvider messageProvider
    ) {
        this.cityRepository = cityRepository;
        this.weatherTypeRepository = weatherTypeRepository;
        this.weatherRepository = weatherRepository;
        this.messageProvider = messageProvider;
    }

    @Override
    public Weather getById(final UUID id) {
        return findWeatherById(id);
    }

    @Override
    public List<Weather> getAll() {
        return weatherRepository.findAll();
    }

    @Override
    @Transactional(isolation = SERIALIZABLE)
    public Weather create(final Weather newWeather) {
        final City newCity = newWeather.getCity();
        if (newCity == null || newCity.getId() == null) {
            throw new InvalidPropertyException(
                    messageProvider.getMessage(MESSAGE_CODE_INVALID_FIELD),
                    FIELD_CITY
            );
        }

        final WeatherType newType = newWeather.getType();
        if (newType == null || newType.getId() == null) {
            throw new InvalidPropertyException(
                    messageProvider.getMessage(MESSAGE_CODE_INVALID_FIELD),
                    FIELD_TYPE
            );
        }

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

        final City foundCity = findCityById(newCity.getId());
        newWeather.setCity(foundCity);

        final WeatherType foundType = findWeatherTypeById(newType.getId());
        newWeather.setType(foundType);

        return weatherRepository.save(newWeather);
    }

    @Override
    @Transactional(isolation = READ_COMMITTED)
    public void delete(final UUID id) {
        weatherRepository.deleteById(id);
    }

    @Override
    @Transactional(isolation = SERIALIZABLE)
    public Weather update(final UUID id, final Weather changedWeather) {
        final Weather weatherToBeUpdated = findWeatherById(id);

        final City newCity = changedWeather.getCity();
        if (newCity != null && newCity.getId() != null) {
            weatherRepository.findFirstByCityIdAndDateTimeAndIdNot(
                    changedWeather.getCity().getId(),
                    changedWeather.getDateTime(),
                    id
            ).ifPresent(weather -> {
                throw new UniqueWeatherException(
                        messageProvider.getMessage(
                                MESSAGE_CODE_WEATHER_UNIQUE_CITY_AND_TIME,
                                changedWeather.getCity().getId(),
                                changedWeather.getDateTime().format(DATE_TIME_FORMATTER)
                        ),
                        FIELD_CITY,
                        FIELD_DATE_TIME
                );
            });

            final City city = findCityById(newCity.getId());
            weatherToBeUpdated.setCity(city);
        }

        final WeatherType newType = changedWeather.getType();
        if (newType != null && newType.getId() != null) {
            final WeatherType type = findWeatherTypeById(newType.getId());
            weatherToBeUpdated.setType(type);
        }

        Optional.ofNullable(changedWeather.getTemperature())
                .ifPresent(weatherToBeUpdated::setTemperature);
        Optional.ofNullable(changedWeather.getDateTime())
                .ifPresent(weatherToBeUpdated::setDateTime);

        return weatherRepository.save(weatherToBeUpdated);
    }

    @Override
    @Transactional(isolation = SERIALIZABLE)
    public Weather replace(final UUID id, final Weather newWeather) {
        final Weather weatherToBeReplaced = findWeatherById(id);

        final var cityId = newWeather.getCity().getId();
        final var dateTime = newWeather.getDateTime();
        weatherRepository.findFirstByCityIdAndDateTimeAndIdNot(
                cityId,
                dateTime,
                id
        ).ifPresent(weather -> {
            throw new UniqueWeatherException(
                    messageProvider.getMessage(
                            MESSAGE_CODE_WEATHER_UNIQUE_CITY_AND_TIME,
                            cityId,
                            dateTime.format(DATE_TIME_FORMATTER)
                    ),
                    FIELD_CITY,
                    FIELD_DATE_TIME
            );
        });

        weatherToBeReplaced.setTemperature(newWeather.getTemperature());
        weatherToBeReplaced.setDateTime(dateTime);

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

        return weatherRepository.save(weatherToBeReplaced);
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
