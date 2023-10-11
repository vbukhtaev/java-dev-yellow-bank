package ru.bukhtaev.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.bukhtaev.model.Weather;
import ru.bukhtaev.repository.IRepository;
import ru.bukhtaev.exception.DataNotFoundException;
import ru.bukhtaev.validation.MessageProvider;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static ru.bukhtaev.validation.MessageUtils.MESSAGE_CODE_TEMPERATURE_NOT_FOUND;

/**
 * Реализация сервиса CRUD операций с данными о погоде.
 */
@Service
public class WeatherCrudServiceImpl implements IWeatherCrudService {

    /**
     * Репозиторий данных о погоде.
     */
    private final IRepository<Weather> repository;

    /**
     * Сервис предоставления сообщений/
     */
    private final MessageProvider messageProvider;

    /**
     * Конструктор.
     *
     * @param repository      репозиторий данных о погоде
     * @param messageProvider сервис предоставления сообщений
     */
    @Autowired
    public WeatherCrudServiceImpl(
            final IRepository<Weather> repository,
            final MessageProvider messageProvider
    ) {
        this.repository = repository;
        this.messageProvider = messageProvider;
    }

    @Override
    public List<Weather> getTemperatures(final String cityName) {
        return repository.findAll()
                .stream()
                .filter(weather -> weather.getCityName().equals(cityName)
                        && weather.getDateTime().toLocalDate().equals(LocalDate.now())
                )
                .toList();
    }

    @Override
    public Double getTemperature(final String cityName, final ChronoUnit timeUnit) {
        final LocalDateTime now = LocalDateTime.now();

        final Weather weather = repository.findFirst(
                w -> w.getCityName().equals(cityName)
                        && w.getDateTime().truncatedTo(timeUnit).equals(now.truncatedTo(timeUnit))
        ).orElseThrow(() -> new DataNotFoundException(
                messageProvider.getMessage(MESSAGE_CODE_TEMPERATURE_NOT_FOUND, cityName)
        ));

        return weather.getTemperature();
    }

    @Override
    public void remove(final String cityName) {
        repository.remove(cityName);
    }

    @Override
    public Weather create(final Weather weather) {
        final UUID cityId = resolveIdFor(weather.getCityName());
        weather.setCityId(cityId);
        repository.save(weather);

        return weather;
    }

    @Override
    public Weather update(final Weather weather) {
        final Optional<Weather> existent = repository.findFirst(
                w -> w.getCityName().equals(weather.getCityName())
                        && w.getDateTime().equals(weather.getDateTime())
        );

        if (existent.isPresent()) {
            final Weather weatherToBeUpdated = existent.get();
            weatherToBeUpdated.setTemperature(weather.getTemperature());
            return weatherToBeUpdated;
        }

        final UUID cityId = resolveIdFor(weather.getCityName());
        weather.setCityId(cityId);
        repository.save(weather);

        return weather;
    }

    private UUID resolveIdFor(final String cityName) {
        final Optional<Weather> found = repository.findFirst(w -> w.getCityName().equals(cityName));

        if (found.isPresent()) {
            return found.get().getCityId();
        }

        return UUID.randomUUID();
    }
}
