package jwt.project.entity;

import jakarta.persistence.*;
import jwt.project.entity.enums.Role;
import jwt.project.entity.enums.SocialType;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Member {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String email; // loginId -> email로 변경

    private String password;

    private String name;

    @Enumerated(EnumType.STRING)
    private Role role;

    @Enumerated(EnumType.STRING)
    private SocialType socialType;

    private String socialId;      // 구글에서 제공하는 sub 값

    // 이메일 인증 관련
    @Column(name = "email_verified")  // ✅ snake_case 매핑
    private boolean emailVerified = false; // 이메일 인증 여부
    private String emailVerificationToken; // 인증용 토큰
    private LocalDateTime emailVerificationExpiry; // 만료 시간

    // 비밀번호 재설정 관련
    @Column(name = "reset_password_token")
    private String resetPasswordToken;

    @Column(name = "reset_password_expiry")
    private LocalDateTime resetPasswordExpiry;
}
