package jwt.project.controller;

import jwt.project.entity.Member;
import jwt.project.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user")
public class UserController {
    private final MemberService memberService;

    @GetMapping("/me")
    public ResponseEntity<?> getMe(@AuthenticationPrincipal(expression = "username") String loginId) {
        Member member = memberService.findByLoginId(loginId);

        return ResponseEntity.ok(Map.of(
                "loginId", member.getLoginId(),
                "name", member.getName(),
                "role", member.getRole(),
                "socialType", member.getSocialType()
        ));
    }

    @PostMapping("/disconnect")
    public ResponseEntity<?> disconnectSocial(@AuthenticationPrincipal(expression = "username") String loginId) {
        memberService.disconnectSocialAccount(loginId);
        return ResponseEntity.ok(Map.of("message", "소셜 연동이 해제되었습니다"));
    }
}
