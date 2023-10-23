package ru.bukhtaev.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import ru.bukhtaev.model.Weather;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO для модели {@link Weather}, используемый в качестве тела HTTP-ответа.
 */
@Schema(description = "Погода в городе")
@Getter
public class WeatherResponseDto extends BaseResponseDto {

    /**
     * Город.
     */
    @Schema(description = "Город")
    private final NameableResponseDto city;

    /**
     * Тип погоды.
     */
    @Schema(description = "Тип погоды")
    private final NameableResponseDto type;

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

    /**
     * Конструктор.
     *
     * @param id          ID
     * @param city        город
     * @param type        тип погоды
     * @param temperature температура
     * @param dateTime    дата и время
     */
    public WeatherResponseDto(
            final UUID id,
            final NameableResponseDto city,
            final NameableResponseDto type,
            final Double temperature,
            final LocalDateTime dateTime
    ) {
        super(id);
        this.city = city;
        this.type = type;
        this.temperature = temperature;
        this.dateTime = dateTime;
    }
}
