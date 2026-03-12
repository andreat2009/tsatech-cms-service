package com.newproject.cms.service.translation;

import com.newproject.cms.dto.LocalizedContent;
import java.util.Set;
import org.springframework.stereotype.Component;

@Component
public class NoopTranslationProvider implements TranslationProvider {
    @Override
    public TranslationResult translateInformationContent(String sourceLanguage, LocalizedContent sourceContent, Set<String> targetLanguages) {
        return new TranslationResult();
    }
}
