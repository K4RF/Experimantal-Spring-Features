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
@RequestMapping("/api/pass")
public class PasswordController {
    private final MemberRepository memberRepository;
    private final EmailService emailService;
    private final PasswordResetService passwordResetService;

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
