package tn.fst.backend.backend.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import tn.fst.backend.backend.dto.ProductSearchRequest;
import tn.fst.backend.backend.dto.ProductResponse;
import tn.fst.backend.backend.entity.Product;
import tn.fst.backend.backend.service.ProductService;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/products")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping
    public ResponseEntity<Page<Product>> getAllProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "ASC") String sortDirection) {

        Sort sort = sortDirection.equalsIgnoreCase("ASC")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Page<Product> products = productService.getAllProducts(PageRequest.of(page, size, sort));
        return ResponseEntity.ok(products);
    }

    @GetMapping("/all")
    public ResponseEntity<List<Product>> getAllProductsList() {
        return ResponseEntity.ok(productService.getAllProducts());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable Long id) {
        Product product = productService.getProductById(id);
        return ResponseEntity.ok(product);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Product> createProduct(@Valid @RequestBody Product product) {
        Product createdProduct = productService.createProduct(product);
        return new ResponseEntity<>(createdProduct, HttpStatus.CREATED);
    }

    // Update product (ADMIN only)
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Product> updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody Product productDetails) {
        Product updatedProduct = productService.updateProduct(id, productDetails);
        return ResponseEntity.ok(updatedProduct);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }



    /**
     * Search and filter products using query parameters
     * GET /api/products/search?keyword=phone&categoryId=1&minPrice=100&maxPrice=1000&inStock=true&sortBy=price&sortDirection=ASC&page=0&size=10
     */
    @GetMapping("/search")
    public ResponseEntity<Page<ProductResponse>> searchProducts(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) Boolean inStock,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "ASC") String sortDirection,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {

        // Build search request
        ProductSearchRequest request = ProductSearchRequest.builder()
                .keyword(keyword)
                .categoryId(categoryId)
                .minPrice(minPrice)
                .maxPrice(maxPrice)
                .inStock(inStock)
                .sortBy(sortBy)
                .sortDirection(sortDirection)
                .page(page)
                .size(size)
                .build();

        // Execute search
        Page<ProductResponse> results = productService.searchProducts(request);

        return ResponseEntity.ok(results);
    }

    /**
     * Alternative: Accept entire search request as body (for complex filters)
     * POST /api/products/search
     */
    @PostMapping("/search")
    public ResponseEntity<Page<ProductResponse>> searchProductsWithBody(
            @RequestBody ProductSearchRequest request) {

        Page<ProductResponse> results = productService.searchProducts(request);
        return ResponseEntity.ok(results);
    }

    /**
     * Quick search by keyword only
     * GET /api/products/quick-search?q=phone
     */
    @GetMapping("/quick-search")
    public ResponseEntity<Page<ProductResponse>> quickSearch(
            @RequestParam String q,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {

        ProductSearchRequest request = ProductSearchRequest.builder()
                .keyword(q)
                .page(page)
                .size(size)
                .build();

        Page<ProductResponse> results = productService.searchProducts(request);
        return ResponseEntity.ok(results);
    }

    /**
     * Get products by category
     * GET /api/products/category/1
     */
    @GetMapping("/category/{categoryId}")
    public ResponseEntity<Page<ProductResponse>> getProductsByCategory(
            @PathVariable Long categoryId,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {

        ProductSearchRequest request = ProductSearchRequest.builder()
                .categoryId(categoryId)
                .page(page)
                .size(size)
                .build();

        Page<ProductResponse> results = productService.searchProducts(request);
        return ResponseEntity.ok(results);
    }

    /**
     * Get products in stock only
     * GET /api/products/in-stock
     */
    @GetMapping("/in-stock")
    public ResponseEntity<Page<ProductResponse>> getInStockProducts(
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {

        ProductSearchRequest request = ProductSearchRequest.builder()
                .inStock(true)
                .page(page)
                .size(size)
                .build();

        Page<ProductResponse> results = productService.searchProducts(request);
        return ResponseEntity.ok(results);
    }
}
