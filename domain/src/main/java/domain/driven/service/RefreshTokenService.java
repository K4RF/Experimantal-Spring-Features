package domain.driven.service;

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

    public String find(String loginId) {
        return redisTemplate.opsForValue().get(key(loginId));
    }

    public void delete(String loginId) {
        redisTemplate.delete(key(loginId));
    }

    private String key(String loginId) {      // 키 네이밍 규칙
        return "RT:" + loginId;
    }
}