package ru.bukhtaev.dto.external;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.io.Serializable;

/**
 * Модель HTTP-ответа от внешнего API данных о погоде.
 * Содержит информацию об ошибке.
 *
 * @see <a href="https://www.weatherapi.com">Weather API</a>
 */
@Schema(description = "Ответ с ошибкой от внешнего API")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExternalApiErrorResponse implements Serializable {

    /**
     * Ошибка API.
     */
    @Schema(description = "Ошибка API")
    @JsonProperty("error")
    private ApiErrorDto error;
}


