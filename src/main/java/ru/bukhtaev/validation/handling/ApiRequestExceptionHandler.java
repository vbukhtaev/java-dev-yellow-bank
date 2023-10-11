package ru.bukhtaev.validation.handling;

import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.bukhtaev.exception.DataNotFoundException;
import ru.bukhtaev.exception.CommonClientSideException;
import ru.bukhtaev.exception.CommonServerSideException;

import java.time.LocalDateTime;
import java.util.List;

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.TOO_MANY_REQUESTS;

/**
 * Обработчик ошибок запросов к API.
 */
@Slf4j
@RestControllerAdvice
public class ApiRequestExceptionHandler {

    @ExceptionHandler(Throwable.class)
    public ResponseEntity<ErrorResponse> handle(final Throwable exception) {
        log.error(exception.getMessage(), exception);
        return ResponseEntity.status(INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse(
                        new Violation("Unknown error"),
                        LocalDateTime.now()
                ));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handle(final MethodArgumentNotValidException exception) {
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
    public ErrorResponse handle(final ConstraintViolationException exception) {
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
    public ErrorResponse handle(final DataNotFoundException exception) {
        log.error(exception.getMessage(), exception);
        return new ErrorResponse(
                new Violation(exception.getMessage()),
                LocalDateTime.now()
        );
    }

    @ExceptionHandler(CommonClientSideException.class)
    public ResponseEntity<ErrorResponse> handle(final CommonClientSideException exception) {
        log.error(exception.getErrorMessage(), exception);
        return ResponseEntity.status(exception.getTargetStatus())
                .body(new ErrorResponse(
                        new Violation(
                                exception.getErrorMessage(),
                                exception.getParamNames()
                        ),
                        LocalDateTime.now()
                ));
    }

    @ExceptionHandler(CommonServerSideException.class)
    public ResponseEntity<ErrorResponse> handle(final CommonServerSideException exception) {
        log.error(exception.getErrorMessage(), exception);
        return ResponseEntity.status(INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse(
                        new Violation(INTERNAL_SERVER_ERROR.getReasonPhrase()),
                        LocalDateTime.now()
                ));
    }

    @ExceptionHandler(RequestNotPermitted.class)
    public ResponseEntity<ErrorResponse> handle(final RequestNotPermitted exception) {
        log.error(exception.getMessage(), exception);
        return ResponseEntity.status(TOO_MANY_REQUESTS)
                .body(new ErrorResponse(
                        new Violation("Request limit exceeded"),
                        LocalDateTime.now()
                ));
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
