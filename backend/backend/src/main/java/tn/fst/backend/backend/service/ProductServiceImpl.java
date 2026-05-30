package tn.fst.backend.backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.fst.backend.backend.dto.BestSellerResponse;
import tn.fst.backend.backend.dto.CategoryResponse;
import tn.fst.backend.backend.dto.ProductImageResponse;
import tn.fst.backend.backend.dto.ProductRequest;
import tn.fst.backend.backend.dto.ProductResponse;
import tn.fst.backend.backend.dto.ProductVariantResponse;
import tn.fst.backend.backend.entity.OrderDetail;
import tn.fst.backend.backend.entity.Product;
import tn.fst.backend.backend.entity.ProductStatus;
import tn.fst.backend.backend.entity.Stock;
import tn.fst.backend.backend.entity.ProductVariant;
import tn.fst.backend.backend.entity.ProductImage;
import tn.fst.backend.backend.entity.Category;
import tn.fst.backend.backend.entity.Supplier;
import tn.fst.backend.backend.repository.OrderDetailRepository;
import tn.fst.backend.backend.repository.ProductRepository;
import tn.fst.backend.backend.repository.ProductVariantRepository;
import tn.fst.backend.backend.repository.StockRepository;
import tn.fst.backend.backend.repository.CategoryRepository;
import tn.fst.backend.backend.repository.SupplierRepository;

import jakarta.annotation.PostConstruct;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ProductServiceImpl implements ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private StockRepository stockRepository;

    @Autowired
    private OrderDetailRepository orderDetailRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private SupplierRepository supplierRepository;

    @Autowired
    private ProductVariantRepository productVariantRepository;

    @PostConstruct
    public void syncMissingStock() {
        try {
            // Unbind legacy unique index mapping product_id strictly
            List<String> indexes = jdbcTemplate.queryForList(
                    "SELECT index_name FROM information_schema.statistics WHERE table_schema='database' AND table_name='stock' AND column_name='product_id' AND non_unique=0",
                    String.class
            );
            for (String index : indexes) {
                if (!"PRIMARY".equalsIgnoreCase(index)) {
                     jdbcTemplate.execute("ALTER TABLE stock DROP INDEX " + index);
                }
            }
        } catch (Exception e) {
            System.err.println("Could not drop legacy unique index: " + e.getMessage());
        }

        try {
            // Fix inconsistent stock entries where variant_id is set but product_id is NULL
            jdbcTemplate.update(
                "UPDATE stock s JOIN product_variants v ON s.variant_id = v.id_variant SET s.product_id = v.product_id WHERE s.product_id IS NULL AND s.variant_id IS NOT NULL"
            );

            // Remove duplicate product-level stock entries for products that have variants
            jdbcTemplate.update(
                "DELETE s FROM stock s JOIN products p ON s.product_id = p.id_product WHERE s.variant_id IS NULL AND EXISTS (SELECT 1 FROM product_variants WHERE product_id = p.id_product)"
            );

            List<Product> products = productRepository.findAll();
    
                for (Product product : products) {
                if (product.getVariants() != null) {
                    for (ProductVariant variant : product.getVariants()) {
                        boolean stockExists = stockRepository.findByVariant(variant).isPresent();
                        if (!stockExists) {
                            Stock stock = new Stock();
                            stock.setProduct(product);
                            stock.setVariant(variant);
                            stock.setQuantity(variant.getStockQuantity() != null ? variant.getStockQuantity() : 0);
                            stockRepository.save(stock);
                        }
                    }
                }
            }
        } catch (Exception e ) {
            System.err.println("Error during stock sync: " + e.getMessage());
        }
    }

    @Override
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    @Override
    public List<Product> getProductsByCategory(Long categoryId) {
        return productRepository.findByCategory_IdCategory(categoryId);
    }

    @Override
    public Product getProductById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));
    }

    @Override
    @Transactional
    public Product createProductFromRequest(ProductRequest productRequest) {
        Product product = new Product();
        product.setName(productRequest.getName());
        product.setDescription(productRequest.getDescription());

        // Set base price from request, or calculate from variants if not provided
        if (productRequest.getPrice() != null && productRequest.getPrice() > 0) {
            product.setPrice(java.math.BigDecimal.valueOf(productRequest.getPrice()));
        } else if (productRequest.getVariants() != null && !productRequest.getVariants().isEmpty()) {
            // Calculate base price from first variant's additional price (assuming variant price = base + additional)
            // For now, set to 0 and let the user set it manually
            product.setPrice(java.math.BigDecimal.ZERO);
        } else {
            product.setPrice(java.math.BigDecimal.ZERO);
        }

        if (productRequest.getCategoryId() != null) {
            Category category = categoryRepository.findById(productRequest.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Category not found with id: " + productRequest.getCategoryId()));
            product.setCategory(category);
        }

        if (productRequest.getSupplierId() != null) {
            Supplier supplier = supplierRepository.findById(productRequest.getSupplierId())
                    .orElseThrow(() -> new RuntimeException("Supplier not found with id: " + productRequest.getSupplierId()));
            product.setSupplier(supplier);
        }

        if (productRequest.getStatus() != null) {
            product.setStatus(ProductStatus.valueOf(productRequest.getStatus().toUpperCase()));
        } else {
            product.setStatus(ProductStatus.ACTIVE);
        }

        product.setRating(4.5);
        
        // Save product first without variants
        Product savedProduct = productRepository.save(product);
        
        // Handle variants with their images
        if (productRequest.getVariants() != null && !productRequest.getVariants().isEmpty()) {
            for (ProductRequest.VariantDto variantDto : productRequest.getVariants()) {
                ProductVariant variant = new ProductVariant();
                variant.setProduct(savedProduct);
                variant.setSku(variantDto.getSku());
                variant.setAdditionalPrice(variantDto.getAdditionalPrice() != null ? 
                    java.math.BigDecimal.valueOf(variantDto.getAdditionalPrice()) : java.math.BigDecimal.ZERO);
                variant.setColor(variantDto.getColor());
                variant.setSize(variantDto.getSize());
                variant.setStockQuantity(variantDto.getStockQuantity() != null ? variantDto.getStockQuantity() : 0);
                
                
                if (variantDto.getImages() != null && !variantDto.getImages().isEmpty()) {
                    List<ProductImage> images = new ArrayList<>();
                    for (ProductRequest.ImageDto imageDto : variantDto.getImages()) {
                        ProductImage image = new ProductImage();
                        image.setProduct(savedProduct);
                        image.setVariant(variant);
                        image.setImageUrl(imageDto.getImageUrl());
                        image.setIsPrimary(imageDto.getIsPrimary() != null ? imageDto.getIsPrimary() : false);
                        image.setDisplayOrder(0);
                        images.add(image);
                    }
                    variant.setImages(images);
                }
                
                // Save variant with images (cascade should handle images)
                productVariantRepository.save(variant);
            }
        }
        
        // Reload product to get all associations
        savedProduct = productRepository.findById(savedProduct.getIdProduct()).orElse(savedProduct);
        
        return savedProduct;
    }

    @Override
    @Transactional
    public Product createProduct(Product product) {
        // Handle variants with their images
        List<ProductVariant> variantsToSave = null;
        if (product.getVariants() != null && !product.getVariants().isEmpty()) {
            variantsToSave = new ArrayList<>(product.getVariants());
            product.setVariants(null);
        }

        // Handle product-level images (if any)
        List<ProductImage> imagesToSave = null;
        if (product.getImages() != null && !product.getImages().isEmpty()) {
            imagesToSave = new ArrayList<>(product.getImages());
            product.setImages(null);
        }

        // Save product first
        Product savedProduct = productRepository.save(product);

        // Save variants with product reference and their images
        if (variantsToSave != null) {
            for (ProductVariant variant : variantsToSave) {
                variant.setProduct(savedProduct);
                // Handle variant-level images
                if (variant.getImages() != null && !variant.getImages().isEmpty()) {
                    for (ProductImage image : variant.getImages()) {
                        image.setVariant(variant);
                        image.setProduct(savedProduct);
                    }
                }
            }
            savedProduct.setVariants(variantsToSave);
        }

        // Save product-level images with product reference
        if (imagesToSave != null) {
            for (ProductImage image : imagesToSave) {
                image.setProduct(savedProduct);
            }
            savedProduct.setImages(imagesToSave);
        }

        // Save again to persist variants and images
        savedProduct = productRepository.save(savedProduct);

        return savedProduct;
    }

    @Override
    @Transactional
    public Product updateProduct(Long id, Product productDetails) {
        Product product = getProductById(id);
        if (productDetails.getName() != null) {
            product.setName(productDetails.getName());
        }
        if (productDetails.getDescription() != null) {
            product.setDescription(productDetails.getDescription());
        }
        if (productDetails.getCategory() != null) {
            product.setCategory(productDetails.getCategory());
        }
        if (productDetails.getSupplier() != null) {
            product.setSupplier(productDetails.getSupplier());
        }
        if (productDetails.getStatus() != null) {
            product.setStatus(productDetails.getStatus());
        }
        if (productDetails.getRating() != null) {
            product.setRating(productDetails.getRating());
        }




        if (productDetails.getVariants() != null) {
            java.util.Map<String, ProductVariant> existingVariants = product.getVariants().stream()
                .filter(v -> v.getSku() != null)
                .collect(java.util.stream.Collectors.toMap(ProductVariant::getSku, v -> v, (v1, v2) -> v1));
            
            java.util.List<ProductVariant> updatedVariants = new java.util.ArrayList<>();
            for (ProductVariant newV : productDetails.getVariants()) {
                if (newV.getSku() != null && existingVariants.containsKey(newV.getSku())) {
                    ProductVariant existingV = existingVariants.get(newV.getSku());
                    existingV.setColor(newV.getColor());
                    existingV.setSize(newV.getSize());
                    existingV.setAdditionalPrice(newV.getAdditionalPrice());
                    existingV.setStockQuantity(newV.getStockQuantity());
                    // Handle variant-level images
                    if (newV.getImages() != null) {
                        existingV.getImages().clear();
                        for (ProductImage img : newV.getImages()) {
                            img.setVariant(existingV);
                            img.setProduct(product);
                            existingV.getImages().add(img);
                        }
                    }
                    updatedVariants.add(existingV);
                    existingVariants.remove(newV.getSku());
                } else {
                    newV.setProduct(product);
                    // Handle variant-level images for new variants
                    if (newV.getImages() != null) {
                        for (ProductImage img : newV.getImages()) {
                            img.setVariant(newV);
                            img.setProduct(product);
                        }
                    }
                    updatedVariants.add(newV);
                }
            }

            for (ProductVariant toDelete : existingVariants.values()) {
                Long variantId = toDelete.getIdVariant();
                if (variantId != null) {
                    jdbcTemplate.update("DELETE FROM cart_items WHERE variant_id = ?", variantId);
                    jdbcTemplate.update("DELETE FROM wishlist_items WHERE variant_id = ?", variantId);
                    jdbcTemplate.update("DELETE FROM stock_movements WHERE variant_id = ?", variantId);
                    jdbcTemplate.update("DELETE FROM purchase_order_items WHERE variant_id = ?", variantId);
                    jdbcTemplate.update("DELETE FROM stock WHERE variant_id = ?", variantId);
                    jdbcTemplate.update("UPDATE order_details SET variant_id = NULL WHERE variant_id = ?", variantId);
                }
            }

            product.getVariants().clear();
            product.getVariants().addAll(updatedVariants);
        }
        
        if (productDetails.getImages() != null) {
            product.getImages().clear();
            for(ProductImage img : productDetails.getImages()) {
                img.setProduct(product);
                product.getImages().add(img);
            }
        }

        return productRepository.save(product);
    }

    @Override
    @Transactional
    public Product updateProductFromRequest(Long id, ProductRequest productRequest) {
        Product product = getProductById(id);
        
        // Update basic fields
        if (productRequest.getName() != null) {
            product.setName(productRequest.getName());
        }
        if (productRequest.getDescription() != null) {
            product.setDescription(productRequest.getDescription());
        }
        if (productRequest.getPrice() != null && productRequest.getPrice() > 0) {
            product.setPrice(java.math.BigDecimal.valueOf(productRequest.getPrice()));
        }
        
        // Update category
        if (productRequest.getCategoryId() != null) {
            Category category = categoryRepository.findById(productRequest.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Category not found with id: " + productRequest.getCategoryId()));
            product.setCategory(category);
        }
        
        // Update supplier
        if (productRequest.getSupplierId() != null) {
            Supplier supplier = supplierRepository.findById(productRequest.getSupplierId())
                    .orElseThrow(() -> new RuntimeException("Supplier not found with id: " + productRequest.getSupplierId()));
            product.setSupplier(supplier);
        }
        
        // Update status
        if (productRequest.getStatus() != null) {
            product.setStatus(ProductStatus.valueOf(productRequest.getStatus().toUpperCase()));
        }
        
        // Handle variants
        if (productRequest.getVariants() != null && !productRequest.getVariants().isEmpty()) {
            java.util.Map<String, ProductVariant> existingVariants = product.getVariants().stream()
                .filter(v -> v.getSku() != null)
                .collect(java.util.stream.Collectors.toMap(ProductVariant::getSku, v -> v, (v1, v2) -> v1));
            
            java.util.List<ProductVariant> updatedVariants = new java.util.ArrayList<>();
            for (ProductRequest.VariantDto variantDto : productRequest.getVariants()) {
                if (variantDto.getSku() != null && existingVariants.containsKey(variantDto.getSku())) {
                    ProductVariant existingV = existingVariants.get(variantDto.getSku());
                    existingV.setColor(variantDto.getColor());
                    existingV.setSize(variantDto.getSize());
                    existingV.setAdditionalPrice(variantDto.getAdditionalPrice() != null ? 
                        java.math.BigDecimal.valueOf(variantDto.getAdditionalPrice()) : java.math.BigDecimal.ZERO);
                    existingV.setStockQuantity(variantDto.getStockQuantity() != null ? variantDto.getStockQuantity() : 0);
                    
                    // Handle variant-level images
                    if (variantDto.getImages() != null) {
                        existingV.getImages().clear();
                        for (ProductRequest.ImageDto imageDto : variantDto.getImages()) {
                            ProductImage image = new ProductImage();
                            image.setProduct(product);
                            image.setVariant(existingV);
                            image.setImageUrl(imageDto.getImageUrl());
                            image.setIsPrimary(imageDto.getIsPrimary() != null ? imageDto.getIsPrimary() : false);
                            image.setDisplayOrder(0);
                            existingV.getImages().add(image);
                        }
                    }
                    updatedVariants.add(existingV);
                    existingVariants.remove(variantDto.getSku());
                } else {
                    ProductVariant newV = new ProductVariant();
                    newV.setProduct(product);
                    newV.setSku(variantDto.getSku());
                    newV.setAdditionalPrice(variantDto.getAdditionalPrice() != null ? 
                        java.math.BigDecimal.valueOf(variantDto.getAdditionalPrice()) : java.math.BigDecimal.ZERO);
                    newV.setColor(variantDto.getColor());
                    newV.setSize(variantDto.getSize());
                    newV.setStockQuantity(variantDto.getStockQuantity() != null ? variantDto.getStockQuantity() : 0);
                    
                    // Handle variant-level images for new variants
                    if (variantDto.getImages() != null) {
                        List<ProductImage> images = new ArrayList<>();
                        for (ProductRequest.ImageDto imageDto : variantDto.getImages()) {
                            ProductImage image = new ProductImage();
                            image.setProduct(product);
                            image.setVariant(newV);
                            image.setImageUrl(imageDto.getImageUrl());
                            image.setIsPrimary(imageDto.getIsPrimary() != null ? imageDto.getIsPrimary() : false);
                            image.setDisplayOrder(0);
                            images.add(image);
                        }
                        newV.setImages(images);
                    }
                    updatedVariants.add(newV);
                }
            }
            
            // Delete removed variants
            for (ProductVariant toDelete : existingVariants.values()) {
                Long variantId = toDelete.getIdVariant();
                if (variantId != null) {
                    jdbcTemplate.update("DELETE FROM cart_items WHERE variant_id = ?", variantId);
                    jdbcTemplate.update("DELETE FROM wishlist_items WHERE variant_id = ?", variantId);
                    jdbcTemplate.update("DELETE FROM stock_movements WHERE variant_id = ?", variantId);
                    jdbcTemplate.update("DELETE FROM purchase_order_items WHERE variant_id = ?", variantId);
                    jdbcTemplate.update("DELETE FROM stock WHERE variant_id = ?", variantId);
                    jdbcTemplate.update("UPDATE order_details SET variant_id = NULL WHERE variant_id = ?", variantId);
                }
            }
            
            product.getVariants().clear();
            product.getVariants().addAll(updatedVariants);
        }
        
        return productRepository.save(product);
    }

    @Override
    @Transactional
    public void deleteProduct(Long id) {
        System.out.println("Attempting to delete product with id: " + id);
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));

        try {
            // Delete from referencing tables using JdbcTemplate to bypass foreign key constraint cascades missing in JPA
            System.out.println("Deleting from cart_items...");
            jdbcTemplate.update("DELETE FROM cart_items WHERE variant_id IN (SELECT id_variant FROM product_variants WHERE product_id = ?)", id);
            
            System.out.println("Deleting from wishlist_items...");
            jdbcTemplate.update("DELETE FROM wishlist_items WHERE variant_id IN (SELECT id_variant FROM product_variants WHERE product_id = ?)", id);
            
            System.out.println("Deleting from stock_movements...");
            jdbcTemplate.update("DELETE FROM stock_movements WHERE variant_id IN (SELECT id_variant FROM product_variants WHERE product_id = ?)", id);
            
            System.out.println("Deleting from purchase_order_items...");
            jdbcTemplate.update("DELETE FROM purchase_order_items WHERE variant_id IN (SELECT id_variant FROM product_variants WHERE product_id = ?)", id);

            // Delete related stock entries by both variant_id and product_id to avoid foreign key constraints
            System.out.println("Deleting stock entries by variant_id...");
            jdbcTemplate.update("DELETE FROM stock WHERE variant_id IN (SELECT id_variant FROM product_variants WHERE product_id = ?)", id);
            System.out.println("Deleting stock entries by product_id...");
            jdbcTemplate.update("DELETE FROM stock WHERE product_id = ?", id);

            // Delete related order details
            System.out.println("Deleting order details...");
            jdbcTemplate.update("DELETE FROM order_details WHERE product_id = ?", id);

            // Delete the product (variants and images will cascade due to CascadeType.ALL)
            System.out.println("Deleting product...");
            productRepository.delete(product);
            
            System.out.println("Product deleted successfully: " + id);
        } catch (Exception e) {
            System.err.println("Error deleting product " + id + ": " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to delete product: " + e.getMessage(), e);
        }
    }

    @Override
    public Page<Product> getAllProducts(Pageable pageable) {
        return productRepository.findAll(pageable);
    }

    @Override
    public Page<ProductResponse> searchProducts(String request) {
        Pageable pageable = PageRequest.of(0, 50);
        Page<Product> page = productRepository.findByNameContainingIgnoreCase(request, pageable);
        return page.map(ProductResponse::fromEntity);
    }

    @Override
    public List<Product> getFeaturedProducts(int limit) {
        PageRequest pageRequest = PageRequest.of(0, limit);
        return productRepository.findAll(pageRequest).getContent();
    }

    @Override
    public List<BestSellerResponse> getBestSellers(int limit) {
        // Aggregate units sold per product from all order details
        List<OrderDetail> allDetails = orderDetailRepository.findAll();

        Map<Long, Integer> soldByProduct = new HashMap<>();
        for (OrderDetail detail : allDetails) {
            if (detail.getProduct() != null) {
                Long pid = detail.getProduct().getIdProduct();
                soldByProduct.merge(pid, detail.getQuantity(), Integer::sum);
            }
        }

        // Get all products, sort by sold count descending
        List<Product> allProducts = productRepository.findAll();

        // If we have order data, sort by it; otherwise fall back to rating
        List<Product> sorted;
        if (!soldByProduct.isEmpty()) {
            sorted = allProducts.stream()
                .sorted((a, b) -> {
                    int soldA = soldByProduct.getOrDefault(a.getIdProduct(), 0);
                    int soldB = soldByProduct.getOrDefault(b.getIdProduct(), 0);
                    return Integer.compare(soldB, soldA); // descending
                })
                .limit(limit)
                .collect(Collectors.toList());
        } else {
            // Fallback: sort by rating descending
            sorted = allProducts.stream()
                .sorted((a, b) -> Double.compare(
                    b.getRating() != null ? b.getRating() : 0.0,
                    a.getRating() != null ? a.getRating() : 0.0))
                .limit(limit)
                .collect(Collectors.toList());
        }

        // Find max sold count for relative percentage calculation
        int maxSold = sorted.stream()
            .mapToInt(p -> soldByProduct.getOrDefault(p.getIdProduct(), 0))
            .max()
            .orElse(1);
        // Avoid division by zero
        int effectiveMax = Math.max(maxSold, 1);

        // Map to BestSellerResponse
        List<BestSellerResponse> result = new ArrayList<>();
        for (Product p : sorted) {
            int sold = soldByProduct.getOrDefault(p.getIdProduct(), 0);
            // If no real data, assign a plausible value based on rating
            if (sold == 0 && soldByProduct.isEmpty()) {
                sold = (int)((p.getRating() != null ? p.getRating() : 4.0) * 20);
            }

            int percentage = effectiveMax > 0
                ? Math.max(10, (int)((sold * 100.0) / effectiveMax))
                : 50;

            // Build category
            CategoryResponse catResp = null;
            if (p.getCategory() != null) {
                var cat = p.getCategory();
                catResp = CategoryResponse.of(cat.getIdCategory(), cat.getName(), cat.getDescription(), cat.getImage());
            }

            // Build variants
            List<ProductVariantResponse> variantResps = null;
            if (p.getVariants() != null) {
                variantResps = p.getVariants().stream()
                    .map(v -> ProductVariantResponse.of(
                        v.getIdVariant(), v.getSku(), v.getSize(), v.getColor(),
                        v.getImageUrl(), v.getAdditionalPrice(), v.getStockQuantity(), v.getTotalPrice()))
                    .collect(Collectors.toList());
            }

            // Build images
            List<ProductImageResponse> imageResps = null;
            if (p.getImages() != null) {
                imageResps = p.getImages().stream()
                    .map(img -> ProductImageResponse.of(
                        img.getIdImage(), img.getImageUrl(), img.getAltText(),
                        img.getIsPrimary(), img.getDisplayOrder(), img.getCreatedAt()))
                    .collect(Collectors.toList());
            }

            // Approximate reviews from stock/rating
            int reviews = sold > 0 ? Math.max(sold / 2, 5) : (int)((p.getRating() != null ? p.getRating() : 4.0) * 15);

            BestSellerResponse resp = BestSellerResponse.builder()
                .idProduct(p.getIdProduct())
                .name(p.getName())
                .description(p.getDescription())
                .price(p.getPrice())
                .rating(p.getRating())
                .category(catResp)
                .variants(variantResps)
                .images(imageResps)
                .soldCount(sold)
                .soldPercentage(percentage)
                .reviews(reviews)
                .build();

            result.add(resp);
        }

        return result;
    }
}
