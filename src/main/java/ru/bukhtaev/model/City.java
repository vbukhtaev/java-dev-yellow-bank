package ru.bukhtaev.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

/**
 * Модель города.
 */
@Getter
@Setter
@Entity
@Table(name = "city")
@NoArgsConstructor
public class City extends NameableEntity {

    /**
     * Конструктор.
     *
     * @param id   ID
     * @param name название
     */
    @Builder
    public City(final UUID id, final String name) {
        super(id, name);
    }
}
