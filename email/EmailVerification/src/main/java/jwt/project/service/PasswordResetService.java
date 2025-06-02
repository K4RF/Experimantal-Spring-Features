package jwt.project.service;

import jwt.project.entity.Member;
import jwt.project.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PasswordResetService {
    private final MemberRepository memberRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

    // 1. 비밀번호 재설정 메일 요청
    public void sendResetPasswordMail(String email, String siteUrl) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("가입된 이메일이 없습니다."));

        String token = UUID.randomUUID().toString();
        member.setResetPasswordToken(token);
        member.setResetPasswordExpiry(LocalDateTime.now().plusHours(1)); // 1시간 유효
        memberRepository.save(member);

        String link = siteUrl + "/api/pass/reset-password?token=" + token;
        String subject = "[Labo] 비밀번호 재설정 안내";
        String text = "아래 링크를 클릭해 비밀번호를 재설정하세요.\n" + link;

        emailService.sendSimpleMail(email, subject, text);
    }

    // 2. 비밀번호 재설정
    public void resetPassword(String token, String newPassword, String confirmPassword) {
        Member member = memberRepository.findByResetPasswordToken(token)
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 토큰입니다."));

        if (member.getResetPasswordExpiry() == null || member.getResetPasswordExpiry().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("토큰이 만료되었습니다. 다시 요청해주세요.");
        }
        if (!newPassword.equals(confirmPassword)) {
            throw new IllegalArgumentException("비밀번호와 비밀번호 확인이 일치하지 않습니다.");
        }
        // 비밀번호 정책(길이 등) 추가 가능

        member.setPassword(passwordEncoder.encode(newPassword));
        member.setResetPasswordToken(null); // 토큰 무효화
        member.setResetPasswordExpiry(null);
        memberRepository.save(member);
    }
}
