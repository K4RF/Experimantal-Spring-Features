package jwt.project.dto.request;

import jwt.project.entity.enums.SocialType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SocialRegisterRequest {
    private String loginId;
    private String password;
    private String name;
    private String socialId;
    private SocialType socialType;
}