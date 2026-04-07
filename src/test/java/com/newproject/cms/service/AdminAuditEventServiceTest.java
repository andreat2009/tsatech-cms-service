package com.newproject.cms.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.newproject.cms.domain.AdminAuditEvent;
import com.newproject.cms.dto.AdminAuditEventRequest;
import com.newproject.cms.dto.AdminAuditEventResponse;
import com.newproject.cms.exception.BadRequestException;
import com.newproject.cms.repository.AdminAuditEventRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AdminAuditEventServiceTest {

    @Mock
    private AdminAuditEventRepository repository;

    @InjectMocks
    private AdminAuditEventService service;

    @Test
    void recordPersistsNormalizedAdminAuditEvent() {
        when(repository.save(any(AdminAuditEvent.class))).thenAnswer(invocation -> {
            AdminAuditEvent event = invocation.getArgument(0);
            event.setId(99L);
            return event;
        });

        AdminAuditEventRequest request = new AdminAuditEventRequest();
        request.setActorUsername("admin");
        request.setActionType("state change");
        request.setTargetType("orders");
        request.setTargetId("42");
        request.setRequestPath("/admin/orders/42/status");
        request.setHttpMethod("post");
        request.setOutcome("success");
        request.setStatusCode(302);
        request.setSummary("Changed order state");

        AdminAuditEventResponse response = service.record(request);

        assertThat(response.getId()).isEqualTo(99L);
        assertThat(response.getActionType()).isEqualTo("STATE_CHANGE");
        assertThat(response.getTargetType()).isEqualTo("ORDERS");
        assertThat(response.getHttpMethod()).isEqualTo("POST");
        assertThat(response.getOutcome()).isEqualTo("SUCCESS");
        assertThat(response.getCreatedAt()).isNotNull();
    }

    @Test
    void recordRejectsMissingActorUsername() {
        AdminAuditEventRequest request = new AdminAuditEventRequest();
        request.setRequestPath("/admin/orders");
        request.setHttpMethod("POST");

        assertThrows(BadRequestException.class, () -> service.record(request));
    }
}
