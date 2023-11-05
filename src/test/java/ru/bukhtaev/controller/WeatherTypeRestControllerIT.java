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
import ru.bukhtaev.dto.mapper.IWeatherTypeMapper;
import ru.bukhtaev.model.WeatherType;
import ru.bukhtaev.repository.jpa.IWeatherTypeJpaRepository;

import java.text.MessageFormat;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static ru.bukhtaev.controller.WeatherTypeRestController.URL_API_V1_WEATHER_TYPES;

/**
 * Интеграционные тесты для CRUD операций над типами погоды.
 */
class WeatherTypeRestControllerIT extends AbstractIntegrationTest {

    /**
     * Маппер для DTO типов погоды.
     */
    @Autowired
    private IWeatherTypeMapper mapper;

    /**
     * Репозиторий.
     */
    @Autowired
    private IWeatherTypeJpaRepository repository;

    private NameableRequestDto typeClear;
    private NameableRequestDto typeBlizzard;

    @BeforeEach
    void setUp() {
        typeClear = NameableRequestDto.builder()
                .name("Ясно")
                .build();
        typeBlizzard = NameableRequestDto.builder()
                .name("Метель")
                .build();
    }

    @AfterEach
    void tearDown() {
        repository.deleteAll();
    }

    @Test
    @WithMockUser(authorities = "weather-types:read")
    void getAll_shouldReturnAllEntities() throws Exception {
        // given
        repository.save(mapper.convertFromDto(typeClear));
        assertThat(repository.findAll()).hasSize(1);
        final var requestBuilder = get(URL_API_V1_WEATHER_TYPES);

        // when
        mockMvc.perform(requestBuilder)

                // then
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", is(typeClear.getName())));
    }

    @Test
    @WithMockUser
    void getAll_withoutReadAuthority_accessShouldBeDenied() throws Exception {
        // given
        repository.save(mapper.convertFromDto(typeClear));
        assertThat(repository.findAll()).hasSize(1);
        final var requestBuilder = get(URL_API_V1_WEATHER_TYPES);

        // when
        mockMvc.perform(requestBuilder)

                // then
                .andExpect(status().isForbidden());
    }

    @Test
    @WithAnonymousUser
    void getAll_withoutAuthentication_accessShouldBeDenied() throws Exception {
        // given
        repository.save(mapper.convertFromDto(typeClear));
        assertThat(repository.findAll()).hasSize(1);
        final var requestBuilder = get(URL_API_V1_WEATHER_TYPES);

        // when
        mockMvc.perform(requestBuilder)

                // then
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(authorities = "weather-types:read")
    void getById_withExistentId_shouldReturnFoundEntity() throws Exception {
        // given
        final WeatherType saved = repository.save(mapper.convertFromDto(typeClear));
        assertThat(repository.findAll()).hasSize(1);
        final var requestBuilder = get(URL_API_V1_WEATHER_TYPES + "/{id}", saved.getId());

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
        final WeatherType saved = repository.save(mapper.convertFromDto(typeClear));
        assertThat(repository.findAll()).hasSize(1);
        final var requestBuilder = get(URL_API_V1_WEATHER_TYPES + "/{id}", saved.getId());

        // when
        mockMvc.perform(requestBuilder)

                // then
                .andExpect(status().isForbidden());
    }

    @Test
    @WithAnonymousUser
    void getById_withoutAuthentication_accessShouldBeDenied() throws Exception {
        // given
        final WeatherType saved = repository.save(mapper.convertFromDto(typeClear));
        assertThat(repository.findAll()).hasSize(1);
        final var requestBuilder = get(URL_API_V1_WEATHER_TYPES + "/{id}", saved.getId());

        // when
        mockMvc.perform(requestBuilder)

                // then
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(authorities = "weather-types:read")
    void getById_withNonExistentId_shouldReturnError() throws Exception {
        // given
        repository.save(mapper.convertFromDto(typeClear));
        assertThat(repository.findAll()).hasSize(1);
        final String nonExistentId = UUID.randomUUID().toString();
        final var requestBuilder = get(URL_API_V1_WEATHER_TYPES + "/{id}", nonExistentId);

        // when
        mockMvc.perform(requestBuilder)

                // then
                .andExpectAll(
                        status().isNotFound(),
                        content().contentType(MediaType.APPLICATION_JSON),
                        jsonPath("$.violations", hasSize(1)),
                        jsonPath("$.violations[0].message", is(
                                MessageFormat.format(
                                        "Weather type with ID = <{0}> not found!",
                                        nonExistentId
                                )
                        ))
                );
    }

    @Test
    @WithMockUser(authorities = "weather-types:write")
    void create_withNonExistentName_shouldReturnCreatedEntity() throws Exception {
        // given
        repository.save(mapper.convertFromDto(typeBlizzard));
        assertThat(repository.findAll()).hasSize(1);
        final String jsonRequest = objectMapper.writeValueAsString(typeClear);
        final var requestBuilder = post(URL_API_V1_WEATHER_TYPES)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest);

        // when
        mockMvc.perform(requestBuilder)

                // then
                .andExpectAll(
                        status().isCreated(),
                        header().exists(HttpHeaders.LOCATION),
                        content().contentType(MediaType.APPLICATION_JSON),
                        jsonPath("$.name", is(typeClear.getName()))
                );

        final List<WeatherType> types = repository.findAll();
        assertThat(types).hasSize(2);
        final WeatherType type = types.get(1);
        assertThat(type.getId()).isNotNull();
        assertThat(type.getName()).isEqualTo(typeClear.getName());
    }

    @Test
    @WithMockUser(authorities = "weather-types:read")
    void create_withoutWriteAuthority_accessShouldBeDenied() throws Exception {
        // given
        repository.save(mapper.convertFromDto(typeBlizzard));
        assertThat(repository.findAll()).hasSize(1);
        final String jsonRequest = objectMapper.writeValueAsString(typeClear);
        final var requestBuilder = post(URL_API_V1_WEATHER_TYPES)
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
        repository.save(mapper.convertFromDto(typeBlizzard));
        assertThat(repository.findAll()).hasSize(1);
        final String jsonRequest = objectMapper.writeValueAsString(typeClear);
        final var requestBuilder = post(URL_API_V1_WEATHER_TYPES)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest);

        // when
        mockMvc.perform(requestBuilder)

                // then
                .andExpect(status().isUnauthorized());

        assertThat(repository.findAll()).hasSize(1);
    }

    @Test
    @WithMockUser(authorities = "weather-types:write")
    void create_withExistentName_shouldReturnError() throws Exception {
        // given
        repository.save(mapper.convertFromDto(typeClear));
        assertThat(repository.findAll()).hasSize(1);
        final String jsonRequest = objectMapper.writeValueAsString(typeClear);
        final var requestBuilder = post(URL_API_V1_WEATHER_TYPES)
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
                                        "Weather type with name <{0}> already exists!",
                                        typeClear.getName()
                                )
                        ))
                );

        assertThat(repository.findAll()).hasSize(1);
    }

    @Test
    @WithMockUser(authorities = "weather-types:write")
    void replace_withNonExistentName_shouldReturnReplacedEntity() throws Exception {
        // given
        repository.save(mapper.convertFromDto(typeClear));
        final WeatherType saved = repository.save(mapper.convertFromDto(typeBlizzard));
        assertThat(repository.findAll()).hasSize(2);
        final String newName = "Пасмурно";
        final NameableRequestDto dto = NameableRequestDto.builder()
                .name(newName)
                .build();
        final String jsonRequest = objectMapper.writeValueAsString(dto);
        final var requestBuilder = put(URL_API_V1_WEATHER_TYPES + "/{id}", saved.getId())
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

        final Optional<WeatherType> optWeatherType = repository.findById(saved.getId());
        assertThat(optWeatherType).isPresent();
        assertThat(optWeatherType.get().getName())
                .isEqualTo(newName);
    }

    @Test
    @WithMockUser(authorities = "weather-types:read")
    void replace_withoutWriteAuthority_accessShouldBeDenied() throws Exception {
        // given
        repository.save(mapper.convertFromDto(typeClear));
        final WeatherType saved = repository.save(mapper.convertFromDto(typeBlizzard));
        assertThat(repository.findAll()).hasSize(2);
        final String newName = "Пасмурно";
        final NameableRequestDto dto = NameableRequestDto.builder()
                .name(newName)
                .build();
        final String jsonRequest = objectMapper.writeValueAsString(dto);
        final var requestBuilder = put(URL_API_V1_WEATHER_TYPES + "/{id}", saved.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest);

        // when
        mockMvc.perform(requestBuilder)

                // then
                .andExpect(status().isForbidden());

        final Optional<WeatherType> optWeatherType = repository.findById(saved.getId());
        assertThat(optWeatherType).isPresent();
        assertThat(optWeatherType.get().getName())
                .isEqualTo(typeBlizzard.getName());
    }

    @Test
    @WithAnonymousUser
    void replace_withoutAuthentication_accessShouldBeDenied() throws Exception {
        // given
        repository.save(mapper.convertFromDto(typeClear));
        final WeatherType saved = repository.save(mapper.convertFromDto(typeBlizzard));
        assertThat(repository.findAll()).hasSize(2);
        final String newName = "Пасмурно";
        final NameableRequestDto dto = NameableRequestDto.builder()
                .name(newName)
                .build();
        final String jsonRequest = objectMapper.writeValueAsString(dto);
        final var requestBuilder = put(URL_API_V1_WEATHER_TYPES + "/{id}", saved.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest);

        // when
        mockMvc.perform(requestBuilder)

                // then
                .andExpect(status().isUnauthorized());

        final Optional<WeatherType> optWeatherType = repository.findById(saved.getId());
        assertThat(optWeatherType).isPresent();
        assertThat(optWeatherType.get().getName())
                .isEqualTo(typeBlizzard.getName());
    }

    @Test
    @WithMockUser(authorities = "weather-types:write")
    void replace_withExistentName_shouldReturnError() throws Exception {
        // given
        repository.save(mapper.convertFromDto(typeClear));
        final WeatherType saved = repository.save(mapper.convertFromDto(typeBlizzard));
        assertThat(repository.findAll()).hasSize(2);
        final NameableRequestDto dto = NameableRequestDto.builder()
                .name(typeClear.getName())
                .build();
        final String jsonRequest = objectMapper.writeValueAsString(dto);
        final var requestBuilder = put(URL_API_V1_WEATHER_TYPES + "/{id}", saved.getId())
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
                                        "Weather type with name <{0}> already exists!",
                                        typeClear.getName()
                                )
                        ))
                );

        final Optional<WeatherType> optWeatherType = repository.findById(saved.getId());
        assertThat(optWeatherType).isPresent();
        assertThat(optWeatherType.get().getName())
                .isEqualTo(typeBlizzard.getName());
    }

    @Test
    @WithMockUser(authorities = "weather-types:write")
    void update_withNonExistentName_shouldReturnUpdatedEntity() throws Exception {
        // given
        repository.save(mapper.convertFromDto(typeClear));
        final WeatherType saved = repository.save(mapper.convertFromDto(typeBlizzard));
        assertThat(repository.findAll()).hasSize(2);
        final String newName = "Пасмурно";
        final NameableRequestDto dto = NameableRequestDto.builder()
                .name(newName)
                .build();
        final String jsonRequest = objectMapper.writeValueAsString(dto);
        final var requestBuilder = patch(URL_API_V1_WEATHER_TYPES + "/{id}", saved.getId())
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

        final Optional<WeatherType> optWeatherType = repository.findById(saved.getId());
        assertThat(optWeatherType).isPresent();
        assertThat(optWeatherType.get().getName())
                .isEqualTo(newName);
    }

    @Test
    @WithMockUser(authorities = "weather-types:read")
    void update_withoutWriteAuthority_accessShouldBeDenied() throws Exception {
        // given
        repository.save(mapper.convertFromDto(typeClear));
        final WeatherType saved = repository.save(mapper.convertFromDto(typeBlizzard));
        assertThat(repository.findAll()).hasSize(2);
        final String newName = "Пасмурно";
        final NameableRequestDto dto = NameableRequestDto.builder()
                .name(newName)
                .build();
        final String jsonRequest = objectMapper.writeValueAsString(dto);
        final var requestBuilder = patch(URL_API_V1_WEATHER_TYPES + "/{id}", saved.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest);

        // when
        mockMvc.perform(requestBuilder)

                // then
                .andExpect(status().isForbidden());

        final Optional<WeatherType> optWeatherType = repository.findById(saved.getId());
        assertThat(optWeatherType).isPresent();
        assertThat(optWeatherType.get().getName())
                .isEqualTo(typeBlizzard.getName());
    }

    @Test
    @WithAnonymousUser
    void update_withoutAuthentication_accessShouldBeDenied() throws Exception {
        // given
        repository.save(mapper.convertFromDto(typeClear));
        final WeatherType saved = repository.save(mapper.convertFromDto(typeBlizzard));
        assertThat(repository.findAll()).hasSize(2);
        final String newName = "Пасмурно";
        final NameableRequestDto dto = NameableRequestDto.builder()
                .name(newName)
                .build();
        final String jsonRequest = objectMapper.writeValueAsString(dto);
        final var requestBuilder = patch(URL_API_V1_WEATHER_TYPES + "/{id}", saved.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest);

        // when
        mockMvc.perform(requestBuilder)

                // then
                .andExpect(status().isUnauthorized());

        final Optional<WeatherType> optWeatherType = repository.findById(saved.getId());
        assertThat(optWeatherType).isPresent();
        assertThat(optWeatherType.get().getName())
                .isEqualTo(typeBlizzard.getName());
    }

    @Test
    @WithMockUser(authorities = "weather-types:write")
    void update_withExistentName_shouldReturnError() throws Exception {
        // given
        repository.save(mapper.convertFromDto(typeBlizzard));
        assertThat(repository.findAll()).hasSize(1);
        final NameableRequestDto dto = NameableRequestDto.builder()
                .name(typeBlizzard.getName())
                .build();

        final UUID typeClearId = repository.save(mapper.convertFromDto(typeClear)).getId();
        final String jsonRequest = objectMapper.writeValueAsString(dto);
        final var requestBuilder = patch(URL_API_V1_WEATHER_TYPES + "/{id}", typeClearId)
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
                                        "Weather type with name <{0}> already exists!",
                                        typeBlizzard.getName()
                                )
                        ))
                );

        final Optional<WeatherType> optWeatherType = repository.findById(typeClearId);
        assertThat(optWeatherType).isPresent();
        assertThat(optWeatherType.get().getName())
                .isEqualTo(typeClear.getName());
    }

    @Test
    @WithMockUser(authorities = "weather-types:write")
    void delete_shouldDeleteEntityAndReturnStatusNoContent() throws Exception {
        // given
        final UUID typeClearId = repository.save(mapper.convertFromDto(typeClear)).getId();
        assertThat(repository.findAll()).hasSize(1);
        final var requestBuilder = delete(URL_API_V1_WEATHER_TYPES + "/{id}", typeClearId);

        // when
        mockMvc.perform(requestBuilder)

                // then
                .andExpect(status().isNoContent());

        assertThat(repository.findAll()).isEmpty();
    }

    @Test
    @WithMockUser(authorities = "weather-types:read")
    void delete_withoutWriteAuthority_accessShouldBeDenied() throws Exception {
        // given
        final UUID typeClearId = repository.save(mapper.convertFromDto(typeClear)).getId();
        assertThat(repository.findAll()).hasSize(1);
        final var requestBuilder = delete(URL_API_V1_WEATHER_TYPES + "/{id}", typeClearId);

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
        final UUID typeClearId = repository.save(mapper.convertFromDto(typeClear)).getId();
        assertThat(repository.findAll()).hasSize(1);
        final var requestBuilder = delete(URL_API_V1_WEATHER_TYPES + "/{id}", typeClearId);

        // when
        mockMvc.perform(requestBuilder)

                // then
                .andExpect(status().isUnauthorized());

        assertThat(repository.findAll()).hasSize(1);
    }
}
