package ru.bukhtaev.service.crud;

import jakarta.validation.Valid;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import ru.bukhtaev.model.City;

import java.util.List;
import java.util.UUID;

/**
 * Сервис CRUD операций над городами.
 */
@Validated
@Transactional(readOnly = true)
public interface ICityCrudService {

    /**
     * Возвращает город с указанным ID.
     *
     * @param id ID
     * @return город с указанным ID
     */
    City getById(final UUID id);

    /**
     * Возвращает все города.
     *
     * @return все города.
     */
    List<City> getAll();

    /**
     * Сохраняет новый город в базу данных.
     *
     * @param newCity новый город
     * @return сохраненный город
     */
    @Transactional
    City create(@Valid final City newCity);

    /**
     * Удаляет город с указанным ID из базы данных.
     *
     * @param id ID
     */
    @Transactional
    void delete(final UUID id);

    /**
     * Обновляет город с указанным ID.
     *
     * @param id          ID
     * @param changedCity данные для обновления
     * @return обновленный город
     */
    @Transactional
    City update(final UUID id, final City changedCity);

    /**
     * Заменяет город с указанными ID.
     *
     * @param id      ID
     * @param newCity новый город
     * @return замененный город
     */
    @Transactional
    City replace(final UUID id, @Valid final City newCity);
}
