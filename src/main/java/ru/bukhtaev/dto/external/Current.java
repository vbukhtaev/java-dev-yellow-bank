package ru.bukhtaev.dto.external;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

import java.io.Serializable;
import java.math.BigDecimal;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

/**
 * Информация о погоде на текущий момент времени.
 */
@Schema(description = "Информация о погоде на текущий момент времени")
@Getter
public class Current implements Serializable {

    /**
     * Местное время, когда данные реального времени были обновлены в формате unix.
     */
    @Schema(description = "Местное время, когда данные реального времени были обновлены в формате unix")
    @JsonProperty("last_updated_epoch")
    private Integer lastUpdatedEpoch;

    /**
     * Местное время, когда данные реального времени были обновлены.
     */
    @Schema(description = "Местное время, когда данные реального времени были обновлены")
    @JsonProperty("last_updated")
    private String lastUpdated;

    /**
     * Температура по Цельсию.
     */
    @Schema(description = "Температура по Цельсию")
    @JsonProperty("temp_c")
    private BigDecimal temperatureC;

    /**
     * Температура по Фаренгейту.
     */
    @Schema(description = "Температура по Фаренгейту")
    @JsonProperty("temp_f")
    private BigDecimal temperatureF;

    /**
     * Показывать ли значок дневного или ночного состояния.
     * <ul>
     * <li>1 = Да</li>
     * <li>0 = Нет</li>
     * </ul>
     */
    @Schema(description = "Показывать ли значок дневного или ночного состояния (1 - Да, 0 - Нет)")
    @JsonProperty("is_day")
    private Integer isDay;

    /**
     * Погодные условия.
     */
    @Schema(description = "Погодные условия")
    @JsonProperty("condition")
    private Condition condition;

    /**
     * Максимальная скорость ветра в милях в час.
     */
    @Schema(description = "Максимальная скорость ветра в милях в час")
    @JsonProperty("wind_mph")
    private BigDecimal windMph;

    /**
     * Максимальная скорость ветра в километрах в час.
     */
    @Schema(description = "Максимальная скорость ветра в километрах в час")
    @JsonProperty("wind_kph")
    private BigDecimal windKph;

    /**
     * Направление ветра в градусах.
     */
    @Schema(description = "Направление ветра в градусах")
    @JsonProperty("wind_degree")
    private Integer windDegree;

    /**
     * Направление ветра по 16-точечному компасу, например: NSW.
     */
    @Schema(description = "Направление ветра по 16-точечному компасу, например: NSW")
    @JsonProperty("wind_dir")
    private String windDirection;

    /**
     * Давление в миллибарах.
     */
    @Schema(description = "Давление в миллибарах")
    @JsonProperty("pressure_mb")
    private Integer pressureMb;

    /**
     * Давление в дюймах.
     */
    @Schema(description = "Давление в дюймах")
    @JsonProperty("pressure_in")
    private BigDecimal pressureIn;

    /**
     * Количество осадков в миллиметрах.
     */
    @Schema(description = "Количество осадков в миллиметрах")
    @JsonProperty("precip_mm")
    private Integer precipitationMm;

    /**
     * Количество осадков в дюймах.
     */
    @Schema(description = "Количество осадков в дюймах")
    @JsonProperty("precip_in")
    private Integer precipitationIn;

    /**
     * Влажность в процентах.
     */
    @Schema(description = "Влажность в процентах")
    @JsonProperty("humidity")
    private Integer humidity;

    /**
     * Облачность в процентах.
     */
    @Schema(description = "Облачность в процентах")
    @JsonProperty("cloud")
    private Integer cloud;

    /**
     * По ощущениям температура по Цельсию.
     */
    @Schema(description = "По ощущениям температура по Цельсию")
    @JsonProperty("feelslike_c")
    private Integer feelsLikeC;

    /**
     * По ощущениям температура по Фаренгейту.
     */
    @Schema(description = "По ощущениям температура по Фаренгейту")
    @JsonProperty("feelslike_f")
    private BigDecimal feelsLikeF;

    /**
     * Средняя видимость в километрах.
     */
    @Schema(description = "Средняя видимость в километрах")
    @JsonProperty("vis_km")
    private Integer visibilityKm;

    /**
     * Средняя видимость в милях.
     */
    @Schema(description = "Средняя видимость в милях")
    @JsonProperty("vis_miles")
    private Integer visibilityMiles;

    /**
     * Ультрафиолетовый индекс.
     */
    @Schema(description = "Ультрафиолетовый индекс")
    @JsonProperty("uv")
    private Integer uv;

    /**
     * Порывы ветра в милях в час.
     */
    @Schema(description = "Порывы ветра в милях в час")
    @JsonProperty("gust_mph")
    private BigDecimal gustMph;

    /**
     * Порывы ветра в километрах в час.
     */
    @Schema(description = "Порывы ветра в километрах в час")
    @JsonProperty("gust_kph")
    private BigDecimal gustKph;

    /**
     * Информация о качестве воздуха.
     */
    @Schema(description = "Информация о качестве воздуха")
    @JsonProperty("air_quality")
    @JsonInclude(NON_NULL)
    private AirQuality airQuality;

}
