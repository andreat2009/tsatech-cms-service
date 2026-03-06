package com.newproject.cms.repository;

import com.newproject.cms.domain.InformationPage;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InformationPageRepository extends JpaRepository<InformationPage, Long> {
    List<InformationPage> findAllByOrderBySortOrderAscTitleAsc();
    List<InformationPage> findByActiveOrderBySortOrderAscTitleAsc(Boolean active);
    Optional<InformationPage> findBySlugIgnoreCase(String slug);
    boolean existsBySlugIgnoreCase(String slug);
}
