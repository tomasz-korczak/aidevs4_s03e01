package pl.tomaszko.s03e01.notes;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;
import pl.tomaszko.s03e01.notes.OperatorNotesIndexer.NotesEntry;
import pl.tomaszko.s03e01.report.InvalidFileReporter;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

@Service
public class OperatorNotesClassifier {

    private static final Pattern JSON_ARRAY = Pattern.compile("\\[[\\s\\S]*]");

    private final ChatClient chatClient;
    private final NotesPromptFactory promptFactory;
    private final InvalidFileReporter reporter;
    private final ObjectMapper objectMapper = JsonMapper.builder().build();

    public OperatorNotesClassifier(
            ChatClient chatClient, NotesPromptFactory promptFactory, InvalidFileReporter reporter) {
        this.chatClient = chatClient;
        this.promptFactory = promptFactory;
        this.reporter = reporter;
    }

    public Set<Integer> classify(List<NotesEntry> entries) {
        if (entries == null || entries.isEmpty()) {
            return Collections.emptySet();
        }
        String systemPrompt = promptFactory.systemPrompt();
        String userJson = promptFactory.userNotesJson(entries);
        promptFactory.logModelRequest(systemPrompt, userJson);
        try {
            String response = chatClient
                    .prompt()
                    .system(systemPrompt)
                    .user(userJson)
                    .call()
                    .content();
            promptFactory.logModelResponse(response);
            return parseIssueNrs(response);
        } catch (ClassificationException ex) {
            reporter.error(ex.getMessage());
            throw ex;
        } catch (Exception ex) {
            String detail = "LLM classification failed: " + ex.getMessage();
            reporter.error(detail);
            throw new ClassificationException(detail, ex);
        }
    }

    Set<Integer> parseIssueNrs(String response) {
        if (response == null || response.isBlank()) {
            throw new ClassificationException("LLM returned empty response");
        }
        String candidate = response.trim();
        Matcher matcher = JSON_ARRAY.matcher(candidate);
        if (matcher.find()) {
            candidate = matcher.group();
        }
        try {
            JsonNode node = objectMapper.readTree(candidate);
            if (node == null || !node.isArray()) {
                throw new ClassificationException("LLM response is not a JSON array of nr values: " + response);
            }
            Set<Integer> nrs = new LinkedHashSet<>();
            for (JsonNode element : node) {
                if (element == null || !element.isNumber()) {
                    throw new ClassificationException("LLM response contains non-numeric nr: " + response);
                }
                nrs.add(element.asInt());
            }
            return nrs;
        } catch (ClassificationException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ClassificationException("Failed to parse LLM nr collection: " + response, ex);
        }
    }

    public static class ClassificationException extends RuntimeException {
        public ClassificationException(String message) {
            super(message);
        }

        public ClassificationException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
