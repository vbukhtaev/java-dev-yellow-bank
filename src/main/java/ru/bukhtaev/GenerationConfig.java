package ru.bukhtaev;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import ru.bukhtaev.model.Weather;
import ru.bukhtaev.repository.IRepository;
import ru.bukhtaev.service.IGenerationService;

import java.util.Set;

/**
 * Конфигурация наполнения начальными данными.
 */
@Configuration
public class GenerationConfig {

    /**
     * Множество городов для генерации начальных данных.
     */
    @Value("${generation.cities}")
    private Set<String> cities;

    /**
     * Множество городов для генерации начальных данных.
     */
    @Value("${generation.count}")
    private Integer count;

    @Bean
    @Profile("!test")
    public ApplicationRunner dataLoader(
            final IGenerationService generator,
            final IRepository<Weather> repository
    ) {
        return args -> repository.saveAll(
                generator.generate(cities, count)
        );
    }
}
