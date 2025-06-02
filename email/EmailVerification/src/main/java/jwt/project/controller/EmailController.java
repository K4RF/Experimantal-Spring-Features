package jwt.project.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jwt.project.dto.request.PasswordResetEmailRequest;
import jwt.project.dto.request.PasswordResetRequest;
import jwt.project.entity.Member;
import jwt.project.repository.MemberRepository;
import jwt.project.service.EmailService;
import jwt.project.service.PasswordResetService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.time.LocalDateTime;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class EmailController {
    private final MemberRepository memberRepository;
    private final EmailService emailService;
    private final PasswordResetService passwordResetService;

    @GetMapping("/verify-email")
    public ResponseEntity<?> verifyEmail(@RequestParam String token) {
        Member member = memberRepository.findByEmailVerificationToken(token)
                .orElseThrow(() -> new IllegalArgumentException("잘못된 토큰입니다."));

        if (member.getEmailVerificationExpiry().isBefore(LocalDateTime.now())) {
            // ✅ 토큰 재발급 로직 추가
            String newToken = UUID.randomUUID().toString();
            member.setEmailVerificationToken(newToken);
            member.setEmailVerificationExpiry(LocalDateTime.now().plusHours(24));
            memberRepository.save(member);

            emailService.sendVerificationMail(member.getEmail(), newToken, "http://localhost:8080");
            return ResponseEntity.status(HttpStatus.GONE)
                    .body("인증 토큰이 만료되었습니다. 새로운 인증 메일을 발송했습니다.");
        }

        member.setEmailVerified(true);
        member.setEmailVerificationToken(null);
        member.setEmailVerificationExpiry(null);
        memberRepository.save(member);

        return ResponseEntity.ok().body("이메일 인증이 완료되었습니다. 로그인 후 서비스를 이용해주세요.");
    }

    @PostMapping("/resend-verification")
    public ResponseEntity<?> resendVerification(@RequestParam String email) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("가입되지 않은 이메일입니다."));

        if (member.isEmailVerified()) {
            return ResponseEntity.badRequest().body("이미 인증된 이메일입니다.");
        }

        String newToken = UUID.randomUUID().toString();
        member.setEmailVerificationToken(newToken);
        member.setEmailVerificationExpiry(LocalDateTime.now().plusHours(24));
        memberRepository.save(member);

        emailService.sendVerificationMail(email, newToken, "http://localhost:8080");
        return ResponseEntity.ok("인증 메일이 재발송되었습니다.");
    }

    // 1. 비밀번호 재설정 메일 요청
    @PostMapping("/request-reset-password")
    public ResponseEntity<?> requestResetPassword(@RequestBody @Valid PasswordResetEmailRequest request,
                                                  HttpServletRequest httpRequest) {
        String siteUrl = ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString();
        passwordResetService.sendResetPasswordMail(request.getEmail(), siteUrl);
        return ResponseEntity.ok("비밀번호 재설정 메일이 전송되었습니다.");
    }

    // 2. 실제 비밀번호 재설정
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestParam String token,
                                           @RequestBody @Valid PasswordResetRequest request) {
        passwordResetService.resetPassword(token, request.getNewPassword(), request.getConfirmPassword());
        return ResponseEntity.ok("비밀번호가 성공적으로 변경되었습니다.");
    }
}
