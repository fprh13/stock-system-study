package com.example.order.service;

import com.example.order.domain.Stock;
import com.example.order.repository.StockRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OptimisticLockSockService {

    private final StockRepository stockRepository;

    public OptimisticLockSockService(StockRepository stockRepository) {
        this.stockRepository = stockRepository;
    }

    @Transactional
    public void decrease(Long id, Long quantity) {
        Stock stock = stockRepository.findByIdWithOptimisticLock(id);

        stock.decrease(quantity);

        stockRepository.save(stock);

        // 실패 했을 때 재실행 하는 facade 가 필요 함
    }

}
