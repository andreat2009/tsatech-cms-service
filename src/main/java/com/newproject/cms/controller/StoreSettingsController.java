package com.newproject.cms.controller;

import com.newproject.cms.dto.PublicStoreSettingsResponse;
import com.newproject.cms.dto.SmtpRuntimeSettingsResponse;
import com.newproject.cms.dto.StoreSettingsRequest;
import com.newproject.cms.dto.StoreSettingsResponse;
import com.newproject.cms.service.StoreSettingsService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/cms/settings")
public class StoreSettingsController {
    private final StoreSettingsService service;
    private final String internalToken;

    public StoreSettingsController(
        StoreSettingsService service,
        @Value("${cms.internal.token:}") String internalToken
    ) {
        this.service = service;
        this.internalToken = internalToken;
    }

    @GetMapping("/public")
    public PublicStoreSettingsResponse publicSettings() {
        return service.getPublicSettings();
    }

    @GetMapping
    public StoreSettingsResponse adminSettings() {
        return service.getAdminSettings();
    }

    @PutMapping
    public StoreSettingsResponse update(@RequestBody StoreSettingsRequest request) {
        return service.update(request);
    }

    @GetMapping("/runtime")
    public SmtpRuntimeSettingsResponse runtimeSettings(
        @RequestHeader(value = "X-Internal-Token", required = false) String requestToken
    ) {
        if (internalToken == null || internalToken.isBlank()) {
            throw new AccessDeniedException("Internal token not configured");
        }
        if (requestToken == null || requestToken.isBlank() || !requestToken.equals(internalToken)) {
            throw new AccessDeniedException("Invalid internal token");
        }
        return service.getRuntimeSmtpSettings();
    }
}
