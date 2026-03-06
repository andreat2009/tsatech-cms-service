package com.newproject.cms.service;

import com.newproject.cms.domain.InformationPage;
import com.newproject.cms.dto.InformationRequest;
import com.newproject.cms.dto.InformationResponse;
import com.newproject.cms.events.EventPublisher;
import com.newproject.cms.exception.NotFoundException;
import com.newproject.cms.repository.InformationPageRepository;
import java.text.Normalizer;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Locale;
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
    public List<InformationResponse> list(Boolean active) {
        List<InformationPage> pages = active == null
            ? repository.findAllByOrderBySortOrderAscTitleAsc()
            : repository.findByActiveOrderBySortOrderAscTitleAsc(active);
        return pages.stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public InformationResponse get(Long id) {
        return toResponse(repository.findById(id)
            .orElseThrow(() -> new NotFoundException("Information page not found")));
    }

    @Transactional(readOnly = true)
    public InformationResponse getBySlug(String slug) {
        return toResponse(repository.findBySlugIgnoreCase(slug)
            .orElseThrow(() -> new NotFoundException("Information page not found")));
    }

    @Transactional
    public InformationResponse create(InformationRequest request) {
        InformationPage page = new InformationPage();
        apply(page, request, true);
        OffsetDateTime now = OffsetDateTime.now();
        page.setCreatedAt(now);
        page.setUpdatedAt(now);

        InformationPage saved = repository.save(page);
        InformationResponse response = toResponse(saved);
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
        InformationResponse response = toResponse(saved);
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
        page.setTitle(request.getTitle());
        page.setContent(request.getContent());
        page.setSortOrder(request.getSortOrder() != null ? request.getSortOrder() : 0);
        page.setActive(request.getActive() != null ? request.getActive() : Boolean.TRUE);
        page.setSlug(uniqueSlug(request.getSlug(), request.getTitle(), createMode ? null : page.getId()));
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

    private InformationResponse toResponse(InformationPage page) {
        InformationResponse response = new InformationResponse();
        response.setId(page.getId());
        response.setTitle(page.getTitle());
        response.setSlug(page.getSlug());
        response.setContent(page.getContent());
        response.setSortOrder(page.getSortOrder());
        response.setActive(page.getActive());
        response.setCreatedAt(page.getCreatedAt());
        response.setUpdatedAt(page.getUpdatedAt());
        return response;
    }
}
