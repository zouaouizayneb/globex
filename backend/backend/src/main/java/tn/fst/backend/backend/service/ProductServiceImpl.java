package tn.fst.backend.backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.fst.backend.backend.dto.ProductResponse;
import tn.fst.backend.backend.entity.Product;
import tn.fst.backend.backend.entity.Stock;
import tn.fst.backend.backend.entity.ProductVariant;
import tn.fst.backend.backend.repository.ProductRepository;
import tn.fst.backend.backend.repository.StockRepository;

import jakarta.annotation.PostConstruct;
import java.util.List;

@Service
public class ProductServiceImpl implements ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private StockRepository stockRepository;

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
        } catch (Exception e) {
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

        // Generate a stock record for the product itself
        Stock productStock = new Stock();
        productStock.setProduct(savedProduct);
        productStock.setQuantity(savedProduct.getStock() != null ? savedProduct.getStock() : 0);
        try {
            stockRepository.save(productStock);
        } catch(Exception e) {
            e.printStackTrace();
        }

        // Generate a stock record for each variant
        if (savedProduct.getVariants() != null) {
            savedProduct.getVariants().forEach(variant -> {
                Stock stock = new Stock();
                stock.setProduct(savedProduct);
                stock.setVariant(variant);
                stock.setQuantity(variant.getStockQuantity() != null ? variant.getStockQuantity() : 0);
                try {
                    stockRepository.save(stock);
                } catch(Exception e) {
                   e.printStackTrace();
                }
            });
        }
        
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
}
