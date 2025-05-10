package domain.driven.service;

import domain.driven.dto.request.SocialRegisterRequest;
import domain.driven.entity.Member;
import domain.driven.entity.SocialInfo;
import domain.driven.entity.enums.Role;
import domain.driven.entity.enums.SocialType;
import domain.driven.repository.MemberRepositoryPort;
import domain.driven.repository.RefreshTokenRedisRepo;
import domain.driven.service.MemberCommandService;
import domain.driven.utils.JwtUtils;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@Slf4j
class MemberCommandServiceTest {

    @Mock MemberRepositoryPort memberRepo;
    @Mock RefreshTokenRedisRepo rtRepo;
    @Mock PasswordEncoder pe;
    @Mock JwtUtils jwt;

    @InjectMocks MemberCommandService service;

    @BeforeEach
    void setup() { MockitoAnnotations.openMocks(this); }

    @Test
    void registerUser_success() {
        // given
        String id = "test", pw = "pw", name = "이름";
        Role role = Role.USER;
        when(pe.encode(pw)).thenReturn("encodedPw");

        // when
        service.registerUser(id, pw, name, role);

        // then
        ArgumentCaptor<Member> captor = ArgumentCaptor.forClass(Member.class);
        verify(memberRepo).save(captor.capture());
        assertEquals(id, captor.getValue().getLoginId());
        assertEquals("encodedPw", captor.getValue().getPassword());
    }

    @Test
    void login_success() {
        // given
        String id = "test", pw = "pw", encodedPw = "encodedPw";
        Member member = new Member(1L, id, encodedPw, "이름", Role.USER, null);
        when(memberRepo.findByLoginId(id)).thenReturn(Optional.of(member));
        when(pe.matches(pw, encodedPw)).thenReturn(true);
        when(jwt.generateToken(id, "USER")).thenReturn("access");
        when(jwt.refreshToken(id)).thenReturn("refresh");

        // when
        Map<String, String> tokens = service.login(id, pw);

        // then
        assertEquals("access", tokens.get("accessToken"));
        assertEquals("refresh", tokens.get("refreshToken"));
        verify(rtRepo).save(id, "refresh");

        // 로그로 회원 정보와 토큰 정보 출력
        log.info("로그인 성공 - 회원 정보: {}", member);
        log.info("발급된 토큰: {}", tokens);
    }

    @Test
    void login_fail_noPermit() {
        // given
        when(memberRepo.findByLoginId("nope")).thenReturn(Optional.empty());

        // when & then
        assertThrows(RuntimeException.class, () -> service.login("nope", "pw"));
    }

    @Test
    void login_fail_passwordIncorrect() {
        // given
        Member member = new Member(1L, "id", "encoded", "이름", Role.USER, null);
        when(memberRepo.findByLoginId("id")).thenReturn(Optional.of(member));
        when(pe.matches("wrong", "encoded")).thenReturn(false);

        // when & then
        assertThrows(RuntimeException.class, () -> service.login("id", "wrong"));
    }

    @Test
    void logout_정상() {
        // given
        String id = "test";

        // when
        service.logout(id);

        // then
        verify(rtRepo).delete(id);
    }

    @Test
    void registerSocial_success() {
        // given
        SocialRegisterRequest dto = new SocialRegisterRequest();
        dto.setLoginId("social");
        dto.setPassword("pw");
        dto.setName("이름");
        dto.setSocialId("sid");
        dto.setSocialType(SocialType.KAKAO);
        when(pe.encode("pw")).thenReturn("encoded");

        // when
        service.registerSocial(dto);

        // then
        verify(memberRepo).save(any(Member.class));
    }

    @Test
    void disconnectSocial_success() {
        // given
        Member member = mock(Member.class);
        when(memberRepo.findByLoginId("id")).thenReturn(Optional.of(member));

        // when
        service.disconnectSocial("id");

        // then
        verify(member).disconnectSocial();
    }
}
