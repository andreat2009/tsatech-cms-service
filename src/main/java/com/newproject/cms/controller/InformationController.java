package com.newproject.cms.controller;

import com.newproject.cms.dto.InformationAutoTranslateRequest;
import com.newproject.cms.dto.InformationAutoTranslateResponse;
import com.newproject.cms.dto.InformationRequest;
import com.newproject.cms.dto.InformationResponse;
import com.newproject.cms.service.InformationService;
import com.newproject.cms.service.LanguageSupport;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpHeaders;
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
    public List<InformationResponse> list(
        @RequestParam(required = false) Boolean active,
        @RequestParam(required = false) String lang,
        @RequestHeader(value = HttpHeaders.ACCEPT_LANGUAGE, required = false) String acceptLanguage
    ) {
        String resolvedLanguage = LanguageSupport.resolveLanguage(lang, acceptLanguage);
        return service.list(active, resolvedLanguage);
    }

    @GetMapping("/{id}")
    public InformationResponse get(
        @PathVariable Long id,
        @RequestParam(required = false) String lang,
        @RequestHeader(value = HttpHeaders.ACCEPT_LANGUAGE, required = false) String acceptLanguage
    ) {
        String resolvedLanguage = LanguageSupport.resolveLanguage(lang, acceptLanguage);
        return service.get(id, resolvedLanguage);
    }

    @GetMapping("/slug/{slug}")
    public InformationResponse getBySlug(
        @PathVariable String slug,
        @RequestParam(required = false) String lang,
        @RequestHeader(value = HttpHeaders.ACCEPT_LANGUAGE, required = false) String acceptLanguage
    ) {
        String resolvedLanguage = LanguageSupport.resolveLanguage(lang, acceptLanguage);
        return service.getBySlug(slug, resolvedLanguage);
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

    @PostMapping("/translate")
    public InformationAutoTranslateResponse autoTranslate(@RequestBody InformationAutoTranslateRequest request) {
        return service.autoTranslate(request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
