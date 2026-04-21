package tn.fst.backend.backend.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import tn.fst.backend.backend.dto.ProductSearchRequest;
import tn.fst.backend.backend.dto.ProductResponse;
import tn.fst.backend.backend.entity.Product;

import java.util.List;

public interface ProductService {

    // Basic CRUD
    List<Product> getAllProducts();
    Product getProductById(Long id);
    Product createProduct(Product product);
    Product updateProduct(Long id, Product productDetails);
    void deleteProduct(Long id);

    // Pagination
    Page<Product> getAllProducts(Pageable pageable);

    // Search and Filter - NEW!
    Page<ProductResponse> searchProducts(ProductSearchRequest request);
}
