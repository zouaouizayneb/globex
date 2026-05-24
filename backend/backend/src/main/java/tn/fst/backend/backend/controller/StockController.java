package tn.fst.backend.backend.controller;

import tn.fst.backend.backend.entity.Stock;
import tn.fst.backend.backend.entity.Product;
import tn.fst.backend.backend.entity.ProductVariant;
import tn.fst.backend.backend.repository.StockRepository;
import tn.fst.backend.backend.repository.ProductRepository;
import tn.fst.backend.backend.repository.ProductVariantRepository;
import tn.fst.backend.backend.dto.StockResponse;

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
        return stockRepository.findAll().stream()
                .map(StockResponse::fromEntity)
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
    public ResponseEntity<StockResponse> updateStock(@PathVariable Long id, @RequestBody Stock stockDetails) {
        Optional<Stock> optionalStock = stockRepository.findById(id);
        if (!optionalStock.isPresent()) {
            return ResponseEntity.notFound().build();
        }

        Stock stock = optionalStock.get();
        stock.setQuantity(stockDetails.getQuantity());

        // Sync back to Product or Variant
        if (stock.getVariant() != null) {
            ProductVariant variant = stock.getVariant();
            variant.setStockQuantity(stock.getQuantity());
            variantRepository.save(variant);
        } else if (stock.getProduct() != null) {
            Product product = stock.getProduct();
            product.setStock(stock.getQuantity());
            productRepository.save(product);
        }

        if (stockDetails.getProduct() != null) {
            Optional<Product> product = productRepository.findById(stockDetails.getProduct().getIdProduct());
            product.ifPresent(stock::setProduct);
        }

        Stock updatedStock = stockRepository.save(stock);
        return ResponseEntity.ok(StockResponse.fromEntity(updatedStock));
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

