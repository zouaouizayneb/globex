package tn.fst.backend.backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.fst.backend.backend.dto.ProductSearchRequest;
import tn.fst.backend.backend.dto.ProductResponse;
import tn.fst.backend.backend.entity.Product;
import tn.fst.backend.backend.exeptions.ResourceNotFoundException;
import tn.fst.backend.backend.repository.ProductRepository;
import tn.fst.backend.backend.specification.ProductSpecification;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;

    @Override
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    @Override
    public Page<Product> getAllProducts(Pageable pageable) {
        return productRepository.findAll(pageable);
    }

    @Override
    public Product getProductById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", id));
    }

    @Override
    @Transactional
    public Product createProduct(Product product) {
        return productRepository.save(product);
    }

    @Override
    @Transactional
    public Product updateProduct(Long id, Product productDetails) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", id));

        product.setName(productDetails.getName());
        product.setDescription(productDetails.getDescription());
        product.setPrice(productDetails.getPrice());

        if (productDetails.getCategory() != null) {
            product.setCategory(productDetails.getCategory());
        }

        return productRepository.save(product);
    }

    @Override
    @Transactional
    public void deleteProduct(Long id) {
        if (!productRepository.existsById(id)) {
            throw new ResourceNotFoundException("Product", id);
        }
        productRepository.deleteById(id);
    }

    // NEW: Search and Filter Implementation
    @Override
    public Page<ProductResponse> searchProducts(ProductSearchRequest request) {

        // Build specification from request
        Specification<Product> spec = ProductSpecification.buildSpecification(request);

        // Build sort
        Sort sort = buildSort(request.getSortBy(), request.getSortDirection());

        // Build pageable
        Pageable pageable = PageRequest.of(
                request.getPage(),
                request.getSize(),
                sort
        );

        // Execute query
        Page<Product> productPage = productRepository.findAll(spec, pageable);

        // Convert to response DTOs
        return productPage.map(this::mapToProductResponse);
    }

    /**
     * Build Sort object from sort parameters
     */
    private Sort buildSort(String sortBy, String sortDirection) {
        // Default to name if null or empty
        if (sortBy == null || sortBy.trim().isEmpty()) {
            sortBy = "name";
        }

        // Determine direction
        Sort.Direction direction = "DESC".equalsIgnoreCase(sortDirection)
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;

        // Map sort field names
        switch (sortBy.toLowerCase()) {
            case "price":
                return Sort.by(direction, "price");
            case "name":
                return Sort.by(direction, "name");
            case "createdat":
            case "newest":
                return Sort.by(direction, "createdAt");
            default:
                return Sort.by(Sort.Direction.ASC, "name");
        }
    }

    /**
     * Convert Product entity to ProductResponse DTO
     */
    private ProductResponse mapToProductResponse(Product product) {
        return ProductResponse.builder()
                .idProduct(product.getIdProduct())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .categoryName(product.getCategory() != null ? product.getCategory().getName() : null)
                .categoryId(product.getCategory() != null ? product.getCategory().getIdCategory() : null)
                .primaryImageUrl(product.getPrimaryImageUrl())
                .totalStock(product.getTotalStock())
                .inStock(product.isInStock())
                .build();
    }
}