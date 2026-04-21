package tn.fst.backend.backend.service;

import tn.fst.backend.backend.entity.Stock;
import tn.fst.backend.backend.entity.Product;
import tn.fst.backend.backend.repository.StockRepository;
import tn.fst.backend.backend.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class StockServiceImpl implements StockService {

    @Autowired
    private StockRepository stockRepository;

    @Autowired
    private ProductRepository productRepository;

    @Override
    public List<Stock> getAllStocks() {
        return stockRepository.findAll();
    }

    @Override
    public Optional<Stock> getStockById(Long id) {
        return stockRepository.findById(id);
    }

    @Override
    public Stock createStock(Stock stock) {
        if (stock.getProduct() != null) {
            productRepository.findById(stock.getProduct().getIdProduct())
                    .ifPresent(stock::setProduct);
        }
        return stockRepository.save(stock);
    }

    @Override
    public Stock updateStock(Long id, Stock stockDetails) {
        Optional<Stock> optional = stockRepository.findById(id);
        if (!optional.isPresent()) throw new RuntimeException("Stock not found with id: " + id);

        Stock stock = optional.get();
        stock.setQuantity(stockDetails.getQuantity());

        if (stockDetails.getProduct() != null)
            productRepository.findById(stockDetails.getProduct().getIdProduct())
                    .ifPresent(stock::setProduct);

        return stockRepository.save(stock);
    }

    @Override
    public void deleteStock(Long id) {
        if (!stockRepository.existsById(id)) throw new RuntimeException("Stock not found with id: " + id);
        stockRepository.deleteById(id);
    }
}
