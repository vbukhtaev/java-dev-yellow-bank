package ru.bukhtaev.repository;

import lombok.Getter;
import org.springframework.stereotype.Component;
import ru.bukhtaev.model.Weather;
import ru.bukhtaev.exception.NotEnoughSpaceException;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import static ru.bukhtaev.validation.MessageUtils.MESSAGE_INVALID_CAPACITY;

/**
 * Репозиторий, хранящий данные о погоде в оперативной памяти.
 */
@Component
public class InMemoryRepository implements IRepository<Weather> {

    /**
     * Максимальная вместимость репозитория по умолчанию.
     */
    private static final int DEFAULT_CAPACITY = 256;

    /**
     * Хранилище в оперативной памяти.
     */
    private final List<Weather> storage = new ArrayList<>();

    /**
     * Максимальная вместимость репозитория.
     */
    @Getter
    private final int capacity;

    /**
     * Конструктор без параметров.
     */
    public InMemoryRepository() {
        this.capacity = DEFAULT_CAPACITY;
    }

    /**
     * Конструктор.
     *
     * @param capacity максимальная вместимость репозитория
     */
    public InMemoryRepository(int capacity) {
        if (capacity < 1) {
            throw new IllegalArgumentException(
                    MessageFormat.format(MESSAGE_INVALID_CAPACITY, capacity)
            );
        }

        this.capacity = capacity;
    }

    @Override
    public List<Weather> findAll() {
        return this.storage;
    }

    @Override
    public List<Weather> find(final Predicate<Weather> condition) {
        return this.storage.stream()
                .filter(condition)
                .toList();
    }

    @Override
    public Optional<Weather> findFirst(final Predicate<Weather> condition) {
        return this.storage.stream()
                .filter(condition)
                .findFirst();
    }

    @Override
    public void saveAll(final List<Weather> objects) {
        if (this.storage.size() + objects.size() > this.capacity) {
            throw new NotEnoughSpaceException(this.capacity - this.storage.size());
        }

        this.storage.addAll(objects);
    }

    @Override
    public void save(final Weather object) {
        if (this.storage.size() == this.capacity) {
            throw new NotEnoughSpaceException(0);
        }

        this.storage.add(object);
    }

    @Override
    public void clear() {
        this.storage.clear();
    }

    @Override
    public void remove(final Weather object) {
        this.storage.remove(object);
    }

    @Override
    public void remove(final String cityName) {
        this.storage.removeIf(weather -> weather.getCityName().equals(cityName));
    }
}
