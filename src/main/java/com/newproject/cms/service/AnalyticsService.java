package com.newproject.cms.service;

import com.newproject.cms.domain.AnalyticsEvent;
import com.newproject.cms.domain.AnalyticsVisitorSession;
import com.newproject.cms.dto.AnalyticsEventRequest;
import com.newproject.cms.dto.AnalyticsEventResponse;
import com.newproject.cms.dto.AnalyticsSummaryResponse;
import com.newproject.cms.repository.AnalyticsEventRepository;
import com.newproject.cms.repository.AnalyticsVisitorSessionRepository;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AnalyticsService {
    private final AnalyticsVisitorSessionRepository sessionRepository;
    private final AnalyticsEventRepository eventRepository;

    public AnalyticsService(
        AnalyticsVisitorSessionRepository sessionRepository,
        AnalyticsEventRepository eventRepository
    ) {
        this.sessionRepository = sessionRepository;
        this.eventRepository = eventRepository;
    }

    @Transactional
    public AnalyticsEventResponse trackEvent(AnalyticsEventRequest request) {
        String visitorId = trimToNull(request != null ? request.getVisitorId() : null);
        if (visitorId == null) {
            visitorId = UUID.randomUUID().toString();
        }
        final String resolvedVisitorId = visitorId;

        String path = trimToNull(request != null ? request.getPath() : null);
        if (path == null) {
            path = "/";
        }

        String eventType = trimToNull(request != null ? request.getEventType() : null);
        if (eventType == null) {
            eventType = "page_view";
        }

        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);

        AnalyticsVisitorSession session = sessionRepository.findByVisitorId(resolvedVisitorId)
            .orElseGet(() -> {
                AnalyticsVisitorSession created = new AnalyticsVisitorSession();
                created.setVisitorId(resolvedVisitorId);
                created.setFirstSeen(now);
                created.setViewCount(0L);
                return created;
            });

        session.setLastSeen(now);
        session.setLastPath(path);
        session.setLastIp(trimToNull(request != null ? request.getIp() : null));
        session.setLastUserAgent(trimToNull(request != null ? request.getUserAgent() : null));
        session.setLastReferrer(trimToNull(request != null ? request.getReferrer() : null));
        session.setLastLocale(trimToNull(request != null ? request.getLocale() : null));
        session.setLastUserId(trimToNull(request != null ? request.getUserId() : null));
        session.setViewCount((session.getViewCount() != null ? session.getViewCount() : 0L) + 1L);
        session = sessionRepository.save(session);

        AnalyticsEvent event = new AnalyticsEvent();
        event.setSession(session);
        event.setEventType(eventType);
        event.setPath(path);
        event.setPageTitle(trimToNull(request != null ? request.getPageTitle() : null));
        event.setReferrer(trimToNull(request != null ? request.getReferrer() : null));
        event.setEntityType(trimToNull(request != null ? request.getEntityType() : null));
        event.setEntityId(trimToNull(request != null ? request.getEntityId() : null));
        event.setIp(trimToNull(request != null ? request.getIp() : null));
        event.setUserAgent(trimToNull(request != null ? request.getUserAgent() : null));
        event.setLocale(trimToNull(request != null ? request.getLocale() : null));
        event.setUserId(trimToNull(request != null ? request.getUserId() : null));
        event.setCreatedAt(now);

        event = eventRepository.save(event);
        return toResponse(event);
    }

    @Transactional(readOnly = true)
    public AnalyticsSummaryResponse summary() {
        AnalyticsSummaryResponse response = new AnalyticsSummaryResponse();
        response.setTotalVisitors(sessionRepository.count());
        response.setTotalPageViews(eventRepository.countByEventType("page_view"));
        response.setUniqueProductsViewed(eventRepository.countDistinctViewedProducts());
        response.setTodayVisitors(sessionRepository.countByLastSeenAfter(OffsetDateTime.now(ZoneOffset.UTC).toLocalDate().atStartOfDay().atOffset(ZoneOffset.UTC)));

        List<AnalyticsSummaryResponse.PathStat> topPaths = eventRepository.findTopPagePaths(PageRequest.of(0, 8)).stream()
            .map(row -> {
                AnalyticsSummaryResponse.PathStat stat = new AnalyticsSummaryResponse.PathStat();
                stat.setPath(row.getPath());
                stat.setViews(row.getTotal() != null ? row.getTotal() : 0L);
                return stat;
            })
            .toList();
        response.setTopPaths(topPaths);
        return response;
    }

    @Transactional(readOnly = true)
    public List<AnalyticsEventResponse> recentEvents(int limit) {
        int resolvedLimit = Math.max(1, Math.min(limit, 200));
        return eventRepository.findAllByOrderByCreatedAtDesc(PageRequest.of(0, resolvedLimit)).stream()
            .map(this::toResponse)
            .toList();
    }

    private AnalyticsEventResponse toResponse(AnalyticsEvent event) {
        AnalyticsEventResponse response = new AnalyticsEventResponse();
        response.setId(event.getId());
        response.setVisitorId(event.getSession() != null ? event.getSession().getVisitorId() : null);
        response.setEventType(event.getEventType());
        response.setPath(event.getPath());
        response.setPageTitle(event.getPageTitle());
        response.setReferrer(event.getReferrer());
        response.setEntityType(event.getEntityType());
        response.setEntityId(event.getEntityId());
        response.setIp(event.getIp());
        response.setLocale(event.getLocale());
        response.setUserId(event.getUserId());
        response.setCreatedAt(event.getCreatedAt());
        return response;
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
