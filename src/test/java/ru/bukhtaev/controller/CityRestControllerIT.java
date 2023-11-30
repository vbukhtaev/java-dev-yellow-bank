package ru.bukhtaev.controller;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
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
import static ru.bukhtaev.controller.CityRestController.URL_API_V1_CITIES;

/**
 * Интеграционные тесты для CRUD операций над городами.
 */
class CityRestControllerIT extends AbstractIntegrationTest {

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
    @WithMockUser(authorities = "cities:read")
    void getAll_withReadAuthority_shouldReturnAllEntities() throws Exception {
        // given
        repository.save(mapper.convertFromDto(cityKazan));
        assertThat(repository.findAll()).hasSize(1);
        final var requestBuilder = get(URL_API_V1_CITIES);

        // when
        mockMvc.perform(requestBuilder)

                // then
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", is(cityKazan.getName())));
    }

    @Test
    @WithMockUser
    void getAll_withoutReadAuthority_accessShouldBeDenied() throws Exception {
        // given
        repository.save(mapper.convertFromDto(cityKazan));
        assertThat(repository.findAll()).hasSize(1);
        final var requestBuilder = get(URL_API_V1_CITIES);

        // when
        mockMvc.perform(requestBuilder)

                // then
                .andExpect(status().isForbidden());
    }

    @Test
    @WithAnonymousUser
    void getAll_withoutAuthentication_accessShouldBeDenied() throws Exception {
        // given
        repository.save(mapper.convertFromDto(cityKazan));
        assertThat(repository.findAll()).hasSize(1);
        final var requestBuilder = get(URL_API_V1_CITIES);

        // when
        mockMvc.perform(requestBuilder)

                // then
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(authorities = "cities:read")
    void getById_withExistentId_shouldReturnFoundEntity() throws Exception {
        // given
        final City saved = repository.save(mapper.convertFromDto(cityKazan));
        assertThat(repository.findAll()).hasSize(1);
        final var requestBuilder = get(URL_API_V1_CITIES + "/{id}", saved.getId());

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
    @WithMockUser
    void getById_withoutReadAuthority_accessShouldBeDenied() throws Exception {
        // given
        final City saved = repository.save(mapper.convertFromDto(cityKazan));
        assertThat(repository.findAll()).hasSize(1);
        final var requestBuilder = get(URL_API_V1_CITIES + "/{id}", saved.getId());

        // when
        mockMvc.perform(requestBuilder)

                // then
                .andExpect(status().isForbidden());
    }

    @Test
    @WithAnonymousUser
    void getById_withoutAuthentication_accessShouldBeDenied() throws Exception {
        // given
        final City saved = repository.save(mapper.convertFromDto(cityKazan));
        assertThat(repository.findAll()).hasSize(1);
        final var requestBuilder = get(URL_API_V1_CITIES + "/{id}", saved.getId());

        // when
        mockMvc.perform(requestBuilder)

                // then
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(authorities = "cities:read")
    void getById_withNonExistentId_shouldReturnError() throws Exception {
        // given
        repository.save(mapper.convertFromDto(cityKazan));
        assertThat(repository.findAll()).hasSize(1);
        final String nonExistentId = UUID.randomUUID().toString();
        final var requestBuilder = get(URL_API_V1_CITIES + "/{id}", nonExistentId);

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
    @WithMockUser(authorities = "cities:write")
    void create_withNonExistentName_shouldReturnCreatedEntity() throws Exception {
        // given
        repository.save(mapper.convertFromDto(cityYekaterinburg));
        assertThat(repository.findAll()).hasSize(1);
        final String jsonRequest = objectMapper.writeValueAsString(cityKazan);
        final var requestBuilder = post(URL_API_V1_CITIES)
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
    @WithMockUser(authorities = "cities:read")
    void create_withoutWriteAuthority_accessShouldBeDenied() throws Exception {
        // given
        repository.save(mapper.convertFromDto(cityYekaterinburg));
        assertThat(repository.findAll()).hasSize(1);
        final String jsonRequest = objectMapper.writeValueAsString(cityKazan);
        final var requestBuilder = post(URL_API_V1_CITIES)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest);

        // when
        mockMvc.perform(requestBuilder)

                // then
                .andExpect(status().isForbidden());

        assertThat(repository.findAll()).hasSize(1);
    }

    @Test
    @WithAnonymousUser
    void create_withoutAuthentication_accessShouldBeDenied() throws Exception {
        // given
        repository.save(mapper.convertFromDto(cityYekaterinburg));
        assertThat(repository.findAll()).hasSize(1);
        final String jsonRequest = objectMapper.writeValueAsString(cityKazan);
        final var requestBuilder = post(URL_API_V1_CITIES)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest);

        // when
        mockMvc.perform(requestBuilder)

                // then
                .andExpect(status().isUnauthorized());

        assertThat(repository.findAll()).hasSize(1);
    }

    @Test
    @WithMockUser(authorities = "cities:write")
    void create_withExistentName_shouldReturnError() throws Exception {
        // given
        repository.save(mapper.convertFromDto(cityKazan));
        assertThat(repository.findAll()).hasSize(1);
        final String jsonRequest = objectMapper.writeValueAsString(cityKazan);
        final var requestBuilder = post(URL_API_V1_CITIES)
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
    @WithMockUser(authorities = "cities:write")
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
        final var requestBuilder = put(URL_API_V1_CITIES + "/{id}", saved.getId())
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
    @WithMockUser(authorities = "cities:read")
    void replace_withoutWriteAuthority_accessShouldBeDenied() throws Exception {
        // given
        repository.save(mapper.convertFromDto(cityKazan));
        final City saved = repository.save(mapper.convertFromDto(cityYekaterinburg));
        assertThat(repository.findAll()).hasSize(2);
        final String newName = "Новосибирск";
        final NameableRequestDto dto = NameableRequestDto.builder()
                .name(newName)
                .build();
        final String jsonRequest = objectMapper.writeValueAsString(dto);
        final var requestBuilder = put(URL_API_V1_CITIES + "/{id}", saved.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest);

        // when
        mockMvc.perform(requestBuilder)

                // then
                .andExpect(status().isForbidden());

        final Optional<City> optCity = repository.findById(saved.getId());
        assertThat(optCity).isPresent();
        assertThat(optCity.get().getName())
                .isEqualTo(cityYekaterinburg.getName());
    }

    @Test
    @WithAnonymousUser
    void replace_withoutAuthentication_accessShouldBeDenied() throws Exception {
        // given
        repository.save(mapper.convertFromDto(cityKazan));
        final City saved = repository.save(mapper.convertFromDto(cityYekaterinburg));
        assertThat(repository.findAll()).hasSize(2);
        final String newName = "Новосибирск";
        final NameableRequestDto dto = NameableRequestDto.builder()
                .name(newName)
                .build();
        final String jsonRequest = objectMapper.writeValueAsString(dto);
        final var requestBuilder = put(URL_API_V1_CITIES + "/{id}", saved.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest);

        // when
        mockMvc.perform(requestBuilder)

                // then
                .andExpect(status().isUnauthorized());

        final Optional<City> optCity = repository.findById(saved.getId());
        assertThat(optCity).isPresent();
        assertThat(optCity.get().getName())
                .isEqualTo(cityYekaterinburg.getName());
    }

    @Test
    @WithMockUser(authorities = "cities:write")
    void replace_withExistentName_shouldReturnError() throws Exception {
        // given
        repository.save(mapper.convertFromDto(cityKazan));
        final City saved = repository.save(mapper.convertFromDto(cityYekaterinburg));
        assertThat(repository.findAll()).hasSize(2);
        final NameableRequestDto dto = NameableRequestDto.builder()
                .name(cityKazan.getName())
                .build();
        final String jsonRequest = objectMapper.writeValueAsString(dto);
        final var requestBuilder = put(URL_API_V1_CITIES + "/{id}", saved.getId())
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
    @WithMockUser(authorities = "cities:write")
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
        final var requestBuilder = patch(URL_API_V1_CITIES + "/{id}", saved.getId())
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
    @WithMockUser(authorities = "cities:read")
    void update_withoutWriteAuthority_accessShouldBeDenied() throws Exception {
        // given
        repository.save(mapper.convertFromDto(cityKazan));
        final City saved = repository.save(mapper.convertFromDto(cityYekaterinburg));
        assertThat(repository.findAll()).hasSize(2);
        final String newName = "Новосибирск";
        final NameableRequestDto dto = NameableRequestDto.builder()
                .name(newName)
                .build();
        final String jsonRequest = objectMapper.writeValueAsString(dto);
        final var requestBuilder = patch(URL_API_V1_CITIES + "/{id}", saved.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest);

        // when
        mockMvc.perform(requestBuilder)

                // then
                .andExpect(status().isForbidden());

        final Optional<City> optCity = repository.findById(saved.getId());
        assertThat(optCity).isPresent();
        assertThat(optCity.get().getName())
                .isEqualTo(cityYekaterinburg.getName());
    }

    @Test
    @WithAnonymousUser
    void update_withoutAuthentication_accessShouldBeDenied() throws Exception {
        // given
        repository.save(mapper.convertFromDto(cityKazan));
        final City saved = repository.save(mapper.convertFromDto(cityYekaterinburg));
        assertThat(repository.findAll()).hasSize(2);
        final String newName = "Новосибирск";
        final NameableRequestDto dto = NameableRequestDto.builder()
                .name(newName)
                .build();
        final String jsonRequest = objectMapper.writeValueAsString(dto);
        final var requestBuilder = patch(URL_API_V1_CITIES + "/{id}", saved.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest);

        // when
        mockMvc.perform(requestBuilder)

                // then
                .andExpect(status().isUnauthorized());

        final Optional<City> optCity = repository.findById(saved.getId());
        assertThat(optCity).isPresent();
        assertThat(optCity.get().getName())
                .isEqualTo(cityYekaterinburg.getName());
    }

    @Test
    @WithMockUser(authorities = "cities:write")
    void update_withExistentName_shouldReturnError() throws Exception {
        // given
        repository.save(mapper.convertFromDto(cityYekaterinburg));
        assertThat(repository.findAll()).hasSize(1);
        final NameableRequestDto dto = NameableRequestDto.builder()
                .name(cityYekaterinburg.getName())
                .build();

        final UUID cityKazanId = repository.save(mapper.convertFromDto(cityKazan)).getId();
        final String jsonRequest = objectMapper.writeValueAsString(dto);
        final var requestBuilder = patch(URL_API_V1_CITIES + "/{id}", cityKazanId)
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
    @WithMockUser(authorities = "cities:write")
    void delete_shouldDeleteEntityAndReturnStatusNoContent() throws Exception {
        // given
        final UUID cityKazanId = repository.save(mapper.convertFromDto(cityKazan)).getId();
        assertThat(repository.findAll()).hasSize(1);
        final var requestBuilder = delete(URL_API_V1_CITIES + "/{id}", cityKazanId);

        // when
        mockMvc.perform(requestBuilder)

                // then
                .andExpect(status().isNoContent());

        assertThat(repository.findAll()).isEmpty();
    }

    @Test
    @WithMockUser(authorities = "cities:read")
    void delete_withoutWriteAuthority_accessShouldBeDenied() throws Exception {
        // given
        final UUID cityKazanId = repository.save(mapper.convertFromDto(cityKazan)).getId();
        assertThat(repository.findAll()).hasSize(1);
        final var requestBuilder = delete(URL_API_V1_CITIES + "/{id}", cityKazanId);

        // when
        mockMvc.perform(requestBuilder)

                // then
                .andExpect(status().isForbidden());

        assertThat(repository.findAll()).hasSize(1);
    }

    @Test
    @WithAnonymousUser
    void delete_withoutAuthentication_accessShouldBeDenied() throws Exception {
        // given
        final UUID cityKazanId = repository.save(mapper.convertFromDto(cityKazan)).getId();
        assertThat(repository.findAll()).hasSize(1);
        final var requestBuilder = delete(URL_API_V1_CITIES + "/{id}", cityKazanId);

        // when
        mockMvc.perform(requestBuilder)

                // then
                .andExpect(status().isUnauthorized());

        assertThat(repository.findAll()).hasSize(1);
    }
}
