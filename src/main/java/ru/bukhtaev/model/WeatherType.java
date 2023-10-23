package ru.bukhtaev.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

/**
 * Модель типа погоды.
 */
@Getter
@Setter
@Entity
@Table(name = "weather_type")
@NoArgsConstructor
public class WeatherType extends NameableEntity {

    /**
     * Конструктор.
     *
     * @param id   ID
     * @param name название
     */
    @Builder
    public WeatherType(final UUID id, final String name) {
        super(id, name);
    }
}
