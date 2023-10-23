package ru.bukhtaev.controller;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import ru.bukhtaev.dto.NameableRequestDto;
import ru.bukhtaev.dto.mapper.ICityMapper;
import ru.bukhtaev.model.City;
import ru.bukhtaev.repository.jpa.ICityJpaRepository;

import java.text.MessageFormat;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Интеграционные тесты для CRUD операций над городами.
 */
class CityRestControllerIT extends AbstractIntegrationTest {

    /**
     * URL.
     */
    private static final String API_V1_CITIES = "/api/v1/cities";

    /**
     * Маппер для DTO городов.
     */
    @Autowired
    private ICityMapper mapper;

    /**
     * Репозиторий.
     */
    @Autowired
    private ICityJpaRepository repository;

    private NameableRequestDto cityKazan;
    private NameableRequestDto cityYekaterinburg;

    @BeforeEach
    void setUp() {
        cityKazan = NameableRequestDto.builder()
                .name("Казань")
                .build();
        cityYekaterinburg = NameableRequestDto.builder()
                .name("Екатеринбург")
                .build();
    }

    @AfterEach
    void tearDown() {
        repository.deleteAll();
    }

    @Test
    void getAll_shouldReturnAllEntities() throws Exception {
        // given
        repository.save(mapper.convertFromDto(cityKazan));
        assertThat(repository.findAll()).hasSize(1);
        final var requestBuilder = get(API_V1_CITIES);

        // when
        mockMvc.perform(requestBuilder)

                // then
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", is(cityKazan.getName())));
    }

    @Test
    void getById_withExistentId_shouldReturnFoundEntity() throws Exception {
        // given
        final City saved = repository.save(mapper.convertFromDto(cityKazan));
        assertThat(repository.findAll()).hasSize(1);
        final var requestBuilder = get(API_V1_CITIES + "/{id}", saved.getId());

        // when
        mockMvc.perform(requestBuilder)

                // then
                .andExpectAll(
                        status().isOk(),
                        content().contentType(MediaType.APPLICATION_JSON),
                        jsonPath("$.name", is(saved.getName()))
                );
    }

    @Test
    void getById_withNonExistentId_shouldReturnError() throws Exception {
        // given
        repository.save(mapper.convertFromDto(cityKazan));
        assertThat(repository.findAll()).hasSize(1);
        final String nonExistentId = UUID.randomUUID().toString();
        final var requestBuilder = get(API_V1_CITIES + "/{id}", nonExistentId);

        // when
        mockMvc.perform(requestBuilder)

                // then
                .andExpectAll(
                        status().isNotFound(),
                        content().contentType(MediaType.APPLICATION_JSON),
                        jsonPath("$.violations", hasSize(1)),
                        jsonPath("$.violations[0].message", is(
                                MessageFormat.format(
                                        "City with ID = <{0}> not found!",
                                        nonExistentId
                                )
                        ))
                );
    }

    @Test
    void create_withNonExistentName_shouldReturnCreatedEntity() throws Exception {
        // given
        repository.save(mapper.convertFromDto(cityYekaterinburg));
        assertThat(repository.findAll()).hasSize(1);
        final String jsonRequest = objectMapper.writeValueAsString(cityKazan);
        final var requestBuilder = post(API_V1_CITIES)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest);

        // when
        mockMvc.perform(requestBuilder)

                // then
                .andExpectAll(
                        status().isCreated(),
                        header().exists(HttpHeaders.LOCATION),
                        content().contentType(MediaType.APPLICATION_JSON),
                        jsonPath("$.name", is(cityKazan.getName()))
                );

        final List<City> cities = repository.findAll();
        assertThat(cities).hasSize(2);
        final City city = cities.get(1);
        assertThat(city.getId()).isNotNull();
        assertThat(city.getName()).isEqualTo(cityKazan.getName());
    }

    @Test
    void create_withExistentName_shouldReturnError() throws Exception {
        // given
        repository.save(mapper.convertFromDto(cityKazan));
        assertThat(repository.findAll()).hasSize(1);
        final String jsonRequest = objectMapper.writeValueAsString(cityKazan);
        final var requestBuilder = post(API_V1_CITIES)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest);

        // when
        mockMvc.perform(requestBuilder)

                // then
                .andExpectAll(
                        status().isBadRequest(),
                        content().contentType(MediaType.APPLICATION_JSON),
                        jsonPath("$.violations", hasSize(1)),
                        jsonPath("$.violations[0].paramNames", contains("name")),
                        jsonPath("$.violations[0].message", is(
                                MessageFormat.format(
                                        "City with name <{0}> already exists!",
                                        cityKazan.getName()
                                )
                        ))
                );

        assertThat(repository.findAll()).hasSize(1);
    }

    @Test
    void replace_withNonExistentName_shouldReturnReplacedEntity() throws Exception {
        // given
        repository.save(mapper.convertFromDto(cityKazan));
        final City saved = repository.save(mapper.convertFromDto(cityYekaterinburg));
        assertThat(repository.findAll()).hasSize(2);
        final String newName = "Новосибирск";
        final NameableRequestDto dto = NameableRequestDto.builder()
                .name(newName)
                .build();
        final String jsonRequest = objectMapper.writeValueAsString(dto);
        final var requestBuilder = put(API_V1_CITIES + "/{id}", saved.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest);

        // when
        mockMvc.perform(requestBuilder)

                // then
                .andExpectAll(
                        status().isOk(),
                        content().contentType(MediaType.APPLICATION_JSON),
                        jsonPath("$.name", is(newName))
                );

        final Optional<City> optCity = repository.findById(saved.getId());
        assertThat(optCity).isPresent();
        assertThat(optCity.get().getName())
                .isEqualTo(newName);
    }

    @Test
    void replace_withExistentName_shouldReturnError() throws Exception {
        // given
        repository.save(mapper.convertFromDto(cityKazan));
        final City saved = repository.save(mapper.convertFromDto(cityYekaterinburg));
        assertThat(repository.findAll()).hasSize(2);
        final NameableRequestDto dto = NameableRequestDto.builder()
                .name(cityKazan.getName())
                .build();
        final String jsonRequest = objectMapper.writeValueAsString(dto);
        final var requestBuilder = put(API_V1_CITIES + "/{id}", saved.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest);

        // when
        mockMvc.perform(requestBuilder)

                // then
                .andExpectAll(
                        status().isBadRequest(),
                        content().contentType(MediaType.APPLICATION_JSON),
                        jsonPath("$.violations", hasSize(1)),
                        jsonPath("$.violations[0].paramNames", contains("name")),
                        jsonPath("$.violations[0].message", is(
                                MessageFormat.format(
                                        "City with name <{0}> already exists!",
                                        cityKazan.getName()
                                )
                        ))
                );

        final Optional<City> optCity = repository.findById(saved.getId());
        assertThat(optCity).isPresent();
        assertThat(optCity.get().getName())
                .isEqualTo(cityYekaterinburg.getName());
    }

    @Test
    void update_withNonExistentName_shouldReturnUpdatedEntity() throws Exception {
        // given
        repository.save(mapper.convertFromDto(cityKazan));
        final City saved = repository.save(mapper.convertFromDto(cityYekaterinburg));
        assertThat(repository.findAll()).hasSize(2);
        final String newName = "Новосибирск";
        final NameableRequestDto dto = NameableRequestDto.builder()
                .name(newName)
                .build();
        final String jsonRequest = objectMapper.writeValueAsString(dto);
        final var requestBuilder = patch(API_V1_CITIES + "/{id}", saved.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest);

        // when
        mockMvc.perform(requestBuilder)

                // then
                .andExpectAll(
                        status().isOk(),
                        content().contentType(MediaType.APPLICATION_JSON),
                        jsonPath("$.name", is(newName))
                );

        final Optional<City> optCity = repository.findById(saved.getId());
        assertThat(optCity).isPresent();
        assertThat(optCity.get().getName())
                .isEqualTo(newName);
    }

    @Test
    void update_withExistentName_shouldReturnError() throws Exception {
        // given
        repository.save(mapper.convertFromDto(cityYekaterinburg));
        assertThat(repository.findAll()).hasSize(1);
        final NameableRequestDto dto = NameableRequestDto.builder()
                .name(cityYekaterinburg.getName())
                .build();

        final UUID cityKazanId = repository.save(mapper.convertFromDto(cityKazan)).getId();
        final String jsonRequest = objectMapper.writeValueAsString(dto);
        final var requestBuilder = patch(API_V1_CITIES + "/{id}", cityKazanId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest);

        // when
        mockMvc.perform(requestBuilder)

                // then
                .andExpectAll(
                        status().isBadRequest(),
                        content().contentType(MediaType.APPLICATION_JSON),
                        jsonPath("$.violations", hasSize(1)),
                        jsonPath("$.violations[0].paramNames", contains("name")),
                        jsonPath("$.violations[0].message", is(
                                MessageFormat.format(
                                        "City with name <{0}> already exists!",
                                        cityYekaterinburg.getName()
                                )
                        ))
                );

        final Optional<City> optCity = repository.findById(cityKazanId);
        assertThat(optCity).isPresent();
        assertThat(optCity.get().getName())
                .isEqualTo(cityKazan.getName());
    }

    @Test
    void delete_shouldDeleteEntityAndReturnStatusNoContent() throws Exception {
        // given
        final UUID cityKazanId = repository.save(mapper.convertFromDto(cityKazan)).getId();
        assertThat(repository.findAll()).hasSize(1);
        final var requestBuilder = delete(API_V1_CITIES + "/{id}", cityKazanId);

        // when
        mockMvc.perform(requestBuilder)

                // then
                .andExpect(status().isNoContent());

        assertThat(repository.findAll()).isEmpty();
    }
}
