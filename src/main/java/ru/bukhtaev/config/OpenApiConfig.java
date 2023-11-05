package ru.bukhtaev.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;

/**
 * Конфигурация Open API.
 */
@OpenAPIDefinition(
        info = @Info(
                title = "Weather CRUD API",
                description = "API for CRUD operations and weather data processing operations",
                version = "0.0.1-SNAPSHOT",
                contact = @Contact(
                        name = "Bukhtaev Vladislav",
                        url = "https://t.me/VBukhtaev"
                )
        )
)
@SecurityScheme(
        type = SecuritySchemeType.HTTP,
        name = "basicAuth",
        scheme = "basic"
)
public class OpenApiConfig {
}
