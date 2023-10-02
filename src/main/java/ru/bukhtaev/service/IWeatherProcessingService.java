package ru.bukhtaev.service;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;
import ru.bukhtaev.model.Weather;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Сервис обработки данных о погоде.
 */
@Validated
public interface IWeatherProcessingService {

    /**
     * Вычисляет среднюю температуру для каждого города в переданном наборе данных
     *
     * @param data данные
     * @return среднюю температуру для каждого города в переданном наборе данных
     */
    Map<String, Double> getAverageTemperatures(
            @NotEmpty final List<@NotNull Weather> data,
            @Min(0) final int precision
    );

    /**
     * Вычисляет среднюю температуру в переданном наборе данных
     *
     * @param data данные
     * @return среднюю температуру в переданном наборе данных
     */
    double getAverageTemperature(
            @NotEmpty final List<@NotNull Weather> data,
            @Min(0) final int precision
    );

    /**
     * Определяет города, для которых хотя бы одно измерение со значением температуры выше указанной.
     *
     * @param data        данные
     * @param temperature температура
     * @return города, для которых существуют измерения со значением температуры выше указанной
     */
    Set<String> getCitiesWarmerThan(
            @NotEmpty final List<@NotNull Weather> data,
            final double temperature
    );

    /**
     * Определяет города, для которых все измерения со значением температуры выше указанной.
     *
     * @param data        данные
     * @param temperature температура
     * @return города, для которых существуют измерения со значением температуры выше указанной
     */
    Set<String> getCitiesStrictlyWarmerThan(
            @NotEmpty final List<@NotNull Weather> data,
            final double temperature
    );

    /**
     * Группирует значения температуры по идентификатору города.
     *
     * @param data данные
     * @return сгруппированные по идентификатору города значения температуры
     */
    Map<UUID, List<Double>> groupTemperaturesById(@NotEmpty final List<@NotNull Weather> data);

    /**
     * Группирует измерения по температуре.
     *
     * @param data данные
     * @return сгруппированные по температуре измерения
     */
    Map<Integer, List<Weather>> groupByTemperature(@NotEmpty final List<@NotNull Weather> data);
}
