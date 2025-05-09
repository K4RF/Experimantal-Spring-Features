package domain.driven.service;

import static org.junit.jupiter.api.Assertions.*;

import domain.driven.entity.Member;
import domain.driven.repository.MemberRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.util.Optional;

import static org.mockito.Mockito.*;

class MemberQueryServiceTest {

    @Mock MemberRepositoryPort memberRepo;
    @InjectMocks MemberQueryService service;

    @BeforeEach
    void setup() { MockitoAnnotations.openMocks(this); }

    @Test
    void me_success() {
        // given
        Member member = new Member();
        when(memberRepo.findByLoginId("id")).thenReturn(Optional.of(member));

        // when
        Member result = service.me("id");

        // then
        assertEquals(member, result);
    }

    @Test
    void me_fail_noData() {
        // given
        when(memberRepo.findByLoginId("id")).thenReturn(Optional.empty());

        // when & then
        assertThrows(Exception.class, () -> service.me("id"));
    }
}
