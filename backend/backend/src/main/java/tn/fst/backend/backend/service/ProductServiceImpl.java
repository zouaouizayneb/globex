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
import tn.fst.backend.backend.dto.ProductResponse;
import tn.fst.backend.backend.dto.ProductVariantResponse;
import tn.fst.backend.backend.entity.OrderDetail;
import tn.fst.backend.backend.entity.Product;
import tn.fst.backend.backend.entity.Stock;
import tn.fst.backend.backend.entity.ProductVariant;
import tn.fst.backend.backend.repository.OrderDetailRepository;
import tn.fst.backend.backend.repository.ProductRepository;
import tn.fst.backend.backend.repository.StockRepository;

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
            List<Product> products = productRepository.findAll();
            for (Product product : products) {
                // Ensure stock for product itself
                boolean productStockExists = stockRepository.findByProductAndVariantIsNull(product).isPresent();
                if (!productStockExists) {
                    Stock stock = new Stock();
                    stock.setProduct(product);
                    stock.setQuantity(product.getStock() != null ? product.getStock() : 0);
                    stockRepository.save(stock);
                }

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
    public Product createProduct(Product product) {
        // Automatically tie variants to the product
        if (product.getVariants() != null) {
            product.getVariants().forEach(v -> v.setProduct(product));
        }

        Product savedProduct = productRepository.save(product);
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
        if (productDetails.getRating() != null) {
            product.setRating(productDetails.getRating());
        }
        if (productDetails.getPrice() != null) {
            product.setPrice(productDetails.getPrice());
        }
        if (productDetails.getStock() != null) {
            product.setStock(productDetails.getStock());
            
            // Sync with Stock table (product-level)
            stockRepository.findByProductAndVariantIsNull(product)
                .ifPresentOrElse(
                    s -> {
                        s.setQuantity(productDetails.getStock());
                        stockRepository.save(s);
                    },
                    () -> {
                        Stock s = new Stock();
                        s.setProduct(product);
                        s.setQuantity(productDetails.getStock());
                        stockRepository.save(s);
                    }
                );
        }
        return productRepository.save(product);
    }

    @Override
    @Transactional
    public void deleteProduct(Long id) {
        productRepository.deleteById(id);
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
                .stock(p.getStock())
                .imageUrl(p.getImageUrl())
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
