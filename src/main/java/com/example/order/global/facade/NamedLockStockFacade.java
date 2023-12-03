package com.example.order.global.facade;

import com.example.order.repository.LockRepository;
import com.example.order.service.StockService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NamedLockStockFacade {

    private final LockRepository lockRepository;
    private final StockService stockService;

    public NamedLockStockFacade(LockRepository lockRepository, StockService stockService) {
        this.lockRepository = lockRepository;
        this.stockService = stockService;
    }

    @Transactional
    public void decrease(Long id, Long quantity) {
        try {
            lockRepository.getLock(id.toString());
            stockService.decrease(id,quantity);
        } finally {
            // 모든 로직 종료시 락 해제
            lockRepository.releaseLock(id.toString());
        }
    }
}
