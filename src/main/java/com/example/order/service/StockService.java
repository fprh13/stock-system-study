package com.example.order.service;

import com.example.order.domain.Stock;
import com.example.order.repository.StockRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
// 함부로 Transactinal read 를 깔고 가지 말자
//@Transactional(readOnly = true)
public class StockService {

    private final StockRepository stockRepository;

    public StockService(StockRepository stockRepository) {
        this.stockRepository = stockRepository;
    }

    /**
     * 여러개 동시 요청 시 race condition 문제 발생
     */
//    @Transactional
    public void decreaseV1(Long id, Long quantity) {
        // Stock 조회
        // 재고 감소 후
        // 갱신 값 저장
        Stock stock = stockRepository.findById(id).orElseThrow();
        stock.decrease(quantity);
        stockRepository.saveAndFlush(stock);
    }

    /**
     * synchronized : 해당 메서드는 한개의 스래드만 접근 가능
     * 문제점 : 테스트 코드의 Transactional 의 문제
     * 1. transactional 시작
     * 2. decrease 메서드 동작
     * 3. transational 커밋
     * 이유 : 종료 시점에 DB에 업데이트를 진행 하기 때문이다.
     * 종료 전에 decrease 메서드에 다른 스래드가 접근 할 수 있기 때문이다.
     * 그렇다면 다른 스래드는 갱신 되기 전에 값을 가져가서 이전과 동일한 문제가 발생
     */
    @Transactional
    public synchronized void decreaseV2(Long id, Long quantity) {
        // Stock 조회
        // 재고 감소 후
        // 갱신 값 저장
        Stock stock = stockRepository.findById(id).orElseThrow();
        stock.decrease(quantity);
        stockRepository.saveAndFlush(stock);
    }

    /**
     * Transactional 삭제 메서
     * synchronized 의 문제 : 서버가 한대에서만 작동하는 원리이다.
     * 여러 서버에서 진행 할 시에 문제가 생긴다.
     * synchronized 는 각 프로세스 안에서 만 보장되기 때문에 서버가 여러대 라면 race condition 발생
     * 그렇기 때문에 synchronized 거의 사용을 안한다.
     * synchronized 가 아니라면 DB락을 사용해서 하는 방법을 고려
     */
//    @Transactional
    public synchronized void decreaseV3(Long id, Long quantity) {
        // Stock 조회
        // 재고 감소 후
        // 갱신 값 저장
        Stock stock = stockRepository.findById(id).orElseThrow();
        stock.decrease(quantity);
        stockRepository.saveAndFlush(stock);
    }

    /**
     * Pessimistic Lock
     * - 실제로 데이터에 Lock을 걸어서 정합성을 맞추는 방법
     * - exclusive Lock을 걸게 되면 다른 트랜잭션에서는 Lock 이 해제되기 전에 데이터를 가져갈 수 없게 된다.
     * - 데드락이 걸릴 수 있기 때문에 주의!
     *
     * 요약 : 여러 서버가 DB를 바라보고 있을 때 server 1 에서 lock을 걸고 데이터를 가져가게 된다면 server 1이 lock을
     * 해제하기 전까지는 다른 서버에서는 접근이 불가능해진다.
     * 락을 가진 스래드만 접근이 가능함
     *
     *
     * Optimistic Lock
     * - 실제로 Lock을 이용하지 않고 버젼을 이용함으로써 정합성을 맞추는 방법
     * - 먼저 데이터를 읽은 후에 update 를 수행 할 때 현재 내가 읽은 버젼에 맞는지 확인하여 업데이트 진행
     * - 내가 읽은 버젼에서 수정사항이 생겼을 경우에는 application에서 다시 읽은 후에 작업을 수행 한다.
     *
     * 요약: service 1, 2가 sql을 날릴 때 where문 뒤에 version =1 과 같은 형태로 같이 보내게 된다면 진행이 된다.
     * server 1 이 update set version = version + 1, quantity = 2, from stock where id = 1 and version = 1
     * 이라는 쿼리를 날리게 된다면 version은 2가 되므로 server 2에서 version 1의 요청을 보내면 접근이 불가능해진다.
     * 실제 데이터는 version 2 이 기 때문에 server2는 다시 데이터를 조회후에 요청을 보내는 작업을 추가해줘야한다.
     *
     *
     * Named Lock
     * - 이름을 가진 metadata locking 이다. 이름을 가진 Lock 을 획득한 후 해제할 때 까지 다른 세션은 이 Lock을 획득 불가
     * - 주의할 점으로는 transaction 이 종료될 때 lock이 자동으로 해제되지 않는다.
     * - 별도의 명령어로 해제를 수정해주거나 선정시간으로 끝나야 해제됨
     *
     * 요약: pessimistic lock 과 유사하지만 pessimistic lock은 row나 table을 단위로 걸지만
     * named lock은 metadata로 락을 건다.
     * Stock에는 lock을 걸지않고 Lock이라는 공간을 활용한다.
     */

    /**
     * Named Lock service
     * 부모의 트랜잭션과 별도로 실행이 되어야 함으로 propagation 변경
     * yml 에 connection pool 지정해둘것 (같은 데이터소스를 이용할 것 이기 때문)
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW) // named Lock 진행 시 이용
    public void decrease(Long id, Long quantity) {
        // Stock 조회
        // 재고 감소 후
        // 갱신 값 저장
        Stock stock = stockRepository.findById(id).orElseThrow();
        stock.decrease(quantity);
        stockRepository.saveAndFlush(stock);
    }


}
