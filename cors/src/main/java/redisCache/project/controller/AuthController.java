package redisCache.project.controller;

import io.jsonwebtoken.Claims;
import redisCache.project.dto.SocialUserInfo;
import redisCache.project.dto.request.LoginRequest;
import redisCache.project.dto.request.RegisterRequest;
import redisCache.project.dto.request.SocialLoginRequest;
import redisCache.project.dto.request.SocialRegisterRequest;
import redisCache.project.dto.response.LoginResponse;
import redisCache.project.dto.response.RegisterResponse;
import redisCache.project.repository.RefreshTokenRepository;
import redisCache.project.service.MemberService;
import redisCache.project.service.RefreshTokenService;
import redisCache.project.service.SocialLoginService;
import redisCache.project.utils.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {
    private final MemberService memberService;
    private final JwtUtil jwtUtil;
    private final RefreshTokenRepository refreshTokenRepository;
    private final RefreshTokenService refreshTokenService;
    private final SocialLoginService socialLoginService;

    @PostMapping("/register/user")
    public ResponseEntity<RegisterResponse> registerUser(@RequestBody RegisterRequest requestDto) {
        memberService.registerUser(requestDto.getEmail(), requestDto.getPassword(), requestDto.getName());
        return ResponseEntity.ok(new RegisterResponse("회원가입 성공(USER)", requestDto.getEmail()));
    }

    @PostMapping("/register/admin")
    public ResponseEntity<RegisterResponse> registerAdmin(@RequestBody RegisterRequest requestDto) {
        memberService.registerAdmin(requestDto.getEmail(), requestDto.getPassword(), requestDto.getName());
        return ResponseEntity.ok(new RegisterResponse("회원가입 성공(ADMIN)", requestDto.getEmail()));
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest requestDto) {
        Map<String, String> tokens = memberService.loginAndGetToken(requestDto.getEmail(), requestDto.getPassword());
        String accessToken = tokens.get("accessToken");
        String refreshToken = tokens.get("refreshToken");

        return ResponseEntity.ok(new LoginResponse(accessToken, refreshToken, requestDto.getEmail()));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@AuthenticationPrincipal String loginId) {
        refreshTokenService.delete(loginId);   // ✅ Redis 키 삭제
        //refreshTokenRepository.findByLoginId(loginId).ifPresent(refreshTokenRepository::delete);

        return ResponseEntity.ok(Map.of("message", "로그아웃 처리 완료. Refresh Token 삭제됨"));
    }

    @PostMapping("/social-register")
    public ResponseEntity<?> registerSocial(@RequestBody SocialRegisterRequest request) {
        memberService.registerSocialUser(request);
        return ResponseEntity.ok(Map.of(
                "message", "소셜 회원가입 성공",
                "loginId", request.getEmail()
        ));
    }
    @PostMapping("/social-login")
    public ResponseEntity<LoginResponse> socialLogin(@RequestBody SocialLoginRequest request) {
        SocialUserInfo userInfo = null;
        switch (request.getSocialType()) {
            case GOOGLE -> userInfo = socialLoginService.getGoogleUserInfo(request.getAccessToken());
            case KAKAO  -> userInfo = socialLoginService.getKakaoUserInfo(request.getAccessToken());
            case NAVER  -> userInfo = socialLoginService.getNaverUserInfo(request.getAccessToken());
        }

        // userInfo에서 sub/socialId, email, name 등 추출
        // 이미 가입된 회원인지 확인, 없으면 회원가입, 있으면 로그인 처리
        LoginResponse response = memberService.socialLoginOrRegister(userInfo, request.getSocialType());
        return ResponseEntity.ok(response);
    }


    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@RequestBody Map<String, String> request) {
        String refreshToken = request.get("refresh_token");         // 스웨거에서 사용시 "refresh_token": "토큰값"

        // Refresh Token 유효성 검증
        Claims claims = jwtUtil.validateToken(refreshToken);
        if (claims == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid refresh token");
        }

        String loginId = claims.getSubject();

        String saved = refreshTokenService.find(loginId);
        if (saved == null || !saved.equals(refreshToken)) {
            return ResponseEntity.status(401).body(Map.of("msg","Token mismatch"));
        }
        /* DB에서 Refresh Token 확인
        Optional<RefreshToken> refreshTokenOpt = refreshTokenRepository.findByLoginId(loginId);
        if(refreshTokenOpt.isEmpty() || !refreshTokenOpt.get().getId().equals(refreshToken)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Refresh token mismatch");
        }
         */

        // ✅ 새로운 Access Token + Refresh Token 발급
        String newAccessToken = jwtUtil.generateToken(loginId, "USER");
        String newRefreshToken = jwtUtil.refreshToken(loginId);

        // Redis 갱신
        refreshTokenService.save(loginId, newRefreshToken);

        /* ✅ DB에 Refresh Token 갱신
        RefreshToken storedToken = refreshTokenOpt.get();
        storedToken.setRefreshToken(newRefreshToken);
        refreshTokenRepository.save(storedToken);
         */
        return ResponseEntity.ok(Map.of(
                "accessToken", newAccessToken,
                "refreshToken", newRefreshToken
        ));
    }
}
