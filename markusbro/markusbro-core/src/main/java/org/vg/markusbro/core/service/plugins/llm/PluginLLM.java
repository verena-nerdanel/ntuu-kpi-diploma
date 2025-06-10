package org.vg.markusbro.core.service.plugins.llm;

import jakarta.annotation.PostConstruct;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.vg.markusbro.core.service.plugins.Context;
import org.vg.markusbro.core.service.plugins.AbstractPlugin;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Slf4j
@Component
public class PluginLLM extends AbstractPlugin {

    private static final String KEY_HISTORY = "history";
    private static final int MAX_VALUE_SIZE = 65536;

    private final String OPENAI_MODEL = getResource("openai.model");
    private final String OPENAI_API_KEY = getResource("openai.apiKey");
    private final int OPENAI_MAX_HISTORY_SIZE = Integer.parseInt(getResource("openai.maxHistorySize"));

    @Data
    private static class HistoryEntry {
        private long timestamp;
        private String role;
        private String message;
    }

    @PostConstruct
    private void validate() {
        if (StringUtils.isBlank(OPENAI_MODEL)) {
            throw new IllegalArgumentException("Please provide model name");
        }

        if (StringUtils.isBlank(OPENAI_API_KEY)) {
            throw new IllegalArgumentException("Please provide API key");
        }
    }

    @Override
    public String getId() {
        return "llm";
    }

    @Override
    public double getScore(Context context) {
        return context.getUser().isAccessLlm()
                ? SCORE_LAST_RESORT
                : SCORE_NEVER;
    }

    @Override
    public void handle(Context context) {
        addHistory(context, Role.user, context.getMessage());

        final List<Message> chatHistory = getHistory(context, OPENAI_MAX_HISTORY_SIZE).stream()
                .map(e -> new Message(Role.valueOf(e.getRole()), e.getMessage()))
                .toList();

        final String response = sendRequest(chatHistory);

        context.reply(response);
        addHistory(context, Role.assistant, response);

        // TODO: LLM stats
    }

    private String sendRequest(List<Message> chatHistory) {
        final ApiRequest request = new ApiRequest();
        request.setModel(OPENAI_MODEL);
        request.setInput(chatHistory);

        final ApiResponse result = RestClient.create().post()
                .uri("https://api.openai.com/v1/responses")
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + OPENAI_API_KEY)
                .body(request)
                .retrieve()
                .body(ApiResponse.class);

        return TextFormatUtils.formatText(result.getOutput()
                .get(0)
                .getContent()
                .get(0)
                .getText());
    }

    private void addHistory(Context context, Role role, String message) {
        writeValue(context,
                KEY_HISTORY + ":" + System.currentTimeMillis() + ":" + role,
                StringUtils.truncate(message, MAX_VALUE_SIZE)
        );
    }

    /**
     * Fetches last maxSize history entries; automatically removes elder entries
     *
     * @param context
     * @param maxSize
     * @return
     */
    private List<HistoryEntry> getHistory(Context context, int maxSize) {

        final List<HistoryEntry> history = new ArrayList<>(readValues(context, KEY_HISTORY).stream()
                .map(e -> {
                    final HistoryEntry entry = parseHistoryEntry(e.key());
                    entry.setMessage(e.value());
                    return entry;
                })
                .sorted(Comparator.comparing(HistoryEntry::getTimestamp))
                .toList());

        while (history.size() > maxSize) {
            removeValue(context, KEY_HISTORY + ":" + history.get(0).getTimestamp() + ":" + history.get(0).getRole());
            history.remove(0);
        }

        return history;
    }

    /**
     * Parses string in format of "history:{timestamp}:{role}"
     * <br/>
     * E.g.: "history:1749484031:user"
     *
     * @param s
     * @return
     */
    private static HistoryEntry parseHistoryEntry(String s) {
        if (StringUtils.isBlank(s) || !s.matches("^" + KEY_HISTORY + ":\\d{10,}:\\w{4,}$")) {
            throw new IllegalArgumentException("Can't parse string: [" + s + "]");
        }

        final String[] chunks = s.split(":");
        if (chunks.length != 3) {
            throw new IllegalArgumentException("Invalid string: [" + s + "]");
        }

        final HistoryEntry entry = new HistoryEntry();
        entry.setTimestamp(Long.parseLong(chunks[1]));
        entry.setRole(chunks[2]);

        return entry;
    }
}
