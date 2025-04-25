package jwt.project.controller;

import jwt.project.dto.response.UserInfoResponseDto;
import jwt.project.entity.Member;
import jwt.project.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.prepost.PreAuthorize;
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
    public ResponseEntity<?> getMe(@AuthenticationPrincipal String loginId) {
        Member member = memberService.findByLoginId(loginId);

        Map<String, Object> result = new HashMap<>();
        result.put("loginId", member.getLoginId());
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

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/secret")
    public ResponseEntity<?> getAdminOnlyData() {
        return ResponseEntity.ok("관리자 전용 데이터입니다.");
    }

    @Secured("ROLE_ADMIN")
    @GetMapping("/admin/users")
    public ResponseEntity<?> getAllUsers() {
        return ResponseEntity.ok("어드민만 볼 수 있는 회원 목록");
    }
}
