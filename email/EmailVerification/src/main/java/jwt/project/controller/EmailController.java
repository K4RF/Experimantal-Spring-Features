package jwt.project.controller;

import jwt.project.entity.Member;
import jwt.project.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class EmailController {
    private final MemberRepository memberRepository;

    @GetMapping("/verify-email")
    public ResponseEntity<?> verifyEmail(@RequestParam String token) {
        Member member = memberRepository.findByEmailVerificationToken(token)
                .orElseThrow(() -> new IllegalArgumentException("잘못된 토큰입니다."));
        if (member.getEmailVerificationExpiry().isBefore(LocalDateTime.now())) {
            return ResponseEntity.status(HttpStatus.GONE).body("인증 토큰이 만료되었습니다.");
        }
        member.setEmailVerified(true);
        member.setEmailVerificationToken(null);
        member.setEmailVerificationExpiry(null);
        memberRepository.save(member);
        return ResponseEntity.ok("이메일 인증이 완료되었습니다. 이제 로그인할 수 있습니다.");
    }
}
