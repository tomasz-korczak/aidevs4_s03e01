package pl.tomaszko.s03e01.runner;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.ExitCodeGenerator;
import org.springframework.stereotype.Component;
import pl.tomaszko.s03e01.config.JsonDirProperties;
import pl.tomaszko.s03e01.report.InvalidFileReporter;
import pl.tomaszko.s03e01.scan.JsonFileScanner;
import pl.tomaszko.s03e01.scan.ScanSummary;

@Component
public class SensorScanRunner implements ApplicationRunner, ExitCodeGenerator {

    private static final Logger log = LoggerFactory.getLogger(SensorScanRunner.class);

    private final JsonDirProperties jsonDirProperties;
    private final JsonFileScanner scanner;
    private final InvalidFileReporter reporter;
    private final AtomicInteger exitCode = new AtomicInteger(1);

    public SensorScanRunner(
            JsonDirProperties jsonDirProperties, JsonFileScanner scanner, InvalidFileReporter reporter) {
        this.jsonDirProperties = jsonDirProperties;
        this.scanner = scanner;
        this.reporter = reporter;
    }

    @Override
    public void run(ApplicationArguments args) {
        try {
            if (args.getSourceArgs().length != 0) {
                log.error("No command-line arguments are allowed; use environment variable {}", JsonDirProperties.ENV_NAME);
                exitCode.set(1);
                return;
            }

            String raw = jsonDirProperties.rawValue();
            if (raw == null || raw.isBlank()) {
                log.error("Environment variable {} is missing or empty", JsonDirProperties.ENV_NAME);
                exitCode.set(1);
                return;
            }

            Path dir = Path.of(raw).toAbsolutePath().normalize();
            if (!Files.isDirectory(dir) || !Files.isReadable(dir)) {
                log.error("{} does not point to a readable directory: {}", JsonDirProperties.ENV_NAME, dir);
                exitCode.set(1);
                return;
            }

            log.info("Scanning directory: {}", dir);
            ScanSummary summary = scanner.scan(dir);

            if (summary.getFilesScanned() == 0) {
                log.error("No .json files found in {}", dir);
                exitCode.set(1);
                return;
            }

            log.info(
                    "Scan finished. scanned={} valid={} parseInvalid={} scopeInvalid={}",
                    summary.getFilesScanned(),
                    summary.getValid(),
                    summary.getParseInvalid(),
                    summary.getScopeInvalid());
            reporter.flagCaptured();
            exitCode.set(0);
        } catch (Exception ex) {
            log.error("Fatal error during scan", ex);
            exitCode.set(1);
        }
    }

    @Override
    public int getExitCode() {
        return exitCode.get();
    }
}
