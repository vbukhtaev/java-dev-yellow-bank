package ru.bukhtaev.service;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.bukhtaev.model.City;
import ru.bukhtaev.model.Weather;
import ru.bukhtaev.model.WeatherType;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Абстрактный класс модульных тестов сервиса.
 */
@ExtendWith(MockitoExtension.class)
public abstract class AbstractServiceTest {

    /**
     * Текущая дата и время.
     */
    protected static final LocalDateTime NOW = LocalDateTime.now().withNano(0);

    /**
     * Дата и время сутки назад от текущей.
     */
    protected static final LocalDateTime YESTERDAY = NOW.minusDays(1);

    /**
     * Название поля исключения, хранящего сообщение об ошибке.
     */
    protected static final String ERROR_MESSAGE_PROPERTY_NAME = "errorMessage";

    /**
     * Перехватчик ID, передаваемого в качестве аргумента метода.
     */
    @Captor
    protected ArgumentCaptor<UUID> idCaptor;

    /**
     * Перехватчик строки, передаваемой в качестве аргумента метода.
     */
    @Captor
    protected ArgumentCaptor<String> stringCaptor;

    /**
     * Перехватчик записи о погоде, передаваемого в качестве аргумента метода.
     */
    @Captor
    protected ArgumentCaptor<Weather> weatherCaptor;

    /**
     * Перехватчик города, передаваемого в качестве аргумента метода.
     */
    @Captor
    protected ArgumentCaptor<City> cityCaptor;

    /**
     * Перехватчик типа погоды, передаваемого в качестве аргумента метода.
     */
    @Captor
    protected ArgumentCaptor<WeatherType> typeCaptor;
}
