package domain.driven.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/")
public class AdminController {
    // hasAnyRole('USER','ADMIN')	둘 중 하나라도 가지면 허용
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
