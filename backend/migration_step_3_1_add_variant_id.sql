-- STEP 3.1: Add variant_id column to product_images
-- This is a safe migration - adding a nullable column

-- Add variant_id column (nullable for backward compatibility)
ALTER TABLE product_images 
ADD COLUMN variant_id BIGINT NULL;

-- Add foreign key constraint
ALTER TABLE product_images 
ADD CONSTRAINT FK_product_images_variant 
FOREIGN KEY (variant_id) REFERENCES product_variants(id_variant) 
ON DELETE SET NULL ON UPDATE CASCADE;

-- Add index for performance
CREATE INDEX idx_product_images_variant_id ON product_images(variant_id);
