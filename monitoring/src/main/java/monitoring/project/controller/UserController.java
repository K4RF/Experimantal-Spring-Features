package monitoring.project.controller;

import monitoring.project.entity.Member;
import monitoring.project.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user")
public class UserController {

    private final MemberService memberService;

    @GetMapping("/me")
    public ResponseEntity<?> getMe(@AuthenticationPrincipal String email) {
        Member member = memberService.findByEmail(email);

        Map<String, Object> result = new HashMap<>();
        result.put("email", member.getEmail());
        result.put("name", member.getName());
        result.put("role", member.getRole());
        result.put("socialType", member.getSocialType()); // null 허용

        return ResponseEntity.ok(result);
    }

    @PostMapping("/disconnect")
    public ResponseEntity<?> disconnectSocial(@AuthenticationPrincipal String loginId) {
        memberService.disconnectSocialAccount(loginId);
        return ResponseEntity.ok(Map.of("message", "소셜 연동이 해제되었습니다"));
    }


}
