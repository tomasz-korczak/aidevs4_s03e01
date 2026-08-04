package pl.tomaszko.s03e01.hub;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import pl.tomaszko.s03e01.config.HubProperties;
import pl.tomaszko.s03e01.scan.ClassifiedFile;

@Service
public class HubVerificationService {

    private static final Pattern FLAG_PATTERN = Pattern.compile("\\{FLG:.*?\\}");

    private final HubProperties hubProperties;
    private final RestClient.Builder restClientBuilder;

    public HubVerificationService(HubProperties hubProperties, RestClient.Builder restClientBuilder) {
        this.hubProperties = hubProperties;
        this.restClientBuilder = restClientBuilder;
    }

    public HubVerifyOutcome verify(List<ClassifiedFile> invalidFiles) {
        List<String> recheck = invalidFiles.stream().map(ClassifiedFile::getStem).toList();
        HubVerifyRequest request = new HubVerifyRequest(hubProperties.apiKey(), "evaluation", new Answer(recheck));
        try {
            String body = restClientBuilder
                    .build()
                    .post()
                    .uri(hubProperties.getVerifyUrl())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .body(String.class);
            return fromBody(body == null ? "" : body);
        } catch (Exception ex) {
            return HubVerifyOutcome.failure(ex.getMessage() == null ? ex.toString() : ex.getMessage());
        }
    }

    HubVerifyOutcome fromBody(String body) {
        Matcher matcher = FLAG_PATTERN.matcher(body);
        if (matcher.find()) {
            return HubVerifyOutcome.success(matcher.group(), body);
        }
        return HubVerifyOutcome.failure(body);
    }

    public record HubVerifyRequest(String apikey, String task, Answer answer) {}

    public record Answer(List<String> recheck) {}

    public record HubVerifyOutcome(boolean success, String flagToken, String rawBody) {
        public static HubVerifyOutcome success(String flagToken, String rawBody) {
            return new HubVerifyOutcome(true, flagToken, rawBody);
        }

        public static HubVerifyOutcome failure(String rawBody) {
            return new HubVerifyOutcome(false, null, rawBody);
        }
    }
}
