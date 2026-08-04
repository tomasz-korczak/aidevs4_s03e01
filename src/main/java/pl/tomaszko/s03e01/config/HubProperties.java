package pl.tomaszko.s03e01.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.hub")
public class HubProperties {

    public static final String ENV_API_KEY = "HUB_API_KEY";

    private String verifyUrl = "https://hub.ag3nts.org/verify";

    public String getVerifyUrl() {
        return verifyUrl;
    }

    public void setVerifyUrl(String verifyUrl) {
        this.verifyUrl = verifyUrl;
    }

    public String apiKey() {
        return System.getenv(ENV_API_KEY);
    }
}
