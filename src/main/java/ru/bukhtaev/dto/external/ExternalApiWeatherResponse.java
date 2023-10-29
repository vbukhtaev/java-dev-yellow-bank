package ru.bukhtaev.dto.external;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.io.Serializable;

/**
 * Модель HTTP-ответа от внешнего API данных о погоде.
 * Содержит информацию о погоде в указанном местоположении в текущее время.
 *
 * @see <a href="https://www.weatherapi.com">Weather API</a>
 */
@Schema(description = "Информация о погоде в указанном местоположении в текущее время")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExternalApiWeatherResponse implements Serializable {

    /**
     * Информация о местоположении.
     */
    @Schema(description = "Информация о местоположении")
    @JsonProperty("location")
    private LocationDto location;

    /**
     * Информация о погоде на текущий момент времени.
     */
    @Schema(description = "Информация о погоде на текущий момент времени")
    @JsonProperty("current")
    private Current current;
}
