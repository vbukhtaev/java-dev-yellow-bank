package ru.bukhtaev;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import ru.bukhtaev.external.ExternalApiConfig;

@SpringBootApplication
@EnableConfigurationProperties(ExternalApiConfig.class)
public class WeatherCrudApplication {

    public static void main(String[] args) {
        SpringApplication.run(WeatherCrudApplication.class, args);
    }
}
