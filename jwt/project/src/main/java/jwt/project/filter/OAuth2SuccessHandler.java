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

        String socialId = oAuth2User.getAttribute("sub"); // 구글 고유 ID
        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");

        response.setContentType("application/json;charset=UTF-8");

        Optional<Member> memberOpt = memberRepository.findBySocialIdAndSocialType(socialId, SocialType.GOOGLE);

        if (memberOpt.isPresent()) {
            // ✅ 로그인 처리
            Member member = memberOpt.get();
            String token = jwtUtil.generateToken(member.getLoginId(), member.getRole().name());

            Map<String, Object> result = Map.of(
                    "message", "소셜 로그인 성공",
                    "token", token,
                    "loginId", member.getLoginId()
            );
            response.getWriter().write(objectMapper.writeValueAsString(result));
        } else {
            // ❗ 추가정보 입력을 위한 안내
            Map<String, Object> result = Map.of(
                    "message", "회원 정보가 없습니다. 추가 정보를 입력해주세요.",
                    "socialId", socialId,
                    "socialType", SocialType.GOOGLE,
                    "email", email,
                    "name", name
            );
            response.getWriter().write(objectMapper.writeValueAsString(result));

            /*
             // 리디렉션으로 추가 정보 입력 페이지로 이동
            String redirectUrl = UriComponentsBuilder.fromUriString("http://localhost:3000/oauth-register")
            .queryParam("socialId", socialId)
            .queryParam("socialType", SocialType.GOOGLE)
            .queryParam("email", email)
            .queryParam("name", name)
            .build().toUriString();

             response.sendRedirect(redirectUrl);
             */
        }
    }
}
