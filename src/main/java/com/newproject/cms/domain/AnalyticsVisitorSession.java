package com.newproject.cms.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;

@Entity
@Table(name = "analytics_visitor_session")
public class AnalyticsVisitorSession {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "visitor_id", nullable = false, unique = true, length = 128)
    private String visitorId;

    @Column(name = "first_seen", nullable = false)
    private OffsetDateTime firstSeen;

    @Column(name = "last_seen", nullable = false)
    private OffsetDateTime lastSeen;

    @Column(name = "last_ip", length = 128)
    private String lastIp;

    @Column(name = "last_user_agent", length = 1024)
    private String lastUserAgent;

    @Column(name = "last_path", length = 1024)
    private String lastPath;

    @Column(name = "last_referrer", length = 1024)
    private String lastReferrer;

    @Column(name = "last_locale", length = 16)
    private String lastLocale;

    @Column(name = "last_user_id", length = 128)
    private String lastUserId;

    @Column(name = "view_count", nullable = false)
    private Long viewCount;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getVisitorId() {
        return visitorId;
    }

    public void setVisitorId(String visitorId) {
        this.visitorId = visitorId;
    }

    public OffsetDateTime getFirstSeen() {
        return firstSeen;
    }

    public void setFirstSeen(OffsetDateTime firstSeen) {
        this.firstSeen = firstSeen;
    }

    public OffsetDateTime getLastSeen() {
        return lastSeen;
    }

    public void setLastSeen(OffsetDateTime lastSeen) {
        this.lastSeen = lastSeen;
    }

    public String getLastIp() {
        return lastIp;
    }

    public void setLastIp(String lastIp) {
        this.lastIp = lastIp;
    }

    public String getLastUserAgent() {
        return lastUserAgent;
    }

    public void setLastUserAgent(String lastUserAgent) {
        this.lastUserAgent = lastUserAgent;
    }

    public String getLastPath() {
        return lastPath;
    }

    public void setLastPath(String lastPath) {
        this.lastPath = lastPath;
    }

    public String getLastReferrer() {
        return lastReferrer;
    }

    public void setLastReferrer(String lastReferrer) {
        this.lastReferrer = lastReferrer;
    }

    public String getLastLocale() {
        return lastLocale;
    }

    public void setLastLocale(String lastLocale) {
        this.lastLocale = lastLocale;
    }

    public String getLastUserId() {
        return lastUserId;
    }

    public void setLastUserId(String lastUserId) {
        this.lastUserId = lastUserId;
    }

    public Long getViewCount() {
        return viewCount;
    }

    public void setViewCount(Long viewCount) {
        this.viewCount = viewCount;
    }
}
