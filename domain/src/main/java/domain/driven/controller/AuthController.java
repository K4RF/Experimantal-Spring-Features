package domain.driven.controller;

import domain.driven.dto.request.LoginRequest;
import domain.driven.dto.request.RegisterRequest;
import domain.driven.dto.request.SocialRegisterRequest;
import domain.driven.dto.response.LoginResponse;
import domain.driven.entity.enums.Role;
import domain.driven.service.MemberCommandService;
import domain.driven.service.RefreshTokenService;
import domain.driven.utils.JwtUtils;
import io.jsonwebtoken.Claims;

import lombok.RequiredArgsConstructor;

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

    private final MemberCommandService cmd;
    private final RefreshTokenService refreshTokenService;   // 주입
    private final JwtUtils jwt;

    /* ---------- 회원가입 ---------- */
    @PostMapping("/register/user")
    public ResponseEntity<?> regUser(@RequestBody RegisterRequest dto) {
        cmd.registerUser(dto.getLoginId(), dto.getPassword(), dto.getName(), Role.USER);
        return ResponseEntity.ok(Map.of("msg","회원가입 성공(USER)"));
    }

    @PostMapping("/register/admin")
    public ResponseEntity<?> regAdmin(@RequestBody RegisterRequest dto) {
        cmd.registerUser(dto.getLoginId(), dto.getPassword(), dto.getName(), Role.ADMIN);
        return ResponseEntity.ok(Map.of("msg","회원가입 성공(ADMIN)"));
    }

    /* ---------- 로그인 ---------- */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest dto) {
        Map<String,String> t = cmd.login(dto.getLoginId(), dto.getPassword());
        return ResponseEntity.ok(
                new LoginResponse(t.get("accessToken"), t.get("refreshToken"), dto.getLoginId())
        );
    }

    /* ---------- 로그아웃 ---------- */
    @PostMapping("/logout")
    public ResponseEntity<?> logout(@AuthenticationPrincipal String loginId) {
        if (loginId == null) return ResponseEntity.status(401).build();
        cmd.logout(loginId);
        return ResponseEntity.ok(Map.of("message","로그아웃 완료 (Refresh 삭제)"));
    }

    /* ---------- 소셜 회원가입 ---------- */
    @PostMapping("/social-register")
    public ResponseEntity<?> regSocial(@RequestBody SocialRegisterRequest dto) {
        cmd.registerSocial(dto);
        return ResponseEntity.ok(Map.of("msg","소셜 회원가입 성공"));
    }

    /* ---------- 토큰 재발급 ---------- */
    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@RequestBody Map<String,String> body) {
        String oldRt = body.get("refresh_token");              // swagger: refresh_token
        Claims c = jwt.validateToken(oldRt);
        if (c == null)  return ResponseEntity.status(401).body("invalid RT");

        String id = c.getSubject();
        String saved = refreshTokenService.find(id);                    // cmd 내부에서 rtRepo.find 호출
        if (!oldRt.equals(saved))
            return ResponseEntity.status(401).body("token mismatch");

        String newAt = jwt.generateToken(id, c.get("role",String.class));
        String newRt = jwt.refreshToken(id);
        refreshTokenService.save(id, newRt);

        return ResponseEntity.ok(Map.of(
                "accessToken",  newAt,
                "refreshToken", newRt
        ));
    }
}