package ru.bukhtaev.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ru.bukhtaev.dto.WeatherResponseDto;
import ru.bukhtaev.dto.external.ExternalApiWeatherResponse;
import ru.bukhtaev.dto.mapper.IWeatherMapper;
import ru.bukhtaev.model.Weather;
import ru.bukhtaev.service.IExternalWeatherApiService;
import ru.bukhtaev.validation.handling.ErrorResponse;

import java.util.Locale;

import static ru.bukhtaev.controller.ExternalWeatherApiController.URL_API_EXTERNAL;

/**
 * Контроллер для взаимодействия с внешним сервисом данных о погоде.
 *
 * @see <a href="https://www.weatherapi.com">Weather API</a>
 */
@Tag(name = "Взаимодействие с внешним API")
@RestController
@SecurityRequirement(name = "basicAuth")
@RequestMapping(value = URL_API_EXTERNAL, produces = "application/json")
public class ExternalWeatherApiController {

    /**
     * URL.
     */
    public static final String URL_API_EXTERNAL = "/api/external";

    /**
     * Сервис для выполнения запросов к внешнему API данных о погоде.
     */
    private final IExternalWeatherApiService externalApiService;

    /**
     * Маппер для DTO данных о погоде.
     */
    private final IWeatherMapper mapper;

    /**
     * Конструктор.
     *
     * @param externalApiService сервис для выполнения запросов к внешнему API
     * @param mapper             маппер для DTO данных о погоде
     */
    @Autowired
    public ExternalWeatherApiController(
            @Qualifier("weatherApiServiceJpa") final IExternalWeatherApiService externalApiService,
            final IWeatherMapper mapper
    ) {
        this.externalApiService = externalApiService;
        this.mapper = mapper;
    }

    @Operation(summary = "Получение данных о погоде в текущее время в указанном месте")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Данные о погоде получены"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Местоположение не предоставлено",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )}
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Местоположение не найдено",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )}
            ),
            @ApiResponse(
                    responseCode = "429",
                    description = "Превышен лимит запросов",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )}
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Внутренняя ошибка сервера",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )}
            )
    })
    @GetMapping("/current")
    @PreAuthorize("hasAuthority('weather-data:read')")
    public ResponseEntity<ExternalApiWeatherResponse> get(
            @Parameter(description = "Местоположение")
            @RequestParam(value = "location", required = false) final String location,
            @Parameter(description = "Язык")
            @RequestParam(value = "language", required = false) final String language,
            @Parameter(description = "Нужна ли информация о качестве воздуха")
            @RequestParam(value = "aqi", required = false) final Boolean aqi
    ) {
        return ResponseEntity.ok(
                externalApiService.getCurrent(location, language, aqi)
        );
    }

    @Operation(summary = "Получение данных о погоде в текущее время в указанном месте с сохранением")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Данные о погоде получены"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Местоположение не предоставлено",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )}
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Местоположение не найдено",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )}
            ),
            @ApiResponse(
                    responseCode = "429",
                    description = "Превышен лимит запросов",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )}
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Внутренняя ошибка сервера",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )}
            )
    })
    @PostMapping("/save-current")
    @PreAuthorize("hasAuthority('weather-data:write')")
    public ResponseEntity<WeatherResponseDto> getAndSave(
            @Parameter(description = "Местоположение")
            @RequestParam(value = "location", required = false) final String location,
            @Parameter(description = "Нужна ли информация о качестве воздуха")
            @RequestParam(value = "aqi", required = false) final Boolean aqi
    ) {
        final var weatherResponse = externalApiService.getCurrent(
                location,
                Locale.ENGLISH.getLanguage(),
                aqi
        );

        final Weather saved = externalApiService.saveWithTransaction(
                mapper.convertFromExternalDto(weatherResponse)
        );

        return ResponseEntity.ok(
                mapper.convertToDto(saved)
        );
    }
}
