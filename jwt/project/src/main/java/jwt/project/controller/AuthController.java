package jwt.project.controller;

import jwt.project.entity.enums.Role;
import jwt.project.service.MemberService;
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

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {
    private final MemberService memberService;
    private final JwtUtil jwtUtil;

    @PostMapping("/register/user")
    public ResponseEntity<RegisterResponse> registerUser(@RequestBody RegisterRequest requestDto) {
        Member member = new Member();
        member.setLoginId(requestDto.getLoginId());
        member.setPassword(requestDto.getPassword());
        member.setName(requestDto.getName());
        member.setRole(Role.USER);  // ✅ USER 역할

        memberService.register(member);

        return ResponseEntity.ok(new RegisterResponse("회원가입 성공(USER)", member.getLoginId()));
    }

    @PostMapping("/register/admin")
    public ResponseEntity<RegisterResponse> registerAdmin(@RequestBody RegisterRequest requestDto) {
        Member member = new Member();
        member.setLoginId(requestDto.getLoginId());
        member.setPassword(requestDto.getPassword());
        member.setName(requestDto.getName());
        member.setRole(Role.ADMIN);  // ✅ ADMIN 역할

        memberService.register(member);

        return ResponseEntity.ok(new RegisterResponse("회원가입 성공(ADMIN)", member.getLoginId()));
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest requestDto) {
        Member member = memberService.login(requestDto.getLoginId(), requestDto.getPassword());
        String token = jwtUtil.generateToken(member.getLoginId(), member.getRole().name());

        LoginResponse response = new LoginResponse(token, member.getLoginId());
        return ResponseEntity.ok(response);
    }
}