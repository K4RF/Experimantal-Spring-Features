package batch.project.handler;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class StateValidatingSuccessHandler implements AuthenticationSuccessHandler {

    private final StringRedisTemplate redis;
    private final SocialLoginHandler socialLoginHandler;   // 원래 성공 처리기

    @Override
    public void onAuthenticationSuccess(HttpServletRequest req, HttpServletResponse res,
                                        Authentication auth) throws IOException, ServletException {

        String state = req.getParameter("state");
        String key   = "OAUTH_STATE:" + state;
        String saved = redis.opsForValue().get(key);

        // ① state 값이 없거나, 세션/IP 불일치 → 401
        if (state == null || saved == null ||
                !saved.equals(req.getRemoteAddr())) {
            res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            res.setContentType("application/json;charset=UTF-8");
            res.getWriter().write("{\"message\":\"Invalid or expired state\"}");
            return;
        }

        // ② 사용된 state 즉시 삭제 (1회용)
        redis.delete(key);

        // ③ 원래 소셜 로그인 성공 처리 계속 진행
        socialLoginHandler.onAuthenticationSuccess(req, res, auth);
    }
}