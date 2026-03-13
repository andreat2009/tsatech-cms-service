package com.newproject.cms.repository;

import com.newproject.cms.domain.AnalyticsVisitorSession;
import java.time.OffsetDateTime;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AnalyticsVisitorSessionRepository extends JpaRepository<AnalyticsVisitorSession, Long> {
    Optional<AnalyticsVisitorSession> findByVisitorId(String visitorId);

    long countByLastSeenAfter(OffsetDateTime dateTime);
}
