package com.newproject.cms.service;

import com.newproject.cms.domain.StoreSettings;
import com.newproject.cms.dto.PublicStoreSettingsResponse;
import com.newproject.cms.dto.SmtpRuntimeSettingsResponse;
import com.newproject.cms.dto.StoreSettingsRequest;
import com.newproject.cms.dto.StoreSettingsResponse;
import com.newproject.cms.repository.StoreSettingsRepository;
import java.time.OffsetDateTime;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StoreSettingsService {
    private static final Long SETTINGS_ID = 1L;

    private final StoreSettingsRepository repository;

    public StoreSettingsService(StoreSettingsRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public PublicStoreSettingsResponse getPublicSettings() {
        return toPublic(loadSettings());
    }

    @Transactional(readOnly = true)
    public StoreSettingsResponse getAdminSettings() {
        return toAdmin(loadSettings());
    }

    @Transactional(readOnly = true)
    public SmtpRuntimeSettingsResponse getRuntimeSmtpSettings() {
        return toRuntime(loadSettings());
    }

    @Transactional
    public StoreSettingsResponse update(StoreSettingsRequest request) {
        StoreSettings settings = loadSettings();
        applyUpdate(settings, request);
        settings.setUpdatedAt(OffsetDateTime.now());
        return toAdmin(repository.save(settings));
    }

    private StoreSettings loadSettings() {
        return repository.findById(SETTINGS_ID).orElseGet(this::createDefaultSettings);
    }

    private StoreSettings createDefaultSettings() {
        StoreSettings defaults = new StoreSettings();
        OffsetDateTime now = OffsetDateTime.now();

        defaults.setId(SETTINGS_ID);
        defaults.setSiteName("TSATech Store");
        defaults.setContactEmail("andrea.terrasi78@gmail.com");
        defaults.setSupportEmail("andrea.terrasi78@gmail.com");
        defaults.setSupportPhone("+39 800 000 000");
        defaults.setAddressLine1("Via Roma 1");
        defaults.setCity("Milano");
        defaults.setPostalCode("20100");
        defaults.setCountry("IT");

        defaults.setSmtpEnabled(true);
        defaults.setSmtpHost("smtp.gmail.com");
        defaults.setSmtpPort(587);
        defaults.setSmtpUsername("andrea.terrasi78@gmail.com");
        defaults.setSmtpPassword("12345678");
        defaults.setSmtpAuth(true);
        defaults.setSmtpStarttls(true);
        defaults.setMailFromEmail("andrea.terrasi78@gmail.com");
        defaults.setMailFromName("TSATech Store");

        defaults.setCreatedAt(now);
        defaults.setUpdatedAt(now);
        return repository.save(defaults);
    }

    private void applyUpdate(StoreSettings settings, StoreSettingsRequest request) {
        settings.setSiteName(firstNonBlank(trimToNull(request.getSiteName()), settings.getSiteName(), "TSATech Store"));
        settings.setLogoUrl(trimToNull(request.getLogoUrl()));
        settings.setContactEmail(trimToNull(request.getContactEmail()));
        settings.setSupportEmail(trimToNull(request.getSupportEmail()));
        settings.setSupportPhone(trimToNull(request.getSupportPhone()));
        settings.setSupportPhoneSecondary(trimToNull(request.getSupportPhoneSecondary()));
        settings.setAddressLine1(trimToNull(request.getAddressLine1()));
        settings.setAddressLine2(trimToNull(request.getAddressLine2()));
        settings.setCity(trimToNull(request.getCity()));
        settings.setPostalCode(trimToNull(request.getPostalCode()));
        settings.setCountry(trimToNull(request.getCountry()));

        if (request.getSmtpEnabled() != null) {
            settings.setSmtpEnabled(request.getSmtpEnabled());
        }
        if (trimToNull(request.getSmtpHost()) != null) {
            settings.setSmtpHost(trimToNull(request.getSmtpHost()));
        }
        if (request.getSmtpPort() != null && request.getSmtpPort() > 0) {
            settings.setSmtpPort(request.getSmtpPort());
        }
        if (trimToNull(request.getSmtpUsername()) != null) {
            settings.setSmtpUsername(trimToNull(request.getSmtpUsername()));
        }
        if (trimToNull(request.getSmtpPassword()) != null) {
            settings.setSmtpPassword(trimToNull(request.getSmtpPassword()));
        }
        if (request.getSmtpAuth() != null) {
            settings.setSmtpAuth(request.getSmtpAuth());
        }
        if (request.getSmtpStarttls() != null) {
            settings.setSmtpStarttls(request.getSmtpStarttls());
        }
        if (trimToNull(request.getMailFromEmail()) != null) {
            settings.setMailFromEmail(trimToNull(request.getMailFromEmail()));
        }
        if (trimToNull(request.getMailFromName()) != null) {
            settings.setMailFromName(trimToNull(request.getMailFromName()));
        }
    }

    private StoreSettingsResponse toAdmin(StoreSettings settings) {
        StoreSettingsResponse response = new StoreSettingsResponse();
        response.setId(settings.getId());
        response.setSiteName(settings.getSiteName());
        response.setLogoUrl(settings.getLogoUrl());
        response.setContactEmail(settings.getContactEmail());
        response.setSupportEmail(settings.getSupportEmail());
        response.setSupportPhone(settings.getSupportPhone());
        response.setSupportPhoneSecondary(settings.getSupportPhoneSecondary());
        response.setAddressLine1(settings.getAddressLine1());
        response.setAddressLine2(settings.getAddressLine2());
        response.setCity(settings.getCity());
        response.setPostalCode(settings.getPostalCode());
        response.setCountry(settings.getCountry());
        response.setSmtpEnabled(settings.getSmtpEnabled());
        response.setSmtpHost(settings.getSmtpHost());
        response.setSmtpPort(settings.getSmtpPort());
        response.setSmtpUsername(settings.getSmtpUsername());
        response.setSmtpPassword(settings.getSmtpPassword());
        response.setSmtpAuth(settings.getSmtpAuth());
        response.setSmtpStarttls(settings.getSmtpStarttls());
        response.setMailFromEmail(settings.getMailFromEmail());
        response.setMailFromName(settings.getMailFromName());
        response.setCreatedAt(settings.getCreatedAt());
        response.setUpdatedAt(settings.getUpdatedAt());
        return response;
    }

    private PublicStoreSettingsResponse toPublic(StoreSettings settings) {
        PublicStoreSettingsResponse response = new PublicStoreSettingsResponse();
        response.setSiteName(settings.getSiteName());
        response.setLogoUrl(settings.getLogoUrl());
        response.setContactEmail(settings.getContactEmail());
        response.setSupportEmail(settings.getSupportEmail());
        response.setSupportPhone(settings.getSupportPhone());
        response.setSupportPhoneSecondary(settings.getSupportPhoneSecondary());
        response.setAddressLine1(settings.getAddressLine1());
        response.setAddressLine2(settings.getAddressLine2());
        response.setCity(settings.getCity());
        response.setPostalCode(settings.getPostalCode());
        response.setCountry(settings.getCountry());
        return response;
    }

    private SmtpRuntimeSettingsResponse toRuntime(StoreSettings settings) {
        SmtpRuntimeSettingsResponse response = new SmtpRuntimeSettingsResponse();
        response.setSmtpEnabled(settings.getSmtpEnabled());
        response.setSmtpHost(settings.getSmtpHost());
        response.setSmtpPort(settings.getSmtpPort());
        response.setSmtpUsername(settings.getSmtpUsername());
        response.setSmtpPassword(settings.getSmtpPassword());
        response.setSmtpAuth(settings.getSmtpAuth());
        response.setSmtpStarttls(settings.getSmtpStarttls());
        response.setMailFromEmail(settings.getMailFromEmail());
        response.setMailFromName(settings.getMailFromName());
        return response;
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return "";
    }
}
