package batch.project.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import batch.project.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {
    private final JavaMailSender mailSender;
    private final MemberRepository memberRepository;

    @Value("${spring.mail.username}")
    private String from;

    public void sendVerificationMail(String to, String token, String siteUrl) {
        String subject = "🔐 이메일 인증을 완료해주세요!";
        String verificationUrl = siteUrl + "/api/auth/verify-email?token=" + token;

        // HTML 형식의 이메일 본문
        String text = "<div style='font-family: Arial; padding: 20px;'>" +
                "<h2>회원가입을 축하드립니다🎉</h2>" +
                "<p>아래 버튼을 클릭해 이메일 인증을 완료해주세요.</p>" +
                "<a href='" + verificationUrl + "' style='" +
                "display: inline-block; padding: 10px 20px; " +
                "background-color: #4CAF50; color: white; " +
                "text-decoration: none; border-radius: 5px;'>" +
                "인증하기</a>" +
                "</div>";

        MimeMessage message = mailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(text, true); // HTML 활성화
            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("이메일 발송 실패", e);
        }
    }

    public void sendHtmlMail(String to, String subject, String html) {
        MimeMessage message = mailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(to);
            helper.setFrom(from);
            helper.setSubject(subject);
            helper.setText(html, true); // HTML 활성화
            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("이메일 발송 실패", e);
        }
    }
}
