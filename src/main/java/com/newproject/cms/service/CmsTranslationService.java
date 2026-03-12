package com.newproject.cms.service;

import com.newproject.cms.config.CmsTranslationProperties;
import com.newproject.cms.dto.LocalizedContent;
import com.newproject.cms.service.translation.NoopTranslationProvider;
import com.newproject.cms.service.translation.OpenAiTranslationProvider;
import com.newproject.cms.service.translation.TranslationResult;
import java.util.Set;
import org.springframework.stereotype.Service;

@Service
public class CmsTranslationService {
    private final CmsTranslationProperties properties;
    private final OpenAiTranslationProvider openAiTranslationProvider;
    private final NoopTranslationProvider noopTranslationProvider;

    public CmsTranslationService(
        CmsTranslationProperties properties,
        OpenAiTranslationProvider openAiTranslationProvider,
        NoopTranslationProvider noopTranslationProvider
    ) {
        this.properties = properties;
        this.openAiTranslationProvider = openAiTranslationProvider;
        this.noopTranslationProvider = noopTranslationProvider;
    }

    public TranslationResult translateInformationContent(String sourceLanguage, LocalizedContent sourceContent, Set<String> targetLanguages) {
        if (!properties.isEnabled()) {
            TranslationResult result = noopTranslationProvider.translateInformationContent(sourceLanguage, sourceContent, targetLanguages);
            result.getWarnings().add("Translation disabled");
            return result;
        }

        String provider = properties.getProvider() != null ? properties.getProvider().trim().toLowerCase() : "";
        if ("openai".equals(provider)) {
            return openAiTranslationProvider.translateInformationContent(sourceLanguage, sourceContent, targetLanguages);
        }

        TranslationResult result = noopTranslationProvider.translateInformationContent(sourceLanguage, sourceContent, targetLanguages);
        result.getWarnings().add("Unsupported translation provider: " + provider);
        return result;
    }
}
