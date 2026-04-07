package com.newproject.cms.service;

import com.newproject.cms.domain.CommerceIntegration;
import com.newproject.cms.dto.CommerceIntegrationRequest;
import com.newproject.cms.dto.CommerceIntegrationResponse;
import com.newproject.cms.exception.BadRequestException;
import com.newproject.cms.exception.NotFoundException;
import com.newproject.cms.repository.CommerceIntegrationRepository;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CommerceIntegrationService {
    private static final Set<String> PROVIDER_TYPES = Set.of("BROADLEAF", "SHOPIFY", "WOOCOMMERCE", "MAGENTO", "OPENCART", "CUSTOM_REST");
    private static final Set<String> AUTH_TYPES = Set.of("NONE", "API_KEY", "BEARER", "BASIC");
    private static final Set<String> SYNC_MODES = Set.of("PULL", "PUSH", "BIDIRECTIONAL");

    private final CommerceIntegrationRepository repository;
    private final IntegrationCredentialCryptoService cryptoService;

    public CommerceIntegrationService(
        CommerceIntegrationRepository repository,
        IntegrationCredentialCryptoService cryptoService
    ) {
        this.repository = repository;
        this.cryptoService = cryptoService;
    }

    @Transactional(readOnly = true)
    public List<CommerceIntegrationResponse> list() {
        return repository.findAll(Sort.by(Sort.Order.desc("active"), Sort.Order.asc("displayName"), Sort.Order.asc("id")))
            .stream()
            .map(this::toResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public CommerceIntegrationResponse get(Long id) {
        return toResponse(load(id));
    }

    @Transactional
    public CommerceIntegrationResponse create(CommerceIntegrationRequest request) {
        CommerceIntegration integration = new CommerceIntegration();
        OffsetDateTime now = OffsetDateTime.now();
        integration.setCreatedAt(now);
        integration.setUpdatedAt(now);
        integration.setLastSyncStatus("READY");
        integration.setLastSyncSummary("Connector saved. Sync orchestration can start from this profile.");
        apply(integration, request);
        return toResponse(repository.save(integration));
    }

    @Transactional
    public CommerceIntegrationResponse update(Long id, CommerceIntegrationRequest request) {
        CommerceIntegration integration = load(id);
        integration.setUpdatedAt(OffsetDateTime.now());
        apply(integration, request);
        return toResponse(repository.save(integration));
    }

    @Transactional
    public void delete(Long id) {
        repository.delete(load(id));
    }

    private CommerceIntegration load(Long id) {
        return repository.findById(id)
            .orElseThrow(() -> new NotFoundException("Integration not found: " + id));
    }

    private void apply(CommerceIntegration integration, CommerceIntegrationRequest request) {
        String code = requireText(request.getCode(), "Connector code is required").toLowerCase(Locale.ROOT);
        repository.findByCodeIgnoreCase(code)
            .filter(existing -> integration.getId() == null || !existing.getId().equals(integration.getId()))
            .ifPresent(existing -> {
                throw new BadRequestException("Another connector already uses code '" + code + "'");
            });

        integration.setCode(code);
        integration.setDisplayName(requireText(request.getDisplayName(), "Display name is required"));
        integration.setProviderType(requireEnum(request.getProviderType(), PROVIDER_TYPES, "Unsupported provider type"));
        integration.setSyncMode(requireEnum(request.getSyncMode(), SYNC_MODES, "Unsupported sync mode"));
        integration.setAuthType(requireEnum(request.getAuthType(), AUTH_TYPES, "Unsupported auth type"));
        integration.setBaseUrl(normalizeBaseUrl(requireText(request.getBaseUrl(), "Base URL is required")));
        integration.setUsername(trimToNull(request.getUsername()));
        integration.setCatalogEndpoint(trimToNull(request.getCatalogEndpoint()));
        integration.setInventoryEndpoint(trimToNull(request.getInventoryEndpoint()));
        integration.setOrdersEndpoint(trimToNull(request.getOrdersEndpoint()));
        integration.setCustomersEndpoint(trimToNull(request.getCustomersEndpoint()));
        integration.setSyncCatalog(defaultBoolean(request.getSyncCatalog(), true));
        integration.setSyncInventory(defaultBoolean(request.getSyncInventory(), true));
        integration.setSyncOrders(defaultBoolean(request.getSyncOrders(), true));
        integration.setSyncCustomers(defaultBoolean(request.getSyncCustomers(), false));
        integration.setActive(defaultBoolean(request.getActive(), true));

        if (Boolean.TRUE.equals(request.getClearApiKey())) {
            integration.setApiKeyEncrypted(null);
        } else if (hasText(request.getApiKey())) {
            integration.setApiKeyEncrypted(cryptoService.encrypt(request.getApiKey()));
        }

        if (Boolean.TRUE.equals(request.getClearApiSecret())) {
            integration.setApiSecretEncrypted(null);
        } else if (hasText(request.getApiSecret())) {
            integration.setApiSecretEncrypted(cryptoService.encrypt(request.getApiSecret()));
        }

        if (integration.getLastSyncStatus() == null || integration.getLastSyncStatus().isBlank()) {
            integration.setLastSyncStatus("READY");
        }
        if (integration.getLastSyncSummary() == null || integration.getLastSyncSummary().isBlank()) {
            integration.setLastSyncSummary("Connector profile saved and ready for synchronization workflows.");
        }
    }

    private CommerceIntegrationResponse toResponse(CommerceIntegration integration) {
        CommerceIntegrationResponse response = new CommerceIntegrationResponse();
        response.setId(integration.getId());
        response.setCode(integration.getCode());
        response.setDisplayName(integration.getDisplayName());
        response.setProviderType(integration.getProviderType());
        response.setSyncMode(integration.getSyncMode());
        response.setAuthType(integration.getAuthType());
        response.setBaseUrl(integration.getBaseUrl());
        response.setUsername(integration.getUsername());
        response.setCatalogEndpoint(integration.getCatalogEndpoint());
        response.setInventoryEndpoint(integration.getInventoryEndpoint());
        response.setOrdersEndpoint(integration.getOrdersEndpoint());
        response.setCustomersEndpoint(integration.getCustomersEndpoint());
        response.setSyncCatalog(integration.getSyncCatalog());
        response.setSyncInventory(integration.getSyncInventory());
        response.setSyncOrders(integration.getSyncOrders());
        response.setSyncCustomers(integration.getSyncCustomers());
        response.setActive(integration.getActive());
        response.setApiKeyConfigured(hasText(cryptoService.decrypt(integration.getApiKeyEncrypted())));
        response.setApiSecretConfigured(hasText(cryptoService.decrypt(integration.getApiSecretEncrypted())));
        response.setProviderConfigurationAvailable(isConfigurationReady(integration));
        response.setCredentialKeySource(cryptoService.keySource());
        response.setSyncSummary(buildSyncSummary(integration));
        response.setLastSyncAt(integration.getLastSyncAt());
        response.setLastSyncStatus(integration.getLastSyncStatus());
        response.setLastSyncSummary(integration.getLastSyncSummary());
        response.setCreatedAt(integration.getCreatedAt());
        response.setUpdatedAt(integration.getUpdatedAt());
        return response;
    }

    private boolean isConfigurationReady(CommerceIntegration integration) {
        String authType = integration.getAuthType();
        if ("NONE".equals(authType)) {
            return true;
        }
        if ("BASIC".equals(authType)) {
            return hasText(integration.getUsername()) && hasText(cryptoService.decrypt(integration.getApiSecretEncrypted()));
        }
        return hasText(cryptoService.decrypt(integration.getApiKeyEncrypted()));
    }

    private String buildSyncSummary(CommerceIntegration integration) {
        List<String> domains = new ArrayList<>();
        if (Boolean.TRUE.equals(integration.getSyncCatalog())) {
            domains.add("Catalog");
        }
        if (Boolean.TRUE.equals(integration.getSyncInventory())) {
            domains.add("Inventory");
        }
        if (Boolean.TRUE.equals(integration.getSyncOrders())) {
            domains.add("Orders");
        }
        if (Boolean.TRUE.equals(integration.getSyncCustomers())) {
            domains.add("Customers");
        }
        if (domains.isEmpty()) {
            return "No sync domains selected";
        }
        return String.join(", ", new LinkedHashSet<>(domains));
    }

    private String requireEnum(String value, Set<String> allowed, String message) {
        String normalized = requireText(value, message).toUpperCase(Locale.ROOT);
        if (!allowed.contains(normalized)) {
            throw new BadRequestException(message + ": " + value);
        }
        return normalized;
    }

    private String requireText(String value, String message) {
        String trimmed = trimToNull(value);
        if (trimmed == null) {
            throw new BadRequestException(message);
        }
        return trimmed;
    }

    private String normalizeBaseUrl(String value) {
        String trimmed = value.trim();
        while (trimmed.endsWith("/")) {
            trimmed = trimmed.substring(0, trimmed.length() - 1);
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

    private boolean defaultBoolean(Boolean value, boolean fallback) {
        return value != null ? value : fallback;
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
