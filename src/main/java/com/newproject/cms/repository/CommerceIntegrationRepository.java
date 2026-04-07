package com.newproject.cms.repository;

import com.newproject.cms.domain.CommerceIntegration;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommerceIntegrationRepository extends JpaRepository<CommerceIntegration, Long> {
    Optional<CommerceIntegration> findByCodeIgnoreCase(String code);
}
