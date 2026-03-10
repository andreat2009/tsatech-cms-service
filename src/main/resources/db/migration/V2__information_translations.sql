CREATE TABLE information_page_translation (
    id BIGSERIAL PRIMARY KEY,
    page_id BIGINT NOT NULL,
    language_code VARCHAR(5) NOT NULL,
    title VARCHAR(255) NOT NULL,
    content TEXT NOT NULL,
    CONSTRAINT fk_information_page_translation_page
        FOREIGN KEY (page_id) REFERENCES information_page(id) ON DELETE CASCADE,
    CONSTRAINT uk_information_page_translation UNIQUE (page_id, language_code)
);

INSERT INTO information_page_translation (page_id, language_code, title, content)
SELECT p.id, l.language_code, p.title, p.content
FROM information_page p
CROSS JOIN (
    VALUES ('it'), ('en'), ('fr'), ('de'), ('es')
) AS l(language_code)
ON CONFLICT (page_id, language_code) DO NOTHING;
