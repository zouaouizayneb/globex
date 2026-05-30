package tn.fst.backend.backend.controller;

import tn.fst.backend.backend.entity.Stock;
import tn.fst.backend.backend.entity.Product;
import tn.fst.backend.backend.entity.ProductVariant;
import tn.fst.backend.backend.repository.StockRepository;
import tn.fst.backend.backend.repository.ProductRepository;
import tn.fst.backend.backend.repository.ProductVariantRepository;
import tn.fst.backend.backend.dto.StockResponse;
import tn.fst.backend.backend.dto.ProductResponse;
import tn.fst.backend.backend.dto.ProductVariantResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/stocks")
@CrossOrigin(origins = "http://localhost:4200")
public class StockController {

    @Autowired
    private StockRepository stockRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductVariantRepository variantRepository;

    @GetMapping
    public List<StockResponse> getAllStocks() {
        // Return stock data from ProductVariant directly with product eagerly loaded
        return variantRepository.findAllWithProduct().stream()
                .map(variant -> {
                    StockResponse response = new StockResponse();
                    response.setIdStock(variant.getIdVariant()); // Use variant ID as stock ID
                    response.setQuantity(variant.getStockQuantity());
                    
                    // Set product info
                    if (variant.getProduct() != null) {
                        response.setProduct(ProductResponse.fromEntity(variant.getProduct()));
                    }
                    
                    // Set variant info
                    response.setVariant(ProductVariantResponse.of(
                        variant.getIdVariant(),
                        variant.getSku(),
                        variant.getSize(),
                        variant.getColor(),
                        variant.getImageUrl(),
                        variant.getAdditionalPrice(),
                        variant.getStockQuantity(),
                        variant.getTotalPrice()
                    ));
                    
                    return response;
                })
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<StockResponse> getStockById(@PathVariable Long id) {
        Optional<Stock> stock = stockRepository.findById(id);
        return stock.map(s -> ResponseEntity.ok(StockResponse.fromEntity(s)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<StockResponse> createStock(@RequestBody Stock stock) {
        if (stock.getProduct() != null) {
            Optional<Product> product = productRepository.findById(stock.getProduct().getIdProduct());
            if (product.isPresent()) {
                stock.setProduct(product.get());
            } else {
                return ResponseEntity.badRequest().build();
            }
        } else {
            return ResponseEntity.badRequest().build();
        }

        Stock savedStock = stockRepository.save(stock);
        return ResponseEntity.ok(StockResponse.fromEntity(savedStock));
    }

    @PutMapping("/{id}")
    public ResponseEntity<StockResponse> updateStock(@PathVariable Long id, @RequestBody tn.fst.backend.backend.dto.StockUpdateRequest request) {
        // Update ProductVariant directly
        Optional<ProductVariant> optionalVariant = variantRepository.findById(id);
        if (!optionalVariant.isPresent()) {
            return ResponseEntity.notFound().build();
        }

        ProductVariant variant = optionalVariant.get();
        variant.setStockQuantity(request.getQuantity());
        variantRepository.save(variant);

        // Return updated stock response
        StockResponse response = new StockResponse();
        response.setIdStock(variant.getIdVariant());
        response.setQuantity(variant.getStockQuantity());

        if (variant.getProduct() != null) {
            response.setProduct(ProductResponse.fromEntity(variant.getProduct()));
        }

        response.setVariant(ProductVariantResponse.of(
            variant.getIdVariant(),
            variant.getSku(),
            variant.getSize(),
            variant.getColor(),
            variant.getImageUrl(),
            variant.getAdditionalPrice(),
            variant.getStockQuantity(),
            variant.getTotalPrice()
        ));

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteStock(@PathVariable Long id) {
        if (!stockRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }

        stockRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}

