package pl.tomaszko.s03e01.validation;

import java.util.EnumMap;
import java.util.Map;

public final class ReadingRanges {

    public record Range(double min, double max) {
        public boolean contains(double value) {
            return value >= min && value <= max;
        }
    }

    private static final Map<ReadingField, Range> RANGES = new EnumMap<>(ReadingField.class);

    static {
        RANGES.put(ReadingField.temperature_K, new Range(553, 873));
        RANGES.put(ReadingField.pressure_bar, new Range(60, 160));
        RANGES.put(ReadingField.water_level_meters, new Range(5.0, 15.0));
        RANGES.put(ReadingField.voltage_supply_v, new Range(229.0, 231.0));
        RANGES.put(ReadingField.humidity_percent, new Range(40.0, 80.0));
    }

    private ReadingRanges() {
    }

    public static Range of(ReadingField field) {
        return RANGES.get(field);
    }
}
