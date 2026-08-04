package pl.tomaszko.s03e01.notes;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.template.st.StTemplateRenderer;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;
import pl.tomaszko.s03e01.config.OperatorNotesPromptProperties;
import pl.tomaszko.s03e01.notes.OperatorNotesIndexer.NotesEntry;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

@Component
public class NotesPromptFactory {

    private static final Logger log = LoggerFactory.getLogger(NotesPromptFactory.class);

    private final OperatorNotesPromptProperties promptProperties;
    private final ResourceLoader resourceLoader;
    private final ObjectMapper objectMapper = JsonMapper.builder().build();

    public NotesPromptFactory(OperatorNotesPromptProperties promptProperties, ResourceLoader resourceLoader) {
        this.promptProperties = promptProperties;
        this.resourceLoader = resourceLoader;
    }

    public String systemPrompt() {
        String location = promptProperties.getPromptTemplate();
        Resource resource = resourceLoader.getResource(location);
        try (InputStream in = resource.getInputStream()) {
            String templateText = new String(in.readAllBytes(), StandardCharsets.UTF_8);
            PromptTemplate template = PromptTemplate.builder()
                    .renderer(StTemplateRenderer.builder()
                            .startDelimiterToken('<')
                            .endDelimiterToken('>')
                            .build())
                    .template(templateText)
                    .build();
            return template.render();
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to load operator-notes prompt template: " + location, ex);
        }
    }

    public String userNotesJson(List<NotesEntry> entries) {
        List<Map<String, Object>> notes = new ArrayList<>();
        for (NotesEntry entry : entries) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("nr", entry.nr());
            item.put("operator_notes", entry.operatorNotes());
            notes.add(item);
        }
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("notes", notes);
        try {
            return objectMapper.writeValueAsString(body);
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to serialize notes classification request", ex);
        }
    }

    public void logModelRequest(String systemPrompt, String userJson) {
        log.info("LLM system prompt:\n{}", systemPrompt);
        log.info("LLM tools: none");
        log.info("LLM user prompt:\n{}", userJson);
    }

    public void logModelResponse(String response) {
        log.info("LLM response:\n{}", response);
    }
}
