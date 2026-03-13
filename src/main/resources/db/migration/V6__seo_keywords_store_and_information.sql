ALTER TABLE information_page ADD COLUMN IF NOT EXISTS seo_keywords VARCHAR(1024);
ALTER TABLE store_settings ADD COLUMN IF NOT EXISTS site_meta_keywords VARCHAR(1024);

UPDATE store_settings
SET site_meta_keywords = 'ecommerce, catalogo online, offerte, shop'
WHERE site_meta_keywords IS NULL;
