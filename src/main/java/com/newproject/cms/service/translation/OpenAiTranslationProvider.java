package com.newproject.cms.service.translation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.newproject.cms.config.CmsTranslationProperties;
import com.newproject.cms.dto.LocalizedContent;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class OpenAiTranslationProvider implements TranslationProvider {
    private static final Logger logger = LoggerFactory.getLogger(OpenAiTranslationProvider.class);
    private static final char[] DEFAULT_CACERTS_PASSWORD = "changeit".toCharArray();
    private static final int DEFAULT_RETRY_COUNT = 2;
    private static final long RETRY_BASE_DELAY_MS = 750L;

    private final CmsTranslationProperties properties;
    private final ObjectMapper objectMapper;

    public OpenAiTranslationProvider(CmsTranslationProperties properties, ObjectMapper objectMapper) {
        this.properties = properties;
        this.objectMapper = objectMapper;
    }

    @Override
    public TranslationResult translateInformationContent(String sourceLanguage, LocalizedContent sourceContent, Set<String> targetLanguages) {
        TranslationResult result = new TranslationResult();

        if (targetLanguages == null || targetLanguages.isEmpty()) {
            return result;
        }

        if (!properties.isEnabled()) {
            result.getWarnings().add("CMS translation is disabled");
            return result;
        }

        if (!"openai".equalsIgnoreCase(trimToNull(properties.getProvider()))) {
            result.getWarnings().add("Translation provider is not OpenAI");
            return result;
        }

        String apiKey = trimToNull(properties.getOpenai().getApiKey());
        if (apiKey == null) {
            result.getWarnings().add("OPENAI_API_KEY missing: translation skipped");
            return result;
        }

        String sourceTitle = trimToNull(sourceContent != null ? sourceContent.getTitle() : null);
        String sourceContentHtml = trimToNull(sourceContent != null ? sourceContent.getContent() : null);
        if (sourceTitle == null || sourceContentHtml == null) {
            result.getWarnings().add("Missing source information title/content");
            return result;
        }

        String model = firstNonBlank(trimToNull(properties.getModel()), "gpt-4o-mini");
        String baseUrl = normalizeBaseUrl(firstNonBlank(trimToNull(properties.getOpenai().getBaseUrl()), "https://api.openai.com/v1"));
        HttpClient client = buildOpenAiHttpClient();
        Map<String, LocalizedContent> translated = new LinkedHashMap<>();
        int attempts = Math.max(DEFAULT_RETRY_COUNT, 1);
        LinkedHashSet<String> remainingTargets = new LinkedHashSet<>();
        for (String targetLanguage : new LinkedHashSet<>(targetLanguages)) {
            if (targetLanguage != null && !targetLanguage.isBlank()) {
                remainingTargets.add(targetLanguage);
            }
        }

        if (!remainingTargets.isEmpty()) {
            try {
                Map<String, LocalizedContent> batchTranslations = translateAllTargetsWithRetry(
                    client,
                    apiKey,
                    model,
                    baseUrl,
                    sourceLanguage,
                    sourceContent,
                    remainingTargets,
                    attempts
                );
                if (batchTranslations != null && !batchTranslations.isEmpty()) {
                    translated.putAll(batchTranslations);
                    remainingTargets.removeAll(batchTranslations.keySet());
                }
            } catch (Exception ex) {
                String msg = "OpenAI CMS batch translation failed: " + ex.getMessage();
                logger.warn(msg);
                result.getWarnings().add(msg);
            }
        }

        for (String targetLanguage : remainingTargets) {
            try {
                LocalizedContent translatedContent = translateSingleTargetWithRetry(
                    client,
                    apiKey,
                    model,
                    baseUrl,
                    sourceLanguage,
                    sourceContent,
                    targetLanguage,
                    attempts
                );
                if (translatedContent != null
                    && (trimToNull(translatedContent.getTitle()) != null || trimToNull(translatedContent.getContent()) != null)) {
                    translated.put(targetLanguage, translatedContent);
                } else {
                    result.getWarnings().add("No translated content returned for language " + targetLanguage);
                }
            } catch (Exception ex) {
                String msg = "OpenAI CMS translation failed for " + targetLanguage + ": " + ex.getMessage();
                logger.warn(msg);
                result.getWarnings().add(msg);
            }
        }

        result.setTranslations(translated);
        if (translated.isEmpty()) {
            result.getWarnings().add("OpenAI response parsed but no translations were produced");
        }

        return result;
    }

    private Map<String, LocalizedContent> translateAllTargetsWithRetry(
        HttpClient client,
        String apiKey,
        String model,
        String baseUrl,
        String sourceLanguage,
        LocalizedContent sourceContent,
        Set<String> targetLanguages,
        int maxAttempts
    ) throws IOException, InterruptedException {
        Exception last = null;
        for (int attempt = 1; attempt <= Math.max(1, maxAttempts); attempt++) {
            try {
                String content = callOpenAiBatch(
                    client,
                    apiKey,
                    model,
                    baseUrl,
                    sourceLanguage,
                    sourceContent,
                    targetLanguages
                );
                return parseBatchTranslations(content, targetLanguages);
            } catch (Exception ex) {
                last = ex;
                if (attempt >= maxAttempts || !isRetryable(ex)) {
                    break;
                }
                long delayMs = RETRY_BASE_DELAY_MS * attempt;
                logger.warn(
                    "OpenAI CMS batch translation retry {}/{} for languages={} after error: {}",
                    attempt,
                    maxAttempts,
                    targetLanguages,
                    ex.getMessage()
                );
                sleepQuietly(delayMs);
            }
        }

        if (last instanceof IOException ioEx) {
            throw ioEx;
        }
        if (last instanceof InterruptedException interruptedException) {
            Thread.currentThread().interrupt();
            throw interruptedException;
        }
        if (last != null) {
            throw new IOException(last.getMessage(), last);
        }
        return Map.of();
    }

    private LocalizedContent translateSingleTargetWithRetry(
        HttpClient client,
        String apiKey,
        String model,
        String baseUrl,
        String sourceLanguage,
        LocalizedContent sourceContent,
        String targetLanguage,
        int maxAttempts
    ) throws IOException, InterruptedException {
        Exception last = null;
        for (int attempt = 1; attempt <= Math.max(1, maxAttempts); attempt++) {
            try {
                String content = callOpenAi(
                    client,
                    apiKey,
                    model,
                    baseUrl,
                    sourceLanguage,
                    sourceContent,
                    targetLanguage
                );
                return parseSingleTranslation(content, targetLanguage);
            } catch (Exception ex) {
                last = ex;
                if (attempt >= maxAttempts || !isRetryable(ex)) {
                    break;
                }
                long delayMs = RETRY_BASE_DELAY_MS * attempt;
                logger.warn(
                    "OpenAI CMS translation retry {}/{} for lang={} after error: {}",
                    attempt,
                    maxAttempts,
                    targetLanguage,
                    ex.getMessage()
                );
                sleepQuietly(delayMs);
            }
        }

        if (last instanceof IOException ioEx) {
            throw ioEx;
        }
        if (last instanceof InterruptedException interruptedException) {
            Thread.currentThread().interrupt();
            throw interruptedException;
        }
        if (last != null) {
            throw new IOException(last.getMessage(), last);
        }
        return null;
    }

    private String callOpenAiBatch(
        HttpClient client,
        String apiKey,
        String model,
        String baseUrl,
        String sourceLanguage,
        LocalizedContent sourceContent,
        Set<String> targetLanguages
    ) throws IOException, InterruptedException {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("model", model);
        payload.put("temperature", 0.0);
        payload.put("max_tokens", 7000);
        payload.put("response_format", Map.of("type", "json_object"));
        payload.put("messages", List.of(
            Map.of(
                "role", "system",
                "content", "You translate ecommerce informational pages. Return ONLY a valid JSON object with schema {\"translations\":{\"en\":{\"title\":\"...\",\"content\":\"...\"}}}. Keep HTML structure and links valid. Preserve brand names and legal references. Escape quotes, backslashes, tabs and new lines with standard JSON escaping."
            ),
            Map.of(
                "role", "user",
                "content", buildBatchUserPrompt(sourceLanguage, sourceContent, targetLanguages)
            )
        ));

        String body = objectMapper.writeValueAsString(payload);

        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(baseUrl + "/chat/completions"))
            .timeout(Duration.ofMillis(Math.max(1000, properties.getTimeoutMs())))
            .header("Authorization", "Bearer " + apiKey)
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(body))
            .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() >= 400) {
            throw new IOException("HTTP " + response.statusCode() + " from OpenAI: " + abbreviate(trimToNull(response.body()), 240));
        }

        JsonNode root = objectMapper.readTree(response.body());
        String content = root.path("choices").path(0).path("message").path("content").asText();
        String trimmed = trimToNull(content);
        if (trimmed == null) {
            throw new IOException("Empty OpenAI content");
        }
        return trimmed;
    }

    private String callOpenAi(
        HttpClient client,
        String apiKey,
        String model,
        String baseUrl,
        String sourceLanguage,
        LocalizedContent sourceContent,
        String targetLanguage
    ) throws IOException, InterruptedException {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("model", model);
        payload.put("temperature", 0.0);
        payload.put("max_tokens", 2400);
        payload.put("response_format", Map.of("type", "json_object"));
        payload.put("messages", List.of(
            Map.of(
                "role", "system",
                "content", "You translate ecommerce informational pages. Return ONLY a valid JSON object with fields title and content. Keep HTML structure and links valid. Preserve brand names and legal references. Escape quotes, backslashes, tabs and new lines with standard JSON escaping."
            ),
            Map.of(
                "role", "user",
                "content", buildUserPrompt(sourceLanguage, sourceContent, targetLanguage)
            )
        ));

        String body = objectMapper.writeValueAsString(payload);

        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(baseUrl + "/chat/completions"))
            .timeout(Duration.ofMillis(Math.max(1000, properties.getTimeoutMs())))
            .header("Authorization", "Bearer " + apiKey)
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(body))
            .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() >= 400) {
            throw new IOException("HTTP " + response.statusCode() + " from OpenAI: " + abbreviate(trimToNull(response.body()), 240));
        }

        JsonNode root = objectMapper.readTree(response.body());
        String content = root.path("choices").path(0).path("message").path("content").asText();
        String trimmed = trimToNull(content);
        if (trimmed == null) {
            throw new IOException("Empty OpenAI content");
        }
        return trimmed;
    }

    private HttpClient buildOpenAiHttpClient() {
        HttpClient.Builder builder = HttpClient.newBuilder()
            .connectTimeout(Duration.ofMillis(Math.max(1000, properties.getTimeoutMs())));

        SSLContext context = loadJvmDefaultCacertsContext();
        if (context != null) {
            builder.sslContext(context);
        }

        return builder.build();
    }

    private SSLContext loadJvmDefaultCacertsContext() {
        String javaHome = trimToNull(System.getProperty("java.home"));
        if (javaHome == null) {
            return null;
        }

        List<Path> candidates = List.of(
            Path.of(javaHome, "lib", "security", "cacerts"),
            Path.of(javaHome, "jre", "lib", "security", "cacerts")
        );

        for (Path candidate : candidates) {
            if (!Files.isRegularFile(candidate)) {
                continue;
            }
            try (InputStream in = Files.newInputStream(candidate)) {
                KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
                keyStore.load(in, DEFAULT_CACERTS_PASSWORD);

                TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
                trustManagerFactory.init(keyStore);

                SSLContext sslContext = SSLContext.getInstance("TLS");
                sslContext.init(null, trustManagerFactory.getTrustManagers(), new SecureRandom());
                return sslContext;
            } catch (Exception ex) {
                logger.debug("Unable to load JVM cacerts from {}: {}", candidate, ex.getMessage());
            }
        }

        logger.warn("Unable to load JVM default cacerts; OpenAI HTTPS calls will use process truststore config");
        return null;
    }

    private Map<String, LocalizedContent> parseBatchTranslations(String content, Set<String> targetLanguages) throws IOException {
        String jsonPayload = extractJsonPayload(content);
        JsonNode root = readModelJson(jsonPayload);

        JsonNode translationsNode = root.path("translations");
        if (!translationsNode.isObject()) {
            Map<String, LocalizedContent> single = new LinkedHashMap<>();
            if (targetLanguages.size() == 1) {
                String onlyLanguage = targetLanguages.iterator().next();
                LocalizedContent translated = parseSingleTranslation(content, onlyLanguage);
                if (translated != null) {
                    single.put(onlyLanguage, translated);
                }
            }
            return single;
        }

        Map<String, LocalizedContent> parsed = new LinkedHashMap<>();
        for (String targetLanguage : targetLanguages) {
            JsonNode candidate = translationsNode.path(targetLanguage);
            if (!candidate.isObject()) {
                continue;
            }
            LocalizedContent localized = new LocalizedContent();
            localized.setTitle(trimToNull(candidate.path("title").asText(null)));
            localized.setContent(trimToNull(candidate.path("content").asText(null)));
            if (localized.getTitle() != null || localized.getContent() != null) {
                parsed.put(targetLanguage, localized);
            }
        }
        return parsed;
    }

    private LocalizedContent parseSingleTranslation(String content, String targetLanguage) throws IOException {
        String jsonPayload = extractJsonPayload(content);
        JsonNode root = readModelJson(jsonPayload);

        JsonNode candidate = root.has("translations") && root.get("translations").isObject()
            ? root.get("translations")
            : root;

        if (candidate.has(targetLanguage) && candidate.get(targetLanguage).isObject()) {
            candidate = candidate.get(targetLanguage);
        }

        if (!candidate.isObject()) {
            return null;
        }

        LocalizedContent localized = new LocalizedContent();
        localized.setTitle(trimToNull(candidate.path("title").asText(null)));
        localized.setContent(trimToNull(candidate.path("content").asText(null)));

        if (localized.getTitle() == null && localized.getContent() == null) {
            return null;
        }
        return localized;
    }



    private JsonNode readModelJson(String jsonPayload) throws IOException {
        try {
            return objectMapper.readTree(jsonPayload);
        } catch (IOException ex) {
            String sanitized = sanitizeModelJson(jsonPayload);
            if (!sanitized.equals(jsonPayload)) {
                return objectMapper.readTree(sanitized);
            }
            throw ex;
        }
    }

    private String sanitizeModelJson(String value) {
        StringBuilder sanitized = new StringBuilder(value.length() + 32);
        boolean inString = false;
        boolean escaping = false;

        for (int i = 0; i < value.length(); i++) {
            char current = value.charAt(i);

            if (escaping) {
                if (!isValidJsonEscape(current)) {
                    sanitized.append('\\');
                }
                sanitized.append(current);
                escaping = false;
                continue;
            }

            if (current == '"') {
                sanitized.append(current);
                inString = !inString;
                continue;
            }

            if (inString && current == '\\') {
                sanitized.append(current);
                escaping = true;
                continue;
            }

            if (inString && current < 0x20) {
                sanitized.append(escapeControlCharacter(current));
                continue;
            }

            sanitized.append(current);
        }

        if (escaping) {
            sanitized.append('\\');
        }

        return sanitized.toString();
    }

    private boolean isValidJsonEscape(char value) {
        return value == '"' || value == '\\' || value == '/' || value == 'b' || value == 'f'
            || value == 'n' || value == 'r' || value == 't' || value == 'u';
    }

    private String escapeControlCharacter(char value) {
        return switch (value) {
            case '\n' -> "\\n";
            case '\r' -> "\\r";
            case '\t' -> "\\t";
            case '\b' -> "\\b";
            case '\f' -> "\\f";
            default -> String.format("\\u%04x", (int) value);
        };
    }
    private String extractJsonPayload(String content) {
        String trimmed = content.trim();
        if (trimmed.startsWith("```") && trimmed.contains("\n")) {
            int start = trimmed.indexOf('\n') + 1;
            int end = trimmed.lastIndexOf("```");
            if (end > start) {
                trimmed = trimmed.substring(start, end).trim();
            }
        }

        int firstBrace = trimmed.indexOf('{');
        int lastBrace = trimmed.lastIndexOf('}');
        if (firstBrace >= 0 && lastBrace > firstBrace) {
            return trimmed.substring(firstBrace, lastBrace + 1);
        }
        return trimmed;
    }

    private String buildBatchUserPrompt(String sourceLanguage, LocalizedContent sourceContent, Set<String> targetLanguages) {
        String sourceTitle = firstNonBlank(trimToNull(sourceContent.getTitle()), "");
        String sourceHtml = firstNonBlank(trimToNull(sourceContent.getContent()), "");

        return "Source language: " + sourceLanguage + "\n"
            + "Target languages: " + String.join(", ", targetLanguages) + "\n"
            + "Title: " + sourceTitle + "\n"
            + "HTML content: " + sourceHtml + "\n"
            + "Return JSON only with schema: {\"translations\":{\"en\":{\"title\":\"...\",\"content\":\"...\"}}}."
            + " Include every requested target language exactly once."
            + " Keep the HTML valid and preserve links.";
    }

    private String buildUserPrompt(String sourceLanguage, LocalizedContent sourceContent, String targetLanguage) {
        String sourceTitle = firstNonBlank(trimToNull(sourceContent.getTitle()), "");
        String sourceHtml = firstNonBlank(trimToNull(sourceContent.getContent()), "");

        return "Source language: " + sourceLanguage + "\n"
            + "Target language: " + targetLanguage + "\n"
            + "Title: " + sourceTitle + "\n"
            + "HTML content: " + sourceHtml + "\n"
            + "Return JSON only with schema: {\"title\":\"...\",\"content\":\"...\"}."
            + " Keep the HTML valid and preserve links.";
    }

    private boolean isRetryable(Exception ex) {
        if (ex instanceof InterruptedException) {
            return false;
        }
        if (ex instanceof HttpTimeoutException) {
            return true;
        }
        String message = trimToNull(ex.getMessage());
        return message != null
            && (message.toLowerCase().contains("timed out")
            || message.toLowerCase().contains("timeout")
            || message.toLowerCase().contains("connection reset"));
    }

    private void sleepQuietly(long delayMs) throws InterruptedException {
        try {
            TimeUnit.MILLISECONDS.sleep(Math.max(0L, delayMs));
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw ex;
        }
    }

    private String normalizeBaseUrl(String baseUrl) {
        String normalized = baseUrl.trim();
        while (normalized.endsWith("/")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        return normalized;
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return "";
    }

    private String abbreviate(String value, int maxLen) {
        if (value == null) {
            return "";
        }
        if (value.length() <= maxLen) {
            return value;
        }
        return value.substring(0, maxLen) + "...";
    }
}
