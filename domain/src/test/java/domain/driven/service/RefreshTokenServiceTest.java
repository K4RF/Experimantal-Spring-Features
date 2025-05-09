package domain.driven.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class RefreshTokenServiceTest {

    @Mock StringRedisTemplate redisTemplate;
    @Mock ValueOperations<String, String> valueOps;
    @InjectMocks RefreshTokenService service;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
    }

    @Test
    void save_success() {
        // given
        String loginId = "id";
        String token = "refresh";

        // when
        service.save(loginId, token);

        // then
        verify(valueOps).set(startsWith("RT:"), eq(token), anyLong(), any());
    }

    @Test
    void find_success() {
        // given
        when(valueOps.get("RT:id")).thenReturn("refresh");

        // when
        String result = service.find("id");

        // then
        assertEquals("refresh", result);
    }

    @Test
    void delete_success() {
        // given
        // when
        service.delete("id");

        // then
        verify(redisTemplate).delete("RT:id");
    }
}
