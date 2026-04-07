package com.newproject.cms.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;

@Entity
@Table(name = "admin_audit_events")
public class AdminAuditEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "actor_username", nullable = false, length = 255)
    private String actorUsername;

    @Column(name = "actor_subject", length = 255)
    private String actorSubject;

    @Column(name = "action_type", nullable = false, length = 48)
    private String actionType;

    @Column(name = "target_type", nullable = false, length = 128)
    private String targetType;

    @Column(name = "target_id", length = 128)
    private String targetId;

    @Column(name = "request_path", nullable = false, length = 1024)
    private String requestPath;

    @Column(name = "http_method", nullable = false, length = 16)
    private String httpMethod;

    @Column(name = "outcome", nullable = false, length = 24)
    private String outcome;

    @Column(name = "status_code")
    private Integer statusCode;

    @Column(name = "summary", length = 1024)
    private String summary;

    @Column(name = "ip_address", length = 128)
    private String ipAddress;

    @Column(name = "user_agent", length = 1024)
    private String userAgent;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getActorUsername() { return actorUsername; }
    public void setActorUsername(String actorUsername) { this.actorUsername = actorUsername; }
    public String getActorSubject() { return actorSubject; }
    public void setActorSubject(String actorSubject) { this.actorSubject = actorSubject; }
    public String getActionType() { return actionType; }
    public void setActionType(String actionType) { this.actionType = actionType; }
    public String getTargetType() { return targetType; }
    public void setTargetType(String targetType) { this.targetType = targetType; }
    public String getTargetId() { return targetId; }
    public void setTargetId(String targetId) { this.targetId = targetId; }
    public String getRequestPath() { return requestPath; }
    public void setRequestPath(String requestPath) { this.requestPath = requestPath; }
    public String getHttpMethod() { return httpMethod; }
    public void setHttpMethod(String httpMethod) { this.httpMethod = httpMethod; }
    public String getOutcome() { return outcome; }
    public void setOutcome(String outcome) { this.outcome = outcome; }
    public Integer getStatusCode() { return statusCode; }
    public void setStatusCode(Integer statusCode) { this.statusCode = statusCode; }
    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }
    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }
    public String getUserAgent() { return userAgent; }
    public void setUserAgent(String userAgent) { this.userAgent = userAgent; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
}
