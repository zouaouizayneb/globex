package tn.fst.backend.backend.migration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.util.List;

@Component
public class DatabaseMigration {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @PostConstruct
    public void migrate() {
        try {
            // STEP 3.1: Add variant_id column to product_images
            addVariantIdColumn();
            
            // STEP 3.2: Migrate variant images to product_images
            migrateVariantImages();
            
            // STEP 3.7: Remove image_url from product_variants
            removeVariantImageUrlColumn();
            
            // STEP 3.8: Remove stock from products (LAST)
            removeProductStockColumn();
            
            System.out.println("✅ Database migration completed successfully");
        } catch (Exception e) {
            System.err.println("❌ Database migration failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void addVariantIdColumn() {
        try {
            // Check if column already exists
            List<String> columns = jdbcTemplate.queryForList(
                "SELECT COLUMN_NAME FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA='database' AND TABLE_NAME='product_images' AND COLUMN_NAME='variant_id'",
                String.class
            );
            
            if (columns.isEmpty()) {
                System.out.println("Adding variant_id column to product_images...");
                jdbcTemplate.execute("ALTER TABLE product_images ADD COLUMN variant_id BIGINT NULL");
                jdbcTemplate.execute("ALTER TABLE product_images ADD CONSTRAINT FK_product_images_variant FOREIGN KEY (variant_id) REFERENCES product_variants(id_variant) ON DELETE SET NULL ON UPDATE CASCADE");
                jdbcTemplate.execute("CREATE INDEX idx_product_images_variant_id ON product_images(variant_id)");
                System.out.println("✅ variant_id column added successfully");
            } else {
                System.out.println("ℹ️ variant_id column already exists, skipping");
            }
        } catch (Exception e) {
            System.err.println("Error adding variant_id column: " + e.getMessage());
            throw e;
        }
    }

    private void migrateVariantImages() {
        try {
            // Check if migration already done
            Long variantImageCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM product_images WHERE variant_id IS NOT NULL",
                Long.class
            );
            
            if (variantImageCount != null && variantImageCount > 0) {
                System.out.println("ℹ️ Variant images already migrated (" + variantImageCount + " records), skipping");
                return;
            }
            
            System.out.println("Migrating variant images to product_images...");
            
            // Migrate variant images
            int migrated = jdbcTemplate.update(
                "INSERT INTO product_images (product_id, variant_id, image_url, alt_text, is_primary, display_order, created_at) " +
                "SELECT pv.product_id, pv.id_variant, pv.image_url, CONCAT('Variant: ', pv.color, ' ', pv.size) as alt_text, 0 as is_primary, 0 as display_order, CURDATE() as created_at " +
                "FROM product_variants pv " +
                "WHERE pv.image_url IS NOT NULL AND pv.image_url != ''"
            );
            
            System.out.println("✅ Migrated " + migrated + " variant images successfully");
        } catch (Exception e) {
            System.err.println("Error migrating variant images: " + e.getMessage());
            throw e;
        }
    }

    private void removeVariantImageUrlColumn() {
        try {
            // Check if column still exists
            List<String> columns = jdbcTemplate.queryForList(
                "SELECT COLUMN_NAME FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA='database' AND TABLE_NAME='product_variants' AND COLUMN_NAME='image_url'",
                String.class
            );
            
            if (columns.isEmpty()) {
                System.out.println("ℹ️ image_url column already removed from product_variants, skipping");
                return;
            }
            
            System.out.println("Removing image_url column from product_variants...");
            jdbcTemplate.execute("ALTER TABLE product_variants DROP COLUMN image_url");
            System.out.println("✅ image_url column removed successfully");
        } catch (Exception e) {
            System.err.println("Error removing image_url column: " + e.getMessage());
            throw e;
        }
    }

    private void removeProductStockColumn() {
        try {
            // Check if column still exists
            List<String> columns = jdbcTemplate.queryForList(
                "SELECT COLUMN_NAME FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA='database' AND TABLE_NAME='products' AND COLUMN_NAME='stock'",
                String.class
            );
            
            if (columns.isEmpty()) {
                System.out.println("ℹ️ stock column already removed from products, skipping");
                return;
            }
            
            System.out.println("Removing stock column from products...");
            jdbcTemplate.execute("ALTER TABLE products DROP COLUMN stock");
            System.out.println("✅ stock column removed successfully");
        } catch (Exception e) {
            System.err.println("Error removing stock column: " + e.getMessage());
            throw e;
        }
    }
}
