package tn.fst.backend.backend.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import tn.fst.backend.backend.dto.BestSellerResponse;
import tn.fst.backend.backend.dto.ProductResponse;
import tn.fst.backend.backend.entity.Product;

import java.util.List;

public interface ProductService {

    List<Product> getAllProducts();
    List<Product> getProductsByCategory(Long categoryId);
    Product getProductById(Long id);
    Product createProduct(Product product);
    Product updateProduct(Long id, Product productDetails);
    void deleteProduct(Long id);

    Page<Product> getAllProducts(Pageable pageable);
    Page<ProductResponse> searchProducts(String request);

    List<Product> getFeaturedProducts(int limit);

    List<BestSellerResponse> getBestSellers(int limit);
}
