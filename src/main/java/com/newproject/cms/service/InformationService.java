package com.newproject.cms.service;

import com.newproject.cms.domain.InformationPage;
import com.newproject.cms.domain.InformationPageTranslation;
import com.newproject.cms.dto.InformationRequest;
import com.newproject.cms.dto.InformationResponse;
import com.newproject.cms.dto.LocalizedContent;
import com.newproject.cms.events.EventPublisher;
import com.newproject.cms.exception.BadRequestException;
import com.newproject.cms.exception.NotFoundException;
import com.newproject.cms.repository.InformationPageRepository;
import java.text.Normalizer;
import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InformationService {
    private final InformationPageRepository repository;
    private final EventPublisher eventPublisher;

    public InformationService(InformationPageRepository repository, EventPublisher eventPublisher) {
        this.repository = repository;
        this.eventPublisher = eventPublisher;
    }

    @Transactional(readOnly = true)
    public List<InformationResponse> list(Boolean active, String language) {
        List<InformationPage> pages = active == null
            ? repository.findAll()
            : repository.findByActiveOrderBySortOrderAscTitleAsc(active);

        String requestedLanguage = LanguageSupport.normalizeLanguage(language);
        final String resolvedLanguage = requestedLanguage != null ? requestedLanguage : LanguageSupport.DEFAULT_LANGUAGE;

        pages.sort(Comparator
            .comparing(InformationPage::getSortOrder)
            .thenComparing(page -> resolveLocalizedContent(page.getTranslations(), resolvedLanguage, page.getTitle(), page.getContent()).getTitle(), String.CASE_INSENSITIVE_ORDER));

        return pages.stream()
            .map(page -> toResponse(page, resolvedLanguage))
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public InformationResponse get(Long id, String language) {
        InformationPage page = repository.findById(id)
            .orElseThrow(() -> new NotFoundException("Information page not found"));
        return toResponse(page, language);
    }

    @Transactional(readOnly = true)
    public InformationResponse getBySlug(String slug, String language) {
        InformationPage page = repository.findBySlugIgnoreCase(slug)
            .orElseThrow(() -> new NotFoundException("Information page not found"));
        return toResponse(page, language);
    }

    @Transactional
    public InformationResponse create(InformationRequest request) {
        InformationPage page = new InformationPage();
        apply(page, request, true);
        OffsetDateTime now = OffsetDateTime.now();
        page.setCreatedAt(now);
        page.setUpdatedAt(now);

        InformationPage saved = repository.save(page);
        InformationResponse response = toResponse(saved, LanguageSupport.DEFAULT_LANGUAGE);
        eventPublisher.publish("INFORMATION_PAGE_CREATED", "information_page", saved.getId().toString(), response);
        return response;
    }

    @Transactional
    public InformationResponse update(Long id, InformationRequest request) {
        InformationPage page = repository.findById(id)
            .orElseThrow(() -> new NotFoundException("Information page not found"));

        apply(page, request, false);
        page.setUpdatedAt(OffsetDateTime.now());
        InformationPage saved = repository.save(page);
        InformationResponse response = toResponse(saved, LanguageSupport.DEFAULT_LANGUAGE);
        eventPublisher.publish("INFORMATION_PAGE_UPDATED", "information_page", saved.getId().toString(), response);
        return response;
    }

    @Transactional
    public void delete(Long id) {
        InformationPage page = repository.findById(id)
            .orElseThrow(() -> new NotFoundException("Information page not found"));
        repository.delete(page);
        eventPublisher.publish("INFORMATION_PAGE_DELETED", "information_page", id.toString(), null);
    }

    private void apply(InformationPage page, InformationRequest request, boolean createMode) {
        Map<String, LocalizedContent> normalizedTranslations = normalizeTranslations(
            request.getTranslations(),
            request.getTitle(),
            request.getContent(),
            page.getTitle(),
            page.getContent()
        );

        LocalizedContent defaultContent = normalizedTranslations.get(LanguageSupport.DEFAULT_LANGUAGE);
        page.setTitle(defaultContent.getTitle());
        page.setContent(defaultContent.getContent());

        page.setSortOrder(request.getSortOrder() != null ? request.getSortOrder() : 0);
        page.setActive(request.getActive() != null ? request.getActive() : Boolean.TRUE);
        page.setSlug(uniqueSlug(request.getSlug(), defaultContent.getTitle(), createMode ? null : page.getId()));

        syncTranslations(page, normalizedTranslations);
    }

    private void syncTranslations(InformationPage page, Map<String, LocalizedContent> localizedContents) {
        Map<String, InformationPageTranslation> existingByLanguage = page.getTranslations().stream()
            .collect(Collectors.toMap(
                translation -> translation.getLanguageCode().toLowerCase(Locale.ROOT),
                translation -> translation,
                (first, ignored) -> first
            ));

        for (String language : LanguageSupport.SUPPORTED_LANGUAGES) {
            LocalizedContent localizedContent = localizedContents.get(language);
            InformationPageTranslation translation = existingByLanguage.get(language);
            if (translation == null) {
                translation = new InformationPageTranslation();
                translation.setPage(page);
                translation.setLanguageCode(language);
                page.getTranslations().add(translation);
                existingByLanguage.put(language, translation);
            }

            translation.setTitle(localizedContent.getTitle());
            translation.setContent(localizedContent.getContent());
        }

        page.getTranslations().removeIf(translation ->
            !LanguageSupport.SUPPORTED_LANGUAGES.contains(translation.getLanguageCode().toLowerCase(Locale.ROOT)));
    }

    private String uniqueSlug(String requestedSlug, String title, Long currentId) {
        String base = normalizeSlug(requestedSlug != null && !requestedSlug.isBlank() ? requestedSlug : title);
        String candidate = base;
        int i = 2;
        while (repository.existsBySlugIgnoreCase(candidate)) {
            if (currentId != null) {
                InformationPage existing = repository.findBySlugIgnoreCase(candidate).orElse(null);
                if (existing != null && currentId.equals(existing.getId())) {
                    return candidate;
                }
            }
            candidate = base + "-" + i;
            i++;
        }
        return candidate;
    }

    private String normalizeSlug(String value) {
        String normalized = Normalizer.normalize(value, Normalizer.Form.NFD)
            .replaceAll("\\p{M}", "")
            .toLowerCase(Locale.ROOT)
            .replaceAll("[^a-z0-9]+", "-")
            .replaceAll("(^-|-$)", "");
        return normalized.isBlank() ? "page" : normalized;
    }

    private InformationResponse toResponse(InformationPage page, String language) {
        LocalizedContent localized = resolveLocalizedContent(page.getTranslations(), language, page.getTitle(), page.getContent());

        InformationResponse response = new InformationResponse();
        response.setId(page.getId());
        response.setTitle(localized.getTitle());
        response.setSlug(page.getSlug());
        response.setContent(localized.getContent());
        response.setSortOrder(page.getSortOrder());
        response.setActive(page.getActive());
        response.setCreatedAt(page.getCreatedAt());
        response.setUpdatedAt(page.getUpdatedAt());
        response.setTranslations(toLocalizedContentMap(page.getTranslations(), page.getTitle(), page.getContent()));
        return response;
    }

    private Map<String, LocalizedContent> toLocalizedContentMap(List<InformationPageTranslation> translations, String fallbackTitle, String fallbackContent) {
        Map<String, LocalizedContent> map = new LinkedHashMap<>();
        Map<String, InformationPageTranslation> byLanguage = translations.stream()
            .collect(Collectors.toMap(
                translation -> translation.getLanguageCode().toLowerCase(Locale.ROOT),
                translation -> translation,
                (first, ignored) -> first
            ));

        for (String language : LanguageSupport.SUPPORTED_LANGUAGES) {
            InformationPageTranslation translation = byLanguage.get(language);
            LocalizedContent content = new LocalizedContent();
            content.setTitle(firstNonBlank(
                translation != null ? translation.getTitle() : null,
                language.equals(LanguageSupport.DEFAULT_LANGUAGE) ? fallbackTitle : null,
                fallbackTitle
            ));
            content.setContent(firstNonBlank(
                translation != null ? translation.getContent() : null,
                language.equals(LanguageSupport.DEFAULT_LANGUAGE) ? fallbackContent : null,
                fallbackContent
            ));
            map.put(language, content);
        }

        return map;
    }

    private LocalizedContent resolveLocalizedContent(
        List<InformationPageTranslation> translations,
        String language,
        String fallbackTitle,
        String fallbackContent
    ) {
        String resolvedLanguage = LanguageSupport.normalizeLanguage(language);
        if (resolvedLanguage == null) {
            resolvedLanguage = LanguageSupport.DEFAULT_LANGUAGE;
        }

        Map<String, LocalizedContent> map = toLocalizedContentMap(translations, fallbackTitle, fallbackContent);
        LocalizedContent localized = map.get(resolvedLanguage);
        if (localized == null) {
            localized = map.get(LanguageSupport.DEFAULT_LANGUAGE);
        }
        if (localized == null) {
            localized = new LocalizedContent();
            localized.setTitle(fallbackTitle);
            localized.setContent(fallbackContent);
        }
        return localized;
    }

    private Map<String, LocalizedContent> normalizeTranslations(
        Map<String, LocalizedContent> requested,
        String fallbackTitle,
        String fallbackContent,
        String existingTitle,
        String existingContent
    ) {
        Map<String, LocalizedContent> normalized = new LinkedHashMap<>();

        String defaultTitle = firstNonBlank(
            extractValue(requested, LanguageSupport.DEFAULT_LANGUAGE, true),
            fallbackTitle,
            existingTitle
        );
        String defaultContent = firstNonBlank(
            extractValue(requested, LanguageSupport.DEFAULT_LANGUAGE, false),
            fallbackContent,
            existingContent
        );

        if (defaultTitle == null || defaultTitle.isBlank()) {
            throw new BadRequestException("Information page title is required");
        }
        if (defaultContent == null || defaultContent.isBlank()) {
            throw new BadRequestException("Information page content is required");
        }

        for (String language : LanguageSupport.SUPPORTED_LANGUAGES) {
            LocalizedContent content = new LocalizedContent();
            content.setTitle(firstNonBlank(
                extractValue(requested, language, true),
                language.equals(LanguageSupport.DEFAULT_LANGUAGE) ? fallbackTitle : null,
                defaultTitle
            ));
            content.setContent(firstNonBlank(
                extractValue(requested, language, false),
                language.equals(LanguageSupport.DEFAULT_LANGUAGE) ? fallbackContent : null,
                defaultContent
            ));
            normalized.put(language, content);
        }

        return normalized;
    }

    private String extractValue(Map<String, LocalizedContent> requested, String language, boolean titleField) {
        if (requested == null) {
            return null;
        }

        LocalizedContent content = requested.get(language);
        if (content == null) {
            return null;
        }

        return trimToNull(titleField ? content.getTitle() : content.getContent());
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            String trimmed = trimToNull(value);
            if (trimmed != null) {
                return trimmed;
            }
        }
        return null;
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
