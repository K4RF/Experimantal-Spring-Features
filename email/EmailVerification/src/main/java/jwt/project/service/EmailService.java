package jwt.project.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {
    private final JavaMailSender mailSender;
    @Value("${spring.mail.username}")
    private String from;

    public void sendVerificationMail(String to, String token, String siteUrl) {
        String subject = "이메일 인증을 완료해주세요";
        String link = siteUrl + "/api/auth/verify-email?token=" + token;
        String text = "아래 링크를 클릭해 이메일 인증을 완료하세요:\n" + link;

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setFrom(from);
        message.setSubject(subject);
        message.setText(text);
        mailSender.send(message);
    }
}
