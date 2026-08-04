package pl.tomaszko.s03e01.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({HubProperties.class, OpenRouterProperties.class, OperatorNotesPromptProperties.class})
public class AppConfigProperties {
}
