package pl.tomaszko.s03e01.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = false)
public class SensorReading {

    @JsonProperty("sensor_type")
    private String sensorType;

    @JsonProperty("timestamp")
    private Number timestamp;

    @JsonProperty("temperature_K")
    private Number temperatureK;

    @JsonProperty("pressure_bar")
    private Number pressureBar;

    @JsonProperty("water_level_meters")
    private Number waterLevelMeters;

    @JsonProperty("voltage_supply_v")
    private Number voltageSupplyV;

    @JsonProperty("humidity_percent")
    private Number humidityPercent;

    @JsonProperty("operator_notes")
    private String operatorNotes;

    public String getSensorType() {
        return sensorType;
    }

    public void setSensorType(String sensorType) {
        this.sensorType = sensorType;
    }

    public Number getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Number timestamp) {
        this.timestamp = timestamp;
    }

    public Number getTemperatureK() {
        return temperatureK;
    }

    public void setTemperatureK(Number temperatureK) {
        this.temperatureK = temperatureK;
    }

    public Number getPressureBar() {
        return pressureBar;
    }

    public void setPressureBar(Number pressureBar) {
        this.pressureBar = pressureBar;
    }

    public Number getWaterLevelMeters() {
        return waterLevelMeters;
    }

    public void setWaterLevelMeters(Number waterLevelMeters) {
        this.waterLevelMeters = waterLevelMeters;
    }

    public Number getVoltageSupplyV() {
        return voltageSupplyV;
    }

    public void setVoltageSupplyV(Number voltageSupplyV) {
        this.voltageSupplyV = voltageSupplyV;
    }

    public Number getHumidityPercent() {
        return humidityPercent;
    }

    public void setHumidityPercent(Number humidityPercent) {
        this.humidityPercent = humidityPercent;
    }

    public String getOperatorNotes() {
        return operatorNotes;
    }

    public void setOperatorNotes(String operatorNotes) {
        this.operatorNotes = operatorNotes;
    }
}
