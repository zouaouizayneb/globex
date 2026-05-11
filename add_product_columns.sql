-- Add color and size columns to products table
ALTER TABLE products ADD COLUMN color VARCHAR(255) DEFAULT NULL AFTER description;
ALTER TABLE products ADD COLUMN size VARCHAR(255) DEFAULT NULL AFTER color;
ALTER TABLE products ADD COLUMN image_url VARCHAR(255) DEFAULT NULL AFTER size;
