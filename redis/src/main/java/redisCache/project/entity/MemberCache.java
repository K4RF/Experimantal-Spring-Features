package redisCache.project.entity;

import jakarta.persistence.Id;
import lombok.*;
import org.springframework.data.redis.core.RedisHash;

import java.io.Serializable;

@RedisHash(value = "memberCache", timeToLive = 600) // 10분 TTL
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MemberCache implements Serializable {
    @Id
    private String email;
    private String name;
    private String role;
    private boolean emailVerified;
}
