package ru.bukhtaev.service.impl;

import ru.bukhtaev.model.Weather;
import ru.bukhtaev.repository.Repository;
import ru.bukhtaev.service.GenerationService;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static ru.bukhtaev.util.Utils.DATE_TIME_FORMATTER;

/**
 * Реализация сервиса генерации данных о погоде.
 */
public class GenerationServiceImpl implements GenerationService {

    /**
     * Репозиторий для данных о погоде.
     */
    private final Repository<Weather> repository;

    /**
     * Генератор случайных чисел.
     */
    private final Random random;

    /**
     * Конструктор.
     *
     * @param repository репозиторий для данных о погоде
     */
    public GenerationServiceImpl(final Repository<Weather> repository) {
        this.repository = repository;
        this.random = new Random();
    }

    @Override
    public void generate(final Set<String> regions, int count) {
        if (regions.isEmpty()) {
            throw new IllegalArgumentException("Empty regions set!");
        }

        if (count < 1) {
            throw new IllegalArgumentException("Incorrect count: " + count);
        }

        final Map<UUID, String> regionsMap = regions.stream()
                .collect(Collectors.toMap(id -> UUID.randomUUID(), region -> region));

        final List<UUID> regionIds = regionsMap.keySet().stream().toList();

        final List<Weather> data = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            final int index = random.nextInt(regionIds.size());

            final Weather weather = Weather.builder()
                    .regionId(regionIds.get(index))
                    .regionName(regionsMap.get(regionIds.get(index)))
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

        repository.saveAll(data);
    }

    @Override
    public void print() {
        final List<Weather> data = repository.findAll();

        if (!data.isEmpty()) {
            final String line = "-".repeat(109) + "\n";
            final StringBuilder builder = new StringBuilder();
            builder.append("\n").append(line);
            builder.append(
                    String.format("| %36s | %20s | %20s | %20s |",
                            "ID",
                            "Region",
                            "Temperature",
                            "Date and time"
                    )
            );
            builder.append("\n").append(line);

            data.forEach(weather -> {
                builder.append(
                String.format("| %36s | %20s | %20.15f | %20s |",
                        weather.getRegionId(),
                        weather.getRegionName(),
                        weather.getTemperature(),
                        weather.getDateTime().format(DATE_TIME_FORMATTER)
                )
                );
                builder.append("\n");
            });
            builder.append(line);

            System.out.print(builder);
        }
    }

    @Override
    public void clear() {
        repository.clear();
    }
}
