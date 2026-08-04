package pl.tomaszko.s03e01.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.llm")
public class OpenRouterProperties {

    public static final String ENV_API_KEY = "OPENROUTER_API_KEY";

    private String model = "nvidia/nemotron-3-ultra-550b-a55b:free";

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String apiKey() {
        return System.getenv(ENV_API_KEY);
    }
}
