package pl.tomaszko.s03e01.validation;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;
import org.springframework.stereotype.Component;
import pl.tomaszko.s03e01.model.SensorReading;

@Component
public class SensorReadingValidator {

    public boolean isValid(SensorReading reading) {
        if (reading == null) {
            return false;
        }
        if (reading.getSensorType() == null
                || reading.getTimestamp() == null
                || reading.getTemperatureK() == null
                || reading.getPressureBar() == null
                || reading.getWaterLevelMeters() == null
                || reading.getVoltageSupplyV() == null
                || reading.getHumidityPercent() == null
                || reading.getOperatorNotes() == null) {
            return false;
        }
        if (reading.getOperatorNotes().isEmpty()) {
            return false;
        }

        List<String> rawTokens = Stream.of(reading.getSensorType().split("/"))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
        if (rawTokens.isEmpty()) {
            return false;
        }
        Set<String> seen = new HashSet<>();
        Set<SensorType> active = EnumSet.noneOf(SensorType.class);
        for (String token : rawTokens) {
            if (!seen.add(token)) {
                return false;
            }
            var type = SensorType.fromToken(token);
            if (type.isEmpty()) {
                return false;
            }
            active.add(type.get());
        }

        return checkReading(ReadingField.temperature_K, reading.getTemperatureK().doubleValue(), active)
                && checkReading(ReadingField.pressure_bar, reading.getPressureBar().doubleValue(), active)
                && checkReading(ReadingField.water_level_meters, reading.getWaterLevelMeters().doubleValue(), active)
                && checkReading(ReadingField.voltage_supply_v, reading.getVoltageSupplyV().doubleValue(), active)
                && checkReading(ReadingField.humidity_percent, reading.getHumidityPercent().doubleValue(), active);
    }

    private boolean checkReading(ReadingField field, double value, Set<SensorType> active) {
        boolean expectedActive = active.stream().anyMatch(t -> Objects.equals(t.readingField(), field.name()));
        if (expectedActive) {
            if (value == 0.0) {
                return false;
            }
            return ReadingRanges.of(field).contains(value);
        }
        return value == 0.0;
    }
}
