package ru.bukhtaev.validation.handling;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.ResponseErrorHandler;

import java.io.IOException;
import java.text.MessageFormat;

import static org.springframework.http.HttpStatus.Series.CLIENT_ERROR;
import static org.springframework.http.HttpStatus.Series.SERVER_ERROR;

/**
 * Обработчик ошибок запросов к внешним API.
 */
@Slf4j
public class RestTemplateResponseErrorHandler implements ResponseErrorHandler {

    /**
     * Шаблон сообщения для логов.
     */
    private static final String MESSAGE_TEMPLATE = "<{0}> handled <{1}> with status code <{2}>";

    @Override
    public boolean hasError(final ClientHttpResponse httpResponse) throws IOException {
        return (httpResponse.getStatusCode().is4xxClientError()
                || httpResponse.getStatusCode().is5xxServerError());
    }

    @Override
    public void handleError(final ClientHttpResponse httpResponse) throws IOException {
        final String handlerClassName = this.getClass().getSimpleName();
        final HttpStatusCode statusCode = httpResponse.getStatusCode();

        if (httpResponse.getStatusCode().is5xxServerError()) {
            log.warn(MessageFormat.format(
                    MESSAGE_TEMPLATE,
                    handlerClassName,
                    SERVER_ERROR,
                    statusCode
            ));

        } else if (httpResponse.getStatusCode().is4xxClientError()) {
            log.warn(MessageFormat.format(
                    MESSAGE_TEMPLATE,
                    handlerClassName,
                    CLIENT_ERROR,
                    statusCode
            ));
        }
    }
}
