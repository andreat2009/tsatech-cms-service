package com.newproject.cms.service.translation;

import com.newproject.cms.dto.LocalizedContent;
import java.util.Set;

public interface TranslationProvider {
    TranslationResult translateInformationContent(String sourceLanguage, LocalizedContent sourceContent, Set<String> targetLanguages);
}
