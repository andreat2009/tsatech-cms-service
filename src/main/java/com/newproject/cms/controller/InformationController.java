package com.newproject.cms.controller;

import com.newproject.cms.dto.InformationRequest;
import com.newproject.cms.dto.InformationResponse;
import com.newproject.cms.service.InformationService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cms/information")
public class InformationController {
    private final InformationService service;

    public InformationController(InformationService service) {
        this.service = service;
    }

    @GetMapping
    public List<InformationResponse> list(@RequestParam(required = false) Boolean active) {
        return service.list(active);
    }

    @GetMapping("/{id}")
    public InformationResponse get(@PathVariable Long id) {
        return service.get(id);
    }

    @GetMapping("/slug/{slug}")
    public InformationResponse getBySlug(@PathVariable String slug) {
        return service.getBySlug(slug);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public InformationResponse create(@Valid @RequestBody InformationRequest request) {
        return service.create(request);
    }

    @PutMapping("/{id}")
    public InformationResponse update(@PathVariable Long id, @Valid @RequestBody InformationRequest request) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
