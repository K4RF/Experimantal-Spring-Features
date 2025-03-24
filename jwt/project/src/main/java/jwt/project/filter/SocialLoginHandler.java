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
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class SocialLoginHandler implements AuthenticationSuccessHandler {

    private final MemberRepository memberRepository;
    private final JwtUtil jwtUtil;
    private final ObjectMapper objectMapper;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        // 소셜 타입 파악 (GOOGLE, NAVER 등)
        String registrationId = ((OAuth2AuthenticationToken) authentication).getAuthorizedClientRegistrationId().toUpperCase();
        SocialType socialType = SocialType.valueOf(registrationId); // "GOOGLE", "NAVER" → enum으로 변환

        // 소셜 플랫폼별 정보 파싱
        String socialId;
        String email;
        String name;

        if (socialType == SocialType.NAVER) {
            Map<String, Object> responseData = (Map<String, Object>) oAuth2User.getAttributes().get("response");
            socialId = (String) responseData.get("id");
            email = (String) responseData.get("email");
            name = (String) responseData.get("name");
        } else { // 기본은 구글
            socialId = oAuth2User.getAttribute("sub");
            email = oAuth2User.getAttribute("email");
            name = oAuth2User.getAttribute("name");
        }

        response.setContentType("application/json;charset=UTF-8");

        Optional<Member> memberOpt = memberRepository.findBySocialIdAndSocialType(socialId, socialType);

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
                    "socialType", socialType,
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
