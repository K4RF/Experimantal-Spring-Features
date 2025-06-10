package redisCache.project.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CacheResponse { // 비밀번호 등 민감 정보는 포함하지 않음
    private String email;
    private String name;
    private String role;
    private boolean emailVerified;
}
