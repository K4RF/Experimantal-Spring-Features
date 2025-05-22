package jwt.project.entity;

import jakarta.persistence.*;
import jwt.project.entity.enums.Role;
import jwt.project.entity.enums.SocialType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Member {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String loginId;

    private String password;

    private String name;

    @Enumerated(EnumType.STRING)
    private Role role;

    @Enumerated(EnumType.STRING)
    private SocialType socialType;

    private String socialId;      // 구글에서 제공하는 sub 값

    // 이메일 인증 관련
    private boolean emailVerified = false; // 이메일 인증 여부
    private String emailVerificationToken; // 인증용 토큰
    private LocalDateTime emailVerificationExpiry; // 만료 시간
}
