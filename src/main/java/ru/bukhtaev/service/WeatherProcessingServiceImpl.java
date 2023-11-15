package ru.bukhtaev.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.bukhtaev.exception.DataNotFoundException;
import ru.bukhtaev.model.Weather;
import ru.bukhtaev.repository.jpa.IWeatherJpaRepository;
import ru.bukhtaev.validation.MessageProvider;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;
import static ru.bukhtaev.util.Utils.round;
import static ru.bukhtaev.validation.MessageUtils.*;

/**
 * Реализация сервиса обработки данных о погоде.
 */
@Service
@Transactional(
        isolation = READ_COMMITTED,
        readOnly = true
)
public class WeatherProcessingServiceImpl implements IWeatherProcessingService {

    /**
     * Репозиторий данных о погоде.
     */
    private final IWeatherJpaRepository weatherRepository;

    /**
     * Сервис предоставления сообщений.
     */
    private final MessageProvider messageProvider;

    /**
     * Конструктор.
     *
     * @param weatherRepository репозиторий данных о погоде
     * @param messageProvider   сервис предоставления сообщений
     */
    @Autowired
    public WeatherProcessingServiceImpl(
            final IWeatherJpaRepository weatherRepository,
            final MessageProvider messageProvider
    ) {
        this.weatherRepository = weatherRepository;
        this.messageProvider = messageProvider;
    }

    @Override
    public List<Weather> getTemperatures(final String cityName) {
        return weatherRepository.findAll()
                .stream()
                .filter(weather -> weather.getCity().getName().equals(cityName)
                        && weather.getDateTime().toLocalDate().equals(LocalDate.now())
                )
                .toList();
    }

    @Override
    public Double getTemperature(final String cityName, final ChronoUnit timeUnit) {
        final LocalDateTime now = LocalDateTime.now();

        final Weather weather = weatherRepository.findAllByCityName(cityName)
                .stream()
                .filter(w -> w.getDateTime().truncatedTo(timeUnit)
                        .equals(now.truncatedTo(timeUnit)))
                .findFirst()
                .orElseThrow(() -> new DataNotFoundException(
                        messageProvider.getMessage(
                                MESSAGE_CODE_TEMPERATURE_NOT_FOUND,
                                cityName
                        )
                ));

        return weather.getTemperature();
    }

    @Override
    @Transactional(isolation = READ_COMMITTED)
    public void delete(final String cityName) {
        weatherRepository.deleteAllByCityName(cityName);
    }

    @Override
    public Map<String, Double> getAverageTemperatures(final List<Weather> data, final int precision) {
        validate(data);

        return data.stream()
                .collect(Collectors.groupingBy(weather ->
                                weather.getCity().getName(),
                        Collectors.collectingAndThen(
                                Collectors.averagingDouble(Weather::getTemperature),
                                avgTemperature -> round(avgTemperature, precision)
                        )
                ));
    }

    @Override
    public double getAverageTemperature(final List<Weather> data, final int precision) {
        validate(data);

        return round(data.stream()
                        .mapToDouble(Weather::getTemperature)
                        .average()
                        .orElseThrow(() -> new RuntimeException(
                                messageProvider.getMessage(MESSAGE_CODE_FAILED_TO_COMPUTE)
                        )),
                precision
        );
    }

    @Override
    public Set<String> getCitiesWarmerThan(final List<Weather> data, final double temperature) {
        validate(data);

        return data.stream()
                .filter(weather -> weather.getTemperature() > temperature)
                .map(weather -> weather.getCity().getName())
                .collect(Collectors.toSet());
    }

    @Override
    public Set<String> getCitiesStrictlyWarmerThan(final List<Weather> data, final double temperature) {
        validate(data);

        return data.stream()
                .collect(Collectors.groupingBy(weather ->
                                weather.getCity().getName(),
                        Collectors.mapping(weather -> weather.getTemperature() > temperature,
                                Collectors.toList())))
                .entrySet().stream()
                .filter(entry -> entry.getValue().stream().allMatch(Boolean::booleanValue))
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
    }

    @Override
    public Map<UUID, List<Double>> groupTemperaturesById(final List<Weather> data) {
        validate(data);

        return data.stream()
                .collect(
                        Collectors.groupingBy(weather ->
                                        weather.getCity().getId(),
                                Collectors.mapping(
                                        Weather::getTemperature, Collectors.toList()
                                )
                        )
                );
    }

    @Override
    public Map<Integer, List<Weather>> groupByTemperature(final List<Weather> data) {
        validate(data);

        return data.stream()
                .collect(
                        Collectors.groupingBy(
                                weather -> (int) Math.round(weather.getTemperature())
                        )
                );
    }

    /**
     * Проверяет наличие данных в переданном списке.
     *
     * @param data список с данными
     */
    private void validate(List<Weather> data) {
        if (data.isEmpty()) {
            throw new IllegalArgumentException(
                    messageProvider.getMessage(MESSAGE_CODE_THERE_IS_NO_DATA)
            );
        }
    }
}
