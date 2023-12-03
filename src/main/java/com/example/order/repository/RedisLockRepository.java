package com.example.order.repository;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class RedisLockRepository {

    /**
     * redis의 명령어를 실행하기 위함
     */
    private RedisTemplate<String, String> redisTemplate;

    public RedisLockRepository(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * 종류 :
     * Lettuce
     * - setnx 명령어를 활용하여 분산락 구현
     * - spin lock 방식
     *
     * Redisson
     * - pub sub 기반으로 Lock 구현 제
     *
     * named Lock 과 비슷한데 세션관리와 같은 번거로움이 적어짐
     */


    /**
     * 방식:
     * 로직 실행 전에 setnx 로 락을 걸고
     * 로직 종료시 unlock으로 락을 해제
     */

    /**
     * 방식:
     * 로직 실행 전 후 로 락획득 해제를 수행하기 때문에 facade 클래스가 필요함
     */

    //==Setnx 명령어==//
    public Boolean lock(Long key) {
        return redisTemplate
                .opsForValue()
                .setIfAbsent(generateKey(key), "Lock", Duration.ofMillis(3_000));
    }

    //==unlock 메서드==//
    public Boolean unlock(Long key) {
        return redisTemplate.delete(generateKey(key));
    }

    //==키.toString 메서드==//
    private String generateKey(Long key) {
        return key.toString();
    }
}
