package jwt.project.controller;

import jwt.project.MemberService;
import jwt.project.dto.request.LoginRequest;
import jwt.project.dto.request.RegisterRequest;
import jwt.project.dto.response.LoginResponse;
import jwt.project.dto.response.RegisterResponse;
import jwt.project.entity.Member;
import jwt.project.utils.JwtUtil;
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
    private final JwtUtil jwtUtil;

    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(@RequestBody RegisterRequest requestDto) {
        Member member = new Member();
        member.setUsername(requestDto.getUsername());
        member.setPassword(requestDto.getPassword());
        member.setName(requestDto.getName());

        memberService.register(member);

        RegisterResponse response = new RegisterResponse("회원가입 성공", member.getUsername());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest requestDto) {
        Member member = memberService.login(requestDto.getUsername(), requestDto.getPassword());
        String token = jwtUtil.generateToken(member.getUsername());

        LoginResponse response = new LoginResponse(token, member.getUsername());
        return ResponseEntity.ok(response);
    }
}