package com.example.order.repository;

import com.example.order.domain.Stock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;


// 편의상 Stock을 이용하지만 실무에서는 별도의 JDBC와 같은 다른 방안이 필요함.
public interface LockRepository extends JpaRepository<Stock,Long> {

    @Query(value = "select get_lock(:key, 3000)", nativeQuery = true)
    void getLock(String key);

    @Query(value = "select release_lock(:key)", nativeQuery = true)
    void releaseLock(String key);

}
