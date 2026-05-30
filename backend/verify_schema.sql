-- Verify database schema changes
SELECT 
    'product_images.variant_id' as column_name,
    COUNT(*) as exists_count
FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_SCHEMA='database' 
AND TABLE_NAME='product_images' 
AND COLUMN_NAME='variant_id'

UNION ALL

SELECT 
    'product_variants.image_url' as column_name,
    COUNT(*) as exists_count
FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_SCHEMA='database' 
AND TABLE_NAME='product_variants' 
AND COLUMN_NAME='image_url'

UNION ALL

SELECT 
    'products.stock' as column_name,
    COUNT(*) as exists_count
FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_SCHEMA='database' 
AND TABLE_NAME='products' 
AND COLUMN_NAME='stock';

-- Verify variant images migrated
SELECT COUNT(*) as variant_images_count
FROM product_images 
WHERE variant_id IS NOT NULL;
