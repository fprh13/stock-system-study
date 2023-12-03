package com.example.order.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "stock")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Stock {

    /**
     * 재고 관리 무엇으로 할까 ?
     *
     * mysql vs redis
     *
     * mysql :
     * 이미 사용중이라면 별도의 비용없이 사용가능하다.
     * 어느정도의 트래픽까지는 문제없이 활용이 가능하다.
     * redis보다는 성능이 좋지 않다.
     *
     * redis :
     * 활용중인 redis가 없다면 별도의 구축비용과 인프라 관리비용이 발생한다.
     * mysql보다 성능이 좋다.
     *
     * redis는 휘발가능 성이 있기 때문에
     * 재고와 같은 중요한 데이터는 rdb를 활용하는 것이 일반적이다.
     * rdb를 사용할때 여러 스레드가 접근할 수 있으므로 한 스레드만 접근할 수 있도록 제어할 lock이 필요하게 된다.
     */



    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long productId;

    private Long quantity;

    public Stock(Long productId, Long quantity) {
        this.productId = productId;
        this.quantity = quantity;
    }

    // jakarta.persistence 로 진행 필수
    @Version
    private Long version;

    //==비즈니스 메서드==//
    public void decrease(Long quantity) {
        if(this.quantity - quantity < 0) {
            throw  new RuntimeException("재고는 0개 미만이 될 수 없습니다.");
        }
        this.quantity -= quantity;
    }

}
