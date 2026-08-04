package pl.tomaszko.s03e01.validation;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

public enum SensorType {
    humidity("humidity_percent"),
    temperature("temperature_K"),
    water("water_level_meters"),
    pressure("pressure_bar"),
    voltage("voltage_supply_v");

    private static final Map<String, SensorType> BY_TOKEN =
            Arrays.stream(values()).collect(Collectors.toUnmodifiableMap(Enum::name, Function.identity()));

    private final String readingField;

    SensorType(String readingField) {
        this.readingField = readingField;
    }

    public String readingField() {
        return readingField;
    }

    public static Optional<SensorType> fromToken(String token) {
        if (token == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(BY_TOKEN.get(token));
    }
}
