package ru.bukhtaev.service;

import org.springframework.stereotype.Service;
import ru.bukhtaev.model.City;
import ru.bukhtaev.model.Weather;
import ru.bukhtaev.model.WeatherType;
import ru.bukhtaev.validation.MessageProvider;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static ru.bukhtaev.validation.MessageUtils.*;

/**
 * Реализация сервиса генерации данных о погоде.
 */
@Service
public class GenerationServiceImpl implements IGenerationService {

    /**
     * Генератор случайных чисел.
     */
    private static final Random random = new Random();

    /**
     * Сервис предоставления сообщений.
     */
    private final MessageProvider messageProvider;

    /**
     * Конструктор.
     *
     * @param messageProvider сервис предоставления сообщений
     */
    public GenerationServiceImpl(final MessageProvider messageProvider) {
        this.messageProvider = messageProvider;
    }

    @Override
    public List<Weather> generate(
            final List<City> cities,
            final List<WeatherType> types,
            int count
    ) {
        if (cities.isEmpty()) {
            throw new IllegalArgumentException(
                    messageProvider.getMessage(MESSAGE_CODE_EMPTY_CITIES_LIST)
            );
        }

        if (types.isEmpty()) {
            throw new IllegalArgumentException(
                    messageProvider.getMessage(MESSAGE_CODE_EMPTY_TYPES_LIST)
            );
        }

        if (count < 1) {
            throw new IllegalArgumentException(
                    messageProvider.getMessage(MESSAGE_CODE_INCORRECT_COUNT, count)
            );
        }

        final List<Weather> data = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            final int cityIndex = random.nextInt(cities.size());
            final int typeIndex = random.nextInt(types.size());

            final Weather weather = Weather.builder()
                    .city(cities.get(cityIndex))
                    .type(types.get(typeIndex))
                    .temperature(
                            random.nextDouble(-40.0, 40.0)
                    )
                    .dateTime(LocalDateTime.of(
                            random.nextInt(2000, 2024),
                            random.nextInt(1, 13),
                            random.nextInt(1, 29),
                            random.nextInt(0, 24),
                            random.nextInt(0, 60),
                            random.nextInt(0, 60)
                    ))
                    .build();

            data.add(weather);
        }

        return data;
    }
}
