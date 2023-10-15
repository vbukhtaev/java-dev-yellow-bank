package ru.bukhtaev.repository.jdbc;

import org.springframework.jdbc.core.RowMapper;
import ru.bukhtaev.model.City;
import ru.bukhtaev.model.Weather;
import ru.bukhtaev.model.WeatherType;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Маппер для объектов типа {@link Weather}.
 */
public class WeatherRowMapper implements RowMapper<Weather> {

    @Override
    public Weather mapRow(ResultSet rs, int rowNum) throws SQLException {

        final UUID cityId = UUID.fromString(rs.getString("city_id"));
        final String cityName = rs.getString("city_name");
        final City city = City.builder()
                .id(cityId)
                .name(cityName)
                .build();

        final UUID typeId = UUID.fromString(rs.getString("type_id"));
        final String typeName = rs.getString("type_name");
        final WeatherType type = WeatherType.builder()
                .id(typeId)
                .name(typeName)
                .build();

        final UUID id = UUID.fromString(rs.getString("id"));
        final Double temperature = rs.getDouble("temperature");
        final LocalDateTime dateTime = rs.getObject("date_time", LocalDateTime.class);
        return Weather.builder()
                .id(id)
                .city(city)
                .type(type)
                .temperature(temperature)
                .dateTime(dateTime)
                .build();
    }
}
