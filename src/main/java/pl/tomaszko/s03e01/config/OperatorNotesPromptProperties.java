package pl.tomaszko.s03e01.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.operator-notes")
public class OperatorNotesPromptProperties {

    private String promptTemplate = "classpath:prompts/operator-notes-system.st";

    public String getPromptTemplate() {
        return promptTemplate;
    }

    public void setPromptTemplate(String promptTemplate) {
        this.promptTemplate = promptTemplate;
    }
}
