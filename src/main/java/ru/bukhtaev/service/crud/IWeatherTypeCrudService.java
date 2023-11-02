package ru.bukhtaev.service.crud;

import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import ru.bukhtaev.model.WeatherType;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Сервис CRUD операций над типами погоды.
 */
@Validated
public interface IWeatherTypeCrudService {

    /**
     * Возвращает тип погоды с указанным ID.
     *
     * @param id ID
     * @return тип погоды с указанным ID
     */
    WeatherType getById(final UUID id);

    /**
     * Возвращает объект типа {@link Optional} с типом погоды с указанным названием, если он существует.
     * В противном случае возвращает пустой объект типа {@link Optional}.
     *
     * @param name название
     * @return объект типа {@link Optional} с типом погоды с указанным названием, если он существует
     */
    Optional<WeatherType> getByName(final String name);

    /**
     * Возвращает все типы погоды.
     *
     * @return все типы погоды.
     */
    List<WeatherType> getAll();

    /**
     * Сохраняет новый тип погоды в базу данных.
     *
     * @param newType новый тип погоды
     * @return сохраненный тип погоды
     */
    WeatherType create(@Valid final WeatherType newType);

    /**
     * Удаляет тип погоды с указанным ID из базы данных.
     *
     * @param id ID
     */
    void delete(final UUID id);

    /**
     * Обновляет тип погоды с указанным ID.
     *
     * @param id                 ID
     * @param changedType данные для обновления
     * @return обновленный тип погоды
     */
    WeatherType update(final UUID id, final WeatherType changedType);

    /**
     * Заменяет тип погоды с указанными ID.
     *
     * @param id             ID
     * @param newType новый тип погоды
     * @return замененный тип погоды
     */
    WeatherType replace(final UUID id, @Valid final WeatherType newType);
}
