package jwt.project.dto;

import lombok.Getter;
import lombok.Setter;

// 소셜 사용자 정보 DTO
@Getter
@Setter
public class SocialUserInfo {
    private String socialId; // 구글: sub, 카카오: id, 네이버: id 등
    private String email;
    private String name;
}