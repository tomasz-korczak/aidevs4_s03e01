package pl.tomaszko.s03e01.config;

import org.springframework.stereotype.Component;

@Component
public class JsonDirProperties {

    public static final String ENV_NAME = "JSON_DIR";

    public String rawValue() {
        return System.getenv(ENV_NAME);
    }
}
