package domain.driven.handler.resolver;


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
public class CustomAuthorizationRequestResolver implements OAuth2AuthorizationRequestResolver {

    private final ClientRegistrationRepository clientRegistrationRepository;
    private final SecureRandom random = new SecureRandom();
    private final StringRedisTemplate redis;
    private static final long EXPIRE = 300;

    private final OAuth2AuthorizationRequestResolver defaultResolver;

    public CustomAuthorizationRequestResolver(ClientRegistrationRepository clientRegistrationRepository, StringRedisTemplate redis) {
        this.clientRegistrationRepository = clientRegistrationRepository;
        this.redis = redis;
        this.defaultResolver = new DefaultOAuth2AuthorizationRequestResolver(clientRegistrationRepository, "/oauth2/authorization");
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