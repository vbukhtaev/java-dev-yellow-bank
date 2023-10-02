package ru.bukhtaev.service;

import org.springframework.stereotype.Service;
import ru.bukhtaev.model.Weather;
import ru.bukhtaev.validation.MessageProvider;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static ru.bukhtaev.validation.MessageUtils.MESSAGE_CODE_EMPTY_CITIES_SET;
import static ru.bukhtaev.validation.MessageUtils.MESSAGE_CODE_INCORRECT_COUNT;

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
    public List<Weather> generate(final Set<String> cities, int count) {
        if (cities.isEmpty()) {
            throw new IllegalArgumentException(
                    messageProvider.getMessage(MESSAGE_CODE_EMPTY_CITIES_SET)
            );
        }

        if (count < 1) {
            throw new IllegalArgumentException(
                    messageProvider.getMessage(MESSAGE_CODE_INCORRECT_COUNT, count)
            );
        }

        final Map<UUID, String> citiesMap = cities.stream()
                .collect(Collectors.toMap(id -> UUID.randomUUID(), city -> city));

        final List<UUID> cityIds = citiesMap.keySet().stream().toList();

        final List<Weather> data = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            final int index = random.nextInt(cityIds.size());

            final Weather weather = Weather.builder()
                    .cityId(cityIds.get(index))
                    .cityName(citiesMap.get(cityIds.get(index)))
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
