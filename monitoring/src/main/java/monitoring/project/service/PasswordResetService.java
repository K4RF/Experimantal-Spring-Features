package monitoring.project.service;

import monitoring.project.entity.Member;
import monitoring.project.repository.MemberRepository;
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

    // 1. 비밀번호 재설정 메일 요청 (HTML 메일)
    public void sendResetPasswordMail(String email, String siteUrl) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("가입된 이메일이 없습니다."));

        String token = UUID.randomUUID().toString();
        member.setResetPasswordToken(token);
        member.setResetPasswordExpiry(LocalDateTime.now().plusMinutes(15)); // 15분 유효
        memberRepository.save(member);

        String link = siteUrl + "/api/pass/reset-password?token=" + token;
        String subject = "[Labo] 비밀번호 재설정 안내";

        // HTML 형식의 메일 본문
        String html = "<div style='font-family: Arial, sans-serif; padding: 24px; background: #f9f9f9; color: #333;'>"
                + "<h2 style='color: #4CAF50;'>비밀번호 재설정 안내</h2>"
                + "<p>안녕하세요.<br>"
                + "비밀번호 재설정 요청이 접수되었습니다.<br>"
                + "아래 버튼을 클릭하여 비밀번호를 재설정해 주세요.<br>"
                + "<span style='color: #888; font-size:12px;'>(링크는 15분간만 유효합니다)</span></p>"
                + "<a href='" + link + "' style='"
                + "display: inline-block; margin: 24px 0; padding: 12px 28px; "
                + "background-color: #4CAF50; color: white; text-decoration: none; border-radius: 5px; font-size: 16px;'>"
                + "비밀번호 재설정</a>"
                + "<p style='font-size:13px; color:#888;'>"
                + "만약 본인이 요청하지 않았다면 이 메일을 무시하셔도 됩니다.<br>"
                + "감사합니다."
                + "</p></div>";

        emailService.sendHtmlMail(email, subject, html);
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
