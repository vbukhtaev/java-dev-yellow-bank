package ru.bukhtaev.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import ru.bukhtaev.model.Weather;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO для модели {@link Weather}, используемый в качестве тела HTTP-запроса.
 */
@Schema(description = "Погода в городе")
@Getter
@Builder
@AllArgsConstructor
public class WeatherRequestDto {

    /**
     * Температура.
     */
    @Schema(
            description = "Температура",
            example = "36.6"
    )
    @NotNull
    @Min(-100)
    @Max(100)
    private final Double temperature;

    /**
     * Дата и время.
     */
    @Schema(description = "Дата и время")
    @NotNull
    private final LocalDateTime dateTime;

    /**
     * ID города.
     */
    @Schema(description = "ID города")
    @NotNull
    private final UUID cityId;

    /**
     * ID типа погоды.
     */
    @Schema(description = "ID типа погоды")
    @NotNull
    private final UUID typeId;
}
