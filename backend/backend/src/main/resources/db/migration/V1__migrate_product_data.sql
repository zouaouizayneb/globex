-- Migration script to clean up product data structure
-- This script migrates color/size from products to variants and image_url to product_images
-- Then removes obsolete columns from products table

-- Step 1: Create variants for products that have color/size but no variants
INSERT INTO product_variants (sku, color, size, additional_price, stock_quantity, product_id)
SELECT 
    CONCAT('PROD-', p.id_product, '-', UNIX_TIMESTAMP()) as sku,
    p.color,
    p.size,
    0.00 as additional_price,
    COALESCE((SELECT SUM(stock_quantity) FROM product_variants WHERE product_id = p.id_product), 0) as stock_quantity,
    p.id_product
FROM products p
WHERE p.color IS NOT NULL OR p.size IS NOT NULL
AND NOT EXISTS (
    SELECT 1 FROM product_variants v 
    WHERE v.product_id = p.id_product 
    AND (v.color = p.color OR v.size = p.size)
)
AND NOT EXISTS (
    SELECT 1 FROM product_variants v 
    WHERE v.product_id = p.id_product
);

-- Step 2: Create product_images for products that have image_url but no images
INSERT INTO product_images (image_url, alt_text, is_primary, display_order, product_id, variant_id, created_at)
SELECT 
    p.image_url,
    p.name as alt_text,
    1 as is_primary,
    0 as display_order,
    p.id_product,
    NULL as variant_id,
    p.created_at
FROM products p
WHERE p.image_url IS NOT NULL
AND NOT EXISTS (
    SELECT 1 FROM product_images i 
    WHERE i.product_id = p.id_product 
    AND i.image_url = p.image_url
);

-- Step 3: Remove obsolete columns from products table
ALTER TABLE products DROP COLUMN color;
ALTER TABLE products DROP COLUMN size;
ALTER TABLE products DROP COLUMN image_url;
