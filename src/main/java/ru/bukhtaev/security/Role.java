package ru.bukhtaev.security;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Set;

import static ru.bukhtaev.security.Permission.*;

/**
 * Модель роли.
 */
@Getter
@RequiredArgsConstructor
public enum Role {

    /**
     * Администратор.
     */
    ADMIN(Set.of(Permission.values())),

    /**
     * Пользователь
     */
    USER(Set.of(
            CITIES_READ,
            WEATHER_TYPES_READ,
            WEATHER_DATA_READ
    ));

    /**
     * Разрешения.
     */
    private final Set<Permission> permissions;
}
