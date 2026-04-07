package com.newproject.cms.repository;

import com.newproject.cms.domain.AdminAuditEvent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdminAuditEventRepository extends JpaRepository<AdminAuditEvent, Long> {
}
