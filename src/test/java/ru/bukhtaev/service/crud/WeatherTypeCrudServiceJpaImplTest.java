package ru.bukhtaev.service.crud;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import ru.bukhtaev.exception.DataNotFoundException;
import ru.bukhtaev.exception.UniqueNameException;
import ru.bukhtaev.model.WeatherType;
import ru.bukhtaev.repository.jpa.IWeatherTypeJpaRepository;
import ru.bukhtaev.service.AbstractServiceTest;
import ru.bukhtaev.validation.MessageProvider;

import java.text.MessageFormat;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static ru.bukhtaev.validation.MessageUtils.MESSAGE_CODE_WEATHER_TYPE_NOT_FOUND;
import static ru.bukhtaev.validation.MessageUtils.MESSAGE_CODE_WEATHER_TYPE_UNIQUE_NAME;

/**
 * Модульные тесты для JPA-реализации сервиса CRUD операций
 * над типами погоды {@link WeatherTypeCrudServiceJpaImpl}
 */
class WeatherTypeCrudServiceJpaImplTest extends AbstractServiceTest {

    /**
     * Имитация сервиса предоставления сообщений.
     */
    @Mock
    private MessageProvider messageProvider;

    /**
     * Имитация JPA-репозитория типов погоды.
     */
    @Mock
    private IWeatherTypeJpaRepository repository;

    /**
     * Тестируемая JPA-реализация сервиса CRUD операций над типами погоды.
     */
    @InjectMocks
    private WeatherTypeCrudServiceJpaImpl underTest;

    /**
     * Перехватчик ID, передаваемого в качестве аргумента метода.
     */
    @Captor
    private ArgumentCaptor<UUID> idCaptor;

    /**
     * Перехватчик названия, передаваемого в качестве аргумента метода.
     */
    @Captor
    private ArgumentCaptor<String> nameCaptor;

    /**
     * Перехватчик типа погоды, передаваемого в качестве аргумента метода.
     */
    @Captor
    private ArgumentCaptor<WeatherType> typeCaptor;

    private WeatherType typeClear;
    private WeatherType typeBlizzard;

    @BeforeEach
    void setUp() {
        typeClear = WeatherType.builder()
                .name("Ясно")
                .build();
        typeBlizzard = WeatherType.builder()
                .name("Метель")
                .build();
    }

    @Test
    void getById_withExistentId_shouldReturnEntity() {
        // given
        final UUID typeClearId = typeClear.getId();
        given(repository.findById(typeClearId))
                .willReturn(Optional.of(typeClear));

        // when
        underTest.getById(typeClearId);

        // then
        verify(repository, times(1))
                .findById(idCaptor.capture());
        verifyNoMoreInteractions(repository);
        assertThat(idCaptor.getValue())
                .isEqualTo(typeClearId);
    }

    @Test
    void getById_withNonExistentId_shouldThrowException() {
        // given
        final UUID typeBlizzardId = typeBlizzard.getId();
        final String errorMessage = MessageFormat.format(
                "Weather type with ID = <{0}> not found!",
                typeBlizzardId
        );
        given(repository.findById(typeBlizzardId))
                .willReturn(Optional.empty());
        given(messageProvider.getMessage(
                MESSAGE_CODE_WEATHER_TYPE_NOT_FOUND,
                typeBlizzardId
        )).willReturn(errorMessage);

        // when
        // then
        assertThatThrownBy(() -> underTest.getById(typeBlizzardId))
                .isInstanceOf(DataNotFoundException.class)
                .extracting(ERROR_MESSAGE_PROPERTY_NAME)
                .isEqualTo(errorMessage);

        verify(repository, times(1))
                .findById(idCaptor.capture());
        verifyNoMoreInteractions(repository);
        assertThat(idCaptor.getValue())
                .isEqualTo(typeBlizzardId);
    }

    @Test
    void getByName_shouldReturnEntity() {
        // given
        final String typeClearName = typeClear.getName();

        // when
        underTest.getByName(typeClearName);

        // then
        verify(repository, times(1))
                .findFirstByName(nameCaptor.capture());
        verifyNoMoreInteractions(repository);
        assertThat(nameCaptor.getValue())
                .isEqualTo(typeClearName);
    }

    @Test
    void getAll_shouldReturnAllEntities() {
        // given
        given(repository.findAll())
                .willReturn(List.of(typeClear, typeBlizzard));

        // when
        underTest.getAll();

        // then
        verify(repository, times(1)).findAll();
        verifyNoMoreInteractions(repository);
    }

    @Test
    void create_withNonExistentName_shouldCreateEntity() {
        // given
        given(repository.findFirstByName(typeClear.getName()))
                .willReturn(Optional.empty());

        // when
        underTest.create(typeClear);

        // then
        verify(repository, times(1))
                .save(typeCaptor.capture());
        assertThat(typeCaptor.getValue())
                .isEqualTo(typeClear);
    }

    @Test
    void create_withExistentName_shouldThrowException() {
        // given
        final String typeBlizzardName = typeBlizzard.getName();
        final String errorMessage = MessageFormat.format(
                "Weather type with name <{0}> already exists!",
                typeBlizzardName
        );
        given(repository.findFirstByName(typeBlizzard.getName()))
                .willReturn(Optional.of(typeBlizzard));
        given(messageProvider.getMessage(
                MESSAGE_CODE_WEATHER_TYPE_UNIQUE_NAME,
                typeBlizzardName
        )).willReturn(errorMessage);

        // when
        // then
        assertThatThrownBy(() -> underTest.create(typeBlizzard))
                .isInstanceOf(UniqueNameException.class)
                .extracting(ERROR_MESSAGE_PROPERTY_NAME)
                .isEqualTo(errorMessage);
        verify(repository, never()).save(any());
    }

    @Test
    void delete_shouldDeleteEntity() {
        // given
        final UUID typeClearId = typeClear.getId();

        // when
        underTest.delete(typeClearId);

        // then
        verify(repository, times(1))
                .deleteById(idCaptor.capture());
        verifyNoMoreInteractions(repository);
        assertThat(idCaptor.getValue()).isEqualTo(typeClearId);
    }

    @Test
    void update_withExistentName_shouldThrowException() {
        // given
        final UUID typeClearId = typeClear.getId();
        final String typeBlizzardName = typeBlizzard.getName();
        final String errorMessage = MessageFormat.format(
                "Weather type with name <{0}> already exists!",
                typeBlizzardName
        );
        given(repository.findFirstByNameAndIdNot(
                typeBlizzardName,
                typeClearId
        )).willReturn(Optional.of(typeBlizzard));
        given(messageProvider.getMessage(
                MESSAGE_CODE_WEATHER_TYPE_UNIQUE_NAME,
                typeBlizzardName
        )).willReturn(errorMessage);

        // when
        // then
        Assertions.assertThatThrownBy(() -> underTest.update(typeClearId, typeBlizzard))
                .isInstanceOf(UniqueNameException.class)
                .extracting(ERROR_MESSAGE_PROPERTY_NAME)
                .isEqualTo(errorMessage);
        verify(repository, never()).save(any());
    }

    @Test
    void update_withNonExistentNameAndNonExistentId_shouldThrowException() {
        // given
        final UUID typeClearId = typeClear.getId();
        final String errorMessage = MessageFormat.format(
                "Weather type with ID = <{0}> not found!",
                typeClearId
        );
        given(repository.findFirstByNameAndIdNot(
                typeBlizzard.getName(),
                typeClearId
        )).willReturn(Optional.empty());
        given(repository.findById(typeClearId))
                .willReturn(Optional.empty());
        given(messageProvider.getMessage(
                MESSAGE_CODE_WEATHER_TYPE_NOT_FOUND,
                typeClearId
        )).willReturn(errorMessage);

        // when
        // then
        Assertions.assertThatThrownBy(() -> underTest.update(typeClearId, typeBlizzard))
                .isInstanceOf(DataNotFoundException.class)
                .extracting(ERROR_MESSAGE_PROPERTY_NAME)
                .isEqualTo(errorMessage);
        verify(repository, never()).save(any());
    }

    @Test
    void update_withNonExistentNameAndExistentId_shouldUpdateEntity() {
        // given
        final UUID typeClearId = typeClear.getId();
        given(repository.findFirstByNameAndIdNot(
                typeBlizzard.getName(),
                typeClearId
        )).willReturn(Optional.empty());
        given(repository.findById(typeClearId))
                .willReturn(Optional.of(typeClear));

        // when
        underTest.update(typeClearId, typeBlizzard);

        // then
        verify(repository, times(1)).save(typeCaptor.capture());
        verifyNoMoreInteractions(repository);
        Assertions.assertThat(typeCaptor.getValue()).isEqualTo(typeBlizzard);
    }

    @Test
    void replace_withExistentName_shouldThrowException() {
        // given
        final UUID typeClearId = typeClear.getId();
        final String typeBlizzardName = typeBlizzard.getName();
        final String errorMessage = MessageFormat.format(
                "Weather type with name <{0}> already exists!",
                typeBlizzardName
        );
        given(repository.findFirstByNameAndIdNot(
                typeBlizzardName,
                typeClearId
        )).willReturn(Optional.of(typeBlizzard));
        given(messageProvider.getMessage(
                MESSAGE_CODE_WEATHER_TYPE_UNIQUE_NAME,
                typeBlizzardName
        )).willReturn(errorMessage);

        // when
        // then
        Assertions.assertThatThrownBy(() -> underTest.replace(typeClearId, typeBlizzard))
                .isInstanceOf(UniqueNameException.class)
                .extracting(ERROR_MESSAGE_PROPERTY_NAME)
                .isEqualTo(errorMessage);
        verify(repository, never()).save(any());
    }

    @Test
    void replace_withNonExistentNameAndNonExistentId_shouldThrowException() {
        // given
        final UUID typeClearId = typeClear.getId();
        final String errorMessage = MessageFormat.format(
                "Weather type with ID = <{0}> not found!",
                typeClearId
        );
        given(repository.findFirstByNameAndIdNot(
                typeBlizzard.getName(),
                typeClearId
        )).willReturn(Optional.empty());
        given(repository.findById(typeClearId))
                .willReturn(Optional.empty());
        given(messageProvider.getMessage(
                MESSAGE_CODE_WEATHER_TYPE_NOT_FOUND,
                typeClearId
        )).willReturn(errorMessage);

        // when
        // then
        Assertions.assertThatThrownBy(() -> underTest.replace(typeClearId, typeBlizzard))
                .isInstanceOf(DataNotFoundException.class)
                .extracting(ERROR_MESSAGE_PROPERTY_NAME)
                .isEqualTo(errorMessage);
        verify(repository, never()).save(any());
    }

    @Test
    void replace_withNonExistentNameAndExistentId_shouldReplaceEntity() {
        // given
        final UUID typeClearId = typeClear.getId();
        given(repository.findFirstByNameAndIdNot(
                typeBlizzard.getName(),
                typeClearId
        )).willReturn(Optional.empty());
        given(repository.findById(typeClearId))
                .willReturn(Optional.of(typeClear));

        // when
        underTest.replace(typeClearId, typeBlizzard);

        // then
        verify(repository, times(1))
                .save(typeCaptor.capture());
        verifyNoMoreInteractions(repository);
        Assertions.assertThat(typeCaptor.getValue())
                .isEqualTo(typeBlizzard);
    }
}