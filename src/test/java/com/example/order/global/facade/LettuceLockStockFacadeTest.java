package com.example.order.global.facade;

import com.example.order.domain.Stock;
import com.example.order.repository.StockRepository;
import com.example.order.service.StockService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class LettuceLockStockFacadeTest {

    @Autowired
    private StockService stockService;
    @Autowired
    private LettuceLockStockFacade lettuceLockStockFacade;
    @Autowired
    private StockRepository stockRepository;


    @BeforeEach // 테스트를 실행 하기 전에 재고를 생성하기 위한 Annotation
    public void before() {
        stockRepository.saveAndFlush(new Stock(1L, 100L));
    }

    @AfterEach // 테스트가 끝나면 모든 아이템 제거
    public void after() {
        stockRepository.deleteAll();
    }


    /**
     * 구현이 간단하다는 장점이 있다.
     * 단점은 spin Lock 방식임으로 redis에 부화를 줄수 있다. (Lock을 사용할 수 있는지 반복적으로 확인하며 락 획득을 시도하는 방식)
     * 그래서 로직 중간에 TreadSleep을 하는 while 문 을 추가하여 redis에 부하를 줄여준 것
     */
    @Test
    public void Redis_Lettuce_Lock_동시에_100개의_요청() throws InterruptedException {
        int threadCount = 100;
        // ExecutorService : 비동기로 실행하는 작업을 단순화하여 사용할 수 있게 도와주는 자바의 API
        ExecutorService executorService = Executors.newFixedThreadPool(32);

        // CountDownLatch : 다른 스래드에서 수행 중인 작업이 완료 될 때 까지 대기 할 수 있도록 도와주는 클래스
        CountDownLatch latch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    lettuceLockStockFacade.decrease(1L,1L);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                } finally {
                    latch.countDown();
                }
            });
        }
        latch.await();
        Stock stock = stockRepository.findById(1L).orElseThrow();
        assertEquals(0, stock.getQuantity());
    }

}