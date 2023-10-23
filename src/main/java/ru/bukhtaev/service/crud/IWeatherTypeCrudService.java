package ru.bukhtaev.service.crud;

import jakarta.validation.Valid;
import org.springframework.transaction.annotation.Transactional;
import ru.bukhtaev.model.WeatherType;

import java.util.List;
import java.util.UUID;

/**
 * Сервис CRUD операций над типами погоды.
 */
@Transactional(readOnly = true)
public interface IWeatherTypeCrudService {

    /**
     * Возвращает тип погоды с указанным ID.
     *
     * @param id ID
     * @return тип погоды с указанным ID
     */
    WeatherType getById(final UUID id);

    /**
     * Возвращает все типы погоды.
     *
     * @return все типы погоды.
     */
    List<WeatherType> getAll();

    /**
     * Сохраняет новый тип погоды в базу данных.
     *
     * @param newWeatherType новый тип погоды
     * @return сохраненный тип погоды
     */
    @Transactional
    WeatherType create(@Valid final WeatherType newWeatherType);

    /**
     * Удаляет тип погоды с указанным ID из базы данных.
     *
     * @param id ID
     */
    @Transactional
    void delete(final UUID id);

    /**
     * Обновляет тип погоды с указанным ID.
     *
     * @param id                 ID
     * @param changedWeatherType данные для обновления
     * @return обновленный тип погоды
     */
    @Transactional
    WeatherType update(final UUID id, final WeatherType changedWeatherType);

    /**
     * Заменяет тип погоды с указанными ID.
     *
     * @param id             ID
     * @param newWeatherType новый тип погоды
     * @return замененный тип погоды
     */
    @Transactional
    WeatherType replace(final UUID id, @Valid final WeatherType newWeatherType);
}
