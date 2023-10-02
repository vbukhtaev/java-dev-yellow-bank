package ru.bukhtaev.validation.handling;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.bukhtaev.util.DataNotFoundException;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Обработчик ошибок запросов к API.
 */
@Slf4j
@RestControllerAdvice
public class ApiRequestExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse onMethodArgumentNotValidException(final MethodArgumentNotValidException exception) {
        log.error(exception.getMessage(), exception);
        final List<Violation> violations = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> new Violation(
                        error.getDefaultMessage(),
                        error.getField()
                ))
                .toList();
        return new ErrorResponse(violations, LocalDateTime.now());
    }

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse onConstraintValidationException(final ConstraintViolationException exception) {
        log.error(exception.getMessage(), exception);
        final List<Violation> violations = exception.getConstraintViolations().stream()
                .map(violation -> new Violation(
                        violation.getMessage(),
                        resolveParamName(violation)
                ))
                .toList();
        return new ErrorResponse(violations, LocalDateTime.now());
    }

    @ExceptionHandler(DataNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse onDataNotFoundException(final DataNotFoundException exception) {
        log.error(exception.getMessage(), exception);
        return new ErrorResponse(
                new Violation(exception.getMessage()),
                LocalDateTime.now()
        );
    }

    /**
     * Распознает название параметра, значение которого нарушает правила валидации.
     *
     * @param violation нарушение
     * @return название параметра, значение которого нарушает правила валидации
     */
    private String resolveParamName(final ConstraintViolation<?> violation) {
        final String proprtyPathString = violation.getPropertyPath().toString();
        return proprtyPathString.substring(
                proprtyPathString.lastIndexOf(".") + 1
        );
    }
}
