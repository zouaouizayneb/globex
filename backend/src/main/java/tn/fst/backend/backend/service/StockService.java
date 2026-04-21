package tn.fst.backend.backend.service;

import tn.fst.backend.backend.entity.Stock;
import java.util.List;
import java.util.Optional;

public interface StockService {
    List<Stock> getAllStocks();
    Optional<Stock> getStockById(Long id);
    Stock createStock(Stock stock);
    Stock updateStock(Long id, Stock stock);
    void deleteStock(Long id);
}
