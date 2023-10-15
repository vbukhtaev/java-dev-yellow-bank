package ru.bukhtaev.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Погода в городе.
 */
@Getter
@Setter
@Entity
@Table(
        name = "weather",
        uniqueConstraints = @UniqueConstraint(columnNames = {"city_id", "date_time"})
)
@NoArgsConstructor
public final class Weather extends BaseEntity {

    /**
     * Название поля, хранящего город.
     */
    public static final String FIELD_CITY = "city";

    /**
     * Название поля, хранящего тип погоды.
     */
    public static final String FIELD_TYPE = "type";

    /**
     * Название поля, хранящего температуру.
     */
    public static final String FIELD_TEMPERATURE = "temperature";

    /**
     * Название поля, хранящего дату и время.
     */
    public static final String FIELD_DATE_TIME = "dateTime";

    /**
     * Город.
     */
    @ManyToOne
    @JoinColumn(name = "city_id", referencedColumnName = "id", nullable = false)
    private City city;

    /**
     * Тип погоды.
     */
    @ManyToOne
    @JoinColumn(name = "weather_type_id", referencedColumnName = "id", nullable = false)
    private WeatherType type;

    /**
     * Температура.
     */
    @NotNull
    @Min(-100)
    @Max(100)
    @Column(name = "temperature", nullable = false)
    private Double temperature;

    /**
     * Дата и время.
     */
    @NotNull
    @Column(name = "date_time", nullable = false)
    private LocalDateTime dateTime;

    /**
     * Конструктор.
     *
     * @param id          ID
     * @param city        город
     * @param type        тип погоды
     * @param temperature температура
     * @param dateTime    дата и время
     */
    @Builder
    public Weather(
            final UUID id,
            final City city,
            final WeatherType type,
            final Double temperature,
            final LocalDateTime dateTime
    ) {
        super(id);
        this.city = city;
        this.type = type;
        this.temperature = temperature;
        this.dateTime = dateTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        Weather weather = (Weather) o;

        if (!Objects.equals(city, weather.city)) return false;
        if (!Objects.equals(type, weather.type)) return false;
        if (!Objects.equals(temperature, weather.temperature)) return false;
        return Objects.equals(dateTime, weather.dateTime);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (city != null ? city.hashCode() : 0);
        result = 31 * result + (type != null ? type.hashCode() : 0);
        result = 31 * result + (temperature != null ? temperature.hashCode() : 0);
        result = 31 * result + (dateTime != null ? dateTime.hashCode() : 0);
        return result;
    }
}