package ru.bukhtaev.repository;

import java.util.List;
import java.util.function.Predicate;

/**
 * Репозиторий для работы с объектами типа {@code T}.
 *
 * @param <T> тип объектов
 */
public interface Repository<T> {

    /**
     * Возвращает все объекты типа {@code T}.
     *
     * @return указанное количество объектов типа {@code T}
     */
    List<T> findAll();

    /**
     * Возвращает объекты, удовлетворяющие переданному условию.
     *
     * @param condition условие
     * @return объекты, удовлетворяющие переданному условию.
     */
    List<T> find(final Predicate<T> condition);

    /**
     * Сохраняет переданный список объектов типа {@code T}.
     *
     * @param objects список объектов типа {@code T}
     */
    void saveAll(final List<T> objects);

    /**
     * Сохраняет переданный объект типа {@code T}.
     *
     * @param object объект типа {@code T}
     */
    void save(final T object);

    /**
     * Удаляет все объекты типа {@code T}.
     */
    void clear();

    /**
     * Удаляет переданный объект типа {@code T}.
     *
     * @param object объект типа {@code T}
     */
    void remove(final T object);
}
