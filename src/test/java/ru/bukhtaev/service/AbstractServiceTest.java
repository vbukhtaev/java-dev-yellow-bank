package ru.bukhtaev.service;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Абстрактный класс модульных тестов сервиса.
 */
@ExtendWith(MockitoExtension.class)
public abstract class AbstractServiceTest {

    /**
     * Название поля исключения, хранящего сообщение об ошибке.
     */
    protected static final String ERROR_MESSAGE_PROPERTY_NAME = "errorMessage";
}
