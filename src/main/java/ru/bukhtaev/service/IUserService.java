package ru.bukhtaev.service;

import jakarta.validation.Valid;
import ru.bukhtaev.model.User;
import ru.bukhtaev.security.Role;

/**
 * Сервис для работы с пользователями.
 */
public interface IUserService {

    /**
     * Регистрирует нового пользователя с ролью {@link Role#USER}.
     *
     * @param newUser новый пользователь
     * @return зарегистрированный пользователь
     */
    User register(@Valid final User newUser);
}
