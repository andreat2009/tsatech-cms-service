package com.newproject.cms.controller;

import com.newproject.cms.dto.AnalyticsEventRequest;
import com.newproject.cms.dto.AnalyticsEventResponse;
import com.newproject.cms.dto.AnalyticsSummaryResponse;
import com.newproject.cms.service.AnalyticsService;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/cms/analytics")
public class AnalyticsController {
    private final AnalyticsService analyticsService;

    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @PostMapping("/events")
    @ResponseStatus(HttpStatus.CREATED)
    public AnalyticsEventResponse trackEvent(@RequestBody AnalyticsEventRequest request) {
        return analyticsService.trackEvent(request);
    }

    @GetMapping("/summary")
    public AnalyticsSummaryResponse summary() {
        return analyticsService.summary();
    }

    @GetMapping("/events")
    public List<AnalyticsEventResponse> recentEvents(@RequestParam(name = "limit", defaultValue = "50") int limit) {
        return analyticsService.recentEvents(limit);
    }
}
