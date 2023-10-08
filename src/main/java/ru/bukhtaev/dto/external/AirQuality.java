package ru.bukhtaev.dto.external;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Информация о качестве воздуха.
 */
@Schema(description = "Информация о качестве воздуха")
@Getter
public class AirQuality implements Serializable {

    /**
     * Оксид углерода (мкг/м<sup>3</sup>).
     */
    @Schema(description = "Оксид углерода (мкг/м3)")
    @JsonProperty("co")
    private BigDecimal co;

    /**
     * Озон (мкг/м<sup>3</sup>).
     */
    @Schema(description = "Озон (мкг/м3)")
    @JsonProperty("no2")
    private BigDecimal no2;

    /**
     * Диоксид азота (мкг/м<sup>3</sup>).
     */
    @Schema(description = "Диоксид азота (мкг/м3)")
    @JsonProperty("o3")
    private BigDecimal o3;

    /**
     * Диоксид серы (мкг/м<sup>3</sup>).
     */
    @Schema(description = "Диоксид серы (мкг/м3)")
    @JsonProperty("so2")
    private Integer so2;

    /**
     * PM2.5 (мкг/м<sup>3</sup>).
     */
    @Schema(description = "PM2.5 (мкг/м3)")
    @JsonProperty("pm2_5")
    private BigDecimal pm25;

    /**
     * PM10 (мкг/м<sup>3</sup>).
     */
    @Schema(description = "PM10 (мкг/м3)")
    @JsonProperty("pm10")
    private Integer pm10;

    /**
     * Стандарт качества воздуха Агентства по охране окружающей среды США.
     * <ul>
     * <li>1 означает Хороший</li>
     * <li>2 означает Умеренный</li>
     * <li>3 означает Вредный для здоровья для чувствительной группы</li>
     * <li>4 означает Вредный для здоровья</li>
     * <li>5 означает Очень вредный для здоровья</li>
     * <li>6 означает Опасный</li>
     * </ul>
     */
    @Schema(description = "Стандарт качества воздуха АООС США (1...6)")
    @JsonProperty("us-epa-index")
    private Integer usEpaIndex;

    /**
     * Индекс загрязнения воздуха DEFRA Великобритании.
     * <table border="10">
     *   <tr>
     *     <td>Значение</td>
     *     <td>1</td>
     *     <td>2</td>
     *     <td>3</td>
     *     <td>4</td>
     *     <td>5</td>
     *     <td>6</td>
     *     <td>7</td>
     *     <td>8</td>
     *     <td>9</td>
     *     <td>10</td>
     *   </tr>
     *   <tr>
     *     <td>Диапазон загрязнения</td>
     *     <td>Низкое</td>
     *     <td>Низкое</td>
     *     <td>Низкое</td>
     *     <td>Умеренное</td>
     *     <td>Умеренное</td>
     *     <td>Умеренное</td>
     *     <td>Высокое</td>
     *     <td>Высокое</td>
     *     <td>Высокое</td>
     *     <td>Очень высокое</td>
     *   </tr>
     *   <tr>
     *     <td>Содержание (мкг<sup>-3</sup>)</td>
     *     <td>0-11</td>
     *     <td>12-23</td>
     *     <td>24-35</td>
     *     <td>36-41</td>
     *     <td>42-47</td>
     *     <td>48-53</td>
     *     <td>54-58</td>
     *     <td>59-64</td>
     *     <td>65-70</td>
     *     <td>71 и больше</td>
     *   </tr>
     * </table>
     */
    @Schema(description = "Индекс загрязнения воздуха DEFRA Великобритании (1...10)")
    @JsonProperty("gb-defra-index")
    private Integer gbDefraIndex;
}
