package domain.driven.controller;



import domain.driven.entity.Member;
import domain.driven.service.MemberCommandService;
import domain.driven.service.MemberQueryService;
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

    private final MemberQueryService qry;
    private final MemberCommandService cmd;

    /* ---------- 내 정보 ---------- */
    @GetMapping("/me")
    public ResponseEntity<?> me(@AuthenticationPrincipal String loginId) {
        Member m = qry.me(loginId);
        Map<String, Object> result = new HashMap<>();
        result.put("loginId", m.getLoginId());
        result.put("name", m.getName());
        result.put("role", m.getRole());
        if (m.getSocial() != null) {
            result.put("socialType", m.getSocial().getSocialType());
        }
        return ResponseEntity.ok(result);
    }

    /* ---------- 소셜 연결 해제 ---------- */
    @PostMapping("/disconnect")
    public ResponseEntity<?> disconnect(@AuthenticationPrincipal String loginId) {
        cmd.disconnectSocial(loginId);
        return ResponseEntity.ok(Map.of("msg","소셜 연동 해제 완료"));
    }
}