package redisCache.project.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final StringRedisTemplate redisTemplate;
    private static final long EXPIRE_SECONDS = 7 * 24 * 60 * 60;   // 7일

    public void save(String loginId, String refreshToken) {
        redisTemplate.opsForValue()
                .set(key(loginId), refreshToken, EXPIRE_SECONDS, TimeUnit.SECONDS);
    }

    public String find(String email) {
        return redisTemplate.opsForValue().get(key(email));
    }

    public void delete(String email) {
        redisTemplate.delete(key(email));
    }

    private String key(String email) {      // 키 네이밍 규칙
        return "RT:" + email;
    }
}