package com.example.order.global.facade;

import com.example.order.service.StockService;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

// 실무에서는 재시도가 필요한 Lock 일 때  Redisson 사용
@Component
public class RedissonLockStockFacade {

    /**
     * 락 획득 재시도를 기본으로 제공한다.
     * pub-sub 방식으로 구현이 되어있기 때문에 lettuce와 비교했을 때 redis 부하가 덜 간다.
     * 별도의 라이브러리를 사용해야한다.
     * lock을 라이브러리 차원에서 제공해주기 때문에 사용법을 공부해야한다.
     */

    private RedissonClient redissonClient;

    private StockService stockService;

    public RedissonLockStockFacade(RedissonClient redissonClient, StockService stockService) {
        this.redissonClient = redissonClient;
        this.stockService = stockService;
    }

    public void decrease(Long id, Long quantity) {
        RLock lock = redissonClient.getLock(id.toString()); // 락 객체 가지고 오기
        try {
            // 몇 초 동안 락 획득을 시도 할 것인지 와 점유할 것인지
            boolean available = lock.tryLock(10,1, TimeUnit.SECONDS);
            if (!available) {
                System.out.println("lock 획득 실패");
                return;
            }
            stockService.decrease(id,quantity);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            // 락 해제
            lock.unlock();
        }

    }
}
