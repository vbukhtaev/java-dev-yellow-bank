package ru.bukhtaev.service;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;
import ru.bukhtaev.model.City;
import ru.bukhtaev.model.Weather;
import ru.bukhtaev.util.LruCache;
import ru.bukhtaev.validation.MessageProvider;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.locks.ReentrantLock;

import static ru.bukhtaev.validation.MessageUtils.MESSAGE_CODE_INVALID_DATA_TO_BE_CACHED;

/**
 * Сервис, предоставляющий потокобезопасный LRU-кэш для данных о погоде.
 */
@Component
@Validated
public class WeatherCache {

    /**
     * Блокировщик.
     */
    private final ReentrantLock lock = new ReentrantLock(true);

    /**
     * LRU-кэш для данных о погоде, использующий в качестве ключа ID.
     */
    private final LruCache<UUID, Weather> uuidCache;

    /**
     * LRU-кэш для данных о погоде, использующий в качестве ключа название города.
     */
    private final LruCache<String, Weather> cityNameCache;

    /**
     * Сервис предоставления сообщений.
     */
    private final MessageProvider messageProvider;

    /**
     * Конструктор.
     *
     * @param capacity        вместимость
     * @param messageProvider сервис предоставления сообщений
     */
    @Autowired
    public WeatherCache(
            @Value("${cache.weather.size}") final int capacity,
            final MessageProvider messageProvider
    ) {
        this.uuidCache = new LruCache<>(capacity);
        this.cityNameCache = new LruCache<>(capacity);
        this.messageProvider = messageProvider;
    }

    /**
     * Возвращает объект типа {@link Optional} с записью о погоде
     * с указанным ID из кэша, если такая запись существует в кэше.
     * В противном случае возвращает пустой объект типа {@link Optional}.
     *
     * @param id ID
     * @return объект типа {@link Optional} с записью о погоде
     * с указанным ID из кэша, если такая запись существует в кэше.
     */
    public Optional<Weather> get(final UUID id) {
        lock.lock();
        try {
            final Weather weather = uuidCache.get(id);
            if (weather == null) {
                return Optional.empty();
            }

            cityNameCache.get(weather.getCity().getName());
            return Optional.of(weather);

        } finally {
            lock.unlock();
        }
    }

    /**
     * Возвращает объект типа {@link Optional} с записью о погоде
     * с указанным названием города из кэша, если такая запись существует в кэше.
     * В противном случае возвращает пустой объект типа {@link Optional}.
     *
     * @param cityName название города
     * @return объект типа {@link Optional} с записью о погоде
     * с указанным названием города из кэша, если такая запись существует в кэше.
     */
    public Optional<Weather> get(@NotBlank final String cityName) {
        lock.lock();
        try {
            final Weather weather = cityNameCache.get(cityName);
            if (weather == null) {
                return Optional.empty();
            }

            uuidCache.get(weather.getId());
            return Optional.of(weather);

        } finally {
            lock.unlock();
        }
    }

    /**
     * Добавляет запись о погоде в кэш.
     *
     * @param newWeather запись о погоде для добавления в кэш
     * @return добавленную в кэш запись о погоде
     */
    public Weather put(@Valid final Weather newWeather) {
        lock.lock();
        try {
            final UUID id = newWeather.getId();
            final City city = newWeather.getCity();

            validate(newWeather);

            uuidCache.put(id, newWeather);
            cityNameCache.put(city.getName(), newWeather);

            return newWeather;

        } finally {
            lock.unlock();
        }
    }

    /**
     * Удаляет запись о погоде из кэша.
     *
     * @param toBeDeleted запись о погоде для удаления из кэша
     */
    public void delete(@Valid final Weather toBeDeleted) {
        lock.lock();
        try {
            final UUID id = toBeDeleted.getId();
            final City city = toBeDeleted.getCity();

            validate(toBeDeleted);

            uuidCache.delete(id);
            cityNameCache.delete(city.getName());

        } finally {
            lock.unlock();
        }
    }

    /**
     * Проверяет переданную для кэширования запись о погоде на валидность.
     *
     * @param weather запись о погоде
     */
    private void validate(final Weather weather) {
        if (Objects.isNull(weather.getId())
                || Objects.isNull(weather.getCity())
                || Objects.isNull(weather.getCity().getName())
        ) {
            throw new IllegalArgumentException(
                    messageProvider.getMessage(MESSAGE_CODE_INVALID_DATA_TO_BE_CACHED)
            );
        }
    }
}
