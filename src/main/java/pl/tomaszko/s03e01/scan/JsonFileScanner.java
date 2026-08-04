package pl.tomaszko.s03e01.scan;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Component;
import pl.tomaszko.s03e01.model.SensorReading;
import pl.tomaszko.s03e01.report.InvalidFileReporter;
import pl.tomaszko.s03e01.validation.SensorReadingValidator;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

@Component
public class JsonFileScanner {

    private final SensorReadingValidator validator;
    private final InvalidFileReporter reporter;
    private final ObjectMapper objectMapper;

    public JsonFileScanner(SensorReadingValidator validator, InvalidFileReporter reporter) {
        this.validator = validator;
        this.reporter = reporter;
        this.objectMapper = JsonMapper.builder()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, true)
                .build();
    }

    public ScanSummary scan(Path directory) throws IOException {
        ScanSummary summary = new ScanSummary();
        List<Path> files = listJsonFiles(directory);
        for (Path file : files) {
            summary.incrementScanned();
            categorize(file, summary);
        }
        return summary;
    }

    public List<Path> listJsonFiles(Path directory) throws IOException {
        List<Path> files = new ArrayList<>();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(directory, "*.json")) {
            for (Path path : stream) {
                if (Files.isRegularFile(path)) {
                    files.add(path);
                }
            }
        }
        files.sort(Comparator.comparing(p -> p.getFileName().toString()));
        return files;
    }

    private void categorize(Path file, ScanSummary summary) {
        String basename = file.getFileName().toString();
        try {
            JsonNode root = objectMapper.readTree(file.toFile());
            if (root == null || !root.isObject()) {
                reporter.parse(basename);
                summary.incrementParseInvalid();
                return;
            }
            SensorReading reading = objectMapper.treeToValue(root, SensorReading.class);
            if (reading == null || !validator.isValid(reading)) {
                reporter.scope(basename);
                summary.incrementScopeInvalid();
                return;
            }
            summary.incrementValid();
        } catch (Exception ex) {
            reporter.parse(basename);
            summary.incrementParseInvalid();
        }
    }
}
