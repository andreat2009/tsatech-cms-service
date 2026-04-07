package com.newproject.cms.service;

import com.newproject.cms.domain.AdminAuditEvent;
import com.newproject.cms.dto.AdminAuditEventRequest;
import com.newproject.cms.dto.AdminAuditEventResponse;
import com.newproject.cms.dto.PagedResponse;
import com.newproject.cms.exception.BadRequestException;
import com.newproject.cms.repository.AdminAuditEventRepository;
import java.time.OffsetDateTime;
import java.util.Locale;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminAuditEventService {
    private static final int MAX_PAGE_SIZE = 100;

    private final AdminAuditEventRepository repository;

    public AdminAuditEventService(AdminAuditEventRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public PagedResponse<AdminAuditEventResponse> list(int page, int size) {
        int safePage = Math.max(0, page);
        int safeSize = Math.max(1, Math.min(MAX_PAGE_SIZE, size));
        return PagedResponse.from(repository.findAll(
            PageRequest.of(safePage, safeSize, Sort.by(Sort.Order.desc("createdAt"), Sort.Order.desc("id")))
        ).map(this::toResponse));
    }

    @Transactional
    public AdminAuditEventResponse record(AdminAuditEventRequest request) {
        AdminAuditEvent event = new AdminAuditEvent();
        event.setActorUsername(requireText(request.getActorUsername(), "Actor username is required"));
        event.setActorSubject(trimToNull(request.getActorSubject()));
        event.setActionType(normalizeUpper(request.getActionType(), "ADMIN_ACTION"));
        event.setTargetType(normalizeUpper(request.getTargetType(), "ADMIN_RESOURCE"));
        event.setTargetId(trimToNull(request.getTargetId()));
        event.setRequestPath(requireText(request.getRequestPath(), "Request path is required"));
        event.setHttpMethod(normalizeUpper(request.getHttpMethod(), "GET"));
        event.setOutcome(normalizeUpper(request.getOutcome(), "SUCCESS"));
        event.setStatusCode(request.getStatusCode());
        event.setSummary(trimToNull(request.getSummary()));
        event.setIpAddress(trimToNull(request.getIpAddress()));
        event.setUserAgent(trimToNull(request.getUserAgent()));
        event.setCreatedAt(OffsetDateTime.now());
        return toResponse(repository.save(event));
    }

    private AdminAuditEventResponse toResponse(AdminAuditEvent event) {
        AdminAuditEventResponse response = new AdminAuditEventResponse();
        response.setId(event.getId());
        response.setActorUsername(event.getActorUsername());
        response.setActorSubject(event.getActorSubject());
        response.setActionType(event.getActionType());
        response.setTargetType(event.getTargetType());
        response.setTargetId(event.getTargetId());
        response.setRequestPath(event.getRequestPath());
        response.setHttpMethod(event.getHttpMethod());
        response.setOutcome(event.getOutcome());
        response.setStatusCode(event.getStatusCode());
        response.setSummary(event.getSummary());
        response.setIpAddress(event.getIpAddress());
        response.setUserAgent(event.getUserAgent());
        response.setCreatedAt(event.getCreatedAt());
        return response;
    }

    private String requireText(String value, String message) {
        String trimmed = trimToNull(value);
        if (trimmed == null) {
            throw new BadRequestException(message);
        }
        return trimmed;
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String normalizeUpper(String value, String fallback) {
        String normalized = trimToNull(value);
        if (normalized == null) {
            return fallback;
        }
        return normalized.replace(' ', '_').replace('-', '_').toUpperCase(Locale.ROOT);
    }
}
