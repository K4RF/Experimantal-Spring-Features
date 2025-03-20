package jwt.project.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jwt.project.entity.Member;
import jwt.project.entity.enums.SocialType;
import jwt.project.repository.MemberRepository;
import jwt.project.utils.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final MemberRepository memberRepository;
    private final JwtUtil jwtUtil;
    private final ObjectMapper objectMapper;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        String email = oAuth2User.getAttribute("email");
        String googleSub = oAuth2User.getAttribute("sub");
        String name = oAuth2User.getAttribute("name");

        Optional<Member> memberOpt = memberRepository.findBySocialIdAndSocialType(googleSub, SocialType.GOOGLE);

        response.setContentType("application/json;charset=UTF-8");

        if (memberOpt.isPresent()) {
            // ✅ 로그인 성공 → JWT 발급
            Member member = memberOpt.get();
            String token = jwtUtil.generateToken(member.getLoginId(), member.getRole().name());

            Map<String, Object> result = Map.of(
                    "message", "로그인 성공",
                    "token", token,
                    "loginId", member.getLoginId()
            );

            response.getWriter().write(objectMapper.writeValueAsString(result));

        } else {
            // ✅ 아직 회원가입 안 된 경우 → 회원가입 유도 정보 반환
            Map<String, Object> result = Map.of(
                    "message", "추가 정보가 필요합니다. /api/auth/social-register를 호출하세요.",
                    "socialId", googleSub,
                    "socialType", SocialType.GOOGLE,
                    "email", email,
                    "name", name
            );

            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write(objectMapper.writeValueAsString(result));
        }
    }
}
