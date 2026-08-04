package pl.tomaszko.s03e01.runner;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.ExitCodeGenerator;
import org.springframework.stereotype.Component;
import pl.tomaszko.s03e01.config.HubProperties;
import pl.tomaszko.s03e01.config.JsonDirProperties;
import pl.tomaszko.s03e01.config.OpenRouterProperties;
import pl.tomaszko.s03e01.hub.HubVerificationService;
import pl.tomaszko.s03e01.hub.HubVerificationService.HubVerifyOutcome;
import pl.tomaszko.s03e01.notes.OperatorNotesClassifier;
import pl.tomaszko.s03e01.notes.OperatorNotesClassifier.ClassificationException;
import pl.tomaszko.s03e01.notes.OperatorNotesIndexer;
import pl.tomaszko.s03e01.notes.OperatorNotesIndexer.NotesEntry;
import pl.tomaszko.s03e01.notes.OperatorNotesReclassifier;
import pl.tomaszko.s03e01.report.InvalidFileReporter;
import pl.tomaszko.s03e01.scan.JsonFileScanner;
import pl.tomaszko.s03e01.scan.ScanSummary;

@Component
public class SensorScanRunner implements ApplicationRunner, ExitCodeGenerator {

    private static final Logger log = LoggerFactory.getLogger(SensorScanRunner.class);

    private final JsonDirProperties jsonDirProperties;
    private final OpenRouterProperties openRouterProperties;
    private final HubProperties hubProperties;
    private final JsonFileScanner scanner;
    private final InvalidFileReporter reporter;
    private final OperatorNotesIndexer notesIndexer;
    private final OperatorNotesClassifier notesClassifier;
    private final OperatorNotesReclassifier notesReclassifier;
    private final HubVerificationService hubVerificationService;
    private final AtomicInteger exitCode = new AtomicInteger(1);

    public SensorScanRunner(
            JsonDirProperties jsonDirProperties,
            OpenRouterProperties openRouterProperties,
            HubProperties hubProperties,
            JsonFileScanner scanner,
            InvalidFileReporter reporter,
            OperatorNotesIndexer notesIndexer,
            OperatorNotesClassifier notesClassifier,
            OperatorNotesReclassifier notesReclassifier,
            HubVerificationService hubVerificationService) {
        this.jsonDirProperties = jsonDirProperties;
        this.openRouterProperties = openRouterProperties;
        this.hubProperties = hubProperties;
        this.scanner = scanner;
        this.reporter = reporter;
        this.notesIndexer = notesIndexer;
        this.notesClassifier = notesClassifier;
        this.notesReclassifier = notesReclassifier;
        this.hubVerificationService = hubVerificationService;
    }

    @Override
    public void run(ApplicationArguments args) {
        try {
            if (args.getSourceArgs().length != 0) {
                reporter.error("No command-line arguments are allowed; use environment variable " + JsonDirProperties.ENV_NAME);
                exitCode.set(1);
                return;
            }

            String openRouterKey = openRouterProperties.apiKey();
            if (openRouterKey == null || openRouterKey.isBlank()) {
                reporter.error("Environment variable " + OpenRouterProperties.ENV_API_KEY + " is missing or empty");
                exitCode.set(1);
                return;
            }

            String hubKey = hubProperties.apiKey();
            if (hubKey == null || hubKey.isBlank()) {
                reporter.error("Environment variable " + HubProperties.ENV_API_KEY + " is missing or empty");
                exitCode.set(1);
                return;
            }

            String raw = jsonDirProperties.rawValue();
            if (raw == null || raw.isBlank()) {
                reporter.error("Environment variable " + JsonDirProperties.ENV_NAME + " is missing or empty");
                exitCode.set(1);
                return;
            }

            Path dir = Path.of(raw).toAbsolutePath().normalize();
            if (!Files.isDirectory(dir) || !Files.isReadable(dir)) {
                reporter.error(JsonDirProperties.ENV_NAME + " does not point to a readable directory: " + dir);
                exitCode.set(1);
                return;
            }

            log.info("Scanning directory: {}", dir);
            ScanSummary summary = scanner.scan(dir);

            if (summary.getFilesScanned() == 0) {
                reporter.error("No .json files found in " + dir);
                exitCode.set(1);
                return;
            }

            if (!summary.getValidFiles().isEmpty()) {
                List<NotesEntry> entries = notesIndexer.index(summary.getValidFiles());
                Set<Integer> issueNrs = notesClassifier.classify(entries);
                notesReclassifier.apply(summary, entries, issueNrs);
            }

            log.info(
                    "Scan finished. scanned={} valid={} parseInvalid={} scopeInvalid={} operatorInvalid={}",
                    summary.getFilesScanned(),
                    summary.getValid(),
                    summary.getParseInvalid(),
                    summary.getScopeInvalid(),
                    summary.getOperatorInvalid());

            HubVerifyOutcome outcome = hubVerificationService.verify(summary.allInvalidFiles());
            if (outcome.success()) {
                reporter.flagCaptured();
                reporter.flagToken(outcome.flagToken());
                exitCode.set(0);
            } else {
                reporter.error(outcome.rawBody());
                exitCode.set(1);
            }
        } catch (ClassificationException ex) {
            exitCode.set(1);
        } catch (Exception ex) {
            reporter.error("Fatal error during scan: " + ex.getMessage());
            log.error("Fatal error during scan", ex);
            exitCode.set(1);
        }
    }

    @Override
    public int getExitCode() {
        return exitCode.get();
    }
}
