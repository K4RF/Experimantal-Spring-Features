package redisCache.project.handler.resolver;

import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.stereotype.Component;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class CustomAuthorizationRequestResolver
        implements OAuth2AuthorizationRequestResolver {

    private final ClientRegistrationRepository repo;
    private final SecureRandom random = new SecureRandom();
    private final StringRedisTemplate redis;          // ┐ state 저장소  (쿠키를 쓰고 싶으면 생략)
    private static final long EXPIRE = 300;           // ┘ 5분 TTL

    private OAuth2AuthorizationRequestResolver defaultResolver;

    @PostConstruct
    public void init() {
        this.defaultResolver = new DefaultOAuth2AuthorizationRequestResolver(repo, "/oauth2/authorization");
    }

    @Override
    public OAuth2AuthorizationRequest resolve(HttpServletRequest req) {
        OAuth2AuthorizationRequest original = defaultResolver.resolve(req);
        return customize(original, req);
    }

    @Override
    public OAuth2AuthorizationRequest resolve(HttpServletRequest req, String clientRegId) {
        OAuth2AuthorizationRequest original = defaultResolver.resolve(req, clientRegId);
        return customize(original, req);
    }

    private OAuth2AuthorizationRequest customize(OAuth2AuthorizationRequest original,
                                                 HttpServletRequest req) {
        if (original == null) return null;

        // 1) state 랜덤 생성
        String state = new BigInteger(130, random).toString(32);

        // 2) Redis에 [STATE -> IP+UA] 저장 (5분)
        redis.opsForValue().set("OAUTH_STATE:" + state,
                req.getRemoteAddr(), EXPIRE, TimeUnit.SECONDS);

        // 3) state 값 교체
        return OAuth2AuthorizationRequest.from(original)
                .state(state)
                .build();
    }
}