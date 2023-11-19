package ru.bukhtaev.service.crud;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import ru.bukhtaev.exception.DataNotFoundException;
import ru.bukhtaev.exception.UniqueNameException;
import ru.bukhtaev.model.City;
import ru.bukhtaev.repository.jpa.ICityJpaRepository;
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
import static ru.bukhtaev.validation.MessageUtils.MESSAGE_CODE_CITY_NOT_FOUND;
import static ru.bukhtaev.validation.MessageUtils.MESSAGE_CODE_CITY_UNIQUE_NAME;

/**
 * Модульные тесты для JPA-реализации сервиса CRUD операций
 * над городами {@link CityCrudServiceJpaImpl}.
 */
class CityCrudServiceJpaImplTest extends AbstractServiceTest {

    /**
     * Имитация сервиса предоставления сообщений.
     */
    @Mock
    private MessageProvider messageProvider;

    /**
     * Имитация JPA-репозитория городов.
     */
    @Mock
    private ICityJpaRepository repository;

    /**
     * Тестируемая JPA-реализация сервиса CRUD операций над городами.
     */
    @InjectMocks
    private CityCrudServiceJpaImpl underTest;

    private City cityKazan;
    private City cityYekaterinburg;

    @BeforeEach
    void setUp() {
        cityKazan = City.builder()
                .name("Казань")
                .build();
        cityYekaterinburg = City.builder()
                .name("Екатеринбург")
                .build();
    }

    @Test
    void getById_withExistentId_shouldReturnEntity() {
        // given
        final UUID cityKazanId = cityKazan.getId();
        given(repository.findById(cityKazanId))
                .willReturn(Optional.of(cityKazan));

        // when
        underTest.getById(cityKazanId);

        // then
        verify(repository, times(1))
                .findById(idCaptor.capture());
        verifyNoMoreInteractions(repository);
        assertThat(idCaptor.getValue())
                .isEqualTo(cityKazanId);
    }

    @Test
    void getById_withNonExistentId_shouldThrowException() {
        // given
        final UUID cityYekaterinburgId = cityYekaterinburg.getId();
        final String errorMessage = MessageFormat.format(
                "City with ID = <{0}> not found!",
                cityYekaterinburgId
        );
        given(repository.findById(cityYekaterinburgId))
                .willReturn(Optional.empty());
        given(messageProvider.getMessage(
                MESSAGE_CODE_CITY_NOT_FOUND,
                cityYekaterinburgId
        )).willReturn(errorMessage);

        // when
        // then
        assertThatThrownBy(() -> underTest.getById(cityYekaterinburgId))
                .isInstanceOf(DataNotFoundException.class)
                .extracting(ERROR_MESSAGE_PROPERTY_NAME)
                .isEqualTo(errorMessage);

        verify(repository, times(1))
                .findById(idCaptor.capture());
        verifyNoMoreInteractions(repository);
        assertThat(idCaptor.getValue())
                .isEqualTo(cityYekaterinburgId);
    }

    @Test
    void getByName_shouldReturnEntity() {
        // given
        final String cityKazanName = cityKazan.getName();

        // when
        underTest.getByName(cityKazanName);

        // then
        verify(repository, times(1))
                .findFirstByName(stringCaptor.capture());
        verifyNoMoreInteractions(repository);
        assertThat(stringCaptor.getValue())
                .isEqualTo(cityKazanName);
    }

    @Test
    void getAll_shouldReturnAllEntities() {
        // given
        given(repository.findAll())
                .willReturn(List.of(cityKazan, cityYekaterinburg));

        // when
        underTest.getAll();

        // then
        verify(repository, times(1)).findAll();
        verifyNoMoreInteractions(repository);
    }

    @Test
    void create_withNonExistentName_shouldCreateEntity() {
        // given
        given(repository.findFirstByName(cityKazan.getName()))
                .willReturn(Optional.empty());

        // when
        underTest.create(cityKazan);

        // then
        verify(repository, times(1))
                .save(cityCaptor.capture());
        assertThat(cityCaptor.getValue())
                .isEqualTo(cityKazan);
    }

    @Test
    void create_withExistentName_shouldThrowException() {
        // given
        final String cityYekaterinburgName = cityYekaterinburg.getName();
        final String errorMessage = MessageFormat.format(
                "City with name <{0}> already exists!",
                cityYekaterinburgName
        );
        given(repository.findFirstByName(cityYekaterinburg.getName()))
                .willReturn(Optional.of(cityYekaterinburg));
        given(messageProvider.getMessage(
                MESSAGE_CODE_CITY_UNIQUE_NAME,
                cityYekaterinburgName
        )).willReturn(errorMessage);

        // when
        // then
        assertThatThrownBy(() -> underTest.create(cityYekaterinburg))
                .isInstanceOf(UniqueNameException.class)
                .extracting(ERROR_MESSAGE_PROPERTY_NAME)
                .isEqualTo(errorMessage);
        verify(repository, never()).save(any());
    }

    @Test
    void delete_shouldDeleteEntity() {
        // given
        final UUID cityKazanId = cityKazan.getId();

        // when
        underTest.delete(cityKazanId);

        // then
        verify(repository, times(1))
                .deleteById(idCaptor.capture());
        verifyNoMoreInteractions(repository);
        assertThat(idCaptor.getValue()).isEqualTo(cityKazanId);
    }

    @Test
    void update_withExistentName_shouldThrowException() {
        // given
        final UUID cityKazanId = cityKazan.getId();
        final String cityYekaterinburgName = cityYekaterinburg.getName();
        final String errorMessage = MessageFormat.format(
                "City with name <{0}> already exists!",
                cityYekaterinburgName
        );
        given(repository.findFirstByNameAndIdNot(
                cityYekaterinburgName,
                cityKazanId
        )).willReturn(Optional.of(cityYekaterinburg));
        given(messageProvider.getMessage(
                MESSAGE_CODE_CITY_UNIQUE_NAME,
                cityYekaterinburgName
        )).willReturn(errorMessage);

        // when
        // then
        Assertions.assertThatThrownBy(() -> underTest.update(cityKazanId, cityYekaterinburg))
                .isInstanceOf(UniqueNameException.class)
                .extracting(ERROR_MESSAGE_PROPERTY_NAME)
                .isEqualTo(errorMessage);
        verify(repository, never()).save(any());
    }

    @Test
    void update_withNonExistentNameAndNonExistentId_shouldThrowException() {
        // given
        final UUID cityKazanId = cityKazan.getId();
        final String errorMessage = MessageFormat.format(
                "City with ID = <{0}> not found!",
                cityKazanId
        );
        given(repository.findFirstByNameAndIdNot(
                cityYekaterinburg.getName(),
                cityKazanId
        )).willReturn(Optional.empty());
        given(repository.findById(cityKazanId))
                .willReturn(Optional.empty());
        given(messageProvider.getMessage(
                MESSAGE_CODE_CITY_NOT_FOUND,
                cityKazanId
        )).willReturn(errorMessage);

        // when
        // then
        Assertions.assertThatThrownBy(() -> underTest.update(cityKazanId, cityYekaterinburg))
                .isInstanceOf(DataNotFoundException.class)
                .extracting(ERROR_MESSAGE_PROPERTY_NAME)
                .isEqualTo(errorMessage);
        verify(repository, never()).save(any());
    }

    @Test
    void update_withNonExistentNameAndExistentId_shouldUpdateEntity() {
        // given
        final UUID cityKazanId = cityKazan.getId();
        given(repository.findFirstByNameAndIdNot(
                cityYekaterinburg.getName(),
                cityKazanId
        )).willReturn(Optional.empty());
        given(repository.findById(cityKazanId))
                .willReturn(Optional.of(cityKazan));

        // when
        underTest.update(cityKazanId, cityYekaterinburg);

        // then
        verify(repository, times(1)).save(cityCaptor.capture());
        verifyNoMoreInteractions(repository);
        Assertions.assertThat(cityCaptor.getValue()).isEqualTo(cityYekaterinburg);
    }

    @Test
    void replace_withExistentName_shouldThrowException() {
        // given
        final UUID cityKazanId = cityKazan.getId();
        final String cityYekaterinburgName = cityYekaterinburg.getName();
        final String errorMessage = MessageFormat.format(
                "City with name <{0}> already exists!",
                cityYekaterinburgName
        );
        given(repository.findFirstByNameAndIdNot(
                cityYekaterinburgName,
                cityKazanId
        )).willReturn(Optional.of(cityYekaterinburg));
        given(messageProvider.getMessage(
                MESSAGE_CODE_CITY_UNIQUE_NAME,
                cityYekaterinburgName
        )).willReturn(errorMessage);

        // when
        // then
        Assertions.assertThatThrownBy(() -> underTest.replace(cityKazanId, cityYekaterinburg))
                .isInstanceOf(UniqueNameException.class)
                .extracting(ERROR_MESSAGE_PROPERTY_NAME)
                .isEqualTo(errorMessage);
        verify(repository, never()).save(any());
    }

    @Test
    void replace_withNonExistentNameAndNonExistentId_shouldThrowException() {
        // given
        final UUID cityKazanId = cityKazan.getId();
        final String errorMessage = MessageFormat.format(
                "City with ID = <{0}> not found!",
                cityKazanId
        );
        given(repository.findFirstByNameAndIdNot(
                cityYekaterinburg.getName(),
                cityKazanId
        )).willReturn(Optional.empty());
        given(repository.findById(cityKazanId))
                .willReturn(Optional.empty());
        given(messageProvider.getMessage(
                MESSAGE_CODE_CITY_NOT_FOUND,
                cityKazanId
        )).willReturn(errorMessage);

        // when
        // then
        Assertions.assertThatThrownBy(() -> underTest.replace(cityKazanId, cityYekaterinburg))
                .isInstanceOf(DataNotFoundException.class)
                .extracting(ERROR_MESSAGE_PROPERTY_NAME)
                .isEqualTo(errorMessage);
        verify(repository, never()).save(any());
    }

    @Test
    void replace_withNonExistentNameAndExistentId_shouldReplaceEntity() {
        // given
        final UUID cityKazanId = cityKazan.getId();
        given(repository.findFirstByNameAndIdNot(
                cityYekaterinburg.getName(),
                cityKazanId
        )).willReturn(Optional.empty());
        given(repository.findById(cityKazanId))
                .willReturn(Optional.of(cityKazan));

        // when
        underTest.replace(cityKazanId, cityYekaterinburg);

        // then
        verify(repository, times(1))
                .save(cityCaptor.capture());
        verifyNoMoreInteractions(repository);
        Assertions.assertThat(cityCaptor.getValue())
                .isEqualTo(cityYekaterinburg);
    }
}