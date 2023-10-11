package ru.bukhtaev.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.bukhtaev.dto.external.ExternalApiWeatherResponse;
import ru.bukhtaev.service.IExternalWeatherApiService;
import ru.bukhtaev.validation.handling.ErrorResponse;

/**
 * Контроллер для взаимодействия с внешним сервисом данных о погоде.
 *
 * @see <a href="https://www.weatherapi.com">Weather API</a>
 */
@Tag(name = "Взаимодействие с внешним API")
@RestController
@RequestMapping(value = "/api/external", produces = "application/json")
public class ExternalWeatherApiController {

    /**
     * Сервис для выполнения запросов к внешнему API данных о погоде.
     */
    private final IExternalWeatherApiService externalApiService;

    /**
     * Конструктор.
     *
     * @param externalApiService сервис для выполнения запросов к внешнему API
     */
    @Autowired
    public ExternalWeatherApiController(final IExternalWeatherApiService externalApiService) {
        this.externalApiService = externalApiService;
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
    public ResponseEntity<ExternalApiWeatherResponse> get(
            @Parameter(description = "Местоположение")
            @RequestParam(value = "location") final String location,
            @Parameter(description = "Язык")
            @RequestParam(value = "language", required = false) final String language,
            @Parameter(description = "Нужна ли информация о качестве воздуха")
            @RequestParam(value = "aqi", required = false) final Boolean aqi
    ) {
        return ResponseEntity.ok(
                externalApiService.current(location, language, aqi)
        );
    }
}
