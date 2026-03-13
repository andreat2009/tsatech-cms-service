package com.newproject.cms.repository;

import com.newproject.cms.domain.AnalyticsEvent;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface AnalyticsEventRepository extends JpaRepository<AnalyticsEvent, Long> {
    long countByEventType(String eventType);

    List<AnalyticsEvent> findAllByOrderByCreatedAtDesc(Pageable pageable);

    @Query("select count(distinct e.entityId) from AnalyticsEvent e where e.entityType = 'product' and e.entityId is not null")
    long countDistinctViewedProducts();

    @Query("select e.path as path, count(e) as total from AnalyticsEvent e where e.eventType = 'page_view' and e.path is not null group by e.path order by count(e) desc")
    List<PathCountProjection> findTopPagePaths(Pageable pageable);

    interface PathCountProjection {
        String getPath();
        Long getTotal();
    }
}
