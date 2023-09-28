package ru.bukhtaev.repository.impl;

import lombok.Getter;
import ru.bukhtaev.model.Weather;
import ru.bukhtaev.repository.Repository;
import ru.bukhtaev.util.NotEnoughSpaceException;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/**
 * Репозиторий, хранящий данные в оперативной памяти.
 */
public class InMemoryRepository implements Repository<Weather> {

    /**
     * Хранилище в оперативной памяти.
     */
    private final List<Weather> storage = new ArrayList<>();

    /**
     * Максимальная вместимость репозитория.
     */
    @Getter
    private final int capacity;

    public InMemoryRepository(final int capacity) {
        if (capacity < 1) {
            throw new IllegalArgumentException("Invalid capacity: " + capacity);
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
    public void saveAll(final List<Weather> objects) {
        if (this.storage.size() + objects.size() > capacity) {
            throw new NotEnoughSpaceException(
                    "Not enough storage space! Free storage space: "
                            + (capacity - this.storage.size())
            );
        }

        this.storage.addAll(objects);
    }

    @Override
    public void save(final Weather object) {
        if (this.storage.size() == capacity) {
            throw new NotEnoughSpaceException("There is no storage space!");
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
}
