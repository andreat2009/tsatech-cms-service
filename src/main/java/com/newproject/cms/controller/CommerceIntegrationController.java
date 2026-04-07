package com.newproject.cms.controller;

import com.newproject.cms.dto.CommerceIntegrationRequest;
import com.newproject.cms.dto.CommerceIntegrationResponse;
import com.newproject.cms.service.CommerceIntegrationService;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/cms/integrations")
public class CommerceIntegrationController {
    private final CommerceIntegrationService service;

    public CommerceIntegrationController(CommerceIntegrationService service) {
        this.service = service;
    }

    @GetMapping
    public List<CommerceIntegrationResponse> list() {
        return service.list();
    }

    @GetMapping("/{id}")
    public CommerceIntegrationResponse get(@PathVariable Long id) {
        return service.get(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CommerceIntegrationResponse create(@RequestBody CommerceIntegrationRequest request) {
        return service.create(request);
    }

    @PutMapping("/{id}")
    public CommerceIntegrationResponse update(@PathVariable Long id, @RequestBody CommerceIntegrationRequest request) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
