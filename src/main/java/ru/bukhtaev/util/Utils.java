package ru.bukhtaev.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Утилитный класс, содержащий полезные константы и методы.
 */
public final class Utils {

    /**
     * Форматтер для {@link LocalDateTime}
     */
    public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");

    /**
     * Только для статического использования.
     */
    private Utils() {
    }

    /**
     * Округляет число с плавающей точкой с заданной точностью.
     *
     * @param value     число с плавающей точкой
     * @param precision точность
     * @return округленное с заданной точностью число с плавающей точкой
     */
    public static double round(final double value, final int precision) {
        final int scale = (int) Math.pow(10, precision);
        return (double) Math.round(value * scale) / scale;
    }
}
