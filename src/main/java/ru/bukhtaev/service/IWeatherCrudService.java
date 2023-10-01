package ru.bukhtaev.service;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;
import ru.bukhtaev.model.Weather;

import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Сервис CRUD операций с данными о погоде.
 */
@Validated
public interface IWeatherCrudService {

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
    void remove(@NotBlank final String cityName);

    /**
     * Создает запись о погоде.
     *
     * @param weather запись о погоде
     * @return созданную запись о погоде
     */
    Weather create(@Valid final Weather weather);

    /**
     * Обновляет температуру в записи о погоде если она существует.
     * В противном случае создает новую запись о погоде.
     *
     * @param newWeather запись о погоде с данными для изменения
     * @return обновленную запись о погоде
     */
    Weather update(@Valid final Weather newWeather);
}
