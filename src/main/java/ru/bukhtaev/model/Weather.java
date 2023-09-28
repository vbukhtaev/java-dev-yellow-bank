package ru.bukhtaev.model;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

import static ru.bukhtaev.util.Utils.DATE_TIME_FORMATTER;

/**
 * Измерение температуры в регионе.
 */
@Getter
@Builder
public final class Weather {

    /**
     * Уникальный идентификатор региона.
     */
    private final UUID regionId;

    /**
     * Название региона
     */
    private final String regionName;

    /**
     * Температура
     */
    private final Double temperature;

    /**
     * Время и дата
     */
    private final LocalDateTime dateTime;

    @Override
    public String toString() {
        return "Weather(" + regionId +
                ", '" + regionName + "'" +
                ", " + temperature + "°C" +
                ", " + dateTime.format(DATE_TIME_FORMATTER) + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Weather weather = (Weather) o;

        if (!regionId.equals(weather.regionId)) return false;
        if (!regionName.equals(weather.regionName)) return false;
        if (!temperature.equals(weather.temperature)) return false;
        return dateTime.equals(weather.dateTime);
    }

    @Override
    public int hashCode() {
        int result = regionId.hashCode();
        result = 31 * result + regionName.hashCode();
        result = 31 * result + temperature.hashCode();
        result = 31 * result + dateTime.hashCode();
        return result;
    }
}