package com.example.order.service;

import com.example.order.domain.Stock;
import com.example.order.repository.StockRepository;
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
class StockServiceTest {

    @Autowired
    private StockService stockService;
    @Autowired
    private PessimisticLockStockService pessimisticLockStockService;
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
     * 기본 재고감소 메소드
     * 문제점 : 요청이 1개씩 들어오는 상황이라 여러개 요청은 불가능하다.
     */
    @Test
    public void 재고감소() {
        stockService.decrease(1L,1L);

        // 100 -1 = 99 가 되어야한다.

        Stock stock = stockRepository.findById(1L).orElseThrow();
        assertEquals(99, stock.getQuantity());
    }

    /**
     * 100개의 재고감소 요청 메서드
     */
    @Test
    public void 동시에_100개의_요청() throws InterruptedException {
        int threadCount = 100;
        // ExecutorService : 비동기로 실행하는 작업을 단순화하여 사용할 수 있게 도와주는 자바의 API
        ExecutorService executorService = Executors.newFixedThreadPool(32);

        // CountDownLatch : 다른 스래드에서 수행 중인 작업이 완료 될 때 까지 대기 할 수 있도록 도와주는 클래스
        CountDownLatch latch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    stockService.decreaseV3(1L,1L);
                } finally {
                    latch.countDown();
                }
            });
        }
        latch.await();
        Stock stock = stockRepository.findById(1L).orElseThrow();
        // 예상 : 100 - (1 * 100) = 0개가 반환 될 것으로 예상
        // race condition 발생 !
        assertEquals(0, stock.getQuantity());
    }

    /**
     * Pessimistic_Lock으로 100개의 재고감소 요청 메서드
     * Pessimistic Lock 확인 쿼리
     * select
     *         s1_0.id,
     *         s1_0.product_id,
     *         s1_0.quantity
     *     from
     *         stock s1_0
     *     where
     *         s1_0.id=? for update
     *
     * for update 부분이 Lock이 걸리는 모습을 보여줌
     *
     * 충돌이 빈번하게 일어난다면 optimistic Lock 보다 성능이 좋음
     * 단점: 별도의 Lock을 걸기 때문에 성능 저하 가능성이 있음
     */
    @Test
    public void Pessimistic_Lock_동시에_100개의_요청() throws InterruptedException {
        int threadCount = 100;
        // ExecutorService : 비동기로 실행하는 작업을 단순화하여 사용할 수 있게 도와주는 자바의 API
        ExecutorService executorService = Executors.newFixedThreadPool(32);

        // CountDownLatch : 다른 스래드에서 수행 중인 작업이 완료 될 때 까지 대기 할 수 있도록 도와주는 클래스
        CountDownLatch latch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    pessimisticLockStockService.decrease(1L,1L);
                } finally {
                    latch.countDown();
                }
            });
        }
        latch.await();
        Stock stock = stockRepository.findById(1L).orElseThrow();
        // 예상 : 100 - (1 * 100) = 0개가 반환 될 것으로 예상
        // race condition 발생 !
        assertEquals(0, stock.getQuantity());
    }


}