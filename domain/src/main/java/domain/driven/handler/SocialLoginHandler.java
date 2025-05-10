package domain.driven.handler;


import com.fasterxml.jackson.databind.ObjectMapper;
import domain.driven.entity.Member;
import domain.driven.entity.enums.SocialType;
import domain.driven.repository.MemberRepository;
import domain.driven.utils.JwtUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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
    private final JwtUtils jwtUtil;
    private final ObjectMapper objectMapper;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication)throws IOException{
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        // 소셜 타입 파악 (Google, Naver, Kakao 등)
        String registrationId = ((OAuth2AuthenticationToken) authentication).getAuthorizedClientRegistrationId().toUpperCase();
        SocialType socialType = SocialType.valueOf(registrationId);

        // 소셜 플랫폼별 정보 파싱
        String socialId;
        String email;
        String name;

        if (socialType == SocialType.NAVER) {
            Map<String, Object> responseData = (Map<String, Object>) oAuth2User.getAttributes().get("response");
            socialId = String.valueOf(responseData.get("id")); // ✅ 안전하게 Long → String 변환
            email = (String) responseData.get("email");
            name = (String) responseData.get("name");

        }else if (socialType == SocialType.KAKAO) {
            Map<String, Object> kakaoAccount = (Map<String, Object>) oAuth2User.getAttributes().get("kakao_account");
            Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");

            socialId = String.valueOf(oAuth2User.getAttributes().get("id")); // ✅ 정확하게 이렇게 되어 있어야 해
            email = (String) kakaoAccount.get("email");
            name = (String) profile.get("nickname");
        } else { // 기본은 GOOGLE
            socialId = oAuth2User.getAttribute("sub");
            email = oAuth2User.getAttribute("email");
            name = oAuth2User.getAttribute("name");
        }

        response.setContentType("application/json; charset=utf-8");

        Optional<Member> memberOpt = memberRepository.findBySocial_SocialIdAndSocial_SocialType(socialId, socialType);

        if(memberOpt.isPresent()){
            // 로그인 처리
            Member member = memberOpt.get();
            // AccessToken 과 Refresh Token 생성
            String accessToken = jwtUtil.generateToken(member.getLoginId(), member.getRole().name());
            String refreshToken = jwtUtil.refreshToken(member.getLoginId());

            Map<String, Object> result = Map.of(
                    "message", "소셜 로그인 성공",
                    "accessToken", accessToken,
                    "refreshToken", refreshToken,
                    "loginId", member.getLoginId()
            );

            response.getWriter().write(objectMapper.writeValueAsString(result));
        }else{
            // 추가 정보 입력을 위한 안내
            Map<String, Object> result = Map.of(
                    "message", "회원 정보가 없습니다, 추가 정보를 입력해 주세요.",
                    "socialId", socialId,
                    "socialType", socialType,
                    "email", email,
                    "name", name
            );
            response.getWriter().write(objectMapper.writeValueAsString(result));
        }
    }
}
