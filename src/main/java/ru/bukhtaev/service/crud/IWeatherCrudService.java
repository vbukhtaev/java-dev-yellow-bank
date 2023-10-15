package ru.bukhtaev.service.crud;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import ru.bukhtaev.model.Weather;

import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

/**
 * Сервис CRUD операций над данными о погоде.
 */
@Validated
@Transactional(readOnly = true)
public interface IWeatherCrudService {

    /**
     * Возвращает запись о погоде с указанным ID.
     *
     * @param id ID
     * @return запись о погоде с указанным ID
     */
    Weather getById(final UUID id);

    /**
     * Возвращает все данные о погоде.
     *
     * @return все данные о погоде.
     */
    List<Weather> getAll();

    /**
     * Сохраняет новую запись о погоде в базу данных.
     *
     * @param newWeather новая запись о погоде
     * @return сохраненная запись о погоде
     */
    @Transactional
    Weather create(@Valid final Weather newWeather);

    /**
     * Удаляет запись о погоде с указанным ID из базы данных.
     *
     * @param id ID
     */
    @Transactional
    void delete(final UUID id);

    /**
     * Обновляет запись о погоде с указанным ID.
     *
     * @param id             ID
     * @param changedWeather данные для обновления
     * @return обновленная запись о погоде
     */
    @Transactional
    Weather update(final UUID id, final Weather changedWeather);

    /**
     * Заменяет запись о погоде с указанными ID.
     *
     * @param id         ID
     * @param newWeather новая запись о погоде
     * @return замененная запись о погоде
     */
    @Transactional
    Weather replace(final UUID id, @Valid final Weather newWeather);

    /**
     * Возвращает данные о погоде в указанном городе на текущую дату.
     *
     * @param cityName название города
     * @return данные о погоде
     */
    List<Weather> getTemperatures(@NotBlank final String cityName);

    /**
     * Возвращает данные о погоде в указанном городе на текущую дату и время.
     * Дата и время усекаются до указанных единиц.
     *
     * @param cityName название города
     * @param timeUnit единица времени, до которой производится усечения
     * @return данные о погоде
     */
    Double getTemperature(@NotBlank final String cityName, @NotNull final ChronoUnit timeUnit);

    /**
     * Удаляет все данные о погоде для указанного города.
     *
     * @param cityName название города
     */
    @Transactional
    void delete(@NotBlank final String cityName);
}
