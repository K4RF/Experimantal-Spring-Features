package jwt.project.controller;

import jwt.project.dto.request.SocialRegisterRequest;
import jwt.project.service.MemberService;
import jwt.project.dto.request.LoginRequest;
import jwt.project.dto.request.RegisterRequest;
import jwt.project.dto.response.LoginResponse;
import jwt.project.dto.response.RegisterResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
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
        String token = memberService.loginAndGetToken(requestDto.getLoginId(), requestDto.getPassword());
        return ResponseEntity.ok(new LoginResponse(token, requestDto.getLoginId()));
    }

    @PostMapping("/social-register")
    public ResponseEntity<?> registerSocial(@RequestBody SocialRegisterRequest request) {
        memberService.registerSocialUser(request);
        return ResponseEntity.ok(Map.of(
                "message", "소셜 회원가입 성공",
                "loginId", request.getLoginId()
        ));
    }
}