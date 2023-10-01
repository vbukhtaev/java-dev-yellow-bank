package ru.bukhtaev.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import ru.bukhtaev.model.Weather;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO для модели {@link Weather}, используемый в качестве тела HTTP-ответа.
 */
@Schema(description = "Данные о погоде")
@Getter
@AllArgsConstructor
public class WeatherResponseDto {

    /**
     * Уникальный идентификатор города.
     */
    @Schema(
            description = "ID города",
            example = "829b005b-8ea0-4c77-aab8-d2ea97bed47c"
    )
    private final UUID cityId;

    /**
     * Название города.
     */
    @Schema(description = "Название города")
    private final String cityName;

    /**
     * Температура.
     */
    @Schema(
            description = "Температура",
            example = "36.6"
    )
    private final Double temperature;

    /**
     * Дата и время.
     */
    @Schema(description = "Дата и время")
    @JsonFormat(pattern = "dd.MM.yyyy HH:mm:ss.SSS")
    private final LocalDateTime dateTime;
}
