package domain.driven.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.concurrent.TimeUnit;

// member/infra/RefreshTokenRedisRepo
@Repository
@RequiredArgsConstructor
public class RefreshTokenRedisRepo {
    private final StringRedisTemplate redis;
    private static final long TTL = 7*24*60*60;
    private String key(String id){ return "RT:"+id; }

    public void save(String id,String token){
        redis.opsForValue().set(key(id), token, TTL, TimeUnit.SECONDS);
    }
    public String find(String id){ return redis.opsForValue().get(key(id)); }
    public void delete(String id){ redis.delete(key(id)); }
}