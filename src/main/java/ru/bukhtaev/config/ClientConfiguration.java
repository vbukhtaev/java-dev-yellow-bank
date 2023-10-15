package ru.bukhtaev.config;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import ru.bukhtaev.validation.handling.RestTemplateResponseErrorHandler;

/**
 * Конфигурация клиентов для взаимодействия с внешними API.
 */
@Configuration
public class ClientConfiguration {

    @Bean("restTemplateWithLoggingErrors")
    public RestTemplate restTemplate(final RestTemplateBuilder restTemplateBuilder) {
        return restTemplateBuilder
                .errorHandler(responseErrorHandler())
                .build();
    }

    @Bean("restTemplateResponseErrorHandler")
    public RestTemplateResponseErrorHandler responseErrorHandler() {
        return new RestTemplateResponseErrorHandler();
    }
}
