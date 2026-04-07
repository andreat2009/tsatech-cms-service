package com.newproject.cms.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;

@Entity
@Table(name = "commerce_integrations")
public class CommerceIntegration {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "code", nullable = false, length = 96, unique = true)
    private String code;

    @Column(name = "display_name", nullable = false, length = 255)
    private String displayName;

    @Column(name = "provider_type", nullable = false, length = 32)
    private String providerType;

    @Column(name = "sync_mode", nullable = false, length = 24)
    private String syncMode;

    @Column(name = "auth_type", nullable = false, length = 24)
    private String authType;

    @Column(name = "base_url", nullable = false, length = 1024)
    private String baseUrl;

    @Column(name = "username", length = 255)
    private String username;

    @Column(name = "api_key_encrypted", length = 2048)
    private String apiKeyEncrypted;

    @Column(name = "api_secret_encrypted", length = 2048)
    private String apiSecretEncrypted;

    @Column(name = "catalog_endpoint", length = 255)
    private String catalogEndpoint;

    @Column(name = "inventory_endpoint", length = 255)
    private String inventoryEndpoint;

    @Column(name = "orders_endpoint", length = 255)
    private String ordersEndpoint;

    @Column(name = "customers_endpoint", length = 255)
    private String customersEndpoint;

    @Column(name = "sync_catalog", nullable = false)
    private Boolean syncCatalog;

    @Column(name = "sync_inventory", nullable = false)
    private Boolean syncInventory;

    @Column(name = "sync_orders", nullable = false)
    private Boolean syncOrders;

    @Column(name = "sync_customers", nullable = false)
    private Boolean syncCustomers;

    @Column(name = "active", nullable = false)
    private Boolean active;

    @Column(name = "last_sync_at")
    private OffsetDateTime lastSyncAt;

    @Column(name = "last_sync_status", length = 48)
    private String lastSyncStatus;

    @Column(name = "last_sync_summary", length = 1024)
    private String lastSyncSummary;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }
    public String getProviderType() { return providerType; }
    public void setProviderType(String providerType) { this.providerType = providerType; }
    public String getSyncMode() { return syncMode; }
    public void setSyncMode(String syncMode) { this.syncMode = syncMode; }
    public String getAuthType() { return authType; }
    public void setAuthType(String authType) { this.authType = authType; }
    public String getBaseUrl() { return baseUrl; }
    public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getApiKeyEncrypted() { return apiKeyEncrypted; }
    public void setApiKeyEncrypted(String apiKeyEncrypted) { this.apiKeyEncrypted = apiKeyEncrypted; }
    public String getApiSecretEncrypted() { return apiSecretEncrypted; }
    public void setApiSecretEncrypted(String apiSecretEncrypted) { this.apiSecretEncrypted = apiSecretEncrypted; }
    public String getCatalogEndpoint() { return catalogEndpoint; }
    public void setCatalogEndpoint(String catalogEndpoint) { this.catalogEndpoint = catalogEndpoint; }
    public String getInventoryEndpoint() { return inventoryEndpoint; }
    public void setInventoryEndpoint(String inventoryEndpoint) { this.inventoryEndpoint = inventoryEndpoint; }
    public String getOrdersEndpoint() { return ordersEndpoint; }
    public void setOrdersEndpoint(String ordersEndpoint) { this.ordersEndpoint = ordersEndpoint; }
    public String getCustomersEndpoint() { return customersEndpoint; }
    public void setCustomersEndpoint(String customersEndpoint) { this.customersEndpoint = customersEndpoint; }
    public Boolean getSyncCatalog() { return syncCatalog; }
    public void setSyncCatalog(Boolean syncCatalog) { this.syncCatalog = syncCatalog; }
    public Boolean getSyncInventory() { return syncInventory; }
    public void setSyncInventory(Boolean syncInventory) { this.syncInventory = syncInventory; }
    public Boolean getSyncOrders() { return syncOrders; }
    public void setSyncOrders(Boolean syncOrders) { this.syncOrders = syncOrders; }
    public Boolean getSyncCustomers() { return syncCustomers; }
    public void setSyncCustomers(Boolean syncCustomers) { this.syncCustomers = syncCustomers; }
    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }
    public OffsetDateTime getLastSyncAt() { return lastSyncAt; }
    public void setLastSyncAt(OffsetDateTime lastSyncAt) { this.lastSyncAt = lastSyncAt; }
    public String getLastSyncStatus() { return lastSyncStatus; }
    public void setLastSyncStatus(String lastSyncStatus) { this.lastSyncStatus = lastSyncStatus; }
    public String getLastSyncSummary() { return lastSyncSummary; }
    public void setLastSyncSummary(String lastSyncSummary) { this.lastSyncSummary = lastSyncSummary; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(OffsetDateTime updatedAt) { this.updatedAt = updatedAt; }
}
