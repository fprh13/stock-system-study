package com.example.order.global.facade;

import com.example.order.repository.RedisLockRepository;
import com.example.order.service.StockService;
import org.springframework.stereotype.Component;

// 실무에서는 재시도가 필요하지 않은 Lock일 때 lettuce 사용
@Component
public class LettuceLockStockFacade {
    /**
     * 구현이 간단하다
     * spring data redis를 이용하면 기본이기 때문에 별도의 라이브러리를 사용하지 않아도 된다.
     * spin lock 방식이기 때문에 동시에 많은 스레드가 lock 획득 대기 상태라면 redis에 부하가 갈 수 있다.
     */



    private final RedisLockRepository redisLockRepository;

    private final StockService stockService;

    public LettuceLockStockFacade(RedisLockRepository redisLockRepository, StockService stockService) {
        this.redisLockRepository = redisLockRepository;
        this.stockService = stockService;
    }

    public void decrease(Long id, Long quantity) throws InterruptedException {
        while (!redisLockRepository.lock(id)) { // 락 획득 실패시 텀을 주고 재실행 redis 로 가는 부하 방지
            Thread.sleep(100);
        }
        try {
            stockService.decrease(id,quantity);
        } finally {
            redisLockRepository.unlock(id);
        }
    }

}
