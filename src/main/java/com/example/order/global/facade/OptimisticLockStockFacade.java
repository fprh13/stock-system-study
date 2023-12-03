package com.example.order.global.facade;

import com.example.order.service.OptimisticLockSockService;
import org.springframework.stereotype.Component;


@Component
public class OptimisticLockStockFacade {

    private final OptimisticLockSockService optimisticLockSockService;

    public OptimisticLockStockFacade(OptimisticLockSockService optimisticLockSockService) {
        this.optimisticLockSockService = optimisticLockSockService;
    }

    public void decrease(Long id, Long quantity) throws InterruptedException {
        while (true) {
            try{
                optimisticLockSockService.decrease(id,quantity);
                break;
            } catch (Exception e) {
                // 수량 감소 실패시 50밀리세컨드 이후에 진행
                Thread.sleep(50);
            }
        }
    }
}
