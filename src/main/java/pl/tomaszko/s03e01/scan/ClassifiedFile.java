package pl.tomaszko.s03e01.scan;

import pl.tomaszko.s03e01.model.SensorReading;

public class ClassifiedFile {

    private final String basename;
    private final String stem;
    private FileCategory category;
    private final SensorReading reading;

    public ClassifiedFile(String basename, String stem, FileCategory category, SensorReading reading) {
        this.basename = basename;
        this.stem = stem;
        this.category = category;
        this.reading = reading;
    }

    public static String stemOf(String basename) {
        if (basename != null && basename.toLowerCase().endsWith(".json")) {
            return basename.substring(0, basename.length() - 5);
        }
        return basename;
    }

    public String getBasename() {
        return basename;
    }

    public String getStem() {
        return stem;
    }

    public FileCategory getCategory() {
        return category;
    }

    public void setCategory(FileCategory category) {
        this.category = category;
    }

    public SensorReading getReading() {
        return reading;
    }
}
