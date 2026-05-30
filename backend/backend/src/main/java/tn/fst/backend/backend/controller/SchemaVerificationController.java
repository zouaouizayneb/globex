package tn.fst.backend.backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/verify-schema")
public class SchemaVerificationController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @GetMapping
    public Map<String, Object> verifySchema() {
        Map<String, Object> result = new java.util.HashMap<>();
        
        // Check variant_id in product_images
        List<String> variantIdColumn = jdbcTemplate.queryForList(
            "SELECT COLUMN_NAME FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA='database' AND TABLE_NAME='product_images' AND COLUMN_NAME='variant_id'",
            String.class
        );
        result.put("product_images.variant_id", !variantIdColumn.isEmpty());
        
        // Check image_url in product_variants (should NOT exist)
        List<String> imageUrlColumn = jdbcTemplate.queryForList(
            "SELECT COLUMN_NAME FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA='database' AND TABLE_NAME='product_variants' AND COLUMN_NAME='image_url'",
            String.class
        );
        result.put("product_variants.image_url", !imageUrlColumn.isEmpty());
        
        // Check stock in products (should NOT exist)
        List<String> stockColumn = jdbcTemplate.queryForList(
            "SELECT COLUMN_NAME FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA='database' AND TABLE_NAME='products' AND COLUMN_NAME='stock'",
            String.class
        );
        result.put("products.stock", !stockColumn.isEmpty());
        
        // Count variant images
        Long variantImageCount = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM product_images WHERE variant_id IS NOT NULL",
            Long.class
        );
        result.put("variant_images_count", variantImageCount);
        
        return result;
    }
}
