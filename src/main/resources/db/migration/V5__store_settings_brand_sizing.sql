ALTER TABLE store_settings
    ADD COLUMN IF NOT EXISTS logo_max_height_px INTEGER NOT NULL DEFAULT 96,
    ADD COLUMN IF NOT EXISTS site_name_font_size_px INTEGER NOT NULL DEFAULT 28;

UPDATE store_settings
SET
    logo_max_height_px = COALESCE(NULLIF(logo_max_height_px, 0), 96),
    site_name_font_size_px = COALESCE(NULLIF(site_name_font_size_px, 0), 28)
WHERE id = 1;
