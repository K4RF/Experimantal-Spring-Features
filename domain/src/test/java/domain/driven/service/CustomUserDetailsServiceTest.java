package domain.driven.service;
import domain.driven.entity.Member;
import domain.driven.entity.enums.Role;
import domain.driven.repository.MemberRepository;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CustomUserDetailsServiceTest {

    @Mock
    MemberRepository memberRepository;

    @InjectMocks
    CustomUserDetailsService service;

    public CustomUserDetailsServiceTest() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void loadUserByUsername_정상() {
        // given
        Member member = new Member(1L, "testId", "hashedPw", "홍길동", Role.USER, null);
        when(memberRepository.findByLoginId("testId")).thenReturn(Optional.of(member));

        // when
        UserDetails userDetails = service.loadUserByUsername("testId");

        // then
        assertEquals("testId", userDetails.getUsername());
        assertEquals("hashedPw", userDetails.getPassword());
        assertTrue(userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_USER")));
    }

    @Test
    void loadUserByUsername_실패_회원없음() {
        // given
        when(memberRepository.findByLoginId("notExist")).thenReturn(Optional.empty());

        // when & then
        assertThrows(UsernameNotFoundException.class, () -> service.loadUserByUsername("notExist"));
    }
}