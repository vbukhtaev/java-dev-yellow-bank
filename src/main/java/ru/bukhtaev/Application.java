package ru.bukhtaev;

import ru.bukhtaev.model.Weather;
import ru.bukhtaev.service.GenerationService;
import ru.bukhtaev.service.WeatherService;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Приложение.
 */
public class Application {

    /**
     * Множество регионов.
     */
    private static final Set<String> REGIONS = Set.of(
            "Kazan",
            "Yekaterinburg",
            "Novosibirsk",
            "Vladivostok",
            "St. Petersburg",
            "Moscow"
    );

    /**
     * Сервис обработки данных о погоде.
     */
    private final WeatherService weatherService;

    /**
     * Сервис генерации данных о погоде.
     */
    private final GenerationService generator;

    /**
     * Конструктор.
     *
     * @param weatherService сервис обработки данных о погоде
     * @param generator      сервис генерации данных о погоде
     */
    public Application(
            final WeatherService weatherService,
            final GenerationService generator
    ) {
        this.weatherService = weatherService;
        this.generator = generator;
    }

    /**
     * Запускает приложение.
     */
    public void run() {

        generator.clear();
        generator.generate(REGIONS, 32);
        generator.print();

        final double averageTemperature = weatherService.averageTemperature();
        final Map<String, Double> averageTemperatures = weatherService.averageTemperatures();

        final double temperature1 = 28;
        final Set<String> regionsWarmerThan = weatherService.getRegionsWarmerThan(temperature1);

        final double temperature2 = -20;
        final Set<String> regionsStrictlyWarmerThan = weatherService.getRegionsStrictlyWarmerThan(temperature2);

        final Map<UUID, List<Double>> temperaturesGroupedById = weatherService.groupTemperaturesById();

        final Map<Integer, List<Weather>> groupedByTemperature = weatherService.groupByTemperature();

        System.out.println("\nЗадание №1.1. Общая средняя температура: " + averageTemperature + "°C");

        System.out.println("\nЗадание №1.2. Средняя температура в регионах.");
        averageTemperatures.forEach((region, temperature) ->
                System.out.printf("%16s : %8.2f°C%n", region, temperature)
        );

        System.out.println("""
                                
                Задание №2.1. Регионы с температурой выше указанной.
                Если значение температуры хотя бы в одном измерении выше указанной.
                Найдено регионов:\s""" + regionsWarmerThan.size()
        );
        regionsWarmerThan.forEach(region -> System.out.println(" - " + region));

        System.out.println("""
                                
                Задание №2.2. Регионы с температурой выше указанной.
                Если значение температуры во всех измерениях выше указанной.
                Найдено регионов:\s""" + regionsStrictlyWarmerThan.size()
        );
        regionsStrictlyWarmerThan.forEach(region -> System.out.println(" - " + region));

        System.out.println("\nЗадание №3. Сгруппировать значения температуры по идентификаторам регионов.");
        temperaturesGroupedById.forEach((id, temperatures)
                -> System.out.printf("<%36s> : %s%n", id, temperatures)
        );

        System.out.println("""
                                
                Задание №4. Сгруппировать измерения по значениям температуры.
                P. S. Группировал по округленным целым значениям."""
        );
        groupedByTemperature.forEach((temperature, weathers) ->
                System.out.printf("%5d°C : %s%n", temperature, weathers)
        );
    }
}
