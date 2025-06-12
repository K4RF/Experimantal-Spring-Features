package batch.project.dto.request;

import batch.project.entity.enums.SocialType;
import lombok.Getter;
import lombok.Setter;

// 소셜 로그인 요청 DTO
@Getter
@Setter
public class SocialLoginRequest {
    private String accessToken;
    private SocialType socialType;
}