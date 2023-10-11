package ru.bukhtaev.util;

import lombok.Getter;

/**
 * Перечисление с возможными значениями кодов ошибок внешнего API.
 */
@Getter
public enum ErrorCode {

    /**
     * Токен для внешнего API не предоставлен.
     */
    TOKEN_NOT_PROVIDED(1002),

    /**
     * Местоположение не предоставлено.
     */
    LOCATION_NOT_PROVIDED(1003),

    /**
     * Некорректный URL запроса к внешнему API.
     */
    INVALID_URL(1005),

    /**
     * Местоположение не найдено.
     */
    LOCATION_NOT_FOUND(1006),

    /**
     * Предоставлен некорректный токен для внешнего API.
     */
    INVALID_TOKEN(2006),

    /**
     * Превышен месячный лимит запросов к внешнему API для указанного токена.
     */
    TOKEN_LIMIT_EXCEEDED(2007),

    /**
     * Токен для внешнего API более не действителен.
     */
    DISABLED_TOKEN(2008),

    /**
     * Нет доступа к внешнему API для указанного токена.
     */
    ACCESS_DENIED(2009),

    /**
     * Некорректное тело массового запроса.
     */
    INVALID_JSON(9000),

    /**
     * Тело массового запроса содержит слишком много местоположений.
     */
    TOO_MANY_LOCATIONS(9001),

    /**
     * Ошибка на стороне сервера внешнего API.
     */
    EXTERNAL_API_ERROR(9999);

    /**
     * Код ошибки внешнего API.
     */
    private final Integer code;

    /**
     * Конструктор.
     *
     * @param code код ошибки внешнего API
     */
    ErrorCode(int code) {
        this.code = code;
    }

    /**
     * Возвращает значение перечисления с указанными кодом ошибки, если оно существует.
     * В противном случае возвращает null.
     *
     * @param code код ошибки внешнего API
     * @return значение перечисления с указанными кодом ошибки
     */
    public static ErrorCode withCode(final Integer code) {
        if (code != null) {
            for (ErrorCode errorCode : values()) {
                if (errorCode.code.equals(code)) {
                    return errorCode;
                }
            }
        }

        return null;
    }
}
