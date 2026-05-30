-- STEP 3.2: Migrate variant image_url to product_images
-- This copies variant images to product_images with variant_id

-- First, check what we're migrating
SELECT 
    id_variant, 
    product_id, 
    image_url, 
    color 
FROM product_variants 
WHERE image_url IS NOT NULL AND image_url != '';

-- Migrate variant images to product_images
INSERT INTO product_images (product_id, variant_id, image_url, alt_text, is_primary, display_order, created_at)
SELECT 
    pv.product_id, 
    pv.id_variant, 
    pv.image_url, 
    CONCAT('Variant: ', pv.color, ' ', pv.size) as alt_text,
    0 as is_primary,
    0 as display_order,
    CURDATE() as created_at
FROM product_variants pv
WHERE pv.image_url IS NOT NULL AND pv.image_url != '';

-- Verify the migration
SELECT 
    pi.id_image,
    pi.product_id,
    pi.variant_id,
    pi.image_url,
    pi.alt_text,
    pv.color,
    pv.size
FROM product_images pi
LEFT JOIN product_variants pv ON pi.variant_id = pv.id_variant
WHERE pi.variant_id IS NOT NULL;
