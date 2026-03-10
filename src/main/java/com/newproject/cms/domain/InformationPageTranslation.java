package com.newproject.cms.domain;

import jakarta.persistence.*;

@Entity
@Table(
    name = "information_page_translation",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_information_page_translation", columnNames = {"page_id", "language_code"})
    }
)
public class InformationPageTranslation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "page_id", nullable = false)
    private InformationPage page;

    @Column(name = "language_code", length = 5, nullable = false)
    private String languageCode;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(columnDefinition = "text", nullable = false)
    private String content;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public InformationPage getPage() {
        return page;
    }

    public void setPage(InformationPage page) {
        this.page = page;
    }

    public String getLanguageCode() {
        return languageCode;
    }

    public void setLanguageCode(String languageCode) {
        this.languageCode = languageCode;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
