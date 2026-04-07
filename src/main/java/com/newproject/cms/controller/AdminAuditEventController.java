package com.newproject.cms.controller;

import com.newproject.cms.dto.AdminAuditEventRequest;
import com.newproject.cms.dto.AdminAuditEventResponse;
import com.newproject.cms.dto.PagedResponse;
import com.newproject.cms.service.AdminAuditEventService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/cms/admin-audit")
public class AdminAuditEventController {
    private final AdminAuditEventService service;

    public AdminAuditEventController(AdminAuditEventService service) {
        this.service = service;
    }

    @GetMapping
    public PagedResponse<AdminAuditEventResponse> list(
        @RequestParam(name = "page", defaultValue = "0") int page,
        @RequestParam(name = "size", defaultValue = "25") int size
    ) {
        return service.list(page, size);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AdminAuditEventResponse record(@RequestBody AdminAuditEventRequest request) {
        return service.record(request);
    }
}
