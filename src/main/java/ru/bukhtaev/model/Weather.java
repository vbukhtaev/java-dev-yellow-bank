package ru.bukhtaev.model;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

import static ru.bukhtaev.util.Utils.DATE_TIME_FORMATTER;

/**
 * Измерение температуры в городе.
 */
@Getter
@Setter
@Builder
public final class Weather {

    /**
     * Уникальный идентификатор города.
     */
    private UUID cityId;

    /**
     * Название города
     */
    @NotBlank
    private String cityName;

    /**
     * Температура
     */
    @NotNull
    @Min(-100)
    @Max(100)
    private Double temperature;

    /**
     * Дата и время
     */
    @NotNull
    private LocalDateTime dateTime;

    @Override
    public String toString() {
        return "Weather(" + cityId +
                ", '" + cityName + "'" +
                ", " + temperature + "°C" +
                ", " + dateTime.format(DATE_TIME_FORMATTER) + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Weather weather = (Weather) o;

        if (!cityId.equals(weather.cityId)) return false;
        if (!cityName.equals(weather.cityName)) return false;
        if (!temperature.equals(weather.temperature)) return false;
        return dateTime.equals(weather.dateTime);
    }

    @Override
    public int hashCode() {
        int result = cityId.hashCode();
        result = 31 * result + cityName.hashCode();
        result = 31 * result + temperature.hashCode();
        result = 31 * result + dateTime.hashCode();
        return result;
    }
}