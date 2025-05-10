package domain.driven.entity;

import domain.driven.entity.enums.Role;
import domain.driven.entity.enums.SocialType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.crypto.password.PasswordEncoder;

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

    @Embedded
    private SocialInfo social;        // null 이면 미연동

    /* 핵심 도메인 규칙 */
    public void encodePw(PasswordEncoder pe){ this.password = pe.encode(this.password); }
    public void disconnectSocial(){ this.social = null; }
}