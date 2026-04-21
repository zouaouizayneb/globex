package tn.fst.backend.backend.controller;

import tn.fst.backend.backend.entity.Stock;
import tn.fst.backend.backend.entity.Product;
import tn.fst.backend.backend.repository.StockRepository;
import tn.fst.backend.backend.repository.ProductRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/stocks")
@CrossOrigin(origins = "*")
public class StockController {

    @Autowired
    private StockRepository stockRepository;

    @Autowired
    private ProductRepository productRepository;

    @GetMapping
    public List<Stock> getAllStocks() {
        return stockRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Stock> getStockById(@PathVariable Long id) {
        Optional<Stock> stock = stockRepository.findById(id);
        return stock.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Stock> createStock(@RequestBody Stock stock) {
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
        return ResponseEntity.ok(savedStock);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Stock> updateStock(@PathVariable Long id, @RequestBody Stock stockDetails) {
        Optional<Stock> optionalStock = stockRepository.findById(id);
        if (!optionalStock.isPresent()) {
            return ResponseEntity.notFound().build();
        }

        Stock stock = optionalStock.get();
        stock.setQuantity(stockDetails.getQuantity());

        if (stockDetails.getProduct() != null) {
            Optional<Product> product = productRepository.findById(stockDetails.getProduct().getIdProduct());
            product.ifPresent(stock::setProduct);
        }

        Stock updatedStock = stockRepository.save(stock);
        return ResponseEntity.ok(updatedStock);
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

