package jwt.project.controller;

import io.jsonwebtoken.Claims;
import jwt.project.dto.request.LoginRequest;
import jwt.project.dto.request.RegisterRequest;
import jwt.project.dto.request.SocialRegisterRequest;
import jwt.project.dto.response.LoginResponse;
import jwt.project.dto.response.RegisterResponse;
import jwt.project.entity.RefreshToken;
import jwt.project.repository.RefreshTokenRepository;
import jwt.project.service.MemberService;
import jwt.project.service.RefreshTokenService;
import jwt.project.utils.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {
    private final MemberService memberService;
    private final JwtUtil jwtUtil;
    private final RefreshTokenRepository refreshTokenRepository;
    private final RefreshTokenService refreshTokenService;

    @PostMapping("/register/user")
    public ResponseEntity<RegisterResponse> registerUser(@RequestBody RegisterRequest requestDto) {
        memberService.registerUser(requestDto.getLoginId(), requestDto.getPassword(), requestDto.getName());
        return ResponseEntity.ok(new RegisterResponse("회원가입 성공(USER)", requestDto.getLoginId()));
    }

    @PostMapping("/register/admin")
    public ResponseEntity<RegisterResponse> registerAdmin(@RequestBody RegisterRequest requestDto) {
        memberService.registerAdmin(requestDto.getLoginId(), requestDto.getPassword(), requestDto.getName());
        return ResponseEntity.ok(new RegisterResponse("회원가입 성공(ADMIN)", requestDto.getLoginId()));
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest requestDto) {
        Map<String, String> tokens = memberService.loginAndGetToken(requestDto.getLoginId(), requestDto.getPassword());
        String accessToken = tokens.get("accessToken");
        String refreshToken = tokens.get("refreshToken");

        return ResponseEntity.ok(new LoginResponse(accessToken, refreshToken, requestDto.getLoginId()));
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
                "loginId", request.getLoginId()
        ));
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
