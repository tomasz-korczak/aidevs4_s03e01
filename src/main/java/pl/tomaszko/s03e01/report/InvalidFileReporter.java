package pl.tomaszko.s03e01.report;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class InvalidFileReporter {

    private static final Logger log = LoggerFactory.getLogger(InvalidFileReporter.class);

    public void parse(String basename) {
        log.warn("PARSE: {}", basename);
    }

    public void scope(String basename) {
        log.warn("SCOPE: {}", basename);
    }

    public void operator(String basename) {
        log.warn("OPERATOR: {}", basename);
    }

    public void flagCaptured() {
        log.info("FLAG: captured");
    }

    public void flagToken(String token) {
        log.info("FLAG: {}", token);
    }

    public void error(String detail) {
        log.error("ERROR: {}", detail);
    }
}
