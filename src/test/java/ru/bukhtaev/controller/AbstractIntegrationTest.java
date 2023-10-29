package ru.bukhtaev.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import ru.bukhtaev.AbstractContainerizedTest;

import java.time.LocalDateTime;

/**
 * Абстрактный интеграционный тест.
 */
@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc(printOnlyOnFailure = false)
public abstract class AbstractIntegrationTest extends AbstractContainerizedTest {

    /**
     * Текущая дата и время.
     */
    protected static final LocalDateTime NOW = LocalDateTime.now().withNano(0);

    /**
     * Дата и время сутки назад от текущей.
     */
    protected static final LocalDateTime YESTERDAY = NOW.minusDays(1);

    /**
     * Автоматически сконфигурированный {@link MockMvc}.
     */
    @Autowired
    protected MockMvc mockMvc;

    /**
     * Маппер объектов.
     */
    @Autowired
    protected ObjectMapper objectMapper;
}
